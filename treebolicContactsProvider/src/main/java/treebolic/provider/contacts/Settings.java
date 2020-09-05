/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>
 */

package treebolic.provider.contacts;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

/**
 * Settings
 *
 * @author <a href="mailto:1313ou@gmail.com">Bernard Bou</a>
 */
public class Settings
{
	/**
	 * URLSCHEME that is handled
	 */
	static final public String URLSCHEME = "contact:";

	// C O U N T    P R O J E C T I O N

	// static final public String COUNT_COL = "count";

	// static public final String[] COUNT_PROJECTION = {//
	//		"count(*) AS " + COUNT_COL};

	// C O N T A C T   P R O J E C T I O N

	static public final Uri CONTACT_URI = ContactsContract.Contacts.CONTENT_URI;
	static public final String[] BASE_CONTACT_PROJECTION = {//
			ContactsContract.Contacts._ID, //
	};
	static public final String[] CONTACT_PROJECTION = {//
			ContactsContract.Contacts._ID, //
			ContactsContract.Contacts.DISPLAY_NAME, //
			ContactsContract.Contacts.DISPLAY_NAME_ALTERNATIVE, //
			ContactsContract.Contacts.PHOTO_THUMBNAIL_URI, // A URI that can be used to retrieve a thumbnail of the contact's photo.
			ContactsContract.Contacts.PHOTO_URI, // A URI that can be used to retrieve the contact's full-size photo.
			ContactsContract.Contacts.PHOTO_ID, // Reference to the row in the data table holding the photo.
			ContactsContract.Contacts.PHOTO_FILE_ID, // Photo file ID of the full-size photo.
			ContactsContract.Contacts.LOOKUP_KEY, //
	};

	static public final int CONTACT_ID_INDEX = 0;
	static public final int DISPLAY_NAME_INDEX = 1;
	static public final int DISPLAY_NAME_ALTERNATIVE_INDEX = 2;
	static public final int PHOTO_THUMBNAIL_URI_INDEX = 3;
	static public final int PHOTO_URI_INDEX = 4;
	static public final int PHOTO_THUMBNAIL_ID_INDEX = 5;
	static public final int PHOTO_ID_INDEX = 6;
	//static public final int LOOKUP_KEY_INDEX = 7;

	// G R O U P  M E M B E R  P R O J E C T I O N

	static public final Uri GROUP_MEMBERS_URI = ContactsContract.Data.CONTENT_URI;

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	static public final String[] BASE_GROUP_MEMBERS_PROJECTION = //
			new String[]{ //
					ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID, // contact
			};

	static public final String[] GROUP_MEMBERS_PROJECTION = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? //
			new String[]{ //
					ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID, // contact
					ContactsContract.CommonDataKinds.GroupMembership.DISPLAY_NAME, //
					ContactsContract.CommonDataKinds.GroupMembership.DISPLAY_NAME_ALTERNATIVE, //
					ContactsContract.CommonDataKinds.GroupMembership.PHOTO_THUMBNAIL_URI, //
					ContactsContract.CommonDataKinds.GroupMembership.PHOTO_URI, //
					ContactsContract.CommonDataKinds.GroupMembership.PHOTO_ID, //
					ContactsContract.CommonDataKinds.GroupMembership.PHOTO_FILE_ID, //
					ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID, //
					ContactsContract.CommonDataKinds.GroupMembership.RAW_CONTACT_ID, // raw contact id
					ContactsContract.CommonDataKinds.GroupMembership._ID, // membership row
					ContactsContract.CommonDataKinds.GroupMembership.ACCOUNT_TYPE_AND_DATA_SET,} : // account
			new String[]{ //
					ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID, // contact
					ContactsContract.CommonDataKinds.GroupMembership.DISPLAY_NAME, //
					ContactsContract.CommonDataKinds.GroupMembership.DISPLAY_NAME_ALTERNATIVE, //
					ContactsContract.CommonDataKinds.GroupMembership.PHOTO_THUMBNAIL_URI, //
					ContactsContract.CommonDataKinds.GroupMembership.PHOTO_URI, //
					ContactsContract.CommonDataKinds.GroupMembership.PHOTO_ID, //
					ContactsContract.CommonDataKinds.GroupMembership.PHOTO_FILE_ID, //
					ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID, //
					ContactsContract.CommonDataKinds.GroupMembership.RAW_CONTACT_ID, // raw contact id
					ContactsContract.CommonDataKinds.GroupMembership._ID,}; // membership row

