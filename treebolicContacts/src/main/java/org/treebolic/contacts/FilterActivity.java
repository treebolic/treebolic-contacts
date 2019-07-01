/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.contacts;

import android.os.Bundle;

import org.treebolic.AppCompatCommonActivity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

public class FilterActivity extends AppCompatCommonActivity
{
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// layout
		setContentView(R.layout.activity_sub_settings);

		// toolbar
		final Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		// show the Up button in the action bar.
		final ActionBar actionBar = getSupportActionBar();
		if (actionBar != null)
		{
			actionBar.setDisplayOptions(ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP);
		}

		// fragment
		if (savedInstanceState == null)
		{
			final Fragment fragment = new SettingsActivity.FilterPreferenceFragment();
			getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).commit();
		}
	}
}
