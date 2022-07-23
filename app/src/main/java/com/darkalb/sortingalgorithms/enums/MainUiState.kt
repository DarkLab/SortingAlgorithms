package com.darkalb.sortingalgorithms.enums

import com.darkalb.sortingalgorithms.Indexes

sealed class MainUiState {
    data class UpdateUiState(
        val algorithm: Algorithm,
        val palette: Array<String>,
        val duration: DURATION
    ) : MainUiState() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as UpdateUiState

            if (algorithm != other.algorithm) return false
            if (!palette.contentEquals(other.palette)) return false
            if (duration != other.duration) return false

            return true
        }

        override fun hashCode(): Int {
            var result = algorithm.hashCode()
            result = 31 * result + palette.contentHashCode()
            result = 31 * result + duration.hashCode()
            return result
        }
    }

    data class RenderList(val numbers: List<Float>) : MainUiState()
    data class AnimateStep(
        val numbers: List<Float>,
        val newNumbers: List<Float>,
        val datas: List<Indexes>,
        val duration: DURATION,
        val level: Float? = null
    ) : MainUiState()

    data class Congratulation(val message: String) : MainUiState()
    data class Error(val message: String) : MainUiState()
}