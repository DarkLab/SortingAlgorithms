package com.darkalb.sortingalgorithms.variants

import com.darkalb.sortingalgorithms.enteties.RenderData
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow

abstract class SortingAlgorithm(private val originalList: List<Float>) {
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
}