package com.example.songappkotlin.manager

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.example.songappkotlin.listener.OnSongCompleteListener
import com.example.songappkotlin.model.Song
import java.io.IOException

class PlayManager private constructor(context: Context){
    private val mediaPlayer = MediaPlayer()
    private lateinit var playList: List<Song>
    private var currentIndex = 0
    private val songRepository = SongRepository(context)
    private var isPrepared = false
    private var countPath = 0
    private lateinit var song: Song
    private var songCompleteListener: OnSongCompleteListener? = null

    init{
        loadPlayList()
        mediaPlayer.setOnCompletionListener {
            songCompleteListener?.onSongCompleted()
        }
    }

    fun setOnSongCompleteListener(listener: OnSongCompleteListener){
        this.songCompleteListener = listener
    }
    private fun loadPlayList(){
        playList = songRepository.getAllSongs()
    }

    companion object{
        @Volatile
        private var instance: PlayManager? = null
        fun getInstance(context: Context): PlayManager{
            return instance ?: synchronized(this){
                instance ?: PlayManager(context).also { instance = it }
            }
        }
    }

    fun refreshPlayList(){
        loadPlayList()
    }
    fun setPlayList(songs: List<Song>){
        playList = songs
        currentIndex = 0
    }
    fun isPlaying():Boolean{
        return mediaPlayer.isPlaying
    }
    fun continueSong(){
        mediaPlayer.start()
    }
    private fun prepareCurrentSong(){
        if(currentIndex >= 0 && currentIndex <= playList.size -1){
            song = playList[currentIndex]
            try {
                mediaPlayer.reset()
                mediaPlayer.setDataSource(song.path)
                mediaPlayer.prepare()
                isPrepared = true
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }
    fun play(){
        if(!mediaPlayer.isPlaying){
            if(!isPrepared){
                prepareCurrentSong()
            }
            mediaPlayer.start()
        }
    }
    fun pause(){
        if(mediaPlayer.isPlaying){
            mediaPlayer.pause()
        }
    }
    fun next(){
        if(currentIndex <= playList.size -1){
            currentIndex++
            if(currentIndex >= playList.size){
                currentIndex = 0
            }
            prepareCurrentSong()
            play()
        }
    }
    fun previous(){
        if(0 <= currentIndex){
            currentIndex--
            if(currentIndex < 0){
                currentIndex = playList.size -1
            }
            prepareCurrentSong()
            play()
        }
    }
    fun seekTo(position: Int){
        mediaPlayer.seekTo(position)
    }
    fun next10s(){
        val timeNext10s = mediaPlayer.currentPosition + 10000
        if(timeNext10s <= mediaPlayer.duration){
            mediaPlayer.seekTo(timeNext10s)
        }else{
            next()
        }
    }
    fun previous10s(){
        val timePrevious10s = mediaPlayer.currentPosition - 10000
        if(timePrevious10s >= 0 ){
            mediaPlayer.seekTo(timePrevious10s)
        }else{
            previous()
        }
    }
    fun playSongByPath(path:String){
        try {
            countPath = 0
            mediaPlayer.reset()
            mediaPlayer.setDataSource(path)
            mediaPlayer.prepare()
            mediaPlayer.start()
            isPrepared = true
            song = getCurrentSongByPath(path)?: playList[0]
            currentIndex = countPath
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    private fun getCurrentSongByPath(path: String): Song? {
        for(song in playList){
            if(song.path == path){
                return song
            }
            countPath++
        }
        return null
    }
    fun getDuration(): Int{
        return mediaPlayer.duration
    }
    fun getCurPosition(): Int{
        return mediaPlayer.currentPosition
    }
    fun resetSong(yes:Boolean){
        if(yes){
            mediaPlayer.setOnCompletionListener {
                mediaPlayer.seekTo(0)
                mediaPlayer.start()
            }
        }else{
            songCompleteListener?.onSongCompleted()
        }
    }
    fun getCurSong(): Song {
        return song
    }
    fun getCurIndex():Int{
        return currentIndex
    }

}