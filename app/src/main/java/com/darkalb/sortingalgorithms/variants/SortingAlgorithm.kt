package com.darkalb.sortingalgorithms.variants

import com.darkalb.sortingalgorithms.enteties.RenderData
import com.darkalb.sortingalgorithms.listeners.StepListener
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

abstract class SortingAlgorithm(private val originalList: List<Float>) {
    private var stepListener: StepListener? = null

    suspend fun execute(): Flow<RenderData> {
        val workList = originalList.toMutableList()
        return flow {
            sort(workList, this)
            currentCoroutineContext().cancel()
        }
    }

    protected abstract suspend fun sort(
        list: MutableList<Float>,
        collector: FlowCollector<RenderData>
    )

    fun stepPerformed() {
        stepListener?.onStepPerformed()
    }

    suspend fun awaitStepFinish() = suspendCoroutine<Unit> { cont ->
        stepListener = object : StepListener {
            override fun onStepPerformed() {
                stepListener = null
                cont.resume(Unit)
            }
        }
    }
}