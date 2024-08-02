/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.contacts

import androidx.fragment.app.Fragment

/**
 * List of contacts activity
 *
 * @author [Bernard Bou](mailto:1313ou@gmail.com)
 */
class ListContactsUnGroupedActivity : AbstractActivity() {

    override fun makeFragment(): Fragment {
        return ListContactsUnGroupedFragment()
    }
}
