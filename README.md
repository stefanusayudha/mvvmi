# Side to Side Comparison

## Screen
<details>
  
**MVVM**
Screen yang Bulky.
Reduksi dimana-mana.
Prosedur yang tidak terdefinisi dengan jelas.

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Registration(
    tncResult: Result<Any>? = null,
    gotoRegistrationTNC: () -> Unit = {},
    goToRegistrationForm: () -> Unit = {}
) {
    // data state
    val viewModel = viewModel { RegistrationViewModel() }
    val eligibilityState by viewModel.eligibility.collectAsStateWithLifecycle()
    val userInfoState by viewModel.userInfo.collectAsStateWithLifecycle()
    val registrationInquiry by viewModel.registrationInquiry.collectAsStateWithLifecycle()

    // ui state
    val isLoading by produceState(false, eligibilityState, userInfoState, registrationInquiry) {
        value = listOf(eligibilityState, userInfoState, registrationInquiry).any { it is VMState.Loading }
    }

    // note: TIDAK ADA GARANSI LAUNCH EFFECT AKAN TERLOKALISASI
    // serta TIDAK ADA GARANSI bahwa URUTAN LAUNCH EFFECT AKAN SESUAI URUTAN PROSEDUR

    LaunchedEffect(registrationInquiry) {
        when (registrationInquiry) {
            is VMState.Success -> {
                // data tidak dapat mengatakan intensi user,
                // oleh karena itu, pattern recognition / prediction perlu di lakukan untuk meyimpulkan keinginan user.

                // melakukan prediksi intensi user (dummy)
                // ... done

                // kesimpulan = user ingin melakukan registrasi
                // menjalankan prosedur ke 5

                goToRegistrationForm.invoke()
            }

            else -> {

            }
        }
    }

    LaunchedEffect(eligibilityState) {
        when (eligibilityState) {
            is VMState.Success<*> -> {
                // data tidak dapat mengatakan intensi user,
                // oleh karena itu, pattern recognition / prediction perlu di lakukan untuk meyimpulkan keinginan user.

                // melakukan prediksi intensi user (dummy)
                // ... done

                // kesimpulan = user ingin melakukan registrasi
                // menjalankan prosedur ke 2

                gotoRegistrationTNC.invoke()
            }

            else -> {}
        }
    }

    // SEBUAH PROSEDUR YANG MERUSAK LOKALISASI LAUNCH EFEK
    val sebuahReduksiYangSangatPentingDanHarusBeradaDisini = remember { derivedStateOf { Unit } }
    val sebuahReduksiYangSangatPentingDanHarusBeradaDisini2 = remember { derivedStateOf { Unit } }
    val sebuahReduksiYangSangatPentingDanHarusBeradaDisini3 = remember { derivedStateOf { Unit } }
    val sebuahReduksiYangSangatPentingDanHarusBeradaDisini4 = remember { derivedStateOf { Unit } }

    LaunchedEffect(userInfoState) {
        when (userInfoState) {
            is VMState.Success -> {
                // data tidak dapat mengatakan intensi user,
                // oleh karena itu, pattern recognition / prediction perlu di lakukan untuk meyimpulkan keinginan user.

                // melakukan prediksi intensi user (dummy)
                // ... done

                // kesimpulan = user ingin melakukan registrasi
                // menjalankan prosedur ke 4

                viewModel.inquiryRegistration()
            }

            else -> {

            }
        }
    }

    LaunchedEffect(Unit) {
        // SEBUAH LAUNCH EFFEK YANG TIDAK RELEVAN
        // YANG KARENA BANYAK FAKTOR BERADA DISINI
    }

    LaunchedEffect(Unit) {
        // SEBUAH LAUNCH EFFEK YANG TIDAK RELEVAN
        // YANG KARENA BANYAK FAKTOR BERADA DISINI
    }

    LaunchedEffect(tncResult) {
        when {
            tncResult?.isSuccess == true -> {
                // data tidak dapat mengatakan intensi user,
                // oleh karena itu, pattern recognition / prediction perlu di lakukan untuk meyimpulkan keinginan user.

                // melakukan prediksi intensi user (dummy)
                // ... done

                // kesimpulan = user ingin melakukan registrasi
                // menjalankan prosedur ke 3

                viewModel.getUserInfo()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Registration")
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Button(
                modifier = Modifier.align(Alignment.Center),
                enabled = !isLoading,
                onClick = {
                    // UI tidak mengatakan intensi user
                    // procedure invocation 1 get eligibility

                    // lihat pada catatan tambahan
                    // isUserWantToRegister.value = true
                    viewModel.getEligibility()
                }
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    Text("Registration")
                }
            }
        }
    }
}
```

**MVVMI**
Screen lebih sederhana dan proses handling yang indentikal memungkinkan ekstraksi lebih jauh jika diperlukan.
Tidak ada reduksi internal, menghilangkan sepenuhnya ketergantungan UI dengan domain entity.
Mengurangi variant dari komponent ui dengan signifikan.

```kotlin
internal data class UISTate(
    val isSubmitButtonEnabled: Boolean = true,
    val isShowSubmitButtonLoading: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Registration(
    intent: RegistrationIntent? = null,
    onIntent: (RegistrationIntent) -> Unit = {},
    onHandled: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val viewModel = viewModel { RegistrationViewModel() }
    val uisTate by viewModel.uiState.collectAsStateWithLifecycle()

    // new incoming intent
    LaunchedEffect(intent) {
        when (intent) {
            is RegistrationIntent.Registration -> {
                viewModel.onIntent(intent)
                onHandled.invoke()
            }

            else -> {}
        }
    }

    // feed back intent from viewmodel
    val viewModelIntent by viewModel.intent.collectAsStateWithLifecycle()

    LaunchedEffect(viewModelIntent) {
        val intent = viewModelIntent

        when (intent) {
            is RegistrationIntent.DoRegistrationTNC,
            is RegistrationIntent.GoToRegistrationForm -> {
                onIntent.invoke(intent)
                viewModel.onHandled()
            }

            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Registration")
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Button(
                modifier = Modifier.align(Alignment.Center),
                enabled = uisTate.isSubmitButtonEnabled,
                onClick = {
                    scope.launch {
                        viewModel.onIntent(RegistrationIntent.Registration())
                    }
                }
            ) {
                if (uisTate.isShowSubmitButtonLoading) {
                    CircularProgressIndicator()
                } else {
                    Text("Registration")
                }
            }
        }
    }
}
```

</details>

## ViewModel

<details>

**MVVM**
Viewmodel lebih sederhana.

note: Dalam pattern MVVM; Viewmodel tidak boleh menyimpan UIState(pada prakteknya sering dilakukan), tidak boleh mengontrol UI (pada prakteknya sering dilakukan), dan tidak boleh mengandung prosedur UI (pada prakteknya sering dilakukan).

```kotlin
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
```

**MVVMI**
Viewmodel menghandle prosedur (bukan mengontrol) dan hoisting ui state (opsional).
Reduksi Data API didelegasikan pada Interaktor.

```kotlin
class RegistrationViewModel : ViewModel(), RegistrationInteractor by RegistrationInteractorImpl() {

    internal val uiState: StateFlow<UISTate>
        field = MutableStateFlow(UISTate())

    internal val intent: StateFlow<RegistrationIntent?>
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
```
</details>

## Procedure
<details>

**MVVM**
Publisher-Subscriber Pattern membuat proses dari sebuah prosedur tersebar dan tidak terlokalisasi.

```kotlin
    // data state
    val viewModel = viewModel { RegistrationViewModel() }
    val eligibilityState by viewModel.eligibility.collectAsStateWithLifecycle()
    val userInfoState by viewModel.userInfo.collectAsStateWithLifecycle()
    val registrationInquiry by viewModel.registrationInquiry.collectAsStateWithLifecycle()

    // ui state
    val isLoading by produceState(false, eligibilityState, userInfoState, registrationInquiry) {
        value = listOf(eligibilityState, userInfoState, registrationInquiry).any { it is VMState.Loading }
    }

    // note: TIDAK ADA GARANSI LAUNCH EFFECT AKAN TERLOKALISASI
    // serta TIDAK ADA GARANSI bahwa URUTAN LAUNCH EFFECT AKAN SESUAI URUTAN PROSEDUR

    LaunchedEffect(registrationInquiry) {
        when (registrationInquiry) {
            is VMState.Success -> {
                // data tidak dapat mengatakan intensi user,
                // oleh karena itu, pattern recognition / prediction perlu di lakukan untuk meyimpulkan keinginan user.

                // melakukan prediksi intensi user (dummy)
                // ... done

                // kesimpulan = user ingin melakukan registrasi
                // menjalankan prosedur ke 5

                goToRegistrationForm.invoke()
            }

            else -> {

            }
        }
    }

    LaunchedEffect(eligibilityState) {
        when (eligibilityState) {
            is VMState.Success<*> -> {
                // data tidak dapat mengatakan intensi user,
                // oleh karena itu, pattern recognition / prediction perlu di lakukan untuk meyimpulkan keinginan user.

                // melakukan prediksi intensi user (dummy)
                // ... done

                // kesimpulan = user ingin melakukan registrasi
                // menjalankan prosedur ke 2

                gotoRegistrationTNC.invoke()
            }

            else -> {}
        }
    }

    // SEBUAH PROSEDUR YANG MERUSAK LOKALISASI LAUNCH EFEK
    val sebuahReduksiYangSangatPentingDanHarusBeradaDisini = remember { derivedStateOf { Unit } }
    val sebuahReduksiYangSangatPentingDanHarusBeradaDisini2 = remember { derivedStateOf { Unit } }
    val sebuahReduksiYangSangatPentingDanHarusBeradaDisini3 = remember { derivedStateOf { Unit } }
    val sebuahReduksiYangSangatPentingDanHarusBeradaDisini4 = remember { derivedStateOf { Unit } }

    LaunchedEffect(userInfoState) {
        when (userInfoState) {
            is VMState.Success -> {
                // data tidak dapat mengatakan intensi user,
                // oleh karena itu, pattern recognition / prediction perlu di lakukan untuk meyimpulkan keinginan user.

                // melakukan prediksi intensi user (dummy)
                // ... done

                // kesimpulan = user ingin melakukan registrasi
                // menjalankan prosedur ke 4

                viewModel.inquiryRegistration()
            }

            else -> {

            }
        }
    }

    LaunchedEffect(Unit) {
        // SEBUAH LAUNCH EFFEK YANG TIDAK RELEVAN
        // YANG KARENA BANYAK FAKTOR BERADA DISINI
    }

    LaunchedEffect(Unit) {
        // SEBUAH LAUNCH EFFEK YANG TIDAK RELEVAN
        // YANG KARENA BANYAK FAKTOR BERADA DISINI
    }

    LaunchedEffect(tncResult) {
        when {
            tncResult?.isSuccess == true -> {
                // data tidak dapat mengatakan intensi user,
                // oleh karena itu, pattern recognition / prediction perlu di lakukan untuk meyimpulkan keinginan user.

                // melakukan prediksi intensi user (dummy)
                // ... done

                // kesimpulan = user ingin melakukan registrasi
                // menjalankan prosedur ke 3

                viewModel.getUserInfo()
            }
        }
    }

```

**MVVMI**

Prosedur diproses dalam 1 method, minim transaksi screen-viewmodel yang tidak perlu
```kotlin
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

```

</details>

## Kelebihan dak kekurangan
<details>

**MVVM**
Kelebihan
- Lebih sedikit kode pada viewmodel.
- Lebih sedikit objek essensial (+ lebih sedikit membebani effort pengkodean).
- Pada dasarnya Tidak memerlukan interaktor (viewmodel langsung bekerja sebagai decoupler view dan model, dan reducer dari Data Layer API).
- viewmodel lebih sederhana.
- menerapkan prinsib viewmodel sesuai design dari ViewModel object (sebagai decoupler view dan model) menggunakan Publisher-Subscriber Pattern.
- Mudah dibuat dan dipahami oleh developer pemula.

Kekurangan
- Tidak ada yang bisa mengatakan intensi dari proses yang sedang berjalan.
- Kontrol UI bersifat Implicit (Di dalam UI dan tidak bisa dipisahkan atau diinterupsi tanpa memodifikasi ui itu sendiri).
- Reduksi data menjadi UI state terjadi dimana-mana (tersebar) membuat ketergantungan berat pada domain entity.
- **Sulit dimaintain.** Publisher-Subscriber Pattern membuat Prosedur tersebar pada UI/Activity tanpa definisi dan kontrak yang jelas.
- Prosedur tidak sekuensial, bolak-balik, tersebar, dan tidak ada garansi urutan yang bisa dipahami sekali lihat. Dalam kasus Jetpack compose, prosedural call sangatlah rumit, karena urutan prosedur akan mempengaruhi lifecycle dari composable object.
- Pembatalan prosedur mungkin sangat rumit dan harus memperhatikan baik2 semua edgcase, flags, dan buffer terhandle dengan benar di setiap step dari prosedur.
- UI cenderung Bulky dan kompleks.
- Volatilitas tinggi.
- Resiko perubahan prosedur tinggi akibat tidak adanya definisi prosedur yang jelas, dan volatilitas tinggi.
- Mutabilitias tinggi. Publisher yang volatile jika tidak dihandle dengan sangat baik.
- Kemungkinan besar perlu flag-flag untuk membantu prosedur mendefinisikan intensi dan mengamankan step pada prosedur.
- Tidak ada bagian kode yang bisa mengatakan apa proses yang sedang terjadi dan apa intensi dari proses. Misal ketika ui mengdapatkan data dari viewmodel data Publisher, ui tidak bisa secara langsung mengatakan untuk apa data itu, dan data itu sendiri juga tidak bisa mengatakannya. Serangkaian proses diperlukan untuk menentukan apa intensi dari data itu dan apa proses selanjutnya.

**MVVMI**
Kelebihan
- (+) Lebih Robust, pemisahan tanggung jawab yang lebih baik.
- Intent object dapat merepresentasikan intensi, membawa data, mendefinisikan abstrak prosedur, dan hasil dari intensi itu sendiri,
  - Flagging tidak di perlukan.
  - Prosedur terdefinisi dengan baik karena Intent objek membawa kontrak abstrak dari prosedur.
  - State dari prosedur terdefinisi dengan jelas.
  - Prosedur mudah dibatalkan.
  - Kontrol prosedur terlokalisasi dengan lebih baik.
  - Men-suspend, membatalkan, melanjutkan kembali, memindahkan/mendelegasikan prosedur mudah dilakukan.
  - Modifikasi prosedur yang lebih mudah dan minim side effect.
  - Prosedur bahkan bisa di transfer kemana saja dengan mudah dengan mendelegasikan intent objek.
- **Lebih mudah di maintain.**
- Kontrol dari UI bersifat Explicit (Diluar UI, bisa didelegasikan (hoisting), diintervensi, dengan mudah)
- Interaktor memiliki makna yang lebih kuat (Pada mvvm reduksi API terjadi di 2 tempat, yaitu viewmodel dan interaktor).
- Explicit State UI. Dapat didelagasikan / dihoisting dengan mudah.
- Reduksi Model data menjadi UIState terjadi diluar UI (bisa di viewmodel atau di kontroller).
- Mutabilitas rendah. Karena menggunakan prinsib reduksi dan seluruh flow bersifat immutable.
- Volatilitas rendah. Kita tidak mengubah UI state (Mutate), akan tetapi mengeluarkan UIstate baru (Emmit). Tidak merubah variable tapi melakukan reduksi + emit.

Kekurangan
- (-) Lebih Robust. Lebih banyak objek esensial.
- Paradigma yang belum umum. Mungkin perlu penyesuaian mindset dari Imperative ke Declarative. Menciptakan kesan kompleksitas tinggi bagi yang belum terbiasa.
</details>
