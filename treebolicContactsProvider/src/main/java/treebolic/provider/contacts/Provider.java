/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>
 */

package treebolic.provider.contacts;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Build;
import android.provider.ContactsContract;
import android.util.Log;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import treebolic.ILocator;
import treebolic.glue.iface.Colors;
import treebolic.glue.iface.Image;
import treebolic.model.IEdge;
import treebolic.model.INode;
import treebolic.model.Model;
import treebolic.model.Tree;
import treebolic.model.TreeMutableNode;
import treebolic.provider.IProvider;
import treebolic.provider.IProviderContext;
import treebolic.provider.LoadBalancer;

import static treebolic.provider.contacts.Settings.URLSCHEME;

/**
 * Provider for Contacts
 *
 * @author <a href="mailto:1313ou@gmail.com">Bernard Bou</a>
 */
public class Provider implements IProvider
{
	private static final String TAG = "ContactProvider";

	// S E T T I N G S  D A T A

	/**
	 * Ordinals used as indexes of images
	 */
	private enum ImageIndices
	{ROOT, FOLDER, CONTACT, GROUP,}

	/**
	 * Image array
	 */
	@Nullable
	private static final Image[] images = new Image[]{ //
			new treebolic.glue.Image(Provider.class.getResource("images/root.png")), // ROOT
			new treebolic.glue.Image(Provider.class.getResource("images/folder.png")), // FOLDER
			new treebolic.glue.Image(Provider.class.getResource("images/contact.png")), // CONTACT
			new treebolic.glue.Image(Provider.class.getResource("images/group.png")), // GROUP
	};

	private static final String NL = "<br>";

	// COLORS

	/**
	 * Background color
	 */
	static private final Integer backgroundColor = 0x004C41;

	// contact

	/**
	 * Contact background color
	 */
	private static final Integer contactBackColor = 0x00968C;

	/**
	 * Contact foreground color
	 */
	private static final Integer contactForeColor = Colors.LIGHT_GRAY;

	/**
	 * Contact treeedge color
	 */
	private static final Integer contactEdgeColor = Colors.LIGHT_GRAY;

	// group

	/**
	 * Group background color
	 */
	private static final Integer groupBackColor = Colors.LIGHT_GRAY;

	/**
	 * Group foreground color
	 */
	private static final Integer groupForeColor = 0x00968C;

	/**
	 * Group treeedge color
	 */
	private static final Integer groupEdgeColor = Colors.LIGHT_GRAY;

	// folder

	/**
	 * Folder root background color
	 */
	private static final Integer folderBackColor = contactForeColor;

	/**
	 * Folder foreground color
	 */
	private static final Integer folderForeColor = contactBackColor;

	/**
	 * Folder treeedge color
	 */
	private static final Integer folderEdgeColor = Colors.CYAN;

	// root

	/**
	 * Top root background color
	 */
	private static final Integer rootBackColor = Colors.ORANGE;

	/**
	 * Top root root foreground color
	 */
	private static final Integer rootForeColor = Colors.WHITE;

	/**
	 * Root treeedge color
	 */
	private static final Integer rootEdgeColor = Colors.WHITE;

	// P A R A M E T E R S

	/**
	 * Max children nodes at level 0, 1 .. n. Level is just above leaves. Last value i holds for level i to n.
	 */
	private static final int[] TREE_MAX_NODES_AT_LEVEL = {10, 3};

	/**
	 * Truncation threshold
	 */
	private static final int LABEL_TRUNCATE_AT = 3;

	/**
	 * LoadBalancer : edge color
	 */
	static private final Integer LOADBALANCING_EDGE_COLOR = Colors.WHITE;

	/**
	 * LoadBalancer : Edge style
	 */
	static private final int LOADBALANCING_EDGE_STYLE = IEdge.DOT | /* IEdge.FROMDEF | IEdge.FROMCIRCLE | */IEdge.TOTRIANGLE | IEdge.TOFILL | IEdge.STROKEDEF | IEdge.TODEF;

	/**
	 * Sort code 1=first name-last name, 2=last name, first name
	 */
	private int displayCode = 0;

	/**
	 * Sort code 1=first name, 2=last name
	 */
	private int sortCode = 0;

	/**
	 * Whether to merge same-name groups
	 */
	private boolean mergeGroups = true;

	/**
	 * Whether to include photos
	 */
	private boolean photo = false;

	/**
	 * Load balancer
	 */
	@NonNull
	private final LoadBalancer loadBalancer;

	/**
	 * Node count
	 */
	private int contactsCount;

	// C O N T E X T

	/**
	 * Android context
	 */
	private Context context;

