package org.treebolic.contacts;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

/**
 * List of groups activity
 *
 * @author <a href="mailto:1313ou@gmail.com">Bernard Bou</a>
 */
public class ListGroupsActivity extends AbstractActivity
{
	@NonNull
	@Override
	protected Fragment makeFragment()
	{
		return new ListGroupsFragment();
	}
}
