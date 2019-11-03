package com.samih.aurat

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.samih.aurat.dummy.DummyContent
import kotlinx.android.synthetic.main.activity_main3.*
import org.osmdroid.config.Configuration

import com.mikepenz.aboutlibraries.LibsBuilder
import com.mikepenz.aboutlibraries.ui.LibsFragment
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.R.attr.fragment
import com.mikepenz.aboutlibraries.ui.LibsSupportFragment


class Main3Activity : AppCompatActivity(), FavouritesFragment.OnListFragmentInteractionListener {

    lateinit var mFragmentManager: FragmentManager
    lateinit var mapFragment: MapFragment
    lateinit var favouritesFragment: FavouritesFragment
    lateinit var aboutFragment: LibsSupportFragment
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_map -> {
                replaceFragment(mapFragment, R.id.fragment_container)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_favourites -> {
                replaceFragment(favouritesFragment, R.id.fragment_container)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_weather -> {
               // message.setText(R.string.title_notifications)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> FragmentTransaction) {
        beginTransaction().func().commit()
    }

    fun FragmentActivity.addFragment(fragment: Fragment, frameId: Int){
        supportFragmentManager.inTransaction { add(frameId, fragment) }
    }

    fun FragmentActivity.replaceFragment(fragment: Fragment, frameId: Int) {
        supportFragmentManager.inTransaction{replace(frameId, fragment).addToBackStack(null)}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)
        mFragmentManager = supportFragmentManager
        mapFragment = MapFragment()
        favouritesFragment = FavouritesFragment()
        aboutFragment = LibsBuilder()
                //Pass the fields of your application to the lib so it can find all external lib information
                .withFields(R.string::class.java.fields)
                //get the fragment
                .supportFragment()
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        addFragment(MapFragment(), R.id.fragment_container)
        //navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        replaceFragment(aboutFragment, R.id.fragment_container)
        return super.onOptionsItemSelected(item)
    }

    override fun onListFragmentInteraction(item: DummyContent.DummyItem?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