	// C O N S T R U C T

	/**
	 * Constructor
	 */
	public Provider()
	{
		this.loadBalancer = new LoadBalancer(TREE_MAX_NODES_AT_LEVEL, LABEL_TRUNCATE_AT);
		this.loadBalancer.setGroupNode(null, null, null, LOADBALANCING_EDGE_COLOR, LOADBALANCING_EDGE_STYLE, -1 /*ImageIndices.FOLDER.ordinal() */, null, null);
	}

	// C O U N T

	/**
	 * Count
	 *
	 * @param context       context
	 * @param contactFilter contact filter
	 * @param parameters    parameters
	 * @return count
	 */
	static public long count(@NonNull final Context context, @Nullable final String contactFilter, @NonNull final Properties parameters)
	{
		final String filterGroups = parameters.getProperty(Settings.PARAM_FILTER_GROUPS);
		final String groupsParam = parameters.getProperty(Settings.PARAM_GROUPS, "false");
		if ("true".equals(groupsParam))
		{
			return sumGroupMembers(context, filterGroups, contactFilter);
		}
		return filterGroups == null ? countContacts(context, contactFilter) : countContacts(context, contactFilter, filterGroups);
	}

	/**
	 * Count contact nodes
	 *
	 * @param context       context
	 * @param contactFilter contact filter
	 * @return count of contact nodes
	 */
	static public long countContacts(@NonNull final Context context, @Nullable final String contactFilter)
	{
		final boolean nullSource = contactFilter == null || contactFilter.isEmpty();
		final String selection = nullSource ? null : Settings.SELECTION_DN_LIKE;
		final String[] selectionArgs = nullSource ? null : new String[]{"%" + contactFilter + "%"};

		final ContentResolver contentResolver = context.getContentResolver();
		final Cursor cursor = contentResolver.query( //
				Settings.CONTACT_URI, //
				null, // Settings.BASE_CONTACT_PROJECTION, // columns to return for each row
				selection, // selection criteria
				selectionArgs, // selection criteria arguments to placeholders
				null); // sort order for the returned rows
		return count(cursor);
	}

	/**
	 * Collect contact nodes
	 *
	 * @param context     context
	 * @param source      selection
	 * @param groupFilter group filter
	 * @return list of contact nodes
	 */
	static private long countContacts(@NonNull final Context context, @Nullable final String source, final String groupFilter)
	{
		final boolean nullSource = source == null || source.isEmpty();
		String selection = nullSource ? null : Settings.SELECTION_DN_LIKE;
		final String[] selectionArgs = nullSource ? null : new String[]{"%" + source + "%"};

		final String extraSelection = ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + " IN " + groupFilter + " AND " + ContactsContract.Data.MIMETYPE + " = " + "'" + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "'";
		selection = selection == null ? extraSelection : selection + " AND " + extraSelection;
		Log.d(TAG, "selection=" + selection);

		final ContentResolver contentResolver = context.getContentResolver();
		final Cursor cursor = contentResolver.query( //
				Settings.GROUP_MEMBERS_URI, //
				null, //Settings.COUNT_PROJECTION, //Settings.BASE_GROUP_MEMBERS_PROJECTION, // columns to return for each row
				selection, //
				selectionArgs, // selection criteria arguments to placeholders
				null); // sort order for the returned rows
		return count(cursor);
	}

	/**
	 * Count groups
	 *
	 * @param context context
	 * @return count of groups
	 */
	static public long countGroups(@NonNull final Context context)
	{
		final ContentResolver contentResolver = context.getContentResolver();
		final Cursor cursor = contentResolver.query( //
				Settings.GROUP_URI, //
				null, // Settings.COUNT_PROJECTION, //Settings.BASE_GROUP_PROJECTION, //
				null, //
				null, //
				null //
		);
		return count(cursor);
	}

	/**
	 * Count group members
	 *
	 * @param context       context
	 * @param groupID       group id
	 * @param contactFilter contact filter
	 * @return count of members
	 */
	static public long countGroupMembers(@NonNull final Context context, final String groupID, @Nullable final String contactFilter)
	{
		// selection
		String selection = ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + "=" + groupID + " AND " + ContactsContract.Data.MIMETYPE + " = " + "'" + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "'";
		final boolean nullSource = contactFilter == null || contactFilter.isEmpty();
		String extraSelection = nullSource ? null : Settings.SELECTION_DN_LIKE;
		if (extraSelection != null)
		{
			selection += " AND " + extraSelection;
		}
		final String[] selectionArgs = nullSource ? null : new String[]{"%" + contactFilter + "%"};

		final ContentResolver contentResolver = context.getContentResolver();
		final Cursor cursor = contentResolver.query( //
				Settings.GROUP_MEMBERS_URI, //
				null, // Settings.COUNT_PROJECTION, // Settings.BASE_GROUP_MEMBERS_PROJECTION, //
				selection, //
				selectionArgs,  //
				null //
		);
		return count(cursor);
	}

