package com.samih.aurat

import android.graphics.Color
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
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by sami on 19.6.2018.
 */
object MapManager {
    private val parser: Parser = Parser()
        private val api = "https://api.turku.fi/street-maintenance/v1/vehicles/"
    private val centerOfTurku = GeoPoint(60.451628, 22.267044)
    private var map: MapView? = null
    private var activePolylines = ArrayList<Polyline>()
    private val eventColors: Map<String, String> = mapOf("au" to "#a6cee3",
    "su" to "#1f78b4",
    "hi" to "#b2df8a",
    "nt" to "#33a02c",
    "ln" to "#fb9a99",
    "hs" to "#e31a1c",
    "pe" to "#fdbf6f",
    "ps" to "#ff7f00",
    "hn" to "#cab2d6",
    "hj" to "#6a3d9a",
    "pn" to "#ffff99",
    "kv" to "#b15928")
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
    fun initializeOSM(hours: Int, target: MapView){
        map = target
        map!!.setTileSource(TileSourceFactory.MAPNIK)
        map!!.setScrollableAreaLimitDouble(BoundingBox(70.127855,31.748989, 59.687982, 19.236935))
        //map!!.setBuiltInZoomControls(true)
        map!!.setMultiTouchControls(true)

        val mapController = map!!.controller
        mapController.setZoom(15.0)
        mapController.setCenter(centerOfTurku) // TODO: use location if enabled

        populateMap(hours)
        target.invalidate()
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
            val result = StringBuilder(URL(api + id + "?since=" + hours + "hours+ago&temporal_resolution=1").readText())
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
        polyline.isGeodesic = true
        var points = ArrayList<GeoPoint>()
        var currentType = locationHistory[0].array<String>("events")?.get(0)!!  // get the first event type
        var currentTime: String = locationHistory[0]["timestamp"].toString()
        //TODO: parsi aika jsonista


        locationHistory.forEach { location ->
            val eventType = location.array<String>("events")?.get(0)!!
            val time: String = location["timestamp"].toString()
            val timeDifference = timeDifference(currentTime, time)
            if (eventType == currentType && timeDifference < 120000){
                points.add(GeoPoint(location.array<Double>("coords")?.get(1)!!, location.array<Double>("coords")?.get(0)!!))
                // if type == points.add ELSE currentType -> pointsit polylineen ja polyline aktiivisiin --> molempien tyhjennys
            }
            else {
                polyline.setPoints(points)
                polyline.color = Color.parseColor(eventColors[currentType])
                activePolylines.add(polyline)
                map?.overlayManager?.add(polyline) // TODO: activePolylines:n tarkotus? filtteröinti?
                map?.invalidate()
                polyline = Polyline()
                points.clear()
                points.add(GeoPoint(location.array<Double>("coords")?.get(1)!!, location.array<Double>("coords")?.get(0)!!))
                currentType = eventType
            }
            currentTime = time
            // TODO: tarkista tyyppi ja aikaleima, pätki osiin jos tyyppi vaihtuu tai paikka muuttuu nopeasti
        }
        polyline.setPoints(points)
        polyline.color = Color.parseColor(eventColors[currentType])
        activePolylines.add(polyline)
        map?.overlayManager?.add(polyline)
        map?.invalidate()
        polyline = Polyline()

    }

    private fun timeDifference(current: String, next: String): Long {
        var diff: Long = 0
        try {
            val currentDate = dateFormat.parse(current)
            val nextDate = dateFormat.parse(next)
            diff = nextDate.time - currentDate.time
        } catch (e: ParseException) {
            e.printStackTrace()
            return 0
        }
        return diff
    }

}
