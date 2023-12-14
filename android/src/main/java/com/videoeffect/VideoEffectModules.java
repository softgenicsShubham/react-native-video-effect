
package com.videoeffect;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

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

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.CvType;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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

    @ReactMethod
    public void convertImageToMat(String url, Promise promise) {
      String TAG = "VideoEffectModules";
      try {
        // Image downloading
        URL imageUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) imageUrl.openConnection();
        connection.setDoInput(true);
        connection.connect();
        InputStream input = connection.getInputStream();
        Bitmap bitmap = BitmapFactory.decodeStream(input);

        Mat mat = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC1);

        Utils.bitmapToMat(bitmap, mat);

        Log.d(TAG, "Mat dimensions: " + mat.size());
        Log.d(TAG, "Mat type: " + mat.type());

        // converting the image to grayscale

        Mat grayMat = new Mat();

        Imgproc.cvtColor(mat, grayMat, Imgproc.COLOR_BGR2RGB);

        AssetManager assetManager = getReactApplicationContext().getAssets();

        InputStream is = assetManager.open("haarcascade_frontalface_default.xml");

        File cascadeDir = getReactApplicationContext().getDir("cascade", Context.MODE_PRIVATE);
        File cascadeFile = new File(cascadeDir, "haarcascade_frontalface_default.xml");

        FileOutputStream os = new FileOutputStream(cascadeFile);

        byte[] buffer = new byte[4096];

        int bytesRead;

        while ((bytesRead = is.read(buffer)) != -1) {
          os.write(buffer, 0, bytesRead);
        }

        is.close();
        os.close();

        CascadeClassifier faceDectector = new CascadeClassifier();

        faceDectector.load(cascadeFile.getAbsolutePath());

        MatOfRect faces = new MatOfRect();
        faceDectector.detectMultiScale(grayMat, faces);

        for (Rect rect : faces.toArray()) {
          // Draw a rectangle around each face
          Imgproc.rectangle(mat, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0), 3);
        }

        Log.d(TAG, "Number of faces detected: " + faces.toArray().length);


        // Convert the Mat back to a Bitmap
        Bitmap bitmap1 = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bitmap1);

        File cacheDir = getReactApplicationContext().getCacheDir();
        File imageFile = new File(cacheDir, "detected1_faces.png");

        FileOutputStream fos = new FileOutputStream(imageFile);

        bitmap1.compress(Bitmap.CompressFormat.PNG, 100, fos);

        fos.close();

        String imagePath = imageFile.getAbsolutePath();



        // for (int i = 0; i < mat.rows(); i++) {
        //   for (int j = 0; j < mat.cols(); j++) {
        //     double[] pixel = mat.get(i, j);
        //     Log.d(TAG, "Pixel(" + i + ", " + j + "): " + pixel[0]);
        //   }
        // }


        promise.resolve(imagePath);

      } catch (Exception e) {
        promise.reject("Error converting image to mat");
      }


    }

}
