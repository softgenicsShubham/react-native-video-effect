package com.videoeffect;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.Log;
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

        if (checkCameraPermission()) {
            startCamera(previewView);
        } else {
            requestCameraPermission();
            startCamera(previewView);
        }
    }

  private void setupImageAnalysis() {
    // Create ImageAnalysis use case
    imageAnalysis = new ImageAnalysis.Builder()
      .setTargetResolution(new Size(/* Your desired resolution */))
      .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
      .build();

    // Set the analyzer to handle the frames
    imageAnalysis.setAnalyzer(cameraExecutor, new ImageAnalysis.Analyzer() {
      @Override
      public void analyze(@NonNull ImageProxy image) {
        // Process the captured image here
        processImage(image);
      }
    });

    // Bind the use case to the lifecycle
    CameraX.bindToLifecycle(lifecycleOwner, imageAnalysis);
  }


  private void processImage(ImageProxy imageProxy) {
      imageProxy.close();
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
