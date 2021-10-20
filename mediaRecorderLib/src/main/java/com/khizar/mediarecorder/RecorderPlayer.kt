package com.khizar.mediarecorder

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Group
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.transition.TransitionManager
import com.khizar.mediarecorder.media_player.GetViews
import com.khizar.mediarecorder.media_player.HKHMediaPlayer
import com.khizar.mediarecorder.media_player.MPCallBack
import com.khizar.mediarecorder.recorder.HKHVoiceRecorder
import com.khizar.mediarecorder.recorder.RecorderCallBack
import com.khizar.mediarecorder.recorder.RecorderGetViews
import java.io.File
import java.util.*

class RecorderPlayer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    FrameLayout(context, attrs, defStyleAttr), GetViews,
    MPCallBack, RecorderGetViews, RecorderCallBack {
    private lateinit var mediaPlayer: HKHMediaPlayer
    private lateinit var voiceRecorder: HKHVoiceRecorder
    private lateinit var buttonViews: View
    private var currentRecordingMode = RECORDING_MODES.DEFAULT
    private var currentAudioMode = AUDIO_MODES.DEFAULT

    private var file: File? = null
    private var fileName: String? = null
    private var fragment: Fragment? = null
    private var activity: Activity? = null

    init {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        buttonViews = inflate(context, R.layout.recorder_audio_btn, this)

        val ta = context.obtainStyledAttributes(attrs, R.styleable.RecorderPlayer)
        try {
//            val text = ta.getString(R.styleable.CustomView_text)
//            val drawableId = ta.getResourceId(R.styleable.CustomView_image, 0)
//            if (drawableId != 0) {
//                val drawable = AppCompatResources.getDrawable(context, drawableId)
//                image_thumb.setImageDrawable(drawable)
//            }
//            text_title.text = text
        } finally {
            ta.recycle()
        }
    }

    fun build(activity: Activity) {
        this.activity = activity
        initPlayers()
    }

    fun build(fragment: Fragment) {
        this.fragment = fragment
        initPlayers()
    }

    private fun initPlayers() {
        createFileData()
        initMediaPlayer()
        initVoiceRecorder()
        showRecordingViews(currentRecordingMode)
        getDeleteButton().setOnClickListener {
            stopPlaying()
            deleteAudioFile()
            showRecordingViews(RECORDING_MODES.DEFAULT)
        }
        getRightIcon().setOnClickListener {
            if (currentRecordingMode == RECORDING_MODES.PREVIEW) {
                onSubmitClick(file)
            }
        }
    }

    private fun createFileData() {
        deleteAudioFile()
        val directory = File(context?.externalCacheDir?.absolutePath)
        file = File(directory, "recording_${Calendar.getInstance().timeInMillis}.mp3")
        fileName = file?.absolutePath
    }

    private fun deleteAudioFile() {
        file?.delete()
    }

    private fun initVoiceRecorder() {
        if (!::voiceRecorder.isInitialized) {
            voiceRecorder = HKHVoiceRecorder(fragment!!, file, fileName, this, this)
            voiceRecorder.setListeners()
        }
    }

    private fun initMediaPlayer() {
        if (!::mediaPlayer.isInitialized) {
            mediaPlayer = HKHMediaPlayer(
                getViews = this,
                callBack = this,
                url = fileName
            )
            mediaPlayer.setListeners()
        }
    }

    private fun getDeleteButton(): AppCompatImageView =
        buttonViews.findViewById(R.id.iv_delete)

    override fun getPlayButton(): AppCompatImageView {
        return buttonViews.findViewById(R.id.iv_play_icon)
    }

    override fun getSeekBar(): SeekBar {
        return buttonViews.findViewById(R.id.seekbar)
    }

    override fun getTimerTv(): TextView {
        return buttonViews.findViewById(R.id.tv_play_timer)
    }

    override fun getCurrentMode(): AUDIO_MODES {
        return currentAudioMode
    }

    override fun stopPlaying() {
        currentAudioMode = AUDIO_MODES.PAUSE
        if (fragment is MPCallBack) {
            (fragment as MPCallBack).stopPlaying()
        }
    }

    override fun startPlaying() {
        currentAudioMode = AUDIO_MODES.PLAY
        if (fragment is MPCallBack) {
            (fragment as MPCallBack).startPlaying()
        }
    }

    override fun showRecordingViews(mode: RECORDING_MODES) {
        currentRecordingMode = mode
        when (mode) {
            RECORDING_MODES.PREVIEW -> {
                getGroupView().visible()
                getGroupRecording().gone()
                getTvTitleStartRecording().gone()
                getRightIcon().visible()
                getRightIcon().setImageDrawableWithFadeAnimation(R.drawable.ic_msg_send)
                getLeftIcon().gone()
                updateConstraintForPreview()
            }
            RECORDING_MODES.RECORDING -> {
                getGroupPreview().gone()
                getGroupRecording().visible()
                getTvTitleStartRecording().gone()
                getRightIcon().invisible()
                getLeftIcon().visible()
                updateConstraintForRecording()
                getLeftIcon().setImageDrawableWithFadeAnimation(R.drawable.ic_mic_white)
            }
            RECORDING_MODES.DEFAULT -> {
                getGroupPreview().gone()
                getGroupRecording().gone()
                getTvTitleStartRecording().visible()
                getRightIcon().visible()
                getLeftIcon().visible()
                getLeftIcon().setImageDrawableWithFadeAnimation(R.drawable.ic_mic_white)
                getRightIcon().setImageDrawableWithFadeAnimation(R.drawable.ic_mic_color)
                updateConstraintForDefault()
                getPressHoldLayout().visibleWithSlideAnimation(
                    R.id.layout_audio_btn_press_hold,
                    getAudioButtonParent()
                )
            }
        }
        if (fragment is RecorderCallBack) {
            (fragment as RecorderCallBack).showRecordingViews(mode)
        }
    }


    private fun updateConstraintForRecording() {
        getPressHoldLayout().apply {
            TransitionManager.beginDelayedTransition(this)
            animateColorValue(R.color.teal_200, R.color.purple_200)
            updateLayoutParams<ConstraintLayout.LayoutParams> {
                endToStart = ConstraintLayout.LayoutParams.UNSET
                endToEnd = R.id.audio_btn_parent
                marginEnd = 0
                //add other constraints if needed
            }
        }
    }

    private fun updateConstraintForPreview() {
        getPressHoldLayout().apply {
            TransitionManager.beginDelayedTransition(this)
            animateColorValue(R.color.purple_200, R.color.teal_200)
            updateLayoutParams<ConstraintLayout.LayoutParams> {
                endToStart = R.id.right_icon
                endToEnd = ConstraintLayout.LayoutParams.UNSET
                marginEnd = 12.pxTodp
                //add other constraints if needed
            }
        }
    }

    private fun updateConstraintForDefault() {
        getPressHoldLayout().apply {
            animateColorValue(R.color.teal_200, R.color.teal_200)
            updateLayoutParams<ConstraintLayout.LayoutParams> {
                endToStart = R.id.right_icon
                endToEnd = ConstraintLayout.LayoutParams.UNSET
                marginEnd = 12.pxTodp
                //add other constraints if needed
            }
        }
    }


    private fun getAudioButtonParent(): ViewGroup = buttonViews.findViewById(R.id.audio_btn_parent)
    private fun getPressHoldLayout(): ViewGroup =
        buttonViews.findViewById(R.id.layout_audio_btn_press_hold)

    private fun getGroupView(): View =
        buttonViews.findViewById<Group>(R.id.group_preview)

    private fun getGroupRecording(): View =
        buttonViews.findViewById<Group>(R.id.group_recording)

    private fun getGroupPreview(): View =
        buttonViews.findViewById<Group>(R.id.group_preview)

    private fun getTvTitleStartRecording(): View =
        buttonViews.findViewById<TextView>(R.id.tv_title_start_recording)

    private fun getRightIcon(): ImageView =
        buttonViews.findViewById<ImageView>(R.id.right_icon)

    private fun getLeftIcon(): ImageView =
        buttonViews.findViewById<ImageView>(R.id.left_icon)

    override fun onError(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
        if (fragment is RecorderCallBack) {
            (fragment as RecorderCallBack).onError(text)
        }
    }

    override fun onSubmitClick(file: File?) {
        Toast.makeText(context, "Submit this recording", Toast.LENGTH_SHORT).show()
        if (fragment is RecorderCallBack) {
            (fragment as RecorderCallBack).onSubmitClick(file)
        }
    }

    override fun getParentView(): View {
        return buttonViews
    }

    override fun getCurrentRecordingMode(): RECORDING_MODES {
        return currentRecordingMode
    }

    override fun getTimer(): TextView {
        return buttonViews.findViewById(R.id.tv_timer)
    }
}

enum class RECORDING_MODES {
    DEFAULT, RECORDING, PREVIEW
}

enum class AUDIO_MODES {
    DEFAULT, PLAY, PAUSE
}