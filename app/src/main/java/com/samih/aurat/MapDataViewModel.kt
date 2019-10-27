package com.samih.aurat

import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.osmdroid.views.overlay.Polyline
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import org.osmdroid.util.GeoPoint


/**
 * Created by sami on 14.10.2019.
 */
class MapDataViewModel : ViewModel() {
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
            "kv" to "#b15928",
            "ha" to "#0d4b75")
    private val eventNames: Map<String, String> = mapOf(
            "kv" to "Bicycle and pedestrian lanes",
            "au" to "Snow removal",
            "su" to "De-icing with salt",
            "hi" to "Spreading sand",
            "nt" to "Mowing",
            "ln" to "Drag leveling",
            "hs" to "Planing",
            "pe" to "Street washing",
            "ps" to "Dust suppression",
            "hn" to "Sand removal",
            "hj" to "Brushing",
            "pn" to "Coating",
            "ha" to "Brushing and de-icing with salt"
    )

    private val viewModelPolylines: LiveData<ArrayList<Polyline>>  = Transformations.map(MapManager.getPolylines(), ::generatePolylines)
    private fun generatePolylines(data: ArrayList<Pair<ArrayList<GeoPoint>, Array<String>>>): ArrayList<Polyline> {
        val polylineList = ArrayList<Polyline>()
        for (trail in data){
            val polyline = Polyline()
            polyline.setPoints(trail.first)
            polyline.color = Color.parseColor(eventColors[trail.second[0]])
            polyline.title = eventNames[trail.second[0]]
            polyline.subDescription = trail.second[1]
            //TODO: lisää polylinen titleen/descriptioniin event type ja/tai aikaleima (Pairiin string[] esim)
            polylineList.add(polyline)
        }
        return polylineList
    }

    fun getPolylines(): LiveData<ArrayList<Polyline>>  {
        return viewModelPolylines
    }
}