	/**
	 * Count group members
	 *
	 * @param context       context
	 * @param allGroupIDs   space separated group ids
	 * @param contactFilter contact filter
	 * @return count of members
	 */
	@SuppressWarnings("WeakerAccess")
	static public long sumGroupMembers(@NonNull final Context context, @Nullable final String allGroupIDs, @Nullable final String contactFilter)
	{
		long total = 0;
		final String[] groupIDs = allGroupIDs != null ? interpretGroupIDs(allGroupIDs) : getGroups(context);
		for (String groupID : groupIDs)
		{
			total += countGroupMembers(context, groupID, contactFilter);
		}
		return total;
	}

	@NonNull
	static private String[] interpretGroupIDs(@NonNull final String allGroupIDs)
	{
		return allGroupIDs.substring(1, allGroupIDs.length() - 1).split(",+");
	}

	// I P R O V I D E R  I N T E R F A C E  (S E T U P)

	@Override
	public void setHandle(final Object handle)
	{
		this.context = (Context) handle;
	}

	@SuppressWarnings("EmptyMethod")
	@Override
	public void setContext(final IProviderContext providerContext)
	{
		// not needed
	}

	@SuppressWarnings("EmptyMethod")
	@Override
	public void setLocator(final ILocator context)
	{
		// not needed
	}

	// I P R O V I D E R  I N T E R F A C E   (M O D E L)

	@Override
	public Model makeModel(@Nullable final String source, final URL base, final Properties parameters)
	{
		final Tree tree = makeTree(source, base, parameters, false);
		final treebolic.model.Settings settings = makeSettings();
		final Image[] images = Provider.images;
		return new Model(tree, settings, images);
	}

	@SuppressWarnings("WeakerAccess")
	@Override
	public Tree makeTree(@Nullable final String source, final URL base, final Properties parameters, final boolean checkRecursion)
	{
		this.contactsCount = 0;
		final Tree tree = buildTree(source, parameters);
		final TreeMutableNode root = (TreeMutableNode) tree.getRoot();
		root.setContent(Integer.toString(this.contactsCount));
		return tree;
	}

	// T R E E   F A C T O R Y

	/**
	 * Tree factory
	 *
	 * @param contactFilter contact filter
	 * @param parameters    parameters
	 * @return tree
	 */
	@NonNull
	private Tree buildTree(@Nullable final String contactFilter, @Nullable final Properties parameters)
	{
		if (parameters != null)
		{
			final String displayParam = parameters.getProperty(Settings.PARAM_DISPLAY);
			if (Settings.VALUE_DISPLAY_FIRST_LAST.equals(displayParam))
			{
				this.displayCode = 1;
			}
			else if (Settings.VALUE_DISPLAY_LAST_FIRST.equals(displayParam))
			{
				this.displayCode = 2;
			}

			final String sortParam = parameters.getProperty(Settings.PARAM_SORT);
			if (Settings.VALUE_SORT_FIRST.equals(sortParam))
			{
				this.sortCode = 1;
			}
			else if (Settings.VALUE_SORT_LAST.equals(sortParam))
			{
				this.sortCode = 2;
			}

			final String mergeGroupsParam = parameters.getProperty(Settings.PARAM_MERGE_GROUPS, "true");
			this.mergeGroups = "true".equals(mergeGroupsParam);

			final String photoParam = parameters.getProperty(Settings.PARAM_PHOTO, "false");
			this.photo = "true".equals(photoParam);

			// start

			final String groupsParam = parameters.getProperty(Settings.PARAM_GROUPS, "false");
			boolean groups = "true".equals(groupsParam);
			if (groups)
			{
				return buildTree(this.loadBalancer.buildHierarchy(collectGroups(contactFilter), 0));
			}

			final String filterGroupsParam = parameters.getProperty(Settings.PARAM_FILTER_GROUPS);
			return buildTree(this.loadBalancer.buildHierarchy(filterGroupsParam == null ? collectContacts(contactFilter) : collectContacts(contactFilter, filterGroupsParam), 0));
		}
		else
		{
			return buildTree(this.loadBalancer.buildHierarchy(collectGroups(contactFilter), 0));
		}
	}

