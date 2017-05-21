package treebolic.provider.contacts;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;

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
}
