package com.example.flo

import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.flo.databinding.ActivitySongBinding
import com.google.gson.Gson

class SongActivity : AppCompatActivity() {

    lateinit var binding : ActivitySongBinding
    lateinit var timer: Timer
    private var mediaPlayer: MediaPlayer? = null
    private var gson : Gson = Gson()
    private var isRepeating : Boolean = false
    private var isRandom : Boolean = false

    val songs = arrayListOf<Song>()
    lateinit var songDB: SongDatabase
    var nowPos = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySongBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initPlayList()
        initSong()

        initClickListener()

        setPlayer(songs[nowPos])
    }

    private fun initClickListener() {
        binding.songDownIb.setOnClickListener {
            finish()
        }
        binding.songPlayerPlayIb.setOnClickListener {
            setPlayerStatus(true)
        }
        binding.songPauseIb.setOnClickListener {
            setPlayerStatus(false)
        }
        binding.songRepeatInactiveIv.setOnClickListener {
            isRepeating = !isRepeating

            setRepeatStatus(isRepeating)
        }
        binding.songRandomInactiveIv.setOnClickListener {
            isRandom = !isRandom

            setRandomStatus(isRandom)
        }

        binding.songLikeIb.setOnClickListener {
            setLike(songs[nowPos].isLike)
        }

        binding.songNextIb.setOnClickListener {
            moveSong(+1)
        }

        binding.songPreviousIb.setOnClickListener {
            moveSong(-1)
        }

    }

    private fun initPlayList(){
        songDB = SongDatabase.getInstance(this)!!
        songs.addAll(songDB.songDao().getSongs())
    }
    private fun initSong() {
        val spf = getSharedPreferences("song", MODE_PRIVATE)
        val songId = spf.getInt("songId",0)

        nowPos = getPlayingSongPosition(songId)

        startTimer()
        setPlayer(songs[nowPos])
    }

    private fun setLike(isLike: Boolean){
        songs[nowPos].isLike = !isLike
        songDB.songDao().updateIsLikeById(!isLike,songs[nowPos].id)

        if (!isLike){
            binding.songLikeIb.setImageResource(R.drawable.ic_my_like_on)
        } else{
            binding.songLikeIb.setImageResource(R.drawable.ic_my_like_off)
        }

    }

    private fun moveSong(direct: Int){
        if (nowPos + direct < 0){
            Toast.makeText(this,"first song",Toast.LENGTH_SHORT).show()
            return
        }

        if (nowPos + direct >= songs.size){
            Toast.makeText(this,"last song",Toast.LENGTH_SHORT).show()
            return
        }
7
        nowPos += direct

        timer.interrupt()
        startTimer()

        mediaPlayer?.release()
        mediaPlayer = null
7
        setPlayer(songs[nowPos])
    }

    private fun getPlayingSongPosition(songId: Int): Int{
        for (i in 0 until songs.size){
            if(songs[i].id == songId){
                return i
            }
        }
        return 0

    }

    private fun setPlayer(song: Song){
        binding.songTitleTv.text = song.title
        binding.songSingerNameTv.text = song.singer
        binding.songPlayTimeTv.text=String.format("%02d:%02d",song.second / 60, song.second % 60)
        binding.songTotalPlayTimeTv.text=String.format("%02d:%02d",song.playTime / 60, song.playTime % 60)
        binding.songAlbumImgIv.setImageResource(song.coverImg!!)
        binding.songMusicPlaytimeSb.progress = (song.second * 1000 / song.playTime)
        val music = resources.getIdentifier(song.music, "raw",this.packageName)
        mediaPlayer = MediaPlayer.create(this,music)

        if(song.isLike){
            binding.songLikeIb.setImageResource(R.drawable.ic_my_like_on)
        } else{
            binding.songLikeIb.setImageResource(R.drawable.ic_my_like_off)
        }

        setPlayerStatus(song.isPlaying)
    }

    private fun setPlayerStatus(isPlaying : Boolean) {

        songs[nowPos].isPlaying = isPlaying
        timer.isPlaying = isPlaying

        if(isPlaying){
            binding.songPlayerPlayIb.visibility = View.GONE
            binding.songPauseIb.visibility = View.VISIBLE
            mediaPlayer?.start()
        }
        else {
            binding.songPlayerPlayIb.visibility = View.VISIBLE
            binding.songPauseIb.visibility = View.GONE
            if(mediaPlayer?.isPlaying == true){
                mediaPlayer?.pause()
            }
        }
    }

    private fun startTimer(){
        timer = Timer(songs[nowPos].playTime,songs[nowPos].isPlaying)
        timer.start()
    }

    inner class Timer(private val playTime: Int,var isPlaying: Boolean = true):Thread(){

        private var second : Int = 0
        private var mills: Float = 0f

        override fun run() {
            super.run()
            while (true){
                if(second >=playTime){
                    break
                }

                if(isPlaying){

                    sleep(50)
                    mills += 50

                    runOnUiThread {
                        binding.songMusicPlaytimeSb.progress = ((mills / playTime)*100).toInt()
                    }

                    if (mills % 1000 == 0f) {
                        runOnUiThread {
                            binding.songPlayTimeTv.text=String.format("%02d:%02d",second / 60, second % 60)
                        }
                        second++
                    }

                }

            }
        }
    }

    // 사용자가 포커스를 잃었을 때 음악이 중지
    override fun onPause() {
        super.onPause()
        setPlayerStatus(false)
        songs[nowPos].isPlaying = false
        songs[nowPos].second = ((binding.songMusicPlaytimeSb.progress * songs[nowPos].playTime)/100)/1000
        val sharedPreferences = getSharedPreferences("song", MODE_PRIVATE)
        val editor = sharedPreferences.edit() // 에디터

        editor.putInt("songId",songs[nowPos].id)

        editor.apply()

    }

    override fun onDestroy() {
        super.onDestroy()
        timer.interrupt()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun setRepeatStatus(isRepeating : Boolean) {
        if(isRepeating) {
            binding.songRepeatInactiveIv.setColorFilter(Color.parseColor("#3f3fff"))
        }
        else {
            binding.songRepeatInactiveIv.setColorFilter(Color.parseColor("#000000"))
        }
    }

    fun setRandomStatus(isRandom : Boolean) {
        if(isRandom) {
            binding.songRandomInactiveIv.setColorFilter(Color.parseColor("#3f3fff"))
        }
        else {
            binding.songRandomInactiveIv.setColorFilter(Color.parseColor("#000000"))
        }
    }
}