	/**
	 * Tree factory
	 *
	 * @param nodes contact nodes
	 * @return tree of contact nodes
	 */
	@NonNull
	private Tree buildTree(final List<INode> nodes)
	{
		final TreeMutableNode topRoot = new TreeMutableNode(null, "root");
		//topRoot.setLabel("contacts");
		topRoot.setBackColor(Provider.rootBackColor);
		topRoot.setForeColor(Provider.rootForeColor);
		topRoot.setEdgeColor(Provider.rootEdgeColor);
		topRoot.setEdgeStyle(IEdge.SOLID | IEdge.TOTRIANGLE | IEdge.TOFILL);
		topRoot.setImageIndex(ImageIndices.ROOT.ordinal());

		topRoot.addChildren(nodes);
		return new Tree(topRoot, null);
	}

	// G R O U P S

	@SuppressWarnings("WeakerAccess")
	@NonNull
	static public String[] getGroups(@NonNull final Context context)
	{
		final ContentResolver contentResolver = context.getContentResolver();
		final Cursor cursor = contentResolver.query( //
				Settings.GROUP_URI, //
				Settings.BASE_GROUP_PROJECTION, //
				null, //
				null, //
				null //
		);
		final List<String> groupIDs = new ArrayList<>();
		if (cursor != null)
		{
			if (cursor.moveToFirst())
			{
				do
				{
					final String groupId = cursor.getString(Settings.GROUP_ID_INDEX);
					groupIDs.add(groupId);
				}
				while (cursor.moveToNext());
			}
			cursor.close();
		}
		return groupIDs.toArray(new String[0]);
	}

	// L E A V E S

	/**
	 * Collect contact nodes
	 *
	 * @param contactFilter contact filter
	 * @return list of contact nodes
	 */
	@Nullable
	private List<INode> collectContacts(@Nullable final String contactFilter)
	{
		final boolean nullSource = contactFilter == null || contactFilter.isEmpty();
		final String selection = nullSource ? null : Settings.SELECTION_DN_LIKE;
		final String[] selectionArgs = nullSource ? null : new String[]{"%" + contactFilter + "%"};
		final String sort = (this.sortCode == 1 ? ContactsContract.Contacts.DISPLAY_NAME : ContactsContract.Contacts.DISPLAY_NAME_ALTERNATIVE) + " COLLATE LOCALIZED ASC";

		final ContentResolver contentResolver = this.context.getContentResolver();
		final Cursor cursor = contentResolver.query( //
				Settings.CONTACT_URI, //
				Settings.CONTACT_PROJECTION, // columns to return for each row
				selection, // selection criteria
				selectionArgs, // selection criteria arguments to placeholders
				sort); // sort order for the returned rows
		List<INode> nodes = null;
		if (cursor != null)
		{
			if (cursor.moveToFirst())
			{
				nodes = new ArrayList<>();
				do
				{
					// row values
					final String id = cursor.getString(Settings.CONTACT_ID_INDEX);
					final String name = cursor.getString(Settings.DISPLAY_NAME_INDEX);
					final String altName = cursor.getString(Settings.DISPLAY_NAME_ALTERNATIVE_INDEX);
					final String photoThumbnailUri = this.photo ? cursor.getString(Settings.PHOTO_THUMBNAIL_URI_INDEX) : null;
					//final String photoUri = cursor.getString(Settings.PHOTO_URI_INDEX);
					//final Long photoThumbnailId = cursor.getLong(Settings.PHOTO_THUMBNAIL_ID_INDEX);
					//final Long photoId = cursor.getLong(Settings.PHOTO_ID_INDEX);
					//final String lookup = cursor.getString(Settings.LOOKUP_KEY_INDEX);

					final TreeMutableNode node = makeContactNode(id, name, altName, photoThumbnailUri);
					nodes.add(node);
				}
				while (cursor.moveToNext());

				Log.d(TAG, "collected nodes " + nodes.size());
			}
			cursor.close();
		}
		return nodes;
	}

