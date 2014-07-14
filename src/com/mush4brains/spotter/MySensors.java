package com.mush4brains.spotter;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/* MySensors - Adapted from Derick Beckwith and Internet resources.
 * Constructor requires a context to the calling activity
 * The activity onResume() MUST call MySensors.register
 * The activity onStart() MUST call MySensors.unregister
 * Use getters to retrieve acceleration and attitude (roll, pitch, azimuth)
 * Use hasCompass() and hasAccelerometer() to return status of sensors
 * Chuck Bolin - July 10, 2015
 * 
 * Example code in Activity
 * 
 * MySensors mSensors; //data member
 * mSensors = new MySensors(this.getBaseContext()); //onCreate()
 * mSensors.register(); //onResume()
 * mSensors.unregister();//onPause()
 * 
 * use getters as required
 * 
 */
public class MySensors {
  
  //class members
  private SensorManager mSensorManager = null;  
  private Sensor sensorAccelerometer = null;
  private Sensor sensorMagneticField = null;  
  private Sensor sensorPressure = null;
  private Boolean mCompassSensorExists = false;
  private Boolean mAccSensorExists = false;
  private Boolean mPressureSensorExists = false;
  private Context mContext;
  private SensorEventListener mListener = null;
  
  private float[] valuesAccelerometer;
  private float[] valuesMagneticField;  
  private float[] filterAccelerometer;
  private float[] filterMagneticField;  
  
  private float[] matrixR;
  private float[] matrixI;
  private float[] matrixValues;
  
  private double mPitch = 0;
  private double mRoll = 0;
  private double mAzimuth = 0;
  private double mAX, mAY, mAZ; //acceleration values
  private float mPressureMillibars;
  
  private static String TAG = "MyCompass";
  
  //constructor
  public  MySensors(Context context){
    
    //initialize arrays
    valuesAccelerometer = new float[3];
    valuesMagneticField = new float[3];
    filterAccelerometer = new float[3];
    filterMagneticField = new float[3];
    
    matrixR = new float[9];
    matrixI = new float[9];
    matrixValues = new float[3];
    
    
    //set context and determine if sensors exist
    this.mContext = context;  
    PackageManager packageManager = mContext.getPackageManager();
    mCompassSensorExists = packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS);
    mAccSensorExists = packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER);
    mPressureSensorExists = packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_BAROMETER);
    
    //only process if sensors work
    if(mCompassSensorExists && mAccSensorExists){
      mSensorManager = (SensorManager)mContext.getSystemService(mContext.SENSOR_SERVICE);
      sensorMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);   
      sensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
      
      if(mPressureSensorExists)
        sensorPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
      
      //sensor listener
      mListener = new SensorEventListener(){

        @Override
        public void onSensorChanged(SensorEvent event) {
          if( event.sensor.getType() > 0){

              switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                  for (int i = 0; i < 3; i++) {
                    valuesAccelerometer[i] = event.values[i];
                  }
                  mAX = event.values[0];
                  mAY = event.values[1];
                  mAZ = event.values[2];
                  
                  break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                  for (int i = 0; i < 3; i++) {
                    valuesMagneticField[i] = event.values[i];   
                  }
                  break;
                case Sensor.TYPE_PRESSURE:
                    if (mPressureSensorExists)
                      mPressureMillibars = event.values[0];
                    else
                      mPressureMillibars = 0;
                  break;
              }

            //attempt at low pass filter
            //http://blog.thomnichols.org/2011/08/smoothing-sensor-data-with-a-low-pass-filter 
            filterMagneticField = lowPass ( valuesMagneticField, filterMagneticField );
            filterAccelerometer = lowPass ( valuesAccelerometer, filterAccelerometer );
              
            //perform matrix math
            boolean success = SensorManager.getRotationMatrix(matrixR, matrixI,
                filterAccelerometer, filterMagneticField);
//            boolean success = SensorManager.getRotationMatrix(matrixR, matrixI,
//                valuesAccelerometer, valuesMagneticField);
            //math operation successful
            if (success) {
              SensorManager.getOrientation(matrixR, matrixValues);
              mAzimuth = Math.toDegrees(matrixValues[0]);
              mPitch = Math.toDegrees(matrixValues[1]);
              mRoll = Math.toDegrees(matrixValues[2]);
              
              //myCompass.update(matrixValues[0]);
            }//if(success          
          }//if(event.sensor
        }//onSensorChanged

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
          
        }
        
    };//listener
  }//if(compass exists
  }//constructor
  
  
  protected float[] lowPass( float[] input, float[] output ) {
    final float ALPHA = 0.05f;
    if ( output == null ) return input;
     
    for ( int i=0; i<input.length; i++ ) {
        output[i] = output[i] + ALPHA * (input[i] - output[i]);
    }
    return output;
  }
  
  //getters
  public double getAzimuth(){
    if(mCompassSensorExists){
      if(mAzimuth < 0)
        mAzimuth += 360.0;
      
      return mAzimuth;      
    }
    else
      return 0;
  }
  
  public double getRoll(){
    if(mCompassSensorExists)
      return mRoll;
    else
      return 0;
  }
  
  public double getPitch(){
    if(mCompassSensorExists)
      return mPitch;
    else
      return 0;
  }
  
  public double getAccelerationX(){
    if(mAccSensorExists)
      return mAX;
    else
      return 0;
  }
  
  public double getAccelerationY(){
    if(mAccSensorExists)
      return mAY;
    else
      return 0;
  }

  public double getAccelerationZ(){
    if(mAccSensorExists)
      return mAZ;
    else
      return 0;
  }
  
  public float getPressure(){
    return mPressureMillibars;
  }
  
  public boolean hasCompass(){
    return mCompassSensorExists;
  }
  
  public boolean hasAccelerometer(){
    return mAccSensorExists;
  }
  
  public boolean hasPressure(){
    return mPressureSensorExists;
  }

  //registers sensors - CALLED from activity onResume()
  public void register(){

    if(mCompassSensorExists){
      try{
        if(mListener != null){
          mSensorManager.registerListener(mListener, sensorMagneticField, SensorManager.SENSOR_DELAY_NORMAL);
        }
      }catch(Exception e){
        Log.d(TAG, "Register Compass Failed: " + e.getMessage());
      }
    }

    if(mAccSensorExists){
      try{
        if(mListener != null){
          mSensorManager.registerListener(mListener, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
      }catch(Exception e){
        Log.d(TAG, "Register Accelerometer Failed: " + e.getMessage());
      }
    }
  
    if(mPressureSensorExists){
      try{
        if(mListener != null){
          mSensorManager.registerListener(mListener, sensorPressure, SensorManager.SENSOR_DELAY_NORMAL);
        }
      }catch(Exception e){
        Log.d(TAG, "Register Pressure Failed: " + e.getMessage());
      }
    }
  
  }
  
  //unregisters sensors - CALLED from activity onStart()
  public void unregister(){
    if(mCompassSensorExists){
      try{
        mSensorManager.unregisterListener(mListener);
      }catch(Exception e){
        Log.d(TAG, "Unregister Failed: " + e.getMessage());
      }
    }
  }

}