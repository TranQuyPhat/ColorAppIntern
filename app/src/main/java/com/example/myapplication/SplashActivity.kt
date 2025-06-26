package com.example.myapplication



import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.LinearInterpolator
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd

class SplashActivity : AppCompatActivity() {
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spash)

        progressBar = findViewById(R.id.progressBar)
        progressText = findViewById(R.id.progressText)

        startAnimatedProgress()
    }

    private fun startAnimatedProgress() {
        val animator = ObjectAnimator.ofInt(progressBar, "progress", 0, 100)
        animator.duration = 3000 // 3 giây
        animator.interpolator = LinearInterpolator()

        animator.addUpdateListener { animation ->
            val progress = animation.animatedValue as Int
            progressText.text = "$progress%"

            // Di chuyển TextView theo vị trí tiến độ
            val max = progressBar.max
            val barWidth = progressBar.width - progressBar.paddingStart - progressBar.paddingEnd
            val percent = progress.toFloat() / max

            progressText.translationX = barWidth * percent - progressText.width / 2
        }

        animator.start()

        // Sau khi chạy xong thì vào HomeActivity
        animator.doOnEnd {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
