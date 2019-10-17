package com.samih.aurat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.osmdroid.views.overlay.Polyline

/**
 * Created by sami on 14.10.2019.
 */
class MapDataViewModel : ViewModel() {
    private val polylinesList = MutableLiveData<ArrayList<Polyline>>()
    fun setPolylines (polylines: ArrayList<Polyline>) {
        polylinesList.value = polylines
    }
    fun getPolylines(): LiveData<ArrayList<Polyline>>{
        return polylinesList
    }
    //TODO: tee MapFragmenttiin observer getPolylines():lle, ja MapManageriin setteri (tai LiveData
    //TODO: sinnekin?)
}