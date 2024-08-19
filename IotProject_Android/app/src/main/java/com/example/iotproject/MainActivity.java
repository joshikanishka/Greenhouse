package com.example.iotproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {
    TextView
            soilHumidityUnits,
            temperatureUnits,
            humidityUnits;
    ProgressBar humidityGauge,
            temperatureGauge,
            soilHumidityGauge;
    Button exitButton,
            autoModeButton,
            applyButton;
    Switch
            ventilationSwitch,
            waterPumpSwitch;

    DatabaseReference database;

    Integer humidity,
            temperature,
            soilHumidity;

    boolean autoMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        soilHumidityGauge = findViewById(R.id.soilHumidityGauge);
        temperatureGauge = findViewById(R.id.temperatureGauge);
        humidityGauge = findViewById(R.id.humidityGauge);

        soilHumidityUnits = findViewById(R.id.soilHumidityUnits);
        temperatureUnits = findViewById(R.id.temperatureUnits);
        humidityUnits = findViewById(R.id.humidityUnits);

        ventilationSwitch = findViewById(R.id.ventilationSwitch);
        waterPumpSwitch = findViewById(R.id.waterPumpSwitch);

        exitButton = findViewById(R.id.exitButton);
        autoModeButton = findViewById(R.id.autoModeButton);
        applyButton = findViewById(R.id.applyButton);

        ventilationSwitch = findViewById(R.id.ventilationSwitch);
        waterPumpSwitch = findViewById(R.id.waterPumpSwitch);

        soilHumidityGauge.setMax(100);
        temperatureGauge.setMax(60);
        humidityGauge.setMax(100);

        database = FirebaseDatabase.getInstance().getReference();

        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                database.child("Control/AutoMode").setValue(false);
                database.child("Control/Ventilation").setValue(ventilationSwitch.isChecked());
                database.child("Control/WaterPump").setValue(waterPumpSwitch.isChecked());

                Toast.makeText(MainActivity.this, "Manual Mode Enabled!!!", Toast.LENGTH_SHORT).show();

            }
        });

        autoModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if (soilHumidity < 85 && soilHumidity >= 0) {
//                    waterPumpSwitch.setChecked(true);
//                } else {
//                    waterPumpSwitch.setChecked(false);
//                }
//
//                if (temperature > 25 && temperature <= 100) {
//                    ventilationSwitch.setChecked(true);
//                } else {
//                    ventilationSwitch.setChecked(false);
//                }

                database.child("Control/AutoMode").setValue(true);
//                database.child("Control/Ventilation").setValue(ventilationSwitch.isChecked());
//                database.child("Control/WaterPump").setValue(waterPumpSwitch.isChecked());

                Toast.makeText(MainActivity.this, "Auto Mode Enabled!!!", Toast.LENGTH_SHORT).show();
            }
        });
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ProgressBar soilHumidityGauge = MainActivity.this.soilHumidityGauge;
                ProgressBar temperatureGauge = MainActivity.this.temperatureGauge;
                ProgressBar humidityGauge = MainActivity.this.humidityGauge;

                TextView soilHumidityUnits = MainActivity.this.soilHumidityUnits;
                TextView temperatureUnits = MainActivity.this.temperatureUnits;
                TextView humidityUnits = MainActivity.this.humidityUnits;

                Switch ventilationSwitch = MainActivity.this.ventilationSwitch;
                Switch waterPumpSwitch = MainActivity.this.waterPumpSwitch;

                try {

                    soilHumidity = dataSnapshot.child("Data/SoilHumidity").getValue(Integer.class);
                    temperature = dataSnapshot.child("Data/Temperature").getValue(Integer.class);
                    humidity = dataSnapshot.child("Data/Humidity").getValue(Integer.class);

                    soilHumidityGauge.setProgress(soilHumidity);
                    temperatureGauge.setProgress(temperature);
                    humidityGauge.setProgress(humidity);
                    if (!(Integer.parseInt(soilHumidity.toString()) > 100 || Integer.parseInt(soilHumidity.toString()) < 0)) {
                        soilHumidityUnits.setText(soilHumidity.toString() + "%");
                    } else {
                        soilHumidityUnits.setText("--/--");
                    }

                    if (!(Integer.parseInt(temperature.toString()) > 60 || Integer.parseInt(temperature.toString()) < 0)) {
                        temperatureUnits.setText(temperature.toString() + "⁰C");
                    } else {
                        temperatureUnits.setText("--/--");
                    }
                    if (!(Integer.parseInt(humidity.toString()) > 100 || Integer.parseInt(humidity.toString()) < 0)) {
                        humidityUnits.setText(humidity.toString() + "%");
                    } else {
                        humidityUnits.setText("--/--");
                    }

                    waterPumpSwitch.setChecked(dataSnapshot.child("Control/WaterPump").getValue(Boolean.class));
                    ventilationSwitch.setChecked(dataSnapshot.child("Control/Ventilation").getValue(Boolean.class));


                } catch (Exception e) {
                    System.out.println("Error");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                soilHumidityGauge.setProgress(0);
                temperatureGauge.setProgress(0);
                humidityGauge.setProgress(0);

                soilHumidityUnits.setText("--/--");
                temperatureUnits.setText("--/--");
                humidityUnits.setText("--/--");

                waterPumpSwitch.setChecked(false);
                ventilationSwitch.setChecked(false);
            }
        });


    }


}
