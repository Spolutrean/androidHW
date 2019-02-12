package com.example.picturewatcher;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
import android.widget.ImageView;

import com.example.picturewatcher.Utils.Triple;

import java.net.URL;
import java.util.ArrayList;

public class ImageDownloadTask extends AsyncTask<Triple<String, Bitmap, ImageView>, Void, Triple<String, Bitmap, ImageView>> {

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.d("ImageDownloadTask", "Started");
    }

    @Override
    protected void onPostExecute(Triple<String, Bitmap, ImageView> t) {
        super.onPostExecute(t);
        t.getThird().setImageBitmap(t.getSecond());
        Log.d("ImageDownloadTask", "Finished");
    }

    @Override
    protected Triple<String, Bitmap, ImageView> doInBackground(Triple<String, Bitmap, ImageView> ... input) {
        try {
            URL url = new URL(input[0].getFirst());
            Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            input[0].setSecond(bmp);
            return input[0];
        } catch (Exception ex) {
            Log.e("ImageDownloadTask", ex.toString());
            return input[0];
        }
    }
}
