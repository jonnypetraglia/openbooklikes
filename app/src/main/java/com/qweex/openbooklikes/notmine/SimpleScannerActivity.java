package com.qweex.openbooklikes.notmine;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class SimpleScannerActivity extends Activity implements ZBarScannerView.ResultHandler {
    private ZBarScannerView mScannerView;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZBarScannerView(this);    // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        Log.v("barcode--", rawResult.getBarcodeFormat().getName() + " = " + rawResult.getContents());

        if(rawResult.getBarcodeFormat().getName().startsWith("ISBN")
                ||
                rawResult.getBarcodeFormat().getName().startsWith("UPC")) {
            Intent i = new Intent();
            i.putExtra("barcode", rawResult.getContents());
            setResult(RESULT_OK, i);
            finish();
        } else {
            mScannerView.resumeCameraPreview(this);
        }
    }
}