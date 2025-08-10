package mvvmi

sealed class RegistrationIntent {
    data class Registration(
        val eligibilityData: Any? = null,
        val tncResult: Any? = null,
        val registrationInquiry: Any? = null,
        val userInfo: Any? = null,
        val intentResult: Result<Any>? = null,
    ) : RegistrationIntent()

    data class DoRegistrationTNC(
        val data: Registration,
        val tncResult: Result<Any>? = null
    ) : RegistrationIntent()

    data class GoToRegistrationForm(
        val data: Registration
    ) : RegistrationIntent()
}