package mvvm

import RegistrationInteractor
import RegistrationInteractorImpl
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import utils.VMState
import utils.initIdle
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Contoh viewmodel pada pattern mvvm murni
//
// Jika anda bertanya kenapa tidak meng agregasi uistate dan menyimpannya di viewmodel
// Jawaban:
// Menempatkan uistate pada viewmodel adalah sebuah antipattern menurut mvvm
// Kita tidak di ijinkan untuk menyimpan ui state di viewmodel
// Karena fungsi viewmodel adalah decoupler antara ui dan model

// Jika anda bertanya kenapa tidak melakukan agregasi ui state dan menyimpannya di composable function
// Jawaban:
// akan timbul banyak masalah:
// 1. Pertlu hoisting state dan state controller atau state reductor untuk mereduksi data menjadi uistate
//    Kerumitan akan terjadi karena tidak ada standard yang mendukung mekanisme itu saat ini.
// 2. Perlu memastikan state terupdate secara atomic dan meningkatkan kompleksitas
// 3. Melakukan reduksi pada composable function akan membebani main thread.
//    Perlu mekanisme pengamanan mainthread, sayangnya, tidak ada standard yang mendukung mekanisme itu saat ini.

class RegistrationViewModel(
    private val interactor: RegistrationInteractor = RegistrationInteractorImpl()
) : ViewModel() {
    val eligibility: StateFlow<VMState<Any>>
        field = initIdle()

    fun getEligibility() {
        eligibility.update { VMState.Loading }
        viewModelScope.launch {
            val result = interactor.getEligibility()
                .fold(
                    onSuccess = {
                        VMState.Success(it)
                    },
                    onFailure = {
                        VMState.Failed(Exception(it))
                    }
                )
            eligibility.update { result }
        }
    }

    val userInfo: StateFlow<VMState<Any>>
        field = initIdle()

    fun getUserInfo() {
        userInfo.update { VMState.Loading }
        viewModelScope.launch {
            val result = interactor.getUserInfo()
                .fold(
                    onSuccess = {
                        VMState.Success(it)
                    },
                    onFailure = {
                        VMState.Failed(Exception(it))
                    }
                )
            userInfo.update { result }
        }
    }

    val registrationInquiry: StateFlow<VMState<Any>>
        field = initIdle()

    fun inquiryRegistration() {
        registrationInquiry.update { VMState.Loading }
        viewModelScope.launch {
            val result = interactor.getRegistrationInquiry()
                .fold(
                    onSuccess = {
                        VMState.Success(it)
                    },
                    onFailure = {
                        VMState.Failed(Exception(it))
                    }
                )
            registrationInquiry.update { result }
        }
    }
}