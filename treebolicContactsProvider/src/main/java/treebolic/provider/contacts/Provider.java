package treebolic.provider.contacts;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.ContactsContract;
import android.util.Log;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import treebolic.ILocator;
import treebolic.glue.Color;
import treebolic.glue.Image;
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
	public enum ImageIndices
	{
		ROOT, FOLDER, CONTACT, GROUP,
	}

	/**
	 * Image array
	 */
	static Image[] images = new Image[]{ //
			Image.try_make(Provider.class.getResource("images/root.png")), // ROOT
			Image.try_make(Provider.class.getResource("images/folder.png")), // FOLDER
			Image.try_make(Provider.class.getResource("images/contact.png")), // CONTACT
			Image.try_make(Provider.class.getResource("images/group.png")), // GROUP
	};

	private static final String NL = "<br>";

	// COLORS

	/**
	 * Background color
	 */
	static private Color backgroundColor = new Color(0x004C41);

	// contact

	/**
	 * Contact background color
	 */
	public static final Color contactBackColor = new Color(0x00968C);

	/**
	 * Contact foreground color
	 */
	public static final Color contactForeColor = Color.LIGHT_GRAY;

	/**
	 * Contact treeedge color
	 */
	private static final Color contactEdgeColor = Color.LIGHT_GRAY;

	// group

	/**
	 * Group background color
	 */
	public static final Color groupBackColor = Color.LIGHT_GRAY;

	/**
	 * Group foreground color
	 */
	public static final Color groupForeColor = new Color(0x00968C);

	/**
	 * Group treeedge color
	 */
	private static final Color groupEdgeColor = Color.LIGHT_GRAY;

	// folder

	/**
	 * Folder root background color
	 */
	private static final Color folderBackColor = contactForeColor;

	/**
	 * Folder foreground color
	 */
	public static final Color folderForeColor = contactBackColor;

	/**
	 * Folder treeedge color
	 */
	private static final Color folderEdgeColor = Color.CYAN;

	// root

	/**
	 * Top root background color
	 */
	private static final Color rootBackColor = Color.ORANGE;

	/**
	 * Top root root foreground color
	 */
	private static final Color rootForeColor = Color.WHITE;

	/**
	 * Root treeedge color
	 */
	private static final Color rootEdgeColor = Color.WHITE;

	// P A R A M E T E R S

	/**
	 * Max children nodes at level 0, 1 .. n. Level is just above leaves. Last value i holds for level i to n.
	 */
	public static int[] TREE_MAX_NODES_AT_LEVEL = {10, 3};

	/**
	 * Truncation threshold
	 */
	static int LABEL_TRUNCATE_AT = 3;

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
	 * Filter group directive, format = "(g1,g2,...gn)" or null if no filter
	 */
	private String filterGroups = null;

	/**
	 * Load balancer
	 */
	private LoadBalancer loadBalancer;

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
		this.loadBalancer = new LoadBalancer(TREE_MAX_NODES_AT_LEVEL, LABEL_TRUNCATE_AT, this.folderBackColor, this.folderForeColor, this.folderEdgeColor, -1 /*ImageIndices.FOLDER.ordinal() */, null);
	}

	// I P R O V I D E R  I N T E R F A C E  (S E T U P)

	@Override
	public void setHandle(final Object thisHandle)
	{
		this.context = (Context) thisHandle;
	}

	@Override
	public void setContext(final IProviderContext thisProviderContext)
	{
		// not needed
	}

	@Override
	public void setLocator(final ILocator thisContext)
	{
		// not needed
	}

	// I P R O V I D E R  I N T E R F A C E   (M O D E L)

	@Override
	public Model makeModel(final String source, final URL base, final Properties parameters)
	{
		final Tree tree = makeTree(source, base, parameters, false);
		final treebolic.model.Settings settings = makeSettings();
		final Image[] images = Provider.images;
		return new Model(tree, settings, images);
	}

	@Override
	public Tree makeTree(final String source, final URL base, final Properties parameters, final boolean checkRecursion)
	{
		return buildTree(source, parameters);
	}

	// T R E E   F A C T O R Y

	/**
	 * Tree factory
	 *
	 * @param source          source
	 * @param theseParameters parameters
	 * @return tree
	 */
	private Tree buildTree(final String source, final Properties theseParameters)
	{
		if (theseParameters != null)
		{
			final String display = theseParameters.getProperty(Settings.PARAM_DISPLAY);
			if (Settings.VALUE_DISPLAY_FIRST_LAST.equals(display))
			{
				this.displayCode = 1;
			}
			else if (Settings.VALUE_DISPLAY_FIRST_LAST.equals(display))
			{
				this.displayCode = 2;
			}

			final String sort = theseParameters.getProperty(Settings.PARAM_SORT);
			if (Settings.VALUE_SORT_FIRST.equals(sort))
			{
				this.sortCode = 1;
			}
			else if (Settings.VALUE_SORT_LAST.equals(sort))
			{
				this.sortCode = 2;
			}

			final String mergeGroups = theseParameters.getProperty(Settings.PARAM_MERGE_GROUPS, "true");
			this.mergeGroups = "true".equals(mergeGroups);

			this.filterGroups = theseParameters.getProperty(Settings.PARAM_FILTER_GROUPS);

			// start

			final String groups = theseParameters.getProperty(Settings.PARAM_GROUPS, "false");
			if ("true".equals(groups))
			{
				return buildTree(this.loadBalancer.buildHierarchy(collectGroups(), 0));
			}

			return buildTree(this.loadBalancer.buildHierarchy(this.filterGroups == null ? collectContacts(source) : collectContacts(source, this.filterGroups), 0));
		}
		else
		{
			return buildTree(this.loadBalancer.buildHierarchy(collectGroups(), 0));
		}
	}

	/**
	 * Tree factory
	 *
	 * @param nodes contact nodes
	 * @return tree of contact nodes
	 */
	private Tree buildTree(final List<INode> nodes)
	{
		final TreeMutableNode topRoot = new TreeMutableNode(null, "root");
		//topRoot.setLabel("contacts");
		topRoot.setBackColor(Provider.rootBackColor);
		topRoot.setForeColor(Provider.rootForeColor);
		topRoot.setEdgeColor(Provider.rootEdgeColor);
		topRoot.setEdgeStyle(IEdge.SOLID | IEdge.TOTRIANGLE | IEdge.TOFILL);
		topRoot.setImageIndex(ImageIndices.ROOT.ordinal());

		LoadBalancer.attachToParent(topRoot, nodes);
		return new Tree(topRoot, null);
	}

	// L E A V E S

	/**
	 * Collect contact nodes
	 *
	 * @param source selection
	 * @return list of contact nodes
	 */
	private List<INode> collectContacts(final String source)
	{
		final boolean nullSource = source == null || source.isEmpty();
		final String selection = nullSource ? null : Settings.SELECTION_DN_LIKE;
		final String[] selectionArgs = nullSource ? null : new String[]{"%" + source + "%"};
		final String sort = this.sortCode == 1 ? ContactsContract.Contacts.DISPLAY_NAME : ContactsContract.Contacts.DISPLAY_NAME_ALTERNATIVE + " COLLATE LOCALIZED ASC";

		final ContentResolver contentResolver = this.context.getContentResolver();
		final Cursor cursor = contentResolver.query( //
				ContactsContract.Contacts.CONTENT_URI, //
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
					final String id = cursor.getString(Settings.ID_INDEX);
					final String name = cursor.getString(Settings.DISPLAY_NAME_INDEX);
					final String altName = cursor.getString(Settings.DISPLAY_NAME_ALTERNATIVE_INDEX);
					final String photoThumbnailUri = cursor.getString(Settings.PHOTO_THUMBNAIL_URI_INDEX);
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
	 * @param source selection
	 * @param groupFilter group filter
	 * @return list of contact nodes
	 */
	private List<INode> collectContacts(final String source, final String groupFilter)
	{
		final boolean nullSource = source == null || source.isEmpty();
		String selection = nullSource ? null : Settings.SELECTION_DN_LIKE;
		final String[] selectionArgs = nullSource ? null : new String[]{"%" + source + "%"};
		final String sort = this.sortCode == 1 ? ContactsContract.Contacts.DISPLAY_NAME : ContactsContract.Contacts.DISPLAY_NAME_ALTERNATIVE + " COLLATE LOCALIZED ASC";

		final String extraSelection = ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + " IN " + groupFilter + " AND " + ContactsContract.Data.MIMETYPE + " = " + "'" + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "'";
		selection = selection == null ? extraSelection : " AND " + extraSelection;
		Log.d(TAG, "selection=" + selection);

		final ContentResolver contentResolver = this.context.getContentResolver();
		final Cursor cursor = contentResolver.query( //
				ContactsContract.Data.CONTENT_URI, //
				treebolic.provider.contacts.Settings.GROUP_MEMBERS_PROJECTION, // columns to return for each row
				selection, //
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
					final String id = cursor.getString(Settings.GROUP_MEMBERS_ID_INDEX);
					final String name = cursor.getString(Settings.GROUP_MEMBERS_DISPLAY_NAME_INDEX);
					final String altName = cursor.getString(Settings.GROUP_MEMBERS_DISPLAY_NAME_ALTERNATIVE_INDEX);
					final String photoThumbnailUri = cursor.getString(Settings.GROUP_MEMBERS_PHOTO_THUMBNAIL_URI_INDEX);
					//final String photoUri = cursor.getString(Settings.GROUP_MEMBERS_PHOTO_URI_INDEX);
					//final Long photoThumbnailId = cursor.getLong(Settings.GROUP_MEMBERS_PHOTO_THUMBNAIL_ID_INDEX);
					//final Long photoId = cursor.getLong(Settings.GROUP_MEMBERS_PHOTO_ID_INDEX);

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
	 * Collect groups
	 *
	 * @return list of groups (with members)
	 */
	private List<INode> collectGroups()
	{
		final ContentResolver contentResolver = this.context.getContentResolver();
		final Cursor cursor = contentResolver.query( //
				ContactsContract.Groups.CONTENT_URI, //
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
					final String titleRes = cursor.getString(Settings.GROUP_TITLE_RES_INDEX);
					if (titleRes != null)
					{
						int resId = Integer.parseInt(titleRes);
						final String resPackage = cursor.getString(Settings.GROUP_RES_PACKAGE_INDEX);
						title = this.context.getPackageManager().getText(resPackage, resId, null);
					}
					if ("Starred in Android".equals(title))
					{
						title = "Starred";
					}
					final String accountName = cursor.getString(Settings.GROUP_ACCOUNT_NAME);
					final String accountType = cursor.getString(Settings.GROUP_ACCOUNT_TYPE);
					Log.d("group ", groupId + " " + groupSystemId + " " + title + " " + titleRes + " " + accountName + " " + accountType);

					// members
					final List<INode> memberNodes = collectGroupMembers(groupId);
					if (memberNodes != null && memberNodes.size() > 0)
					{
						// group
						final TreeMutableNode node = makeGroupNode(groupId, groupSystemId, title.toString(), titleRes, accountName, accountType);
						nodes.add(node);

						final List<INode> subNodes = this.loadBalancer.buildHierarchy(memberNodes, 0);
						LoadBalancer.attachToParent(node, subNodes);
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
				final TreeMutableNode superNode = makeSuperGroupNode(label, label);
				nodes2.add(superNode);
				LoadBalancer.addToParent(node, superNode);
				for (INode node3 : kinNodes)
				{
					LoadBalancer.addToParent(node3, superNode);
				}
			}
		}
		return nodes2;
	}

	/**
	 * Collect group members
	 *
	 * @param groupID group id
	 * @return list of members
	 */
	private List<INode> collectGroupMembers(final String groupID)
	{
		final String selection = ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + "=" + groupID + " AND " + ContactsContract.Data.MIMETYPE + " = " + "'" + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "'";
		final String sort = this.sortCode == 1 ? ContactsContract.CommonDataKinds.GroupMembership.DISPLAY_NAME : ContactsContract.CommonDataKinds.GroupMembership.DISPLAY_NAME_ALTERNATIVE + " COLLATE LOCALIZED ASC";

		final ContentResolver contentResolver = this.context.getContentResolver();
		final Cursor cursor = contentResolver.query( //
				ContactsContract.Data.CONTENT_URI, //
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
				do
				{
					final String contactId = cursor.getString(Settings.GROUP_MEMBERS_ID_INDEX);
					final String name = cursor.getString(Settings.GROUP_MEMBERS_DISPLAY_NAME_INDEX);
					final String altName = cursor.getString(Settings.GROUP_MEMBERS_DISPLAY_NAME_ALTERNATIVE_INDEX);
					final String photoThumbnailUri = cursor.getString(Settings.GROUP_MEMBERS_PHOTO_THUMBNAIL_URI_INDEX);
					//final String photoUri = cursor.getString(Settings.GROUP_MEMBERS_PHOTO_URI_INDEX);
					//final Long thumbnailId = cursor.getLong(Settings.GROUP_MEMBERS_PHOTO_ID_INDEX);
					//final Long photoId = cursor.getLong(Settings.GROUP_MEMBERS_PHOTO_FILE_ID_INDEX);
					final Long groupId = cursor.getLong(Settings.GROUP_MEMBERS_GROUP_ID_INDEX);
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
	private List<INode> collectGroupsMembers(final String... groupIDs)
	{
		final String selection = ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + " IN " + joinForIn(groupIDs) + " AND " + ContactsContract.Data.MIMETYPE + " = " + "'" + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "'";
		final String sort = this.sortCode == 1 ? ContactsContract.CommonDataKinds.GroupMembership.DISPLAY_NAME : ContactsContract.CommonDataKinds.GroupMembership.DISPLAY_NAME_ALTERNATIVE + " COLLATE LOCALIZED ASC";

		final ContentResolver contentResolver = this.context.getContentResolver();
		final Cursor cursor = contentResolver.query( //
				ContactsContract.Data.CONTENT_URI, //
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
				do
				{
					final String contactId = cursor.getString(Settings.GROUP_MEMBERS_ID_INDEX);
					final String name = cursor.getString(Settings.GROUP_MEMBERS_DISPLAY_NAME_INDEX);
					final String altName = cursor.getString(Settings.GROUP_MEMBERS_DISPLAY_NAME_ALTERNATIVE_INDEX);
					final String photoThumbnailUri = cursor.getString(Settings.GROUP_MEMBERS_PHOTO_THUMBNAIL_URI_INDEX);
					//final String photoUri = cursor.getString(Settings.GROUP_MEMBERS_PHOTO_URI_INDEX);
					//final Long thumbnailId = cursor.getLong(Settings.GROUP_MEMBERS_PHOTO_ID_INDEX);
					//final Long photoId = cursor.getLong(Settings.GROUP_MEMBERS_PHOTO_FILE_ID_INDEX);
					final Long groupId = cursor.getLong(Settings.GROUP_MEMBERS_GROUP_ID_INDEX);
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
	 * Make contact node
	 *
	 * @param id                contact id
	 * @param name              name
	 * @param altName           alternative name
	 * @param photoThumbnailUri photo uri
	 * @return contact node
	 */
	private TreeMutableNode makeContactNode(final String id, final String name, final String altName, final String photoThumbnailUri/*, final long photoThumbnailId*/)
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

		String label = null;
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
			final Bitmap bitmap = Utils.queryThumbnailPhotoFromPhotoUri(this.context, photoThumbnailUri);
			final Bitmap roundBitmap = Utils.roundCrop(bitmap);
			final Bitmap scaledupBitmap = Utils.scale(roundBitmap, 200, 200);
			final Bitmap scaleddownBitmap = Utils.scale(roundBitmap, 50, 50);
			final Image image = new Image(scaledupBitmap);
			node.setImage(image);

			final String data = Utils.bitmapToUrl(scaleddownBitmap);
			node.setContent("<table><tr><td valign='top'><img src='" + data + "'/></td><td>" + node.getContent() + "</tr></table>");
		}
		/*
		else if (photoThumbnailId != 0)
		{
			final Bitmap bitmap = Utils.queryThumbnailPhotoFromPhotoId(this.context, photoThumbnailId);
			final Bitmap roundBitmap = Utils.roundCrop(bitmap);
			final Bitmap scaledupBitmap = Utils.scale(roundBitmap, 200, 200);
			final Image image = new Image(scaledupBitmap);
			node.setImage(image);
		}
		*/
		else
		{
			node.setImageIndex(ImageIndices.CONTACT.ordinal());
		}
		return node;
	}

	/**
	 * Make group node
	 *
	 * @param groupId       group id
	 * @param groupSystemId group system id
	 * @param title         group title
	 * @param titleRes      group title res
	 * @param accountName   account name
	 * @param accountType   account type
	 * @return group node
	 */
	private TreeMutableNode makeGroupNode(final String groupId, final String groupSystemId, final String title, final String titleRes, final String accountName, final String accountType)
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
	private TreeMutableNode makeSuperGroupNode(final String title, final String groupId)
	{
		final TreeMutableNode node = new TreeMutableNode(null, null);
		node.setLabel(title);
		node.setContent(title);
		node.setLink(null);
		node.setImageIndex(ImageIndices.GROUP.ordinal());
		node.setBackColor(groupBackColor);
		node.setForeColor(groupForeColor);
		node.setEdgeColor(groupEdgeColor);
		return node;
	}

	// H E L P E R S

	/**
	 * Left truncation of a string
	 *
	 * @param str string
	 * @param n   n first characters
	 * @return left of string
	 */
	private String left(final String str, final int n)
	{
		if (str == null)
		{
			return null;
		}
		return str.length() > n ? str.substring(0, n).toLowerCase() : str.toLowerCase();
	}

	/**
	 * Join for 'in' selection clause
	 *
	 * @param strs values
	 * @return 'in' clause
	 */
	private String joinForIn(final String[] strs)
	{
		final StringBuilder sb = new StringBuilder();
		boolean first = true;
		sb.append('(');
		for (String str : strs)
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
	treebolic.model.Settings makeSettings()
	{
		final treebolic.model.Settings settings = new treebolic.model.Settings();
		settings.theBackColor = Provider.backgroundColor;
		settings.theNodeBackColor = Provider.contactBackColor;
		settings.theNodeForeColor = Provider.contactForeColor;
		settings.theOrientation = "radial";
		settings.theHasToolbarFlag = true;
		settings.theFontFace = "SansSerif";
		settings.theExpansion = .8F;
		settings.theSweep = 1.3F;
		settings.theEllipsizeFlag = true;
		settings.theLabelMaxLines = 2;
		settings.theLabelExtraLineFactor = .7F;
		settings.theHasToolbarFlag = true;
		settings.theHasStatusbarFlag = true;
		settings.theFocusOnHoverFlag = true;
		settings.theTreeEdgeColor = Provider.contactEdgeColor;
		settings.theTreeEdgeStyle = (int) IEdge.DOT;
		//settings.theDefaultNodeImageIndex = ImageIndices.CONTACT.ordinal();
		return settings;
	}
}
