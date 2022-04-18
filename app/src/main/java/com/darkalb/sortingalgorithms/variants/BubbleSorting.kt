package com.darkalb.sortingalgorithms.variants

import com.darkalb.sortingalgorithms.enteties.RenderData
import kotlinx.coroutines.flow.FlowCollector

class BubbleSorting(originalList: List<Float>) : SortingAlgorithm(originalList) {

    override suspend fun sort(list: MutableList<Float>, collector: FlowCollector<RenderData>) {
        val count = list.count()
        for (i in 0 until (count - 1)) {
            for (j in (i + 1) until count) {
                val oldList = list.toList()
                val temp = list[i]
                val renderData = if (temp > list[j]) {
                    list[i] = list[j]
                    list[j] = temp
                    RenderData(
                        oldList,
                        list.toList(),
                        listOf(
                            i to j,
                            j to i
                        )
                    )
                } else {
                    RenderData(
                        oldList,
                        list.toList(),
                        listOf(
                            i to i,
                            j to j
                        )
                    )
                }
                collector.emit(renderData)
            }
        }
    }
}