package com.example.picturewatcher;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.net.URL;

public class ImageDownloadTask extends AsyncTask {

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.d("ImageDownloadTask", "Started");
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        Log.d("ImageDownloadTask", "Finished");
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        String imgLink = objects[0].toString();

        try {
            URL url = new URL(imgLink);
            Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            return bmp;
        } catch (Exception ex) {
            Log.e("ImageDownloadTask.java", ex.toString());
            return null;
        }
    }
}
