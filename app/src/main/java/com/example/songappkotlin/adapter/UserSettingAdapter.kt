package com.example.songappkotlin.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.songappkotlin.databinding.LayoutUserSettingItemBinding
import com.example.songappkotlin.listener.OnClickItemUserSetting
import com.example.songappkotlin.model.UserSetting

class UserSettingAdapter(
    private val listUserSetting: List<UserSetting>,
    private val onClickItemUserSetting: OnClickItemUserSetting
): RecyclerView.Adapter<UserSettingAdapter.UserSettingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserSettingViewHolder {
        val layoutUserSettingItemBinding = LayoutUserSettingItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return UserSettingViewHolder(layoutUserSettingItemBinding)
    }

    override fun getItemCount(): Int {
        return listUserSetting.size
    }

    override fun onBindViewHolder(holder: UserSettingViewHolder, position: Int) {
        holder.bindData(listUserSetting[position],position)
    }

    inner class UserSettingViewHolder(private val binding: LayoutUserSettingItemBinding):RecyclerView.ViewHolder(binding.root){
        fun bindData(userSetting: UserSetting, po:Int){
            binding.avatarItem.setImageResource(userSetting.symbol)
            binding.textItem.text = userSetting.nameSetting
            binding.iconItem.setImageResource(userSetting.iconForward)
            binding.layoutItemSetting.setOnClickListener { onClickItemUserSetting.onClickedItemSetting(userSetting, po) }
        }
    }
}