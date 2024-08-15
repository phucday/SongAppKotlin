package com.example.songappkotlin.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.songappkotlin.R
import com.example.songappkotlin.adapter.SongAdapter
import com.example.songappkotlin.databinding.ActivitySearchBinding
import com.example.songappkotlin.factory.SongViewModelFactory
import com.example.songappkotlin.listener.OnPlayOrPauseListener
import com.example.songappkotlin.listener.OnSongClicked
import com.example.songappkotlin.manager.SongRepository
import com.example.songappkotlin.model.Song
import com.example.songappkotlin.repository.SongRepo
import com.example.songappkotlin.room.DaoSong
import com.example.songappkotlin.room.SongDatabase
import com.example.songappkotlin.utils.EncodingConverter
import com.example.songappkotlin.viewmodel.SongViewModel

class SearchActivity : AppCompatActivity(), OnSongClicked, OnPlayOrPauseListener {

    private val binding by lazy { ActivitySearchBinding.inflate(layoutInflater) }
    private val songRepository by lazy {SongRepository(this) }
    private lateinit var songRepo: SongRepo
    private lateinit var songDb: SongDatabase
    private lateinit var songDao: DaoSong
    private lateinit var songViewmodel: SongViewModel
    private lateinit var songViewModelFactory: SongViewModelFactory
    private lateinit var songAdapter: SongAdapter
    //    private var listSongFavorite: ArrayList<Song> ?= null
    private lateinit var listSong: ArrayList<Song>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        listSong = ArrayList()
        setup()

        searchSong()
    }

    private fun searchSong() {
        binding.imageBack.setOnClickListener { onBackPressed() }
        binding.inputSearch.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(charSequence: CharSequence?, p1: Int, p2: Int, p3: Int) {

                //called as and when user type/remove letter
                Log.d("onTextChanged", "onTextChanged")
                // dùng class FilterSourceList
//                songAdapter.filter.filter(charSequence)

                // không dùng class FilterSourceList
                if(!charSequence.isNullOrEmpty()){
                    var filterList = ArrayList<Song>()
                    for(song in listSong){
                        if(song.title.uppercase().contains(charSequence.toString().uppercase())){
                            filterList.add(song)
                            Log.d("sizeFileterList",filterList.size.toString())
                        }
                    }
                    songAdapter.updateSongList(filterList)
                }else{
                    songAdapter.updateSongList(listSong)
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })
    }

    private fun setup(){
        songDb = SongDatabase.getInstance(this)
        songDao = songDb.songDao()
        songRepo = SongRepo(songDao)
        songViewModelFactory = SongViewModelFactory(songRepo)
        songViewmodel = ViewModelProvider(this, songViewModelFactory)[SongViewModel::class.java]
        listSong = songRepository.getAllSongs() as ArrayList<Song>
        songAdapter = SongAdapter(listSong,this,this)
        binding.rcvSong.adapter = songAdapter
    }

    override fun onSongClicked(song: Song, po: Int) {
        TODO("Not yet implemented")
    }

    override fun playOrPause(song: Song, po: Int) {
        TODO("Not yet implemented")
    }
}