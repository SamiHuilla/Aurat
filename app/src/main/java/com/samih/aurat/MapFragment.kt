package com.samih.aurat

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_map.*
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [BlankFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [BlankFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class MapFragment : Fragment() {
    // TODO: Rename and change types of parameters
    /*
    private var mParam1: String? = null
    private var mParam2: String? = null
    private var mListener: OnFragmentInteractionListener? = null
    */
    private var map: MapView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //TODO: poista fragmenttisekoilut, initializeOSM kutsutaan nyt joka kerta kun fragmentiin palataan
        MapManager.initializeOSM(96)
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
        map = rootView.findViewById(R.id.map)
        val mapDataViewModel = ViewModelProviders.of(this).get(MapDataViewModel::class.java)
        mapDataViewModel.getPolylines().observe(this, Observer<ArrayList<Polyline>> { t ->
            //map!!.overlayManager.clear()
            map!!.overlayManager.addAll(t!!)
            map!!.invalidate() })
        map!!.setTileSource(TileSourceFactory.MAPNIK)
        map!!.setScrollableAreaLimitDouble(BoundingBox(70.127855,31.748989, 59.687982, 19.236935))
        //map!!.setBuiltInZoomControls(true)
        map!!.setMultiTouchControls(true)

        val mapController = map!!.controller
        mapController.setZoom(15.0)
        mapController.setCenter(MapManager.centerOfTurku) // TODO: use location if enabled
        //rootView.findViewById<FloatingActionButton>(R.id.floatingActionButton).setOnClickListener { toggleLayersMenu() }
        return rootView

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

    // TODO: Rename method, update argument and hook method into UI event
    /*
    fun onButtonPressed(uri: Uri) {
        mListener?.onFragmentInteraction(uri)
    }
    */

    /*
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }
    */

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */

    /*
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment BlankFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String): MapFragment {
            return MapFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
        }
    }
    */

