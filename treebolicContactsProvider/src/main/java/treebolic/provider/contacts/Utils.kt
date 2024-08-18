/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package treebolic.provider.contacts

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.ImageDecoder.ImageInfo
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.MediaStore
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.IOException

/**
 * Photo utilities
 *
 * @author [Bernard Bou](mailto:1313ou@gmail.com)
 */
object Utils {

    /**
     * Get groups
     *
     * @param context context
     * @return group id to group map
     */
    @JvmStatic
    fun queryGroups(context: Context): Map<String, Group>? {
        val cr = context.contentResolver
        val cursor = cr.query(
            Settings.GROUP_URI,
            Settings.GROUP_PROJECTION,
            null,
            null,
            null
        )
        if (cursor != null) {
            val map: MutableMap<String, Group> = HashMap()
            if (cursor.moveToFirst()) {
                do {
                    val groupId = cursor.getString(Settings.GROUP_ID_INDEX)
                    val systemId = cursor.getString(Settings.GROUP_SYSTEM_ID_INDEX)
                    var title = cursor.getString(Settings.GROUP_TITLE_INDEX)
                    if ("Starred in Android" == title) {
                        title = "Starred"
                    }
                    val account = cursor.getString(Settings.GROUP_ACCOUNT_NAME)
                    val accountType = cursor.getString(Settings.GROUP_ACCOUNT_TYPE)

                    val titleRes = cursor.getString(Settings.GROUP_TITLE_RES_INDEX)
                    val resPackage = cursor.getString(Settings.GROUP_RES_PACKAGE_INDEX)

                    val group = Group(groupId, systemId, title, account, accountType, titleRes, resPackage)
                    map[groupId] = group
                } while (cursor.moveToNext())
            }
            cursor.close()
            return map
        }
        return null
    }

    /**
     * Get bitmap from Uri
     *
     * @param context context
     * @param uriStr  image uri
     * @return bitmap
     */
    @JvmStatic
    @Suppress("deprecation")
    fun queryBitmapFromUri(context: Context, uriStr: String?): Bitmap? {
        try {
            val uri = Uri.parse(uriStr)
            if (Build.VERSION.SDK_INT >= 28) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                return ImageDecoder.decodeBitmap(source) { decoder: ImageDecoder, _: ImageInfo?, _: ImageDecoder.Source? -> decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE }
            } else {
                return MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
        } catch (ignored: IOException) {

        }
        return null
    }

    /**
     * Get bitmap from id
     *
     * @param context context
     * @param id      id as per ContactsContract.Contacts.PHOTO_ID
     * @return bitmap
     */
    fun queryBitmapFromId(context: Context, id: Long): Bitmap? {
        val uri = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, id)
        val projection = arrayOf(ContactsContract.Contacts.Photo.PHOTO)
        context.contentResolver.query(uri, projection, null, null, null)?.use {
            return cursorToImage(it)
        }
        return null
    }

    private fun cursorToImage(cursor: Cursor): Bitmap? {
        if (cursor.moveToFirst()) {
            val imageBytes = cursor.getBlob(0)
            if (imageBytes != null) {
                return bytesToImage(imageBytes)
            }
        }
        return null
    }

    private fun bytesToImage(imageBytes: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    @JvmStatic
    fun roundCrop(bitmap: Bitmap): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        val h2 = h / 2f
        val output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        canvas.drawARGB(0, 0, 0, 0)
        val rect = Rect(0, 0, w, h)

        val color = -0x7f7f80
        val paint = Paint()
        paint.isAntiAlias = true
        paint.color = color
        canvas.drawCircle(h2, h2, h2, paint) // canvas.drawRoundRect(rect, roundPx, roundPx, paint);

        paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_IN))
        canvas.drawBitmap(bitmap, rect, rect, paint)

        return output
    }

    fun scale(bitmap: Bitmap, w: Int, h: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, w, h, false)
    }

    fun bitmapToUrl(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        val imageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT)
        return "data:image/png;base64,$imageBase64"
    }

    class Group(val groupId: String, val systemId: String, @JvmField val title: String, @JvmField val account: String, val accountType: String, val titleRes: String?, val resPackage: String?)
}
