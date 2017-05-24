package treebolic.provider.contacts;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
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
		ROOT, FOLDER, CONTACT,
	}

	/**
	 * Image array
	 */
	static Image[] images = new Image[]{ //
			Image.try_make(Provider.class.getResource("images/root.png")), // ROOT
			Image.try_make(Provider.class.getResource("images/folder.png")), // FOLDER
			Image.try_make(Provider.class.getResource("images/contact.png")), // CONTACT
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
	public Model makeModel(String thisSource, URL thisBase, Properties theseParameters)
	{
		final Tree tree = makeTree(thisSource, thisBase, theseParameters, false);
		final treebolic.model.Settings settings = makeSettings();
		final Image[] images = Provider.images;
		return new Model(tree, settings, images);
	}

	@Override
	public Tree makeTree(String thisSource, URL thisBase, Properties theseParameters, boolean checkRecursion)
	{
		return buildTree(thisSource);
	}

	// T R E E   F A C T O R Y

	/**
	 * Tree factory
	 *
	 * @return tree
	 */
	private Tree buildTree(final String source)
	{
		//return buildTree(collectNodes());
		return buildTree(buildHierarchy(collectNodes(source), 0));
	}

	/**
	 * Tree factory
	 *
	 * @param nodes contact nodes
	 * @return tree of contact nodes
	 */
	private Tree buildTree(List<TreeMutableNode> nodes)
	{
		final TreeMutableNode topRoot = new TreeMutableNode(null, "root");
		topRoot.setLabel("contacts");
		topRoot.setBackColor(Provider.rootBackColor);
		topRoot.setForeColor(Provider.rootForeColor);
		topRoot.setEdgeColor(Provider.rootEdgeColor);
		topRoot.setEdgeStyle(IEdge.SOLID | IEdge.TOTRIANGLE | IEdge.TOFILL);
		topRoot.setImageIndex(ImageIndices.ROOT.ordinal());

		if (nodes != null)
		{
			for (TreeMutableNode node : nodes)
			{
				node.addToParent(topRoot);
			}
		}
		return new Tree(topRoot, null);
	}

	/**
	 * Build a list of tree parent nodes
	 *
	 * @param nodes children nodes
	 * @param level current level
	 * @return list of parent nodes
	 */
	private List<TreeMutableNode> buildHierarchy1(List<TreeMutableNode> nodes, int level)
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

			final String startLabel = left(label(first), LABEL_TRUNCATE_AT);
			final String rangeLabel = makeLabel(first, last);
			root.setEdgeLabel(startLabel);
			// root.setLabel(rangeLabel);
			// root.setContent(/* "root" + level + "-" + i + NL  + */ rangeLabel + NL + label(first) + NL + label(last));
			root.setContent(rangeLabel + /* NL + (b - i) + " elements" + */ NL + "<small>" + label(first) + NL + label(last) + "</small>");

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
	private List<TreeMutableNode> buildHierarchy(List<TreeMutableNode> nodes, int level)
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
	 * @param source selectiion
	 * @return list of contact nodes
	 */
	private List<TreeMutableNode> collectNodes(final String source)
	{
		final String[] selectionArgs = {source};
		final boolean nullSource = source == null || source.isEmpty();
		selectionArgs[0] = nullSource ? null : "%" + source + "%";

		final Uri uri = ContactsContract.Contacts.CONTENT_URI;
		final ContentResolver contentResolver = this.context.getContentResolver();
		final Cursor cursor = contentResolver.query(uri, // content URI of the words table
				Settings.PROJECTION, // columns to return for each row
				nullSource ? null : Settings.SELECTION_DN_LIKE, // selection criteria
				nullSource ? null : selectionArgs, // selection criteria arguments to placeholders
				Settings.sortOrder); // sort order for the returned rows
		if (cursor != null)
		{
			if (cursor.moveToFirst())
			{
				final List<TreeMutableNode> nodes = new ArrayList<>();
				// final List<Image> images = new ArrayList<>();
				do
				{
					// row values
					final String id = cursor.getString(Settings.ID_INDEX);
					final String lookup = cursor.getString(Settings.LOOKUP_KEY_INDEX);
					final String name = cursor.getString(Settings.NAME_INDEX);
					final String altName = cursor.getString(Settings.ALT_NAME_INDEX);
					//final Long photoThumbnailId = cursor.getLong(Settings.PHOTO_THUMBNAIL_ID_INDEX);
					final String photoThumbnailUri = cursor.getString(Settings.PHOTO_THUMBNAIL_URI_INDEX);

					// makeLabel
					final String[] fields = altName.split(", *");
					String label = fields[0];
					if (fields.length > 1 && fields[1].charAt(0) != '#')
					{
						label += '\n';
						label += fields[1];
					}
					//final String makeLabel = altName.replace(',','\n');

					// node
					final TreeMutableNode node = new TreeMutableNode(null, id);
					node.setLabel(label);
					node.setContent(name + NL + altName);
					node.setLink(URLSCHEME + id);

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
					else
					{
						node.setImageIndex(ImageIndices.CONTACT.ordinal());
					}

					/*
					if (photoThumbnailId != 0)
					{
						final Bitmap bitmap = Utils.queryThumbnailPhotoFromPhotoId(this.context, photoThumbnailId);
						final Bitmap roundBitmap = Utils.roundCrop(bitmap);
						final Bitmap scaledupBitmap = Utils.scale(roundBitmap, 200, 200);
						final Image image = new Image(scaledupBitmap);
						node.setImage(image);
					}
					*/
					nodes.add(node);
				}
				while (cursor.moveToNext());
				Log.d(TAG, "collected nodes " + nodes.size());
				return nodes;
			}
		}
		return null;
	}

	// H E L P E R S

	/**
	 * Label factory of non-leave nodes
	 *
	 * @param node node
	 * @return label of node
	 */
	private String label(final TreeMutableNode node)
	{
		final String l = node.getLabel();
		if (l != null)
		{
			return l;
		}
		return node.getContent().split(NL)[0];
	}

	/**
	 * Label factory of non-leave nodes
	 *
	 * @param first first child node
	 * @param last  lst child node
	 * @return makeLabel of parent node
	 */
	private String makeLabel(final TreeMutableNode first, final TreeMutableNode last)
	{
		String label1 = label(first);
		String label2 = label(last);
		if (label1 == null || label2 == null)
		{
			return null;
		}
		label1 = label1.replaceFirst("^[@#]*", "").toLowerCase();
		label2 = label2.replaceFirst("^[@#]*", "").toLowerCase();
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
	private String right(String str, int n)
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
	private String left(final String str, int n)
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

	// T E S T

	public void model()
	{
		//final Tree tree = buildTree(collectNodes());
		final Tree tree = buildTree((String)null);
		final String dump = ModelDump.toString(tree);
		System.out.println(dump);

		/*
		final List<MutableNode> contacts = collectNodes();
		for (INode node : contacts)
		{
			Log.d(TAG, node.getLabel());
		}
		*/
	}
}
