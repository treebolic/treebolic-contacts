/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package treebolic.provider.contacts

import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import android.util.Log
import treebolic.ILocator
import treebolic.glue.iface.Colors
import treebolic.glue.iface.Image
import treebolic.model.IEdge
import treebolic.model.INode
import treebolic.model.Model
import treebolic.model.Tree
import treebolic.model.TreeMutableNode
import treebolic.provider.IProvider
import treebolic.provider.IProviderContext
import treebolic.provider.LoadBalancer
import java.net.URL
import java.util.Locale
import java.util.Properties

/**
 * Provider for Contacts
 *
 * @author [Bernard Bou](mailto:1313ou@gmail.com)
 */
class Provider : IProvider {

    // S E T T I N G S   D A T A

    /**
     * Ordinals used as indexes of images
     */
    private enum class ImageIndices {

        ROOT, CONTACT, GROUP,
    }

    /**
     * Sort code 1=first name-last name, 2=last name, first name
     */
    private var displayCode = 0

    /**
     * Sort code 1=first name, 2=last name
     */
    private var sortCode = 0

    /**
     * Whether to merge same-name groups
     */
    private var mergeGroups = true

    /**
     * Whether to include photos
     */
    private var photo = false

    /**
     * Load balancer
     */
    private val loadBalancer: LoadBalancer = LoadBalancer(TREE_MAX_NODES_AT_LEVEL, LABEL_TRUNCATE_AT)

    /**
     * Node count
     */
    private var contactsCount = 0

    // C O N T E X T

    /**
     * Android context
     */
    private var context: Context? = null

    // C O N S T R U C T

    /**
     * Constructor
     */
    init {
        loadBalancer.setGroupNode(null, null, null, LOADBALANCING_EDGE_COLOR, LOADBALANCING_EDGE_STYLE, -1,  /*ImageIndices.FOLDER.ordinal() */null, null)
    }

    // I P R O V I D E R   I N T E R F A C E  (S E T U P)

    override fun setHandle(handle: Any) {
        context = handle as Context
    }

    override fun setContext(providerContext: IProviderContext) {
        // not needed
    }

    override fun setLocator(context: ILocator) {
        // not needed
    }

    // I P R O V I D E R   I N T E R F A C E   (M O D E L)

    override fun makeModel(source: String?, base: URL?, parameters: Properties?): Model {
        val tree = makeTree(source, base, parameters, false)
        val settings = makeSettings()
        val images = images
        return Model(tree, settings, images)
    }

    override fun makeTree(source: String?, base: URL?, parameters: Properties?, checkRecursion: Boolean): Tree {
        contactsCount = 0
        val tree = buildTree(source, parameters)
        val root = tree.root as TreeMutableNode
        root.content = contactsCount.toString()
        return tree
    }

    // T R E E   F A C T O R Y

    /**
     * Tree factory
     *
     * @param contactFilter contact filter
     * @param parameters    parameters
     * @return tree
     */
    private fun buildTree(contactFilter: String?, parameters: Properties?): Tree {
        if (parameters != null) {
            val displayParam = parameters.getProperty(Settings.PARAM_DISPLAY)
            if (Settings.VALUE_DISPLAY_FIRST_LAST == displayParam) {
                displayCode = 1
            } else if (Settings.VALUE_DISPLAY_LAST_FIRST == displayParam) {
                displayCode = 2
            }

            val sortParam = parameters.getProperty(Settings.PARAM_SORT)
            if (Settings.VALUE_SORT_FIRST == sortParam) {
                sortCode = 1
            } else if (Settings.VALUE_SORT_LAST == sortParam) {
                sortCode = 2
            }

            val mergeGroupsParam = parameters.getProperty(Settings.PARAM_MERGE_GROUPS, "true")
            mergeGroups = ("true" == mergeGroupsParam)

            val photoParam = parameters.getProperty(Settings.PARAM_PHOTO, "false")
            photo = ("true" == photoParam)

            // start
            val groupsParam = parameters.getProperty(Settings.PARAM_GROUPS, "false")
            val groups = "true" == groupsParam
            if (groups) {
                return buildTree(loadBalancer.buildHierarchy(collectGroups(contactFilter), 0))
            }

            val filterGroupsParam = parameters.getProperty(Settings.PARAM_FILTER_GROUPS)
            val contacts = if (filterGroupsParam == null) collectContacts(contactFilter) else collectContacts(contactFilter, filterGroupsParam)
            return buildTree(loadBalancer.buildHierarchy(contacts, 0))
        } else {
            return buildTree(loadBalancer.buildHierarchy(collectGroups(contactFilter), 0))
        }
    }

