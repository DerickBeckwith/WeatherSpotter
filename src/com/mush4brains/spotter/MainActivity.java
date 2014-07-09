package com.mush4brains.spotter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity implements SensorEventListener{
  public static String TAG = "MainActivity";
  public static String FILENAME = "SpotterLog.txt";
  
  TextView mTextHeader = null;
  TextView mTextDate = null;
  TextView mTextTime = null;
  TextView mTextLat = null;
  TextView mTextLong = null;
  TextView mTextPressure = null;
  TextView mTextCompass = null;
  ListView mListViewFileData = null;
   
  ArrayAdapter<String> mAdapter;
  private SensorManager mSensorManager;  
  private Sensor mPressureSensor;  
  private Sensor mCompassSensor;
  private Boolean mPressureSensorExists = false;
  private Boolean mCompassSensorExists = false;
  private float mPressureMillibars;
  private float mAzimuth;
  private String mDate;
  private String mTime;
  private Double mLatitude;
  private Double mLongitude;
  
  //********************************************************************** Activity Lifecycle Methods
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    
    //assign all views
    mTextHeader = (TextView)findViewById(R.id.textHeader);
    mTextDate = (TextView)findViewById(R.id.textViewDate);
    mTextTime = (TextView)findViewById(R.id.textViewTime);
    mTextLat = (TextView)findViewById(R.id.textViewLatitude);
    mTextLong = (TextView)findViewById(R.id.textViewLongitude);
    mTextPressure = (TextView)findViewById(R.id.textViewPressure);
    mTextCompass = (TextView)findViewById(R.id.textViewCompass);
    //mListViewFileData = (ListView)findViewById(R.id.listViewFile);
    
    //verify existence of humidity sensor
    PackageManager packageManager = this.getPackageManager();
    mPressureSensorExists = packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_BAROMETER);
    mCompassSensorExists = packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS);
    
    //setup sensor manager
    mSensorManager = (SensorManager) getSystemService(this.SENSOR_SERVICE);
    mPressureSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
    mCompassSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
   
    //showData(findViewById(R.id.textHeader));
    //updateData(findViewById(R.id.textHeader));
    updateTextViews();
    //updateListView();
  }
  
  @Override
  protected void onResume() {
    // Register a listener for the sensor.
    super.onResume();
    mSensorManager.registerListener(this, mPressureSensor, SensorManager.SENSOR_DELAY_NORMAL);
    mSensorManager.registerListener(this, mCompassSensor, SensorManager.SENSOR_DELAY_NORMAL);
  }

  @Override
  protected void onPause() {
    // Be sure to unregister the sensor when the activity pauses.
    super.onPause();
    mSensorManager.unregisterListener(this);
  }    
  
  //********************************************************************** onClick()
  //Update onClick()
  //************************  
  public void updateData(View view){
    updateTextViews();
    
    //write to log cat
    String dataString = mDate + "\t" + mTime + "\t" + mLatitude + "\t" + mLongitude + "\t" +
        Float.toString(mPressureMillibars) + "\t" + Float.toString(mAzimuth);       
    Log.d(TAG,dataString);    
  }  

  //Save onClick()
  //************************
  @SuppressWarnings("deprecation")
  public void saveData(View view){

    String dataString = mDate + "\t" + mTime + "\t" + mLatitude + "\t" + mLongitude + "\t" +
        Float.toString(mPressureMillibars) + "\t" + Float.toString(mAzimuth) + "\n";   
    
        try{
          OutputStreamWriter osw = new OutputStreamWriter(openFileOutput(FILENAME, Context.MODE_APPEND));
          osw.write(dataString);
          osw.close();
          //updateListView();
        }catch(Exception e){
          Log.d(TAG,"Error Save: " + e.getMessage());
        }    
  }
  
  //History onClick()
  public void gotoHistory(View view){
    Intent intent = new Intent(getApplication(),HistoryActivity.class);
    startActivity(intent);    
  }
 
  //Delete file onClick
  public void deleteFile(View view){
    //this.deleteFile(FILENAME);
    File dir = getFilesDir(); 
    File file = new File(dir, FILENAME);
    boolean deleted = file.delete();
    Log.d(TAG,"Dir: " + dir.toString());
    Log.d(TAG,"File: " + file.toString());
    Log.d(TAG, Boolean.toString(deleted));
    
    //clears adapter and list view
    List<String> items = new LinkedList<String>();    
    String[] values = items.toArray(new String[items.size()]);    
    mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, values);
    mListViewFileData.setAdapter(mAdapter);    
  }  
  
  //cloud onClick
  public void processCloud(View v){
    Toast.makeText(getApplicationContext(), "Cloud clicked", Toast.LENGTH_SHORT).show();
  }
  //cloud onClick
  public void processFunnel(View v){
    Toast.makeText(getApplicationContext(), "Funnel clicked", Toast.LENGTH_SHORT).show();
  }
  //cloud onClick
  public void processTornado(View v){
    Toast.makeText(getApplicationContext(), "Tornado clicked", Toast.LENGTH_SHORT).show();
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
      if (mPressureSensorExists)
        mPressureMillibars = event.values[0];
      else
        mPressureMillibars = 0;
    }
    
    if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
      if (mCompassSensorExists)
        mAzimuth = Math.round(event.values[0]);
      else
        mAzimuth = 0;
      //Log.d(TAG, Float.toString(mAzimuth));
    }
    
    updateTextViews();    
  }

