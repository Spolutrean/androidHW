package com.example.picturewatcher;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
import android.widget.ImageView;

import java.net.URL;
import java.util.ArrayList;

public class ImageDownloadTask extends AsyncTask<ArrayList<Pair<String, ImageView>>, Void, Integer> {

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.d("ImageDownloadTask", "Started");
    }

    @Override
    protected void onPostExecute(Integer o) {
        super.onPostExecute(o);
        Log.d("ImageDownloadTask", "Finished");
    }

    @Override
    protected Integer doInBackground(ArrayList<Pair<String, ImageView>> ... input) {
        Integer successfully = 0;
        for (ArrayList<Pair<String, ImageView>> anInputArr : input) {
            for(Pair<String, ImageView> anInput : anInputArr) {
                try {
                    URL url = new URL(anInput.first);
                    Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    anInput.second.setImageBitmap(bmp);
                    ++successfully;
                } catch (Exception ex) {
                    Log.e("ImageDownloadTask", ex.toString());
                }
            }
        }
        return successfully;
    }
}
