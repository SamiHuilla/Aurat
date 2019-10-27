package com.samih.aurat

import com.samih.aurat.BuildConfig.BASE_URL
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.doAsyncResult
import org.jetbrains.anko.uiThread
import java.net.URL

/**
 * Created by sami on 10.10.2019.
 */
object PlowApiWrapper {
    fun fetchActivePlows(hours: Int){
        doAsyncResult {
            val result = StringBuilder(URL(BASE_URL + "?since=" + hours + "hours+ago&limit=50").readText())
            uiThread {
                MapManager.parseActivePlows(result, hours)
            }
        }

    }

    fun fetchIndividualPlowTrail(hours: Int, id: Int?){
        doAsync {
            val result = StringBuilder(URL(BASE_URL + id + "?since=" + hours + "hours+ago&temporal_resolution=1").readText())
            uiThread {
                MapManager.parseIndividualPlowData(result)
            }
        }
    }
}