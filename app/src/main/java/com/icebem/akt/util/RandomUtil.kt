package com.icebem.akt.util

object RandomUtil {
    const val DELTA_POINT = 5
    private const val DELTA_TIME = 150

    private fun random(i: Int, delta: Int) = if (i > delta) i + (Math.random() * delta * 2).toInt() - delta else i

    @JvmStatic
    fun randomIndex(length: Int) = (Math.random() * length).toInt()

    @JvmStatic
    fun randomPoint(point: Int) = random(point, DELTA_POINT)

    @JvmStatic
    fun randomTime(time: Int) = random(time, if (time > DELTA_TIME) DELTA_TIME else DELTA_POINT)
}