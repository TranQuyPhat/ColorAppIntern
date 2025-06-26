package com.example.base

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import com.example.base.service.sound.SettingCheckerProvider
import com.example.base.service.sound.SoundPlayerProvider

fun View.scaleAnimation(
    duration: Long = 200,
    scaleFactor: Float = 1.2f,
    onAnimationEnd: (() -> Unit)? = null
) {
    val scaleX = ObjectAnimator.ofFloat(this, View.SCALE_X, 1f, scaleFactor, 1f)
    val scaleY = ObjectAnimator.ofFloat(this, View.SCALE_Y, 1f, scaleFactor, 1f)

    AnimatorSet().apply {
        playTogether(scaleX, scaleY)
        this.duration = duration
        addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                onAnimationEnd?.invoke()
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        start() // ✅ chỉ gọi start() một lần
    }
}

fun <T : View> T.clickWithSound(
    soundRes: Int = R.raw.touch_1,
    interval: Long = 300L,
    action: ((T) -> Unit)? = null
) {
    setOnClickListener(SingleClickWithSoundListener(interval, soundRes, action))
}

class SingleClickWithSoundListener<T : View>(
    private val interval: Long = 300L,
    private val soundRes: Int = R.raw.touch_1,
    private val action: ((T) -> Unit)?
) : View.OnClickListener {
    private var lastClickTime = 0L

    override fun onClick(v: View?) {
        val nowTime = System.currentTimeMillis()
        if (nowTime - lastClickTime > interval) {
            lastClickTime = nowTime

            v?.scaleAnimation()

            if (SettingCheckerProvider.checker?.isSoundEnabled() == true) {
                SoundPlayerProvider.soundPlayer?.play(soundRes)
            }

            action?.invoke(v as T)
        }
    }
}

