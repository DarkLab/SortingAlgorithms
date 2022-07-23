package com.darkalb.sortingalgorithms.enums

enum class DURATION(val mnemonic: String, val value: Long) {
    SLOW("1x", 960L),
    MEDIUM("2x", 640L),
    FAST("3x", 320L),
    VERY_FAST("4x", 160L),
    SUPER_FAST("5x", 80L);

    fun next(): DURATION {
        return when (this) {
            SLOW -> MEDIUM
            MEDIUM -> FAST
            FAST -> VERY_FAST
            VERY_FAST -> SUPER_FAST
            SUPER_FAST -> SLOW
        }
    }
}