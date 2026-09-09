/*
 * Copyright (c) 2026. Bernard Bou
 */
package org.treebolic.contacts

import androidx.fragment.app.Fragment

/**
 * List of contacts activity
 *
 * @author [Bernard Bou](mailto:1313ou@gmail.com)
 */
class ListContactsGroupedActivity : AbstractActivity() {

    override fun makeFragment(): Fragment {
        return ListContactsGroupedFragment()
    }
}
