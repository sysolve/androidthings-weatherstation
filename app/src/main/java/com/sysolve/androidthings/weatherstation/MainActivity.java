package com.sysolve.androidthings.weatherstation;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.things.contrib.driver.bmx280.Bmx280SensorDriver;
import com.sysolve.androidthings.utils.BoardSpec;

import java.io.IOException;
import java.text.DecimalFormat;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private enum DisplayMode {
        TEMPERATURE,
        PRESSURE
    }
    private DisplayMode mDisplayMode = DisplayMode.TEMPERATURE;

    private SensorManager mSensorManager;
    private Bmx280SensorDriver mEnvironmentalSensorDriver;

    private float mLastTemperature;
    private float mLastPressure;

    private ImageView mImageView;
    private TextView temperatureDisplay;
    private TextView barometerDisplay;

    private static final float BAROMETER_RANGE_LOW = 965.f;
    private static final float BAROMETER_RANGE_HIGH = 1035.f;
    private static final float BAROMETER_RANGE_SUNNY = 1010.f;
    private static final float BAROMETER_RANGE_RAINY = 990.f;

    private static final int MSG_UPDATE_BAROMETER_UI = 1;
    private static final int MSG_UPDATE_TEMPERATURE = 2;
    private static final int MSG_UPDATE_BAROMETER = 3;

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.0");

    private final Handler mHandler = new Handler() {
        private int mBarometerImage = -1;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_BAROMETER_UI:
                    int img;
                    if (mLastPressure > BAROMETER_RANGE_SUNNY) {
                        img = R.drawable.ic_sunny;
                    } else if (mLastPressure < BAROMETER_RANGE_RAINY) {
                        img = R.drawable.ic_rainy;
                    } else {
                        img = R.drawable.ic_cloudy;
                    }
                    if (img != mBarometerImage) {
                        mImageView.setImageResource(img);
                        mBarometerImage = img;
                    }
                    break;
                case MSG_UPDATE_TEMPERATURE:
                    temperatureDisplay.setText(DECIMAL_FORMAT.format(mLastTemperature));
                    break;
                case MSG_UPDATE_BAROMETER:
                    barometerDisplay.setText(DECIMAL_FORMAT.format(mLastPressure*0.1));
                    break;
            }
        }
    };

    // Callback used when we register the BMP280 sensor driver with the system's SensorManager.
    private SensorManager.DynamicSensorCallback mDynamicSensorCallback
            = new SensorManager.DynamicSensorCallback() {
        @Override
        public void onDynamicSensorConnected(Sensor sensor) {
            if (sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                // Our sensor is connected. Start receiving temperature data.
                mSensorManager.registerListener(mTemperatureListener, sensor,
                        SensorManager.SENSOR_DELAY_NORMAL);
                //if (mPubsubPublisher != null) {
                //    mSensorManager.registerListener(mPubsubPublisher.getTemperatureListener(), sensor,
                //            SensorManager.SENSOR_DELAY_NORMAL);
                //}
            } else if (sensor.getType() == Sensor.TYPE_PRESSURE) {
                // Our sensor is connected. Start receiving pressure data.
                mSensorManager.registerListener(mPressureListener, sensor,
                        SensorManager.SENSOR_DELAY_NORMAL);
                //if (mPubsubPublisher != null) {
                //    mSensorManager.registerListener(mPubsubPublisher.getPressureListener(), sensor,
                //            SensorManager.SENSOR_DELAY_NORMAL);
                //}
            }
        }

        @Override
        public void onDynamicSensorDisconnected(Sensor sensor) {
            super.onDynamicSensorDisconnected(sensor);
        }
    };

    // Callback when SensorManager delivers temperature data.
    private SensorEventListener mTemperatureListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            mLastTemperature = event.values[0];
            Log.d(TAG, "温度值反馈: " + mLastTemperature+"℃");
            updateTemperatureDisplay(mLastTemperature);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.d(TAG, "accuracy changed: " + accuracy);
        }
    };

    // Callback when SensorManager delivers pressure data.
    private SensorEventListener mPressureListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            mLastPressure = event.values[0];
            Log.d(TAG, "气压值反馈: " + mLastPressure*0.1 +"kPa");
            updateBarometerDisplay(mLastPressure);
            updateBarometer(mLastPressure);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.d(TAG, "accuracy changed: " + accuracy);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mImageView = (ImageView) findViewById(R.id.imageView);

        temperatureDisplay = (TextView) findViewById(R.id.temperatureDisplay);
        barometerDisplay = (TextView) findViewById(R.id.barometerDisplay);

        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));

        try {
            mEnvironmentalSensorDriver = new Bmx280SensorDriver(BoardSpec.getI2cBus());

            mSensorManager.registerDynamicSensorCallback(mDynamicSensorCallback);
            mEnvironmentalSensorDriver.registerTemperatureSensor();
            mEnvironmentalSensorDriver.registerPressureSensor();
            Log.d(TAG, "Initialized I2C BMP280");
        } catch (IOException e) {
            throw new RuntimeException("Error initializing BMP280", e);
        }
    }

    private void updateBarometerDisplay(float pressure) {
        // Update UI.
        if (!mHandler.hasMessages(MSG_UPDATE_BAROMETER)) {
            mHandler.sendEmptyMessageDelayed(MSG_UPDATE_BAROMETER, 500);
        }
    }
    private void updateTemperatureDisplay(float pressure) {
        // Update UI.
        if (!mHandler.hasMessages(MSG_UPDATE_TEMPERATURE)) {
            mHandler.sendEmptyMessageDelayed(MSG_UPDATE_TEMPERATURE, 500);
        }
    }

    private void updateBarometer(float pressure) {
        // Update UI.
        if (!mHandler.hasMessages(MSG_UPDATE_BAROMETER_UI)) {
            mHandler.sendEmptyMessageDelayed(MSG_UPDATE_BAROMETER_UI, 500);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Clean up sensor registrations
        mSensorManager.unregisterListener(mTemperatureListener);
        mSensorManager.unregisterListener(mPressureListener);
        mSensorManager.unregisterDynamicSensorCallback(mDynamicSensorCallback);

        // Clean up peripheral.
        if (mEnvironmentalSensorDriver != null) {
            try {
                mEnvironmentalSensorDriver.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mEnvironmentalSensorDriver = null;
        }

        // clean up Cloud PubSub publisher.
        /*
        if (mPubsubPublisher != null) {

            mSensorManager.unregisterListener(mPubsubPublisher.getTemperatureListener());
            mSensorManager.unregisterListener(mPubsubPublisher.getPressureListener());
            mPubsubPublisher.close();
            mPubsubPublisher = null;
        }
        */
    }

}
