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

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

//Entry point for app
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
  ImageView mImageView = null;
  Bitmap mThumbnail = null;
  
  
  private String mDate;
  private String mTime;
  private Double mLatitude;
  private Double mLongitude;
  final int CAMERA_PIC_REQUEST = 1337;  
  
  MyTimerTask myTask;// = new MyTimerTask();
  Timer myTimer = new Timer();   
  MenuItem menuDataManual;
  
  //********************************************************************** Activity Life Cycle Methods
  //onCreate()
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    //create default folders if not already existent
    FileUtility fileUtility = new FileUtility(this.getBaseContext());
    fileUtility.createAppExternalFolder("/WeatherSpotter/Files/History");
    fileUtility.createAppExternalFolder("/WeatherSpotter/Files/Reports");
    
    //add title
    setTitle("Weather Spotter Reporter - v0.1");

    //adds actionbar
    ActionBar actionBar = this.getActionBar();
    actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#0000ff")));
    SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm");
    String dateString = formatter.format(new Date());
    actionBar.setSubtitle( dateString);
    
//    read grab shared preferences
//    SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
//    Editor editor = pref.edit();
//    long value = pref.getLong("lastsyncdate", 0);    
//    write pref
//    SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
//    Editor editor = pref.edit();
//    Date date = new Date(System.currentTimeMillis());
//    long value = date.getTime();
//    editor.putLong("lastsyncdate", value);
//    editor.commit();      
    
    
    //assign all views
    mTextHeader = (TextView)findViewById(R.id.textHeader);
    mTextDate = (TextView)findViewById(R.id.textViewDate);
    mTextTime = (TextView)findViewById(R.id.textViewTime);
    mTextLat = (TextView)findViewById(R.id.textViewLatitude);
    mTextLong = (TextView)findViewById(R.id.textViewLongitude);
    mTextPressure = (TextView)findViewById(R.id.textViewPressure);
    mTextCompass = (TextView)findViewById(R.id.textViewCompass);
    mImageView = (ImageView) findViewById(R.id.imageViewCamera);

    //setContentView(R.id.main);    
    Display display = getWindowManager().getDefaultDisplay();
    Point size = new Point();
    display.getSize(size);
    Log.d(TAG,"Size: " + Integer.toString(size.x) + ", " + Integer.toString(size.y));
    int width = size.x * 50/100;
    int height = size.y * 50/100;
    //width = 360;
    //height = 640;
    LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(width, height);
    mImageView.setLayoutParams(parms);
    mImageView.setX((size.x - width)/2);
    mImageView.setY(20);
    
    
   // mImageView.se
    
    mSensors = new MySensors(this.getBaseContext()); 

    updateTextViews();

  }
  
  //onResume()
  @Override
  protected void onResume() {
    super.onResume();
    if (mImageView != null)
      mImageView.setImageBitmap(mThumbnail);
    
    Log.d(TAG,"onResume");
  }

  //onPause()
  @Override
  protected void onPause() {
    Log.d(TAG,"onPause");
    super.onPause();
  }    
  
  @Override
  protected void onStart(){
    // Register a listener for the sensor.    
    super.onStart();
    mSensors.register();
    Log.d(TAG,"onStart");
    
  }
  
  @Override
  protected void onStop(){
    
    mSensors.unregister();
    Log.d(TAG,"onStop");
    super.onStop();
  }
  //************************************************************************ Options menu
  //displays option menu
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    MenuInflater menuInflater = getMenuInflater();
    menuInflater.inflate(R.menu.spotter_menu, menu);
    
    //set Manual interval as default
    menuDataManual = menu.findItem(R.id.menu_data_interval_manual);    
    menuDataManual.setChecked(true);
    return true;
  } 
    
  
  //manages option menu selection
  @Override
   public boolean onOptionsItemSelected(MenuItem item){
    
      //this affects only time intervals
      if(item.isChecked())
        item.setChecked(false);
      else
        item.setChecked(true);

      //responds to specific option choices
      switch(item.getItemId()){
      case R.id.menu_user_name:
        return true;
      
      case R.id.menu_nws_phonenumber:
         return true;
      
      case R.id.menu_nws_email:
         return true;
      
      case R.id.menu_data_interval_manual: 
          changeDataInterval(0);
          return true;
      case R.id.menu_data_interval_2sec:
          changeDataInterval(2000);
          return true;
      case R.id.menu_data_interval_5sec:
          changeDataInterval(5000);
          return true;
      case R.id.menu_data_interval_15sec:
          changeDataInterval(15000);
          return true;
      case R.id.menu_data_interval_30sec:
          changeDataInterval(30000);
          return true;
      case R.id.menu_data_interval_1min:
          changeDataInterval(60000);
          return true;
      case R.id.menu_data_interval_5min:
          changeDataInterval(300000);
          return true;
      case R.id.menu_data_interval_15min:
          changeDataInterval(900000);
          return true;
      default:
          return super.onOptionsItemSelected(item);
      } 
    
  }  
  
  //********************************************************************* onActivityResult
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data){
    
    if(resultCode == RESULT_OK){
      if (requestCode == 1337){
        mThumbnail = (Bitmap) data.getExtras().get("data");
        Log.d(TAG,"Here");
        if (mThumbnail != null)
          mImageView.setImageBitmap(mThumbnail);
        Log.d(TAG,"And here");
      }else{
        Log.d(TAG, "No image you big dummy");
      }
    }
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
  
  //Camera onClick()
  //********************************
  public void showCamera(View view){
    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
    startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);    
  }

  //Save onClick()
  //************************
  @SuppressWarnings("deprecation")
  public void saveData(View view){

    String dataString = mDate + "\t" + mTime + "\t" + mLatitude + "\t" + mLongitude + "\t" +
        Float.toString(mSensors.getPressure()) + "\t" + Double.toString(mSensors.getAzimuth()) + "\n";   
    
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

  
 // Handles the Show Compass button's onClick event.

  public void showCompass(View view) {
    Intent compassIntent = new Intent(this, CompassActivity.class);
    startActivity(compassIntent);
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

  //********************************************************************** Utility methods (helpers)
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
      CardinalPoints cp = new CardinalPoints();
      
      mTextCompass.setText("Azimuth: " + df.format(mSensors.getAzimuth()) + "  " + cp.getCardinalAbbreviation(mSensors.getAzimuth()));
    }else{
      mTextCompass.setText("No compass");
    }    
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

	  //called from option buttons
	  public void changeDataInterval(long interval){
      try{
        myTask.cancel();
      }catch(Exception e){
      }
      
      //an interval of time enters manual mode
      if (interval == 0)
        return;
      
      try{
        myTask = new MyTimerTask();
        myTimer.schedule(myTask, 1000, interval);
      }catch(Exception e){
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
