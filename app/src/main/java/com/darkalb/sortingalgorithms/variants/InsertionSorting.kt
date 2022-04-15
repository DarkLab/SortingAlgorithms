package com.darkalb.sortingalgorithms.variants

import com.darkalb.sortingalgorithms.enteties.RenderData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class InsertionSorting(originalList: List<Float>) : SortingAlgorithm(originalList) {
    override suspend fun execute(): Flow<RenderData> {
        val count = originalList.count()
        val workList = originalList.toMutableList()
        return flow {
            for (i in 1 until count) {
                for (j in i downTo 1) {
                    val oldList = workList.toList()
                    val temp = workList[j - 1]
                    if (temp > workList[j]) {
                        workList[j - 1] = workList[j]
                        workList[j] = temp
                        emit(
                            RenderData(
                                oldList,
                                workList.toList(),
                                listOf(
                                    j to (j - 1),
                                    (j - 1) to j
                                )
                            )
                        )
                    } else {
                        emit(
                            RenderData(
                                oldList,
                                workList.toList(),
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
}