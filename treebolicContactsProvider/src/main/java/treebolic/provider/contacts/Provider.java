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
import treebolic.model.Model;
import treebolic.model.ModelDump;
import treebolic.model.Tree;
import treebolic.model.TreeMutableNode;
import treebolic.provider.IProvider;
import treebolic.provider.IProviderContext;

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

	private int displayCode = 0;

	private int sortCode = 0;

	private boolean mergeGroups = true;

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

		// start

		final String groups = theseParameters.getProperty(Settings.PARAM_GROUPS, "false");
		if ("true".equals(groups))
		{
			return buildTree(buildHierarchy(collectGroups(), 0));
		}

		return buildTree(buildHierarchy(collectContacts(source), 0));
	}

	/**
	 * Tree factory
	 *
	 * @param nodes contact nodes
	 * @return tree of contact nodes
	 */
	private Tree buildTree(final List<TreeMutableNode> nodes)
	{
		final TreeMutableNode topRoot = new TreeMutableNode(null, "root");
		topRoot.setLabel("contacts");
		topRoot.setBackColor(Provider.rootBackColor);
		topRoot.setForeColor(Provider.rootForeColor);
		topRoot.setEdgeColor(Provider.rootEdgeColor);
		topRoot.setEdgeStyle(IEdge.SOLID | IEdge.TOTRIANGLE | IEdge.TOFILL);
		topRoot.setImageIndex(ImageIndices.ROOT.ordinal());

		attachToParent(topRoot, nodes);
		return new Tree(topRoot, null);
	}

	/**
	 * Attach to parent
	 *
	 * @param parent parent node
	 * @param nodes  children nodes
	 */
	private void attachToParent(final TreeMutableNode parent, final List<TreeMutableNode> nodes)
	{
		if (nodes != null)
		{
			for (TreeMutableNode node : nodes)
			{
				node.addToParent(parent);
			}
		}
	}

	/**
	 * Build a list of tree parent nodes
	 *
	 * @param nodes children nodes
	 * @param level current level
	 * @return list of parent nodes
	 */
	private List<TreeMutableNode> buildHierarchy1(final List<TreeMutableNode> nodes, final int level)
	{
		Log.d(TAG, ">" + level + " nodes " + nodes.size());
		final List<TreeMutableNode> roots = new ArrayList<>();

		int m = TREE_MAX_NODES_AT_LEVEL[level > TREE_MAX_NODES_AT_LEVEL.length - 1 ? TREE_MAX_NODES_AT_LEVEL.length - 1 : level];
		int z = nodes.size();

		// balance
		if (z % m == 1)
		{
			m--;
		}
		for (int i = 0; i < z; i = i + m)
		{
			final TreeMutableNode root = new TreeMutableNode(null, "root" + level + "-" + i);
			root.setLink("directory:"); //$NON-NLS-1$
			root.setBackColor(Provider.folderBackColor);
			root.setForeColor(Provider.folderForeColor);
			root.setEdgeColor(Provider.folderEdgeColor);
			root.setEdgeStyle(IEdge.SOLID | /*IEdge.FROMDEF | IEdge.FROMCIRCLE | */IEdge.TOTRIANGLE | IEdge.TOFILL | IEdge.TODEF);
			//root.setImageIndex(ImageIndices.FOLDER.ordinal());

			int b = Math.min(i + m, z);
			TreeMutableNode first = nodes.get(i);
			TreeMutableNode last = nodes.get(b - 1);

			final String startLabel = left(first.getTarget(), LABEL_TRUNCATE_AT);
			root.setEdgeLabel(startLabel);
			final String rangeLabel = makeRangeLabel(first, last);
			root.setTarget(rangeLabel);
			// root.setLabel(rangeLabel);
			// root.setContent(/* "root" + level + "-" + i + NL  + */ rangeLabel + NL + getNodeLabel(first) + NL + getNodeLabel(last));
			root.setContent(rangeLabel + /* NL + (b - i) + " elements" + */ NL + "<small>" + first.getTarget() + NL + last.getTarget() + "</small>");

			for (int k = i; k < b; k++)
			{
				final TreeMutableNode node = nodes.get(k);
				node.addToParent(root);
			}

			roots.add(root);
		}
		Log.d(TAG, "<" + level + " roots " + roots.size());
		return roots;
	}

	/**
	 * Recursive build hierarchy
	 *
	 * @param nodes nodes at level
	 * @param level level
	 * @return
	 */
	private List<TreeMutableNode> buildHierarchy(final List<TreeMutableNode> nodes, final int level)
	{
		int m = TREE_MAX_NODES_AT_LEVEL[level > TREE_MAX_NODES_AT_LEVEL.length - 1 ? TREE_MAX_NODES_AT_LEVEL.length - 1 : level];
		Log.d(TAG, "level=" + level + "  m=" + m);
		if (nodes == null)
		{
			return null;
		}
		if (nodes.size() <= m)
		{
			return nodes;
		}

		List<TreeMutableNode> nodes2 = buildHierarchy1(nodes, level);
		return buildHierarchy(nodes2, level + 1);
	}

	// L E A V E S

	/**
	 * Collect contact nodes
	 *
	 * @param source selection
	 * @return list of contact nodes
	 */
	private List<TreeMutableNode> collectContacts(final String source)
	{
		final boolean nullSource = source == null || source.isEmpty();
		final String selection = nullSource ? null : Settings.SELECTION_DN_LIKE;
		final String[] selectionArgs = nullSource ? null : new String[]{"%" + source + "%"};
		final String sort = this.sortCode == 1 ? ContactsContract.Contacts.DISPLAY_NAME : ContactsContract.Contacts.DISPLAY_NAME_ALTERNATIVE + " COLLATE LOCALIZED ASC";

		final ContentResolver contentResolver = this.context.getContentResolver();
		final Cursor cursor = contentResolver.query( //
				ContactsContract.Contacts.CONTENT_URI, //
				Settings.PROJECTION, // columns to return for each row
				selection, selectionArgs, // selection criteria arguments to placeholders
				sort); // sort order for the returned rows
		List<TreeMutableNode> nodes = null;
		if (cursor != null)
		{
			if (cursor.moveToFirst())
			{
				nodes = new ArrayList<>();
				do
				{
					// row values
					final String id = cursor.getString(Settings.ID_INDEX);
					final String name = cursor.getString(Settings.NAME_INDEX);
					final String altName = cursor.getString(Settings.ALT_NAME_INDEX);
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
	 * Collect groups
	 *
	 * @return list of groups (with members)
	 */
	private List<TreeMutableNode> collectGroups()
	{
		final ContentResolver contentResolver = this.context.getContentResolver();
		final Cursor cursor = contentResolver.query( //
				ContactsContract.Groups.CONTENT_URI, //
				Settings.GROUP_PROJECTION, //
				null, //
				null, //
				ContactsContract.Groups.TITLE + " COLLATE LOCALIZED ASC" //
		);
		List<TreeMutableNode> nodes = null;
		if (cursor != null)
		{
			//final String[] columns = cursor.getColumnNames();
			//Log.d(TAG, "GROUPS " + join(columns));

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
					final List<TreeMutableNode> memberNodes = collectGroupMembers(groupId);
					if (memberNodes != null && memberNodes.size() > 0)
					{
						// group
						final TreeMutableNode node = makeGroupNode(groupId, groupSystemId, title.toString(), titleRes, accountName, accountType);
						nodes.add(node);

						final List<TreeMutableNode> subNodes = buildHierarchy(memberNodes, 0);
						attachToParent(node, subNodes);
					}
				}
				while (cursor.moveToNext());
			}
			cursor.close();
		}
		if (! this.mergeGroups || nodes == null)
		{
			return nodes;
		}

		// merge
		final List<TreeMutableNode> nodes2 = new ArrayList<>();
		for (int i = 0; i < nodes.size(); i++)
		{
			final TreeMutableNode node = nodes.get(i);
			final String label = node.getLabel();

			// kin nodes
			List<TreeMutableNode> kinNodes = new ArrayList<>();
			for (int j = i + 1; j < nodes.size(); j++)
			{
				TreeMutableNode node2 = nodes.get(j);
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
				node.addToParent(superNode);
				for (TreeMutableNode node3 : kinNodes)
				{
					node3.addToParent(superNode);
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
	private List<TreeMutableNode> collectGroupMembers(final String groupID)
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
		List<TreeMutableNode> nodes = null;
		if (cursor != null)
		{
			//final String[] columns = cursor.getColumnNames();
			//Log.d(TAG, "MEMBERS " + join(columns));

			if (cursor.moveToFirst())
			{
				nodes = new ArrayList<>();
				do
				{
					final String contactId = cursor.getString(Settings.GROUP_MEMBERS_CONTACTID_INDEX);
					final String name = cursor.getString(Settings.GROUP_MEMBERS_DISPLAY_NAME_INDEX);
					final String altName = cursor.getString(Settings.GROUP_MEMBERS_DISPLAY_NAME_ALTERNATIVE_INDEX);
					final String photoThumbnailUri = cursor.getString(Settings.GROUP_MEMBERS_PHOTO_THUMBNAIL_URI_INDEX);
					//final String photoUri = cursor.getString(Settings.GROUP_MEMBERS_PHOTO_URI_INDEX);
					//final Long thumbnailId = cursor.getLong(Settings.GROUP_MEMBERS_PHOTO_ID_INDEX);
					//final Long photoId = cursor.getLong(Settings.GROUP_MEMBERS_PHOTO_FILE_ID_INDEX);
					final Long groupId = cursor.getLong(Settings.GROUP_MEMBERS_GROUP_ROW_ID_INDEX);
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
	 * Label factory of non-leave nodes
	 *
	 * @param first first child node
	 * @param last  lst child node
	 * @return makeRangeLabel of parent node
	 */
	private String makeRangeLabel(final TreeMutableNode first, final TreeMutableNode last)
	{
		String label1 = first.getTarget();
		String label2 = last.getTarget();
		if (label1 == null || label2 == null)
		{
			return null;
		}
		label1 = label1.toLowerCase();
		label2 = label2.toLowerCase();
		final StringBuilder sb = new StringBuilder();
		sb.append(left(label1, LABEL_TRUNCATE_AT)) //
				.append('-');
		boolean isContact = !"directory:".equals(last.getLink());
		if (isContact)
		{
			sb.append(left(label2, LABEL_TRUNCATE_AT));
		}
		else
		{
			sb.append(right(label2, LABEL_TRUNCATE_AT));
		}
		return sb.toString();
	}

	/**
	 * Right truncation of a string
	 *
	 * @param str string
	 * @param n   n trailing characters
	 * @return right of string
	 */
	private String right(final String str, final int n)
	{
		if (str == null)
		{
			return null;
		}
		int l = str.length();
		if (l > n)
		{
			return str.substring(l - n).toLowerCase();
		}
		return str.toLowerCase();
	}

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

	/**
	 * Join list
	 *
	 * @param strs array of strings
	 * @return joined strings
	 */
	private String join(final String[] strs)
	{
		final StringBuilder sb = new StringBuilder();
		boolean first = true;
		sb.append('{');
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
		sb.append('}');
		return sb.toString();
	}

	// T E S T

	public void model()
	{
		//final Tree tree = buildTree(collectContacts());
		final Tree tree = buildTree(null, null);
		final String dump = ModelDump.toString(tree);
		System.out.println(dump);

		/*
		final List<MutableNode> contacts = collectContacts();
		for (INode node : contacts)
		{
			Log.d(TAG, node.getLabel());
		}
		*/
	}
}
