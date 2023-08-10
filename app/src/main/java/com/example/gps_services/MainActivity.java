package com.example.gps_services;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
    private TextView tv_lbladdress;

    private Button Btn_Get_Location;

    private Button Btn_send_msm;

    private EditText editTextPhone;

    //Location Request is config a file
    LocationRequest locationRequest;

    //Google Api for location services.
    FusedLocationProviderClient fusedLocationProviderClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Instanciamos las variables
        tv_lbladdress = findViewById(R.id.tv_lbladdress);

        //Colocamos la configuración de  LocationRequest
        locationRequest = new LocationRequest();

        //How often does the default location check accur?
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL);

        //How often does the location check accur when set to the most frequent update?
        locationRequest.setFastestInterval(1000 + FAST_UDPDATE_INTERVAL);

        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        updateGPS();
        Send_Msm();
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

    private void Send_Msm(){
        //Obtenemos el permiso del usuario
        Btn_send_msm = findViewById(R.id.Btn_send_msm);
        editTextPhone = findViewById(R.id.editTextPhone);

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS}, 1);
        }

        Btn_send_msm.setOnClickListener(view -> {

            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(editTextPhone.getText().toString(), null, tv_lbladdress.getText().toString(), null, null);

            Toast.makeText(MainActivity.this, "SMS ENVIADO", Toast.LENGTH_LONG).show();

        });

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

                // Concatenar la latitud y longitud en una cadena de texto
                String latLngAltText = "Lat: " + latitude + ", Lon: " + longitude + ", Alt: " + altitude + ", ToF: " + time;

                // Actualizar el cuadro de texto con la cadena resultante
                tv_lbladdress.setText(latLngAltText);
            }
        });
    }
};