package com.example.gps_services;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;



public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_FINE_LOCATION = 99;
    private static final int PERMISSION_BACKGROUND_LOCATION = 100;
    private TextView tv_lbladdress;
    private Button Btn_Get_Location;
    private Button udpButton;
    private Button StopButton;
    private EditText ipAddressEditText;
    private EditText Input_Port;
    private String ipAddress;
    private String Port;

    //Google Api for location services.
    FusedLocationProviderClient fusedLocationProviderClient;

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Instanciamos las variables
        tv_lbladdress = findViewById(R.id.tv_lbladdress);
        ipAddressEditText = findViewById(R.id.ipAddressEditText);
        updateGPS();

        // Actualiza los permisos al iniciar la actividad
        updatePermissions();
    }

    private void updatePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permiso no concedido, solicitarlo
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                        PERMISSION_BACKGROUND_LOCATION);
            } else {
                // Permiso ya concedido o versión de Android anterior a Q
                updateGPS();
            }
        } else {
            // En versiones anteriores a Android Q, no es necesario el permiso de fondo
            updateGPS();
        }
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
            case PERMISSION_BACKGROUND_LOCATION: // Nuevo código
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    updateGPS();
                } else {
                    Toast.makeText(this, "Esta aplicación requiere de permiso de segundo plano, activalo en ajustes ", Toast.LENGTH_SHORT).show();
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
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
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

                // Crear un formato para la fecha y la hora
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

                String formattedDate = dateFormat.format(date); // Obtener la fecha formateada
                String formattedTime = timeFormat.format(date); // Obtener la hora formateada

                // Ahora tienes la fecha y la hora en las variables formattedDate y formattedTime


                // Concatenar la latitud y longitud en una cadena de texto
                String latLngAltText = latitude +", " + longitude + ", " + formattedDate + ", " +formattedTime;

                // Actualizar el cuadro de texto con la cadena resultante
                tv_lbladdress.setText(latLngAltText);
                Send_Gps_To_Ip();
            }
        });
    }

    private void Send_Gps_To_Ip() {
        udpButton = findViewById(R.id.udpButton);
        Input_Port = findViewById(R.id.Input_Port);

        udpButton.setOnClickListener(v -> {
            ipAddress = ipAddressEditText.getText().toString();
            Port = Input_Port.getText().toString();
            //Toast.makeText(MainActivity.this, "Botón UDP funciona", Toast.LENGTH_SHORT).show();
            sendCoordinatesUsingUDP(ipAddress, Port);
        });
    }

    private void sendCoordinatesUsingUDP(final String ipAddress, String port) {
        new Thread(() -> {
            try {
                DatagramSocket socket = new DatagramSocket();
                byte[] data = (tv_lbladdress.getText().toString()).getBytes();
                DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(ipAddress), Integer.parseInt(port));
                socket.send(packet);
                socket.close();
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Coordenadas enviadas por UDP", Toast.LENGTH_SHORT).show());
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }).start();
    }
};