	static public final int GROUP_MEMBERS_CONTACT_ID_INDEX = 0;
	static public final int GROUP_MEMBERS_DISPLAY_NAME_INDEX = 1;
	static public final int GROUP_MEMBERS_DISPLAY_NAME_ALTERNATIVE_INDEX = 2;
	static public final int GROUP_MEMBERS_PHOTO_THUMBNAIL_URI_INDEX = 3;
	//static public final int GROUP_MEMBERS_PHOTO_URI_INDEX = 4;
	//static public final int GROUP_MEMBERS_PHOTO_ID_INDEX = 5;
	//static public final int GROUP_MEMBERS_PHOTO_FILE_ID_INDEX = 6;
	static public final int GROUP_MEMBERS_GROUP_ID_INDEX = 7;

	// G R O U P  P R O J E C T I O N

	static public final Uri GROUP_URI = ContactsContract.Groups.CONTENT_URI;

	static public final String[] BASE_GROUP_PROJECTION = new String[]{ //
			ContactsContract.Groups._ID,};

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	static public final String[] GROUP_PROJECTION = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? //
			new String[]{ //
					ContactsContract.Groups._ID, //
					ContactsContract.Groups.SYSTEM_ID, //
					ContactsContract.Groups.TITLE, //
					ContactsContract.Groups.ACCOUNT_NAME, //
					ContactsContract.Groups.ACCOUNT_TYPE, //
					ContactsContract.Groups.TITLE_RES, //
					ContactsContract.Groups.RES_PACKAGE,} : //
			new String[]{ //
					ContactsContract.Groups._ID, //
					ContactsContract.Groups.SYSTEM_ID, //
					ContactsContract.Groups.TITLE, //
					ContactsContract.Groups.ACCOUNT_NAME, //
					ContactsContract.Groups.ACCOUNT_TYPE,};

	static public final int GROUP_ID_INDEX = 0;
	static public final int GROUP_SYSTEM_ID_INDEX = 1;
	static public final int GROUP_TITLE_INDEX = 2;
	static public final int GROUP_ACCOUNT_NAME = 3;
	static public final int GROUP_ACCOUNT_TYPE = 4;
	static public final int GROUP_TITLE_RES_INDEX = 5;
	static public final int GROUP_RES_PACKAGE_INDEX = 6;

	// S E L E C T I O N

	/**
	 * Selection expressions
	 */
	static public final String SELECTION_DN_LIKE = ContactsContract.Contacts.DISPLAY_NAME + " LIKE ?";

	// H E L P E R

	/**
	 * Get string preference
	 *
	 * @param context context
	 * @param key     key
	 * @return value
	 */
	@Nullable
	static public String getStringPref(final Context context, final String key)
	{
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPref.getString(key, null);
	}

	/**
	 * Get boolean preference
	 *
	 * @param context context
	 * @param key     key
	 * @return value
	 */
	static public boolean getBooleanPref(final Context context, final String key)
	{
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPref.getBoolean(key, false);
	}

	// P A R A M E T E R S

	static public final String PARAM_GROUPS = "groups";
	static public final String PARAM_DISPLAY = "display";
	static public final String PARAM_SORT = "sort";
	static public final String PARAM_MERGE_GROUPS = "merge_groups";
	static public final String PARAM_FILTER_GROUPS = "filter_groups";
	static public final String PARAM_PHOTO = "photo";

	static public final String VALUE_DISPLAY_FIRST_LAST = "first,last";
	static public final String VALUE_DISPLAY_LAST_FIRST = "last,first";
	static public final String VALUE_SORT_FIRST = "first";
	static public final String VALUE_SORT_LAST = "last";
}

