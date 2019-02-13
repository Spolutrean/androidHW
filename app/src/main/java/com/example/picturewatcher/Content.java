package com.example.picturewatcher;

import android.graphics.Bitmap;
import android.graphics.ImageDecoder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Content {

    /**
     * An array of items.
     */
    public static final List<Item> ITEMS = new ArrayList<Item>();

    /**
     * A map of items, by ID.
     */
    public static final Map<String, Item> ITEM_MAP = new HashMap<String, Item>();

    public static void addItem(Item item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    public static Item createItem(int position, ImageInformation info) {
        return new Item(String.valueOf(position), info);
    }

    private static String makeContent(ImageInformation info) {
        return info.description;
    }

    private static String makeDetails(ImageInformation info) {
        return info.description;
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

        public Item(String id, ImageInformation imageInformation) {
            this.id = id;
            this.content = makeContent(imageInformation);
            this.details = makeDetails(imageInformation);
            this.imageInformation = imageInformation;
            this.image = null;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
