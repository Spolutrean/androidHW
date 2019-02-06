package com.example.picturewatcher;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.net.URL;
import java.util.ArrayList;

public class ImageDownloadTask extends AsyncTask<String, Void, ArrayList<Bitmap>> {

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.d("ImageDownloadTask", "Started");
    }

    @Override
    protected void onPostExecute(ArrayList<Bitmap> o) {
        super.onPostExecute(o);
        Log.d("ImageDownloadTask", "Finished");
    }

    @Override
    protected ArrayList<Bitmap> doInBackground(String ... objects) {
        ArrayList<Bitmap> ret = new ArrayList<>();
        for (String link: objects) {
            try {
                URL url = new URL(link);
                Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                ret.add(bmp);
            } catch (Exception ex) {
                Log.e("ImageDownloadTask.java", ex.toString());
                ret.add(null);
            }

        }
        return ret;
    }
}
