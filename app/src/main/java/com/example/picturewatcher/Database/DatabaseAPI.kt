package com.example.picturewatcher.Database

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteException
import android.provider.Settings
import com.example.picturewatcher.Constants
import com.example.picturewatcher.Database.Tables.Favourites
import com.example.picturewatcher.ImageInformation
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.db.delete
import org.jetbrains.anko.db.insertOrThrow


/**
 * Local database API
 *
 * @param context parent context
 */
public class DatabaseAPI(private val context: Context) {

    /**
     * @param image image to remove in database
     */
    fun removeImageFromDatabase(image: ImageInformation) {
        GalleryDatabase.getInstance(context).use {
            delete(Favourites.NAME, "${Favourites.FIELDS.ID} = '${image.id}'")
        }
    }

    /**
     * @param image to check the existence to the database
     * @return true if exist, or false
     */
    fun isImageExistInDatabase(image: ImageInformation): Boolean {
        return GalleryDatabase.getInstance(context).use {
            val cursor: Cursor
            try {
                cursor = query(Favourites.NAME,
                        arrayOf(Favourites.FIELDS.ID),
                        "${Favourites.FIELDS.ID} = \"${image.id}\"",
                        null,
                        null,
                        null,
                        null)
            } catch (e: SQLiteException) {
                throw Exception("Table ${Favourites.NAME} doesn't exist")
            }
            return@use cursor.count > 0
        }
    }

    /**
     * Get all image essence from database by page
     * @param page current page to load. Used for chunk loading of [IMAGE_PER_PAGE] items
     * @return [Image] list essence
     */
    fun getFavouritesFromDatabase(page: Int): List<ImageInformation> {
        return GalleryDatabase.getInstance(context).use {
            val cursor: Cursor
            try {
                cursor = query(Favourites.NAME, null, null,
                        null, null, null, null, "${Constants.ITEMS_PER_PAGE * (page - 1)}, ${Constants.ITEMS_PER_PAGE}")
            } catch (e: SQLiteException) {
                throw Exception("Table ${Favourites.NAME} doesn't exist")
            }

            cursor.moveToFirst()

            val images = mutableListOf<ImageInformation>()

            if (cursor.count == 0)
                return@use images
            do {
                var image = ImageInformation()
                var pos = 0
                for (columnName in cursor.columnNames) {
                    when (columnName) {
                        Favourites.FIELDS.SERIALIZED_IMAGE -> image =
                                ObjectMapper().readerFor(ImageInformation::class.java)
                                        .readValue<ImageInformation>(cursor.getString(pos))
                    }
                    pos++;
                }

                images.add(image)
            } while (cursor.moveToNext())

            return@use images
        }
    }

    /**
     * Insert imageData in database
     *
     * @param image imageData to add to the database
     */
    fun insertImageInDatabase(image: ImageInformation) {
        val mapper = ObjectMapper()

        GlobalScope.launch(Dispatchers.IO) {
            GalleryDatabase.getInstance(context).use {
                insertOrThrow(Favourites.NAME,
                        Favourites.FIELDS.ID to image.id,
                        Favourites.FIELDS.SERIALIZED_IMAGE to mapper.writeValueAsString(image))
            }
        }
    }

    fun deleteDatabase() {
        context.deleteDatabase(DATABASE_NAME);
    }
}