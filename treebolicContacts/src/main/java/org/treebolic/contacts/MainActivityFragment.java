/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.contacts;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import treebolic.provider.contacts.Permissions;
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
	// static private final String TAG = "MainF";

	private TextView statusView;

	// C R E A T E

	public MainActivityFragment()
	{
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		final View view = inflater.inflate(R.layout.fragment_main, container, false);
		this.statusView = view.findViewById(R.id.status);
		return view;
	}

	@Override
	public void onResume()
	{
		super.onResume();

		final Activity activity = requireActivity();
		if (Permissions.check(activity))
		{
			activity.finish();
			return;
		}

		final Resources resources = activity.getResources();

		final String source = Settings.getStringPref(activity, TreebolicIface.PREF_SOURCE);
		final Properties parameters = Settings.settingsToProperties(requireContext());
		final int count = (int) Provider.count(activity, source, parameters);
		final int countContacts = (int) Provider.countContacts(activity, source);
		final int countGroups = (int) Provider.countGroups(activity);
		final String countStr = resources.getQuantityString(R.plurals.format_count, count, count);
		final String countContactsStr = resources.getQuantityString(R.plurals.format_count_contacts, countContacts, countContacts);
		final String countGroupsStr = resources.getQuantityString(R.plurals.format_count_groups, countGroups, countGroups);
		final String status = resources.getString(R.string.status, countContactsStr, countGroupsStr, countStr);

		this.statusView.setText(status);
	}
}