package com.example.picturewatcher;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcelable;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.example.picturewatcher.Constants.PATH_FOR_LOADED_FILES;

public class DownloadImageService extends IntentService {
    private static final String LOAD_PIC = "LoadPic";

    public DownloadImageService() {
        super("DownloadImageService");
    }

    public static void startLoading(Context context, List<ImageInformation> images) {
        Intent intent = new Intent(context, DownloadImageService.class);
        intent.putExtra("imagesUrls", (Serializable) images);
        intent.setAction(LOAD_PIC);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (LOAD_PIC.equals(action)) {
                List<ImageInformation> imagesUrls = (List<ImageInformation>) intent.getSerializableExtra("imagesUrls");
                loadPictureToMemory(imagesUrls);
            }
        }
    }

    private void loadPictureToMemory(List<ImageInformation> imageInformations) {
        for (int i = 0; i < imageInformations.size(); ++i) {
            ImageInformation imageInformation = imageInformations.get(i);
            String path = PATH_FOR_LOADED_FILES + "small" + imageInformation.id + ".jpg";
            File file = new File(path);
            File dir = new File(PATH_FOR_LOADED_FILES);
            if(!dir.exists())
            {
               dir.mkdirs();
            }
            if(!file.exists()) {
                String url = imageInformation.urls.small;
                Bitmap bitmap = Content.loadImage(url);
                Content.loadToInternalStorage(file.getAbsolutePath(), bitmap);
            }
            Content.Item item = Content.createItem(imageInformation);
            item.smallPicturePath = path;
            ItemListActivity.addNewRecyclerItem(item);
        }
    }

}
