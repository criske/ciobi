package com.crskdev.ciobi.ui.common

import android.content.res.Resources
import kotlin.math.roundToInt

/**
 * Created by Cristian Pela on 10.02.2020.
 */
inline class Dp(val value: Int) {
    companion object {
        val Hairline = Dp(0)
        val Infinity = Dp(Int.MAX_VALUE)
    }

    operator fun div(value: Int): Dp = Dp((this.value.toFloat() / value).roundToInt())

    operator fun times(value: Int): Dp = Dp(this.value * value)

    operator fun plus(value: Int): Dp = Dp(this.value + value)

    operator fun minus(value: Int): Dp = Dp(this.value - value)
}

inline class Px(val value: Float)
inline class IntPx(val value: Int)


class DpSize(val width: Dp, val height: Dp) {
    constructor(size: Dp) : this(size, size)

    companion object {
        val MATCH_PARENT = DpSize(Dp.Infinity)
        val WRAP_CONTENT = DpSize(Dp.Hairline)
    }
}

class PxSize(val width: Px, val height: Px)
class DpPoint(val x: Dp, val y: Dp) {
    companion object {
        val ORIGIN = DpPoint(Dp.Hairline, Dp.Hairline)
    }
}

val Int.dp get() = Dp(this)

val Int.px get() = Px(this.toFloat())

val Float.px get() = Px(this)

class DensityScope {

    private val density = Resources.getSystem().displayMetrics.density

    fun Dp.toPx(): Px = Px(value * density)

    fun Dp.toIntPx(): IntPx = IntPx(toPx().value.roundToInt())

    fun Px.toDp(): Dp = Dp((value / density).roundToInt())

    fun IntPx.toDp(): Dp = Px(value.toFloat()).toDp()

}

fun <T> withDensity(block: DensityScope.() -> T): T = DensityScope().run(block)


