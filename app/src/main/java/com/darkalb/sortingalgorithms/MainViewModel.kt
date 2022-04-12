package com.darkalb.sortingalgorithms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

private const val QUANTITY = 30
private const val INTER_STEP_DURATION = 320L
private const val NUMBER_OF_ATTEMPTS = 50

class MainViewModel : ViewModel() {
    private val random = Random(System.currentTimeMillis())
    private val basedArray = mutableListOf<Float>()
    private var count = NUMBER_OF_ATTEMPTS

    private val isInProcess: Boolean
        get() = count < NUMBER_OF_ATTEMPTS

    private val _uiState: MutableStateFlow<MainUiState> = MutableStateFlow(
        MainUiState.StartAction(emptyList())
    )
    val uiState = _uiState.asStateFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _uiState.update {
            MainUiState.Error(throwable.localizedMessage ?: "Error")
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
        if (isInProcess) return

        count = 0
        viewModelScope.launch {
            _uiState.emit(MainUiState.StartSort("Sorting Name"))
        }
        makeNextStep()
    }

    private fun onNextStep() {
        if (isInProcess) {
            makeNextStep()
        }
    }

    private fun makeNextStep() {
        viewModelScope.launch {
            delay(INTER_STEP_DURATION)
            count++
            val randomIndexes = getRandomIndexes()
            val oldNumbers = basedArray.toList()
            swapItems(randomIndexes)
            _uiState.emit(MainUiState.NextStep(oldNumbers, basedArray, randomIndexes))
        }
    }

    private fun swapItems(indexes: List<Indexes>) {
        val temp = basedArray[indexes[0].first]
        basedArray[indexes[0].first] = basedArray[indexes[1].first]
        basedArray[indexes[1].first] = temp
    }

    private fun getRandomIndexes(): List<Indexes> {
        val index1 = random.nextInt(QUANTITY)
        var index2 = index1
        while (index1 == index2) {
            index2 = random.nextInt(QUANTITY)
        }
        return listOf(
            index1 to index2,
            index2 to index1
        )
    }
}