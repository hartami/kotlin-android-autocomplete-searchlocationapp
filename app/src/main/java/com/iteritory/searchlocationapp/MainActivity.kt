package com.iteritory.searchlocationapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


//import sun.text.normalizer.UTF16.append



class MainActivity : AppCompatActivity() {

    val mydata = arrayListOf(
        "Meisenweg 20789 Mannheim",
        "Mullerstrasse 18789 Ausburg",
        "Michaelestrasse 29332 Stuttgart",
        "Hauptsrasse 12345 Heildelberg",
        "Darwinstrasse 33323 Ulm",
        "Panamastrasse 23412 Berlin",
        "Paristrasse 13453 Leipzig"
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // to obtain data from dataloc.xml
        parseXML()

        // to obtain data from datalokal.json
        var jsonString: String? = null
        try {
            val inputStream: InputStream = assets.open("datalokal.json")
            jsonString = inputStream.bufferedReader().use { it.readText() }
            val foos = Response(jsonString)
            val dataJson= foos.data

            mydata.add(dataJson.toString())

        } catch (e: IOException) {
        }

        // to obtain data from MyExternalDatabase

        //val databaseAccess = DatabaseAccess.getInstance()
        //databaseAccess.open()
        //var dataexternal = databaseAccess.getAddress()
        //databaseAccess.close()
        //val adapter1 = ArrayAdapter<String> (this, android.R.layout.simple_list_item_1, dataexternal)
        //textView.setText(dataexternal)



        // to perform autocomplete search
        var adapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            mydata
        )
        auto_complete_text_view_arrival.threshold = 1
        auto_complete_text_view_arrival.setAdapter(adapter)
        auto_complete_text_view.threshold = 1
        auto_complete_text_view.setAdapter(adapter)

    }

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

    private fun getLocations(locations: ArrayList<Location>) {
        val builder = StringBuilder()


        for (location in locations) {
            builder.append(location.streetname).append(" ").append(location.postal).append(" ").append(location.city)
                .append("\n")
        }
        mydata.add(builder.toString())

    }


    class Response(json: String) : JSONObject(json) {
        val type: String? = this.optString("type")
        val data = this.optJSONArray("data")
            ?.let { 0.until(it.length()).map { i -> it.optJSONObject(i) } } // returns an array of JSONObject
            ?.map {Foo(it.toString())} // transforms each JSONObject of the array into Foo it.toString()

    }
    class Foo(json: String) : JSONObject(json) {
        val streetname: String? = this.optString("street")
        val code = this.optInt("postal_code")
        val cityname: String? = this.optString("city")
        val fullname= streetname + code + cityname
    }


}




/*fun loadAddress(view: View) {
    val res = getResources()
    val `is` = res.openRawResource(R.raw.dataaddress)
    val scanner = Scanner(`is`)

    val builder = StringBuilder()

    while (scanner.hasNextLine()) {
        builder.append(scanner.nextLine())
    }

    parseJson(builder.toString())
}

private fun parseJson(a: String) {
    val textDisplay = findViewById<View>(R.id.text_display) as TextView
    textDisplay.setText(a)
}  */


