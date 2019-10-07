package com.zebra.jamesswinton.warehouseexperiencedemo.utilities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class DataWedgeUtilities {

  // Debugging
  private static final String TAG = "DataWedgeUtilities";

  // DataWedge Constants
  public static final String SCAN_ACTION = "com.zebra.jamesswinton.scan.ACTION";
  private static final String ACTION_DATAWEDGE_FROM_6_2 = "com.symbol.datawedge.api.ACTION";

  private static final String EXTRA_SET_CONFIG = "com.symbol.datawedge.api.SET_CONFIG";
  private static final String EXTRA_CREATE_PROFILE = "com.symbol.datawedge.api.CREATE_PROFILE";

  // Custom Constants
  private static final String PROFILE_NAME = "Warehouse Experience Demo";
  private static final String PROFILE_ENABLED = "true";
  private static final String CONFIG_MODE = "CREATE_IF_NOT_EXIST";

  private static final String PACKAGE_NAME = "com.zebra.jamesswinton.warehouseexperiencedemo";
  private static final String[] ASSOCIATED_ACTIVITIES = {
      "com.zebra.jamesswinton.warehouseexperiencedemo.MainActivity"
  };


  // Static Variables


  // Variables

  /**
   * Utilisation Methods
   */

  public static void enableProfile(Context cx) {
    Bundle profileConfig = new Bundle();
    profileConfig.putString("PROFILE_NAME", PROFILE_NAME);
    profileConfig.putString("PROFILE_ENABLED", "true");
    profileConfig.putString("CONFIG_MODE", "UPDATE");
    sendDataWedgeIntentWithExtra(cx, ACTION_DATAWEDGE_FROM_6_2, EXTRA_SET_CONFIG, profileConfig);
  }

  public static void disableProfile(Context cx) {
    Bundle profileConfig = new Bundle();
    profileConfig.putString("PROFILE_NAME", PROFILE_NAME);
    profileConfig.putString("PROFILE_ENABLED", "false");
    profileConfig.putString("CONFIG_MODE", "UPDATE");
    sendDataWedgeIntentWithExtra(cx, ACTION_DATAWEDGE_FROM_6_2, EXTRA_SET_CONFIG, profileConfig);
  }

  /**
   * Setup Methods
   */

  public static void initDataWedgeProfile(Context cx) {
    // Create Profile (Will ignore if profile already exists)
    createProfile(cx, PROFILE_NAME);

    // Configure Profile Settings
    setProfileConfig(cx);
  }

  private static void createProfile(Context cx, String profileName) {
    sendDataWedgeIntentWithExtra(cx, ACTION_DATAWEDGE_FROM_6_2, EXTRA_CREATE_PROFILE, profileName);
  }

  private static void setProfileConfig(Context cx) {
    // Create Base Profile Config Bundle
    Bundle profileConfig = new Bundle();
    profileConfig.putString("PROFILE_NAME", PROFILE_NAME);
    profileConfig.putString("PROFILE_ENABLED", PROFILE_ENABLED);
    profileConfig.putString("CONFIG_MODE", CONFIG_MODE);

    // Create Associated App Bundle
    Bundle appConfig = new Bundle();
    appConfig.putString("PACKAGE_NAME", PACKAGE_NAME);
    appConfig.putStringArray("ACTIVITY_LIST", ASSOCIATED_ACTIVITIES);

    // Store AppConfig in ProfileConfig
    profileConfig.putParcelableArray("APP_LIST", new Bundle[]{ appConfig });

    // Create Barcode Config Bundle
    Bundle barcodeConfig = new Bundle();
    barcodeConfig.putString("PLUGIN_NAME", "BARCODE");
    barcodeConfig.putString("RESET_CONFIG", "true");

    // Create BarcodeProperties Bundle
    Bundle barcodeProps = new Bundle();
    barcodeProps.putString("scanner_selection", "auto");
    barcodeProps.putString("scanner_input_enabled", "true");
    barcodeProps.putString("decoder_code128", "true");
    barcodeProps.putString("decoder_aztec", "true");
    barcodeProps.putString("decoder_pdf417", "true");
    barcodeProps.putString("decoder_qrcode", "true");
    barcodeProps.putString("decoder_hanxin", "true");
    barcodeProps.putString("decoder_datamatrix", "true");

    // Store BarcodeProperties in BarcodeConfig
    barcodeConfig.putBundle("PARAM_LIST", barcodeProps);

    // Store BarcodeConfig in Profile Config
    profileConfig.putBundle("PLUGIN_CONFIG", barcodeConfig);

    // Send Intent
    sendDataWedgeIntentWithExtra(cx, ACTION_DATAWEDGE_FROM_6_2, EXTRA_SET_CONFIG, profileConfig);

    // Re-build Profile Config
    profileConfig.remove("APP_LIST");
    profileConfig.remove("PLUGIN_CONFIG");

    // Create Intent Config
    Bundle intentConfig = new Bundle();
    intentConfig.putString("PLUGIN_NAME", "INTENT");
    intentConfig.putString("RESET_CONFIG", "true");

    // Create Intent Properties
    Bundle intentProps = new Bundle();
    intentProps.putString("intent_output_enabled", "true");
    intentProps.putString("intent_action", SCAN_ACTION);
    intentProps.putString("intent_delivery", "2");

    // Bundle Intent Config
    intentConfig.putBundle("PARAM_LIST", intentProps);
    profileConfig.putBundle("PLUGIN_CONFIG", intentConfig);

    // Send Config
    sendDataWedgeIntentWithExtra(cx, ACTION_DATAWEDGE_FROM_6_2, EXTRA_SET_CONFIG, profileConfig);
  }

  /**
   * Methods to send Intent
   */

  // String Extra
  private static void sendDataWedgeIntentWithExtra( Context context, String action, String extraKey,
      String extraValue) {
    Intent dwIntent = new Intent();
    dwIntent.setAction(action);
    dwIntent.putExtra(extraKey, extraValue);
    context.sendBroadcast(dwIntent);
  }

  // Bundle Extra
  private static void sendDataWedgeIntentWithExtra( Context context, String action, String extraKey,
      Bundle extras) {
    Intent dwIntent = new Intent();
    dwIntent.setAction(action);
    dwIntent.putExtra(extraKey, extras);
    context.sendBroadcast(dwIntent);
  }

}
