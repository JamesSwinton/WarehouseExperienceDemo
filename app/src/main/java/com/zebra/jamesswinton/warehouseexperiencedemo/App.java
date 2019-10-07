package com.zebra.jamesswinton.warehouseexperiencedemo;

import android.app.Application;
import com.zebra.jamesswinton.warehouseexperiencedemo.utilities.DataWedgeUtilities;

public class App extends Application {

  // Debugging
  private static final String TAG = "ApplicationClass";

  // Constants


  // Static Variables


  // Variables


  @Override
  public void onCreate() {
    super.onCreate();

    // Init DataWedge
    DataWedgeUtilities.initDataWedgeProfile(getApplicationContext());
  }
}
