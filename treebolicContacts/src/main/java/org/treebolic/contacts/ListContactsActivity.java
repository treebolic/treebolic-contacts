package org.treebolic.contacts;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

/**
 * List of contacts activity
 *
 * @author <a href="mailto:1313ou@gmail.com">Bernard Bou</a>
 */
public class ListContactsActivity extends AbstractActivity
{
	@NonNull
	@Override
	protected Fragment makeFragment()
	{
		//final String groups = Settings.getStringsPref(this, Settings.PREF_FILTER_GROUPS);
		//return groups == null || groups.isEmpty() ? new ListContactsFragment() : new ListContactsGroupedFragment();
		return new ListContactsFragment();
	}
}
