package com.bignerdranch.android.draganddraw

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Shapes(val boxes: List<Box>): Parcelable