package com.example.stream

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView

class MainActivity : AppCompatActivity() {

    private lateinit var appLogo: ImageView
    private lateinit var tvAppTitle: TextView
    private lateinit var etRtspUrl: EditText
    private lateinit var playerView: PlayerView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnStartStream: Button
    private lateinit var btnPauseStream: Button
    private lateinit var btnStopStream: Button
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        appLogo = findViewById(R.id.appLogo)
        tvAppTitle = findViewById(R.id.tvAppTitle)
        etRtspUrl = findViewById(R.id.etRtspUrl)
        playerView = findViewById(R.id.playerView)
        progressBar = findViewById(R.id.progressBar)
        btnStartStream = findViewById(R.id.btnStartStream)
        btnPauseStream = findViewById(R.id.btnPauseStream)
        btnStopStream = findViewById(R.id.btnStopStream)

        // Initialize ExoPlayer
        player = ExoPlayer.Builder(this).build().apply {
            setAudioAttributes(com.google.android.exoplayer2.audio.AudioAttributes.DEFAULT, true)
            setHandleAudioBecomingNoisy(true)
        }
        playerView.player = player

        // Set click listeners for the buttons
        btnStartStream.setOnClickListener {
            startStreaming()
        }

        btnPauseStream.setOnClickListener {
            pauseStreaming()
        }

        btnStopStream.setOnClickListener {
            stopStreaming()
        }
    }

    private fun startStreaming() {
        val rtspUrl = etRtspUrl.text.toString().trim()
        if (rtspUrl.isNotEmpty()) {
            progressBar.visibility = View.VISIBLE
            playerView.visibility = View.VISIBLE

            val mediaItem = MediaItem.fromUri(rtspUrl)
            player?.setMediaItem(mediaItem)
            player?.prepare()

            player?.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_READY -> {
                            progressBar.visibility = View.GONE
                            btnStartStream.visibility = View.GONE
                            btnPauseStream.visibility = View.VISIBLE
                            btnStopStream.visibility = View.VISIBLE
                        }
                        Player.STATE_ENDED -> {
                            // Handle video playback ended
                            Toast.makeText(this@MainActivity, "Playback ended", Toast.LENGTH_SHORT).show()
                            resetUI()
                        }
                        Player.STATE_BUFFERING -> {
                            progressBar.visibility = View.VISIBLE
                        }
                        Player.STATE_IDLE -> {
                            progressBar.visibility = View.GONE
                        }
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    // Log the error or show an error message to the user
                    Log.e("PlayerError", "Playback error: ${error.message}", error)
                    progressBar.visibility = View.GONE
                    btnStartStream.visibility = View.VISIBLE
                    btnPauseStream.visibility = View.GONE
                    btnStopStream.visibility = View.GONE
                    Toast.makeText(this@MainActivity, "Error playing video: ${error.message}", Toast.LENGTH_LONG).show()
                }
            })

            player?.play()
        } else {
            Toast.makeText(this, "Please enter a valid RTSP URL", Toast.LENGTH_SHORT).show()
        }
    }

    private fun pauseStreaming() {
        player?.pause()
        btnStartStream.visibility = View.VISIBLE
        btnPauseStream.visibility = View.GONE
        btnStopStream.visibility = View.VISIBLE
    }

    private fun stopStreaming() {
        player?.stop()
        resetUI()
    }

    private fun resetUI() {
        playerView.visibility = View.GONE
        progressBar.visibility = View.GONE
        btnStartStream.visibility = View.VISIBLE
        btnPauseStream.visibility = View.GONE
        btnStopStream.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
}
