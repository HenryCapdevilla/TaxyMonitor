package com.example.gps_services;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity {
    public static final int DEFAULT_UPDATE_INTERVAL = 30;
    public static final int FAST_UDPDATE_INTERVAL = 5;

    private static final int PERMISSION_FINE_LOCATION = 99;
    private TextView tv_lat, tv_lon, tv_altitude, tv_address;

    //Location Request is config a file
    LocationRequest locationRequest;

    //Google Api for location services.
    FusedLocationProviderClient fusedLocationProviderClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Instanciamos las variables
        tv_lat = findViewById(R.id.tv_lat);
        tv_lon = findViewById(R.id.tv_lon);
        tv_altitude = findViewById(R.id.tv_altitude);
        tv_address = findViewById(R.id.tv_address);

        //Colocamos la configuración de  LocationRequest
        locationRequest = new LocationRequest();

        //How often does the default location check accur?
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL);

        //How often does the location check accur when set to the most frequent update?
        locationRequest.setFastestInterval(1000 + FAST_UDPDATE_INTERVAL);

        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        updateGPS();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case PERMISSION_FINE_LOCATION:
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    updateGPS();}
            else{
                    Toast.makeText(this, "Esta aplicación requiere de permiso concedido para trabajar con propiedades ", Toast.LENGTH_SHORT).show();
                    finish();
            }
            break;
        }
    }

    private void updateGPS(){
        //Obtenemos los permisos del usuario
        //Actualizamos la UI
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        if (ActivityCompat.checkSelfPermission (this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            //El usuario da permiso
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    //Cuando obtenemos los datos los actualizamos en la UI
                    updateUIValues(location);

                }
            });
        }
        else{
            //El usuario no da permiso aún :(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
            }
        }
    }

    private void updateUIValues(Location location) {
        //Update all of the text view with a new location
        tv_lat.setText(String.valueOf(location.getLatitude()));
        tv_lon.setText(String.valueOf(location.getLongitude()));

        if(location.hasAltitude()){
            tv_altitude.setText(String.valueOf(location.getAltitude()));
        }
        else {
            tv_altitude.setText("N/A");
        }

    }
}