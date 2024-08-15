package com.example.songappkotlin.listener

import com.example.songappkotlin.model.Song

interface OnPlayOrPauseListener {
    fun playOrPause(song: Song, po:Int)
}