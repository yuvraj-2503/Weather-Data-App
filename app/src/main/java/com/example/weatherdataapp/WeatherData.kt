package com.example.weatherdataapp

import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class WeatherData {
    lateinit var temp : String
    lateinit var minTemp: String
    lateinit var maxTemp: String
    lateinit var pressure: String
    lateinit var humidity: String
    lateinit var sunrise: String
    lateinit var sunset : String
    lateinit var windSpeed: String
    lateinit var weatherDescription : String
    lateinit var address : String
    lateinit var updatedAtText : String

    fun fromJson(jsonObject: JSONObject): WeatherData{
        val weatherData = WeatherData()
        weatherData.address = jsonObject.getString("name") + ", " +
                jsonObject.getJSONObject("sys").getString("country")
        val updatedAt : Long = jsonObject.getLong("dt")
        weatherData.updatedAtText= "Updated At : " + SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH)
            .format(Date(updatedAt * 1000))
        weatherData.weatherDescription = jsonObject.getJSONArray("weather")
            .getJSONObject(0).getString("description").capitalize()
        weatherData.windSpeed = jsonObject.getJSONObject("wind").getString("speed")
        weatherData.pressure = jsonObject.getJSONObject("main").getString("pressure")
        weatherData.humidity = jsonObject.getJSONObject("main").getString("humidity")
        weatherData.temp = (jsonObject.getJSONObject("main").getDouble("temp").roundToInt() - 273).toString() + " ℃"
        weatherData.minTemp = "Min Temp : " + (jsonObject.getJSONObject("main").getDouble("temp_min").roundToInt() - 273).toString() + " ℃"
        weatherData.maxTemp = "Max Temp : " + (jsonObject.getJSONObject("main").getDouble("temp_max").roundToInt() - 273).toString() + " ℃"
        val sunriseTime = jsonObject.getJSONObject("sys").getLong("sunrise")
        val sunsetTime = jsonObject.getJSONObject("sys").getLong("sunset")
        weatherData.sunrise = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunriseTime*1000))
        weatherData.sunset = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunsetTime*1000))
        return weatherData
    }


}