package com.kobbi.util.numbercount.model

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

class CountViewModel {
    private val _count = mutableStateOf(0L)
    private val _locked = mutableStateOf(false)
    private val _checked = mutableStateOf(false)

    val count: State<Long> = _count
    val locked: State<Boolean> = _locked
    val checked: State<Boolean> = _checked

    fun plus() {
        _count.value = _count.value.plus(1)
    }

    fun minus() {
        _count.value = _count.value.let {
            if (it > 0) it.minus(1) else 0
        }
    }

    fun reset() {
        _count.value = 0
    }

    fun setCount(value: Long) {
        _count.value = value
    }

    fun setCount(value: String) {
        value.toLongOrNull()?.let {
            _count.value = if (it >= 0) it else 0
        } ?: kotlin.run { _count.value = 0 }
    }

    fun setLocked(value: Boolean) {
        _locked.value = value
    }

    fun setChecked(value: Boolean) {
        _checked.value = value
    }
}