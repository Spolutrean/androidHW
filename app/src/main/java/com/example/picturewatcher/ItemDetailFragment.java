package com.example.picturewatcher;

import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;

public class ItemDetailFragment extends Fragment {
    public static final String ARG_ITEM_ID = "item_id";

    private Content.Item mItem;

    public ItemDetailFragment() {
    }

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
        View rootView = inflater.inflate(R.layout.item_detail, null);


        if (mItem != null) {
            ((TextView) rootView.findViewById(R.id.item_detail)).setText(mItem.details);

            mItem.bigPicturePath = Constants.PATH_FOR_LOADED_FILES + "big" + mItem.imageInformation.id + ".jpg";

            Glide.with(getContext())
                    .load(mItem.imageInformation.urls.raw)
                    .placeholder(new BitmapDrawable(getResources(), Content.lruCache.get(mItem.imageInformation.id)))
                    .into((ImageView) rootView.findViewById(R.id.item_detail_image));

            rootView.findViewById(R.id.item_detail_layout).setBackgroundColor(Color.parseColor(mItem.imageInformation.color));
        }

        return rootView;
    }
}
