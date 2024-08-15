package com.example.songappkotlin.service

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.songappkotlin.R
import com.example.songappkotlin.activity.ListSongActivity
import com.example.songappkotlin.application.MyApplication
import com.example.songappkotlin.listener.OnSongCompleteListener
import com.example.songappkotlin.manager.PlayManager
import com.example.songappkotlin.model.Song
import com.example.songappkotlin.utils.Constants
import com.example.songappkotlin.utils.Utils

class SongService: Service(), OnSongCompleteListener {
    private val binder = MusicBinder()
    private lateinit var playManager: PlayManager
    private var songg: Song? = null
    private var pathSong = ""

    override fun onCreate() {
        super.onCreate()
        playManager = PlayManager.getInstance(this)
        playManager.setOnSongCompleteListener(this)
    }
    override fun onBind(p0: Intent?): IBinder = binder

    inner class MusicBinder: Binder(){
        val service: SongService
            get() = this@SongService
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent != null && intent.action == null){
            when{
                pathSong.isEmpty() -> {
                    pathSong = intent.getStringExtra("pathS")?: ""
                    playSongByPath(pathSong)
                }
                pathSong == intent.getStringExtra("pathS") -> {
                    playUI()
                }
                pathSong != intent.getStringExtra("pathS") -> {
                    if(getCurSong().path == intent.getStringExtra("pathS")){
                        pathSong = getCurSong().path
                        songChanged()
                        playUI()
                    }else{
                        pathSong = intent.getStringExtra("pathS") ?: ""
                        playSongByPath(pathSong)
                    }
                }
            }
        }

        intent?.action?.let { action ->
            when(action){
                Constants.PLAY -> play()
                Constants.PAUSE -> pause()
                Constants.NEXT -> next()
                Constants.PREVIOUS -> previous()
            }
        }
        return START_NOT_STICKY
    }

    fun previous() {
        playManager.previous()
        songChanged()
    }

    fun next() {
        playManager.next()
        songChanged()
        pathSong = songg?.path ?: ""
    }

    internal fun pause() {
        playManager.pause()
        pauseUI()
        showNotification()
    }

    fun play() {
       playManager.play()
        sendBroadcast(Intent(Constants.PLAYBACK_UPDATED).apply {
            putExtra("status", "playing")
        })
        showNotification()
    }

    private fun songChanged(){
        songg = getCurSong()
        sendBroadcast(Intent(Constants.SONG_CHANGED).apply {
            putExtra("song_id", songg!!.id)
            Log.d("idSongServiceChanged", "songId: ${songg!!.id}")
            putExtra("song_title", songg!!.title)
            putExtra("song_artist", songg!!.artist)
            putExtra("song_path", songg!!.path)
            putExtra("song_image", songg!!.albumArtUri)
        })
        showNotification()
    }

    private fun pauseUI() {
        sendBroadcast(Intent(Constants.PLAYBACK_UPDATED).apply {
            putExtra("status", "paused")
        })
    }

    private fun playUI() {
        playManager.continueSong()
        sendBroadcast(Intent(Constants.PLAYBACK_UPDATED).apply {
            putExtra("status", "playing")
        })
    }

    private fun playSongByPath(path: String) {
        playManager.playSongByPath(path)
        songChanged()
    }
    private fun getCurSong():Song{
        return playManager.getCurSong()
    }
    fun isPlaying():Boolean{
        return playManager.isPlaying()
    }
    fun getDuration():Int{
        return playManager.getDuration()
    }
    fun getCurPosition(): Int{
        return playManager.getCurPosition()
    }
    fun next10s(){
        playManager.next10s()
    }
    fun previous10s(){
        playManager.previous10s()
    }
    fun resetSong(yes: Boolean){
        playManager.resetSong(yes)
    }
    internal fun getCurIndex(): Int{
        return playManager.getCurIndex()
    }
    fun seekTo(position: Int){
        playManager.seekTo(position)
    }

    override fun onSongCompleted() {
       next()
    }

    private fun showNotification() {
        val notificationIntent = Intent(this, ListSongActivity::class.java)
        val notificationIntentTest = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntentTest, PendingIntent.FLAG_MUTABLE)

        val bitmap = songg?.albumArtUri?.let { Utils.getBitmapFromUri(this, Uri.parse(it)) }


        val actionIcon = if (isPlaying()) "PAUSE" else "PLAY"
        val iconPlayOrPause = if (isPlaying()) R.drawable.icon_pause else R.drawable.icon_play

        val notificationTest = NotificationCompat.Builder(this, MyApplication.CHANNEL_ID)
            .setSmallIcon(R.drawable.music_note)
            .setLargeIcon(bitmap)
            .setContentText(songg?.artist)
            .setContentIntent(pendingIntent)
            .setSound(null)
            .setOngoing(false)
            .addAction(R.drawable.icon_previous, "Previous", getPendingIntent(this, "PREVIOUS"))
            .addAction(iconPlayOrPause, actionIcon, getPendingIntent(this, actionIcon))
            .addAction(R.drawable.icon_next, "Next", getPendingIntent(this, "NEXT"))
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0, 1, 2))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        startForeground(3443, notificationTest)
        Log.d("PHUCVP", "showNotification")
    }

    private fun getPendingIntent(context: Context, action: String): PendingIntent {
        val intent = Intent(context, SongService::class.java).apply {
            setAction(action)
        }
        Log.d("PVPLOG", "getPendingIntent $action")
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_MUTABLE)
    }
}