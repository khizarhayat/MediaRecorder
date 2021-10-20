package com.khizar.mediarecorder

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.content.res.Resources
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.transition.Slide
import androidx.transition.TransitionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.floor


fun Int.getFormattedTimer(): String {
    val timeInSeconds = this
    val secondsLeft: Int = timeInSeconds % 3600 % 60
    val minutes = floor((timeInSeconds % 3600 / 60).toDouble()).toInt()
    val hours = floor((timeInSeconds / 3600).toDouble()).toInt()

    val HH = (if (hours < 10) "0" else "") + hours
    val MM = (if (minutes < 10) "0" else "") + minutes
    val SS = (if (secondsLeft < 10) "0" else "") + secondsLeft

    return "$MM:$SS"
}

fun CoroutineScope.launchPeriodicAsync(
    repeatMillis: Long,
    action: () -> Unit,
) = this.async {
    if (repeatMillis > 0) {
        while (isActive) {
            action()
            delay(repeatMillis)
        }
    } else {
        action()
    }
}


fun View.gone() {
    if (visibility != View.GONE) {
        visibility = View.GONE
    }
}

fun View.visible() {
    if (visibility != View.VISIBLE) {
        visibility = View.VISIBLE
    }
}

fun View.invisible() {
    if (visibility != View.INVISIBLE) {
        visibility = View.INVISIBLE
    }
}

fun ImageView.setImageDrawableWithFadeAnimation(drawableId: Int) {
    if (this.tag == drawableId) {
        setImageResource(drawableId)
        return
    }
    val fadeOut = AnimationUtils.loadAnimation(this.context, R.anim.fade_out)
    val fadeIn = AnimationUtils.loadAnimation(this.context, R.anim.fade_in)
    fadeOut.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation?) {
            tag = drawableId
        }

        override fun onAnimationEnd(animation: Animation?) {
            setImageResource(drawableId)
            startAnimation(fadeIn)
        }

        override fun onAnimationRepeat(animation: Animation?) {

        }
    })
    startAnimation(fadeOut)
}


fun View.animateColorValue(oldColor: Int, newColor: Int, duration: Long = 20L) {
    val colorAnimation =
        ValueAnimator.ofObject(ArgbEvaluator(), oldColor, newColor)
    colorAnimation.duration = duration
    colorAnimation.addUpdateListener { animator ->
        this.backgroundTintList =
            ColorStateList.valueOf(resources.getColor(animator.animatedValue as Int))
    }
    colorAnimation.start()
}

fun ViewGroup.visibleWithSlideAnimation(
    targetId: Int, parent: ViewGroup,
    duration: Long = 200L, gravity: Int = Gravity.LEFT,
) {
    val trans = Slide(gravity)
    trans.duration = duration
    trans.addTarget(targetId)
    TransitionManager.beginDelayedTransition(parent, trans)
    this.visible()
}

val Int.pxTodp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()

