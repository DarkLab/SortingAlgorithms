package com.darkalb.sortingalgorithms.variants

import com.darkalb.sortingalgorithms.enteties.RenderData
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CombSorting(originalList: List<Float>) : SortingAlgorithm(originalList) {
    override suspend fun execute(): Flow<RenderData> {
        val count = originalList.count()
        val workList = originalList.toMutableList()
        return flow {
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
                    val oldList = workList.toList()
                    if (workList[i - gap] > workList[i]) {
                        val temp = workList[i]
                        workList[i] = workList[i - gap]
                        workList[i - gap] = temp
                        isSorted = false
                        emit(
                            RenderData(
                                oldList,
                                workList.toList(),
                                listOf(
                                    (i - gap) to i,
                                    i to (i - gap)
                                )
                            )
                        )
                    } else {
                        emit(
                            RenderData(
                                oldList,
                                workList.toList(),
                                listOf(
                                    (i - gap) to (i - gap),
                                    i to i
                                )
                            )
                        )
                    }
                }
            }
            currentCoroutineContext().cancel()
        }
    }
}