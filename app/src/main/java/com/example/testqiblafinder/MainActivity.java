package com.example.testqiblafinder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;

    private Sensor temperatureSensor;
    private Sensor humiditySensor;

    private float[] accelerometerReading = new float[3];
    private float[] magnetometerReading = new float[3];

    private float temperatureValue = 0.0f;
    private float humidityValue = 0.0f;

    private double qiblaAngle = 0.0;

    private ImageView compassIcon;
    private TextView qiblaDirectionTextView;

    private TextView temperatureTextView;
    private TextView humidityTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        compassIcon = findViewById(R.id.compassIcon);
        qiblaDirectionTextView = findViewById(R.id.qiblaDirectionTextView);
        temperatureTextView = findViewById(R.id.temperatureTextView);
        humidityTextView = findViewById(R.id.humidityTextView);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // check if sensor is available in device
        temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        if (temperatureSensor == null) {
            temperatureTextView.setText("Temperature: N/A");
        }

        humiditySensor = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        if (humiditySensor == null) {
            humidityTextView.setText("Humidity: N/A");
        }

        if (accelerometer == null || magnetometer == null) {
            // Device does not have required sensors
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(
                this, accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL
        );
        sensorManager.registerListener(
                this, magnetometer,
                SensorManager.SENSOR_DELAY_NORMAL
        );

        if (temperatureSensor != null) {
            sensorManager.registerListener(this, temperatureSensor,SensorManager.SENSOR_DELAY_NORMAL );
        }

        if (humiditySensor != null) {
            sensorManager.registerListener(this, humiditySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing for now
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor == accelerometer) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.length);
        } else if (event.sensor == magnetometer) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.length);
        } else if (event.sensor == temperatureSensor) {
            temperatureValue = event.values[0];

            temperatureTextView.setText("Temperature: " + temperatureValue + "°C");
        } else if (event.sensor == humiditySensor) {
            humidityValue = event.values[0];
            humidityTextView.setText("Humidity: " + humidityValue + "%");
        }
        updateQiblaDirection();
    }

    private void updateQiblaDirection() {
        float[] rotationMatrix = new float[9];
        boolean success = SensorManager.getRotationMatrix(
                rotationMatrix,
                null,
                accelerometerReading,
                magnetometerReading
        );

        if (success) {
            float[] orientationValues = new float[3];
            SensorManager.getOrientation(rotationMatrix, orientationValues);

            float azimuth = (float) Math.toDegrees(orientationValues[0]);
            qiblaAngle = azimuth + getQiblaOffset();

            if (qiblaAngle < 0) {
                qiblaAngle += 360.0;
            }

            qiblaDirectionTextView.setText("Qibla Direction: " + (int) qiblaAngle + "°");

            // Rotate the compass icon based on the calculated angle
            compassIcon.setRotation(-azimuth);
        }
    }

    private double getQiblaOffset() {
        // The Qibla direction is approximately 2.5 degrees east of the true north.
        return 2.5;
    }
}