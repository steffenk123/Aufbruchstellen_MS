package de.test.aufbruchstellen_ms;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
    private ArrayList<Aufbruchstelle> aufbruchstellenList;
    private HashMap<Polygon, String> polygonValues;
    private UrlConnection urlConnection;

    // Attributes to handle the async-task
    private final int POLYGON_UPDATE_INTERVAL = 6000000;
    private final Handler handler = new Handler();

    // Attributes for location-permission
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mPermissionDenied = false;

    // Attributes to persist polygons
    private static final String PREFERENCE = "Aufbruchstelle";
    private static final String KEY_STRING = "Datensatz";

    // GUI-attributes
    private TextView infoPolygon;
    private EditText address;
    private Context context;
    private EditText adressfield;

    // Attributes for geocoding
    private String adress;
    private Marker marker;

    // Polygon
    private Polygon colourPolygon = null;

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

        Button button = (Button) findViewById(R.id.startgeocode);
        adressfield = (EditText) findViewById(R.id.getAdress);

        button.setOnClickListener(new View.OnClickListener() {
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
        urlConnection = new UrlConnection(mMap);

        int updateIntervall = 0;
        try {
            String value = getValue();
            //urlConnection.buildPolygons(value);
            PolygonController.buildPolygons(value, mMap);
        } catch (Exception ex) {
            //updateIntervall = 0;
            Toast.makeText(context, "Aufbruchstelle werden geladen!", Toast.LENGTH_SHORT).show();
        }

        // execute the async-task to update the polygons
        handler.postDelayed(updatePolygons, updateIntervall);
        updateIntervall = POLYGON_UPDATE_INTERVAL;

        // start position
        LatLng muenster = new LatLng(51.962, 7.626);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(muenster, 12));
        mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.setOnPolygonClickListener(this);

        enableMyLocation();
    }

    /**
     * Method checks whether permisson is set
     */
    public void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Method is called if permission is not set and asks the user again
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
     * Method shows the information-window of the polygon
     *
     * @param polygon
     */
    public void onPolygonClick(Polygon polygon) {
        //polygonValues = urlConnection.getPolygonValues();
        if(colourPolygon!=null){
            colourPolygon.setFillColor(Color.TRANSPARENT);
            colourPolygon.setStrokeColor(Color.BLACK);}

        polygon.setFillColor(Color.YELLOW);
        polygon.setStrokeColor(Color.YELLOW);

        polygonValues = PolygonController.getPolygonValues();
        infoPolygon.setText(polygonValues.get(polygon));
        infoPolygon.setMovementMethod(new ScrollingMovementMethod());

        colourPolygon = polygon;
    }

    /**
     * Method saves the Aufbruchstelle-xml as String in the sharedPreferences
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
     * Method returns the sharedPreference value
     *
     * @return
     */
    public String getValue() throws Exception {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_STRING, null);
    }

    /**
     * Method updates the Aufbruchstellen in background
     */
    Runnable updatePolygons = new Runnable() {
        public void run() {
            // Zuletzt abgerufene Daten speichern
            if (!urlConnection.getResultString().isEmpty()) {
                save(urlConnection.getResultString());
                Toast.makeText(context, "Aufbruchstelle wurden aktualisiert!", Toast.LENGTH_SHORT).show();
            }

            urlConnection = new UrlConnection(mMap);

            // Ausfuehren
            urlConnection.execute();
            handler.postDelayed(this, POLYGON_UPDATE_INTERVAL);
        }
    };

    /**
     * Method get an adress as a String and move the camera to the adress
     *
     * @param address
     * @throws Exception
     */
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
        } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
            Toast.makeText(context, "Keine Adresse gefunden!", Toast.LENGTH_SHORT).show();
        }

    }


}
