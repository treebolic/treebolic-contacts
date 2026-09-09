/*
 * Copyright (c) 2026. Bernard Bou
 */
package treebolic.provider.contacts

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import androidx.preference.PreferenceManager

/**
 * Settings
 *
 * @author [Bernard Bou](mailto:1313ou@gmail.com)
 */
object Settings {

    /**
     * URLSCHEME that is handled
     */
    const val URLSCHEME: String = "contact:"

    // C O N T A C T   P R O J E C T I O N

    @JvmField
    val CONTACT_URI: Uri = ContactsContract.Contacts.CONTENT_URI
    val BASE_CONTACT_PROJECTION: Array<String> = arrayOf(
        ContactsContract.Contacts._ID,
    )

    @JvmField
    val CONTACT_PROJECTION: Array<String> = arrayOf(
        ContactsContract.Contacts._ID,
        ContactsContract.Contacts.DISPLAY_NAME,
        ContactsContract.Contacts.DISPLAY_NAME_ALTERNATIVE,
        ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,  // A URI that can be used to retrieve a thumbnail of the contact's photo.
        ContactsContract.Contacts.PHOTO_URI,  // A URI that can be used to retrieve the contact's full-size photo.
        ContactsContract.Contacts.PHOTO_ID,  // Reference to the row in the data table holding the photo.
        ContactsContract.Contacts.PHOTO_FILE_ID,  // Photo file ID of the full-size photo.
        ContactsContract.Contacts.LOOKUP_KEY,
    )

    const val CONTACT_ID_INDEX: Int = 0
    const val DISPLAY_NAME_INDEX: Int = 1
    const val DISPLAY_NAME_ALTERNATIVE_INDEX: Int = 2
    const val PHOTO_THUMBNAIL_URI_INDEX: Int = 3
    const val PHOTO_URI_INDEX: Int = 4
    const val PHOTO_THUMBNAIL_ID_INDEX: Int = 5
    const val PHOTO_ID_INDEX: Int = 6

    // G R O U P   M E M B E R   P R O J E C T I O N

    @JvmField
    val GROUP_MEMBERS_URI: Uri = ContactsContract.Data.CONTENT_URI

    val BASE_GROUP_MEMBERS_PROJECTION: Array<String> = arrayOf(
        ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID,  // contact
    )

    @JvmField
    val GROUP_MEMBERS_PROJECTION: Array<String> = arrayOf(
        ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID,  // contact
        ContactsContract.CommonDataKinds.GroupMembership.DISPLAY_NAME,
        ContactsContract.CommonDataKinds.GroupMembership.DISPLAY_NAME_ALTERNATIVE,
        ContactsContract.CommonDataKinds.GroupMembership.PHOTO_THUMBNAIL_URI,
        ContactsContract.CommonDataKinds.GroupMembership.PHOTO_URI,
        ContactsContract.CommonDataKinds.GroupMembership.PHOTO_ID,
        ContactsContract.CommonDataKinds.GroupMembership.PHOTO_FILE_ID,
        ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID,
        ContactsContract.CommonDataKinds.GroupMembership.RAW_CONTACT_ID,  // raw contact id
        ContactsContract.CommonDataKinds.GroupMembership._ID,  // membership row
        ContactsContract.CommonDataKinds.GroupMembership.ACCOUNT_TYPE_AND_DATA_SET,
    )

    // membership row

    const val GROUP_MEMBERS_CONTACT_ID_INDEX: Int = 0
    const val GROUP_MEMBERS_DISPLAY_NAME_INDEX: Int = 1
    const val GROUP_MEMBERS_DISPLAY_NAME_ALTERNATIVE_INDEX: Int = 2
    const val GROUP_MEMBERS_PHOTO_THUMBNAIL_URI_INDEX: Int = 3

    const val GROUP_MEMBERS_GROUP_ID_INDEX: Int = 7

    // G R O U P   P R O J E C T I O N

    @JvmField
    val GROUP_URI: Uri = ContactsContract.Groups.CONTENT_URI

    val BASE_GROUP_PROJECTION: Array<String> = arrayOf(

        ContactsContract.Groups._ID,
    )

    @JvmField
    val GROUP_PROJECTION: Array<String> = arrayOf(

        ContactsContract.Groups._ID,
        ContactsContract.Groups.SYSTEM_ID,
        ContactsContract.Groups.TITLE,
        ContactsContract.Groups.ACCOUNT_NAME,
        ContactsContract.Groups.ACCOUNT_TYPE,
        ContactsContract.Groups.TITLE_RES,
        ContactsContract.Groups.RES_PACKAGE,
    )

    const val GROUP_ID_INDEX: Int = 0
    const val GROUP_SYSTEM_ID_INDEX: Int = 1
    const val GROUP_TITLE_INDEX: Int = 2
    const val GROUP_ACCOUNT_NAME: Int = 3
    const val GROUP_ACCOUNT_TYPE: Int = 4
    const val GROUP_TITLE_RES_INDEX: Int = 5
    const val GROUP_RES_PACKAGE_INDEX: Int = 6

    // S E L E C T I O N

    /**
     * Selection expressions
     */
    const val SELECTION_DN_LIKE: String = ContactsContract.Contacts.DISPLAY_NAME + " LIKE ?"

    // H E L P E R

    /**
     * Get string preference
     *
     * @param context context
     * @param key     key
     * @return value
     */
    @JvmStatic
    fun getStringPref(context: Context, key: String?): String? {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPref.getString(key, null)
    }

    /**
     * Get boolean preference
     *
     * @param context context
     * @param key     key
     * @return value
     */
    @JvmStatic
    fun getBooleanPref(context: Context, key: String?): Boolean {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPref.getBoolean(key, false)
    }

    // P A R A M E T E R S

    const val PARAM_GROUPS: String = "groups"
    const val PARAM_DISPLAY: String = "display"
    const val PARAM_SORT: String = "sort"
    const val PARAM_MERGE_GROUPS: String = "merge_groups"
    const val PARAM_FILTER_GROUPS: String = "filter_groups"
    const val PARAM_PHOTO: String = "photo"

    const val VALUE_DISPLAY_FIRST_LAST: String = "first,last"
    const val VALUE_DISPLAY_LAST_FIRST: String = "last,first"
    const val VALUE_SORT_FIRST: String = "first"
    const val VALUE_SORT_LAST: String = "last"
}

