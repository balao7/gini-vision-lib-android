package net.gini.android.vision.test;

import android.content.res.AssetManager;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.test.InstrumentationRegistry;

import net.gini.android.vision.Document;
import net.gini.android.vision.internal.camera.photo.Photo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Helpers {

    public static <T extends Parcelable, C extends Parcelable.Creator<T>> T doParcelingRoundTrip(
            T payload, C creator) {
        Parcel parcel = Parcel.obtain();
        payload.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        return creator.createFromParcel(parcel);
    }

    public static void prepareLooper() {
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
    }

    public static byte[] getTestJpeg() throws IOException {
        return getTestJpeg("invoice.jpg");
    }

    public static byte[] getTestJpeg(String filename) throws IOException {
        AssetManager assetManager = InstrumentationRegistry.getTargetContext().getAssets();
        InputStream inputStream = null;
        try {
            inputStream = assetManager.open(filename);
            return inputStreamToByteArray(inputStream);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    private static byte[] inputStreamToByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] bytes;
        //noinspection TryFinallyCanBeTryWithResources - only for minSdkVersion 19 and above
        try {
            byte[] buffer = new byte[8192];
            int readBytes;
            while ((readBytes = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, readBytes);
            }
            bytes = outputStream.toByteArray();
        } finally {
            outputStream.close();
        }
        return bytes;
    }

    public static Document createDocument(byte[] jpeg, int orientation) {
        return Document.fromPhoto(Photo.fromJpeg(jpeg, orientation));
    }

}
