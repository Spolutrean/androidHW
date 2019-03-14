package com.example.picturewatcher;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.io.File;

import static com.example.picturewatcher.Constants.PATH_FOR_LOADED_FILES;

public class ItemDetailFragment extends Fragment {
    public static final String ARG_ITEM_ID = "item_id";

    private Content.Item mItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItem = com.example.picturewatcher.Content.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.item_detail, null);

        if (mItem != null) {
            ((TextView) rootView.findViewById(R.id.item_detail)).setText(mItem.details);
            ImageView imageView = (ImageView) rootView.findViewById(R.id.item_detail_image);
            mItem.bigPicturePath = PATH_FOR_LOADED_FILES + "big" + mItem.imageInformation.id + ".jpg";

            if(new File(mItem.bigPicturePath).exists()) {
                imageView.setImageBitmap(Content.checkInternalStorage(mItem.bigPicturePath));
            } else {
                Glide.with(getActivity())
                        .asBitmap()
                        .load(mItem.imageInformation.urls.full)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .addListener(new RequestListener<Bitmap>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                Content.loadToInternalStorage(mItem.bigPicturePath, resource);
                                return false;
                            }
                        })
                        .placeholder(new BitmapDrawable(Content.lruCache.get(mItem.imageInformation.id)))
                        .into(imageView);
            }
            rootView.findViewById(R.id.item_detail_layout).setBackgroundColor(Color.parseColor(mItem.imageInformation.color));
        }

        return rootView;
    }
}
