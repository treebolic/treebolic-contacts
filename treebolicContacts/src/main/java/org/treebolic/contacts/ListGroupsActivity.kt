/*
 * Copyright (c) 2026. Bernard Bou
 */
package org.treebolic.contacts

import androidx.fragment.app.Fragment

/**
 * List of groups activity
 *
 * @author [Bernard Bou](mailto:1313ou@gmail.com)
 */
class ListGroupsActivity : AbstractActivity() {

    override fun makeFragment(): Fragment {
        return ListGroupsFragment()
    }
}
