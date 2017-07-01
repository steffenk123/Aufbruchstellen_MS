package de.test.aufbruchstellen_ms;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnPolygonClickListener {

    // Attributes
    private GoogleMap mMap;
    private ArrayList<Aufbruchstellen> aufbruchstellenList;
    private HashMap<Polygon, String> polygonValues;
    private boolean mPermissionDenied = false;
    private UrlConnection urlConnection;

    private ArrayList<Polygon> removeList = new ArrayList<>();
    private final int POLYGON_UPDATE_INTERVAL = 60000;
    private final Handler handler = new Handler();

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final String PREFERENCE = "Aufbruchstellen";
    private static final String KEY_STRING = "Datensatz";

    private TextView infoPolygon;
    private EditText address;
    private Context context;
    private EditText adressfield;

    private String adress;
    private Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        infoPolygon = (TextView) findViewById(R.id.infoPolygon);
        context = this;

        Button button = (Button)findViewById(R.id.startgeocode);
        adressfield =  (EditText) findViewById(R.id.getAdress);

        button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                try {
                    getGeo(adressfield.getText().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
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
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;

        // Neue URLConnection anlegen und String mit letztem Response aus einer Datei auslesen und daraus Polygone erstellen
        urlConnection = new UrlConnection(mMap, removeList);
        try {
            urlConnection.buildPolygons("test");
        } catch (Exception e) {
            e.printStackTrace();
        }
        int updateIntervall = POLYGON_UPDATE_INTERVAL;
        try {
            String value = getValue();
            urlConnection.buildPolygons(value);
        } catch (Exception ex) {
            updateIntervall = 0;
            Toast.makeText(context, "Aufbruchstellen werden geladen!", Toast.LENGTH_SHORT).show();
        }

        // Fuehrt den Hintergund Thread aus um die Polygone zu aktualisieren
        handler.postDelayed(updatePolygons, updateIntervall);
        updateIntervall = POLYGON_UPDATE_INTERVAL;

        //Add a marker in muenster and move the camera
        LatLng muenster = new LatLng(51.962, 7.626);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(muenster, 12));
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Clicklistener setzen, um die POlygone Clickbar zu machen
        mMap.setOnPolygonClickListener(this);

        // Methodenaufruf für eigenen Standort
        enableMyLocation();
    }


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

    /**
     * Liefert die Information zu einem gewaehlten Polygon
     *
     * @param polygon
     */
    public void onPolygonClick(Polygon polygon) {
        polygonValues = urlConnection.getPolygonValues();
        infoPolygon.setText(polygonValues.get(polygon));
        infoPolygon.setMovementMethod(new ScrollingMovementMethod());
    }

    /**
     * Speichert die Aufbruchstellen als String
     *
     * @param text
     */
    public void save(String text) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_STRING, text);
        editor.commit();
    }

    /**
     * gibt den gespeciherten String mit den Aufbruchstellen aus
     *
     * @return
     */
    public String getValue() throws Exception {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_STRING, null);
    }

    /**
     * Laedt im Hintergund die Aufbruchstellen neu
     */
    Runnable updatePolygons = new Runnable() {
        public void run() {
            // Zuletzt abgerufene Daten speichern
            if (!urlConnection.getResultString().isEmpty()) {
                save(urlConnection.getResultString());
                Toast.makeText(context, "Aufbruchstellen wurden aktualisiert!", Toast.LENGTH_SHORT).show();
            }
            urlConnection.getStatus();
            removeList = urlConnection.getRemoveList();
            urlConnection = new UrlConnection(mMap, removeList);

            // Ausfuehren
            urlConnection.execute();
            handler.postDelayed(this, POLYGON_UPDATE_INTERVAL);
        }
    };


   public void getGeo(String address) throws Exception {
        Geocoder geoCoder = new Geocoder(this, Locale.GERMANY);
        Log.d("Zeile 221", address);

        try {
            List<Address> addresses = geoCoder.getFromLocationName(address, 1);


            Address adress = addresses.get(0);

            LatLng coord = new LatLng(adress.getLatitude(), adress.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coord, 15));
            if (marker != null) {
                marker.remove();
            }
            marker = mMap.addMarker(new MarkerOptions().position(coord));
        }catch(IndexOutOfBoundsException indexOutOfBoundsException){
            Toast.makeText(context, "Keine Adresse gefunden!", Toast.LENGTH_SHORT).show();
       }


       //infoPolygon.setText(addresses.size());

    }


}
