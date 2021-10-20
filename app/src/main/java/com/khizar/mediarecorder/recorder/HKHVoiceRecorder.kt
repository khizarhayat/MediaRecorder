package com.khizar.mediarecorder.recorder

import android.Manifest
import android.media.MediaRecorder
import android.os.CountDownTimer
import android.util.Log
import android.view.MotionEvent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.khizar.mediarecorder.RECORDING_MODES
import com.khizar.mediarecorder.getFormattedTimer
import java.io.File
import java.io.IOException

class HKHVoiceRecorder {
    private val LOG_TAG = "HKHVoiceRecorder"
    private var recorder: MediaRecorder? = null
    private var recordedDurationInSec = 0
    private val MAX_DURATION_VALUE = 20

    private var file: File? = null
    private var fileName: String? = null
    private var context: Fragment? = null
    private var countDownTimer: CountDownTimer? = null
    private var isButtonClicked = false
    private var recorderGetViews: RecorderGetViews
    private var recorderCallBack: RecorderCallBack
    private lateinit var activityResultLauncher: ActivityResultLauncher<Array<String>>
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)

    constructor(
        context: Fragment, file: File?, fileName: String?, recorderGetViews: RecorderGetViews,
        recorderCallBack: RecorderCallBack
    ) {
        this.context = context
        this.file = file
        this.fileName = fileName
        this.recorderGetViews = recorderGetViews
        this.recorderCallBack = recorderCallBack
        registerPermissions()
        activityResultLauncher.launch(permissions)
    }

    fun resetFiles(file: File?, fileName: String?) {
        this.file = file
        this.fileName = fileName
    }

    private fun getCurrentMode(): RECORDING_MODES {
        return recorderGetViews.getCurrentRecordingMode()
    }

    fun setListeners() {
        var x1 = 0f
        var x2 = 0f
        val MIN_DISTANCE = 200
        recorderGetViews.getParentView().setOnTouchListener { v, event ->
            if (getCurrentMode() == RECORDING_MODES.PREVIEW) {

            } else {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        x1 = event.x
                        onPressDown()
                    }
                    MotionEvent.ACTION_UP -> {
                        x2 = event.x
                        val deltaX = x2 - x1
                        if (deltaX > MIN_DISTANCE) {
                            onCancel()
                        } else {
                            // consider as something else - a screen tap for example
                            if (recordedDurationInSec < 5) {
                                onError("Minimum length should be 5 seconds")
                                onCancel()
                            } else {
                                onStopRecording()
                            }
                        }
                    }
                    MotionEvent.ACTION_MOVE -> {
//                    onCancel()
                        x2 = event.x
//                    Log.e("audio_review", "ACTION_MOVE x2 == $x2")
                        val deltaX = x2 - x1
                        if (deltaX > MIN_DISTANCE) {
                            onCancel()
                        }
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        // Released - Dragged finger outside
//                        onCancel()
                    }
                }
            }
            true
        }
    }

    private fun onError(text: String) {
        recorderCallBack.onError(text)
    }

    private fun onPressDown() {
        startRecording(true)
        showRecordingViews(RECORDING_MODES.RECORDING)
    }

    fun registerPermissions() {
        if (context == null) return
        this.activityResultLauncher = context!!.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            var allAreGranted = true
            for (b in result.values) {
                allAreGranted = allAreGranted && b
            }

            if (allAreGranted && isButtonClicked) {
                startRecording()
            }
        }
    }

    private fun startRecording() {
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(fileName)
            setMaxDuration(MAX_DURATION_VALUE * 1000)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

            try {
                prepare()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
            }

            try {
                start()
                showRecordTimer()
            } catch (e: IllegalStateException) {
                Log.e(LOG_TAG, "start() failed")
            }
        }
    }

    private fun onCancel() {
        cancelRecording()
    }

    private fun cancelRecording() {
        startRecording(false)
        showRecordingViews(RECORDING_MODES.DEFAULT)
    }

    fun onStopRecording() {
        startRecording(false)
        showRecordingViews(RECORDING_MODES.PREVIEW)
    }

    fun isEligibleToSubmit(): Boolean {
        return file != null && file?.exists() == true && recordedDurationInSec > 5
    }

    private fun showRecordingViews(mode: RECORDING_MODES) {
        recorderCallBack.showRecordingViews(mode)
    }

    fun isRecordingInProgress(): Boolean {
        return getCurrentMode() == RECORDING_MODES.RECORDING
    }

    private fun startRecording(startRecording: Boolean) {
        isButtonClicked = startRecording
        onRecord(startRecording)
    }

    private fun onRecord(start: Boolean) = if (start) {
        activityResultLauncher.launch(permissions)
    } else {
        stopRecording()
    }

    private fun showRecordTimer() {
        countDownTimer?.cancel()
        recordedDurationInSec = 1
        countDownTimer = object : CountDownTimer(MAX_DURATION_VALUE * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.e(LOG_TAG, "onTick() $recordedDurationInSec")
                Log.e(
                    LOG_TAG,
                    "onTick() setProgress ${((100F / MAX_DURATION_VALUE) * recordedDurationInSec).toFloat()}"
                )
                val timer = recordedDurationInSec.getFormattedTimer()
                recorderGetViews.getTimer().text = timer
                recordedDurationInSec++
            }

            override fun onFinish() {
                Log.e(LOG_TAG, "onFinish")
            }
        }
        countDownTimer?.start()
    }

    private fun stopRecording() {
        recorder?.apply {
            try {
                stop()
                reset()
                release()
                countDownTimer?.cancel()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        recorder = null
    }
}