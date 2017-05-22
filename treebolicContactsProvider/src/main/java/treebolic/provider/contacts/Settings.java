package treebolic.provider.contacts;

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

	// P R O J E C T I O N

	static public final String[] PROJECTION = {//
			ContactsContract.Contacts._ID, //
			ContactsContract.Contacts.LOOKUP_KEY, //
			ContactsContract.Contacts.DISPLAY_NAME, //
			ContactsContract.Contacts.DISPLAY_NAME_ALTERNATIVE, //
			ContactsContract.Contacts.PHOTO_ID, // Reference to the row in the data table holding the photo.
			ContactsContract.Contacts.PHOTO_FILE_ID, // Photo file ID of the full-size photo.
			ContactsContract.Contacts.PHOTO_THUMBNAIL_URI, // A URI that can be used to retrieve a thumbnail of the contact's photo.
			ContactsContract.Contacts.PHOTO_URI, // A URI that can be used to retrieve the contact's full-size photo.
	};

	// The column index for the _ID column
	static public final int ID_INDEX = 0;

	// The column index for the LOOKUP_KEY column
	static public final int LOOKUP_KEY_INDEX = 1;

	// The column index for the NAME column
	static public final int NAME_INDEX = 2;

	// The column index for the NAME column
	static public final int ALT_NAME_INDEX = 3;

	// The column index for the photo thumbnail id column
	static public final int PHOTO_THUMBNAIL_ID_INDEX = 4;

	// The column index for the photo id column
	static public final int PHOTO_ID_INDEX = 5;

	// The column index for the photo thumbnail uri column
	static public final int PHOTO_THUMBNAIL_URI_INDEX = 6;

	// The column index for the photo uri column
	static public final int PHOTO_URI_INDEX = 7;

	// S E L E C T I O N

	/**
	 * Selection expressions
	 */
	static public final String SELECTION = ContactsContract.Contacts.DISPLAY_NAME + " LIKE ?";
	static public final String SELECTION_EQUALS = ContactsContract.Contacts.DISPLAY_NAME + " = ?";

	/**
	 * Search string (first selection arg)
	 */
	static public String searchString = "";

	/**
	 * Array to hold values that replace the ?
	 */
	static public String[] selectionArgs = {searchString};

	// S O R T

	static public String sortOrder = ContactsContract.Contacts.DISPLAY_NAME_ALTERNATIVE + " COLLATE LOCALIZED ASC";
}
