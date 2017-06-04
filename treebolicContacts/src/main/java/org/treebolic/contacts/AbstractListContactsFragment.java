package org.treebolic.contacts;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
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
 * Abstract fragment with list of contacts
 *
 * @author <a href="mailto:1313ou@gmail.com">Bernard Bou</a>
 */
abstract public class AbstractListContactsFragment extends Fragment
{
	static private final String TAG = "AListContactsF";

	// C R E A T E

	public AbstractListContactsFragment()
	{
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_list_contacts, container, false);
	}

	// D I A L O G

	public void info(final XSpannableStringBuilder sb, final String contactId, final String contactContentUri, final String thumbnailUri, final String photoUri, final String thumbnailId, final String photoId)
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
		sb.append("content Uri", new StyleSpan(Typeface.BOLD), 0) //
				.append('\n') //
				.append(contactContentUri == null ? null : contactContentUri.toString()) //
				.append('\n');

		if (thumbnailUri != null)
		{
			sb.append("thumbnail Uri", new StyleSpan(Typeface.BOLD), 0) //
					.append('\n') //
					.append(thumbnailUri == null ? "null" : thumbnailUri.toString()) //
					.append('\n');
		}

		if (photoUri != null)
		{
			sb.append("photo Uri", new StyleSpan(Typeface.BOLD), 0) //
					.append('\n') //
					.append(photoUri == null ? "null" : photoUri.toString()) //
					.append('\n');
		}

		if (thumbnailId != null)
		{
			sb.append("thumbnail id", new StyleSpan(Typeface.BOLD), 0) //
					.append('\n') //
					.append(thumbnailId) //
					.append('\n');
		}

		if (photoId != null)
		{
			sb.append("photo id", new StyleSpan(Typeface.BOLD), 0) //
					.append('\n') //
					.append(photoId) //
					.append('\n');
		}

		// view
		final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		final LayoutInflater inflater = getActivity().getLayoutInflater();
		final View dialogView = inflater.inflate(R.layout.detail_contact, null);
		final TextView textView = (TextView) dialogView.findViewById(android.R.id.text1);
		final ImageView imageView = (ImageView) dialogView.findViewById(R.id.photo);
		textView.setText(sb);
		if (photoUri != null)
		{
			imageView.setImageURI(Uri.parse(photoUri));
		}
		else if (thumbnailUri != null)
		{
			imageView.setImageURI(Uri.parse(thumbnailUri));
		}
		else
		{
			imageView.setImageResource(R.drawable.ic_contact_default);
		}

		builder.setView(dialogView).setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				//do things
			}
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
	private void getNames(final String contactId, final XSpannableStringBuilder sb)
	{
		final Context context = getContext();
		final ContentResolver cr = context.getContentResolver();
		final Cursor cursor = cr.query(ContactsContract.Data.CONTENT_URI, null, ContactsContract.Data.MIMETYPE + " = ?" + " AND " + ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID + " = ?", new String[]{ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE, contactId}, null);
		if (cursor != null)
		{
			if (cursor.getCount() > 0)
			{
				final int idxGiven = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME); // first
				final int idxFamily = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME); // last
				final int idxDisplay = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME); // display

				sb.append("names", new StyleSpan(Typeface.BOLD), 0) //
						.append('\n');

				while (cursor.moveToNext())
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
	private void getAddresses(final String contactId, final XSpannableStringBuilder sb)
	{
		final Context context = getContext();
		final ContentResolver cr = context.getContentResolver();
		final Cursor cursor = cr.query(ContactsContract.Data.CONTENT_URI, null, ContactsContract.Data.MIMETYPE + " = ?" + " AND " + ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID + " = ?", new String[]{ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE, contactId}, null);
		if (cursor != null)
		{
			if (cursor.getCount() > 0)
			{
				final int idxFormatted = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS);
				final int idxStreet = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET);
				final int idxCity = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY);
				final int idxPostCode = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE);
				final int idxCountry = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY);

				while (cursor.moveToNext())
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
	private void getEmails(final String contactId, final XSpannableStringBuilder sb)
	{
		final Context context = getContext();
		final ContentResolver cr = context.getContentResolver();
		final Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, ContactsContract.Data.MIMETYPE + " = ?" + " AND " + ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[]{ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE, contactId}, null);
		if (cursor != null)
		{
			if (cursor.getCount() > 0)
			{
				final int idxEmail = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);

				sb.append("email", new StyleSpan(Typeface.BOLD), 0) //
						.append('\n');

				while (cursor.moveToNext())
				{
					final String email = cursor.getString(idxEmail);
					if (email != null && !email.isEmpty())
					{
						sb.append(email) //
								.append('\n');
					}
				}
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
	private void getPhones(String contactId, final XSpannableStringBuilder sb)
	{
		final Context context = getContext();
		final ContentResolver cr = context.getContentResolver();
		final Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.Data.MIMETYPE + " = ?" + " AND " + ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE, contactId}, null);
		if (cursor != null)
		{
			if (cursor.getCount() > 0)
			{
				final int idxPhoneNumber = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
				final int idxPhoneType = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
				sb.append("phone", new StyleSpan(Typeface.BOLD), 0) //
						.append('\n');

				while (cursor.moveToNext())
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
			}
			cursor.close();
		}
	}

	// U T I L S

	class XSpannableStringBuilder extends SpannableStringBuilder
	{
		public SpannableStringBuilder append(CharSequence text, Object what, int flags)
		{
			int start = length();
			append(text);
			setSpan(what, start, length(), flags);
			return this;
		}
	}

	// H E L P E R S

	private void str2sb(final String header, final String[] strs, final XSpannableStringBuilder sb)
	{
		sb.append(header, new StyleSpan(Typeface.BOLD), 0).append('\n');
		sb.append(join(strs)).append('\n');
	}

	private String join(final String[] strs)
	{
		final StringBuilder sb = new StringBuilder();
		boolean first = true;
		sb.append('{');
		for (String str : strs)
		{
			if (first)
			{
				first = false;
			}
			else
			{
				sb.append(',');
			}
			sb.append(str);
		}
		sb.append('}');
		return sb.toString();
	}
}