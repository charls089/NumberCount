package com.kobbi.util.numbercount.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class NumberViewModel : ViewModel() {
    private val _counts = mutableStateListOf(CountViewModel())
    private val _editMode: MutableState<Boolean> = mutableStateOf(false)

    val counts: List<CountViewModel> = _counts
    val editMode: State<Boolean> = _editMode

    fun add(count: Long) {
        _counts.add(CountViewModel().apply { setCount(count) })
    }

    fun remove() {
        _counts.removeAll { it.checked.value }
        if (_counts.size == 0 && _editMode.value) {
            _editMode.value = false
        }
    }

    fun lock() {
        _counts.forEach {
            if (it.checked.value)
                it.setLocked(true)
        }
    }

    fun unlock() {
        _counts.forEach {
            if (it.checked.value)
                it.setLocked(false)
        }
    }

    fun setEditMode() {
        _editMode.value = _editMode.value.not()
        checkedAll(false)
    }

    fun isCheckedAll(): Boolean {
        return _counts.all { it.checked.value }
    }

    fun hasChecked(): Boolean {
        return _counts.any { it.checked.value }
    }

    fun checkedAll(value: Boolean) {
        _counts.forEach {
            it.setChecked(value)
        }
    }
}