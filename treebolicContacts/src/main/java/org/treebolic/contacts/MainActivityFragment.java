/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.contacts;

import android.content.Context;
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
		final String source = Settings.getStringPref(context, TreebolicIface.PREF_SOURCE);
		final Properties parameters = Settings.settingsToProperties(requireContext());
		final long count = Provider.count(context, source, parameters);
		final long countContacts = Provider.countContacts(context, source);
		final long countGroups = Provider.countGroups(context, source);

		final TextView filteredContactsView = view.findViewById(R.id.filteredContactsView);
		filteredContactsView.setText(getString(R.string.format_count, count));

		final TextView contactView = view.findViewById(R.id.contactsView);
		contactView.setText(getString(R.string.format_count_contacts, countContacts));

		final TextView groupView = view.findViewById(R.id.groupsView);
		groupView.setText(getString(R.string.format_count_groups, countGroups));
		return view;
	}
}