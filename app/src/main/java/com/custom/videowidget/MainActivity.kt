package com.custom.videowidget

import android.app.PictureInPictureParams
import android.content.Intent
import android.content.res.Configuration
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.VideoView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var videoView: VideoView
    private lateinit var controlPanel: LinearLayout
    private lateinit var btnMute: Button
    private var mediaPlayer: MediaPlayer? = null
    private var isMuted = false

    private val pickVideoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                playVideo(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_main)

        videoView = findViewById(R.id.videoView)
        controlPanel = findViewById(R.id.controlPanel)
        val btnSelect: Button = findViewById(R.id.btnSelect)
        btnMute = findViewById(R.id.btnMute)
        val btnWidget: Button = findViewById(R.id.btnWidget)

        btnSelect.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "video/*"
            }
            pickVideoLauncher.launch(intent)
        }

        btnMute.setOnClickListener {
            isMuted = !isMuted
            updateVolume()
            btnMute.text = if (isMuted) "음소거: ON" else "음소거: OFF"
        }

        btnWidget.setOnClickListener {
            enterWidgetMode()
        }
    }

    private fun playVideo(uri: Uri) {
        videoView.setVideoURI(uri)
        videoView.setOnPreparedListener { mp ->
            mediaPlayer = mp
            mp.isLooping = true
            updateVolume()
            videoView.start()
        }
    }

    private fun updateVolume() {
        val volume = if (isMuted) 0f else 1f
        mediaPlayer?.setVolume(volume, volume)
    }

    private fun enterWidgetMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val aspectRatio = Rational(16, 9)
            val pipBuilder = PictureInPictureParams.Builder()
                .setAspectRatio(aspectRatio)
            enterPictureInPictureMode(pipBuilder.build())
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        controlPanel.visibility = if (isInPictureInPictureMode) View.GONE else View.VISIBLE
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (videoView.isPlaying) {
            enterWidgetMode()
        }
    }
}
