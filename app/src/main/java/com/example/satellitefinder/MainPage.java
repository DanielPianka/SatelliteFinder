package com.example.satellitefinder;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DecimalFormat;

public class MainPage extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private static Button goToListView;
    private static Button locate;

    protected LocationManager locationManager;
    protected ConnectivityManager connectivityManager;
    Double myLatitude;
    Double myLongitude;

    public GoogleMap map;
    public SupportMapFragment mapFragment;

    public static TextView name;
    public static TextView x;
    public static TextView y;
    public static TextView z;
    public static TextView latitude;
    public static TextView longitude;
    public static TextView data_up_to_date_on;
    public static TextView radial_length;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_main);
        Loader loader = new Loader(MainPage.this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (!locationManager.isLocationEnabled() || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            alertDialogNoGpsOrPermissions();
        } else if (connectivityManager.getActiveNetworkInfo() == null || !connectivityManager.getActiveNetworkInfo().isConnected()) {
            alertDialogNoNetworkConnention();
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            name = findViewById(R.id.name);
            x = findViewById(R.id.x);
            y = findViewById(R.id.y);
            z = findViewById(R.id.z);
            latitude = findViewById(R.id.latitude);
            longitude = findViewById(R.id.longitude);
            data_up_to_date_on = findViewById(R.id.data_up_to_date_on);
            radial_length = findViewById(R.id.radial_length);
            goToListView = findViewById(R.id.go_to_list_view);
            goToListView.setOnClickListener(view -> openListPage());
            locate = findViewById(R.id.locate);
            locate.setOnClickListener(view -> {
                if (!locationManager.isLocationEnabled() || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ) {
                    alertDialogNoGpsOrPermissions();
                } else if (connectivityManager.getActiveNetworkInfo() == null || !connectivityManager.getActiveNetworkInfo().isConnected()) {
                    alertDialogNoNetworkConnention();
                } else if (locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) == null) {
                    alertDialogCantLocate();
                } else {
                    Thread getDataThread = new Thread(() -> {
                        try {
                            if (ListPage.chosenObject == null) {
                                runOnUiThread(this::alertDialogNoneChosen);
                            } else {
                                runOnUiThread(loader::startLoadingDialog);
                                GetData.getData(ListPage.chosenObject.toLowerCase());
                                name.setText("NAME: " + ListPage.chosenObject.toUpperCase());
                                runOnUiThread(() -> {
                                    mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);
                                    assert mapFragment != null;
                                    mapFragment.getMapAsync(MainPage.this);
                                });
                                runOnUiThread(loader::dismissDialog);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    getDataThread.start();
                }
            });
        }
    }

    public void openListPage() {
        Intent intent = new Intent(this, ListPage.class);
        startActivity(intent);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.clear();
        if (GetData.latitude != null && GetData.longitude != null) {
            LatLng myLocation = new LatLng(myLatitude, myLongitude);
            MarkerOptions myMarkerOptions = new MarkerOptions();
            DecimalFormat df = new DecimalFormat("#.####");
            myMarkerOptions.position(myLocation).title("ME (" + df.format(myLatitude) + "°, " + df.format(myLongitude) + "°)").icon(BitmapDescriptorFactory.fromResource(R.drawable.my_marker));
            map.addMarker(myMarkerOptions);
            map.moveCamera(CameraUpdateFactory.newLatLng(myLocation));

            LatLng objectLocation = new LatLng(Double.parseDouble(GetData.latitude[0]), Double.parseDouble(GetData.longitude[0]));
            MarkerOptions objectMarkerOptions = new MarkerOptions();
            objectMarkerOptions.position(objectLocation).title(name.getText().toString()).icon(BitmapDescriptorFactory.fromResource(R.drawable.satellite_marker));

            Handler handlerAddObjectMarker = new Handler();
            handlerAddObjectMarker.postDelayed(() -> {
                map.addMarker(objectMarkerOptions);
                map.moveCamera(CameraUpdateFactory.newLatLng(objectLocation));
            }, 2000);
            Handler handlerAddBetweenLine = new Handler();
            handlerAddBetweenLine.postDelayed(() -> {
                PolylineOptions polyLineOptions = new PolylineOptions();
                polyLineOptions.color(Color.GRAY);
                polyLineOptions.add(myLocation, objectLocation);
                map.addPolyline(polyLineOptions);
            }, 4000);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        myLatitude = location.getLatitude();
        myLongitude = location.getLongitude();
    }

    public void alertDialogNoneChosen() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("None object was chosen!");
        dialog.setTitle("ERROR");
        dialog.setPositiveButton("OK",
                (dialog1, which) -> Toast.makeText(getApplicationContext(), "", Toast.LENGTH_LONG).cancel());
        AlertDialog alertDialog = dialog.create();
        alertDialog.show();
    }

    public void alertDialogNoGpsOrPermissions() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("Turn on GPS and remember to grant all needed permissions to the app!");
        dialog.setTitle("ERROR");
        dialog.setPositiveButton("OK",
                (dialog1, which) -> Toast.makeText(getApplicationContext(), "", Toast.LENGTH_LONG).cancel());
        AlertDialog alertDialog = dialog.create();
        alertDialog.show();
    }

    public void alertDialogNoNetworkConnention() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("No network connection!");
        dialog.setTitle("ERROR");
        dialog.setPositiveButton("OK",
                (dialog1, which) -> Toast.makeText(getApplicationContext(), "", Toast.LENGTH_LONG).cancel());
        AlertDialog alertDialog = dialog.create();
        alertDialog.show();
    }

    public void alertDialogCantLocate() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("Can't locate you, no GPS signal!");
        dialog.setTitle("ERROR");
        dialog.setPositiveButton("OK",
                (dialog1, which) -> Toast.makeText(getApplicationContext(), "", Toast.LENGTH_LONG).cancel());
        AlertDialog alertDialog = dialog.create();
        alertDialog.show();
    }

    public void alertDialogAboutApp(MenuItem item) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setIcon(R.drawable.info_icon);
        dialog.setMessage("Satellite Finder\nVersion 1.0\n\nAuthor: Daniel Pianka \nE-mail: d.pianka@gmail.com");
        dialog.setTitle("ABOUT APP");
        dialog.setPositiveButton("CLOSE",
                (dialog1, which) -> Toast.makeText(getApplicationContext(), "", Toast.LENGTH_LONG).cancel());
        AlertDialog alertDialog = dialog.create();
        alertDialog.show();
    }

    public void alertDialogTerminology(MenuItem item) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("• A satellite is any body with a relatively low mass that orbits another body with a greater mass. This body's path of motion is called an orbit. \n\n" +
                "• An artificial satellite is considered to be a man-made object moving in an orbit around a celestial body. The first artificial satellite was Sputnik 1, launched into orbit around the Earth by the Soviet Union on October 4, 1957. \n\n" +
                "• Geographic coordinates are latitude and longitude expressed as a measure of the angle from the origin of the geographic coordinate system. For Earth, the origin of the system is the intersection of the prime meridian with the equator. \n\n" +
                "• Geographic coordinates are specified in angular degrees (°). Each 1 ° is divided into a smaller auxiliary unit - minutes - ('). 1 ° = 60 ′. \n\n" +
                "• The radial distance is the distance from the center of the earth to the object that orbits it. This value takes into account both the radius of the earth and the height of the object above the surface of the planet. \n\n" +
                "• The ECEF (Earth-centered, Earth-fixed coordinate system) also known as a geocentric coordinate system is a Cartesian spatial reference system that represents locations near the Earth as X, Y, and Z measurements from its center of mass.");
        dialog.setTitle("TERMINOLOGY");
        dialog.setPositiveButton("CLOSE",
                (dialog1, which) -> Toast.makeText(getApplicationContext(), "", Toast.LENGTH_LONG).cancel());
        AlertDialog alertDialog = dialog.create();
        alertDialog.show();
    }

}