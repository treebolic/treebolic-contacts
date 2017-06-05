package org.treebolic.contacts;

import android.support.v4.app.Fragment;

/**
 * List of contacts activity
 *
 * @author <a href="mailto:1313ou@gmail.com">Bernard Bou</a>
 */
public class ListContactsUnGroupedActivity extends AbstractActivity
{
	@Override
	protected Fragment makeFragment()
	{
		return new ListContactsUnGroupedFragment();
	}
}
