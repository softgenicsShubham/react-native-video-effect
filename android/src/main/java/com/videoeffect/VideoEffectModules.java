
package com.videoeffect;

import android.util.Base64;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.uimanager.NativeViewHierarchyManager;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.uimanager.UIBlock;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.CvType;
import org.opencv.core.Scalar;
import org.opencv.core.Size;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class VideoEffectModules extends ReactContextBaseJavaModule {

    public VideoEffectModules(ReactApplicationContext context) {
        super(context);
    }

    // static {
    //     System.loadLibrary("opencv_java4");
    // }

    public String getName() {
        return "VideoEffectModules";
    }

    private void sendEvent(String eventName, int... data) {
        WritableMap params = Arguments.createMap();
        params.putInt("type", data[0]);
        params.putInt("channels", data[1]);
        params.putDouble("height", data[2]);
        params.putDouble("width", data[3]);

        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }

    @ReactMethod
    public void addTwoNumber(double a, double b, Promise promise) {
        double result = a + b;
        promise.resolve(result);
    }

    @ReactMethod
    public void generateRandomMatrix(int rows, int cols, Promise promise) {
        try {
            Mat randomMatrix = new Mat(rows, cols, CvType.CV_8UC1);
            Core.randn(randomMatrix, 0, 255);

            // Convert the matrix to a byte array
            byte[] byteArray = new byte[(int) (randomMatrix.total() * randomMatrix.elemSize())];
            randomMatrix.get(0, 0, byteArray);

            // Encode the byte array as a Base64 string
            String base64String = Base64.encodeToString(byteArray, Base64.DEFAULT);

            // Resolve the promise with the Base64 string
            promise.resolve(base64String);
        } catch (Exception e) {
            // Reject the promise with the error message
            promise.reject(e);
        }
    }

}
