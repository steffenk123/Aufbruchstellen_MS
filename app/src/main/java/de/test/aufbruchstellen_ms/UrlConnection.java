package de.test.aufbruchstellen_ms;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Polygon;

import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Steffen and Sven on 18.06.2017.
 *
 * Class contains methods to build a connection in a seperate thread and handle the response
 */

public class UrlConnection extends AsyncTask<Void, Void, String> {
//&MAXFEATURES=500
    // Attributes
    private static final String URL_LINK = "https://www.stadt-muenster.de/ows/mapserv621/odaufgrabserv?REQUEST=GetFeature&SERVICE=WFS&VERSION=1.1.0&TYPENAME=aufgrabungen&EXCEPTIONS=XML&SRSNAME=EPSG:4326";

    private GoogleMap googleMap;
    private String resultString;

    public static final String PREFS_NAME = "XML_PREFS";
    public static final String PREFS_KEY = "XML_PREFS_String";

    Long start;
    Long end;

    // Constructor
    public UrlConnection(GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.resultString = "";
    }

    /**
     * Method tries to build a connection and create a xml-String from response
     *
     * @param params
     * @return xml as String
     */
    protected String doInBackground(Void... params) {
       start = System.currentTimeMillis();
        try {
            URL url = new URL(URL_LINK);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = httpURLConnection.getInputStream();

            //if response is empty
            if (inputStream == null) {
                return null;
            }
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                resultString += line;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultString;
    }

    /**
     * Method starts when the response is completed and calls the method to create the polygons
     *
     * @param xmlString
     */
    protected void onPostExecute(String xmlString) {
        try {
             PolygonController.buildPolygons(xmlString, googleMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        end = System.currentTimeMillis();
        Long ergebnis = end-start;

        Log.d("Uhrzeiten:",ergebnis.toString() );
    }

    // Getter- / Setter-Methods
    public String getResultString() {
        return resultString;
    }

    public void setResultString(String resultString) {
        this.resultString = resultString;
    }
}

