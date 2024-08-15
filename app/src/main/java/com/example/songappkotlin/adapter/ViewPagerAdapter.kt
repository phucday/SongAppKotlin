package com.example.songappkotlin.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(
    private val fragmentActivity: FragmentActivity,
    private val listFragment:List<Fragment>
): FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return listFragment.size
    }
    override fun createFragment(position: Int): Fragment {
        return listFragment[position]
    }
}