package com.samih.aurat


import android.os.AsyncTask
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import com.opencsv.CSVReader

class PlansRepo {
    private val DEBUG_TAG = "PlansRepo"
    private val PLANS_URL = "http://puhdistussuunnitelmat.fi/turku/api/export.csv"

    private val planList = MutableLiveData<ArrayList<JobPlan>>()

    data class JobPlan(var date: String,
                       var street: String,
                       var description: String,
                       var zipCode: String,
                       var latitude: Double,
                       var longitude: Double)

    fun getPlanList(): LiveData<ArrayList<JobPlan>>{
        return planList
    }

    fun downloadPlans() {
            DownloadPlansTask(this).execute(PLANS_URL)
    }
    companion object {
        private class DownloadPlansTask(private val plansRepo: PlansRepo) : AsyncTask<String, Void, String>() {

            override fun doInBackground(vararg urls: String): String {

                // params comes from the execute() call: params[0] is the url.
                return try {
                    downloadUrl(urls[0])
                } catch (e: IOException) {
                    "Unable to retrieve web page. URL may be invalid."
                }

            }


            override fun onPostExecute(result: String) {
                val reader = CSVReader(StringReader(result))
                val plans = ArrayList<JobPlan>()

                run {
                    var line: Array<out String>? = reader.readNext()
                    while (line != null) {
                        val plan = JobPlan(line[0],
                                line[1],
                                line[2],
                                line[3],
                                line[4].toDouble(),
                                line[5].toDouble())
                        plans.add(plan)
                        line = reader.readNext()
                    }
                }
                plansRepo.planList.value = plans
                reader.close()
            }

            override fun onPreExecute() {

            }


            /**
             * Given a URL, establishes an HttpUrlConnection and retrieves the web page content as a InputStream, which it returns as a string.
             * @param fileUrl URL of the remote content
             * @return String of the web page content, JSON formatted.
             * @throws IOException
             */
            @Throws(IOException::class)
            private fun downloadUrl(fileUrl: String): String {
                var input: InputStream? = null
                var conn: HttpURLConnection? = null


                try {
                    val url = URL(fileUrl)
                    conn = url.openConnection() as HttpURLConnection
                    conn.readTimeout = 10000
                    conn.connectTimeout = 15000
                    conn.requestMethod = "GET"
                    conn.doInput = true
                    // Starts the query
                    conn.connect()
                    val response = conn.responseCode
                    Log.d(plansRepo.DEBUG_TAG, "The response is: $response")
                    //input = conn.inputStream
                    input = BufferedInputStream(url.openStream(),
                            8192)


                    // Convert the InputStream into a string
                    return readIt(input)

                    // Makes sure that the InputStream is closed after the app is
                    // finished using it.
                } finally {
                    input?.close()
                    conn?.disconnect()

                }
            }


            /**
             * Reads an InputStream and converts it to a String.
             * @param stream
             * @return String converted from the InputStream
             * @throws IOException
             * @throws UnsupportedEncodingException
             */
            @Throws(IOException::class, UnsupportedEncodingException::class)
            fun readIt(stream: InputStream): String {
                val reader = BufferedReader(InputStreamReader(stream, "UTF-8"))
                val total = StringBuilder()

                run {
                    var line: String? = reader.readLine()
                    while (line != null) {
                        total.append(line).append('\n')
                        line = reader.readLine()
                    }
                }

                reader.close()
                return total.toString()
            }
        }
    }
}
