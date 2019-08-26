/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.contacts;

import android.content.Context;
import android.content.SharedPreferences;
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
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
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
		final Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

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
	 * A list preference summary provider.
	 */
	private static final Preference.SummaryProvider<MultiSelectListPreference> LIST_SUMMARY_PROVIDER = (preference) -> {

		final Context context = preference.getContext();
		final CharSequence header = context.getString(R.string.pref_description_group_filter);
		final Set<String> values = preference.getPersistedStringSet(null);
		String summary = header.toString() + '\n';
		if (values == null || values.isEmpty())
		{
			summary += context.getString(R.string.all);
		}
		else
		{
			summary += setToString(values);
		}
		return summary;
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
		/**
		 * Source summary provider.
		 */
		private static final Preference.SummaryProvider<EditTextPreference> SOURCE_SUMMARY_PROVIDER = (preference) -> {

			final Context context = preference.getContext();
			final SharedPreferences prefs = preference.getSharedPreferences();
			final String desc = context.getString(R.string.pref_description_source);
			String value = prefs.getString(preference.getKey(), null);
			if (value == null || value.isEmpty())
			{
				value = context.getString(R.string.none);
			}
			else
			{
				value = "'" + value + "'";
			}
			return desc + " :\n" + value;
		};

		@Override
		public void onCreatePreferences(@SuppressWarnings("unused") final Bundle savedInstanceState, @SuppressWarnings("unused") final String rootKey)
		{
			// inflate
			addPreferencesFromResource(R.xml.pref_filter);

			// bind
			final EditTextPreference sourcePref = findPreference(TreebolicIface.PREF_SOURCE);
			assert sourcePref != null;
			sourcePref.setSummaryProvider(SOURCE_SUMMARY_PROVIDER);

			MultiSelectListPreference pref = findPreference(Settings.PREF_FILTER_GROUPS);
			assert pref != null;
			pref.setSummaryProvider(LIST_SUMMARY_PROVIDER);
		}

		private static final String DIALOG_FRAGMENT_TAG = "GroupPreference";

		@Override
		public void onDisplayPreferenceDialog(final Preference pref)
		{
			final FragmentManager manager = getFragmentManager();
			if (manager == null)
			{
				return;
			}

			if (manager.findFragmentByTag(DIALOG_FRAGMENT_TAG) != null)
			{
				return;
			}

			if (pref instanceof GroupPreference)
			{
				final DialogFragment dialogFragment = GroupPreferenceDialogFragmentCompat.newInstance((GroupPreference) pref);
				dialogFragment.setTargetFragment(this, 0);
				dialogFragment.show(manager, DIALOG_FRAGMENT_TAG);
				return;
			}
			super.onDisplayPreferenceDialog(pref);
		}
	}
}
