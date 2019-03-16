package com.example.picturewatcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ContentTest {
    private Context context;
    private String testFolderPath;

    @Before
    public void init() {
        context = InstrumentationRegistry.getTargetContext();
        testFolderPath = context.getFilesDir().getParent() + "/app_test_folder/";
        File dir = new File(testFolderPath);
        dir.mkdir();
    }

    @After
    public void reset() {
        File dir = new File(testFolderPath);
        dir.delete();
    }

    @Test
    public void checkInternalStorageOnIncorrectRandomPath() {
        String incorrectPath = testFolderPath + "incorrect/path";
        assertNull(Content.checkInternalStorage(incorrectPath));
    }
    @Test
    public void checkInternalStorageOnNullPath() {
        String incorrectPath = null;
        assertNull(Content.checkInternalStorage(incorrectPath));
    }
    @Test
    public void checkInternalStorageOnEmptyPath() {
        String incorrectPath = "";
        assertNull(Content.checkInternalStorage(incorrectPath));
    }
    @Test
    public void checkInternalStorageOnDirectoryPath() {
        String incorrectPath = testFolderPath;
        assertNull(Content.checkInternalStorage(incorrectPath));
    }

    @Test
    public void checkFileLoadToStorage() {
        Bitmap bm = Bitmap.createBitmap(543, 543, Bitmap.Config.RGB_565);
        String path = testFolderPath + "testImg.jpg";
        Content.loadToInternalStorage(path, bm);
        File file = new File(path);
        assertTrue(file.exists());
    }

    @Test
    public void checkLoadAndDownloadFileForEquals() {
        Bitmap bm = Bitmap.createBitmap(543, 543, Bitmap.Config.RGB_565);
        String path = testFolderPath + "testImg.jpg";
        Content.loadToInternalStorage(path, bm);
        Bitmap bm1 = Content.checkInternalStorage(path);
        for(int i = 0; i < bm.getWidth(); i++) {
            for(int j = 0; j < bm.getHeight(); j++) {
                assertEquals(bm.getPixel(i, j), bm1.getPixel(i, j));
            }
        }
    }
}