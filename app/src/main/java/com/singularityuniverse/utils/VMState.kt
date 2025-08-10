package com.singularityuniverse.utils

import kotlinx.coroutines.flow.MutableStateFlow

sealed class VMState<out T> {
    data class Success<T>(val data: T) : VMState<T>()
    data class Failed(val e: Exception) : VMState<Nothing>()
    data object Loading : VMState<Nothing>()
    data object Idle : VMState<Nothing>()
}

fun idle() = VMState.Idle
fun <T> initIdle() = MutableStateFlow<VMState<T>>(idle())
fun <T> initDefault(default: VMState<T>) = MutableStateFlow<VMState<T>>(default)

fun <T> success(data: T) = VMState.Success(data)
