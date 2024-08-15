package com.example.songappkotlin.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.songappkotlin.model.Song
import com.example.songappkotlin.repository.SongRepo
import kotlinx.coroutines.launch

class SongViewModel(private val songRepo: SongRepo): ViewModel() {
    val allSongs: LiveData<List<Song>> get() = songRepo.getAllSong()
    private val _allSongs = MutableLiveData<List<Song>>()

    fun getSongById(songId: Long):LiveData<Song>{return songRepo.getSongById(songId)}
    fun insertSong(song: Song) = viewModelScope.launch { songRepo.insertSong(song) }
    fun updateSong(song: Song) = viewModelScope.launch { songRepo.updateSong(song) }
    fun deleteSong(song: Song) = viewModelScope.launch { songRepo.deleteSong(song) }
}