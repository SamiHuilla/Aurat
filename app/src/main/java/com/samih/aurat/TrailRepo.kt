package com.samih.aurat


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.beust.klaxon.*
import com.samih.aurat.BuildConfig.BASE_URL
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.doAsyncResult
import org.jetbrains.anko.uiThread
import org.osmdroid.util.GeoPoint
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.floor

/**
 * Created by sami on 19.6.2018.
 */
class TrailRepo {
    private val parser: Parser = Parser()
    val centerOfTurku = GeoPoint(60.451628, 22.267044)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
    private val weekdayFormat = SimpleDateFormat("EEEE", Locale.ENGLISH)

    private val plowsToLoad = MutableLiveData(0)
    private val polylinesList = MutableLiveData<ArrayList<Pair<ArrayList<GeoPoint>, Array<String>>>>()

    fun getPlowsToLoad(): LiveData<Int> {
        return plowsToLoad
    }
    private fun reducePlowsToLoad() {
        if (plowsToLoad.value!! > 0){
            plowsToLoad.value = plowsToLoad.value?.minus(1)
        }
    }
    fun setPolylines (polylines: ArrayList<Pair<ArrayList<GeoPoint>, Array<String>>>) {
        polylinesList.value = polylines
    }
    fun getPolylines(): LiveData<ArrayList<Pair<ArrayList<GeoPoint>, Array<String>>>> {
        return polylinesList
    }

    fun initializeOSM(hours: Int){
        fetchActivePlows(hours)
        plowsToLoad.value = 10 // set an initial value, change later
    }


    private fun parseActivePlows(activePlows: StringBuilder, hours: Int){
        if (activePlows.toString() != "[]"){
            val json = parser.parse(activePlows) as JsonArray<JsonObject>
            plowsToLoad.value = json.size
            for (plow: JsonObject in json){
                fetchIndividualPlowTrail(hours, plow.int("id"))
            }

        } else {
            //Toast.makeText(ctx, "No activity for the last $hours hours", Toast.LENGTH_SHORT).show()
            plowsToLoad.value = 0
        }

    }

    private fun parseIndividualPlowData(plowDataStr: StringBuilder){
        if (plowDataStr.toString() != "[]"){
            val json = parser.parse(plowDataStr) as JsonObject
            addMapLine(json)
        } else {
            //Toast.makeText(ctx, "No activity for plow $id for the last $hours hours", Toast.LENGTH_SHORT).show()
            //TODO: ilmoita jotenkin
        }


    }

    private fun addMapLine(plowData: JsonObject){
        //val activePolylines = ArrayList<Polyline>()
        val polylineData = ArrayList<Pair<ArrayList<GeoPoint>, Array<String>>>()
        val locationHistory: JsonArray<JsonObject> = plowData.array("location_history")!!
        //var polyline = Polyline()
        //polyline.isGeodesic = true
        var points = ArrayList<GeoPoint>()
        var currentType = locationHistory[0].array<String>("events")?.get(0)!!  // get the first event type
        var currentTime: String = locationHistory[0]["timestamp"].toString()
        var currentCoordinates = GeoPoint(locationHistory[0].array<Double>("coords")?.get(1)!!, locationHistory[0].array<Double>("coords")?.get(0)!!)

        for (location in locationHistory){
            val eventType = location.array<String>("events")?.get(0)!!
            val coordinates = GeoPoint(location.array<Double>("coords")?.get(1)!!, location.array<Double>("coords")?.get(0)!!)
            val time: String = location["timestamp"].toString()
            val timeDifference = timeDifference(currentTime, time)
            val distance = coordinates.distanceToAsDouble(currentCoordinates)
            // The vehicle is staying still
            if (eventType == currentType && distance < 5){
                points.add(coordinates)
            }
            // The vehicle is on a job
            if (eventType == currentType && timeDifference < 60000 && distance < 1500){
                points.add(coordinates)
                // if type == points.add ELSE currentType -> pointsit polylineen ja polyline aktiivisiin --> molempien tyhjennys
            }
            // The vehicle has started another job and/or moved to another location
            else {
                //polyline.setPoints(points)
                //polyline.color = Color.parseColor(eventColors[currentType])
                //activePolylines.add(polyline)
                polylineData.add(Pair(points, arrayOf(currentType, getEventAge(time))))
                //map?.overlayManager?.add(polyline)
                //map?.invalidate()
                //polyline = Polyline()
                points = ArrayList()
                points.add(coordinates)
                currentType = eventType
            }
            currentTime = time
            currentCoordinates = coordinates
        }

        polylineData.add(Pair(points, arrayOf(currentType, getEventAge(currentTime))))
        polylinesList.setValue(polylineData)
        reducePlowsToLoad()
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

    private fun timeDifference(now: Date, eventTime: String): Long {
        val diff: Long
        try {
            val eventDate = dateFormat.parse(eventTime)
            diff = now.time - eventDate.time
        } catch (e: ParseException) {
            e.printStackTrace()
            return 0
        }
        return diff
    }

    /**
     * Calculate time since the event
     */
    private fun getEventAge(eventTime: String): String {
        val now = Date()
        val differenceInSeconds = timeDifference(now, eventTime).toInt()/1000

        return when {
            differenceInSeconds < 3600 -> {
                val minutes = floor(differenceInSeconds/60.0).toInt()
                if (minutes > 1) String.format("%d minutes ago", minutes) else "1 minute ago"
            }
            differenceInSeconds < 24*3600 -> {
                val hours = floor(differenceInSeconds / 3600.0).toInt()
                if (hours > 1) String.format("%d hours ago", hours) else "1 hour ago"
            }
            else -> weekdayFormat.format(dateFormat.parse(eventTime))
        }

    }


    private fun fetchActivePlows(hours: Int){
        doAsyncResult {
            val result = StringBuilder(URL(BASE_URL + "?since=" + hours + "hours+ago&limit=50").readText())
            uiThread {
                parseActivePlows(result, hours)
            }
        }

    }

    private fun fetchIndividualPlowTrail(hours: Int, id: Int?){
        doAsync {
            val result = StringBuilder(URL(BASE_URL + id + "?since=" + hours + "hours+ago&temporal_resolution=1").readText())
            uiThread {
                parseIndividualPlowData(result)
            }
        }
    }


}