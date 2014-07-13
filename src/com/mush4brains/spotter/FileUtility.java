package com.mush4brains.spotter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.media.MediaScannerConnection;
import android.net.Uri;

//Used for writing string contents to a text file on the SD card
public class FileUtility {
  private String TAG = "FileUtility.java";
  private Context mContext;
  
  //constructor
  public FileUtility(Context context){
    this.mContext = context;    
  }

  /* Checks if external storage is available for read and write */
  public boolean isExternalStorageWritable() {
      String state = Environment.getExternalStorageState();
      if (Environment.MEDIA_MOUNTED.equals(state)) {
          return true;
      }
      return false;
  }
  
  /* Checks if external storage is available to at least read */
  public boolean isExternalStorageReadable() {
      String state = Environment.getExternalStorageState();
      if (Environment.MEDIA_MOUNTED.equals(state) ||
          Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
          return true;
      }     
      return false;
  }    

  //creates folders on SD card if not already existent
  //path e.g. "/WeatherSpotter/Files/History"
  public void createAppExternalFolder(String path){
    //Log.d(TAG,"extFolder: " + path);
    addFileToExternalFolder(path, "dummy.txt", " ");
    
//    MediaScannerConnection.scanFile(mContext, new String[]{Environment.getExternalStorageDirectory().toString() +
//        path + "/dummy.txt"}, null,null);
  }
  
 //Function writes string data to a file and compensates for bug fix 
 //path = "/WeatherSpotter/Files/Reports"
 //filename = "SpotterLog.txt
 //data = [contents of file to write] in a String
 //******************************************************************************
  public void addFileToExternalFolder(String path, String filename, String data){    

    //can't write...bail out
    if (!isExternalStorageWritable())
      return;
    
    //Step 1 - Create folder
    File reportDir = new File(
        Environment.getExternalStorageDirectory(),
        path);
    
    // Create the storage directory if it does not exist
    if (!reportDir.exists()) {
      if (!reportDir.mkdirs()) {
        Log.d(TAG, "failed to create directory");        
      }
      else{
        Log.d(TAG,"Success");
      }
    }          
    
    //Step 2 - Write file, write file contents if file exists
    if (reportDir.exists()){
      try {
        File outputFile = new File (reportDir, filename);
        
        if(!outputFile.exists()){
          FileOutputStream out;
          out = new FileOutputStream(outputFile);
          out.write(data.getBytes());
          out.flush();
          out.close();
        }
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (IOException e){
        e.printStackTrace();
      }
    }          
    
    
    //Step 3 - BUG fix
    MediaScannerConnection.scanFile(mContext, new String[]{Environment.getExternalStorageDirectory().toString() +
        path + "/" + filename}, null, null);


  }  
}