	/**
	 * Collect contact nodes
	 *
	 * @param contactFilter contact filter
	 * @param groupFilter   group filter
	 * @return list of contact nodes
	 */
	@Nullable
	private List<INode> collectContacts(@Nullable final String contactFilter, final String groupFilter)
	{
		final boolean nullContactFilter = contactFilter == null || contactFilter.isEmpty();
		String selection = nullContactFilter ? null : Settings.SELECTION_DN_LIKE;
		final String[] selectionArgs = nullContactFilter ? null : new String[]{"%" + contactFilter + "%"};
		final String sort = (this.sortCode == 1 ?
				ContactsContract.CommonDataKinds.GroupMembership.DISPLAY_NAME :
				ContactsContract.CommonDataKinds.GroupMembership.DISPLAY_NAME_ALTERNATIVE) + " COLLATE LOCALIZED ASC," + ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID;

		final String extraSelection = ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + " IN " + groupFilter + " AND " + ContactsContract.Data.MIMETYPE + " = " + "'" + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "'";
		selection = selection == null ? extraSelection : selection + " AND " + extraSelection;
		Log.d(TAG, "selection=" + selection);

		final ContentResolver contentResolver = this.context.getContentResolver();
		final Cursor cursor = contentResolver.query( //
				Settings.GROUP_MEMBERS_URI, //
				Settings.GROUP_MEMBERS_PROJECTION, // columns to return for each row
				selection, //
				selectionArgs, // selection criteria arguments to placeholders
				sort); // sort order for the returned rows
		List<INode> nodes = null;
		if (cursor != null)
		{
			if (cursor.moveToFirst())
			{
				nodes = new ArrayList<>();
				String contactIdCurrent = null;
				do
				{
					final String contactId = cursor.getString(Settings.GROUP_MEMBERS_CONTACT_ID_INDEX);
					if (contactId.equals(contactIdCurrent))
					{
						continue;
					}
					contactIdCurrent = contactId;

					// row values
					final String name = cursor.getString(Settings.GROUP_MEMBERS_DISPLAY_NAME_INDEX);
					final String altName = cursor.getString(Settings.GROUP_MEMBERS_DISPLAY_NAME_ALTERNATIVE_INDEX);
					final String photoThumbnailUri = this.photo ? cursor.getString(Settings.GROUP_MEMBERS_PHOTO_THUMBNAIL_URI_INDEX) : null;
					//final String photoUri = cursor.getString(Settings.GROUP_MEMBERS_PHOTO_URI_INDEX);
					//final Long photoThumbnailId = cursor.getLong(Settings.GROUP_MEMBERS_PHOTO_THUMBNAIL_ID_INDEX);
					//final Long photoId = cursor.getLong(Settings.GROUP_MEMBERS_PHOTO_ID_INDEX);

					final TreeMutableNode node = makeContactNode(contactId, name, altName, photoThumbnailUri);
					nodes.add(node);
				}
				while (cursor.moveToNext());

				Log.d(TAG, "collected nodes " + nodes.size());
			}
			cursor.close();
		}
		return nodes;
	}

	/**
	 * Collect all groups
	 *
	 * @param contactFilter contact filter
	 * @return list of groups (with members)
	 */
	@Nullable
	private List<INode> collectGroups(final String contactFilter)
	{
		final ContentResolver contentResolver = this.context.getContentResolver();
		final Cursor cursor = contentResolver.query( //
				Settings.GROUP_URI, //
				Settings.GROUP_PROJECTION, //
				null, //
				null, //
				ContactsContract.Groups.TITLE + " COLLATE LOCALIZED ASC" //
		);
		List<INode> nodes = null;
		if (cursor != null)
		{
			//final String[] columns = cursor.getColumnNames();
			//Log.d(TAG, "GROUPS " + joinForIn(columns));

			if (cursor.moveToFirst())
			{
				nodes = new ArrayList<>();
				do
				{
					final String groupId = cursor.getString(Settings.GROUP_ID_INDEX);
					final String groupSystemId = cursor.getString(Settings.GROUP_SYSTEM_ID_INDEX);
					CharSequence title = cursor.getString(Settings.GROUP_TITLE_INDEX);

					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
					{
						final String titleRes = cursor.getString(Settings.GROUP_TITLE_RES_INDEX);
						if (titleRes != null)
						{
							int resId = Integer.parseInt(titleRes);
							final String resPackage = cursor.getString(Settings.GROUP_RES_PACKAGE_INDEX);
							if (resPackage != null)
							{
								title = this.context.getPackageManager().getText(resPackage, resId, null);
							}
						}
					}

					if (title != null && "Starred in Android".contentEquals(title))
					{
						title = "Starred";
					}
					final String accountName = cursor.getString(Settings.GROUP_ACCOUNT_NAME);
					final String accountType = cursor.getString(Settings.GROUP_ACCOUNT_TYPE);
					Log.d("group ", groupId + " " + groupSystemId + " " + title + " " + accountName + " " + accountType);

					// members
					final List<INode> memberNodes = collectGroupMembers(groupId, contactFilter);
					if (memberNodes != null && memberNodes.size() > 0)
					{
						// group
						final TreeMutableNode node = makeGroupNode(groupId, groupSystemId, title == null ? "?" : title.toString(), accountName, accountType);
						nodes.add(node);

						final List<INode> subNodes = this.loadBalancer.buildHierarchy(memberNodes, 0);
						node.addChildren(subNodes);
					}
				}
				while (cursor.moveToNext());
			}
			cursor.close();
		}
		if (!this.mergeGroups || nodes == null)
		{
			return nodes;
		}

		// merge
		final List<INode> nodes2 = new ArrayList<>();
		for (int i = 0; i < nodes.size(); i++)
		{
			final INode node = nodes.get(i);
			final String label = node.getLabel();
			assert label != null;

			// kin nodes
			List<INode> kinNodes = new ArrayList<>();
			for (int j = i + 1; j < nodes.size(); j++)
			{
				INode node2 = nodes.get(j);
				if (label.equals(node2.getLabel()))
				{
					kinNodes.add(node2);
				}
			}

			// merge
			int k = kinNodes.size();
			i += k;
			if (k == 0)
			{
				nodes2.add(node);
			}
			else
			{
				final TreeMutableNode superNode = makeSuperGroupNode(label, "groups:" + label);
				nodes2.add(superNode);
				superNode.addChild(node);
				for (INode node3 : kinNodes)
				{
					superNode.addChild(node3);
				}
			}
		}
		return nodes2;
	}

