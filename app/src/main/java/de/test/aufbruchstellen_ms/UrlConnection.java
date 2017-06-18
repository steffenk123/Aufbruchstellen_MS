package de.test.aufbruchstellen_ms;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Steffen on 18.06.2017.
 */

public class UrlConnection extends AsyncTask<Void, Void, String> {

    private static String urlString = "https://www.stadt-muenster.de/ows/mapserv621/odaufgrabserv?REQUEST=GetFeature&SERVICE=WFS&VERSION=1.1.0&TYPENAME=aufgrabungen&EXCEPTIONS=XML&MAXFEATURES=10&SRSNAME=EPSG:4326";
    ArrayList<Aufbruchstellen> aufbruchstellenList;
    public AsyncResponse delegate = null;
    private GoogleMap googleMap;

    public UrlConnection(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    @Override
    protected String doInBackground(Void... params) {
        String ergebnis = "";
        try {
            URL url = new URL(urlString);

            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            InputStream inputStream = httpURLConnection.getInputStream();

            if (inputStream == null) { //falls keine Aufbruchstellendaten vorhanden sind
                return null;
            }
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;


            while ((line = bufferedReader.readLine()) != null) {
                ergebnis += line;
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return ergebnis;
    }

    @Override
    protected void onPostExecute(String s) {
        Log.d("Test zeile 54", s);
        Document document;
        //ArrayList<Aufbruchstellen> aufbruchstellenList;
        try {
            document = AufbruchstellenController2.parseStringtoDocument(s);
            Log.d("Test zeile 58", document.toString());
            aufbruchstellenList = AufbruchstellenController2.getAufbruchstellenList(document);
            Log.d("Test zeile 62", aufbruchstellenList.toString());
            //delegate.processFinish(aufbruchstellenList);
            LatLng muenster = new LatLng(51.962, 7.626);
            googleMap.addMarker(new MarkerOptions().position(muenster));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public ArrayList<Aufbruchstellen> getAufbruchstellenList() {
        return aufbruchstellenList;
    }
}

