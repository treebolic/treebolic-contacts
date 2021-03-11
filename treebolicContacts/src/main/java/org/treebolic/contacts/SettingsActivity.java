/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.contacts;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import org.treebolic.AppCompatCommonPreferenceActivity;
import org.treebolic.TreebolicIface;

import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

/**
 * Settings activity
 *
 * @author Bernard Bou
 */
public class SettingsActivity extends AppCompatCommonPreferenceActivity
{
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

	@SuppressWarnings("WeakerAccess")
	public static class GeneralPreferenceFragment extends PreferenceFragmentCompat
	{
		@Override
		public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey)
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
		public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey)
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
			final FragmentManager manager;
			try
			{
				manager = getParentFragmentManager();
			}
			catch (IllegalStateException e)
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
