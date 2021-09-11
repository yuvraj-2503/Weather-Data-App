package com.example.weatherdataapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    val api : String= Config().API_KEY
    private val url = "https://api.openweathermap.org/data/2.5/weather"
    private val minTime : Long = 5000
    private val minDist : Float = 1000F
    private val requestCode : Int = 101
    private val locationProvider = LocationManager.GPS_PROVIDER
    private lateinit var mLocationManager : LocationManager
    private lateinit var mLocationListener : LocationListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE
        findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.GONE
        findViewById<TextView>(R.id.errorText).visibility = View.GONE
        getCurrentLocation()
    }

    fun getCurrentLocation(){
        mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val dialog= AlertDialog.Builder(this, R.style.Base_Theme_MaterialComponents_Dialog)
        dialog.setTitle("Location Access Needed")
        dialog.setMessage("Please turn on your location to proceed :)")
        dialog.setPositiveButton("OK"){ text,listener ->
            val intent= Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
        dialog.setNegativeButton("CLOSE"){ text, listener ->
            finishAffinity()
        }
        mLocationListener = object : LocationListener{
            override fun onLocationChanged(location: Location) {
                val latitude = location.latitude
                val longitude = location.longitude
                val params = RequestParams()
                params.put("lat", latitude)
                params.put("lon", longitude)
                params.put("appid", api)
                getWeather(params)
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                super.onStatusChanged(provider, status, extras)
            }

            override fun onProviderEnabled(provider: String) {

            }

            override fun onProviderDisabled(provider: String) {
                dialog.create()
                dialog.show()
            }
        }

        if(!ConnectionManager().checkConnectivity(this)){
            val dialog2= AlertDialog.Builder(this, R.style.Base_Theme_MaterialComponents_Dialog)
            dialog2.setTitle("No Internet")
            dialog2.setMessage("Please turn on your network connection to proceed..")
            dialog2.setPositiveButton("OK"){ text,listener ->
                val intent= Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(intent)
            }
            dialog2.setNegativeButton("CLOSE"){ text, listener ->
                finishAffinity()
            }
            dialog2.create()
            dialog2.show()
        }

//        if(!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
//
//            dialog.create()
//            dialog.show()
//        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                requestCode)
            return
        }
        mLocationManager.requestLocationUpdates(locationProvider, minTime, minDist, mLocationListener)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == this.requestCode){
            if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE
                findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.GONE
                findViewById<TextView>(R.id.errorText).visibility = View.GONE
                getCurrentLocation()
            }
        }
    }

    fun getWeather(params : RequestParams){
        val client = AsyncHttpClient()
        client.get(url, params, object : JsonHttpResponseHandler(){
            override fun onSuccess(
                statusCode: Int,
                headers: Array<out Header>?,
                response: JSONObject?
            ) {
//                super.onSuccess(statusCode, headers, response)
                val weatherData = response?.let { WeatherData().fromJson(it) }
                if (weatherData != null) {
                    updateUI(weatherData)
                    findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
                    findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.VISIBLE
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Array<out Header>?,
                throwable: Throwable?,
                errorResponse: JSONObject?
            ) {
//                super.onFailure(statusCode, headers, throwable, errorResponse)
                findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
                findViewById<TextView>(R.id.errorText).visibility = View.VISIBLE
            }
        })
    }

    fun updateUI(weatherData: WeatherData){
        findViewById<TextView>(R.id.address).text = weatherData.address
        findViewById<TextView>(R.id.updated_at).text = weatherData.updatedAtText
        findViewById<TextView>(R.id.temp).text = weatherData.temp
        findViewById<TextView>(R.id.min_temp).text = weatherData.minTemp
        findViewById<TextView>(R.id.max_temp).text = weatherData.maxTemp
        findViewById<TextView>(R.id.sunrise_time).text = weatherData.sunrise
        findViewById<TextView>(R.id.sunset_time).text = weatherData.sunset
        findViewById<TextView>(R.id.pressure).text = weatherData.pressure
        findViewById<TextView>(R.id.humidity).text = weatherData.humidity
        findViewById<TextView>(R.id.wind).text = weatherData.windSpeed
        findViewById<TextView>(R.id.status).text = weatherData.weatherDescription
    }
}