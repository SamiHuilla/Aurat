package com.samih.aurat

import android.view.View
import android.widget.Toast
import com.beust.klaxon.*
import org.jetbrains.anko.toast
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import java.net.URL

/**
 * Created by sami on 19.6.2018.
 */
object MapManager {
    private val parser: Parser = Parser()
    private val api = "https://api.turku.fi/street-maintenance/v1/vehicles/"
    private val centerOfTurku = GeoPoint(60.451628, 22.267044)
    private var map: MapView? = null
    private var activePolylines = ArrayList<Polyline>()

    fun initializeOSM(hours: Int, target: MapView){
        map = target
        map!!.setTileSource(TileSourceFactory.MAPNIK)
        map!!.setScrollableAreaLimitDouble(BoundingBox(70.127855,31.748989, 59.687982, 19.236935))
        //map!!.setBuiltInZoomControls(true)
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
        getActivePlows(hours)
    }

    private fun getActivePlows(hours: Int){
        doAsync {
            val result = StringBuilder(URL(api + "?since=" + hours + "hours+ago").readText())
            uiThread {
                if (result.toString() != "[]"){
                    val json = parser.parse(result) as JsonArray<JsonObject>
                    for (plow: JsonObject in json){
                        createIndividualPlowTrail(hours, plow.int("id"))
                    }

                } else {
                    //Toast.makeText(ctx, "No activity for the last $hours hours", Toast.LENGTH_SHORT).show()

                }
            }
        }
    }

    private fun createIndividualPlowTrail(hours: Int, id: Int?){
        doAsync {
            val result = StringBuilder(URL(api + id + "?since=" + hours + "hours+ago&temporal_resolution=4").readText())
            uiThread {
                if (result.toString() != "[]"){
                    val json = parser.parse(result) as JsonObject
                    addMapLine(json)
                } else {
                    //Toast.makeText(ctx, "No activity for plow $id for the last $hours hours", Toast.LENGTH_SHORT).show()

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

}
