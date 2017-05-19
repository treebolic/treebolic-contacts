package treebolic.provider.contacts;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
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

/**
 * Provider for Contacts
 *
 * @author Bernard Bou
 */
public class Provider implements IProvider
{
	private static final String TAG = "ContactProvider";

	private Context context;

	public Provider()
	{
	}

	@Override
	public void setHandle(final Object thisHandle)
	{
		this.context = (Context) thisHandle;
	}

	@Override
	public void setContext(final IProviderContext thisProviderContext)
	{

	}

	@Override
	public void setLocator(final ILocator thisContext)
	{
		// not needed
	}

	@Override
	public Model makeModel(String thisSource, URL thisBase, Properties theseParameters)
	{
		final Tree tree = makeTree(thisSource, thisBase, theseParameters, false);
		final treebolic.model.Settings settings = makeSettings();
		final Image[] images = theImages;
		return new Model(tree, settings, images);
	}

	@Override
	public Tree makeTree(String thisSource, URL thisBase, Properties theseParameters, boolean checkRecursion)
	{
		return buildTree();
	}

	private Tree buildTree()
	{
		//return buildTree(collectNodes());
		return buildTree(buildHierarchy(collectNodes(), 0));
	}

	private Tree buildTree(List<TreeMutableNode> nodes)
	{
		final TreeMutableNode root = new TreeMutableNode(null, "root");
		root.setLabel("CONTACTS");
		root.setBackColor(Provider.theRootBackColor);
		root.setForeColor(Provider.theRootForeColor);
		root.setEdgeColor(Provider.theRootEdgeColor);
		root.setEdgeStyle(IEdge.SOLID | IEdge.TOTRIANGLE | IEdge.TOFILL);
		root.setImageIndex(ImageIndices.ROOT.ordinal());

		if (nodes != null)
		{
			for (TreeMutableNode node : nodes)
			{
				node.addToParent(root);
			}
		}
		return new Tree(root, null);
	}

	// H E L P E R S

	static int[] LEVEL_MAX = {10, 3};

	private List<TreeMutableNode> buildHierarchy1(List<TreeMutableNode> nodes, int level)
	{
		Log.d(TAG, ">" + level + " nodes " + nodes.size());
		final List<TreeMutableNode> roots = new ArrayList<>();

		int m = LEVEL_MAX[level > LEVEL_MAX.length - 1 ? LEVEL_MAX.length - 1 : level];
		int z = nodes.size();
		for (int i = 0; i < z; i = i + m)
		{
			final TreeMutableNode root = new TreeMutableNode(null, "root" + level + "-" + i);
			//root.setLabel("root" + level + "-" + i);
			//root.setContent("root" + level + "-" + i);
			root.setLink("directory:"); //$NON-NLS-1$
			root.setBackColor(Provider.theFolderBackColor);
			root.setForeColor(Provider.theFolderForeColor);
			root.setEdgeColor(Provider.theFolderEdgeColor);
			root.setEdgeStyle(IEdge.SOLID | IEdge.TOTRIANGLE | IEdge.TOFILL);
			root.setImageIndex(ImageIndices.FOLDER.ordinal());

			int b = Math.min(i + m, z);
			TreeMutableNode first = nodes.get(i);
			TreeMutableNode last = nodes.get(b - 1);

			boolean isContact = !"directory:".equals(last.getLink());
			root.setLabel(first.getLabel().substring(0, 3) + "-" + (isContact ? last.getLabel().substring(0, 3) : tail(last.getLabel(), 3)));
			root.setContent("root" + level + "-" + i + "<br>" + first.getLabel() + "<br>" + last.getLabel());

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

	private String tail(String str, int n)
	{
		int l = str.length();
		if (l == n)
		{
			return str;
		}
		else if (l > 3)
		{
			return str.substring(l - 3);
		}
		else
		{
			// whatever is appropriate in this case
			throw new IllegalArgumentException(str);
		}
	}

	private List<TreeMutableNode> buildHierarchy(List<TreeMutableNode> nodes, int level)
	{
		int m = LEVEL_MAX[level > LEVEL_MAX.length - 1 ? LEVEL_MAX.length - 1 : level];
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

	private List<TreeMutableNode> collectNodes()
	{
		Settings.selectionArgs[0] = "%" + Settings.searchString + "%";
		final Uri uri = ContactsContract.Contacts.CONTENT_URI;
		final ContentResolver contentResolver = this.context.getContentResolver();
		final Cursor cursor = contentResolver.query(uri,   // content URI of the words table
				Settings.PROJECTION,    // columns to return for each row
				Settings.SELECTION,     // selection criteria
				Settings.selectionArgs, // selection criteria arguments to placeholders
				Settings.sortOrder);    // sort order for the returned rows
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
					final String photoTumbnailUriString = cursor.getString(Settings.PHOTO_THUMBNAIL_URI_INDEX);
					final String photoUriString = cursor.getString(Settings.PHOTO_URI_INDEX);
					final String photoTumbnail2UriString = cursor.getString(Settings.PHOTO_THUMBNAIL_ID_INDEX);
					final String photo2UriString = cursor.getString(Settings.PHOTO_ID_INDEX);

					final TreeMutableNode node = new TreeMutableNode(null, id);
					node.setLabel(altName);
					node.setContent(name + "<br>" + altName);
					node.setLink(lookup);
					// node.setImage(image);
					// node.setImageIndex(images.size());

					nodes.add(node);
				}
				while (cursor.moveToNext());
				Log.d(TAG, "collected nodes " + nodes.size());
				return nodes;
			}
		}
		return null;
	}
	// S T A T I C . D A T A

