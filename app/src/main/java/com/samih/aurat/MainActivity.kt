package com.samih.aurat

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_map.*
import org.osmdroid.config.Configuration
import android.app.Activity
import android.content.DialogInterface


class MainActivity : AppCompatActivity() {

    val PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: Int = 1
    lateinit var mFragmentManager: FragmentManager
    lateinit var mapFragment: MapFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Check for permission to write to storage (needed for storing map tiles)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, the map will not display correctly
            promptWritePermissions()
        }
        else {
            // Permission has already been granted
        }

        setContentView(R.layout.activity_main)
        mFragmentManager = supportFragmentManager
        mapFragment = MapFragment()
        supportFragmentManager.beginTransaction().add(R.id.fragment_container, mapFragment).commit()
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID

    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted
                } else {
                    // permission was denied
                    showSnackbar()
                }
                return
            }
            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    /*
    Ask the user for write permission, open an explanatory dialog first if needed
     */
    fun promptWritePermissions(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Explanation should be shown to the user, open dialog
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle(getString(R.string.text_title_alert_dialog))
            alertDialog.setMessage(getString(R.string.text_message_alert_dialog))
            alertDialog.setPositiveButton(getString(R.string.text_positive_button)
            ) { _, _ ->
                ActivityCompat.requestPermissions(this as Activity,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) }
            alertDialog.setOnCancelListener(OnCancelPermissionDialogListener())
            val alert = alertDialog.create()
            alert.show()
        }
        else {
            // Explanation dialog not needed, request the permission
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE)
        }
    }

    /*
    Opens a Snackbar with a button that opens the permission request dialog
     */
    private fun showSnackbar(){
        Snackbar.make(mapContainer, getString(R.string.text_permission_snackbar),
                Snackbar.LENGTH_INDEFINITE).setAction(R.string.text_permission_change,
                ChangePermissionsListener()).show()
    }
    inner class OnCancelPermissionDialogListener : DialogInterface.OnCancelListener {
        override fun onCancel(dialog: DialogInterface?) {
            showSnackbar()
        }
    }
    inner class ChangePermissionsListener : View.OnClickListener {

        override fun onClick(v: View) {
            promptWritePermissions()
        }
    }



}
