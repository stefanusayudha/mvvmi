package com.singularityuniverse.mvvmi

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class RegistrationViewModel : ViewModel(), RegistrationInteractor by RegistrationInteractorImpl() {

    val uiState: StateFlow<UISTate>
        field = MutableStateFlow(UISTate())

    val intent: StateFlow<RegistrationIntent?>
        field = MutableStateFlow<RegistrationIntent?>(null)

    fun onHandled() {
        intent.update { null }
    }

    suspend fun onIntent(intent: RegistrationIntent) {
        when (intent) {
            is RegistrationIntent.Registration -> {
                onRegistrationIntent(intent)
            }

            else -> {

            }
        }
    }

    suspend fun onRegistrationIntent(data: RegistrationIntent.Registration) {

        // reduce ui state
        uiState.update {
            it.copy(
                isSubmitButtonEnabled = false,
                isShowSubmitButtonLoading = true
            )
        }

        // procedure 1: check for eligibility
        requireNotNull(data.eligibilityData) {
            val eligibilityData = withContext(Dispatchers.IO) { getEligibility() }

            // handle failure
            eligibilityData.onFailure { e ->
                onRegistrationFailed(e, data)
            }

            // loop back to reprocess
            eligibilityData.onSuccess {
                onRegistrationIntent(data.copy(eligibilityData = it))
            }

            return
        }

        // check for eligibility in real scenario
        // require(data.eligibilityData.isEligible == true) {
        //
        //     // reduce ui state
        //     uiState.update {
        //         it.copy(
        //             isSubmitButtonEnabled = true,
        //             isShowSubmitButtonLoading = false
        //         )
        //     }
        //
        //     // propagate feeding back intent with result
        //     intent.update {
        //         data.copy(
        //             intentResult = Result.failure(InEligibleException)
        //         )
        //     }
        //
        //     return
        // }

        // procedure 2: check for tnc
        requireNotNull(data.tncResult) {
            // cannot handle this, feeding back to sender
            intent.update {
                RegistrationIntent.DoRegistrationTNC(data)
            }
            return
        }

        // procedure 3: check for user info
        requireNotNull(data.userInfo) {
            val userInfo = withContext(Dispatchers.IO) { getUserInfo() }

            // handle failure
            userInfo.onFailure { e ->
                onRegistrationFailed(e, data)
            }

            // loop back to reprocess
            userInfo.onSuccess {
                onRegistrationIntent(data.copy(userInfo = it))
            }

            return
        }

        // procedure 4 : check for registrationInquiry then go to form
        requireNotNull(data.registrationInquiry) {
            val inquiryData = withContext(Dispatchers.IO) { getRegistrationInquiry() }

            // handle failure
            inquiryData.onFailure { e ->
                onRegistrationFailed(e, data)
            }

            // handle success
            // return GoToRegistration Form
            inquiryData.onSuccess { inquiryData ->
                onRegistrationIntent(data.copy(registrationInquiry = inquiryData))
            }

            return
        }

        // finally all procedure is passed
        // dismiss loading
        uiState.update {
            it.copy(
                isSubmitButtonEnabled = true,
                isShowSubmitButtonLoading = false
            )
        }

        // send new intent to client to continue procedure
        intent.update {
            RegistrationIntent.GoToRegistrationForm(data)
        }
    }

    private fun onRegistrationFailed(e: Throwable, data: RegistrationIntent.Registration) {
        // reduce ui state
        uiState.update {
            it.copy(
                isSubmitButtonEnabled = true,
                isShowSubmitButtonLoading = false
            )
        }

        // propagate feeding back intent with result
        intent.update {
            data.copy(
                intentResult = Result.failure(e)
            )
        }
    }
}
