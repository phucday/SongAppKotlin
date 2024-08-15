package com.example.songappkotlin.repository

import androidx.lifecycle.LiveData
import com.example.songappkotlin.model.Song
import com.example.songappkotlin.room.DaoSong

class SongRepo(private val dao: DaoSong) {
    fun getAllSong(): LiveData<List<Song>>{
        return dao.getAllSong()
    }
    fun getSongById(songId: Long):LiveData<Song>{
        return dao.getSongById(songId)
    }
    suspend fun insertSong(song:Song){
        dao.insertSong(song)
    }
    suspend fun updateSong(song:Song){
        dao.updateSong(song)
    }
    suspend fun deleteSong(song:Song){
        dao.deleteSong(song)
    }
}