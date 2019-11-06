package com.samih.aurat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import org.osmdroid.config.Configuration

class MainActivity : AppCompatActivity() {

    lateinit var mFragmentManager: FragmentManager
    lateinit var mapFragment: MapFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mFragmentManager = supportFragmentManager
        mapFragment = MapFragment()

        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        supportFragmentManager.beginTransaction().add(R.id.fragment_container, mapFragment).commit()

    }

}
