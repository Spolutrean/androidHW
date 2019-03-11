package com.example.picturewatcher;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.os.Environment;
import android.support.v4.util.LruCache;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Content {

    /**
     * An array of items.
     */
    public static final List<Item> ITEMS = Collections.synchronizedList(new ArrayList<Item>());

    /**
     * A map of items, by ID.
     */
    public static final Map<String, Item> ITEM_MAP = new ConcurrentHashMap<String, Item>();

    /**
     * Size for lru cache.
     */
    public static final int cacheSize = (int) ((Runtime.getRuntime().maxMemory()) / 4);

    /**
     * LRU cache for loaded images.
     */
    public static final LruCache<String, Bitmap> lruCache = new LruCache<String, Bitmap>(cacheSize) {
        protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount();
        }
    };

    public static void addItem(Item item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.imageInformation.id, item);
    }

    public static Item createItem(ImageInformation info) {
        return new Item(info);
    }

    private static String makeContent(ImageInformation info) {
        if(info.description != null) {
            return info.description;
        } else {
            return "Empty description";
        }
    }

    private static String makeDetails(ImageInformation info) {
        if(info.description != null) {
            return info.description;
        } else {
            return "Empty description";
        }
    }

    public static Bitmap loadImage(String link) {
        try {
            java.net.URL url = new URL(link);
            Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            return bmp;
        } catch (Exception ex) {
            Log.e(Constants.LOG_TAG, ex.toString());
            return null;
        }
    }

    public static Bitmap checkInternalStorage(String filePath) {
        File imgFile = new File(filePath);
        if (imgFile.exists()) {
            return BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        } else {
            return null;
        }
    }

    public static void loadToInternalStorage(String filePath, Bitmap picture) {
        File imgFile = new File(filePath);
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            picture.compress(Bitmap.CompressFormat.JPEG, 50, stream);

            FileOutputStream outputStream = new FileOutputStream(imgFile, false);
            outputStream.write(stream.toByteArray());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class Item {
        public final String content;
        public final String details;
        public final ImageInformation imageInformation;
        public String bigPicturePath = null;
        public String smallPicturePath = null;

        private String calculateFineColor() {
            int color = Color.parseColor(imageInformation.color);
            int r = ((color >> 16) & 0xff);
            int g = ((color >>  8) & 0xff);
            int b = ((color      ) & 0xff);

            return String.format("#%02x%02x%02x%02x", 128, r, g, b);
        }

        public Item(ImageInformation imageInformation) {
            this.content = makeContent(imageInformation);
            this.details = makeDetails(imageInformation);
            this.imageInformation = imageInformation;
            this.imageInformation.color = calculateFineColor();
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
