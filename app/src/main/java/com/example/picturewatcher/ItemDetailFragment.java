package com.example.picturewatcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.File;

import static com.example.picturewatcher.Constants.PATH_FOR_LOADED_FILES;

public class ItemDetailFragment extends Fragment {
    public static final String ARG_ITEM_ID = "item_id";

    private Content.Item mItem;
    private View rootView;
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItem = com.example.picturewatcher.Content.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
        }
    }

    private void setupOnLongClickImageListener(final Content.Item item, ImageView imageView) {
        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (item.liked) {
                    showFavoriteAnimation(R.drawable.like_broken);
                    if(ItemListActivity.api.isImageExistInDatabase(item.imageInformation)) {
                        ItemListActivity.api.removeImageFromDatabase(item.imageInformation);
                    }
                    item.liked = false;
                } else {
                    showFavoriteAnimation(R.drawable.like_filled);
                    if(!ItemListActivity.api.isImageExistInDatabase(item.imageInformation)) {
                        ItemListActivity.api.insertImageInDatabase(item.imageInformation);
                    }
                    item.liked = true;
                }
                return false;
            }
        });
    }

    private void showFavoriteAnimation(int imageId) {
        ImageView view = new ImageView(context);
        view.setImageDrawable(ContextCompat.getDrawable(context, imageId));
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT, R.id.item_detail_image);
        view.setLayoutParams(lp);
        view.getLayoutParams().height = 400;
        view.getLayoutParams().width = 400;

        ((RelativeLayout)rootView.findViewById(R.id.relativeDetailLayout)).addView(view);

        Animation fadeInAnimation = AnimationUtils.loadAnimation(context,
                R.anim.set_like_unlike);
        view.startAnimation(fadeInAnimation);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.item_detail, null);
        context = getActivity();
        if (mItem != null) {
            ((TextView) rootView.findViewById(R.id.item_detail)).setText(mItem.details);
            ImageView imageView = (ImageView) rootView.findViewById(R.id.item_detail_image);
            setupOnLongClickImageListener(mItem, imageView);
            mItem.bigPicturePath = PATH_FOR_LOADED_FILES + "big" + mItem.imageInformation.id + ".jpg";

            if(new File(mItem.bigPicturePath).exists()) {
                imageView.setImageBitmap(Content.checkInternalStorage(mItem.bigPicturePath));
            } else {
                Glide.with(context)
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
