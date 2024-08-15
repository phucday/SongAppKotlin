package com.example.songappkotlin.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.songappkotlin.R
import com.example.songappkotlin.activity.MainActivity
import com.example.songappkotlin.adapter.SongFavoriteAdapter
import com.example.songappkotlin.databinding.FragmentFavouriteBinding
import com.example.songappkotlin.factory.SongViewModelFactory
import com.example.songappkotlin.listener.OnDeleteFavoriteSong
import com.example.songappkotlin.listener.OnSongClicked
import com.example.songappkotlin.model.Song
import com.example.songappkotlin.repository.SongRepo
import com.example.songappkotlin.room.DaoSong
import com.example.songappkotlin.room.SongDatabase
import com.example.songappkotlin.viewmodel.SongViewModel

class FavouriteFragment : Fragment(), OnSongClicked, OnDeleteFavoriteSong {

    private val binding by lazy { FragmentFavouriteBinding.inflate(layoutInflater) }
    private lateinit var songRepo: SongRepo
    private lateinit var songDb: SongDatabase
    private lateinit var songDao: DaoSong
    private lateinit var songViewmodel: SongViewModel
    private lateinit var songViewModelFactory: SongViewModelFactory
    private lateinit var songFavouriteAdapter: SongFavoriteAdapter
//    private var listSongFavorite: ArrayList<Song> ?= null
    private lateinit var listSongFavorite: ArrayList<Song>
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

        listSongFavorite = ArrayList()
        setup()
//        showSongFavorite()
    }

//    private fun showSongFavorite() {
//        songViewmodel.allSongs.observe(requireActivity(), Observer { songs ->
//            Log.d("comeshowSongFavorite", " ${songs.size}")
//            songFavouriteAdapter = SongFavoriteAdapter(songs, this, this)
//            binding.rcvSong.adapter = songFavouriteAdapter
////            songFavouriteAdapter.updateSongs(songs)
//            songFavouriteAdapter.notifyDataSetChanged()
//        })
//    }

    private fun showSongFavorite() {
        songViewmodel.allSongs.observe(requireActivity(), Observer { songs ->
            Log.d("comeshowSongFavorite", " ${songs.size}")
            if(listSongFavorite.size != 0) listSongFavorite.clear()
            listSongFavorite = songs as ArrayList<Song>
            songFavouriteAdapter = SongFavoriteAdapter(listSongFavorite, this, this)
            binding.rcvSong.adapter = songFavouriteAdapter
//            songFavouriteAdapter.updateSongs(songs)
            songFavouriteAdapter.notifyDataSetChanged()
        })
    }


    private fun setup(){
        songDb = SongDatabase.getInstance(requireActivity())
        songDao = songDb.songDao()
        songRepo = SongRepo(songDao)
        songViewModelFactory = SongViewModelFactory(songRepo)
        songViewmodel = ViewModelProvider(requireActivity(), songViewModelFactory)[SongViewModel::class.java]

    }

    override fun onStart() {
        super.onStart()
        showSongFavorite()
    }

    override fun onResume() {
        super.onResume()
//        showSongFavorite()
    }
    override fun onSongClicked(song: Song, po: Int) {
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

    override fun onClickDeleteSong(song: Song, position: Int) {
        songViewmodel.deleteSong(song)
        listSongFavorite.remove(song)
        songFavouriteAdapter.notifyItemRemoved(position)
        songFavouriteAdapter.notifyItemRangeChanged(position,songFavouriteAdapter.itemCount)
    }
}