	/**
	 * Collect group members
	 *
	 * @param groupID       group id
	 * @param contactFilter contact filter
	 * @return list of members
	 */
	@Nullable
	private List<INode> collectGroupMembers(final String groupID, @Nullable final String contactFilter)
	{
		// selection
		String selection = ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + "=" + groupID + " AND " + ContactsContract.Data.MIMETYPE + " = " + "'" + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "'";
		final boolean nullSource = contactFilter == null || contactFilter.isEmpty();
		String extraSelection = nullSource ? null : Settings.SELECTION_DN_LIKE;
		if (extraSelection != null)
		{
			selection += " AND " + extraSelection;
		}
		final String[] selectionArgs = nullSource ? null : new String[]{"%" + contactFilter + "%"};

		// sort
		final String sort = (this.sortCode == 1 ?
				ContactsContract.CommonDataKinds.GroupMembership.DISPLAY_NAME :
				ContactsContract.CommonDataKinds.GroupMembership.DISPLAY_NAME_ALTERNATIVE) + " COLLATE LOCALIZED ASC," + ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID;

		final ContentResolver contentResolver = this.context.getContentResolver();
		final Cursor cursor = contentResolver.query( //
				Settings.GROUP_MEMBERS_URI, //
				Settings.GROUP_MEMBERS_PROJECTION, //
				selection, //
				selectionArgs,  //
				sort //
		);
		List<INode> nodes = null;
		if (cursor != null)
		{
			//final String[] columns = cursor.getColumnNames();
			//Log.d(TAG, "MEMBERS " + joinForIn(columns));

			if (cursor.moveToFirst())
			{
				nodes = new ArrayList<>();
				do
				{
					final String contactId = cursor.getString(Settings.GROUP_MEMBERS_CONTACT_ID_INDEX);
					final String name = cursor.getString(Settings.GROUP_MEMBERS_DISPLAY_NAME_INDEX);
					final String altName = cursor.getString(Settings.GROUP_MEMBERS_DISPLAY_NAME_ALTERNATIVE_INDEX);
					final String photoThumbnailUri = this.photo ? cursor.getString(Settings.GROUP_MEMBERS_PHOTO_THUMBNAIL_URI_INDEX) : null;
					//final String photoUri = cursor.getString(Settings.GROUP_MEMBERS_PHOTO_URI_INDEX);
					//final Long thumbnailId = cursor.getLong(Settings.GROUP_MEMBERS_PHOTO_ID_INDEX);
					//final Long photoId = cursor.getLong(Settings.GROUP_MEMBERS_PHOTO_FILE_ID_INDEX);
					final long groupId = cursor.getLong(Settings.GROUP_MEMBERS_GROUP_ID_INDEX);
					Log.d("\tmember ", groupId + " " + contactId + " " + name + " " + altName + " " + photoThumbnailUri);

					final TreeMutableNode node = makeContactNode(contactId, name, altName, photoThumbnailUri);
					nodes.add(node);
				}
				while (cursor.moveToNext());
			}
			cursor.close();
		}
		return nodes;
	}

