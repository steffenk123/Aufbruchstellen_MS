package de.test.aufbruchstellen_ms;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.os.Build.VERSION_CODES.M;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnPolygonClickListener {

    // Attributes
    private GoogleMap mMap;
    private ArrayList<Aufbruchstellen> aufbruchstellenList;
    private TextView infoPolygon;
    HashMap<Polygon, String> polygonValues;


    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mPermissionDenied = false;
    UrlConnection urlConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        infoPolygon = (TextView) findViewById(R.id.infoPolygon);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



        // urlConnection.delegate = this;

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // aufbruchstellenList = urlConnection.getAufbruchstellenList();
        //  Log.d("Test Zeile 70", aufbruchstellenList.toString());
        urlConnection = new UrlConnection(googleMap);

    /*  urlConnection.execute();
        while (urlConnection.getResultString().isEmpty()){
            Log.d("Zeile87", "empty");
        }
        Log.d("Zeile 86", urlConnection.getResultString());
        save(urlConnection.getResultString());
*/

        String value = getValue();
        urlConnection.buildPolygons(value);

        // Nur im Hintergund






        //Add a marker in muenster and move the camera
        LatLng muenster = new LatLng(51.962, 7.626);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(muenster, 12));

        mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.setOnPolygonClickListener(this);

        // Methodenaufruf für eigenen Standort
        enableMyLocation();


    }

    /**
     * dt. Ueberpruefen ob Standortberechtigunbgen erteillt wurden
     */
    public void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            // Berechtigung
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * dt. Methode faengt die Antwort des Dialogfensters der Standortberechtigung ab
     * und meldet bei fehlender Berechtigung, dass ein Standort nicht angezeigt werden
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     * @throws SecurityException
     */


    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) throws SecurityException {

        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {

            if (permissions.length == 1 && permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);

            } else {
                mPermissionDenied = true;
                new AlertDialog.Builder(this).setMessage("Keine Anzeige des Gerätestandortes möglich!")
                        .setNegativeButton("Akzeptieren", null)
                        .setPositiveButton("Zurück", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                enableMyLocation();
                            }
                        }).create().show();
            }

        }
    }

    public void onPolygonClick(Polygon polygon) {

        polygonValues = urlConnection.getPolygonValues();
        infoPolygon.setText(polygonValues.get(polygon));


    }

    public void save(String text){
       /* SharedPreferences settings;
        SharedPreferences.Editor editor;
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();

        editor.putString(PREFS_KEY, text);
        editor.commit();*/
        SharedPreferences sharedPreferences;
        sharedPreferences = getSharedPreferences("MyPrefs",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Aufbruchstellen", text);
        Log.d("Zeile 175", text);
        editor.commit();
    }

    public String getValue() {
        SharedPreferences sharedPreferences;
        String text;
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        text = sharedPreferences.getString("Aufbruchstellen",null);
        Log.d("Zeile 183", text);
        return text;
    }
}
