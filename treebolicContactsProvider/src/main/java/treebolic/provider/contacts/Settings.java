package treebolic.provider.contacts;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;

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

	// C O N T A C T   P R O J E C T I O N

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

	static public final int ID_INDEX = 0;
	static public final int DISPLAY_NAME_INDEX = 1;
	static public final int DISPLAY_NAME_ALTERNATIVE_INDEX = 2;
	static public final int PHOTO_THUMBNAIL_URI_INDEX = 3;
	static public final int PHOTO_URI_INDEX = 4;
	static public final int PHOTO_THUMBNAIL_ID_INDEX = 5;
	static public final int PHOTO_ID_INDEX = 6;
	static public final int LOOKUP_KEY_INDEX = 7;

	// G R O U P  M E M B E R  P R O J E C T I O N

	static public final String[] GROUP_MEMBERS_PROJECTION = new String[]{ //
			ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID, //
			ContactsContract.CommonDataKinds.GroupMembership.DISPLAY_NAME, //
			ContactsContract.CommonDataKinds.GroupMembership.DISPLAY_NAME_ALTERNATIVE, //
			ContactsContract.CommonDataKinds.GroupMembership.PHOTO_THUMBNAIL_URI, //
			ContactsContract.CommonDataKinds.GroupMembership.PHOTO_URI, //
			ContactsContract.CommonDataKinds.GroupMembership.PHOTO_ID, //
			ContactsContract.CommonDataKinds.GroupMembership.PHOTO_FILE_ID, //
			ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID, //
			ContactsContract.CommonDataKinds.GroupMembership._ID, //
	};

	static public final int GROUP_MEMBERS_ID_INDEX = 0;
	static public final int GROUP_MEMBERS_DISPLAY_NAME_INDEX = 1;
	static public final int GROUP_MEMBERS_DISPLAY_NAME_ALTERNATIVE_INDEX = 2;
	static public final int GROUP_MEMBERS_PHOTO_THUMBNAIL_URI_INDEX = 3;
	static public final int GROUP_MEMBERS_PHOTO_URI_INDEX = 4;
	static public final int GROUP_MEMBERS_PHOTO_ID_INDEX = 5;
	static public final int GROUP_MEMBERS_PHOTO_FILE_ID_INDEX = 6;
	static public final int GROUP_MEMBERS_GROUP_ID_INDEX = 7;

	// G R O U P  P R O J E C T I O N

	static public final String[] GROUP_PROJECTION = new String[]{ //
			ContactsContract.Groups._ID, //
			ContactsContract.Groups.SYSTEM_ID, //
			ContactsContract.Groups.TITLE, //
			ContactsContract.Groups.ACCOUNT_NAME, //
			ContactsContract.Groups.ACCOUNT_TYPE, //
			ContactsContract.Groups.TITLE_RES, //
			ContactsContract.Groups.RES_PACKAGE, //
	};

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

	static public String PARAM_GROUPS = "groups";
	static public String PARAM_DISPLAY = "display";
	static public String PARAM_SORT = "sort";
	static public String PARAM_MERGE_GROUPS = "merge_groups";
	static public String PARAM_FILTER_GROUPS = "filter_groups";

	static public String VALUE_DISPLAY_FIRST_LAST = "first,last";
	static public String VALUE_DISPLAY_LAST_FIRST = "last,first";
	static public String VALUE_SORT_FIRST = "first";
	static public String VALUE_SORT_LAST = "last";
}

