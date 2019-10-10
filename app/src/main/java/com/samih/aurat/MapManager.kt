package com.samih.aurat

import android.graphics.Color
import com.beust.klaxon.*
import com.samih.aurat.BuildConfig.BASE_URL
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polyline
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by sami on 19.6.2018.
 */
object MapManager {
    private val parser: Parser = Parser()
    val centerOfTurku = GeoPoint(60.451628, 22.267044)
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


    // TODO: kaikki mappiviewiin liittyvä fragmenttiin
    fun initializeOSM(hours: Int){
        PlowApiWrapper.fetchActivePlows(hours)
    }


    fun parseActivePlows(activePlows: StringBuilder, hours: Int){
        if (activePlows.toString() != "[]"){
            val json = parser.parse(activePlows) as JsonArray<JsonObject>
            for (plow: JsonObject in json){
                PlowApiWrapper.fetchIndividualPlowTrail(hours, plow.int("id"))
            }

        } else {
            //Toast.makeText(ctx, "No activity for the last $hours hours", Toast.LENGTH_SHORT).show()

        }

    }

    fun parseIndividualPlowData(plowDataStr: StringBuilder){
        if (plowDataStr.toString() != "[]"){
            val json = parser.parse(plowDataStr) as JsonObject
            addMapLine(json)
        } else {
            //Toast.makeText(ctx, "No activity for plow $id for the last $hours hours", Toast.LENGTH_SHORT).show()

        }


    }

    private fun addMapLine(plowData: JsonObject){
        val activePolylines = ArrayList<Polyline>()
        val locationHistory: JsonArray<JsonObject> = plowData.array("location_history")!!
        var polyline = Polyline()
        polyline.isGeodesic = true
        var points = ArrayList<GeoPoint>()
        var currentType = locationHistory[0].array<String>("events")?.get(0)!!  // get the first event type
        var currentTime: String = locationHistory[0]["timestamp"].toString()

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
                //map?.overlayManager?.add(polyline)
                //map?.invalidate()
                polyline = Polyline()
                points.clear()
                points.add(GeoPoint(location.array<Double>("coords")?.get(1)!!, location.array<Double>("coords")?.get(0)!!))
                currentType = eventType
            }
            currentTime = time

        }
        polyline.setPoints(points)
        polyline.color = Color.parseColor(eventColors[currentType])
        activePolylines.add(polyline)
        // TODO: activePolylines ViewModelin(?) LiveDataan
        //map?.overlayManager?.add(polyline)
        //map?.invalidate()
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
