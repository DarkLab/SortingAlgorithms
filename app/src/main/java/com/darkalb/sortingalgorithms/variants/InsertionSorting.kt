package com.darkalb.sortingalgorithms.variants

import com.darkalb.sortingalgorithms.enteties.RenderData
import kotlinx.coroutines.flow.FlowCollector

class InsertionSorting(originalList: List<Float>) : SortingAlgorithm(originalList) {

    override suspend fun sort(list: MutableList<Float>, collector: FlowCollector<RenderData>) {
        val count = list.count()
        for (i in 1 until count) {
            for (j in i downTo 1) {
                val oldList = list.toList()
                val temp = list[j - 1]
                if (temp > list[j]) {
                    list[j - 1] = list[j]
                    list[j] = temp
                    collector.emit(
                        RenderData(
                            oldList,
                            list.toList(),
                            listOf(
                                j to (j - 1),
                                (j - 1) to j
                            )
                        )
                    )
                } else {
                    collector.emit(
                        RenderData(
                            oldList,
                            list.toList(),
                            listOf(
                                (j - 1) to (j - 1),
                                j to j
                            )
                        )
                    )
                    break
                }
            }
        }
    }
}