package com.example.songappkotlin.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.songappkotlin.model.Song

@Database(entities = [Song::class], version = 1, exportSchema = false)
abstract class SongDatabase: RoomDatabase() {
    abstract fun songDao(): DaoSong
    companion object{
        @Volatile
        private var INSTANCE: SongDatabase ?= null
        fun getInstance(context: Context): SongDatabase{
            synchronized(this){
                var instance = INSTANCE
                if(instance == null){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        SongDatabase::class.java,
                        "database_song"
                    ).build()
                }
                return instance
            }
        }
    }
}