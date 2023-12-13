package com.videoeffect;

import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.camera.view.PreviewView;
import androidx.lifecycle.LifecycleOwner;


import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

public class VideoEffectViewManager extends SimpleViewManager<PreviewView> {
    public static final String REACT_CLASS = "VideoEffectView";
    private CameraXManager cameraXManager;

    @Override
    @NonNull
    public String getName() {
        return REACT_CLASS;
    }

    static {
        System.loadLibrary("opencv_java4");
    }

    @Override
    @NonNull
    public PreviewView createViewInstance(ThemedReactContext reactContext) {
        PreviewView previewView = new PreviewView(reactContext);
        cameraXManager = new CameraXManager(reactContext, previewView, (LifecycleOwner) reactContext.getCurrentActivity());
        return previewView;
    }

    @ReactProp(name = "color")
    public void setColor(PreviewView previewView, String color) {
        previewView.setBackgroundColor(Color.parseColor(color));
    }

    // Override onDetachedFromWindow to release the camera resources when the view is detached
    @Override
    public void onDropViewInstance(@NonNull PreviewView view) {
        super.onDropViewInstance(view);
        if (cameraXManager != null) {
            cameraXManager.releaseCamera();
        }
    }
}