//**************************************************************************** Position
  private Location getLocation(){
    Location location = null;

    // Acquire a reference to the system Location Manager
    final LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

    // Define a listener that responds to location updates
    LocationListener locationListener = new LocationListener() {

      public void onLocationChanged(Location location) {
        // Called when a new location is found by the network location provider.    
        locationManager.removeUpdates(this);  
      }
  
      // We must define these to implement the interface, 
      // but we don't do anything when they're triggered.
      public void onStatusChanged(String provider, int status, Bundle extras) {    
      }
    
      public void onProviderEnabled(String provider) {    
      }
    
      public void onProviderDisabled(String provider) {    
      }
    };

    // Register the listener with the Location Manager to receive location updates
    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

      //Log.d(TAG, "locationManager.isProviderEnabled = true/gps");    
      locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
      location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

      if (location != null) {
        return location;    
      } else {    
        Toast.makeText(getApplicationContext(),"GPS has yet to calculate location.", Toast.LENGTH_LONG).show();
      }
    } else {
        Toast.makeText(getApplicationContext(), "GPS is not enabled.", Toast.LENGTH_LONG).show();    
    }
    return location;
  }

  //********************************************************************** Utility methods (helps)
  private void updateTextViews(){
    mDate = getDate();
    mTime = getTime();
    mTextDate.setText("Date: " + getDate());
    mTextTime.setText("Time: " + getTime());
    
    if(mPressureSensorExists){
      mTextPressure.setText("Pressure: " + Float.toString(mPressureMillibars) + " mb");
    }else{
      mTextPressure.setText("No pressure sensor");
    }
      
    Location location = getLocation();
    if(location != null){
      //setLocation(location);
      double latitude = location.getLatitude();
      double longitude = location.getLongitude();
      mLatitude = latitude;
      mLongitude = longitude;
      
      mTextLat.setText("Latitude: " + Double.toString(latitude));
      mTextLong.setText("Longitude: " + Double.toString(longitude));      
    }else{
      mTextLat.setText("Latitude unavailable");
      mTextLong.setText("Latitude unavailable");
    }
    
    if(mCompassSensorExists){
      mTextCompass.setText("Azimuth: " + Float.toString(mAzimuth));
    }else{
      mTextCompass.setText("No compass");
    }    
  }    


	/*
	 * Handles the Show Compass button's onClick event.
	 */
	public void showCompass(View view) {
		Intent compassIntent = new Intent(this, CompassActivity.class);
		Log.d(TAG,"showCompass");
		
		startActivity(compassIntent);
	}

	
}
