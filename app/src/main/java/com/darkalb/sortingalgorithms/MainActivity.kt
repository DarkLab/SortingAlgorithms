package com.darkalb.sortingalgorithms

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.graphics.applyCanvas
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.darkalb.sortingalgorithms.databinding.ActivityMainBinding
import com.darkalb.sortingalgorithms.enteties.AnimatedData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

typealias Indexes = Pair<Int, Int>

enum class MainUiEvent {
    READY,
    CHANGE_UI_SETTINGS,
    NEW_LIST,
    START_SORT,
    CHANGE_ALGORITHM,
    CHANGE_SIZE,
    CHANGE_PALETTE,
    CHANGE_DURATION,
    ANIMATED_STEP_END
}

sealed class MainUiState {
    data class UpdateUiState(
        val algorithm: Algorithm,
        val palette: Array<String>,
        val duration: DURATION
    ) : MainUiState() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as UpdateUiState

            if (algorithm != other.algorithm) return false
            if (!palette.contentEquals(other.palette)) return false
            if (duration != other.duration) return false

            return true
        }

        override fun hashCode(): Int {
            var result = algorithm.hashCode()
            result = 31 * result + palette.contentHashCode()
            result = 31 * result + duration.hashCode()
            return result
        }
    }

    data class RenderList(val numbers: List<Float>) : MainUiState()
    data class AnimateStep(
        val numbers: List<Float>,
        val newNumbers: List<Float>,
        val datas: List<Indexes>,
        val duration: DURATION
    ) : MainUiState()

    data class Congratulation(val message: String) : MainUiState()
    data class Error(val message: String) : MainUiState()
}

class MainActivity : AppCompatActivity() {

    private var _backgroundColor = 0
    private var _staticColor = 0
        set(value) {
            field = value
            paint.color = value
        }
    private var _dynamicColor = 0
    private val paint = Paint().apply {
        color = _staticColor
    }
    private val emptySpace = 5

    private var itemXSpace = 0
    private var itemWidth = 0
    private var freeLeftXSpace = 0f

