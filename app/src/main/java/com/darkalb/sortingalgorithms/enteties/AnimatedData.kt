package com.darkalb.sortingalgorithms.enteties

import android.animation.ObjectAnimator
import android.view.View
import com.darkalb.sortingalgorithms.Indexes

data class AnimatedData(
    val brick: View,
    val animator: ObjectAnimator,
    val animatedPositions: Indexes
)