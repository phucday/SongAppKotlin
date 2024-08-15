package com.example.songappkotlin.model

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Song")
data class Song(
     @PrimaryKey
     @ColumnInfo("song_id")
     val id: Long,
     @ColumnInfo("song_title")
     val title: String,
     @ColumnInfo("song_artist")
     val artist: String,
     @ColumnInfo("song_path")
     val path: String,
     @ColumnInfo("song_albumArtUri")
     val albumArtUri: String
    )