package com.samih.aurat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import android.widget.ProgressBar
import com.google.android.material.snackbar.Snackbar
import com.samih.aurat.PlansRepo.JobPlan
import kotlinx.android.synthetic.main.fragment_map.*
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow
import android.text.method.LinkMovementMethod
import android.text.Html
import android.text.Html.FROM_HTML_MODE_COMPACT
import android.widget.TextView
import org.osmdroid.tileprovider.tilesource.ThunderforestTileSource
import org.osmdroid.tileprovider.tilesource.TileSourcePolicy
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.views.CustomZoomButtonsController


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [BlankFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [BlankFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class MapFragment : Fragment(), MapEventsReceiver {

    // TODO: Rename and change types of parameters
    /*
    private var mParam1: String? = null
    private var mParam2: String? = null
    private var mListener: OnFragmentInteractionListener? = null
    */
    private var map: MapView? = null
    private lateinit var mapDataViewModel: MapDataViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapDataViewModel = ViewModelProviders.of(this).get(MapDataViewModel::class.java)
        mapDataViewModel.plansRepo().downloadPlans()
        mapDataViewModel.trailRepo().initializeOSM(60)

        /*
        if (arguments != null) {
            mParam1 = arguments.getString(ARG_PARAM1)
            mParam2 = arguments.getString(ARG_PARAM2)
        }
        */

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_map, container, false)
        val spinner = rootView.findViewById<ProgressBar>(R.id.progressBar)
        map = rootView.findViewById(R.id.map)

        // Create a custom tile source
        val tileSource = ThunderforestTileSource(context, ThunderforestTileSource.MOBILE_ATLAS)
        map!!.setTileSource(tileSource)

        val text = "© <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a> contributors"
        val textView = rootView.findViewById<TextView>(R.id.creditsTextView)
        textView.text = Html.fromHtml(text, FROM_HTML_MODE_COMPACT)
        textView.movementMethod = LinkMovementMethod.getInstance()

        val mapEventsOverlay = MapEventsOverlay(rootView.context, this)
        map!!.overlays.add(0, mapEventsOverlay)
        mapDataViewModel.getPolylines().observe(this, Observer<ArrayList<Polyline>> { t ->
            //map!!.overlayManager.clear()

            for (polyline in t){
                if (polyline.points.isNullOrEmpty()){
                    map!!.overlayManager.clear()
                    mapDataViewModel.trailRepo().initializeOSM(60)
                    return@Observer
                }
                polyline.setInfoWindow(BasicInfoWindow(org.osmdroid.bonuspack.R.layout.bonuspack_bubble, map))
                polyline.setOnClickListener { polyline, mapView, eventPos ->
                    InfoWindow.closeAllInfoWindowsOn(mapView)
                    if (!mapView.boundingBox.contains(polyline.infoWindowLocation)) {
                        polyline.infoWindowLocation = eventPos
                    }
                    polyline.showInfoWindow()
                    mapView.controller.animateTo(polyline.infoWindowLocation)

                    return@setOnClickListener true
                }

            }
            map!!.overlayManager.addAll(t!!)
            map!!.invalidate() })
        mapDataViewModel.getRepoLoadingState().observe(this, Observer<Boolean> { loading ->
            if (loading){
                spinner.visibility = View.VISIBLE
            }
            else {
                spinner.visibility = View.GONE
                if (mapDataViewModel.getPolylines().value.isNullOrEmpty()){
                    Snackbar.make(mapContainer, "No recent activity", Snackbar.LENGTH_LONG).show()
                    // No polyline
                }
            }
        })
        mapDataViewModel.getPlanData().observe(this, Observer<ArrayList<JobPlan>> { t ->
            for (data in t){
                val marker = Marker(map)
                marker.position = GeoPoint(data.latitude, data.longitude)
                marker.title = data.street + " " + data.description
                marker.subDescription = data.date
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                marker.setOnMarkerClickListener { marker, mapView ->
                    InfoWindow.closeAllInfoWindowsOn(mapView)
                    marker.showInfoWindow()
                    mapView.controller.animateTo(marker.position)
                    return@setOnMarkerClickListener true
                }
                //TODO: vaihda siistimpi ikoni
                map!!.overlays.add(marker)
                map!!.invalidate()
            }
        })
        map!!.setTileSource(TileSourceFactory.MAPNIK)
        map!!.setScrollableAreaLimitDouble(BoundingBox(60.6463, 22.5913, 60.3286, 21.8195))
        map!!.isHorizontalMapRepetitionEnabled = false
        map!!.isVerticalMapRepetitionEnabled = false
        map!!.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        map!!.setMultiTouchControls(true)

        val mapController = map!!.controller
        mapController.setZoom(15.0)
        mapController.setCenter(mapDataViewModel.trailRepo().centerOfTurku) // TODO: use location if enabled
        //rootView.findViewById<FloatingActionButton>(R.id.floatingActionButton).setOnClickListener { toggleLayersMenu() }
        return rootView

    }

    override fun longPressHelper(p: GeoPoint?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
        InfoWindow.closeAllInfoWindowsOn(map)
        return true
    }
/*
    private fun toggleLayersMenu() {
        if (layersCard.visibility == View.VISIBLE) {
            layersCard.visibility = View.INVISIBLE
        } else {
            layersCard.visibility = View.VISIBLE
        }
    }

 */
}