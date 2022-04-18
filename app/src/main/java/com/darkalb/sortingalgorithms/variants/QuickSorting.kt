package com.darkalb.sortingalgorithms.variants

import com.darkalb.sortingalgorithms.enteties.RenderData
import kotlinx.coroutines.flow.FlowCollector

class QuickSorting(originalList: List<Float>) : SortingAlgorithm(originalList) {
    override suspend fun sort(list: MutableList<Float>, collector: FlowCollector<RenderData>) {
        val li = list.lastIndex
        quickSort(list, 0, li, collector)
        collector.emit(
            RenderData(
                list.toList(),
                list.toList(),
                listOf(
                    0 to 0
                ),
                null
            )
        )
    }

    private suspend fun quickSort(
        array: MutableList<Float>,
        left: Int,
        right: Int,
        collector: FlowCollector<RenderData>
    ) {
        val index = partition(array, left, right, collector)
        if (left < index - 1) {
            quickSort(array, left, index - 1, collector)
        }
        if (index < right) {
            quickSort(array, index, right, collector)
        }
    }

    private suspend fun partition(
        array: MutableList<Float>,
        l: Int,
        r: Int,
        collector: FlowCollector<RenderData>
    ): Int {
        var left = l
        var right = r
        val pivot = array[(left + right) / 2]
//        Нарисовать уровень pivot
        while (left <= right) {
            while (array[left] < pivot) {
                collector.emit(
                    RenderData(
                        array.toList(),
                        array.toList(),
                        listOf(
                            left to left
                        ),
                        pivot
                    )
                )
                left++
            }

            while (array[right] > pivot) {
                collector.emit(
                    RenderData(
                        array.toList(),
                        array.toList(),
                        listOf(
                            right to right
                        ),
                        pivot
                    )
                )
                right--
            }

            if (left <= right) {
                swapArray(array, left, right, pivot, collector)
                left++
                right--
            } else {
                collector.emit(
                    RenderData(
                        array.toList(),
                        array.toList(),
                        listOf(
                            left to left,
                            right to right
                        ),
                        pivot
                    )
                )
            }
        }
        return left
    }

    private suspend fun swapArray(
        a: MutableList<Float>,
        b: Int,
        c: Int,
        pivot: Float,
        collector: FlowCollector<RenderData>
    ) {
        val oldList = a.toList()
        val temp = a[b]
        a[b] = a[c]
        a[c] = temp
        collector.emit(
            RenderData(
                oldList,
                a.toList(),
                listOf(
                    b to c,
                    c to b
                ),
                pivot
            )
        )
    }
}