/*
 * Copyright (c) 2019-2023. Bernard Bou
 */

package treebolic.provider.contacts;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageDecoder;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Photo utilities
 *
 * @author <a href="mailto:1313ou@gmail.com">Bernard Bou</a>
 */
public class Utils
{
	static public class Group
	{
		public final String groupId;

		public final String systemId;

		public final String title;

		public final String account;

		public final String accountType;

		public final String titleRes;

		public final String resPackage;

		@SuppressWarnings("WeakerAccess")
		public Group(String groupId, String systemId, String title, String account, String accountType, String titleRes, String resPackage)
		{
			this.groupId = groupId;
			this.systemId = systemId;
			this.title = title;
			this.account = account;
			this.accountType = accountType;
			this.titleRes = titleRes;
			this.resPackage = resPackage;
		}
	}

	/**
	 * Get groups
	 *
	 * @param context context
	 * @return group id to group map
	 */
	@Nullable
	static public Map<String, Group> queryGroups(@NonNull final Context context)
	{
		final ContentResolver cr = context.getContentResolver();
		final Cursor cursor = cr.query(treebolic.provider.contacts.Settings.GROUP_URI, //
				treebolic.provider.contacts.Settings.GROUP_PROJECTION, //
				null, //
				null, //
				null);
		if (cursor != null)
		{
			final Map<String, Group> map = new HashMap<>();
			if (cursor.moveToFirst())
			{
				do
				{
					final String groupId = cursor.getString(Settings.GROUP_ID_INDEX);
					final String systemId = cursor.getString(Settings.GROUP_SYSTEM_ID_INDEX);
					String title = cursor.getString(Settings.GROUP_TITLE_INDEX);
					if ("Starred in Android".equals(title))
					{
						title = "Starred";
					}
					final String account = cursor.getString(Settings.GROUP_ACCOUNT_NAME);
					final String accountType = cursor.getString(Settings.GROUP_ACCOUNT_TYPE);

					final String titleRes = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ? null : cursor.getString(Settings.GROUP_TITLE_RES_INDEX);
					final String resPackage = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ? null : cursor.getString(Settings.GROUP_RES_PACKAGE_INDEX);

					final Group group = new Group(groupId, systemId, title, account, accountType, titleRes, resPackage);
					map.put(groupId, group);
				}
				while (cursor.moveToNext());
			}
			cursor.close();
			return map;
		}
		return null;
	}

	/**
	 * Get bitmap from Uri
	 *
	 * @param context context
	 * @param uriStr  image uri
	 * @return bitmap
	 */
	@SuppressWarnings("deprecation")
	@Nullable
	static public Bitmap queryBitmapFromUri(@NonNull final Context context, final String uriStr)
	{
		try
		{
			Uri uri = Uri.parse(uriStr);
			if (Build.VERSION.SDK_INT >= 28)
			{
				ImageDecoder.Source source = ImageDecoder.createSource(context.getContentResolver(), uri);
				return ImageDecoder.decodeBitmap(source, (decoder, info, source2) -> decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE));
			}
			else
			{
				return MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
			}
		}
		catch (IOException ignored)
		{
			//
		}
		return null;
	}

	/**
	 * Get bitmap from id
	 *
	 * @param context context
	 * @param id      id as per ContactsContract.Contacts.PHOTO_ID
	 * @return bitmap
	 */
	@Nullable
	static public Bitmap queryBitmapFromId(@NonNull final Context context, final long id)
	{
		final Uri uri = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, id);
		final String[] projection = new String[]{ContactsContract.Contacts.Photo.PHOTO};

		//final Uri uri = ContactsContract.Data.CONTENT_URI;
		//final String[] projection = new String[]{ContactsContract.CommonDataKinds.Photo.PHOTO};
		//final String selection = ContactsContract.Data._ID + " = ?";
		//final String[] selectionArgs = new String[]{Integer.toString(photoId)};

		final Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
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

	@Nullable
	static private Bitmap cursorToImage(@NonNull final Cursor cursor)
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

	static private Bitmap bytesToImage(@NonNull final byte[] imageBytes)
	{
		return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
	}

	static public Bitmap roundCrop(@NonNull final Bitmap bitmap)
	{
		final int w = bitmap.getWidth();
		final int h = bitmap.getHeight();
		final float h2 = h / 2F;
		final Bitmap output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		final Canvas canvas = new Canvas(output);
		canvas.drawARGB(0, 0, 0, 0);
		final Rect rect = new Rect(0, 0, w, h);

		final int color = 0xff808080;
		final Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(color);
		canvas.drawCircle(h2, h2, h2, paint);    // canvas.drawRoundRect(rect, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}

	static public Bitmap scale(@NonNull final Bitmap bitmap, int w, int h)
	{
		return Bitmap.createScaledBitmap(bitmap, w, h, false);
	}

	@NonNull
	static String bitmapToUrl(@NonNull final Bitmap bitmap)
	{
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
		final byte[] byteArray = byteArrayOutputStream.toByteArray();
		final String imageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
		return "data:image/png;base64," + imageBase64;

		// webview.loadUrl(dataURL); //pass the bitmap base64 dataURL in URL parameter
	}
}
