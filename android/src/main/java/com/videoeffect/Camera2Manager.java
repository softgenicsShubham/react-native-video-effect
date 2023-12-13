package com.videoeffect;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.facebook.react.ReactActivity;

import java.util.Arrays;

public class Camera2Manager {
  private static final String TAG = "Camera2Manager";
  private static final int CAMERA_PERMISSION_REQUEST_CODE = 101;
  private Context context;
  private CameraManager cameraManager;
  private String cameraId;
  private CameraDevice cameraDevice;
  private CameraCaptureSession cameraCaptureSession;
  private Handler backgroundHandler;
  private HandlerThread backgroundThread;

  public Camera2Manager(Context context, TextureView textureView) {
    this.context = context;
    cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
    if(checkCameraPermission()) {
      openCamera(textureView);
    } else {
      requestCameraPermission();
      openCamera(textureView);
    }
  }


  private boolean checkCameraPermission() {
    return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
  }


  private void requestCameraPermission() {
    ActivityCompat.requestPermissions((ReactActivity) context,
      new String[]{Manifest.permission.CAMERA},
      CAMERA_PERMISSION_REQUEST_CODE);
  }

  public void setCameraId(String cameraId) {
    this.cameraId = cameraId;
  }






  private void openCamera(TextureView textureView) {
    try {
      cameraId = cameraManager.getCameraIdList()[0]; // Choose the first available camera

      // Open the camera
      cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
          cameraDevice = camera;
          createCameraPreviewSession(textureView);
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
          camera.close();
          cameraDevice = null;
        }

        @Override
        public void onError(CameraDevice camera, int error) {
          camera.close();
          cameraDevice = null;
        }
      }, null);
    } catch (CameraAccessException e) {
      Log.e(TAG, "Error opening camera: " + e.getMessage());
    }
  }

  private void createCameraPreviewSession(TextureView textureView) {
    try {
      SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
      surfaceTexture.setDefaultBufferSize(textureView.getWidth(), textureView.getHeight());
      Surface previewSurface = new Surface(surfaceTexture);

      // Create a capture request for the preview
      final CaptureRequest.Builder captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
      captureRequestBuilder.addTarget(previewSurface);

      // Create a CameraCaptureSession for camera preview
      cameraDevice.createCaptureSession(Arrays.asList(previewSurface), new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession session) {
          if (cameraDevice == null) {
            return;
          }

          // When the session is ready, start displaying the preview
          cameraCaptureSession = session;
          captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
          try {
            cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, backgroundHandler);
          } catch (CameraAccessException e) {
            Log.e(TAG, "Error starting camera preview: " + e.getMessage());
          }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {
          Log.e(TAG, "Failed to configure camera session");
        }
      }, null);
    } catch (CameraAccessException e) {
      Log.e(TAG, "Error creating camera preview session: " + e.getMessage());
    }
  }

  public void releaseCamera() {
    if (cameraCaptureSession != null) {
      cameraCaptureSession.close();
      cameraCaptureSession = null;
    }
    if (cameraDevice != null) {
      cameraDevice.close();
      cameraDevice = null;
    }
    stopBackgroundThread();
  }

  private void startBackgroundThread() {
    backgroundThread = new HandlerThread("CameraBackground");
    backgroundThread.start();
    backgroundHandler = new Handler(backgroundThread.getLooper());
  }

  private void stopBackgroundThread() {
    if (backgroundThread != null) {
      backgroundThread.quitSafely();
      try {
        backgroundThread.join();
        backgroundThread = null;
      } catch (InterruptedException e) {
        Log.e(TAG, "Error stopping background thread: " + e.getMessage());
      }
    }
  }

}
