package com.icebem.akt.util

object Random {
    const val DELTA_POINT = 5
    private const val DELTA_TIME = 150

    private fun random(i: Int, delta: Int): Int = if (i > delta) i + (Math.random() * delta * 2).toInt() - delta else i

    fun randomIndex(length: Int): Int = (Math.random() * length).toInt()

    fun randomPoint(point: Int): Int = random(point, DELTA_POINT)

    fun randomTime(time: Int): Long = random(time, DELTA_TIME).toLong()
}