	/**
	 * Collect groups' members
	 *
	 * @param groupIDs group ids
	 * @return list of members
	 */
	@Nullable
	private List<INode> collectGroupsMembers(@NonNull final String... groupIDs)
	{
		final String selection = ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + " IN " + joinForIn(groupIDs) + " AND " + ContactsContract.Data.MIMETYPE + " = " + "'" + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "'";
		final String sort = (this.sortCode == 1 ?
				ContactsContract.CommonDataKinds.GroupMembership.DISPLAY_NAME :
				ContactsContract.CommonDataKinds.GroupMembership.DISPLAY_NAME_ALTERNATIVE) + " COLLATE LOCALIZED ASC, " + ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID;

		final ContentResolver contentResolver = this.context.getContentResolver();
		final Cursor cursor = contentResolver.query( //
				Settings.GROUP_MEMBERS_URI, //
				Settings.GROUP_MEMBERS_PROJECTION, //
				selection, //
				null,  //
				sort //
		);
		List<INode> nodes = null;
		if (cursor != null)
		{
			//final String[] columns = cursor.getColumnNames();
			//Log.d(TAG, "MEMBERS " + joinForIn(columns));

			if (cursor.moveToFirst())
			{
				nodes = new ArrayList<>();
				String contactIdCurrent = null;
				do
				{
					final String contactId = cursor.getString(Settings.GROUP_MEMBERS_CONTACT_ID_INDEX);
					if (contactId.equals(contactIdCurrent))
					{
						continue;
					}

					contactIdCurrent = contactId;

					final String name = cursor.getString(Settings.GROUP_MEMBERS_DISPLAY_NAME_INDEX);
					final String altName = cursor.getString(Settings.GROUP_MEMBERS_DISPLAY_NAME_ALTERNATIVE_INDEX);
					final String photoThumbnailUri = this.photo ? cursor.getString(Settings.GROUP_MEMBERS_PHOTO_THUMBNAIL_URI_INDEX) : null;
					//final String photoUri = cursor.getString(Settings.GROUP_MEMBERS_PHOTO_URI_INDEX);
					//final Long thumbnailId = cursor.getLong(Settings.GROUP_MEMBERS_PHOTO_ID_INDEX);
					//final Long photoId = cursor.getLong(Settings.GROUP_MEMBERS_PHOTO_FILE_ID_INDEX);
					final long groupId = cursor.getLong(Settings.GROUP_MEMBERS_GROUP_ID_INDEX);
					Log.d("\tmember ", groupId + " " + contactId + " " + name + " " + altName + " " + photoThumbnailUri);

					final TreeMutableNode node = makeContactNode(contactId, name, altName, photoThumbnailUri);
					nodes.add(node);
				}
				while (cursor.moveToNext());
			}
			cursor.close();
		}
		return nodes;
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
	@NonNull
	private TreeMutableNode makeContactNode(final String id, final String name, @NonNull final String altName, @Nullable final String photoThumbnailUri/*, final long photoThumbnailId*/)
	{
		// make label from alt
		final String[] fields = altName.split(", *");
		String last = fields[0];
		// obfuscate
		// last = last.substring(0, 2) + last.substring(2).replaceAll(".", "*");

		String first = null;
		if (fields.length > 1 && fields[1].charAt(0) != '#')
		{
			first = fields[1];
		}

		@SuppressWarnings("UnusedAssignment") String label = null;
		if (this.displayCode == 1) //first last
		{
			label = first;
			if (first == null)
			{
				label = last;
			}
			else if (last != null)
			{
				label += '\n' + last;
			}
		}
		else // last first
		{
			label = last;
			if (last == null)
			{
				label = first;
			}
			else if (first != null)
			{
				label += '\n' + first;
			}
		}
		//final String makeRangeLabel = altName.replace(',','\n');

		final String target = left((this.sortCode == 1 ? name : altName).replaceFirst("^[@# ]*", ""), LABEL_TRUNCATE_AT);

		// node
		final TreeMutableNode node = new TreeMutableNode(null, id);
		node.setLabel(label);
		node.setContent(name + NL + altName);

		// obfuscate
		// String name2 = name.substring(0, 2) + name.substring(2).replaceAll("[A-Z]", "*");
		// int comma = altName.lastIndexOf(',');
		// String altName2 = comma ==-1 ? "" : altName.substring(0, comma).replaceAll("[A-Z]", "*") + altName.substring(comma);
		// node.setContent(name2 + NL + altName2);

		node.setLink(URLSCHEME + id);
		node.setTarget(target);

		// image
		if (photoThumbnailUri != null)
		{
			final Bitmap bitmap = Utils.queryBitmapFromUri(this.context, photoThumbnailUri);
			assert bitmap != null;
			final Bitmap roundBitmap = Utils.roundCrop(bitmap);
			final Bitmap scaledUpBitmap = Utils.scale(roundBitmap, 200, 200);
			final Bitmap scaledDownBitmap = Utils.scale(roundBitmap, 50, 50);
			final Image image = new treebolic.glue.Image(scaledUpBitmap);
			node.setImage(image);

			final String data = Utils.bitmapToUrl(scaledDownBitmap);
			node.setContent("<table><tr><td valign='top'><img src='" + data + "'/></td><td>" + node.getContent() + "</tr></table>");
		}
		/*
		else if (photoThumbnailId != 0)
		{
			final Bitmap bitmap = Utils.queryBitmapFromId(this.context, photoThumbnailId);
			final Bitmap roundBitmap = Utils.roundCrop(bitmap);
			final Bitmap scaledUpBitmap = Utils.scale(roundBitmap, 200, 200);
			final Image image = new Image(scaledUpBitmap);
			node.setImage(image);
		}
		*/
		else
		{
			node.setImageIndex(ImageIndices.CONTACT.ordinal());
		}
		this.contactsCount++;
		return node;
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
	@NonNull
	private TreeMutableNode makeGroupNode(final String groupId, final String groupSystemId, final String title, final String accountName, final String accountType)
	{
		final TreeMutableNode node = new TreeMutableNode(null, groupId);
		node.setLabel(title);
		node.setContent(title + NL + "account: " + accountName + NL + "type: " + accountType + NL + "system: " + groupSystemId);
		node.setLink(URLSCHEME + groupId);
		node.setImageIndex(ImageIndices.GROUP.ordinal());
		node.setBackColor(groupBackColor);
		node.setForeColor(groupForeColor);
		node.setEdgeColor(groupEdgeColor);
		return node;
	}

	/**
	 * Make super group node
	 *
	 * @param title   group title
	 * @param groupId group id
	 * @return super group node
	 */
	@NonNull
	private TreeMutableNode makeSuperGroupNode(final String title, final String groupId)
	{
		final TreeMutableNode node = new TreeMutableNode(null, null);
		node.setLabel(title);
		node.setContent(groupId);
		node.setLink(null);
		node.setImageIndex(ImageIndices.GROUP.ordinal());
		node.setBackColor(groupBackColor);
		node.setForeColor(groupForeColor);
		node.setEdgeColor(groupEdgeColor);
		return node;
	}

	// H E L P E R S

	static private long count(@Nullable final Cursor cursor)
	{
		if (cursor != null)
		{
			long count = cursor.getCount();
			/*
			int countColumnIndex = cursor.getColumnIndex(Settings.COUNT_COL);
			long count = -1;
			if (cursor.moveToFirst())
			{
				count = cursor.getLong(countColumnIndex);
			}
			*/
			cursor.close();
			Log.d(TAG, "count " + count);
			return count;
		}
		return -1L;
	}

	/**
	 * Left truncation of a string
	 *
	 * @param str string
	 * @param n   n first characters
	 * @return left of string
	 */
	@Nullable
	private String left(@Nullable final String str, @SuppressWarnings("SameParameterValue") final int n)
	{
		if (str == null)
		{
			return null;
		}
		return str.length() > n ? str.substring(0, n).toLowerCase(Locale.getDefault()) : str.toLowerCase(Locale.getDefault());
	}

	/**
	 * Join for 'in' selection clause
	 *
	 * @param strings values
	 * @return 'in' clause
	 */
	@NonNull
	private String joinForIn(@NonNull final String[] strings)
	{
		final StringBuilder sb = new StringBuilder();
		boolean first = true;
		sb.append('(');
		for (String str : strings)
		{
			if (first)
			{
				first = false;
			}
			else
			{
				sb.append(',');
			}
			sb.append(str);
		}
		sb.append(')');
		return sb.toString();
	}

	// S E T T I N G S

	/**
	 * Settings factory
	 *
	 * @return settings
	 */
	@NonNull
	private treebolic.model.Settings makeSettings()
	{
		final treebolic.model.Settings settings = new treebolic.model.Settings();
		settings.backColor = Provider.backgroundColor;
		settings.nodeBackColor = Provider.contactBackColor;
		settings.nodeForeColor = Provider.contactForeColor;
		settings.orientation = "radial";
		settings.fontFace = "SansSerif";
		settings.expansion = .8F;
		settings.sweep = 1.3F;
		settings.ellipsizeFlag = true;
		settings.labelMaxLines = 2;
		settings.labelExtraLineFactor = .7F;
		settings.hasToolbarFlag = true;
		settings.hasStatusbarFlag = true;
		settings.focusOnHoverFlag = true;
		settings.treeEdgeColor = Provider.contactEdgeColor;
		settings.treeEdgeStyle = (int) IEdge.DOT;
		//settings.defaultNodeImageIndex = ImageIndices.CONTACT.ordinal();
		return settings;
	}
}
