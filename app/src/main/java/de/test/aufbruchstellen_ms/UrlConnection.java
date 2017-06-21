package de.test.aufbruchstellen_ms;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import static android.os.Build.VERSION_CODES.M;

/**
 * Created by Steffen on 18.06.2017.
 */

public class UrlConnection extends AsyncTask<Void, Void, String>  {

    private static String urlString = "https://www.stadt-muenster.de/ows/mapserv621/odaufgrabserv?REQUEST=GetFeature&SERVICE=WFS&VERSION=1.1.0&TYPENAME=aufgrabungen&EXCEPTIONS=XML&MAXFEATURES=100&SRSNAME=EPSG:4326";
    ArrayList<Aufbruchstellen> aufbruchstellenList;
    private GoogleMap googleMap;
    private String resultString = "";

    private static HashMap<Polygon, String> polygonValues = new HashMap<>();


    public static final String PREFS_NAME = "XML_PREFS";
    public static final String PREFS_KEY = "XML_PREFS_String";


    public UrlConnection(GoogleMap googleMap) {
        this.googleMap = googleMap;

    }

    @Override
    protected String doInBackground(Void... params) {
        //resultString = "";
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
                resultString += line;
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("Zeile 77", resultString);
         //save(); // Muss evtl noch irgendwo ne Erneuerung hin um die Daten zu aktualisieren?
        return resultString;
    }

    @Override  // hier theorhetisch keinen String mehr übergeben sondern den alten String verwenden
    protected void onPostExecute(String s) {
       buildPolygons(s);
    }


    public void buildPolygons(String s){
        Log.d("Zeile 89", "Polygone bauen1");
        Document document;
        Log.d("Zeile 91", "Polygone bauen1");
        // Hier dann die Methode aufrufen um sich den String zu holen aus den results

        try {
            Log.d("Zeile 95", "Polygone bauen1");
            document = AufbruchstellenController2.parseStringtoDocument(s);
            Log.d("Zeile 96", "Polygone bauen1");
            aufbruchstellenList = AufbruchstellenController2.getAufbruchstellenList(document);
            Log.d("Zeile 98", "Polygone bauen1");


            for (int i = 0; i < aufbruchstellenList.size(); i++) {
                for (int j = 0; j < aufbruchstellenList.get(i).getGeometrie().size(); j++) {
                    Log.d("Zeile 101", "Polygone bauen");
                    Polygon polygon = googleMap.addPolygon(aufbruchstellenList.get(i).getGeometrie().get(j).clickable(true));

                    polygonValues.put(polygon, "ID: " + aufbruchstellenList.get(i).getId() + "\n" +
                            "Träger: " + aufbruchstellenList.get(i).getTraeger() + "\n" +
                            "Straßen: " + aufbruchstellenList.get(i).getStrassen() + "\n" +
                            "Spuren: " + aufbruchstellenList.get(i).getSpuren() + "\n" +
                            "Beginn: " + aufbruchstellenList.get(i).getBeginn() + "\n");

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public ArrayList<Aufbruchstellen> getAufbruchstellenList() {
        return aufbruchstellenList;
    }

    public HashMap<Polygon, String> getPolygonValues() {
        return polygonValues;
    }


    public String getResultString() {
        Log.d("Zeile 128", resultString);
        return resultString;
    }


}

