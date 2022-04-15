package com.darkalb.sortingalgorithms.variants

import com.darkalb.sortingalgorithms.enteties.RenderData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class BubbleSorting(originalList: List<Float>) : SortingAlgorithm(originalList) {
    override suspend fun execute(): Flow<RenderData> {
        val count = originalList.count()
        val workList = originalList.toMutableList()
        return flow {
            for (i in 0 until (count - 1)) {
                for (j in (i + 1) until count) {
                    val oldList = workList.toList()
                    val temp = workList[i]
                    val renderData = if (temp > workList[j]) {
                        workList[i] = workList[j]
                        workList[j] = temp
                        RenderData(
                            oldList,
                            workList.toList(),
                            listOf(
                                i to j,
                                j to i
                            )
                        )
                    } else {
                        RenderData(
                            oldList,
                            workList.toList(),
                            listOf(
                                i to i,
                                j to j
                            )
                        )
                    }
                    emit(renderData)
                }
            }
        }
    }
}