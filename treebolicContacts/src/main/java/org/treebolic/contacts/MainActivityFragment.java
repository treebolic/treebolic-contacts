package org.treebolic.contacts;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Main activity fragment
 *
 * @author <a href="mailto:1313ou@gmail.com">Bernard Bou</a>
 */
public class MainActivityFragment extends Fragment
{
	// --Commented out by Inspection (9/23/17 5:48 PM):static private final String TAG = "MainF";

	// C R E A T E

	public MainActivityFragment()
	{
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_main, container, false);
	}
}