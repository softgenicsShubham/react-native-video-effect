package com.videoeffect;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.Image;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.camera.view.PreviewView;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;


import com.facebook.react.ReactActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import androidx.core.app.ActivityCompat;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ThemedReactContext;
import com.google.common.util.concurrent.ListenableFuture;



public class CameraXManager {
  private static final String TAG = "CameraXManager";
  private static final int CAMERA_PERMISSION_REQUEST_CODE = 101;

  private ImageAnalysis imageAnalysis;

  private PreviewView previewView;
  private Context context;
  private Camera camera;
  private ExecutorService cameraExecutor;
  private LifecycleOwner lifecycleOwner;

  public CameraXManager(Context context, PreviewView previewView, LifecycleOwner lifecycleOwner) {
    this.context = context;
    this.lifecycleOwner = lifecycleOwner;
    cameraExecutor = Executors.newSingleThreadExecutor();

    Log.d(TAG, "CameraXManager: " + cameraExecutor.toString());
    if (checkCameraPermission()) {
      startCamera(previewView);
    } else {
      requestCameraPermission();
      startCamera(previewView);
    }
  }




  private boolean checkCameraPermission() {
    return ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA)
      == android.content.pm.PackageManager.PERMISSION_GRANTED;
  }

  private void requestCameraPermission() {
    if (context instanceof ThemedReactContext) {
      ThemedReactContext themedReactContext = (ThemedReactContext) context;
      Activity currentActivity = themedReactContext.getCurrentActivity();

      if (currentActivity != null) {
        ActivityCompat.requestPermissions(currentActivity,
          new String[]{android.Manifest.permission.CAMERA},
          CAMERA_PERMISSION_REQUEST_CODE);
      }
    }
  }



  private void startCamera(PreviewView previewView) {
    ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);
    cameraProviderFuture.addListener(() -> {
      try {
        ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

        CameraSelector cameraSelector = new CameraSelector.Builder()
          .requireLensFacing(CameraSelector.LENS_FACING_BACK)
          .build();

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageAnalysis = new ImageAnalysis.Builder()
          .setTargetResolution(new Size(1920, 1080))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
              .build();

        // To do implement the image analyser to here


        Log.d(TAG, "Image Analysis: " + imageAnalysis.toString());

        imageAnalysis.setAnalyzer(cameraExecutor, new ImageAnalysis.Analyzer() {
          @Override
          public void analyze(@NonNull ImageProxy image) {
//            int width = image.getWidth();
//            int height = image.getHeight();
//
//            Log.d(TAG, "Image Width: " + width);
//            Log.d(TAG, "Image Height: " +

            Handler mainHandler = new Handler(Looper.getMainLooper());

            mainHandler.post(new Runnable() {
              @Override
              public void run() {
                // Update UI elements
              }
            });


            image.close();


          }
        });

        cameraProvider.unbindAll();
        camera = cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview);
      } catch (Exception e) {
        Log.e(TAG, "Error starting camera: " + e.getMessage());
      }
    }, ContextCompat.getMainExecutor(context));
  }

  public void releaseCamera() {
    if (cameraExecutor != null) {
      cameraExecutor.shutdown();
    }
  }
}
