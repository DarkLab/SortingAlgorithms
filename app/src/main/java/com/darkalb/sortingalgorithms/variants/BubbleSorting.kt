package com.darkalb.sortingalgorithms.variants

import com.darkalb.sortingalgorithms.enteties.RenderData
import kotlinx.coroutines.flow.FlowCollector

class BubbleSorting(originalList: List<Float>) : SortingAlgorithm(originalList) {

    override suspend fun sort(list: MutableList<Float>, collector: FlowCollector<RenderData>) {
        val limit = list.lastIndex
        for (offset in 0 until limit) {
            for (j in 0 until (limit - offset)) {
                val oldList = list.toList()
                val temp = list[j]
                val next = j + 1
                val renderData = if (temp > list[next]) {
                    list[j] = list[next]
                    list[next] = temp
                    RenderData(
                        oldList,
                        list.toList(),
                        listOf(
                            j to next,
                            next to j
                        )
                    )
                } else {
                    RenderData(
                        oldList,
                        list.toList(),
                        listOf(
                            j to j,
                            next to next
                        )
                    )
                }
                collector.emit(renderData)
            }
        }
    }
}