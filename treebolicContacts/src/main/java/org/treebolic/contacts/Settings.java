package org.treebolic.contacts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.treebolic.TreebolicIface;

import java.util.Set;

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
