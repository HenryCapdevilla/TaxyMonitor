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
import java.util.Date;



public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_FINE_LOCATION = 99;
    private TextView tv_lbladdress;
    private Button Btn_Get_Location;
    private Button tcpButton;
    private Button udpButton;
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
                Send_Gps_To_Ip();
            }
        });
    }

    private void Send_Gps_To_Ip() {

        tcpButton = findViewById(R.id.tcpButton);
        udpButton = findViewById(R.id.udpButton);
        Input_Port = findViewById(R.id.Input_Port);

        tcpButton.setOnClickListener(v -> {
            ipAddress = ipAddressEditText.getText().toString();
            Port = Input_Port.getText().toString();
            //Toast.makeText(MainActivity.this, "Botón TCP funciona", Toast.LENGTH_SHORT).show();
            sendCoordinatesUsingTCP(ipAddress, Port);
        });

        udpButton.setOnClickListener(v -> {
            ipAddress = ipAddressEditText.getText().toString();
            Port = Input_Port.getText().toString();
            //Toast.makeText(MainActivity.this, "Botón UDP funciona", Toast.LENGTH_SHORT).show();
            sendCoordinatesUsingUDP(ipAddress, Port);
        });
    }
    private void sendCoordinatesUsingTCP(final String ipAddress, String port) {
        new Thread(() -> {
            try {
                Socket socket = new Socket(ipAddress, Integer.parseInt(port));
                OutputStream outputStream = socket.getOutputStream();
                String message = "Cordenadas: " + tv_lbladdress.getText().toString();
                outputStream.write(message.getBytes());
                outputStream.flush();
                socket.close();
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Coordenadas enviadas por TCP", Toast.LENGTH_SHORT).show());

                } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void sendCoordinatesUsingUDP(final String ipAddress, String port) {
        new Thread(() -> {
            try {
                DatagramSocket socket = new DatagramSocket();
                byte[] data = ("Cordenadas" + tv_lbladdress.getText().toString()).getBytes();
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