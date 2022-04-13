package com.darkalb.sortingalgorithms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darkalb.sortingalgorithms.variants.BubbleSorting
import com.darkalb.sortingalgorithms.variants.SortingAlgorithm
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val QUANTITY = 10
private const val STEP_DURATION = 600L
private const val INTER_STEP_DURATION = 160L

class MainViewModel : ViewModel() {
    private val basedArray = mutableListOf<Float>()

    private val _uiState: MutableStateFlow<MainUiState> = MutableStateFlow(
        MainUiState.StartAction(emptyList())
    )
    val uiState = _uiState.asStateFlow()

    private var currentJob: Job? = null
    private var waitingContinuation: Continuation<Unit>? = null
    private var currentAlgorithm: SortingAlgorithm? = null

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        viewModelScope.launch {
            _uiState.emit(MainUiState.Error(throwable.localizedMessage ?: "Error"))
        }
    }

    fun onEvent(event: MainUiEvent) {
        viewModelScope.launch(exceptionHandler) {
            when (event) {
                MainUiEvent.START_ACTION -> onStartAction()
                MainUiEvent.START_SORT -> onStartSort()
                MainUiEvent.NEXT_STEP -> onNextStep()
            }
        }
    }

    private fun onStartAction() {
        viewModelScope.launch {
            with(basedArray) {
                clear()
                repeat(QUANTITY) {
                    add((it + 1f) / QUANTITY)
                }
                shuffle()
            }
            _uiState.emit(MainUiState.StartAction(basedArray))
        }
    }

    private fun onStartSort() {
        if (currentJob?.isActive == true) return

        currentAlgorithm = BubbleSorting(basedArray)
        currentJob = viewModelScope.launch {
            currentAlgorithm?.execute()?.collect {
                delay(INTER_STEP_DURATION)
                _uiState.emit(
                    MainUiState.NextStep(
                        it.oldList,
                        it.newList,
                        it.indexes,
                        STEP_DURATION
                    )
                )
                waitForAnimationEnd()
            }
        }
    }

    private fun onNextStep() {
        viewModelScope.launch {
            waitingContinuation?.resume(Unit)
        }
    }

    private suspend fun waitForAnimationEnd() {
        suspendCoroutine<Unit> {
            waitingContinuation = it
        }
    }
}