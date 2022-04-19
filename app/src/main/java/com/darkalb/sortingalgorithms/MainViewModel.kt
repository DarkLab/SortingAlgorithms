package com.darkalb.sortingalgorithms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darkalb.sortingalgorithms.variants.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

enum class Algorithm(val mnemonic: String) {
    BUBBLE_SORT("bub"),
    INSERTION_SORT("ins"),
    COMB_SORTING("comb"),
    QUICK_SORT("qk");

    fun next(): Algorithm {
        return when (this) {
            BUBBLE_SORT -> INSERTION_SORT
            INSERTION_SORT -> COMB_SORTING
            COMB_SORTING -> QUICK_SORT
            QUICK_SORT -> BUBBLE_SORT
        }
    }
}

enum class QUANTITY(val size: Int) {
    SMALL(20),
    MEDIUM(30),
    BIG(40);

    fun next(): QUANTITY {
        return when (this) {
            SMALL -> MEDIUM
            MEDIUM -> BIG
            BIG -> SMALL
        }
    }
}

enum class DURATION(val mnemonic: String, val value: Long) {
    SLOW("1x", 960L),
    MEDIUM("2x", 640L),
    FAST("3x", 320L),
    VERY_FAST("4x", 160L),
    SUPER_FAST("5x", 80L);

    fun next(): DURATION {
        return when (this) {
            SLOW -> MEDIUM
            MEDIUM -> FAST
            FAST -> VERY_FAST
            VERY_FAST -> SUPER_FAST
            SUPER_FAST -> SLOW
        }
    }
}

private val palettes = arrayOf(
    arrayOf("#001E6C", "#E8630A", "#FCD900", "#035397"),
    arrayOf("#8E3200", "#D7A86E", "#FFEBC1", "#A64B2A"),
    arrayOf("#40DFEF", "#FFFBE7", "#E78EA9", "#B9F8D3"),
    arrayOf("#006778", "#00AFC1", "#FFD124", "#0093AB"),
    arrayOf("#139487", "#FFF1CE", "#D29D2B", "#86C6F4")
)

class MainViewModel : ViewModel() {
    private var currentAlgorithmType: Algorithm = Algorithm.BUBBLE_SORT
    private var currentPaletteIndex = 0
    private val currentPalette: Array<String>
        get() = palettes[currentPaletteIndex]
    private var currentQuantity: QUANTITY = QUANTITY.SMALL
    private var basedArray = emptyList<Float>()
    private var currentDuration: DURATION = DURATION.MEDIUM
        set(value) {
            field = value
            interStepDuration = value.value / 3
        }
    private var interStepDuration = currentDuration.value / 3

    private val _uiState: MutableStateFlow<MainUiState> = MutableStateFlow(
        MainUiState.UpdateUiState(
            currentAlgorithmType,
            currentPalette,
            currentDuration
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
                MainUiEvent.READY -> onReady()
                MainUiEvent.CHANGE_UI_SETTINGS -> onChangeUiSettings()
                MainUiEvent.NEW_LIST -> onNewList()
                MainUiEvent.START_SORT -> onStartSort()
                MainUiEvent.CHANGE_ALGORITHM -> onChangeAlgorithm()
                MainUiEvent.CHANGE_SIZE -> onChangeSize()
                MainUiEvent.CHANGE_PALETTE -> onChangePalette()
                MainUiEvent.CHANGE_DURATION -> onChangeDuration()
                MainUiEvent.ANIMATED_STEP_END -> makeNextStep()
            }
        }
    }

    private fun onReady() {
        onNewList()
    }

    private fun onChangeUiSettings(withoutRenderList: Boolean = false) {
        viewModelScope.launch {
            _uiState.emit(
                MainUiState.UpdateUiState(
                    currentAlgorithmType,
                    currentPalette,
                    currentDuration
                )
            )
            if (withoutRenderList) return@launch
            renderCurrentList()
        }
    }

    private fun renderCurrentList() {
        viewModelScope.launch {
            _uiState.emit(MainUiState.RenderList(basedArray))
        }
    }

    private fun onNewList() {
        if (currentJob?.isActive == true) return

        viewModelScope.launch {
            val newList = mutableListOf<Float>()
            with(newList) {
                repeat(currentQuantity.size) {
                    add((it + 1f) / currentQuantity.size)
                }
                shuffle()
            }
            basedArray = newList
            _uiState.emit(MainUiState.RenderList(basedArray))
        }
    }

    private fun onStartSort() {
        if (currentJob?.isActive == true) return

        currentAlgorithm = when (currentAlgorithmType) {
            Algorithm.BUBBLE_SORT -> BubbleSorting(basedArray)
            Algorithm.INSERTION_SORT -> InsertionSorting(basedArray)
            Algorithm.COMB_SORTING -> CombSorting(basedArray)
            Algorithm.QUICK_SORT -> QuickSorting(basedArray)
        }
        currentJob = viewModelScope.launch {
            currentAlgorithm?.execute()?.collect {
                delay(interStepDuration)
                _uiState.emit(
                    MainUiState.AnimateStep(
                        it.oldList,
                        it.newList,
                        it.indexes,
                        currentDuration,
                        it.level
                    )
                )
                waitForStepAnimationEnd()
            }
            _uiState.emit(MainUiState.Congratulation("Поздравляю!!!"))
        }
    }

    private fun onChangeAlgorithm() {
        if (currentJob?.isActive == true) return
        currentAlgorithmType = currentAlgorithmType.next()
        onChangeUiSettings()
    }

    private fun onChangeSize() {
        if (currentJob?.isActive == true) return
        currentQuantity = currentQuantity.next()
        onNewList()
    }

    private fun onChangePalette() {
        if (currentJob?.isActive == true) return
        currentPaletteIndex = (currentPaletteIndex + 1) % palettes.size
        onChangeUiSettings()
    }

    private fun onChangeDuration() {
        currentDuration = currentDuration.next()
        onChangeUiSettings(true)
    }

    private fun makeNextStep() {
        viewModelScope.launch(exceptionHandler) {
            waitingContinuation?.resume(Unit)
        }
    }

    private suspend fun waitForStepAnimationEnd() {
        suspendCoroutine<Unit> {
            waitingContinuation = it
        }
    }
}