    private lateinit var binding: ActivityMainBinding
    private val viewModel = MainViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initialize()
    }

    private fun initialize() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    applyCurrentState(it)
                }
            }
        }
        binding.mainContainer.post {
            viewModel.onEvent(MainUiEvent.READY)
        }
        binding.start.setOnClickListener {
            viewModel.onEvent(MainUiEvent.START_SORT)
        }
        binding.refresh.setOnClickListener {
            viewModel.onEvent(MainUiEvent.NEW_LIST)
        }
        binding.typeTV.setOnClickListener {
            viewModel.onEvent(MainUiEvent.CHANGE_ALGORITHM)
        }
        binding.sizeTV.setOnClickListener {
            viewModel.onEvent(MainUiEvent.CHANGE_SIZE)
        }
        binding.paletteIV.setOnClickListener {
            viewModel.onEvent(MainUiEvent.CHANGE_PALETTE)
        }
        binding.durationTv.setOnClickListener {
            viewModel.onEvent(MainUiEvent.CHANGE_DURATION)
        }
    }

    private fun renderView(numbers: List<Float>) {
        if (numbers.isEmpty()) return

        binding.mainContainer.apply {
            val (w, h) = width to height
            val quantity = numbers.size

            itemXSpace = w / quantity
            itemWidth = itemXSpace - 2 * emptySpace
            val allItemsXSpace = itemXSpace * quantity
            freeLeftXSpace = (w - allItemsXSpace) / 2f

            background = BitmapDrawable(
                resources,
                generateDrawable(w, h, freeLeftXSpace, itemXSpace, numbers, emptyArray())
            )
        }
    }

    private fun animateStep(
        freeLeftXSpace: Float,
        itemXSpace: Int,
        numbers: List<Float>,
        newNumbers: List<Float>,
        animatedDatas: Array<AnimatedData>,
        stepDuration: Long,
        container: ViewGroup
    ) {
        animatedDatas.onEach { animatedData ->
            container.addView(animatedData.brick)
            animatedData.brick.scaleY = numbers[animatedData.animatedPositions.first]
            animatedData.animator.setFloatValues(
                freeLeftXSpace + animatedData.animatedPositions.first * itemXSpace + emptySpace,
                freeLeftXSpace + animatedData.animatedPositions.second * itemXSpace + emptySpace
            )
        }

        val excludeIndex = animatedDatas.map { it.animatedPositions.first }.toTypedArray()

        val animator = AnimatorSet().apply {
            val builder = play(animatedDatas.first().animator)
            for (i in 1 until animatedDatas.size) {
                builder.with(animatedDatas[i].animator)
            }
            doOnStart {
                container.background = BitmapDrawable(
                    resources,
                    generateDrawable(
                        container.width,
                        container.height,
                        freeLeftXSpace,
                        itemXSpace,
                        numbers,
                        excludeIndex
                    )
                )
            }
            doOnEnd {
                container.background = BitmapDrawable(
                    resources,
                    generateDrawable(
                        container.width,
                        container.height,
                        freeLeftXSpace,
                        itemXSpace,
                        newNumbers,
                        emptyArray()
                    )
                )
                container.removeAllViews()
                viewModel.onEvent(MainUiEvent.ANIMATED_STEP_END)
            }
            duration = stepDuration
        }

        animator.start()
    }

    private fun generateStick(
        context: Context,
        itemWidth: Int,
        freeLeftXSpace: Float,
        h: Int,
        color: Int
    ): View = View(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            itemWidth,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        translationX = freeLeftXSpace + emptySpace
        background = ColorDrawable(color)
        pivotX = 0f
        pivotY = h.toFloat()
    }

    private fun generateDrawable(
        w: Int,
        h: Int,
        freeLeftXSpace: Float,
        itemXSpace: Int,
        numbers: List<Float>,
        excludeIndex: Array<Int>
    ) = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565).applyCanvas {
        drawColor(_backgroundColor)
        repeat(numbers.size) {
            if (it in excludeIndex) return@repeat
            drawRect(
                freeLeftXSpace + itemXSpace.toFloat() * it + emptySpace,
                h * (1f - numbers[it]),
                freeLeftXSpace + itemXSpace.toFloat() * (it + 1) - emptySpace,
                h.toFloat(),
                paint
            )
        }
    }

    private fun generateDataForAnimate(
        animatedItems: Array<Indexes>,
        container: ViewGroup,
        itemWidth: Int,
        freeLeftXSpace: Float,
        h: Int
    ): Array<AnimatedData> {
        return mutableListOf<AnimatedData>().apply {
            repeat(animatedItems.size) {
                val brick =
                    generateStick(
                        container.context,
                        itemWidth,
                        freeLeftXSpace,
                        h,
                        _dynamicColor
                    )
                val animator = ObjectAnimator.ofFloat(brick, View.TRANSLATION_X, brick.translationX)
                add(
                    AnimatedData(
                        brick,
                        animator,
                        animatedItems[it]
                    )
                )
            }
        }.toTypedArray()
    }

    private fun applyCurrentState(state: MainUiState) {
        when (state) {
            is MainUiState.UpdateUiState -> onUpdateUiState(state)
            is MainUiState.RenderList -> onReceiveNewList(state.numbers)
            is MainUiState.AnimateStep -> onReceiveNextStep(
                state.numbers,
                state.newNumbers,
                state.datas,
                state.duration.value
            )
            is MainUiState.Congratulation -> onReceiveCongratulation(state.message)
            is MainUiState.Error -> onReceiveError(state.message)
        }
    }

    private fun onUpdateUiState(state: MainUiState.UpdateUiState) {
        _backgroundColor = Color.parseColor(state.palette[0])
        _staticColor = Color.parseColor(state.palette[1])
        _dynamicColor = Color.parseColor(state.palette[2])
        binding.typeTV.text = state.algorithm.mnemonic
        binding.durationTv.text = state.duration.mnemonic
    }

    private fun onReceiveNewList(numbers: List<Float>) {
        renderView(numbers)
    }

    private fun onReceiveNextStep(
        numbers: List<Float>,
        newNumbers: List<Float>,
        elements: List<Indexes>,
        stepDuration: Long
    ) {
        animateStep(
            freeLeftXSpace,
            itemXSpace,
            numbers,
            newNumbers,
            generateDataForAnimate(
                elements.toTypedArray(),
                binding.mainContainer,
                itemWidth,
                freeLeftXSpace,
                binding.mainContainer.height
            ),
            stepDuration,
            binding.mainContainer
        )
    }

    private fun onReceiveCongratulation(message: String) {
        lifecycleScope.launch {
            with(binding.congratulationIV) {
                val appearing = ObjectAnimator.ofFloat(this, View.ALPHA, 0f, 1f).apply {
                    doOnStart {
                        visibility = View.VISIBLE
                    }
                    duration = 600L
                }
                val disappearing = ObjectAnimator.ofFloat(this, View.ALPHA, 0f).apply {
                    doOnEnd {
                        visibility = View.GONE
                    }
                    duration = 600L
                }
                appearing.start()
                delay(1500L)
                disappearing.start()
            }
        }
    }

    private fun onReceiveError(errorMessage: String) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }
}