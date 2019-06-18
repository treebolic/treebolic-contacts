/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.contacts;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;

import org.treebolic.AppCompatCommonPreferenceActivity;
import org.treebolic.TreebolicIface;

import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.NavUtils;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.legacy.contrib.Header;

/**
 * Settings activity
 *
 * @author <a href="mailto:1313ou@gmail.com">Bernard Bou</a>
 */
public class SettingsActivity extends AppCompatCommonPreferenceActivity
{
	// L I F E C Y C L E

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		// super
		super.onCreate(savedInstanceState);

		// toolbar
		setupToolbar(R.layout.toolbar, R.id.toolbar);

		// set up the action bar
		final ActionBar actionBar = getSupportActionBar();
		if (actionBar != null)
		{
			actionBar.setDisplayOptions(ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP);
		}
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull final MenuItem item)
	{
		if (item.getItemId() == android.R.id.home)
		{
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	// S E T U P

	@Override
	public void onBuildHeaders(@NonNull final List<Header> target)
	{
		loadHeadersFromResource(R.xml.pref_headers, target);
	}

	// D E T E C T I O N

	@Override
	public boolean isValidFragment(final String fragmentName)
	{
		return GeneralPreferenceFragment.class.getName().equals(fragmentName) || FilterPreferenceFragment.class.getName().equals(fragmentName);
	}

	@Override
	public boolean onIsMultiPane()
	{
		return SettingsActivity.isLargeTablet(this);
	}

	/**
	 * Helper method to determine if the device has an extra-large screen. For example, 10" tablets are extra-large.
	 */
	private static boolean isLargeTablet(@NonNull final Context context)
	{
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}

	// S U M M A R Y

	/**
	 * A preference value change listener that updates the preference's summary to reflect its new value.
	 */
	private static final Preference.SummaryProvider<MultiSelectListPreference> LIST_SUMMARY_PROVIDER = (preference) -> {

		final Set<String> values = preference.getPersistedStringSet(null);
		return setToString(values);
	};

	static @NonNull
	private CharSequence setToString(@Nullable final Set<String> values)
	{
		if (values == null)
		{
			return "";
		}
		final StringBuilder sb = new StringBuilder();
		boolean isFirst = true;
		for (String value : values)
		{
			if (isFirst)
			{
				isFirst = false;
			}
			else
			{
				sb.append(' ');
			}
			sb.append(value);
		}
		return sb;
	}

	// F R A G M E N T S

	public static class GeneralPreferenceFragment extends PreferenceFragmentCompat
	{
		@Override
		public void onCreatePreferences(@SuppressWarnings("unused") final Bundle savedInstanceState, @SuppressWarnings("unused") final String rootKey)
		{
			// inflate
			addPreferencesFromResource(R.xml.pref_general);

			// bind
			final EditTextPreference sourcePref = findPreference(TreebolicIface.PREF_SOURCE);
			assert sourcePref != null;
			sourcePref.setSummaryProvider(EditTextPreference.SimpleSummaryProvider.getInstance());

			final ListPreference displayPref = findPreference(Settings.PREF_DISPLAY);
			assert displayPref != null;
			displayPref.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());

			final ListPreference sortPref = findPreference(Settings.PREF_SORT);
			assert sortPref != null;
			sortPref.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
		}
	}

	public static class FilterPreferenceFragment extends PreferenceFragmentCompat
	{
		@Override
		public void onCreatePreferences(@SuppressWarnings("unused") final Bundle savedInstanceState, @SuppressWarnings("unused") final String rootKey)
		{
			// inflate
			addPreferencesFromResource(R.xml.pref_filter);

			// bind
			MultiSelectListPreference pref = findPreference(Settings.PREF_FILTER_GROUPS);
			assert pref != null;
			pref.setSummaryProvider(LIST_SUMMARY_PROVIDER);
		}
	}
}
