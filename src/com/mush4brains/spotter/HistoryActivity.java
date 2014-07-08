package com.mush4brains.spotter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class HistoryActivity extends Activity {
  public static String TAG = "HistoryActivity";
  public static String FILENAME = "SpotterLog.txt";

  ArrayAdapter<String> mAdapter;
  ListView mListViewHistory = null;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_history);
  
    mListViewHistory = (ListView)findViewById(R.id.listViewHistory);
    updateListView();
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
