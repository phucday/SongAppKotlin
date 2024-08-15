package com.example.songappkotlin.listener

import com.example.songappkotlin.model.Song

interface OnDeleteFavoriteSong {
    fun onClickDeleteSong(song: Song, position: Int)
}