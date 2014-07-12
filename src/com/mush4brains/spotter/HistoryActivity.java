package com.mush4brains.spotter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

//Allows user to view and delete history
public class HistoryActivity extends Activity {
  public static String TAG = "HistoryActivity";
  public static String FILENAME = "SpotterLog.txt";

  ArrayAdapter<String> mAdapter;
  ListView mListViewHistory = null;
  Context mContext;

  //********************************************************************** Activity Life Cycle Methods  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_history);
  
    mContext = this;
    setTitle("Data Collection History");
    mListViewHistory = (ListView)findViewById(R.id.listViewHistory);
    updateListView();
  }
  

  
  //********************************************************************** onClick() button events
  //onclick for email button
  public void sendEmail(View view){
    
    //bail out if listview is empty
    if( mAdapter.getCount() < 1){
      Toast.makeText(getApplicationContext(), "No data to email", Toast.LENGTH_SHORT).show();
      return;
    }

    //extract listview
    StringBuilder stringBuilder = new StringBuilder();  
    for( int i = 0; i < mAdapter.getCount(); ++i){
      String msg = mAdapter.getItem(i);
      stringBuilder.append(msg + "\n");
    }
    
    //create email intent
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType("message/rfc822");
    intent.putExtra(Intent.EXTRA_EMAIL, "cbolin@sc.rr.com");
    intent.putExtra(Intent.EXTRA_SUBJECT, "Weather Spotter Report Data");
    intent.putExtra(Intent.EXTRA_TEXT, stringBuilder.toString());
    try{
      startActivity(Intent.createChooser(intent, "Send email..."));
    }catch(Exception e){
      
    }
  }
  
  //onclick for DELETE button
  public void deleteFile(View view){
    
    //generate alert box
    try{
      showDialog("Delete all History?");
      
    }catch(Exception e){
      Log.d(TAG, e.getMessage());
    }
      
  }

  //********************************************************************** Utility methods (helpers)  
 //Alert Dialog prompting user to delete history - called from deleteFile()  
  public void showDialog(final String alertMessage) throws Exception
  {
      AlertDialog.Builder builder = new AlertDialog.Builder(HistoryActivity.this);
      builder.setMessage(alertMessage);
      
      //YES - Delete all
      builder.setPositiveButton("Yes", new OnClickListener()
      {
          @Override
          public void onClick(DialogInterface dialog, int which) 
          {
             
             File dir = getFilesDir(); 
             File file = new File(dir, FILENAME);
             boolean deleted = file.delete();
             Log.d(TAG,"Dir: " + dir.toString());
             Log.d(TAG,"File: " + file.toString());
             Log.d(TAG, Boolean.toString(deleted));
  
             List<String> items = new LinkedList<String>();    
             String[] values = items.toArray(new String[items.size()]);  
             
             //NOTE requires context, therefore I created mContext and set at onCreate()
             mAdapter = new ArrayAdapter<String>(mContext , android.R.layout.simple_dropdown_item_1line, values);
             mListViewHistory.setAdapter(mAdapter);  
             dialog.dismiss();
          }
      });

      //NO - Do not delete
      builder.setNegativeButton("No", new OnClickListener() 
      {   
          @Override
          public void onClick(DialogInterface dialog, int which) 
          {
            dialog.dismiss();
          }
      });

      builder.show();
  }
  
  //loads historical data from FILENAME into list view
  public void updateListView(){
    try{
      List<String> items = new LinkedList<String>();
      InputStream inputStream = openFileInput(FILENAME);
      if (inputStream != null){
        InputStreamReader isr = new InputStreamReader(inputStream);
        BufferedReader br = new BufferedReader(isr);
        String receiveString = "";
        
        StringBuilder stringBuilder = new StringBuilder();
        while ((receiveString = br.readLine()) != null){
          int lineNumber = 0;
          String[] dataArray;
          String outputString = "";
          dataArray =receiveString.split("\t");
          if( dataArray.length >= 6){
            outputString = "Date: " + dataArray[0].toString() + "  Time: " + dataArray[1] + "\n" +
                           "Lat: " + dataArray[2].toString() + " Long: " + dataArray[3] + "\n" +
                           "Pressure: " + dataArray[4].toString() + " Az: " + dataArray[5];
            items.add(outputString);
          }
          
          stringBuilder.append(receiveString  + "\n");
        }
        inputStream.close();
        String[] values = items.toArray(new String[items.size()]);        
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, values);
        mListViewHistory.setAdapter(mAdapter);
      }
    } catch (FileNotFoundException e) {
      Log.e(TAG, "File not found: " + e.toString());
    } catch (IOException e) {
        Log.e(TAG, "Can not read file: " + e.toString());
    }
  }    
  
}
