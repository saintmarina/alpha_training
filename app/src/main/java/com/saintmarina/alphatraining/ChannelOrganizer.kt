package com.saintmarina.alphatraining

import android.content.Context
import android.graphics.Path
import kotlin.math.abs
import kotlin.math.max



const val NUM_SECS_ON_SCREEN = 10
const val SCREEN_DECIMATION_RATE = 3
const val SCREEN_REFRESH_RATE = OpenBCI.SAMPLE_RATE_HZ / SCREEN_DECIMATION_RATE
const val NUM_POINTS_ON_SCREEN = NUM_SECS_ON_SCREEN*SCREEN_REFRESH_RATE

data class ChannelOrganizer(val context: Context) {
    private var counter = 0

    private val vizDataAll = DoubleCircularArray(NUM_POINTS_ON_SCREEN)
    private val vizDataAlpha = DoubleCircularArray(NUM_POINTS_ON_SCREEN)
    private val vizDataAlphaEnvelope = DoubleCircularArray(NUM_POINTS_ON_SCREEN)

    val visualizer = WaveVisualizer(context).apply { values = vizDataAll }
    var maxPoint = 1.0f
    var minPoint = 0.0f

    private val fAll = CascadedBiquadFilter(FilterCoefficients.allWaves)
    private val fAlpha = CascadedBiquadFilter(FilterCoefficients.alphaWaves)
    private val fAlphaEnvelope = CascadedBiquadFilter(FilterCoefficients.envelopeDetection)

    fun pushValue(v: Double) {
        val allV = fAll.filter(v)
        val alphaV = fAlpha.filter(v)
        val alphaEnvelopeV = fAlphaEnvelope.filter(abs(alphaV))

        counter++
        if (counter == SCREEN_DECIMATION_RATE) {
            counter = 0

            vizDataAll.push(allV)
            vizDataAlpha.push(alphaV)
            vizDataAlphaEnvelope.push(alphaEnvelopeV)

            updateVisualizer()
        }
    }

    fun showAllWaves() {
        visualizer.values = vizDataAll
        updateVisualizer()
    }
    fun showAlphaWaves() {
        visualizer.values = vizDataAlpha
        updateVisualizer()
    }
    fun showAlphaEnvelopeWaves() {
        visualizer.values = vizDataAlphaEnvelope
        updateVisualizer()
    }

    private fun updateVisualizer() {
        maxPoint = max(visualizer.values.array.maxOrNull() ?: 1.00,
            abs(visualizer.values.array.minOrNull()?: 1.00) ).toFloat()
        minPoint = if (visualizer.values == vizDataAlphaEnvelope) 0.0f else -maxPoint

        visualizer.invalidate()
    }
}