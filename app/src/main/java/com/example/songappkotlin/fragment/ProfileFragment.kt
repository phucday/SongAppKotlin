package com.example.songappkotlin.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.songappkotlin.R
import com.example.songappkotlin.adapter.UserSettingAdapter
import com.example.songappkotlin.databinding.FragmentProfileBinding
import com.example.songappkotlin.listener.OnClickItemUserSetting
import com.example.songappkotlin.model.UserSetting

class ProfileFragment : Fragment(), OnClickItemUserSetting {
    private val binding by lazy { FragmentProfileBinding.inflate(layoutInflater)}
    private val userSettingAdapter by lazy { UserSettingAdapter(listSetting,this) }
    private val listSetting = ArrayList<UserSetting>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun init(){
        binding.rcvItemUserSetting.adapter = userSettingAdapter

        val setting1 = UserSetting(R.drawable.icon_person,"Personal Information", R.drawable.icon_forward_right)
        val setting2 = UserSetting(R.drawable.icon_share,"Sharing", R.drawable.icon_forward_right)
        val setting3 = UserSetting(R.drawable.icon_like_filled,"Favorite", R.drawable.icon_forward_right)
        val setting4 = UserSetting(R.drawable.icon_setting,"Setting", R.drawable.icon_forward_right)
        val setting5 = UserSetting(R.drawable.icon_notice,"Others", R.drawable.icon_forward_right)
        listSetting.add(setting1)
        listSetting.add(setting2)
        listSetting.add(setting3)
        listSetting.add(setting4)
        listSetting.add(setting5)
    }

    override fun onClickedItemSetting(userSetting: UserSetting, po: Int) {
        Toast.makeText(requireActivity(),"Updating", Toast.LENGTH_SHORT).show()
    }

}