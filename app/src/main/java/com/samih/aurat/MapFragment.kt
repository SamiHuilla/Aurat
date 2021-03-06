package com.samih.aurat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
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
import org.osmdroid.views.CustomZoomButtonsController


/*
Fragment that contains an OSMDroid MapView
 */
class MapFragment : Fragment(), MapEventsReceiver {

    private var map: MapView? = null
    private lateinit var mapDataViewModel: MapDataViewModel

    /*
    Assign the ViewModel and initiate downloading of cleaning plans and vehicle trails.
    Vehicle location history is downloaded from the past 2,5 days
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapDataViewModel = ViewModelProviders.of(this).get(MapDataViewModel::class.java)
        mapDataViewModel.plansRepo().downloadPlans()
        mapDataViewModel.trailRepo().downloadTrails(60)

    }

    /*
    Configure the MapView, observe data from the ViewModel and add it to the map
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_map, container, false)
        val spinner = rootView.findViewById<ProgressBar>(R.id.progressBar)

        // Get the MapView
        map = rootView.findViewById(R.id.map)

        // Create a custom tile source
        val tileSource = ThunderforestTileSource(rootView.context, ThunderforestTileSource.CYCLE)
        map!!.setTileSource(tileSource)

        // Settings for map behaviour
        map!!.setScrollableAreaLimitDouble(BoundingBox(60.6463, 22.5913, 60.3286, 21.8195))
        map!!.isHorizontalMapRepetitionEnabled = false
        map!!.isVerticalMapRepetitionEnabled = false
        map!!.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        map!!.setMultiTouchControls(true)
        val mapController = map!!.controller
        mapController.setZoom(15.0)
        mapController.setCenter(mapDataViewModel.trailRepo().centerOfTurku)

        // Show credits in the bottom of the map
        val text = "Maps © <a href=\"https://www.thunderforest.com\">Thunderforest</a><br>" +
                "Data © <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a> contributors " +
                "& <a href=\"https://creativecommons.org/licenses/by/4.0/legalcode\">City of Turku</a>"
        val textView = rootView.findViewById<TextView>(R.id.creditsTextView)
        textView.text = Html.fromHtml(text, FROM_HTML_MODE_COMPACT)
        //textView.text = tileSource.copyrightNotice
        textView.movementMethod = LinkMovementMethod.getInstance()

        // Handle touches outside of polylines and markers
        val mapEventsOverlay = MapEventsOverlay(rootView.context, this)
        map!!.overlays.add(0, mapEventsOverlay)

        // Observe trail data
        mapDataViewModel.getPolylines().observe(this, Observer<ArrayList<Polyline>> { t ->

            // Add an info bubble and a touch listener for each polyline
            for (polyline in t){
                polyline.infoWindow = BasicInfoWindow(org.osmdroid.bonuspack.R.layout.bonuspack_bubble, map)
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
            // Add the polylines to map
            map!!.overlayManager.addAll(t!!)
            map!!.invalidate() })

        // Observe loading state and show the progress spinner accordingly
        mapDataViewModel.getRepoLoadingState().observe(this, Observer<Boolean> { loading ->
            if (loading){
                spinner.visibility = View.VISIBLE
            }
            else {
                spinner.visibility = View.GONE
                if (mapDataViewModel.getPolylines().value.isNullOrEmpty()){
                    // Notify user if no recent data was found
                    Snackbar.make(mapContainer, getString(R.string.text_snackbar), Snackbar.LENGTH_LONG).show()
                }
            }
        })

        // Observe cleaning plan data, and create markers for them
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
                // Add the marker to map
                map!!.overlays.add(marker)
                map!!.invalidate()
            }
        })

        return rootView

    }

    override fun longPressHelper(p: GeoPoint?): Boolean {
        // Do not handle long presses with the MapEventsOverlay
        return false
    }

    /*
    Called when the user taps the map outside polylines or markers.
     */
    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
        // Close all info bubbles
        InfoWindow.closeAllInfoWindowsOn(map)
        return true
    }

}