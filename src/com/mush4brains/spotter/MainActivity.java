package com.mush4brains.spotter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

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


public class MainActivity extends Activity{
  public static String TAG = "MainActivity";
  public static String FILENAME = "SpotterLog.txt";
  
  MySensors mSensors;
  
  TextView mTextHeader = null;
  TextView mTextDate = null;
  TextView mTextTime = null;
  TextView mTextLat = null;
  TextView mTextLong = null;
  TextView mTextPressure = null;
  TextView mTextCompass = null;

  private float mPressureMillibars;
  private float mAzimuth;
  private String mDate;
  private String mTime;
  private Double mLatitude;
  private Double mLongitude;
  
  int mCount = 0;
  
  MyTimerTask myTask;// = new MyTimerTask();
  Timer myTimer = new Timer();   
  
  //********************************************************************** Activity Lifecycle Methods
  //onClick()
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    
    setTitle("Weather Spotter Reporter - v0.1");
    
    //assign all views
    mTextHeader = (TextView)findViewById(R.id.textHeader);
    mTextDate = (TextView)findViewById(R.id.textViewDate);
    mTextTime = (TextView)findViewById(R.id.textViewTime);
    mTextLat = (TextView)findViewById(R.id.textViewLatitude);
    mTextLong = (TextView)findViewById(R.id.textViewLongitude);
    mTextPressure = (TextView)findViewById(R.id.textViewPressure);
    mTextCompass = (TextView)findViewById(R.id.textViewCompass);
    
    mSensors = new MySensors(this.getBaseContext()); 
    updateTextViews();

  }
  
  //onResume()
  @Override
  protected void onResume() {
    // Register a listener for the sensor.
    super.onResume();
    mSensors.register();
  }

  //onPause()
  @Override
  protected void onPause() {
    super.onPause();
    mSensors.unregister();
  }    
  
  //********************************************************************** onClick() button events
  //Update onClick()
  //************************  
  public void updateData(View view){
    updateTextViews();
    
    //write to log cat
    String dataString = mDate + "\t" + mTime + "\t" + mLatitude + "\t" + mLongitude + "\t" +
        Float.toString(mSensors.getPressure()) + "\t" + Double.toString(mSensors.getAzimuth());       
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
          
          Toast.makeText(getApplicationContext(), "Data point saved!", Toast.LENGTH_SHORT).show();
        }catch(Exception e){
          Log.d(TAG,"Error Save: " + e.getMessage());
        }    
  }
  
  //History onClick()
  public void gotoHistory(View view){
    Intent intent = new Intent(getApplication(),HistoryActivity.class);
    startActivity(intent);    
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
    
    if(mSensors.hasPressure()){      
      try{
        DecimalFormat df = new DecimalFormat("####.00");
        mTextPressure.setText("Pressure: " + df.format(mSensors.getPressure()) );
      }catch(Exception e){        
      }
    }else{
      mTextPressure.setText("No pressure sensor");
    }
      
    Location location = getLocation();
    if(location != null){
      try{
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        mLatitude = latitude;
        mLongitude = longitude;
        DecimalFormat df = new DecimalFormat("###.0000");
        mTextLat.setText("Latitude: " + df.format(latitude));
        mTextLong.setText("Longitude: " + df.format(longitude));
      }catch(Exception e){      
      }
      
    }else{
      mTextLat.setText("Latitude unavailable");
      mTextLong.setText("Latitude unavailable");
    }
    
    if(mSensors.hasCompass()){
      DecimalFormat df = new DecimalFormat("###.0");
      mTextCompass.setText("Azimuth: " + df.format(mSensors.getAzimuth()));
    }else{
      mTextCompass.setText("No compass");
    }    
  }    

	/*
	 * Handles the Show Compass button's onClick event.
	 */
	public void showCompass(View view) {
		Intent compassIntent = new Intent(this, CompassActivity.class);
		startActivity(compassIntent);
	}

	
	//******************************************************************* SCHEDULER - TIMED
	 public void onStartButtonClick(View view){
	    try{
	      myTask = new MyTimerTask();
	      myTimer.schedule(myTask, 1000, 2000);
	    }catch(Exception e){
	      Log.d(TAG, e.getMessage());
	    }finally{
	      
	    }    
	  }

	  public void onPauseButtonClick(View view){	    
	    try{
	      myTask.cancel();
	    }catch(Exception e){
	      Log.d(TAG, e.getMessage());
	    }finally{

	    }
	  }  

	  public void onResetButtonClick(View view){
	    myTask.cancel();
	  }

	  //contains run that is called every time interval
	  class MyTimerTask extends TimerTask{
	    public void run(){
	      runOnUiThread(new Runnable(){
	        public void run(){
	          updateTextViews();
	        }
	      });

	    }
	  }		
}
