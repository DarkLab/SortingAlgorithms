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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.random.Random

typealias Indexes = Pair<Int, Int>

enum class MainUiEvent {
    START_ACTION,
    START_SORT,
    NEXT_STEP
}

sealed class MainUiState {
    data class StartAction(val numbers: List<Float>) : MainUiState()
    data class StartSort(val name: String) : MainUiState()
    data class NextStep(
        val numbers: List<Float>,
        val newNumbers: List<Float>,
        val datas: List<Indexes>,
        val stepDuration: Long
    ) : MainUiState()

    data class Error(val message: String) : MainUiState()
}

class MainActivity : AppCompatActivity() {

    private val staticColor = Color.argb(255, 40, 225, 40)
    private val dynamicColor = Color.argb(255, 80, 40, 220)
    private val paint = Paint().apply {
        color = staticColor
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
        binding.root.apply {
            setOnClickListener {
                viewModel.onEvent(MainUiEvent.START_SORT)
            }
            post {
                viewModel.onEvent(MainUiEvent.START_ACTION)
            }
        }
    }

    private fun renderView(numbers: List<Float>) {
        if (numbers.isEmpty()) return

        binding.root.apply {
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
                viewModel.onEvent(MainUiEvent.NEXT_STEP)
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
                        dynamicColor
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
            is MainUiState.StartAction -> onReceiveStartAction(state.numbers)
            is MainUiState.StartSort -> onReceiveStartSort(state.name)
            is MainUiState.NextStep -> onReceiveNextStep(
                state.numbers,
                state.newNumbers,
                state.datas,
                state.stepDuration
            )
            is MainUiState.Error -> onReceiveError(state.message)
        }
    }

    private fun onReceiveStartAction(numbers: List<Float>) {
        renderView(numbers)
    }

    private fun onReceiveStartSort(name: String) {
        Toast.makeText(this, name, Toast.LENGTH_SHORT).show()
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
                binding.root,
                itemWidth,
                freeLeftXSpace,
                binding.root.height
            ),
            stepDuration,
            binding.root
        )
    }

    private fun onReceiveError(errorMessage: String) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }
}