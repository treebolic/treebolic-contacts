/*
 * Copyright (c) 2019-2023. Bernard Bou
 */

package org.treebolic.contacts;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.util.AttributeSet;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.MultiSelectListPreference;
import treebolic.provider.contacts.Provider;

/**
 * Group preference
 *
 * @author <a href="mailto:1313ou@gmail.com">Bernard Bou</a>
 */
@SuppressWarnings("WeakerAccess")
public class GroupPreference extends MultiSelectListPreference
{
	public final List<String> accountNames = new ArrayList<>();

	public final List<String> accountTypes = new ArrayList<>();

	public final List<Long> counts = new ArrayList<>();

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public GroupPreference(@NonNull final Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes)
	{
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public GroupPreference(@NonNull final Context context, @Nullable AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		init(context);
	}

	public GroupPreference(@NonNull final Context context, @Nullable AttributeSet attrs)
	{
		super(context, attrs);
		init(context);
	}

	public GroupPreference(@NonNull final Context context)
	{
		super(context);
		init(context);
	}

	private void init(@NonNull Context context)
	{
		final String sort = ContactsContract.Groups.TITLE + " COLLATE LOCALIZED ASC";

		final ContentResolver contentResolver = context.getContentResolver();
		final Cursor cursor = contentResolver.query( //
				ContactsContract.Groups.CONTENT_URI, //
				treebolic.provider.contacts.Settings.GROUP_PROJECTION, //
				null, //
				null, //
				sort);

		final List<String> labels = new ArrayList<>();
		final List<String> values = new ArrayList<>();
		if (cursor != null)
		{
			if (cursor.moveToFirst())
			{
				do
				{
					final String groupId = cursor.getString(treebolic.provider.contacts.Settings.GROUP_ID_INDEX);
					CharSequence title = cursor.getString(treebolic.provider.contacts.Settings.GROUP_TITLE_INDEX);

					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
					{
						final String titleRes = cursor.getString(treebolic.provider.contacts.Settings.GROUP_TITLE_RES_INDEX);
						if (titleRes != null)
						{
							int resId = Integer.parseInt(titleRes);
							final String resPackage = cursor.getString(treebolic.provider.contacts.Settings.GROUP_RES_PACKAGE_INDEX);
							if (resPackage != null)
							{
								title = context.getPackageManager().getText(resPackage, resId, null);
							}
						}
					}

					if (title != null && "Starred in Android".contentEquals(title))
					{
						title = "Starred";
					}
					final String accountName = cursor.getString(treebolic.provider.contacts.Settings.GROUP_ACCOUNT_NAME);
					final String accountType = cursor.getString(treebolic.provider.contacts.Settings.GROUP_ACCOUNT_TYPE);
					Log.d("group ", groupId + " " + title + " " + accountName + " " + accountType);

					labels.add(title == null ? "?" : title.toString());
					values.add(groupId);
					this.accountNames.add(accountName);
					this.accountTypes.add(accountType);
					this.counts.add(Provider.countGroupMembers(context, groupId, null));
				}
				while (cursor.moveToNext());
			}
			cursor.close();
		}

		setEntries(labels.toArray(new String[0]));
		setEntryValues(values.toArray(new String[0]));
	}
}
