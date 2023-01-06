/*
 * Copyright (c) 2019-2023. Bernard Bou
 */

package org.treebolic.contacts;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

/**
 * List of contacts activity
 *
 * @author <a href="mailto:1313ou@gmail.com">Bernard Bou</a>
 */
public class ListContactsGroupedActivity extends AbstractActivity
{
	@NonNull
	@Override
	protected Fragment makeFragment()
	{
		return new ListContactsGroupedFragment();
	}
}
