package com.example.songappkotlin.utils

import android.util.Log
import android.widget.Filter
import com.example.songappkotlin.adapter.SongAdapter
import com.example.songappkotlin.model.Song

class FilterSourceList(
    private val adapter: SongAdapter,
    private val songListSearch: ArrayList<Song>
): Filter() {

    override fun performFiltering(charSequence: CharSequence?): FilterResults {
        var result = FilterResults()
        var tempCharSequence: CharSequence?
        if(!charSequence.isNullOrEmpty()){
            tempCharSequence = charSequence.toString().uppercase()
            var filterList = ArrayList<Song>()
            for(song in songListSearch){
                if(song.title.uppercase().contains(tempCharSequence)){
                    filterList.add(song)
                    Log.d("sizeFileterList",filterList.size.toString())
                }
            }
            result.count = filterList.size
            result.values = filterList
        }else{
            result.count = songListSearch.size
            result.values = songListSearch
        }
        Log.d("checkResultt",result.values.toString())
        return result
    }

    override fun publishResults(charSequence: CharSequence?, result: FilterResults?) {
//        adapter.updateSongList(result?.values as ArrayList<Song>)
        Log.d("publishResultsHere","${result?.values.toString()}")
        adapter.listSong = result?.values as ArrayList<Song>
        adapter.notifyDataSetChanged()
    }
}