/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.contacts

import android.content.Context
import android.provider.ContactsContract
import android.util.AttributeSet
import android.util.Log
import androidx.preference.MultiSelectListPreference
import treebolic.provider.contacts.Provider.Companion.countGroupMembers
import treebolic.provider.contacts.Settings

/**
 * Group preference
 *
 * @author [Bernard Bou](mailto:1313ou@gmail.com)
 */
class GroupPreference : MultiSelectListPreference {

    @JvmField
    val accountNames: MutableList<String?> = ArrayList()

    @JvmField
    val accountTypes: MutableList<String?> = ArrayList()

    @JvmField
    val counts: MutableList<Long> = ArrayList()

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context) : super(context) {
        init(context)
    }

    private fun init(context: Context) {
        val sort = ContactsContract.Groups.TITLE + " COLLATE LOCALIZED ASC"

        val contentResolver = context.contentResolver
        val cursor = contentResolver.query( //
            ContactsContract.Groups.CONTENT_URI,  //
            Settings.GROUP_PROJECTION,  //
            null,  //
            null,  //
            sort
        )

        val labels: MutableList<String> = ArrayList()
        val values: MutableList<String> = ArrayList()
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val groupId = cursor.getString(Settings.GROUP_ID_INDEX)
                    var title: CharSequence? = cursor.getString(Settings.GROUP_TITLE_INDEX)

                    val titleRes = cursor.getString(Settings.GROUP_TITLE_RES_INDEX)
                    if (titleRes != null) {
                        val resId = titleRes.toInt()
                        val resPackage = cursor.getString(Settings.GROUP_RES_PACKAGE_INDEX)
                        if (resPackage != null) {
                            title = context.packageManager.getText(resPackage, resId, null)
                        }
                    }

                    if (title != null && "Starred in Android".contentEquals(title)) {
                        title = "Starred"
                    }
                    val accountName = cursor.getString(Settings.GROUP_ACCOUNT_NAME)
                    val accountType = cursor.getString(Settings.GROUP_ACCOUNT_TYPE)
                    Log.d("group ", "$groupId $title $accountName $accountType")

                    labels.add(title?.toString() ?: "?")
                    values.add(groupId)
                    accountNames.add(accountName)
                    accountTypes.add(accountType)
                    counts.add(countGroupMembers(context, groupId, null))
                } while (cursor.moveToNext())
            }
            cursor.close()
        }

        entries = labels.toTypedArray<String>()
        entryValues = values.toTypedArray<String>()
    }
}
