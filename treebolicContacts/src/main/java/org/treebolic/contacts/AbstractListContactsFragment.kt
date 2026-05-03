/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.contacts

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import org.treebolic.makeDialog

/**
 * Abstract fragment for list of contacts
 *
 * @author [Bernard Bou](mailto:1313ou@gmail.com)
 */
abstract class AbstractListContactsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    // D I A L O G

    /**
     * Show dialog for contact info
     *
     * @param sb        string buffer
     * @param contactId contact id
     * @param photoUri  photo uri
     * @param labels    labels
     * @param values    values
     */
    fun showDialog(sb: XSpannableStringBuilder, contactId: String, photoUri: Uri?, labels: Array<String>, vararg values: String?) {
        // id
        sb.append("ID", StyleSpan(Typeface.BOLD), 0)
            .append('\n')
            .append(contactId).append('\n')

        // extra info
        getNames(contactId, sb)
        getPhones(contactId, sb)
        getEmails(contactId, sb)
        getAddresses(contactId, sb)

        // info
        for (i in labels.indices) {
            val label = labels[i]
            val value = values[i]

            sb.append(label, StyleSpan(Typeface.BOLD), 0)
                .append('\n')
                .append(value ?: "null")
                .append('\n')
        }

        // view
        @SuppressLint("InflateParams") val dialogView = layoutInflater.inflate(R.layout.detail_contact, null)
        val textView = dialogView.findViewById<TextView>(android.R.id.text1)
        textView.text = sb

        val imageView = dialogView.findViewById<ImageView>(R.id.photo)
        if (photoUri != null) {
            imageView.setImageURI(photoUri)
        } else {
            imageView.setImageResource(R.drawable.ic_contact_large)
        }

        makeDialog(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ -> }
            .show()
    }

    // M O R E

    /**
     * Get contact names
     *
     * @param contactId contact id
     * @param sb        string buffer
     */
    private fun getNames(contactId: String, sb: XSpannableStringBuilder) {
        val context = requireContext()
        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            null,
            ContactsContract.Data.MIMETYPE + " = ?" + " AND " + ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID + " = ?",
            arrayOf(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE, contactId),
            null
        )
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val idxGiven = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME) // first
                val idxFamily = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME) // last
                val idxDisplay = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME) // display

                sb.append("names", StyleSpan(Typeface.BOLD), 0)
                    .append('\n')
                do {
                    val given = cursor.getString(idxGiven)
                    if (given != null && given.isNotEmpty()) {
                        sb.append(given)
                            .append('\n')
                    }
                    val family = cursor.getString(idxFamily)
                    if (family != null && family.isNotEmpty()) {
                        sb.append(family)
                            .append('\n')
                    }
                    val display = cursor.getString(idxDisplay)
                    if (display != null && display.isNotEmpty()) {
                        sb.append(display)
                            .append('\n')
                    }
                } while (cursor.moveToNext())
            }
            cursor.close()
        }
    }

    /**
     * Get contact addresses
     *
     * @param contactId contact id
     * @param sb        string buffer
     */
    private fun getAddresses(contactId: String, sb: XSpannableStringBuilder) {
        val context = requireContext()
        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            null,
            ContactsContract.Data.MIMETYPE + " = ?" + " AND " + ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID + " = ?",
            arrayOf(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE, contactId),
            null
        )
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val idxFormatted = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS)
                val idxStreet = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET)
                val idxCity = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY)
                val idxPostCode = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE)
                val idxCountry = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY)
                do {
                    sb.append("address", StyleSpan(Typeface.BOLD), 0)
                        .append('\n')

                    val formatted = cursor.getString(idxFormatted)
                    if (formatted != null && formatted.isNotEmpty()) {
                        sb.append(formatted)
                            .append(" (formatted)\n")
                    }
                    val street = cursor.getString(idxStreet)
                    if (street != null && street.isNotEmpty()) {
                        sb.append(street)
                            .append(" (street)\n")
                    }
                    val city = cursor.getString(idxCity)
                    if (city != null && city.isNotEmpty()) {
                        sb.append(city)
                            .append(" (city)\n")
                    }
                    val postCode = cursor.getString(idxPostCode)
                    if (postCode != null && postCode.isNotEmpty()) {
                        sb.append(postCode)
                            .append(" (postcode)\n")
                    }
                    val country = cursor.getString(idxCountry)
                    if (country != null && country.isNotEmpty()) {
                        sb.append(country)
                            .append(" (country)\n")
                    }
                } while (cursor.moveToNext())
            }
            cursor.close()
        }
    }

    /**
     * Get contact emails
     *
     * @param contactId contact id
     * @param sb        string buffer
     */
    private fun getEmails(contactId: String, sb: XSpannableStringBuilder) {
        val context = requireContext()
        val cr = context.contentResolver
        val cursor = cr.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            null,
            ContactsContract.Data.MIMETYPE + " = ?" + " AND " + ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
            arrayOf(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE, contactId),
            null
        )
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val idxEmail = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)

                sb.append("email", StyleSpan(Typeface.BOLD), 0)
                    .append('\n')
                do {
                    val email = cursor.getString(idxEmail)
                    if (email != null && email.isNotEmpty()) {
                        sb.append(email)
                            .append('\n')
                    }
                } while (cursor.moveToNext())
            }
            cursor.close()
        }
    }

    /**
     * Get contact phones
     *
     * @param contactId contact id
     * @param sb        string buffer
     */
    private fun getPhones(contactId: String, sb: XSpannableStringBuilder) {
        val context = requireContext()
        val cr = context.contentResolver
        val cursor = cr.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            ContactsContract.Data.MIMETYPE + " = ?" + " AND " + ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
            arrayOf(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE, contactId),
            null
        )
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val idxPhoneNumber = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val idxPhoneType = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)
                sb.append("phone", StyleSpan(Typeface.BOLD), 0)
                    .append('\n')
                do {
                    val phoneNumber = if (idxPhoneNumber == -1) "" else cursor.getString(idxPhoneNumber)
                    val phoneType = if (idxPhoneNumber == -1) -1 else cursor.getInt(idxPhoneType)
                    if (phoneNumber != null && phoneNumber.isNotEmpty()) {
                        // phone
                        when (phoneType) {
                            ContactsContract.CommonDataKinds.Phone.TYPE_HOME -> sb
                                .append(phoneNumber)
                                .append(" (home)\n")

                            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE -> sb
                                .append(phoneNumber)
                                .append(" (mobile)\n")

                            ContactsContract.CommonDataKinds.Phone.TYPE_WORK -> sb
                                .append(phoneNumber)
                                .append(" (work)\n")

                            -1 -> sb
                                .append(phoneNumber)
                                .append(" (other)\n")

                            else -> sb
                                .append(phoneNumber)
                                .append(" (other)\n")
                        }
                    }
                } while (cursor.moveToNext())
            }
            cursor.close()
        }
    }

    // U T I L S

    class XSpannableStringBuilder : SpannableStringBuilder() {

        override fun append(text: CharSequence, what: Any, flags: Int): SpannableStringBuilder {
            val start = length
            append(text)
            setSpan(what, start, length, flags)
            return this
        }
    }
}