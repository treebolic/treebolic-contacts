package org.treebolic.contacts;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Abstract fragment for list of contacts
 *
 * @author <a href="mailto:1313ou@gmail.com">Bernard Bou</a>
 */
abstract public class AbstractListContactsFragment extends Fragment
{
	// --Commented out by Inspection (9/23/17 5:48 PM):static private final String TAG = "AListContactsF";

	// C R E A T E

	/**
	 * Constructor
	 */
	public AbstractListContactsFragment()
	{
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_list, container, false);
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
	void showDialog(@NonNull final XSpannableStringBuilder sb, final String contactId, @Nullable final Uri photoUri, @NonNull final String[] labels, final String... values)
	{
		// id
		sb.append("ID", new StyleSpan(Typeface.BOLD), 0) //
				.append('\n') //
				.append(contactId).append('\n');

		// extra info
		getNames(contactId, sb);
		getPhones(contactId, sb);
		getEmails(contactId, sb);
		getAddresses(contactId, sb);

		// info
		for (int i = 0; i < labels.length; i++)
		{
			String label = labels[i];
			String value = values[i];

			sb.append(label, new StyleSpan(Typeface.BOLD), 0) //
					.append('\n') //
					.append(value == null ? "null" : value) //
					.append('\n');
		}

		// view
		final Activity activity = getActivity();
		assert activity != null;
		final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		final LayoutInflater inflater = activity.getLayoutInflater();
		@SuppressLint("InflateParams") final View dialogView = inflater.inflate(R.layout.detail_contact, null);
		final TextView textView = dialogView.findViewById(android.R.id.text1);
		final ImageView imageView = dialogView.findViewById(R.id.photo);
		textView.setText(sb);
		if (photoUri != null)
		{
			imageView.setImageURI(photoUri);
		}
		else
		{
			imageView.setImageResource(R.drawable.ic_contact_large);
		}

		builder.setView(dialogView).setCancelable(false).setPositiveButton("OK", (dialog, id) ->
		{
			//do things
		});
		builder.show();
	}

	// M O R E

	/**
	 * Get contact names
	 *
	 * @param contactId contact id
	 * @param sb        string buffer
	 */
	private void getNames(final String contactId, @NonNull final XSpannableStringBuilder sb)
	{
		final Context context = getContext();
		assert context != null;
		final ContentResolver contentResolver = context.getContentResolver();
		final Cursor cursor = contentResolver.query(ContactsContract.Data.CONTENT_URI, null, ContactsContract.Data.MIMETYPE + " = ?" + " AND " + ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID + " = ?", new String[]{ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE, contactId}, null);
		if (cursor != null)
		{
			if (cursor.moveToFirst())
			{
				final int idxGiven = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME); // first
				final int idxFamily = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME); // last
				final int idxDisplay = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME); // display

