package com.iteritory.searchlocationapp

import android.app.DownloadManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils.replace
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.io.InputStream
import java.util.ArrayList



class MainActivity : AppCompatActivity() {

    val initialdata = arrayListOf(
        "Meisenweg 45, 20789 Mannheim",
        "Mullerstrasse 1, 18789 Ausburg",
        "Michaelestrasse 89, 29332 Stuttgart",
        "Hauptsrasse 12, 12345 Heildelberg",
        "Darwinstrasse, 33323 Ulm",
        "Panamastrasse, 23412 Berlin",
        "Parisertrasse, 13453 Leipzig"
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fetchJson()

        // to call methods and obtain data from dataloc.xml
        parseXML()

        // to call methods and obtain data from datalokal.json
        var jsonString: String? = null
        try {
            val inputStream: InputStream = assets.open("datalokal.json")
            jsonString = inputStream.bufferedReader().use { it.readText() }

            val foos = Response(jsonString)
            val dataJson= foos.data
            val length = foos.length()
            for (i in 0..length-1) {
                var eachdata = foos.data?.get(i).toString()
                val cleaner = Regex("[^A-Za-z0-9]")
                eachdata = cleaner.replace(eachdata, "")
                var cleandata = eachdata.replace("street", "", true)
                var cleandata2 = cleandata.replace("postalcode", ", ", true)
                var cleandata3 = cleandata2.replace("city", " ", true)
                initialdata.add(cleandata3)
            }

        } catch (e: IOException) {
        }

        // to obtain data from MyExternalDatabase

        //val databaseAccess = DatabaseAccess.getInstance()
        //databaseAccess.open()
        //var dataexternal = databaseAccess.getAddress()
        //databaseAccess.close()
        //val adapter1 = ArrayAdapter<String> (this, android.R.layout.simple_list_item_1, dataexternal)
        //textView.setText(dataexternal)



        // to perform autocomplete when users input data
        // using ArrayAdapter
        var adapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            initialdata
        )
        auto_complete_text_view_arrival.threshold = 1
        auto_complete_text_view_arrival.setAdapter(adapter)
        auto_complete_text_view.threshold = 1
        auto_complete_text_view.setAdapter(adapter)
    }

    // to read XML file: dataloc.xml
    private fun parseXML() {
        val parserFactory: XmlPullParserFactory
        try {
            parserFactory = XmlPullParserFactory.newInstance()
            val parser = parserFactory.newPullParser()
            val `is` = assets.open("dataloc.xml")
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(`is`, null)

            processParsing(parser)

        } catch (e: XmlPullParserException) {

        } catch (e: IOException) {
        }
    }
    @Throws(IOException::class, XmlPullParserException::class)
    private fun processParsing(parser: XmlPullParser) {
        val locations = ArrayList<Location>()
        var eventType = parser.eventType
        var currentLoc: Location? = null

        while (eventType != XmlPullParser.END_DOCUMENT) {
            var eltName: String? = null

            when (eventType) {
                XmlPullParser.START_TAG -> {
                    eltName = parser.name

                    if ("location" == eltName) {
                        currentLoc = Location()
                        locations.add(currentLoc)
                    } else if (currentLoc != null) {
                        if ("streetname" == eltName) {
                            currentLoc!!.streetname = parser.nextText()
                        } else if ("postal" == eltName) {
                            currentLoc!!.postal = parser.nextText()
                        } else if ("city" == eltName) {
                            currentLoc!!.city = parser.nextText()
                        }
                    }
                }
            }

            eventType = parser.next()
        }

        getLocations(locations)
    }

    // to process each location data from dataloc.xml
    private fun getLocations(locations: ArrayList<Location>) {
        for (location in locations) {
            val builder = StringBuilder()
            builder.append(location.streetname).append(", ").append(location.postal).append(" ").append(location.city)
                .append("\n")
            initialdata.add(builder.toString())
        }
    }

    // to process each data from datalokal.json
    class Response(json: String) : JSONObject(json) {
        val type: String? = this.optString("type")
        val data = this.optJSONArray("data")
            ?.let { 0.until(it.length()).map { i -> it.optJSONObject(i) } } // returns an array of JSONObject
            ?.map {Foo(it.toString())} // transforms each JSONObject of the array into Foo
    }
    class Foo(json: String) : JSONObject(json) {
        var streetname: String? = this.optString("street")
        var code = this.optInt("postal_code")
        var cityname: String? = this.optString("city")
        var fullname= streetname + code + cityname
    }

    fun fetchJson(){
        println("Success")
        val url = "https://raw.githubusercontent.com/openaddresses/openaddresses/master/sources/de/berlin.json"
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object: Callback{
            override fun onResponse(call: Call, response: okhttp3.Response) {
                val body = response.body()?.string()
                println(body)
            }

            override fun onFailure(call: Call, e: IOException) {
                println("Failed to execute request")
            }

        })


    }
}