	public enum ImageIndices
	{
		ROOT, FOLDER, CONTACT,
	}

	static Image[] theImages = new Image[]{ //
			Image.try_make(Provider.class.getResource("images/root.png")), // ROOT
			Image.try_make(Provider.class.getResource("images/folder.png")), // FOLDER
			Image.try_make(Provider.class.getResource("images/contact.png")), // CONTACT
	};

	/**
	 * Background color
	 */
	static private Color theBackgroundColor = new Color(0x004C41);

	// CONTACT

	/**
	 * Contact background color
	 */
	public static final Color theDefaultContactBackColor = new Color(0x00968C);

	/**
	 * Contact foreground color
	 */
	public static final Color theDefaultContactForeColor = Color.LIGHT_GRAY;

	/**
	 * Contact treeedge color
	 */
	private static final Color theContactEdgeColor = Color.LIGHT_GRAY;

	// FOLDER

	/**
	 * Folder root background color
	 */
	private static final Color theFolderBackColor = Color.ORANGE;

	/**
	 * Folder foreground color
	 */
	public static final Color theFolderForeColor = Color.BLACK;

	/**
	 * Folder treeedge color
	 */
	private static final Color theFolderEdgeColor = Color.WHITE;

	// ROOT

	/**
	 * Top root background color
	 */
	private static final Color theRootBackColor = Color.RED;

	/**
	 * Top root root foreground color
	 */
	private static final Color theRootForeColor = Color.WHITE;

	/**
	 * Root treeedge color
	 */
	private static final Color theRootEdgeColor = Color.WHITE;

	treebolic.model.Settings makeSettings()
	{
		final treebolic.model.Settings settings = new treebolic.model.Settings();
		settings.theBackColor = Provider.theBackgroundColor;
		settings.theNodeBackColor = Provider.theDefaultContactBackColor;
		settings.theNodeForeColor = Provider.theDefaultContactForeColor;
		settings.theOrientation = "radial";
		settings.theHasToolbarFlag = true;
		settings.theFontFace = "SansSerif";
		settings.theExpansion = .8F;
		settings.theSweep = 1.3F;
		settings.theEllipsizeFlag = true;
		settings.theHasToolbarFlag = true;
		settings.theHasStatusbarFlag = true;
		settings.theFocusOnHoverFlag = true;
		settings.theTreeEdgeColor = Provider.theContactEdgeColor;
		settings.theTreeEdgeStyle = (int) IEdge.DOT;
		settings.theDefaultNodeImageIndex = ImageIndices.CONTACT.ordinal();
		return settings;
	}

	/**
	 * Root Folder foreground color
	 */
	private static final Color theRootFolderForeColor = Color.BLACK; // Color.WHITE;0xCCCCCC

	public void model()
	{
		//final Tree tree = buildTree(collectNodes());
		final Tree tree = buildTree();
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
