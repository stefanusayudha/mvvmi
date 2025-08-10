package com.singularityuniverse.mvvmi

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
import kotlinx.coroutines.launch

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

@Preview
@Composable
private fun Preview() {
    val context = LocalContext.current
    val newIntent = remember { mutableStateOf<RegistrationIntent?>(null) }

    Registration(
        intent = newIntent.value,
        onIntent = { intent ->
            when (intent) {
                is RegistrationIntent.DoRegistrationTNC -> {
                    // dummy, just agree
                    newIntent.value = intent.data.copy(
                        tncResult = Unit
                    )
                }

                is RegistrationIntent.GoToRegistrationForm -> {
                    Toast.makeText(context, "Go to Registration Form", Toast.LENGTH_SHORT).show()
                }

                else -> {}
            }
        },

        onHandled = {
            newIntent.value = null
        },
    )
}