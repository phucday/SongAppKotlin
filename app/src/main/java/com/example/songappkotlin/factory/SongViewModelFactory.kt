package com.example.songappkotlin.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.songappkotlin.repository.SongRepo
import com.example.songappkotlin.viewmodel.SongViewModel

class SongViewModelFactory(private val songRepo: SongRepo): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(SongViewModel::class.java)){
            return SongViewModel(songRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}