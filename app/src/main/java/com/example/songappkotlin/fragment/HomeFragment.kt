package com.example.songappkotlin.fragment

import android.Manifest
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.songappkotlin.R
import com.example.songappkotlin.activity.ListSongActivity
import com.example.songappkotlin.activity.ListSongActivity.Companion
import com.example.songappkotlin.activity.MainActivity
import com.example.songappkotlin.activity.SearchActivity
import com.example.songappkotlin.adapter.SongAdapter
import com.example.songappkotlin.databinding.FragmentHomeBinding
import com.example.songappkotlin.listener.OnPlayOrPauseListener
import com.example.songappkotlin.listener.OnSongClicked
import com.example.songappkotlin.manager.SongRepository
import com.example.songappkotlin.model.Song
import com.example.songappkotlin.service.SongService
import com.example.songappkotlin.utils.Constants
import com.example.songappkotlin.utils.PermissionUtil

class HomeFragment : Fragment(), OnSongClicked, OnPlayOrPauseListener {

    private val binding by lazy{ FragmentHomeBinding.inflate(layoutInflater)}
    private lateinit var songAdapter: SongAdapter
    private val songRepository by lazy { activity?.let { SongRepository(it.applicationContext) } }
    private lateinit var songList: List<Song>
    private var curPosition = -1
    private var oldPosition = -1
    private var pathSong = ""
    private lateinit var musicService: SongService
    private var bound = false
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
        override fun onReceive(p0: Context?, intent: Intent?) {
            when(intent?.action){
                Constants.PLAYBACK_UPDATED -> {
                    val status = intent.getStringExtra("status")
                    updatePlaybackUI(status.toString())
                }
                Constants.SONG_CHANGED -> {
                    pathSong = intent.getStringExtra("song_path").toString()
                    updatePlaybackUI("")
                }
            }
        }
    }

    companion object{
        private const val REQUEST_CODE = 1
        private const val REQUEST_CODE_READ_EXTERNAL_STORAGE = 1
        private const val REQUEST_CODE_MANAGE_EXTERNAL_STORAGE = 2
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                loadSongs()
            } else {
                Toast.makeText(requireActivity(), "Please give us permission", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getPermissionFileAudio()
        setBindService()
        setReceiver()
        searchSong()
    }

    private fun searchSong() {
        binding.imageSearch.setOnClickListener {
            startActivity(Intent(requireActivity(),SearchActivity::class.java))
        }
    }

    private fun setBindService(){
        val intent = Intent(requireActivity(), SongService::class.java)
        if(!bound){
            requireActivity().bindService(intent,connection,Context.BIND_AUTO_CREATE)
        }
    }

    private fun setReceiver(){
        val filter = IntentFilter()
        filter.addAction(Constants.PLAYBACK_UPDATED)
        filter.addAction(Constants.SONG_CHANGED)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            requireActivity().registerReceiver(playBackReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        }else{
            requireActivity().registerReceiver(playBackReceiver, filter)
        }
    }
    private fun getPermissionFileAudio(){
        if(PermissionUtil.checkReadAudioPermission(requireActivity().applicationContext)){
            loadSongs()
        }else requestPermissionFileAudio()
    }

    private fun getPermissionAllFile(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11 (API 30) và cao hơn
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.setData(Uri.parse("package:${requireActivity().packageName}"))
                startActivityForResult(
                    intent,
                    REQUEST_CODE_MANAGE_EXTERNAL_STORAGE
                )
            } else {
                loadSongs()
            }
        }
        else {
            // Android 10 (API 29) và thấp hơn
            if (ContextCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_CODE_READ_EXTERNAL_STORAGE
                )
            } else {
                loadSongs()
            }
        }
    }

    private fun requestPermissionFileAudio(){
        if(PermissionUtil.shouldShowRequestPermissionRationaleForReadAudio(requireActivity())){
            // set intent to setting
        }else{
            if(PermissionUtil.checkReadAudioPermission(requireActivity().applicationContext)){
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }else{
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
            }
        }
    }

    private fun updatePlaybackUI(status:String){
        setImagePlayOrPauseItem()
    }

    private fun setImagePlayOrPauseItem() {
        var temp = musicService.getCurIndex()
        if(oldPosition != curPosition){
            if(oldPosition != -1){
                songAdapter.setIsPlaying(false,oldPosition)
            }
        }
        if(curPosition != temp){
            oldPosition = curPosition
            curPosition = musicService.getCurIndex()
        }
        binding.rcvSong.scrollToPosition(curPosition)
        songAdapter.setIsPlaying(musicService.isPlaying(),curPosition)
        songAdapter.setIsPlaying(false,oldPosition)
    }


    private fun loadSongs() {
        songList = songRepository!!.getAllSongs()
        Log.d("checkSongsSize", songList.size.toString())
//        Log.d("checkSongsId", "idSong1: ${songList[0].id} and idSong2: ${songList[1].id}")
        songAdapter = SongAdapter(songList,this,this)
        binding.rcvSong.adapter = songAdapter
    }

    override fun onSongClicked(song: Song, po: Int) {
//        if (curPosition != position) {
        val intentSong = Intent(requireActivity(), MainActivity::class.java)
        val bundle = Bundle()
        bundle.putLong("idSong", song.id)
        bundle.putString("artistSong", song.artist)
        bundle.putString("titleSong", song.title)
        bundle.putString("pathSong", song.path)
        bundle.putString("imageUriSong", song.albumArtUri)
        bundle.putInt("positionSong", po)
        Log.d("onSongClickedPo", po.toString() + "")
        intentSong.putExtras(bundle)
        startActivity(intentSong)

    }

    override fun playOrPause(song: Song, po: Int) {
        if(bound){
            if(curPosition != po){
                onSongClicked(song,po)
            }else{
                if(musicService.isPlaying()){
                    musicService.pause()
                }else{
                    musicService.play()
                }
            }
        }else{
            onSongClicked(song,po)
        }
    }

}