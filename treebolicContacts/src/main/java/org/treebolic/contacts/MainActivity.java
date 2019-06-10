package org.treebolic.contacts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import org.treebolic.AppCompatCommonActivity;
import org.treebolic.TreebolicIface;
import org.treebolic.guide.AboutActivity;
import org.treebolic.guide.HelpActivity;
import org.treebolic.guide.Tip;

import androidx.fragment.app.Fragment;
import treebolic.model.Model;
import treebolic.model.ModelDump;
import treebolic.provider.contacts.Permissions;
import treebolic.provider.contacts.Provider;

/**
 * Main activity
 *
 * @author <a href="mailto:1313ou@gmail.com">Bernard Bou</a>
 */
public class MainActivity extends AppCompatCommonActivity
{
	private static final String TAG = "MainActivity";

	// L I F E C Y C L E

	@Override
	protected void onCreate(@Nullable final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// layout
		setContentView(R.layout.activity_main);

		// toolbar
		final Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		// set up the action bar
		final ActionBar actionBar = getSupportActionBar();
		if (actionBar != null)
		{
			actionBar.setDisplayOptions(ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_TITLE);
		}

		// initialize
		initialize();

		// fragment
		if (savedInstanceState == null)
		{
			PlaceholderFragment fragment = new PlaceholderFragment();
			getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).commit();
		}
	}

	@Override
	protected void onResume()
	{
		updateButton();
		super.onResume();
	}

	// H E L P E R

	private void updateButton()
	{
		final ImageButton button = findViewById(R.id.queryButton);
		// final String source = Settings.getStringPref(this, TreebolicIface.PREF_SOURCE);
		button.setVisibility(View.VISIBLE);
	}

	// M E N U

	@SuppressWarnings("SameReturnValue")
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		switch (id)
		{
			case R.id.action_run:
			{
				query();
				return true;
			}
			case R.id.action_tips:
			{
				Tip.show(getSupportFragmentManager());
				return true;
			}
			case R.id.action_help:
			{
				startActivity(new Intent(this, HelpActivity.class));
				return true;
			}
			case R.id.action_about:
			{
				startActivity(new Intent(this, AboutActivity.class));
				return true;
			}
			case R.id.action_list_contacts:
			{
				final Intent intent = new Intent(this, ListContactsActivity.class);
				startActivity(intent);
				return true;
			}
			case R.id.action_list_grouped_contacts:
			{
				final Intent intent = new Intent(this, ListContactsGroupedActivity.class);
				startActivity(intent);
				return true;
			}
			case R.id.action_list_ungrouped_contacts:
			{
				final Intent intent = new Intent(this, ListContactsUnGroupedActivity.class);
				startActivity(intent);
				return true;
			}
			case R.id.action_list_groups:
			{
				final Intent intent = new Intent(this, ListGroupsActivity.class);
				startActivity(intent);
				return true;
			}
			case R.id.action_settings:
			{
				final Intent intent = new Intent(this, SettingsActivity.class);
				startActivity(intent);
				return true;
			}
			case R.id.action_app_settings:
			{
				Settings.applicationSettings(this, "org.treebolic.contacts");
				return true;
			}
			case R.id.action_model:
			{
				final Provider provider = new Provider();
				provider.setHandle(this);
				final Model model = provider.makeModel(null, null, null);
				final String dump = ModelDump.toString(model);
				//System.out.println(dump);
				final AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(dump);
				builder.show();
				return true;
			}
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	// I N I T I A L I Z E

	/**
	 * Initialize
	 */
	@SuppressLint({"CommitPrefEdits", "ApplySharedPref"})
	private void initialize()
	{
		// permissions
		Permissions.check(this);

		// test if initialized
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		final boolean initialized = sharedPref.getBoolean(Settings.PREF_INITIALIZED, false);
		if (!initialized)
		{
			// default settings
			Settings.setDefaults(this);

			// flag as initialized
			sharedPref.edit().putBoolean(Settings.PREF_INITIALIZED, true).commit();
		}
	}

	// Q U E R Y
	private void query()
	{
		tryStartTreebolic(this);
	}

	/**
	 * Start Treebolic plugin activity from source + base
	 *
	 * @param context context
	 */
	private static void tryStartTreebolic(@NonNull final Context context)
	{
		final Intent intent = MainActivity.makeTreebolicIntent(context);
		Log.d(MainActivity.TAG, "Start treebolic");
		context.startActivity(intent);
	}

	/**
	 * Make Treebolic intent
	 *
	 * @param context context
	 * @return intent
	 */
	@NonNull
	private static Intent makeTreebolicIntent(@NonNull final Context context)
	{
		final String source = treebolic.provider.contacts.Settings.getStringPref(context, TreebolicIface.PREF_SOURCE);
		final boolean groups = treebolic.provider.contacts.Settings.getBooleanPref(context, Settings.PREF_GROUPS);
		final boolean mergeGroups = treebolic.provider.contacts.Settings.getBooleanPref(context, Settings.PREF_MERGE_GROUPS);
		final String display = treebolic.provider.contacts.Settings.getStringPref(context, Settings.PREF_DISPLAY);
		final String sort = treebolic.provider.contacts.Settings.getStringPref(context, Settings.PREF_SORT);
		final String filterGroups = Settings.getStringsPref(context, Settings.PREF_FILTER_GROUPS);
		final Bundle bundle = new Bundle();
		bundle.putBoolean(treebolic.provider.contacts.Settings.PARAM_GROUPS, groups);
		bundle.putBoolean(treebolic.provider.contacts.Settings.PARAM_MERGE_GROUPS, mergeGroups);
		bundle.putString(treebolic.provider.contacts.Settings.PARAM_DISPLAY, display);
		bundle.putString(treebolic.provider.contacts.Settings.PARAM_SORT, sort);
		bundle.putString(treebolic.provider.contacts.Settings.PARAM_FILTER_GROUPS, filterGroups);

		// parent activity to return to
		final Intent parentIntent = new Intent();
		parentIntent.setClass(context, org.treebolic.contacts.MainActivity.class);

		// intent
		final Intent intent = new Intent(context, TreebolicActivity.class);

		// model passing
		intent.putExtra(TreebolicIface.ARG_PROVIDER, "treebolic.provider.contacts.Provider");
		intent.putExtra(TreebolicIface.ARG_SOURCE, source);

		// other parameters passing
		intent.putExtra(TreebolicIface.ARG_BASE, (String) null);
		intent.putExtra(TreebolicIface.ARG_IMAGEBASE, (String) null);
		intent.putExtra(TreebolicIface.ARG_SETTINGS, (String) null);
		intent.putExtra(TreebolicIface.ARG_MORE, bundle);

		// parent passing
		intent.putExtra(TreebolicIface.ARG_PARENTACTIVITY, parentIntent);

		// scheme
		intent.putExtra(TreebolicIface.ARG_URLSCHEME, "contacts:");

		return intent;
	}

	// C L I C K

	/**
	 * Click listener
	 *
	 * @param view view
	 */
	public void onClick(@SuppressWarnings("unused") final View view)
	{
		query();
	}

	// F R A G M E N T

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment
	{
		@Override
		public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
		{
			return inflater.inflate(R.layout.fragment_main, container, false);
		}
	}
}