    /**
     * Tree factory
     *
     * @param nodes contact nodes
     * @return tree of contact nodes
     */
    private fun buildTree(nodes: List<INode>): Tree {
        val topRoot = TreeMutableNode(null, "root")
        //topRoot.setLabel("contacts")
        topRoot.backColor = ROOT_BACKCOLOR
        topRoot.foreColor = ROOT_FORECOLOR
        topRoot.edgeColor = ROOT_EDGE_COLOR
        topRoot.edgeStyle = IEdge.SOLID.toInt() or IEdge.TOTRIANGLE or IEdge.TOFILL
        topRoot.imageIndex = ImageIndices.ROOT.ordinal

        topRoot.addChildren(nodes)
        return Tree(topRoot, null)
    }

    // L E A V E S

    /**
     * Collect contact nodes
     *
     * @param contactFilter contact filter
     * @return list of contact nodes
     */
    private fun collectContacts(contactFilter: String?): List<INode>? {
        val nullSource = contactFilter.isNullOrEmpty()
        val selection = if (nullSource) null else Settings.SELECTION_DN_LIKE
        val selectionArgs = if (nullSource) null else arrayOf("%$contactFilter%")
        val sort = (if (sortCode == 1) ContactsContract.Contacts.DISPLAY_NAME else ContactsContract.Contacts.DISPLAY_NAME_ALTERNATIVE) + " COLLATE LOCALIZED ASC"

        val contentResolver = context!!.contentResolver
        val cursor = contentResolver.query( //
            Settings.CONTACT_URI,  //
            Settings.CONTACT_PROJECTION,  // columns to return for each row
            selection,  // selection criteria
            selectionArgs,  // selection criteria arguments to placeholders
            sort
        ) // sort order for the returned rows
        var nodes: MutableList<INode>? = null
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                nodes = ArrayList()
                do {
                    // row values
                    val id = cursor.getString(Settings.CONTACT_ID_INDEX)
                    val name = cursor.getString(Settings.DISPLAY_NAME_INDEX)
                    val altName = cursor.getString(Settings.DISPLAY_NAME_ALTERNATIVE_INDEX)
                    val photoThumbnailUri = if (photo) cursor.getString(Settings.PHOTO_THUMBNAIL_URI_INDEX) else null

                    //val photoUri: String = cursor.getString(Settings.PHOTO_URI_INDEX)
                    //val photoThumbnailId: Long  = cursor.getLong(Settings.PHOTO_THUMBNAIL_ID_INDEX)
                    //val photoIdKey: Long = cursor.getLong(Settings.PHOTO_ID_INDEX)
                    //val lookupKey:  String = cursor.getString(Settings.LOOKUP_KEY_INDEX)
                    val node = makeContactNode(id, name, altName, photoThumbnailUri)
                    nodes.add(node)
                } while (cursor.moveToNext())

                Log.d(TAG, "collected nodes " + nodes.size)
            }
            cursor.close()
        }
        return nodes
    }

    /**
     * Collect contact nodes
     *
     * @param contactFilter contact filter
     * @param groupFilter   group filter
     * @return list of contact nodes
     */
    private fun collectContacts(contactFilter: String?, groupFilter: String): List<INode?>? {
        val nullContactFilter = contactFilter.isNullOrEmpty()
        var selection = if (nullContactFilter) null else Settings.SELECTION_DN_LIKE
        val selectionArgs = if (nullContactFilter) null else arrayOf("%$contactFilter%")
        val sort =
            (if (sortCode == 1) ContactsContract.CommonDataKinds.GroupMembership.DISPLAY_NAME else ContactsContract.CommonDataKinds.GroupMembership.DISPLAY_NAME_ALTERNATIVE) + " COLLATE LOCALIZED ASC," + ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID

        val extraSelection = ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + " IN " + groupFilter + " AND " + ContactsContract.Data.MIMETYPE + " = " + "'" + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "'"
        selection = if (selection == null) extraSelection else "$selection AND $extraSelection"
        Log.d(TAG, "selection=$selection")

        val contentResolver = context!!.contentResolver
        val cursor = contentResolver.query( //
            Settings.GROUP_MEMBERS_URI,  //
            Settings.GROUP_MEMBERS_PROJECTION,  // columns to return for each row
            selection,  //
            selectionArgs,  // selection criteria arguments to placeholders
            sort
        ) // sort order for the returned rows
        var nodes: MutableList<INode?>? = null
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                nodes = ArrayList()
                var contactIdCurrent: String? = null
                do {
                    val contactId = cursor.getString(Settings.GROUP_MEMBERS_CONTACT_ID_INDEX)
                    if (contactId == contactIdCurrent) {
                        continue
                    }
                    contactIdCurrent = contactId

                    // row values
                    val name = cursor.getString(Settings.GROUP_MEMBERS_DISPLAY_NAME_INDEX)
                    val altName = cursor.getString(Settings.GROUP_MEMBERS_DISPLAY_NAME_ALTERNATIVE_INDEX)
                    val photoThumbnailUri = if (photo) cursor.getString(Settings.GROUP_MEMBERS_PHOTO_THUMBNAIL_URI_INDEX) else null

                    val node = makeContactNode(contactId, name, altName, photoThumbnailUri)
                    nodes.add(node)
                } while (cursor.moveToNext())

                Log.d(TAG, "collected nodes " + nodes.size)
            }
            cursor.close()
        }
        return nodes
    }

    /**
     * Collect all groups
     *
     * @param contactFilter contact filter
     * @return list of groups (with members)
     */
    private fun collectGroups(contactFilter: String?): List<INode>? {
        val contentResolver = context!!.contentResolver
        val cursor = contentResolver.query( //
            Settings.GROUP_URI,  //
            Settings.GROUP_PROJECTION,  //
            null,  //
            null,  //
            ContactsContract.Groups.TITLE + " COLLATE LOCALIZED ASC" //
        )
        var nodes: MutableList<INode>? = null
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                nodes = ArrayList()
                do {
                    val groupId = cursor.getString(Settings.GROUP_ID_INDEX)
                    val groupSystemId = cursor.getString(Settings.GROUP_SYSTEM_ID_INDEX)
                    var title: CharSequence? = cursor.getString(Settings.GROUP_TITLE_INDEX)

                    val titleRes = cursor.getString(Settings.GROUP_TITLE_RES_INDEX)
                    if (titleRes != null) {
                        val resId = titleRes.toInt()
                        val resPackage = cursor.getString(Settings.GROUP_RES_PACKAGE_INDEX)
                        if (resPackage != null) {
                            title = context!!.packageManager.getText(resPackage, resId, null)
                        }
                    }

                    if (title != null && "Starred in Android".contentEquals(title)) {
                        title = "Starred"
                    }
                    val accountName = cursor.getString(Settings.GROUP_ACCOUNT_NAME)
                    val accountType = cursor.getString(Settings.GROUP_ACCOUNT_TYPE)
                    Log.d("group ", "$groupId $groupSystemId $title $accountName $accountType")

                    // members
                    val memberNodes = collectGroupMembers(groupId, contactFilter)
                    if (!memberNodes.isNullOrEmpty()) {
                        // group
                        val node = makeGroupNode(groupId, groupSystemId, title?.toString() ?: "?", accountName, accountType)
                        nodes.add(node)

                        val subNodes = loadBalancer.buildHierarchy(memberNodes, 0)
                        node.addChildren(subNodes)
                    }
                } while (cursor.moveToNext())
            }
            cursor.close()
        }
        if (!mergeGroups || nodes == null) {
            return nodes
        }

        // merge
        val nodes2: MutableList<INode> = ArrayList()
        var i = 0
        while (i < nodes.size) {
            val node = nodes[i]
            val label = node.label!!
            // kin nodes
            val kinNodes: MutableList<INode> = ArrayList()
            for (j in i + 1 until nodes.size) {
                val node2 = nodes[j]
                if (label == node2.label) {
                    kinNodes.add(node2)
                }
            }

            // merge
            val k = kinNodes.size
            i += k
            if (k == 0) {
                nodes2.add(node)
            } else {
                val superNode = makeSuperGroupNode(label, "groups:$label")
                nodes2.add(superNode)
                superNode.addChild(node)
                for (node3 in kinNodes) {
                    superNode.addChild(node3)
                }
            }
            i++
        }
        return nodes2
    }

    /**
     * Collect group members
     *
     * @param groupID       group id
     * @param contactFilter contact filter
     * @return list of members
     */
    private fun collectGroupMembers(groupID: String, contactFilter: String?): List<INode>? {
        // selection
        var selection = ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + "=" + groupID + " AND " + ContactsContract.Data.MIMETYPE + " = " + "'" + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "'"
        val nullSource = contactFilter.isNullOrEmpty()
        val extraSelection = if (nullSource) null else Settings.SELECTION_DN_LIKE
        if (extraSelection != null) {
            selection += " AND $extraSelection"
        }
        val selectionArgs = if (nullSource) null else arrayOf("%$contactFilter%")

        // sort
        val sort =
            (if (sortCode == 1) ContactsContract.CommonDataKinds.GroupMembership.DISPLAY_NAME else ContactsContract.CommonDataKinds.GroupMembership.DISPLAY_NAME_ALTERNATIVE) + " COLLATE LOCALIZED ASC," + ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID

        val contentResolver = context!!.contentResolver
        val cursor = contentResolver.query( //
            Settings.GROUP_MEMBERS_URI,  //
            Settings.GROUP_MEMBERS_PROJECTION,  //
            selection,  //
            selectionArgs,  //
            sort //
        )
        var nodes: MutableList<INode>? = null
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                nodes = ArrayList()
                do {
                    val contactId = cursor.getString(Settings.GROUP_MEMBERS_CONTACT_ID_INDEX)
                    val name = cursor.getString(Settings.GROUP_MEMBERS_DISPLAY_NAME_INDEX)
                    val altName = cursor.getString(Settings.GROUP_MEMBERS_DISPLAY_NAME_ALTERNATIVE_INDEX)
                    val photoThumbnailUri = if (photo) cursor.getString(Settings.GROUP_MEMBERS_PHOTO_THUMBNAIL_URI_INDEX) else null
                    val groupId = cursor.getLong(Settings.GROUP_MEMBERS_GROUP_ID_INDEX)
                    Log.d("\tmember ", "$groupId $contactId $name $altName $photoThumbnailUri")

                    val node = makeContactNode(contactId, name, altName, photoThumbnailUri)
                    nodes.add(node)
                } while (cursor.moveToNext())
            }
            cursor.close()
        }
        return nodes
    }

    /**
     * Collect groups' members
     *
     * @param groupIDs group ids
     * @return list of members
     */
    private fun collectGroupsMembers(vararg groupIDs: String): List<INode?>? {
        val selection = ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + " IN " + joinForIn(groupIDs) + " AND " + ContactsContract.Data.MIMETYPE + " = " + "'" + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "'"
        val sort =
            (if (sortCode == 1) ContactsContract.CommonDataKinds.GroupMembership.DISPLAY_NAME else ContactsContract.CommonDataKinds.GroupMembership.DISPLAY_NAME_ALTERNATIVE) + " COLLATE LOCALIZED ASC, " + ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID

        val contentResolver = context!!.contentResolver
        val cursor = contentResolver.query( //
            Settings.GROUP_MEMBERS_URI,  //
            Settings.GROUP_MEMBERS_PROJECTION,  //
            selection,  //
            null,  //
            sort //
        )
        var nodes: MutableList<INode?>? = null
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                nodes = ArrayList()
                var contactIdCurrent: String? = null
                do {
                    val contactId = cursor.getString(Settings.GROUP_MEMBERS_CONTACT_ID_INDEX)
                    if (contactId == contactIdCurrent) {
                        continue
                    }

                    contactIdCurrent = contactId

                    val name = cursor.getString(Settings.GROUP_MEMBERS_DISPLAY_NAME_INDEX)
                    val altName = cursor.getString(Settings.GROUP_MEMBERS_DISPLAY_NAME_ALTERNATIVE_INDEX)
                    val photoThumbnailUri = if (photo) cursor.getString(Settings.GROUP_MEMBERS_PHOTO_THUMBNAIL_URI_INDEX) else null
                    val groupId = cursor.getLong(Settings.GROUP_MEMBERS_GROUP_ID_INDEX)
                    Log.d("\tmember ", "$groupId $contactId $name $altName $photoThumbnailUri")

                    val node = makeContactNode(contactId, name, altName, photoThumbnailUri)
                    nodes.add(node)
                } while (cursor.moveToNext())
            }
            cursor.close()
        }
        return nodes
    }

    // N O D E   F A C T O R I E S

    /**
     * Make contact node
     *
     * @param id                contact id
     * @param name              name
     * @param altName           alternative name
     * @param photoThumbnailUri photo uri
     * @return contact node
     */
    private fun makeContactNode(id: String, name: String, altName: String, photoThumbnailUri: String? /*, final long photoThumbnailId*/): TreeMutableNode {
        // make label from alt
        val fields = altName.split(", *".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val last = fields[0]

        // obfuscate
        var first: String? = null
        if (fields.size > 1 && fields[1][0] != '#') {
            first = fields[1]
        }

        var label: String?
        if (displayCode == 1) //first last
        {
            label = first
            if (first == null) {
                label = last
            } else {
                label += "\n$last\n"
            }
        } else  // last first
        {
            label = last
            if (first != null) {
                label += "\n$first\n"
            }
        }

        val target = left((if (sortCode == 1) name else altName).replaceFirst("^[@# ]*".toRegex(), ""), LABEL_TRUNCATE_AT)

        // node
        val node = TreeMutableNode(null, id)
        node.label = label
        node.content = name + NL + altName

        // obfuscate
        node.link = Settings.URLSCHEME + id
        node.target = target

        // image
        if (photoThumbnailUri != null) {
            val bitmap = Utils.queryBitmapFromUri(context!!, photoThumbnailUri)!!
            val roundBitmap = Utils.roundCrop(bitmap)
            val scaledUpBitmap = Utils.scale(roundBitmap, 200, 200)
            val scaledDownBitmap = Utils.scale(roundBitmap, 50, 50)
            val image: Image = treebolic.glue.Image(scaledUpBitmap)
            node.image = image

            val data = Utils.bitmapToUrl(scaledDownBitmap)
            node.content = "<table><tr><td valign='top'><img src='" + data + "'/></td><td>" + node.content + "</tr></table>"
        } else {
            node.imageIndex = ImageIndices.CONTACT.ordinal
        }
        contactsCount++
        return node
    }

    /**
     * Make group node
     *
     * @param groupId       group id
     * @param groupSystemId group system id
     * @param title         group title
     * @param accountName   account name
     * @param accountType   account type
     * @return group node
     */
    private fun makeGroupNode(groupId: String, groupSystemId: String?, title: String?, accountName: String?, accountType: String?): TreeMutableNode {
        val node = TreeMutableNode(null, groupId)
        node.label = title
        node.content =
            title + NL + "account: " + accountName + NL + "type: " + accountType + NL + "system: " + groupSystemId
        node.link = Settings.URLSCHEME + groupId
        node.imageIndex = ImageIndices.GROUP.ordinal
        node.backColor = GROUP_BACKCOLOR
        node.foreColor = GROUP_FORECOLOR
        node.edgeColor = GROUP_EDGE_COLOR
        return node
    }

    /**
     * Make super group node
     *
     * @param title   group title
     * @param groupId group id
     * @return super group node
     */
    private fun makeSuperGroupNode(title: String, groupId: String): TreeMutableNode {
        val node = TreeMutableNode(null, null)
        node.label = title
        node.content = groupId
        node.link = null
        node.imageIndex = ImageIndices.GROUP.ordinal
        node.backColor = GROUP_BACKCOLOR
        node.foreColor = GROUP_FORECOLOR
        node.edgeColor = GROUP_EDGE_COLOR
        return node
    }

    /**
     * Left truncation of a string
     *
     * @param str string
     * @param n   n first characters
     * @return left of string
     */
    private fun left(str: String?, @Suppress("SameParameterValue") n: Int): String? {
        if (str == null) {
            return null
        }
        return if (str.length > n) str.substring(0, n).lowercase(Locale.getDefault()) else str.lowercase(Locale.getDefault())
    }

    /**
     * Join for 'in' selection clause
     *
     * @param strings values
     * @return 'in' clause
     */
    private fun joinForIn(strings: Array<out String>): String {
        val sb = StringBuilder()
        var first = true
        sb.append('(')
        for (str in strings) {
            if (first) {
                first = false
            } else {
                sb.append(',')
            }
            sb.append(str)
        }
        sb.append(')')
        return sb.toString()
    }

    // S E T T I N G S

    /**
     * Settings factory
     *
     * @return settings
     */
    private fun makeSettings(): treebolic.model.Settings {
        val settings = treebolic.model.Settings()
        settings.backColor = BACKCOLOR
        settings.nodeBackColor = CONTACT_BACKCOLOR
        settings.nodeForeColor = CONTACT_FORECOLOR
        settings.orientation = "radial"
        settings.fontFace = "SansSerif"
        settings.expansion = .8f
        settings.sweep = 1.3f
        settings.ellipsizeFlag = true
        settings.labelMaxLines = 2
        settings.labelExtraLineFactor = .7f
        settings.hasToolbarFlag = true
        settings.hasStatusbarFlag = true
        settings.focusOnHoverFlag = true
        settings.treeEdgeColor = CONTACT_EDGE_COLOR
        settings.treeEdgeStyle = IEdge.DOT.toInt()
        //settings.defaultNodeImageIndex = ImageIndices.CONTACT.ordinal()
        return settings
    }

    companion object {

        private const val TAG = "ContactProvider"

        /**
         * Image array
         */
        private val images: Array<Image> = arrayOf(
            //
            treebolic.glue.Image(Provider::class.java.getResource("images/root.png")),  // ROOT
            treebolic.glue.Image(Provider::class.java.getResource("images/folder.png")),  // FOLDER
            treebolic.glue.Image(Provider::class.java.getResource("images/contact.png")),  // CONTACT
            treebolic.glue.Image(Provider::class.java.getResource("images/group.png")),  // GROUP
        )

        private const val NL = "<br>"

        // C O L O R S

        /**
         * Background color
         */
        private const val BACKCOLOR = 0x004C41

        // contact
        /**
         * Contact background color
         */
        private const val CONTACT_BACKCOLOR = 0x00968C

        /**
         * Contact foreground color
         */
        private const val CONTACT_FORECOLOR = Colors.LIGHT_GRAY

        /**
         * Contact treeedge color
         */
        private const val CONTACT_EDGE_COLOR = Colors.LIGHT_GRAY

        // group

        /**
         * Group background color
         */
        private const val GROUP_BACKCOLOR = Colors.LIGHT_GRAY

        /**
         * Group foreground color
         */
        private const val GROUP_FORECOLOR = 0x00968C

        /**
         * Group treeedge color
         */
        private const val GROUP_EDGE_COLOR = Colors.LIGHT_GRAY

        // root

        /**
         * Top root background color
         */
        private const val ROOT_BACKCOLOR = Colors.ORANGE

        /**
         * Top root root foreground color
         */
        private const val ROOT_FORECOLOR = Colors.WHITE

        /**
         * Root treeedge color
         */
        private const val ROOT_EDGE_COLOR = Colors.WHITE

        // P A R A M E T E R S

        /**
         * Max children nodes at level 0, 1 .. n. Level is just above leaves. Last value i holds for level i to n.
         */
        private val TREE_MAX_NODES_AT_LEVEL = intArrayOf(10, 3)

        /**
         * Truncation threshold
         */
        private const val LABEL_TRUNCATE_AT = 3

        /**
         * LoadBalancer : edge color
         */
        private const val LOADBALANCING_EDGE_COLOR = Colors.WHITE

        /**
         * LoadBalancer : Edge style
         */
        private const val LOADBALANCING_EDGE_STYLE = IEdge.DOT.toInt() or  /* IEdge.FROMDEF | IEdge.FROMCIRCLE | */IEdge.TOTRIANGLE or IEdge.TOFILL or IEdge.STROKEDEF or IEdge.TODEF

        // C O U N T

        /**
         * Count
         *
         * @param context       context
         * @param contactFilter contact filter
         * @param parameters    parameters
         * @return count
         */
        @JvmStatic
        fun count(context: Context, contactFilter: String?, parameters: Properties): Long {
            val filterGroups = parameters.getProperty(Settings.PARAM_FILTER_GROUPS)
            val groupsParam = parameters.getProperty(Settings.PARAM_GROUPS, "false")
            if ("true" == groupsParam) {
                return sumGroupMembers(context, filterGroups, contactFilter)
            }
            return if (filterGroups == null) countContacts(context, contactFilter) else countContacts(context, contactFilter, filterGroups)
        }

        /**
         * Count contact nodes
         *
         * @param context       context
         * @param contactFilter contact filter
         * @return count of contact nodes
         */
        @JvmStatic
        fun countContacts(context: Context, contactFilter: String?): Long {
            val nullSource = contactFilter.isNullOrEmpty()
            val selection = if (nullSource) null else Settings.SELECTION_DN_LIKE
            val selectionArgs = if (nullSource) null else arrayOf("%$contactFilter%")

            val contentResolver = context.contentResolver
            val cursor = contentResolver.query( //
                Settings.CONTACT_URI,  //
                null,  // Settings.BASE_CONTACT_PROJECTION, // columns to return for each row
                selection,  // selection criteria
                selectionArgs,  // selection criteria arguments to placeholders
                null
            ) // sort order for the returned rows
            return count(cursor)
        }

        /**
         * Collect contact nodes
         *
         * @param context     context
         * @param source      selection
         * @param groupFilter group filter
         * @return count of contact nodes
         */
        private fun countContacts(context: Context, source: String?, groupFilter: String): Long {
            val nullSource = source.isNullOrEmpty()
            var selection = if (nullSource) null else Settings.SELECTION_DN_LIKE
            val selectionArgs = if (nullSource) null else arrayOf("%$source%")

            val extraSelection = ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + " IN " + groupFilter + " AND " + ContactsContract.Data.MIMETYPE + " = " + "'" + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "'"
            selection = if (selection == null) extraSelection else "$selection AND $extraSelection"
            Log.d(TAG, "selection=$selection")

            val contentResolver = context.contentResolver
            val cursor = contentResolver.query( //
                Settings.GROUP_MEMBERS_URI,  //
                null,  //Settings.COUNT_PROJECTION, //Settings.BASE_GROUP_MEMBERS_PROJECTION, // columns to return for each row
                selection,  //
                selectionArgs,  // selection criteria arguments to placeholders
                null
            ) // sort order for the returned rows
            return count(cursor)
        }

        /**
         * Count groups
         *
         * @param context context
         * @return count of groups
         */
        @JvmStatic
        fun countGroups(context: Context): Long {
            val contentResolver = context.contentResolver
            val cursor = contentResolver.query( //
                Settings.GROUP_URI,  //
                null,  // Settings.COUNT_PROJECTION, //Settings.BASE_GROUP_PROJECTION, //
                null,  //
                null,  //
                null //
            )
            return count(cursor)
        }

        /**
         * Count group members
         *
         * @param context       context
         * @param groupID       group id
         * @param contactFilter contact filter
         * @return count of members
         */
        @JvmStatic
        fun countGroupMembers(context: Context, groupID: String, contactFilter: String?): Long {
            // selection
            var selection = ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + "=" + groupID + " AND " + ContactsContract.Data.MIMETYPE + " = " + "'" + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "'"
            val nullSource = contactFilter.isNullOrEmpty()
            val extraSelection = if (nullSource) null else Settings.SELECTION_DN_LIKE
            if (extraSelection != null) {
                selection += " AND $extraSelection"
            }
            val selectionArgs = if (nullSource) null else arrayOf("%$contactFilter%")

            val contentResolver = context.contentResolver
            val cursor = contentResolver.query( //
                Settings.GROUP_MEMBERS_URI,  //
                null,  // Settings.COUNT_PROJECTION, // Settings.BASE_GROUP_MEMBERS_PROJECTION, //
                selection,  //
                selectionArgs,  //
                null //
            )
            return count(cursor)
        }

        /**
         * Count group members
         *
         * @param context       context
         * @param allGroupIDs   space separated group ids
         * @param contactFilter contact filter
         * @return count of members
         */
        private fun sumGroupMembers(context: Context, allGroupIDs: String?, contactFilter: String?): Long {
            var total: Long = 0
            val groupIDs = if (allGroupIDs != null) interpretGroupIDs(allGroupIDs) else getGroups(context)
            for (groupID in groupIDs) {
                total += countGroupMembers(context, groupID, contactFilter)
            }
            return total
        }

        private fun interpretGroupIDs(allGroupIDs: String): Array<String> {
            return allGroupIDs.substring(1, allGroupIDs.length - 1).split(",+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        }

        // G R O U P S

        private fun getGroups(context: Context): Array<String> {
            val contentResolver = context.contentResolver
            val cursor = contentResolver.query( //
                Settings.GROUP_URI,  //
                Settings.BASE_GROUP_PROJECTION,  //
                null,  //
                null,  //
                null //
            )
            val groupIDs: MutableList<String> = ArrayList()
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        val groupId = cursor.getString(Settings.GROUP_ID_INDEX)
                        groupIDs.add(groupId)
                    } while (cursor.moveToNext())
                }
                cursor.close()
            }
            return groupIDs.toTypedArray<String>()
        }

        // H E L P E R S

        private fun count(cursor: Cursor?): Long {
            if (cursor != null) {
                val count = cursor.count.toLong()
                cursor.close()
                Log.d(TAG, "count $count")
                return count
            }
            return -1L
        }
    }
}
