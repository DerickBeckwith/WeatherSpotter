package com.mush4brains.spotter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements SensorEventListener{
  public static String TAG = "MainActivity";
  
  TextView mTextDate = null;
  TextView mTextTime = null;
  TextView mTextLat = null;
  TextView mTextLong = null;
  TextView mTextPressure = null;
  TextView mTextCompass = null;
  
  private SensorManager mSensorManager;
  
  private Sensor mPressureSensor;  
  Boolean mPressureSensorExists = false;
  private float mPressureMillibars;
  
  //********************************************************************** Activity Lifecycle Methods
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    
    //assign all text views
    mTextDate = (TextView)findViewById(R.id.textViewDate);
    mTextTime = (TextView)findViewById(R.id.textViewTime);
    mTextLat = (TextView)findViewById(R.id.textViewLatitude);
    mTextLong = (TextView)findViewById(R.id.textViewLongitude);
    mTextPressure = (TextView)findViewById(R.id.textViewPressure);
    mTextCompass = (TextView)findViewById(R.id.textViewCompass);
    
    //verify existence of humidity sensor
    PackageManager packageManager = this.getPackageManager();
    mPressureSensorExists = packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_BAROMETER);
    
    //setup sensor manager
    mSensorManager = (SensorManager) getSystemService(this.SENSOR_SERVICE);
    mPressureSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
  }
  
  @Override
  protected void onResume() {
    // Register a listener for the sensor.
    super.onResume();
    mSensorManager.registerListener(this, mPressureSensor, SensorManager.SENSOR_DELAY_NORMAL);
  }

  @Override
  protected void onPause() {
    // Be sure to unregister the sensor when the activity pauses.
    super.onPause();
    mSensorManager.unregisterListener(this);
  }  
  
  
  //********************************************************************** onClick()
  //Update onClick()
  public void updateData(View view){
    mTextDate.setText(getDate());
    mTextTime.setText(getTime());
    
    if(mPressureSensorExists){
      mTextPressure.setText(Float.toString(mPressureMillibars));
    }else{
      mTextPressure.setText("No pressure sensor");
    }
      

  }
  
  //Save onClick()
  public void saveData(View view){
    Toast.makeText(getApplicationContext(), "Saved",Toast.LENGTH_SHORT).show();
  }
  
  //ShowAll onClick()
  public void showData(View view){
    Toast.makeText(getApplicationContext(), "Shown",Toast.LENGTH_SHORT).show();
  }
  
  //*********************************************************************** Date and Time Getters
  //returns date
  private String getDate(){
    String text = null;
    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy",Locale.US);
    text = (dateFormat.format(new Date())).toString();    
    return text;    
  }
  
  //returns time
  private String getTime(){
    String text = null;
    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss",Locale.US);
    text = (dateFormat.format(new Date())).toString();
    return text;
  }
  
  //**************************************************************************** Sensor listener methods
  @Override
  public final void onAccuracyChanged(Sensor sensor, int accuracy) {
    // Do something here if sensor accuracy changes.    
  }  
  
  @Override
  public final void onSensorChanged(SensorEvent event) {
  
    Sensor sensor = event.sensor;

    if (sensor.getType() == Sensor.TYPE_PRESSURE){
      mPressureMillibars = event.values[0];
    }
  }

}
