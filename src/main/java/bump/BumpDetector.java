package bump;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;


public class BumpDetector extends Observable implements SensorEventListener, IBumpDetector {
    private final int WAIT_BETWEEN_BUMPS = 1000;
    private double adjustment_apple_to_android = 0.1;
    private final String _label = "BDETECT";

    private static Object _lockobj = new Object();

    private final Sensor _linearAccelerometer;
    private SensorManager _sensorManager;

    private boolean _recording = false;

    private Sample _currentSample = new Sample(0.0, 0.0, 0.0);
    private Sample _previousSample = new Sample(0.0, 0.0, 0.0);

    private ArrayList<Sample> _samples = new ArrayList<Sample>();

    public BumpDetector(SensorManager sensorManager) {
        this._sensorManager = sensorManager;
        _linearAccelerometer = _sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        startMonitoring();
    }

    public void startMonitoring() {
        Log.d(_label, "register");
        _sensorManager.registerListener(this, _linearAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void stopMonitoring() {
        Log.d(_label, "unregister");
        _sensorManager.unregisterListener(this, _linearAccelerometer);
    }

    @Override
    public void addBumpObserver(Observer observer) {
        addObserver(observer);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        synchronized (_lockobj) {
            Sample newData = new Sample(event.values, adjustment_apple_to_android);

            new KalmanFilter().applyTo(newData, _currentSample);

            Delta delta = _currentSample.delta(_previousSample);

            if (delta.exceedsThreshold() && !_recording) {
                _samples.add(_currentSample.clone());
                _recording = true;
                Log.d(_label, "Started recording.");
            } else if (_recording) {
                if (_samples.size() < 31) {
                    _samples.add(_currentSample.clone());
                } else {
                    _recording = false;
                    checkForBump();
                }
            }

            _previousSample = _currentSample.clone();
        }
    }

    private void checkForBump() {
        stopMonitoring();

        if (isBump()) {
            Log.d(_label, "Bump detected!");

            fireBumpEvent();

            new Handler().postDelayed(new Runnable() {
                public void run() {
                    startMonitoring();
                }
            }, WAIT_BETWEEN_BUMPS);

        } else {
            Log.d(_label, "False alarm.");
            startMonitoring();
        }

        _samples.clear();
    }

    private boolean isBump() {
        Log.d(_label, "Checking for Bump...");

        int minPeaks = _samples.size() - 10;
        int maxPeaks = _samples.size() - 2;

        return Peaks.readFrom(_samples).between(minPeaks, maxPeaks);
    }

    private void fireBumpEvent() {
        setChanged();
        notifyObservers();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
