package com.darkalb.sortingalgorithms.enums

enum class Algorithm(val mnemonic: String) {
    BUBBLE_SORT("bub"),
    INSERTION_SORT("ins"),
    COMB_SORTING("comb"),
    QUICK_SORT("qk");

    fun next(): Algorithm {
        return when (this) {
            BUBBLE_SORT -> INSERTION_SORT
            INSERTION_SORT -> COMB_SORTING
            COMB_SORTING -> QUICK_SORT
            QUICK_SORT -> BUBBLE_SORT
        }
    }
}