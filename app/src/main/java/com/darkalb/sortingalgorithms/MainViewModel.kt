package com.darkalb.sortingalgorithms

import android.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darkalb.sortingalgorithms.variants.BubbleSorting
import com.darkalb.sortingalgorithms.variants.InsertionSorting
import com.darkalb.sortingalgorithms.variants.SortingAlgorithm
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val QUANTITY = 20
private const val STEP_DURATION = 600L
private const val INTER_STEP_DURATION = 160L

enum class Algorithm {
    BUBBLE_SORT,
    INSERTION_SORT
}

private val defaultPalette = arrayOf("#D06224", "#8A8635", "#E9C891")
private val palettes = mapOf(
    "1" to arrayOf("#001E6C", "#E8630A", "#FCD900"),
    "2" to arrayOf("#8E3200", "#D7A86E", "#FFEBC1"),
    "3" to arrayOf("#40DFEF", "#FFFBE7", "#E78EA9"),
    "4" to arrayOf("#006778", "#00AFC1", "#FFD124"),
    "5" to arrayOf("#139487", "#FFF1CE", "#D29D2B")
)

class MainViewModel : ViewModel() {
    private var basedArray = emptyList<Float>()
    private val palette: Array<String> = palettes["1"] ?: defaultPalette

    private val _uiState: MutableStateFlow<MainUiState> = MutableStateFlow(
        MainUiState.UpdateUiState(
            Color.parseColor(palette[0]),
            Color.parseColor(palette[1]),
            Color.parseColor(palette[2])
        )
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
                MainUiEvent.CHANGE_UI_SETTINGS -> onChangeUiSettings()
                MainUiEvent.NEW_LIST -> onNewList()
                MainUiEvent.START_SORT -> onStartSort()
                MainUiEvent.ANIMATED_STEP_END -> makeNextStep()
            }
        }
    }

    private fun onChangeUiSettings() {
        viewModelScope.launch {
        }
    }

    private fun onNewList() {
        if (currentJob?.isActive == true) return

        viewModelScope.launch {
            _uiState.emit(MainUiState.RenderList(emptyList()))
            val newList = mutableListOf<Float>()
            with(newList) {
                repeat(QUANTITY) {
                    add((it + 1f) / QUANTITY)
                }
                shuffle()
            }
            basedArray = newList
            _uiState.emit(MainUiState.RenderList(basedArray))
        }
    }

    private fun onStartSort(algorithm: Algorithm = Algorithm.INSERTION_SORT) {
        if (currentJob?.isActive == true) return

        currentAlgorithm = when (algorithm) {
            Algorithm.BUBBLE_SORT -> BubbleSorting(basedArray)
            Algorithm.INSERTION_SORT -> InsertionSorting(basedArray)
        }
        currentJob = viewModelScope.launch {
            currentAlgorithm?.execute()?.collect {
                delay(INTER_STEP_DURATION)
                _uiState.emit(
                    MainUiState.AnimateStep(
                        it.oldList,
                        it.newList,
                        it.indexes,
                        STEP_DURATION
                    )
                )
                waitForStepAnimationEnd()
            }
        }
    }

    private fun makeNextStep() {
        viewModelScope.launch {
            waitingContinuation?.resume(Unit)
        }
    }

    private suspend fun waitForStepAnimationEnd() {
        suspendCoroutine<Unit> {
            waitingContinuation = it
        }
    }
}