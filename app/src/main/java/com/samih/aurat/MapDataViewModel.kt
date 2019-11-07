package com.samih.aurat

import android.app.Application
import android.graphics.Color
import android.graphics.Paint
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.samih.aurat.PlansRepo.JobPlan
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.util.GeoPoint

/*
Acts in between the view (MapFragment) and the model (TrailRepo, PlansRepo).
Provides observable LiveData objects to the view by transforming the LiveData of the repos.
 */
class MapDataViewModel(application: Application) : AndroidViewModel(application) {
    private val trailRepo = TrailRepo(application.applicationContext)
    private val plansRepo = PlansRepo()
    private val eventColors: Map<String, String> = mapOf("au" to "#005BFF",
            "su" to "#1f78b4",
            "hi" to "#b2df8a",
            "nt" to "#33a02c",
            "ln" to "#fb9a99",
            "hs" to "#e31a1c",
            "pe" to "#fdbf6f",
            "ps" to "#ff7f00",
            "hn" to "#cab2d6",
            "hj" to "#B32EB3",
            "pn" to "#ffff99",
            "kv" to "#b15928",
            "ha" to "#0d4b75")
    private val eventNames: Map<String, Int> = mapOf(
            "kv" to R.string.kv,
            "au" to R.string.au,
            "su" to R.string.su,
            "hi" to R.string.hi,
            "nt" to R.string.nt,
            "ln" to R.string.ln,
            "hs" to R.string.hs,
            "pe" to R.string.pe,
            "ps" to R.string.ps,
            "hn" to R.string.hn,
            "hj" to R.string.hj,
            "pn" to R.string.pn,
            "ha" to R.string.ha
    )
    /*
    LiveData and getter for the street cleaning plans data provided by PlansRepo.
    The plan data is provided to the view as-is
     */
    private val viewModelPlanData: LiveData<ArrayList<JobPlan>> = Transformations.map(plansRepo.getPlanList()){ data -> data}
    fun getPlanData(): LiveData<ArrayList<JobPlan>>{
        return viewModelPlanData
    }
    fun plansRepo(): PlansRepo{
        return plansRepo
    }

    /*
    LiveData, getter and the transformation function for the vehicle trail data provided by TrailRepo
     */
    private val viewModelPolylines: LiveData<ArrayList<Polyline>>  = Transformations.map(trailRepo.getPolylines(), ::generatePolylines)
    private fun generatePolylines(data: ArrayList<Pair<ArrayList<GeoPoint>, Array<String>>>): ArrayList<Polyline> {
        val polylineList = ArrayList<Polyline>()
        for (trail in data){
            val polyline = Polyline()
            polyline.setPoints(trail.first)
            polyline.color = Color.parseColor(eventColors[trail.second[0]])
            if (trail.second[0] in eventNames.keys){
                polyline.title = getApplication<Application>().getString(eventNames[trail.second[0]] ?: error(""))
            }
            polyline.subDescription = trail.second[1]
            polyline.paint.strokeJoin = Paint.Join.ROUND
            polyline.paint.strokeCap = Paint.Cap.ROUND
            polyline.width = 15.0f
            polylineList.add(polyline)
        }
        return polylineList
    }
    fun getPolylines(): LiveData<ArrayList<Polyline>>  {
        return viewModelPolylines
    }
    fun trailRepo(): TrailRepo {
        return trailRepo
    }

    /*
    LiveData, getter and transformation function for the download counter.
    When the counter reaches 0, the view knows to hide the progress spinner.
     */
    private val repoIsLoading: LiveData<Boolean> = Transformations.map(trailRepo.getPlowsToLoad(), ::getLoadingState)
    private fun getLoadingState(remainingDownloads: Int): Boolean {
        return (remainingDownloads > 0)
    }
    fun getRepoLoadingState(): LiveData<Boolean>{
        return repoIsLoading
    }

}
