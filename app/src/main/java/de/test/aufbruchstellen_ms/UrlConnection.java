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
 * Created by Steffen Kramer and Sven Beyel on 18.06.2017.
 */

public class UrlConnection extends AsyncTask<Void, Void, String> {

    private static String urlString = "https://www.stadt-muenster.de/ows/mapserv621/odaufgrabserv?REQUEST=GetFeature&SERVICE=WFS&VERSION=1.1.0&TYPENAME=aufgrabungen&EXCEPTIONS=XML&MAXFEATURES=500&SRSNAME=EPSG:4326";
    private ArrayList<Aufbruchstellen> aufbruchstellenList;
    private ArrayList<Polygon> removeList = new ArrayList<>();
    private static HashMap<Polygon, String> polygonValues = new HashMap<>();

    private GoogleMap googleMap;
    private String resultString = "";

    public static final String PREFS_NAME = "XML_PREFS";
    public static final String PREFS_KEY = "XML_PREFS_String";


    public UrlConnection(GoogleMap googleMap, ArrayList<Polygon> removeList) {
        this.googleMap = googleMap;
        this.removeList = removeList;
    }

    /**a
     * @param params
     * @return
     */
    protected String doInBackground(Void... params) {
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

        return resultString;
    }

    /**
     * Wir aufgerufen, wenn der Response vollstaendig angekommen ist
     * ruft das Erzeugen der Polygone auf
     *
     * @param s
     */
    protected void onPostExecute(String s) {
        try {
            buildPolygons(s);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Erzeugt die Polygone und fuegt sie der Karte hinzu
     *
     * @param s
     * @throws Exception
     */
    public void buildPolygons(String s) throws Exception {
        Document document = AufbruchstellenController2.parseStringtoDocument(s);
        aufbruchstellenList = AufbruchstellenController2.getAufbruchstellenList(document);

        // Alle Polygone loeschen und Liste leeren
        removeAllPolygons();
        removeList.clear();

        for (int i = 0; i < aufbruchstellenList.size(); i++) {
            for (int j = 0; j < aufbruchstellenList.get(i).getGeometrie().size(); j++) {
                Polygon polygon = googleMap.addPolygon(aufbruchstellenList.get(i).getGeometrie().get(j).clickable(true));

                polygonValues.put(polygon, "ID: " + aufbruchstellenList.get(i).getId() + "\n" +
                        "Träger: " + aufbruchstellenList.get(i).getTraeger() + "\n" +
                        "Straßen: " + aufbruchstellenList.get(i).getStrassen() + "\n" +
                        "Spuren: " + aufbruchstellenList.get(i).getSpuren() + "\n" +
                        "Beginn: " + aufbruchstellenList.get(i).getBeginn() + "\n");

                removeList.add(polygon);
            }
        }
    }

    /**
     * Loescht alle Polygone aus der Map
     */
    public void removeAllPolygons() {
        for (Polygon polygon : removeList) {
            polygon.remove();
        }
    }

    public ArrayList<Polygon> getRemoveList() {
        return removeList;
    }

    public ArrayList<Aufbruchstellen> getAufbruchstellenList() {
        return aufbruchstellenList;
    }

    public HashMap<Polygon, String> getPolygonValues() {
        return polygonValues;
    }

    public String getResultString() {
        return resultString;
    }

    public void setResultString(String resultString) {
        this.resultString = resultString;
    }
}

