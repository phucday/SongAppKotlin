package com.example.songappkotlin.activity

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.songappkotlin.R
import com.example.songappkotlin.adapter.SongAdapter
import com.example.songappkotlin.databinding.ActivityMainBinding
import com.example.songappkotlin.databinding.LayoutNoticeBinding
import com.example.songappkotlin.factory.SongViewModelFactory
import com.example.songappkotlin.listener.OnPlayOrPauseListener
import com.example.songappkotlin.listener.OnSongClicked
import com.example.songappkotlin.manager.SongRepository
import com.example.songappkotlin.model.Song
import com.example.songappkotlin.repository.SongRepo
import com.example.songappkotlin.room.DaoSong
import com.example.songappkotlin.room.SongDatabase
import com.example.songappkotlin.service.SongService
import com.example.songappkotlin.utils.Constants
import com.example.songappkotlin.viewmodel.SongViewModel
import java.text.SimpleDateFormat
import kotlin.random.Random

class MainActivity : AppCompatActivity(), OnSongClicked, OnPlayOrPauseListener {
    companion object {
        const val RC_NOTIFICATION = 33
    }

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val noticeBinding by lazy { LayoutNoticeBinding.inflate(layoutInflater) }
    private lateinit var musicService: SongService
    private var bound = false
    private lateinit var pathSong: String
    private val songAdapter by lazy { SongAdapter(songs, this, this) }
    private lateinit var songs: List<Song>
    private var songMain: Song? = null
    private val songRepository by lazy { SongRepository(this) }
    private var resetSong = false
    private var nav_down = true
    private val spf by lazy { SimpleDateFormat("mm:ss") }
    private lateinit var rotationAnimator: ObjectAnimator
    private var curRotation = 0f
    private var curPosition = -1
    private var oldPosition = -1
    private lateinit var waveform: FloatArray

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
            val binder = service as SongService.MusicBinder
            musicService = binder.service
            bound = true
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            bound = false
        }
    }
    private val playBackReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Constants.PLAYBACK_UPDATED -> {
                    val status = intent.getStringExtra("status")
                    updatePlaybackUI(status ?: "")
                }

                Constants.SONG_CHANGED -> {
                    val id = intent.getLongExtra("song_id", 0)
                    Log.d("checkID", "kkkk: $id")
                    val title = intent.getStringExtra("song_title").toString()
                    Log.d("checkID", "titile: $title")
                    val artist = intent.getStringExtra("song_artist").toString()
                    pathSong = intent.getStringExtra("song_path").toString()
                    val image = intent.getStringExtra("song_image").toString()
                    songMain = Song(id, title, artist, pathSong, image)
                    updateSongInfoUI(songMain!!)
//                    updateSongInfoUI(title, artist,image )
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        permission_post_notification()

        setUp()
        getDataSong()
        setBindService()
        setReceiver()

        loadSongs()
        userClick()
    }

    private fun setWaveformSeekBar() {
        val rd = Random
        waveform = FloatArray(28)
        val min = 0.2f
        val max = 1f
        for (i in waveform.indices) {
            waveform[i] = min + (max - min) * rd.nextFloat()
        }
        binding.processSong.setWaveform(waveform)
    }

    private fun userClick() {
        onPlayOrPauseClick()
        onNextClick()
        onPreviousClick()
        next10s()
        previous10s()
        shareSong()
        notice()
        resetSong()
        likeSong()
        testVisibleGone()
        // testNav()
    }

    private lateinit var songRepo: SongRepo
    private lateinit var songDb: SongDatabase
    private lateinit var songDao: DaoSong
    private lateinit var songViewmodel: SongViewModel
    private lateinit var songViewModelFactory: SongViewModelFactory
    private var isLikedSong = false

    private fun likeSong() {
        songDb = SongDatabase.getInstance(this)
        songDao = songDb.songDao()
        songRepo = SongRepo(songDao)
        songViewModelFactory = SongViewModelFactory(songRepo)
        songViewmodel = ViewModelProvider(this, songViewModelFactory)[SongViewModel::class.java]

        songMain?.let { checkIsLikedSong(it.id) }
        binding.btnLikeSong.setOnClickListener {
            if (isLikedSong) {
                isLikedSong = false
                songViewmodel.deleteSong(songMain!!)
                binding.btnLikeSong.setImageResource(R.drawable.icon_favorite)
            } else {
                isLikedSong = true
                songViewmodel.insertSong(songMain!!)
                binding.btnLikeSong.setImageResource(R.drawable.icon_like_filled)
            }
        }
    }

    private fun checkIsLikedSong(songId: Long) {
        songViewmodel.getSongById(songId).observe(this, Observer { song ->
            if (song == null) {
                binding.btnLikeSong.setImageResource(R.drawable.icon_favorite)
                isLikedSong = false
            } else {
                binding.btnLikeSong.setImageResource(R.drawable.icon_like_filled)
                isLikedSong = true
            }
        })
    }

    private fun updateSongTime() {
        val handler = android.os.Handler()
        handler.postDelayed({
            binding.tvStartSong.text = spf.format(musicService.getCurPosition())
            binding.processSong.progress = musicService.getCurPosition()
            updateSongTime()
        }, 200)
    }

    private fun setUp() {
        initRotationAnimator()
        binding.imgBack.setOnClickListener { onBackPressed() }
    }

    private fun getDataSong() {
        pathSong = intent.extras?.getString("pathSong").toString()
        val imageUriSong = intent.extras?.getString("imageUriSong").toString()
        val artistSong = intent.extras?.getString("artistSong").toString()
        val nameSong = intent.extras?.getString("titleSong").toString()
        val idSong = intent.extras?.getLong("idSong") as Long
        curPosition = intent.extras?.getInt("positionSong") ?: 0
        val song = Song(idSong, nameSong, artistSong, pathSong, imageUriSong)
        songMain = song
        Log.d("curPositiongetDataSong", curPosition.toString())
        Glide.with(this).load(Uri.parse(imageUriSong))
            .into(binding.imgCd)
        startRotation()
        binding.nameSong.text = nameSong
        Log.d("nameSOngSOng: ", nameSong)
        binding.tvDepeche.text = artistSong
        setWaveformSeekBar()
    }

    private fun loadSongs() {
        songs = songRepository.getAllSongs()
        Log.d("songssongs", songs.size.toString())
        binding.rcvSongPlay.adapter = songAdapter
    }

    private fun setImagePlayOrPauseItem() {
        if (oldPosition != curPosition) {
            if (oldPosition != -1) {
                Log.d("oldPosition != -1", "oldPosition: $oldPosition")
                songAdapter.setIsPlaying(false, oldPosition)
            }
        }
        Log.d("oldPositionNN ", "oldPosition: $oldPosition")
        oldPosition = curPosition
        curPosition = musicService.getCurIndex()
        songAdapter.setIsPlaying(musicService.isPlaying(), curPosition)
        Log.d("curPositionsetViewModel", " $curPosition")
    }

    private fun setBindService() {
        val intent = Intent(this, SongService::class.java)
        if (!bound) {
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
//        bindService(intent, connection, Context.BIND_AUTO_CREATE)
        intent.putExtra("pathS", pathSong)
        startService(intent)
    }

    private fun setReceiver() {
        val filter = IntentFilter()
        filter.addAction(Constants.PLAYBACK_UPDATED)
        filter.addAction(Constants.SONG_CHANGED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(playBackReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(playBackReceiver, filter)
        }
    }

    private fun updatePlaybackUI(status: String) {
        when (status) {
            "playing" -> {
                binding.btnPlayPause.setImageResource(R.drawable.icon_pause)
                startRotation()
            }

            "paused" -> {
                binding.btnPlayPause.setImageResource(R.drawable.icon_play)
                pauseRotation()
            }
        }
        binding.rcvSongPlay.scrollToPosition(curPosition)
        setImagePlayOrPauseItem()

        updateProcess()
    }

    private fun updateProcess() {
        binding.tvEndSong.text = spf.format(musicService.getDuration()) + ""
        binding.processSong.max = musicService.getDuration()
        binding.processSong.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                if (b) {
                    musicService.seekTo(binding.processSong.progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // musicService.seekTo(binding.processSong.getProgress());
            }
        })
        updateSongTime()
    }

    private fun updateSongInfoUI(song: Song) {
        // Update UI with new song information
        if (musicService.isPlaying()) {
            binding.btnPlayPause.setImageResource(R.drawable.icon_pause)
            binding.nameSong.text = song.title
            binding.tvDepeche.text = song.artist
            Glide.with(this).load(Uri.parse(song.albumArtUri))
                .into(binding.imgCd)
        }

        curPosition = musicService.getCurIndex()
        binding.rcvSongPlay.scrollToPosition(curPosition)

        Log.d("curPosiSongInfoUI", " $curPosition")
        setImagePlayOrPauseItem()

        setWaveformSeekBar()
        updateProcess()
    }

    private fun onPlayOrPauseClick() {
        binding.btnPlayPause.setOnClickListener {
            if (musicService.isPlaying()) {
                if (bound) musicService.pause()
                songAdapter.setIsPlaying(false, curPosition)
            } else {
                if (bound) musicService.play()
                songAdapter.setIsPlaying(true, curPosition)
            }
        }
    }

    private fun onNextClick() {
        binding.btnNext.setOnClickListener {
            if (bound) musicService.next()
        }
    }

    private fun onPreviousClick() {
        binding.btnPrevious.setOnClickListener {
            if (bound) musicService.previous()
        }
    }

    private fun next10s() {
        binding.btnNext10s.setOnClickListener {
            musicService.next10s()
        }
    }

    private fun previous10s() {
        binding.btnBack10s.setOnClickListener {
            musicService.previous10s()
        }
    }

    private fun shareSong() {
        binding.btnShare.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.setAction(Intent.ACTION_SEND)
            shareIntent.setType("audio/*")
            shareIntent.putExtra(
                Intent.EXTRA_STREAM,
                Uri.parse(pathSong)
            )
            startActivity(Intent.createChooser(shareIntent, "Sharing File Song"))
        }
    }

    private fun notice() {
        binding.btnNotice.setOnClickListener {
            Toast.makeText(this, "Nothing happen ! Keep enjoy", Toast.LENGTH_SHORT).show()
            val dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(noticeBinding.getRoot())
            val window = dialog.window ?: return@setOnClickListener
            window.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val wdAttributes = window.attributes
            wdAttributes.gravity = Gravity.BOTTOM
            window.attributes = wdAttributes
            dialog.setCancelable(true)

            noticeBinding.btnCloseFb.setOnClickListener { vi -> dialog.dismiss() }
            noticeBinding.btnSendFb.setOnClickListener { vi ->
                Toast.makeText(this, "Sent", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            dialog.show()
        }
    }

    private fun resetSong() {
        binding.btnRenew.setOnClickListener { v ->
            resetSong = !resetSong
            Log.d("PhucTAG", "resetSong: $resetSong")
            if (resetSong) {
                musicService.resetSong(true)
                binding.btnRenew.setImageResource(R.drawable.icon_change_finish)
            } else {
                musicService.resetSong(false)
                binding.btnRenew.setImageResource(R.drawable.icon_renew)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (bound) {
            unbindService(connection)
            bound = false
        }
        unregisterReceiver(playBackReceiver)
    }

    private fun testVisibleGone() {
        binding.navUpDown.setOnClickListener { v ->
            pauseRotation()
            if (nav_down) {
                binding.navUpDown.setImageResource(R.drawable.icon_down)
                binding.imgCd.visibility = View.GONE
                binding.btnPlayPause.visibility = View.GONE
                binding.btnNext.visibility = View.GONE
                binding.btnPrevious.visibility = View.GONE
                binding.btnNext10s.visibility = View.GONE
                binding.btnBack10s.visibility = View.GONE
                binding.btnNotice.visibility = View.GONE
                binding.btnShare.visibility = View.GONE
                binding.btnRenew.visibility = View.GONE
                nav_down = false
            } else {
                binding.navUpDown.setImageResource(R.drawable.icon_up)
                binding.imgCd.visibility = View.VISIBLE
                binding.btnPlayPause.visibility = View.VISIBLE
                binding.btnNext.visibility = View.VISIBLE
                binding.btnPrevious.visibility = View.VISIBLE
                binding.btnNext10s.visibility = View.VISIBLE
                binding.btnBack10s.visibility = View.VISIBLE
                binding.btnNotice.visibility = View.VISIBLE
                binding.btnShare.visibility = View.VISIBLE
                binding.btnRenew.visibility = View.VISIBLE
                if (musicService.isPlaying()) {
                    resumeRotation()
                }
                nav_down = true
            }
        }
    }

    private fun permission_post_notification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                RC_NOTIFICATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RC_NOTIFICATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            }
        }
    }

    private fun initRotationAnimator() {
        rotationAnimator = ObjectAnimator.ofFloat(binding.imgCd, "rotation", 0f, 360f)
        rotationAnimator.setDuration(10000)
        rotationAnimator.interpolator = LinearInterpolator()
        rotationAnimator.repeatCount = ObjectAnimator.INFINITE
    }

    private fun startRotation() {
        rotationAnimator.setFloatValues(curRotation, curRotation + 360f)
        rotationAnimator.start()
    }

    private fun pauseRotation() {
        curRotation = binding.imgCd.rotation
        rotationAnimator.cancel()
    }

    private fun resumeRotation() {
        startRotation()
    }

    private fun testNav() {
        binding.navUpDown.setOnClickListener { v ->
            collapseView(binding.ctnHeader)
            Log.d("testNavtestNav: ", nav_down.toString() + "")
        }
    }

    private fun collapseView(view: View) {
        val initialHeight = view.measuredHeight

        val animator = ObjectAnimator.ofInt(view, "navUp", initialHeight, 0)
        animator.setDuration(1000) // Thời gian thu gọn

        animator.addUpdateListener { animation: ValueAnimator ->
            view.layoutParams.height = animation.animatedValue as Int
            view.requestLayout()
        }
        animator.start()
    }

    private fun expandView(view: View) {
    }

    override fun onSongClicked(song: Song, position: Int) {
        //        if(!pathSong.equals(song.getPath())){
        if (curPosition != position) {
            checkIsLikedSong(song.id)
            pathSong = song.path
            Glide.with(this).load(Uri.parse(song.albumArtUri))
                .into(binding.imgCd)
//            binding.imgCd.animation = animation
            setBindService()
            Log.d("ssssafter", curPosition.toString() + "")
        }
    }

    override fun playOrPause(song: Song, position: Int) {
        if (curPosition != oldPosition) {
            onSongClicked(song, position)
        } else {
            if (musicService.isPlaying()) {
                musicService.pause()
            } else {
                musicService.play()
            }
        }
    }
}