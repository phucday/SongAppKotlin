package com.example.songappkotlin.activity

import android.Manifest
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.ui.AppBarConfiguration
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.songappkotlin.R
import com.example.songappkotlin.adapter.SongAdapter
import com.example.songappkotlin.adapter.ViewPagerAdapter
import com.example.songappkotlin.databinding.ActivityListSongBinding
import com.example.songappkotlin.fragment.FavouriteFragment
import com.example.songappkotlin.fragment.HomeFragment
import com.example.songappkotlin.fragment.ProfileFragment
import com.example.songappkotlin.listener.OnPlayOrPauseListener
import com.example.songappkotlin.listener.OnSongClicked
import com.example.songappkotlin.manager.SongRepository
import com.example.songappkotlin.model.Song
import com.example.songappkotlin.service.SongService
import com.example.songappkotlin.utils.Constants
import com.example.songappkotlin.utils.PermissionUtil
import com.google.android.material.bottomnavigation.BottomNavigationView

class ListSongActivity : AppCompatActivity() {
    private lateinit var binding: ActivityListSongBinding
    private val homeFragment = HomeFragment()
    private val favouriteFragment = FavouriteFragment()
    private val profileFragment = ProfileFragment()

    companion object{
        private const val REQUEST_CODE = 1
        private const val REQUEST_CODE_READ_EXTERNAL_STORAGE = 1
        private const val REQUEST_CODE_MANAGE_EXTERNAL_STORAGE = 2
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
//                loadSongs()
            } else {
                Toast.makeText(this@ListSongActivity, "Please give us permission", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityListSongBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

//        getPermissionAllFile()
//          getPermissionFileAudio()

        setupViewPager()
        setupBottomNavigation()
    }

    private fun setupViewPager() {
        val listFragment = listOf(homeFragment, favouriteFragment, profileFragment)
        val viewPagerAdapter by lazy { ViewPagerAdapter(this, listFragment) }
        binding.viewPagerMain.adapter = viewPagerAdapter

        binding.viewPagerMain.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> binding.navView.selectedItemId = R.id.itHome
                    1 -> binding.navView.selectedItemId = R.id.itFavorite
                    2 -> binding.navView.selectedItemId = R.id.itProflie
                }
            }
        })
    }

    private fun setupBottomNavigation(){
        binding.navView.setOnNavigationItemSelectedListener (object :
            BottomNavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                when(item.itemId){
                    R.id.itHome -> {
                        binding.viewPagerMain.currentItem = 0
                        return true
                    }
                    R.id.itFavorite -> {
                        binding.viewPagerMain.currentItem = 1
                        return true
                    }
                    R.id.itProflie -> {
                        binding.viewPagerMain.currentItem = 2
                        return true
                    }
                    else -> {
                        return false
                    }
                }
                return false
            }

        })
    }


    private fun getPermissionFileAudio(){
        if(PermissionUtil.checkReadAudioPermission(this)){
//            loadSongs()
        }else requestPermissionFileAudio()
    }

    private fun getPermissionAllFile(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11 (API 30) và cao hơn
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.setData(Uri.parse("package:$packageName"))
                startActivityForResult(
                    intent,
                    REQUEST_CODE_MANAGE_EXTERNAL_STORAGE
                )
            } else {
//                loadSongs()
            }
        }
        else {
            // Android 10 (API 29) và thấp hơn
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_CODE_READ_EXTERNAL_STORAGE
                )
            } else {
//                loadSongs()
            }
        }
    }

    private fun requestPermissionFileAudio(){
        if(PermissionUtil.shouldShowRequestPermissionRationaleForReadAudio(this)){
            // set intent to setting
        }else{
            if(PermissionUtil.checkReadAudioPermission(this)){
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }else{
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
            }
        }
    }



}