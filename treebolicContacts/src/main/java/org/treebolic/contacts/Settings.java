/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.contacts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;

import org.treebolic.TreebolicIface;

import java.util.Properties;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Settings
 *
 * @author <a href="mailto:1313ou@gmail.com">Bernard Bou</a>
 */
class Settings
{
	/**
	 * Initialized preference key
	 */
	public static final String PREF_INITIALIZED = "pref_initialized";

	/**
	 * First preference key
	 */
	public static final String PREF_FIRSTRUN = "pref_first_run";

	/**
	 * Groups preference key
	 */
	public static final String PREF_GROUPS = "pref_groups";

	/**
	 * Display preference key
	 */
	public static final String PREF_DISPLAY = "pref_display";

	/**
	 * Sort preference key
	 */
	public static final String PREF_SORT = "pref_sort";

	/**
	 * Group merging preference key
	 */
	public static final String PREF_MERGE_GROUPS = "pref_merge_groups";

	/**
	 * Group filter preference key
	 */
	public static final String PREF_FILTER_GROUPS = "pref_filter_groups";

	/**
	 * Photo preference key
	 */
	public static final String PREF_PHOTO = "pref_photo";

	/**
	 * Default CSS
	 */
	public static final String STYLE_DEFAULT = ".content { }\n" + //
			".link {color: #FFA500;font-size: small;}\n" + //
			".linking {color: #FFA500; font-size: small; }" + //
			".mount {color: #CD5C5C; font-size: small;}" + //
			".mounting {color: #CD5C5C; font-size: small; }" + //
			".searching {color: #FF7F50; font-size: small; }";

	/**
	 * Set default initial settings
	 *
	 * @param context context
	 */
	@SuppressWarnings("EmptyMethod")
	static public void setDefaults(@SuppressWarnings("unused") final Context context)
	{
		//
	}

	/**
	 * Save source and base to preferences
	 *
	 * @param context context
	 * @param source  source
	 */
	public static void save(final Context context, final String source)
	{
		Settings.putStringPref(context, TreebolicIface.PREF_SOURCE, source);
	}

	/**
	 * Get string preference
	 *
	 * @param context context
	 * @param key     key
	 * @return value
	 */
	@Nullable
	static public String getStringPref(final Context context, final String key)
	{
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPref.getString(key, null);
	}

	/**
	 * Put string preference
	 *
	 * @param context context
	 * @param key     key
	 * @param value   value
	 */
	@SuppressLint({"CommitPrefEdits", "ApplySharedPref"})
	private static void putStringPref(final Context context, @SuppressWarnings("SameParameterValue") final String key, final String value)
	{
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		sharedPref.edit().putString(key, value).commit();
	}

	/**
	 * Get strings preference
	 *
	 * @param context context
	 * @param key     key
	 * @return value
	 */
	static public String getStringsPref(final Context context, @SuppressWarnings("SameParameterValue") final String key)
	{
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		final Set<String> values = sharedPref.getStringSet(key, null);
		if (values == null || values.isEmpty())
		{
			return null;
		}
		return joinForIn(values);
	}

	/**
	 * Get boolean preference
	 *
	 * @param context context
	 * @param key     key
	 * @return value
	 */
	@SuppressWarnings("WeakerAccess")
	static public boolean getBooleanPref(final Context context, final String key)
	{
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPref.getBoolean(key, false);
	}

	/**
	 * Join for 'in' selection clause
	 *
	 * @param strings values
	 * @return 'in' clause
	 */
	static private String joinForIn(@NonNull final Iterable<String> strings)
	{
		final StringBuilder sb = new StringBuilder();
		boolean first = true;
		sb.append('(');
		for (String str : strings)
		{
			if (first)
			{
				first = false;
			}
			else
			{
				sb.append(',');
			}
			sb.append(str);
		}
		sb.append(')');
		return sb.toString();
	}

	// U T I L S

	/**
	 * Collect preferences to properties
	 *
	 * @param context context
	 * @return properties
	 */
	static public Properties settingsToProperties(final Context context)
	{
		final String source = getStringPref(context, TreebolicIface.PREF_SOURCE);
		final boolean groups = getBooleanPref(context, PREF_GROUPS);
		final boolean mergeGroups = getBooleanPref(context, PREF_MERGE_GROUPS);
		final boolean photo = getBooleanPref(context, PREF_PHOTO);
		final String display = getStringPref(context, PREF_DISPLAY);
		final String sort = getStringPref(context, PREF_SORT);
		final String filterGroups = getStringsPref(context, PREF_FILTER_GROUPS);

		final Properties properties = new Properties();
		properties.setProperty(TreebolicIface.PREF_SOURCE, source);
		properties.setProperty(treebolic.provider.contacts.Settings.PARAM_GROUPS, groups ? "true" : "false");
		properties.setProperty(treebolic.provider.contacts.Settings.PARAM_MERGE_GROUPS, mergeGroups ? "true" : "false");
		properties.setProperty(treebolic.provider.contacts.Settings.PARAM_PHOTO, photo ? "true" : "false");
		properties.setProperty(treebolic.provider.contacts.Settings.PARAM_DISPLAY, display);
		properties.setProperty(treebolic.provider.contacts.Settings.PARAM_SORT, sort);
		properties.setProperty(treebolic.provider.contacts.Settings.PARAM_FILTER_GROUPS, filterGroups);
		return properties;
	}

	/**
	 * Application settings
	 *
	 * @param context context
	 * @param pkgName package name
	 */
	static public void applicationSettings(@NonNull final Context context, @SuppressWarnings("SameParameterValue") final String pkgName)
	{
		final int apiLevel = Build.VERSION.SDK_INT;
		final Intent intent = new Intent();

		if (apiLevel >= 9)
		{
			intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
			intent.setData(Uri.parse("package:" + pkgName));
		}
		else
		{
			final String appPkgName = apiLevel == 8 ? "pkg" : "com.android.settings.ApplicationPkgName";

			intent.setAction(Intent.ACTION_VIEW);
			intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
			intent.putExtra(appPkgName, pkgName);
		}

		// start activity
		context.startActivity(intent);
	}
}
