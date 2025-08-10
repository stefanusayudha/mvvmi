import kotlinx.coroutines.delay

interface RegistrationInteractor {
    suspend fun getEligibility(): Result<Any>
    suspend fun getUserInfo(): Result<Any>
    suspend fun getRegistrationInquiry(): Result<Any>
}

internal class RegistrationInteractorImpl : RegistrationInteractor {
    override suspend fun getEligibility(): Result<Any> {
        // dummy model call
        delay(1000)
        return Result.success(Unit)
    }

    override suspend fun getUserInfo(): Result<Any> {
        // dummy model call
        delay(1000)
        return Result.success(Unit)
    }

    override suspend fun getRegistrationInquiry(): Result<Any> {
        // dummy model call
        delay(1000)
        return Result.success(Unit)
    }
}