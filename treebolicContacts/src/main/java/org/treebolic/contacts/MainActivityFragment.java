/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.contacts;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import treebolic.provider.contacts.Provider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.treebolic.TreebolicIface;

import java.util.Properties;

/**
 * Main activity fragment
 *
 * @author <a href="mailto:1313ou@gmail.com">Bernard Bou</a>
 */
public class MainActivityFragment extends Fragment
{
	// --Commented out by Inspection (9/23/17 5:48 PM):static private final String TAG = "MainF";

	// C R E A T E

	public MainActivityFragment()
	{
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		final View view = inflater.inflate(R.layout.fragment_main, container, false);

		final Context context = requireContext();
		final Resources resources = context.getResources();

		final String source = Settings.getStringPref(context, TreebolicIface.PREF_SOURCE);
		final Properties parameters = Settings.settingsToProperties(requireContext());
		final int count = (int) Provider.count(context, source, parameters);
		final int countContacts = (int) Provider.countContacts(context, source);
		final int countGroups = (int) Provider.countGroups(context, source);
		final String countStr = resources.getQuantityString(R.plurals.format_count, count, count);
		final String countContactsStr = resources.getQuantityString(R.plurals.format_count_contacts, countContacts, countContacts);
		final String countGroupsStr = resources.getQuantityString(R.plurals.format_count_groups, countGroups, countGroups);
		final String status = resources.getString(R.string.status, countContactsStr, countGroupsStr, countStr);

		final TextView statusView = view.findViewById(R.id.status);
		statusView.setText(status);
		return view;
	}
}