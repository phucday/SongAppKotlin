package com.example.songappkotlin.adapter

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.songappkotlin.R
import com.example.songappkotlin.databinding.ItemSongBinding
import com.example.songappkotlin.listener.OnPlayOrPauseListener
import com.example.songappkotlin.listener.OnSongClicked
import com.example.songappkotlin.model.Song
import com.example.songappkotlin.utils.FilterSourceList
import java.util.logging.Filter

class SongAdapter(
     var listSong: List<Song>,
    private val onSongClicked: OnSongClicked,
    private val onPlayOrPauseItem: OnPlayOrPauseListener
    ): RecyclerView.Adapter<SongAdapter.SongViewHolder>(), Filterable {

    private var currentPlayingPosition = -1
    private var isPlaying = false
    private var filterSourceList: FilterSourceList ?= null
    private var filterSList = listSong

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val itemSongBinding = ItemSongBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return SongViewHolder(itemSongBinding)
    }

    override fun getItemCount(): Int {
        return listSong.size
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bindData(listSong[position],position)
    }

    override fun onBindViewHolder(
        holder: SongViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if(payloads.isNotEmpty()){
            for(i in payloads){
                val isPlaying = i as Boolean
                if(isPlaying){
                    holder.changeState(true)
                }else{
                    holder.changeState(false)
                }
            }
        }else{
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    fun setIsPlaying(playing:Boolean, position: Int){
        currentPlayingPosition = position
        if(position != -1){
            this.isPlaying = playing
            notifyItemChanged(currentPlayingPosition,playing)
        }
    }


    override fun getFilter(): android.widget.Filter {
        if(filterSourceList == null){
            Log.d("getFilterll","come")
            filterSourceList = FilterSourceList(this, filterSList as ArrayList<Song>)
            Log.d("sizeFilterll",filterSList.size.toString())
        }
        Log.d("getFilterll", filterSourceList.toString())
        return filterSourceList as FilterSourceList
    }

    fun updateSongList(songs: ArrayList<Song>){
        listSong = songs
        notifyDataSetChanged()
    }

    inner class SongViewHolder(private val binding:ItemSongBinding): RecyclerView.ViewHolder(binding.root){
        fun bindData(song: Song, position:Int){
            binding.nameSong.text = song.title
            binding.nameDepeche.text = song.artist
            Glide.with(binding.imageSong.context)
                .load(Uri.parse(song.albumArtUri))
                .placeholder(R.drawable.bg_dark)
                .into(binding.imageSong)

            binding.layoutNotContainBtnPlay.setOnClickListener {
                onSongClicked.onSongClicked(song, position)
            }
            binding.imagePlay.setImageResource(if ((currentPlayingPosition == position && isPlaying)) R.drawable.icon_pause else R.drawable.icon_play)
            binding.imagePlay.setOnClickListener{ onPlayOrPauseItem.playOrPause(song,position)}
        }
         fun changeState(play:Boolean){
            binding.imagePlay.setImageResource(if(play) R.drawable.icon_pause else R.drawable.icon_play)
        }
    }
}