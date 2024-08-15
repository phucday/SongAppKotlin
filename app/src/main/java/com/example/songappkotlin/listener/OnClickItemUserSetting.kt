package com.example.songappkotlin.listener

import com.example.songappkotlin.model.UserSetting

interface OnClickItemUserSetting {
    fun onClickedItemSetting(userSetting: UserSetting, po: Int)
}