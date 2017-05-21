package org.treebolic.contacts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;

import org.treebolic.TreebolicIface;

/**
 * Settings
 *
 * @author <a href="mailto:1313ou@gmail.com">Bernard Bou</a>
 */
public class Settings
{
	/**
	 * Initialized preference name
	 */
	public static final String PREF_INITIALIZED = "pref_initialized";

	/**
	 * First preference name
	 */
	public static final String PREF_FIRSTRUN = "pref_first_run";

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
	 * @param context
	 *            context
	 */
	static public void setDefaults(final Context context)
	{
	}

	/**
	 * Save source and base to preferences
	 *
	 * @param context
	 *            context
	 * @param source
	 *            source
	 * @param base
	 *            base
	 */
	public static void save(final Context context, final String source, final String base)
	{
		Settings.putStringPref(context, TreebolicIface.PREF_SOURCE, source);
		Settings.putStringPref(context, TreebolicIface.PREF_BASE, base);
	}

	/**
	 * Get string preference
	 *
	 * @param context
	 *            context
	 * @param key
	 *            key
	 * @return value
	 */
	static public String getStringPref(final Context context, final String key)
	{
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPref.getString(key, null);
	}

	/**
	 * Put string preference
	 *
	 * @param context
	 *            context
	 * @param key
	 *            key
	 * @param value
	 *            value
	 */
	@SuppressLint({"CommitPrefEdits", "ApplySharedPref"})
	static public void putStringPref(final Context context, final String key, final String value)
	{
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		sharedPref.edit().putString(key, value).commit();
	}

	// U T I L S

	/**
	 * Application settings
	 *
	 * @param context
	 *            context
	 * @param pkgName
	 *            package name
	 */
	static public void applicationSettings(final Context context, final String pkgName)
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
