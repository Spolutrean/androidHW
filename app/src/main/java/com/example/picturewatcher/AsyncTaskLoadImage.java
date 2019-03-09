package com.example.picturewatcher;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

public class AsyncTaskLoadImage  extends AsyncTask<String, String, Bitmap> {
    private WeakReference<ImageView> imageViewReference;
    public AsyncTaskLoadImage(ImageView imageViewReference) {
        this.imageViewReference = new WeakReference<ImageView>(imageViewReference);
    }
    @Override
    protected Bitmap doInBackground(String... params) {
       return Content.getPicture(params[0], params[1], params[2]);
    }
    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (imageViewReference != null && bitmap != null) {
            final ImageView imageView = imageViewReference.get();
            if(imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }
}