package com.samih.aurat

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import com.samih.aurat.dummy.DummyContent
import kotlinx.android.synthetic.main.activity_main3.*


class Main3Activity : FragmentActivity(), FavouritesFragment.OnListFragmentInteractionListener {

    lateinit var mFragmentManager: FragmentManager
    lateinit var mapFragment: MapFragment
    lateinit var favouritesFragment: FavouritesFragment
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
        supportFragmentManager.inTransaction{replace(frameId, fragment)}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)
        mFragmentManager = supportFragmentManager
        mapFragment = MapFragment()
        favouritesFragment = FavouritesFragment()

        addFragment(MapFragment(), R.id.fragment_container)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

    }

    override fun onListFragmentInteraction(item: DummyContent.DummyItem?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
