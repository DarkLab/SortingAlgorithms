package com.darkalb.sortingalgorithms.enums

enum class QUANTITY(val size: Int) {
    SMALL(20),
    MEDIUM(30),
    BIG(40);

    fun next(): QUANTITY {
        return when (this) {
            SMALL -> MEDIUM
            MEDIUM -> BIG
            BIG -> SMALL
        }
    }
}