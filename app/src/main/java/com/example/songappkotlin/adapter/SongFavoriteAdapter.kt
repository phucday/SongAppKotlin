package com.example.songappkotlin.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.songappkotlin.R
import com.example.songappkotlin.databinding.ItemSongBinding
import com.example.songappkotlin.databinding.ItemSongFavoriteBinding
import com.example.songappkotlin.listener.OnDeleteFavoriteSong
import com.example.songappkotlin.listener.OnPlayOrPauseListener
import com.example.songappkotlin.listener.OnSongClicked
import com.example.songappkotlin.model.Song

class SongFavoriteAdapter(
    private var listSong: List<Song>,
    private val onSongClicked: OnSongClicked,
    private val onDeleteFavoriteSong: OnDeleteFavoriteSong
    ): RecyclerView.Adapter<SongFavoriteAdapter.SongFavouriteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongFavouriteViewHolder {
        val itemSongFavoriteBinding = ItemSongFavoriteBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return SongFavouriteViewHolder(itemSongFavoriteBinding)
    }

    override fun getItemCount(): Int {
        return listSong.size
    }

    override fun onBindViewHolder(holder: SongFavouriteViewHolder, position: Int) {
        holder.bindData(listSong[position],position)
    }

    fun updateSongs(songs: List<Song>){
        listSong = songs
        notifyDataSetChanged()
    }

    inner class SongFavouriteViewHolder(private val binding:ItemSongFavoriteBinding): RecyclerView.ViewHolder(binding.root){
        fun bindData(song: Song, position:Int){
            binding.nameSong.text = song.title
            binding.nameDepeche.text = song.artist
            Glide.with(binding.imageSong.context)
                .load(Uri.parse(song.albumArtUri))
                .placeholder(R.drawable.bg_dark)
                .into(binding.imageSong)

            binding.root.setOnClickListener {
                onSongClicked.onSongClicked(song, position)
            }
            binding.btnDeleteSong.setOnClickListener{
                onDeleteFavoriteSong.onClickDeleteSong(song, position)
            }
        }
    }
}