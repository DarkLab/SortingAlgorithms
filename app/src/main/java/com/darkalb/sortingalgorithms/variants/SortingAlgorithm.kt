package com.darkalb.sortingalgorithms.variants

import com.darkalb.sortingalgorithms.enteties.RenderData
import kotlinx.coroutines.flow.Flow

abstract class SortingAlgorithm(val originalList: List<Float>) {
    abstract suspend fun execute(): Flow<RenderData>
}