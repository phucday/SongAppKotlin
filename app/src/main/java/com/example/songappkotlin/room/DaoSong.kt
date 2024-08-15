package com.example.songappkotlin.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.songappkotlin.model.Song

@Dao
interface DaoSong {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: Song)

    @Update
    suspend fun updateSong(song:Song)

    @Delete
    suspend fun deleteSong(song: Song)

    @Query("SELECT * FROM song")
    fun getAllSong(): LiveData<List<Song>>

    @Query("SELECT * FROM song WHERE song_id = :songId")
    fun getSongById(songId: Long): LiveData<Song>
}