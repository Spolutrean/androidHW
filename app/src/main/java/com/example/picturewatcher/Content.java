package com.example.picturewatcher;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

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

    public static void addItem(Item item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    public static Item createItem(int id, ImageInformation info) {
        return new Item(Integer.toString(id + ITEMS.size()), info);
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

    /**
     * A item representing a piece of content.
     */
    public static class Item {
        public final String id;
        public final String content;
        public final String details;
        public final ImageInformation imageInformation;
        public Bitmap image;

        public Bitmap LoadImage(String link) {
            try {
                java.net.URL url = new URL(link);
                Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                return bmp;
            } catch (Exception ex) {
                Log.e(Constants.LOG_TAG, ex.toString());
                return null;
            }
        }

        public Item(String id, ImageInformation imageInformation) {
            this.id = id;
            this.content = id + " " + makeContent(imageInformation);
            this.details = id + " " + makeDetails(imageInformation);
            this.imageInformation = imageInformation;
            this.image = LoadImage(imageInformation.urls.raw + "&h=" + Constants.MEDIUM_IMAGE_H);
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
