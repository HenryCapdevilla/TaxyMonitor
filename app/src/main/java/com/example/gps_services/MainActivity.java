package com.example.gps_services;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import java.util.Date;

import androidx.appcompat.app.AppCompatActivity;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;



public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_FINE_LOCATION = 99;
    private TextView tv_lbladdress;
    private Button Btn_Get_Location;
    private Button tcpButton;
    private Button udpButton;
    private EditText ipAddressEditText;

    //Google Api for location services.
    FusedLocationProviderClient fusedLocationProviderClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Instanciamos las variables
        tv_lbladdress = findViewById(R.id.tv_lbladdress);

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
        Btn_Get_Location = findViewById(R.id.Btn_Get_Location);
        Btn_Get_Location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Aquí colocas el código que deseas que se ejecute cuando se hace clic en el botón
                // Por ejemplo, aquí llamamos a la función updateUIValues y pasamos la ubicación
                updateUIValues(location);

                // Resto de tu código updateUIValues
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                double altitude = location.getAltitude();
                double time = location.getTime();
                Date date = new Date((long) time);  // Convertir el valor de tiempo a una instancia de Date
                String formattedDate = date.toString();

                // Concatenar la latitud y longitud en una cadena de texto
                String latLngAltText = "Lat: " + latitude + ", Lon: " + longitude + ", Alt: " + altitude + ", Time Stamp: " + formattedDate;

                // Actualizar el cuadro de texto con la cadena resultante
                tv_lbladdress.setText(latLngAltText);
            }
        });
    }

};