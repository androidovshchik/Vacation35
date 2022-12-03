package rf.vacation35.extension

import android.view.View

fun View.dp(value: Int) = context.dp(value)

fun View.dp(value: Float) = context.dp(value)

fun View.sp(value: Int) = context.sp(value)

fun View.sp(value: Float) = context.sp(value)
