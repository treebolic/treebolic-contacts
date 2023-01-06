/*
 * Copyright (c) 2019-2023. Bernard Bou
 */

package org.treebolic.contacts;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.treebolic.AppCompatCommonActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import treebolic.model.Model;
import treebolic.model.ModelDump;
import treebolic.provider.contacts.Provider;

/**
 * Abstract activity
 *
 * @author <a href="mailto:1313ou@gmail.com">Bernard Bou</a>
 */
abstract public class AbstractActivity extends AppCompatCommonActivity
{
	@NonNull
	abstract Fragment makeFragment();

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// layout
		setContentView(R.layout.activity_fragment_container);

		// toolbar
		final Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		// set up the action bar
		final ActionBar actionBar = getSupportActionBar();
		if (actionBar != null)
		{
			actionBar.setDisplayOptions(ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_HOME_AS_UP);
		}

		// fragment
		if (savedInstanceState == null)
		{
			final Fragment fragment = makeFragment();
			getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).commit();
		}
	}

	// M E N U

	@SuppressWarnings("SameReturnValue")
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (R.id.action_model == id)
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
		else if (R.id.action_settings == id)
		{
			final Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		}
		else
		{
			return super.onOptionsItemSelected(item);
		}
	}
}
