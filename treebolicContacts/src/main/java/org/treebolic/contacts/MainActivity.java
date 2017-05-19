package org.treebolic.contacts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import org.treebolic.TreebolicIface;

import treebolic.provider.contacts.Provider;

public class MainActivity extends AppCompatActivity
{
	private static final String TAG = "MainActivity";

	// L I F E C Y C L E

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// layout
		setContentView(R.layout.activity_main);

		// toolbar
		final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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
		final ImageButton button = (ImageButton) findViewById(R.id.queryButton);
		final String source = Settings.getStringPref(this, TreebolicIface.PREF_SOURCE);
		button.setVisibility(View.VISIBLE);
	}

	// M E N U

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		switch (id)
		{
			case R.id.action_run:
			{
				query();
				return true;
			}

			case R.id.action_list_contacts:
			{
				final Intent intent = new Intent(this, ListContactsActivity.class);
				startActivity(intent);
				return true;
			}

			case R.id.action_model:
			{
				final Provider provider = new Provider();
				provider.setHandle(this);
				provider.model();
				return true;
			}

			case R.id.action_settings:
			{
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
	static public void tryStartTreebolic(final Context context)
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
	static public Intent makeTreebolicIntent(final Context context)
	{
		// parent activity to return to
		final Intent parentIntent = new Intent();
		parentIntent.setClass(context, org.treebolic.contacts.MainActivity.class);

		// intent
		final Intent intent = new Intent(context, TreebolicActivity.class);

		// model passing
		intent.putExtra(TreebolicIface.ARG_PROVIDER, "treebolic.provider.contacts.Provider");
		intent.putExtra(TreebolicIface.ARG_SOURCE, (String) null);

		// other parameters passing
		intent.putExtra(TreebolicIface.ARG_BASE, (String) null);
		intent.putExtra(TreebolicIface.ARG_IMAGEBASE, (String) null);
		intent.putExtra(TreebolicIface.ARG_SETTINGS, (String) null);

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
	public void onClick(final View view)
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
		public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
		{
			return inflater.inflate(R.layout.fragment_main, container, false);
		}
	}
}
