package com.darkalb.sortingalgorithms.variants

import com.darkalb.sortingalgorithms.enteties.RenderData
import kotlinx.coroutines.flow.FlowCollector

class CombSorting(originalList: List<Float>) : SortingAlgorithm(originalList) {

    override suspend fun sort(list: MutableList<Float>, collector: FlowCollector<RenderData>) {
        val count = list.count()
        var gap = count
        var isSorted = false
        while (isSorted.not() || gap != 1) {
            gap = if (gap > 1) {
                gap * 10 / 13
            } else {
                1
            }

            isSorted = true
            for (i in gap until count) {
                val oldList = list.toList()
                if (list[i - gap] > list[i]) {
                    val temp = list[i]
                    list[i] = list[i - gap]
                    list[i - gap] = temp
                    isSorted = false
                    collector.emit(
                        RenderData(
                            oldList,
                            list.toList(),
                            listOf(
                                (i - gap) to i,
                                i to (i - gap)
                            )
                        )
                    )
                } else {
                    collector.emit(
                        RenderData(
                            oldList,
                            list.toList(),
                            listOf(
                                (i - gap) to (i - gap),
                                i to i
                            )
                        )
                    )
                }
            }
        }
    }
}