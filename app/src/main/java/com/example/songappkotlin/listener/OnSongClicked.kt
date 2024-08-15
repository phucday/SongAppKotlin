package com.example.songappkotlin.listener

import com.example.songappkotlin.model.Song

interface OnSongClicked {
    fun onSongClicked(song: Song, po:Int)
}