package com.example.tugas3

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import android.app.Activity
import android.content.Intent

class MainActivity : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var seekBar: SeekBar
    private lateinit var pauseButton: Button
    private lateinit var playButton: Button
    private lateinit var selectFileButton: Button
    private var isPaused = false
    private var pausedPosition = 0

    private val chooseAudio = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedAudio: Uri? = result.data?.data
            selectedAudio?.let {
                mediaPlayer?.reset()
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(this@MainActivity, selectedAudio)
                    prepareAsync()
                    setOnPreparedListener {
                        seekBar.max = duration
                        startSeekBarUpdate()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        seekBar = findViewById(R.id.seekBar)
        pauseButton = findViewById(R.id.pauseButton)
        playButton = findViewById(R.id.playButton)
        selectFileButton = findViewById(R.id.selectFileButton)

        pauseButton.setOnClickListener {
            mediaPlayer?.pause()
            isPaused = true
            pausedPosition = seekBar.progress // Simpan posisi seekbar saat tombol pause ditekan
        }

        playButton.setOnClickListener {
            if (isPaused) {
                mediaPlayer?.seekTo(pausedPosition)
                mediaPlayer?.start()
                startSeekBarUpdate()
                isPaused = false
            } else {
                mediaPlayer?.start()
                startSeekBarUpdate()
            }
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Jika pengguna yang memindahkan thumb seekbar, majukan audio ke posisi yang sama
                if (fromUser) {
                    mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        selectFileButton.setOnClickListener {
            openFileChooser()
        }
    }

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "audio/*"
        chooseAudio.launch(intent)
    }

    private fun startSeekBarUpdate() {
        mediaPlayer?.let { player ->
            Thread {
                while (player.isPlaying) {
                    runOnUiThread {
                        seekBar.progress = player.currentPosition
                    }
                    try {
                        Thread.sleep(1000)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
                runOnUiThread {
                    seekBar.progress = player.duration
                }
            }.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }
}


