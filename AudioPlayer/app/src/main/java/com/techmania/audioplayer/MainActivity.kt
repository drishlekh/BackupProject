import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.techmania.audioplayer.R
import java.io.IOException

class MainActivity : Activity() {

    private lateinit var mediaPlayer: MediaPlayer
    private val REQUEST_CODE_PICK_AUDIO = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mediaPlayer = MediaPlayer()

        val btnPlayFromStorage = findViewById<Button>(R.id.btn_play_from_storage)
        val btnPlayLiveMicrophone = findViewById<Button>(R.id.btn_play_live_microphone)
        val btnPlayFromYouTube = findViewById<Button>(R.id.btn_play_from_youtube)

        btnPlayFromStorage.setOnClickListener {
            playAudioFromStorage()
        }

        btnPlayLiveMicrophone.setOnClickListener {
            playAudioFromMicrophone()
        }

        btnPlayFromYouTube.setOnClickListener {
            playAudioFromYouTube()
        }
    }

    private fun playAudioFromStorage() {
        val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_CODE_PICK_AUDIO)
    }

    private fun playAudioFromMicrophone() {
        val audioServer = AudioServer()
        audioServer.startStreamingMicrophone()
    }

    private fun playAudioFromYouTube() {
        // Implement YouTube API integration here
        val youtubeUrl = "https://www.youtube.com"//watch?v=VIDEO_ID
        playYouTubeAudio(youtubeUrl)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_AUDIO && resultCode == RESULT_OK && data != null) {
            val audioUri = data.data
            try {
                mediaPlayer.reset()
                mediaPlayer.setDataSource(this, audioUri!!)
                mediaPlayer.prepare()
                mediaPlayer.start()
            } catch (e: IOException) {
                Log.e("MainActivity", "Error playing audio from storage", e)
            }
        }
    }

    private fun playYouTubeAudio(youtubeUrl: String) {
        // Use YouTube Data API or an external library to extract audio URL and stream it
    }
}
