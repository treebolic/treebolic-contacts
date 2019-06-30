/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.contacts;

import android.os.Bundle;
import android.widget.ListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupPreferenceDialogFragmentCompat extends MultiSelectListPreferenceCustomDialogFragmentCompat
{
	private final List<String> accountNames = new ArrayList<>();

	private final List<String> accountTypes = new ArrayList<>();

	private final List<Long> counts = new ArrayList<>();

	public static MultiSelectListPreferenceCustomDialogFragmentCompat newInstance(final GroupPreference pref)
	{
		final GroupPreferenceDialogFragmentCompat fragment = new GroupPreferenceDialogFragmentCompat();
		final Bundle bundle = new Bundle(1);
		bundle.putString(ARG_KEY, pref.getKey());
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		final GroupPreference preference = (GroupPreference) getPreference();
		accountNames.clear();
		accountNames.addAll(preference.accountNames);
		accountTypes.clear();
		accountTypes.addAll(preference.accountTypes);
		counts.clear();
		counts.addAll(preference.counts);
	}

	@Override
	protected ListAdapter getAdapter()
	{
		final int entryCount = mEntryValues.length;
		final List<Map<String, Object>> data = new ArrayList<>();
		for (int i = 0; i < entryCount; i++)

		{
			final Map<String, Object> row = new HashMap<>();
			boolean checked = mNewValues.contains(mEntryValues[i].toString());
			row.put("check", checked);
			row.put("group_id", mEntryValues[i].toString());
			row.put("display_name", mEntries[i]);
			row.put("account_name", accountNames.get(i));
			row.put("account_type", accountTypes.get(i));
			row.put("count", counts.get(i));
			data.add(row);
		}

		final String[] from = {"check", "group_id", "display_name", "account_name", "account_type", "count"};
		final int[] to = {android.R.id.checkbox, R.id.group_id, R.id.display_name, R.id.account_name, R.id.account_type, R.id.count};
		return new CheckBoxSimpleAdapter(getContext(), data, R.layout.item_group_select, from, to, android.R.id.checkbox);
	}
}
