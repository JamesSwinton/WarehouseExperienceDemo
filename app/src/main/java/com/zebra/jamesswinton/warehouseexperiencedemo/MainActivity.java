package com.zebra.jamesswinton.warehouseexperiencedemo;

import static com.zebra.jamesswinton.warehouseexperiencedemo.utilities.DataWedgeUtilities.SCAN_ACTION;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.databinding.DataBindingUtil;
import com.airbnb.lottie.LottieDrawable;
import com.zebra.jamesswinton.warehouseexperiencedemo.databinding.ActivityMainBinding;
import com.zebra.jamesswinton.warehouseexperiencedemo.utilities.CustomDialog;
import com.zebra.jamesswinton.warehouseexperiencedemo.utilities.DataWedgeUtilities;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

  // Debugging
  private static final String TAG = "MainActivity";

  // Constants
  private static final int DEFAULT_PADDING = 25;
  private static final Map<String, Boolean> demoBarcodes  = new HashMap<String, Boolean>() {{
    put("Demo-01", false); put("Demo-02", false); put("Demo-03", false); put("Demo-04", false);
    put("Demo-05", false); put("Demo-06", false);
  }};

  private static final String DEMO_01 = "Demo-01";
  private static final String DEMO_02 = "Demo-02";
  private static final String DEMO_03 = "Demo-03";
  private static final String DEMO_04 = "Demo-04";
  private static final String DEMO_05 = "Demo-05";
  private static final String DEMO_06 = "Demo-06";

  // Static Variables
  private static IntentFilter mDataWedgeIntentFilter = new IntentFilter();

  // Variables
  private ActivityMainBinding mDataBinding = null;


  private int mDpAsPixels;
  private enum AnimationState { SUCCESS, ERROR, DUPLICATE, FINISHED }

  /**
   * Life Cycle Methods
   */

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Init DataBinding
    mDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

    // Add DW Scan Filter
    mDataWedgeIntentFilter.addAction(SCAN_ACTION);

    // Init Counter to Zero
    mDataBinding.scanCount.setText(String.format(getString(R.string.scan_progress), 0, demoBarcodes.size()));

    // Init DP Setting
    mDpAsPixels = getPaddingInDp(DEFAULT_PADDING);

    // Init Toolbar
    configureToolbar();
  }

  @Override
  protected void onResume() {
    super.onResume();

    // Register Receiver
    registerReceiver(mDataWedgeBroadcastReceiver, mDataWedgeIntentFilter);
  }

  @Override
  protected void onPause() {
    super.onPause();

    // Unregister Receiver
    unregisterReceiver(mDataWedgeBroadcastReceiver);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.toolbar_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    // Handle Navigation Events
    switch(item.getItemId()) {
      case R.id.reset:
        resetDemo();
        break;
    } return true;
  }

  private void configureToolbar() {
    setSupportActionBar(mDataBinding.toolbarLayout.toolbar);
  }

  /**
   * DataWedge Broadcast Receiver
   */

  private BroadcastReceiver mDataWedgeBroadcastReceiver = new BroadcastReceiver() {

    // Barcode Data
    private static final String SCAN_DATA = "com.symbol.datawedge.data_string";

    @Override
    public void onReceive(Context context, Intent intent) {
      // Validate Action
      if (intent.getAction() == null) {
        Log.e(TAG, "Intent Does Not Contain Action!");
        return;
      }

      // Get Scan Intent
      if (intent.getAction().equals(SCAN_ACTION)) {
        // Log Receipt
        Log.i(TAG, "DataWedge Scan Intent Received");

        // Get Scanned Data
        String scannedData = intent.getStringExtra(SCAN_DATA);

        // Handle Scan Result
        handleScannedBarcode(scannedData);
      }
    }
  };

  /**
   * Barcode Logic
   */

  private void handleScannedBarcode(String scannedData) {
    // Verify Barcode is part of demo
    if (!demoBarcodes.containsKey(scannedData)) {
      Log.e(TAG, "Scanned barcode is not part of demo!");
      // Scan Animation Removed
      // showScanAnimation(AnimationState.ERROR);
      return;
    }

    // Verify if Barcode has already been scanned
    if (demoBarcodes.get(scannedData)) {
      Log.e(TAG, "Barcode has already been scanned!");
      // Scan Animation Removed
      // showScanAnimation(AnimationState.DUPLICATE);
      return;
    }

    // Barcode exists & has not been scanned before, update map value
    demoBarcodes.put(scannedData, true);

    // Scan Animation Removed
    // showScanAnimation(AnimationState.SUCCESS);

    // Update UI State
    updateUiState();

    // Handle Swap / Finish
    handleSwapAndFinishState();
  }

  /**
   * Scan State Logic
   */

  private void handleSwapAndFinishState() {
    // Get Total Scanned
    int scanCount = getScanCount();

    // Handle Battery Swap Event
    if (scanCount == (demoBarcodes.size() / 2)) {
      // Disable Scanning
      DataWedgeUtilities.disableProfile(this);

      // Show Dialog
      CustomDialog.showCustomDialog(
        this,
        CustomDialog.DialogType.INFO,
        "Swap Battery!",
        "You've scanned half of the barcodes, it's time to replace the battery!",
        "CONTINUE",
        (dialogInterface, i) -> {
          // Dialog Confirmed -> Dismiss & Re-enable Scanner
          dialogInterface.dismiss();
          DataWedgeUtilities.enableProfile(this);
        });
    }

    // Handle Finish Event
    if (scanCount == demoBarcodes.size()) {
      // Disable Scanning
      DataWedgeUtilities.disableProfile(this);

      // Show Dialog
      CustomDialog.showCustomDialog(
        this,
        CustomDialog.DialogType.SUCCESS,
        "Finished!",
        "You've scanned all available barcodes, please move to the next station!",
        "FINISH",
        (dialogInterface, i) -> {
          // Dialog Confirmed -> Dismiss, Re-enable Scanner & Reset Demo State
          dialogInterface.dismiss();
          DataWedgeUtilities.enableProfile(this);
          resetDemo();
        });
    }
  }

  private void updateUiState() {
    // Get total
    int scanCount = getScanCount();

    // Update TextView State
    mDataBinding.scanCount.setText(String.format(getString(R.string.scan_progress), scanCount, demoBarcodes.size()));

    // Update ImageViews
    for (Map.Entry<String, Boolean> entry : demoBarcodes.entrySet()) {
      // If Barcode Scanned, Get Key && Update Corresponding ImageView
      if (entry.getValue()) {
        switch (entry.getKey()) {
          case DEMO_01:
            mDataBinding.barcodeTable.rowOneBarcodeOne.setImageResource(R.drawable.ic_success);
            mDataBinding.barcodeTable.rowOneBarcodeOne.setPadding(mDpAsPixels, mDpAsPixels,
                    mDpAsPixels, mDpAsPixels);
            break;
          case DEMO_02:
            mDataBinding.barcodeTable.rowOneBarcodeTwo.setImageResource(R.drawable.ic_success);
            mDataBinding.barcodeTable.rowOneBarcodeTwo.setPadding(mDpAsPixels, mDpAsPixels,
                    mDpAsPixels, mDpAsPixels);
            break;
          case DEMO_03:
            mDataBinding.barcodeTable.rowOneBarcodeThree.setImageResource(R.drawable.ic_success);
            mDataBinding.barcodeTable.rowOneBarcodeThree.setPadding(mDpAsPixels, mDpAsPixels,
                    mDpAsPixels, mDpAsPixels);
            break;
          case DEMO_04:
            mDataBinding.barcodeTable.rowTwoBarcodeOne.setImageResource(R.drawable.ic_success);
            mDataBinding.barcodeTable.rowTwoBarcodeOne.setPadding(mDpAsPixels, mDpAsPixels,
                    mDpAsPixels, mDpAsPixels);
            break;
          case DEMO_05:
            mDataBinding.barcodeTable.rowTwoBarcodeTwo.setImageResource(R.drawable.ic_success);
            mDataBinding.barcodeTable.rowTwoBarcodeTwo.setPadding(mDpAsPixels, mDpAsPixels,
                    mDpAsPixels, mDpAsPixels);
            break;
          case DEMO_06:
            mDataBinding.barcodeTable.rowTwoBarcodeThree.setImageResource(R.drawable.ic_success);
            mDataBinding.barcodeTable.rowTwoBarcodeThree.setPadding(mDpAsPixels, mDpAsPixels,
                    mDpAsPixels, mDpAsPixels);
            break;
        }
      }
    }
  }

  private int getScanCount() {
    int scanCount = 0;
    for (Boolean scanned : demoBarcodes.values()) {
      if (scanned) {
        scanCount++;
      }
    } return scanCount;
  }

  /**
   * Animation Logic - Currently Redundant
   */

  private void showScanAnimation(AnimationState animationState) {
    // Show Animation View
    mDataBinding.scanAnimation.setVisibility(View.VISIBLE);

    // Handle States
    switch (animationState) {
      case SUCCESS:
        mDataBinding.scanAnimation.setAnimation(R.raw.success);
        break;
      case ERROR:
      case DUPLICATE:
        mDataBinding.scanAnimation.setAnimation(R.raw.error);
        break;
      case FINISHED:
        mDataBinding.scanAnimation.setAnimation(R.raw.finish);
        break;
    }

    // Show Animation; Handle finish with Listener
    mDataBinding.scanAnimation.playAnimation();
    mDataBinding.scanAnimation.addAnimatorListener(hideAnimationOnFinish());
  }

  private AnimatorListener hideAnimationOnFinish() {
    return new AnimatorListener() {
      @Override
      public void onAnimationStart(Animator animator) {
        Log.i(TAG, "Scan Animation Started");
      }

      @Override
      public void onAnimationEnd(Animator animator) {
        Log.i(TAG, "Scan Animation Finished");
        mDataBinding.scanAnimation.setVisibility(View.GONE);
      }

      @Override
      public void onAnimationCancel(Animator animator) {
        Log.i(TAG, "Scan Animation Cancelled");
      }

      @Override
      public void onAnimationRepeat(Animator animator) {
        Log.i(TAG, "Scan Animation Repeated");
      }
    };
  }

  /**
   * Reset Logic
   */

  private void resetDemo() {
    // Loop array and reset all values to false
    for (Map.Entry<String, Boolean> entry : demoBarcodes.entrySet()) {
      entry.setValue(false);
    }

    // Update UI
    mDataBinding.scanCount.setText(String.format(getString(R.string.scan_progress), 0,
        demoBarcodes.size()));

    // Update Images
    mDataBinding.barcodeTable.rowOneBarcodeOne.setImageResource(R.drawable.ic_code_128_demo_01);
    mDataBinding.barcodeTable.rowOneBarcodeTwo.setImageResource(R.drawable.ic_aztec_demo_02);
    mDataBinding.barcodeTable.rowOneBarcodeThree.setImageResource(R.drawable.ic_pdf417_demo_03);
    mDataBinding.barcodeTable.rowTwoBarcodeOne.setImageResource(R.drawable.ic_qr_code_demo_04);
    mDataBinding.barcodeTable.rowTwoBarcodeTwo.setImageResource(R.drawable.ic_han_xin_demo_05);
    mDataBinding.barcodeTable.rowTwoBarcodeThree.setImageResource(R.drawable.ic_data_matrix_demo_06);

    // Apply Custom Paddings
    mDataBinding.barcodeTable.rowOneBarcodeOne.setPadding(getPaddingInDp(10), getPaddingInDp(10),
            getPaddingInDp(10), getPaddingInDp(10));
    mDataBinding.barcodeTable.rowOneBarcodeThree.setPadding(0,0,0,0);
  }

  /**
   * UI Logic
   */

  private int getPaddingInDp(int dp) {
    return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
  }
}