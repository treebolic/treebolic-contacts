package treebolic.provider.contacts;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Photo utilities
 *
 * @author <a href="mailto:1313ou@gmail.com">Bernard Bou</a>
 */
public class Utils
{
	/**
	 * Get thumbnail photo from photoId
	 *
	 * @param context  context
	 * @param photoUri id as per ContactsContract.Contacts.PHOTO_THUMBNAIL_URI
	 * @return bitmap
	 */
	static public Bitmap queryThumbnailPhotoFromPhotoUri(final Context context, final String photoUri)
	{
		try
		{
			return MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(photoUri));
		}
		catch (IOException e)
		{
			//
		}
		return null;
	}

	/**
	 * Get thumbnail photo from photoId
	 *
	 * @param context context
	 * @param photoId id as per ContactsContract.Contacts.PHOTO_ID
	 * @return bitmap
	 */
	static public Bitmap queryThumbnailPhotoFromPhotoId(final Context context, final long photoId)
	{
		final Uri uri = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, photoId);
		final String[] projection = new String[]{ContactsContract.Contacts.Photo.PHOTO};
		final String selection = null;
		final String[] selectionArgs = null;

		//final Uri uri = ContactsContract.Data.CONTENT_URI;
		//final String[] projection = new String[]{ContactsContract.CommonDataKinds.Photo.PHOTO};
		//final String selection = ContactsContract.Data._ID + " = ?";
		//final String[] selectionArgs = new String[]{Integer.toString(photoId)};

		final Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
		if (cursor != null)
		{
			try
			{
				return cursorToImage(cursor);
			}
			finally
			{
				cursor.close();
			}
		}
		return null;
	}

	static private Bitmap cursorToImage(final Cursor cursor)
	{
		if (cursor.moveToFirst())
		{
			final byte[] imageBytes = cursor.getBlob(0);
			if (imageBytes != null)
			{
				return bytesToImage(imageBytes);
			}
		}
		return null;
	}

	static private Bitmap bytesToImage(final byte[] imageBytes)
	{
		return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
	}

	static public Bitmap roundCrop(final Bitmap bitmap)
	{
		final int w = bitmap.getWidth();
		final int h = bitmap.getHeight();
		final Bitmap output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		final Canvas canvas = new Canvas(output);
		canvas.drawARGB(0, 0, 0, 0);
		final Rect rect = new Rect(0, 0, w, h);

		final int color = 0xff808080;
		final Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(color);
		canvas.drawCircle(h / 2, h / 2, h / 2, paint);    // canvas.drawRoundRect(rect, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}

	static public Bitmap scale(final Bitmap bitmap, int w, int h)
	{
		return Bitmap.createScaledBitmap(bitmap, w, h, false);
	}

	static String bitmapToUrl(final Bitmap bitmap)
	{
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
		final byte[] byteArray = byteArrayOutputStream.toByteArray();
		final String imageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
		final String dataUrl = "data:image/png;base64," + imageBase64;
		return dataUrl;

		// webview.loadUrl(dataURL); //pass the bitmap base64 dataurl in URL parameter
	}
}