				sb.append("names", new StyleSpan(Typeface.BOLD), 0) //
						.append('\n');
				do
				{
					final String given = cursor.getString(idxGiven);
					if (given != null && !given.isEmpty())
					{
						sb.append(given) //
								.append('\n');
					}
					final String family = cursor.getString(idxFamily);
					if (family != null && !family.isEmpty())
					{
						sb.append(family) //
								.append('\n');
					}
					final String display = cursor.getString(idxDisplay);
					if (display != null && !display.isEmpty())
					{
						sb.append(display) //
								.append('\n');
					}
				}
				while (cursor.moveToNext());
			}
			cursor.close();
		}
	}

	/**
	 * Get contact addresses
	 *
	 * @param contactId contact id
	 * @param sb        string buffer
	 */
	private void getAddresses(final String contactId, @NonNull final XSpannableStringBuilder sb)
	{
		final Context context = getContext();
		assert context != null;
		final ContentResolver contentResolver = context.getContentResolver();
		final Cursor cursor = contentResolver.query(ContactsContract.Data.CONTENT_URI, null, ContactsContract.Data.MIMETYPE + " = ?" + " AND " + ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID + " = ?", new String[]{ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE, contactId}, null);
		if (cursor != null)
		{
			if (cursor.moveToFirst())
			{
				final int idxFormatted = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS);
				final int idxStreet = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET);
				final int idxCity = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY);
				final int idxPostCode = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE);
				final int idxCountry = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY);
				do
				{
					sb.append("address", new StyleSpan(Typeface.BOLD), 0) //
							.append('\n');

					final String formatted = cursor.getString(idxFormatted);
					if (formatted != null && !formatted.isEmpty())
					{
						sb.append(formatted) //
								.append(" (formatted)\n");
					}
					final String street = cursor.getString(idxStreet);
					if (street != null && !street.isEmpty())
					{
						sb.append(street) //
								.append(" (street)\n");
					}
					final String city = cursor.getString(idxCity);
					if (city != null && !city.isEmpty())
					{
						sb.append(city) //
								.append(" (city)\n");
					}
					final String postCode = cursor.getString(idxPostCode);
					if (postCode != null && !postCode.isEmpty())
					{
						sb.append(postCode) //
								.append(" (postcode)\n");
					}
					final String country = cursor.getString(idxCountry);
					if (country != null && !country.isEmpty())
					{
						sb.append(country) //
								.append(" (country)\n");
					}
				}
				while (cursor.moveToNext());
			}
			cursor.close();
		}
	}

	/**
	 * Get contact emails
	 *
	 * @param contactId contact id
	 * @param sb        string buffer
	 */
	private void getEmails(final String contactId, @NonNull final XSpannableStringBuilder sb)
	{
		final Context context = getContext();
		assert context != null;
		final ContentResolver cr = context.getContentResolver();
		final Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, ContactsContract.Data.MIMETYPE + " = ?" + " AND " + ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[]{ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE, contactId}, null);
		if (cursor != null)
		{
			if (cursor.moveToFirst())
			{
				final int idxEmail = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);

				sb.append("email", new StyleSpan(Typeface.BOLD), 0) //
						.append('\n');
				do
				{
					final String email = cursor.getString(idxEmail);
					if (email != null && !email.isEmpty())
					{
						sb.append(email) //
								.append('\n');
					}
				}
				while (cursor.moveToNext());
			}
			cursor.close();
		}
	}

	/**
	 * Get contact phones
	 *
	 * @param contactId contact id
	 * @param sb        string buffer
	 */
	private void getPhones(String contactId, @NonNull final XSpannableStringBuilder sb)
	{
		final Context context = getContext();
		assert context != null;
		final ContentResolver cr = context.getContentResolver();
		final Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.Data.MIMETYPE + " = ?" + " AND " + ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE, contactId}, null);
		if (cursor != null)
		{
			if (cursor.moveToFirst())
			{
				final int idxPhoneNumber = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
				final int idxPhoneType = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
				sb.append("phone", new StyleSpan(Typeface.BOLD), 0) //
						.append('\n');
				do
				{
					final String phoneNumber = idxPhoneNumber == -1 ? "" : cursor.getString(idxPhoneNumber);
					final int phoneType = idxPhoneNumber == -1 ? -1 : cursor.getInt(idxPhoneType);
					if (phoneNumber != null && !phoneNumber.isEmpty())
					{
						// phone
						switch (phoneType)
						{
							case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
								sb.append(phoneNumber) //
										.append(" (home)\n");
								break;
							case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
								sb.append(phoneNumber) //
										.append(" (mobile)\n");
								break;
							case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
								sb.append(phoneNumber) //
										.append(" (work)\n");
								break;
							default:
							case -1:
								sb.append(phoneNumber) //
										.append(" (other)\n");
								break;
						}
					}
				}
				while (cursor.moveToNext());
			}
			cursor.close();
		}
	}

	// U T I L S

	static public class XSpannableStringBuilder extends SpannableStringBuilder
	{
		@NonNull
		public SpannableStringBuilder append(CharSequence text, Object what, int flags)
		{
			int start = length();
			append(text);
			setSpan(what, start, length(), flags);
			return this;
		}
	}

	// H E L P E R S

	// --Commented out by Inspection START (9/23/17 5:48 PM):
	//	private void str2sb(final String header, final String[] strings, final XSpannableStringBuilder sb)
	//	{
	//		sb.append(header, new StyleSpan(Typeface.BOLD), 0).append('\n');
	//		sb.append(join(strings)).append('\n');
	//	}
	// --Commented out by Inspection STOP (9/23/17 5:48 PM)

	// --Commented out by Inspection START (9/23/17 5:56 PM):
	//	/**
	//	 * Join strings
	//	 *
	//	 * @param strings strings
	//	 * @return joined strings
	//	 */
	//	private String join(final String[] strings)
	//	{
	//		final StringBuilder sb = new StringBuilder();
	//		boolean first = true;
	//		sb.append('{');
	//		for (String str : strings)
	//		{
	//			if (first)
	//			{
	//				first = false;
	//			}
	//			else
	//			{
	//				sb.append(',');
	//			}
	//			sb.append(str);
	//		}
	//		sb.append('}');
	//		return sb.toString();
	//	}
	// --Commented out by Inspection STOP (9/23/17 5:56 PM)
}