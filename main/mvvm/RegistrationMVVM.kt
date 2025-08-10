package mvvm

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import utils.VMState

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

// catatan tambahan:

// 1. DELAYED INTENTION FLAG
val isUserWantToRegister = mutableStateOf(false)

// Praktik yang umum digunakan untuk memudahkan prediksi intensi user ketika intensi tertunda oleh prosedur.
//
// Jika prosedur bertambah dan flag ini lupa di tangani maka flag ini tidak dapat di percaya.
// Jika prosedur berubah tanpa melakukan penyesuaian pada flag handling di setiap langkah prosedur,
// maka flag ini tidak dapat dipercaya.
//
// Karena pada hakikatnya, flag ini bukan untuk mendelegasikan intensi, namun hanya alat bagi kontroller untuk
// membantu proses prediksi intensi.
// Flag ini tidak dapat di percaya, hanya kontroller yang dapat di percaya.
// Akan tetapi, dimana kontroller tersebut berada(?)
// Dalam declarative programming, tidak ada konsep kontroller.
// Dalam imperative programming, umumnya activity itu sendirilah kontrollernya;
// Maka akan menjadi dan masalah volatility terutama ketika satu screen memiliki banyak fitur.

// 2. Tidak ada agregasi state dan state controller
//    Pemulihan state dan proses mempertahankan state bersama UI lifecycle akan sangat sulit.
//    Perlu mereduksi data state menjadi ui state di UI sehingga menyebabkan banyak internal reduction di setiap layer composable function.


@Preview
@Composable
private fun Preview() {
    val tncResult = remember { mutableStateOf<Result<Any>?>(null) }
    val context = LocalContext.current

    Registration(
        tncResult = tncResult.value,
        gotoRegistrationTNC = {
            // dummy just agree
            tncResult.value = Result.success(Unit)
        },
        goToRegistrationForm = {
            Toast.makeText(context, "Go to Registration Form", Toast.LENGTH_SHORT).show()
        }
    )
}
