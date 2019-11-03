package com.samih.aurat

import android.app.Activity
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.beust.klaxon.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView

import org.osmdroid.config.Configuration
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polyline
import java.net.URL

class MainActivity : Activity() {
    private val parser: Parser = Parser()
    private val api = "https://api.turku.fi/street-maintenance/v1/vehicles/"
    private val centerOfTurku = GeoPoint(60.451628, 22.267044)
    private var map: MapView? = null
    private var activePolylines = ArrayList<Polyline>()


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ctx = applicationContext
        //important! set your user agent to prevent getting banned from the osm servers
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        setContentView(R.layout.activity_main)

       // TrailRepo.initializeOSM(12)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    public override fun onResume() {
        super.onResume()
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
    }
/*
    private fun initializeOSM(hours: Int){
        map = findViewById<View>(R.id.map) as MapView
        map!!.setTileSource(TileSourceFactory.MAPNIK)
        map!!.setScrollableAreaLimitDouble(BoundingBox(70.127855,31.748989, 59.687982, 19.236935))
        map!!.setBuiltInZoomControls(true)
        map!!.setMultiTouchControls(true)

        val mapController = map!!.controller
        mapController.setZoom(15)
        mapController.setCenter(centerOfTurku) // TODO: use location if enabled

        populateMap(hours)
    }

    private fun clearMap(){
        activePolylines.clear()

    }

    private fun populateMap(hours: Int){
        clearMap()
        fetchActivePlows(hours)
    }

    private fun fetchActivePlows(hours: Int){
        doAsync {
            val result = StringBuilder(URL(api + "?since=" + hours + "hours+ago").readText())
            uiThread {
                if (result.toString() != "[]"){
                    val json = parser.parse(result) as JsonArray<JsonObject>
                    for (plow: JsonObject in json){
                        fetchIndividualPlowTrail(hours, plow.int("id"))
                    }

                } else {
                    Toast.makeText(ctx, "No activity for the last $hours hours", Toast.LENGTH_SHORT).show()

                }
            }
        }
    }

    private fun fetchIndividualPlowTrail(hours: Int, id: Int?){
        doAsync {
            val result = StringBuilder(URL(api + id + "?since=" + hours + "hours+ago&temporal_resolution=4").readText())
            uiThread {
                if (result.toString() != "[]"){
                    val json = parser.parse(result) as JsonObject
                    addMapLine(json)
                } else {
                    Toast.makeText(ctx, "No activity for plow $id for the last $hours hours", Toast.LENGTH_SHORT).show()

                }
            }
        }
    }

    private fun addMapLine(plowData: JsonObject){
        val locationHistory: JsonArray<JsonObject> = plowData.array("location_history")!!
        var polyline = Polyline()
        var points = ArrayList<GeoPoint>()
        locationHistory.forEach { location ->
            points.add(GeoPoint(location.array<Double>("coords")?.get(1)!!, location.array<Double>("coords")?.get(0)!!))
        }
        polyline.points = points
        polyline.isGeodesic = true
        activePolylines.add(polyline)
        map?.overlays?.add(polyline)
    }
*/
}
