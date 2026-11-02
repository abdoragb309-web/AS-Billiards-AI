package com.dezel.asbilliards

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AnalysisReceiver(
    private val onResult: (Int) -> Unit
) : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        if (intent.action == ACTION_ANALYSIS_RESULT) {

            val detectedCount = intent.getIntExtra(
                EXTRA_DETECTED_COUNT,
                0
            )

            onResult(detectedCount)
        }
    }

    companion object {
        const val ACTION_ANALYSIS_RESULT =
            "com.dezel.asbilliards.ANALYSIS_RESULT"

        const val EXTRA_DETECTED_COUNT =
            "detected_count"
    }
}
