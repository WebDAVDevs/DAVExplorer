/*
 * PropModel.java
 *
 * Based on the JTreeTable examples provided by Sun Microsystems, Inc.:
 * http://java.sun.com/products/jfc/tsc/articles/treetable1/index.html
 * http://java.sun.com/products/jfc/tsc/articles/treetable2/index.html
 */

package DAVExplorer;

import java.util.Vector;
import java.util.Enumeration;
import javax.swing.JButton;
import javax.swing.JTree;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.*;
import javax.swing.event.*;
import com.ms.xml.om.*;
import com.ms.xml.parser.*;
import com.ms.xml.util.*;

public class PropModel extends AbstractTableModel implements TreeTableModel
{

    public PropModel( Element properties )
    {
        root = new PropNode( "Properties", null, null );
        parseProperties( properties, root );
    }

    public void setTree(JTree tree)
    {
        this.tree = tree;

    	tree.addTreeExpansionListener(new TreeExpansionListener()
        {
    	    // Don't use fireTableRowsInserted() here;
    	    // the selection model would get updated twice.
    	    public void treeExpanded(TreeExpansionEvent event)
            {
    	        fireTableDataChanged();
    	    }
            public void treeCollapsed(TreeExpansionEvent event)
            {
    	        fireTableDataChanged();
    	    }
    	});
    }

    public int getRowCount()
    {
        if( tree != null )
        	return tree.getRowCount();
        return 0;
    }

    public Element getModified( boolean removed )
    {
        if( removed )
            return getRemoved( root, null );
        else
            return getModified( root, null );
    }

    public void addNode( PropNode parentNode, PropNode node )
    {
        parentNode.addChild( node );
        Object[] pathToRoot = null;
        if( tree.isSelectionEmpty() )
        {
            pathToRoot = new Object[1];
            pathToRoot[0] = getRoot();
        }
        else
        {
            TreePath path = tree.getSelectionPath();
            pathToRoot = path.getPath();
        }
        int[] nodeIndices = new int[1];
        PropNode[] nodes = new PropNode[1];
        nodeIndices[0] = getIndexOfChild( parentNode, node );
        nodes[0] = node;
        fireTreeNodesInserted( parentNode, pathToRoot, nodeIndices, nodes );
    }

    public void removeNode( TreePath path )
    {
        TreePath parentPath = path.getParentPath();
        PropNode parentNode = (PropNode)parentPath.getLastPathComponent();
        PropNode node = (PropNode)path.getLastPathComponent();

        int[] nodeIndices = new int[1];
        PropNode[] nodes = new PropNode[1];
        nodeIndices[0] = getIndexOfChild( parentNode, node );
        nodes[0] = node;
        parentNode.removeChild( node );
        fireTreeNodesRemoved( parentNode, parentPath.getPath(), nodeIndices, nodes );
    }

    public void clear()
    {
        clear( root );
    }

    protected Object nodeForRow(int row)
    {
        if( tree != null )
        {
        	TreePath treePath = tree.getPathForRow(row);
        	return treePath.getLastPathComponent();
         }
         return null;
    }


    // The TreeTableModel interface
    public int getColumnCount()
    {
        return names.length;
    }

    public String getColumnName( int column )
    {
        return names[column];
    }

    public Class getColumnClass( int column )
    {
        return types[column];
    }

    public Object getValueAt(int row, int column)
    {
    	return getValueAt(nodeForRow(row), column);
    }

    public Object getValueAt( Object node, int column )
    {
        try {
            switch(column) {
            case 0:
                return ((PropNode)node).getTag();
            case 1:
                return ((PropNode)node).getNamespace();
            case 2:
                return ((PropNode)node).getValue();
            }
        }
        catch  (SecurityException se)
        {
        }
        return null;
    }

    public boolean isCellEditable( Object node, int column )
    {
        try {
            switch(column) {
            case 0:
                break;
            case 1:
            case 2:
                // modification of DAV properties not allowed
                if(((PropNode)node).getNamespace().equals("DAV:"))
                    return false;
                break;
            }
        }
        catch  (SecurityException se)
        {
        }
        return true;
    }

    public boolean isNodeRemovable( Object node )
    {
        // removal of DAV properties not allowed
        if(((PropNode)node).getNamespace().equals("DAV:"))
            return false;
        return true;
    }

    public void setValueAt( Object aValue, Object node, int column )
    {
        String oldValue;
        try {
            switch(column) {
            case 0:
                break;
            case 1:
                oldValue = ((PropNode)node).getNamespace();
                if( !oldValue.equals((String)aValue) )
                {
                    ((PropNode)node).setNamespace((String)aValue);
                    fireModelChanged(node);
                }
                break;
            case 2:
                oldValue = ((PropNode)node).getValue();
                if( !oldValue.equals((String)aValue) )
                {
                    ((PropNode)node).setValue((String)aValue);
                    fireModelChanged(node);
                }
                break;
            }
        }
        catch  (SecurityException se)
        {
        }
    }


    // The TreeModel interface
    public Object getRoot()
    {
        return root;
    }

    public int getChildCount(Object node)
    {
        Object[] children = ((PropNode)node).getChildren();
        return (children == null) ? 0 : children.length;
    }

    public Object getChild(Object node, int i)
    {
        return ((PropNode)node).getChildren()[i];
    }

    public boolean isLeaf(Object node)
    {
        return getChildCount(node) == 0;
    }

    public void valueForPathChanged(TreePath path, Object newValue)
    {
    }

    public int getIndexOfChild(Object parent, Object child)
    {
        for (int i = 0; i < getChildCount(parent); i++)
        {
            if (getChild(parent, i).equals(child))
            {
                return i;
            }
        }
        return -1;
    }

    public void addChangeListener(ChangeListener l)
    {
        listenerList.add(ChangeListener.class, l);
    }

    public void removeChangeListener(ChangeListener l)
    {
        listenerList.remove(ChangeListener.class, l);
    }

    public void addTreeModelListener(TreeModelListener l)
    {
        listenerList.add(TreeModelListener.class, l);
    }

    public void removeTreeModelListener(TreeModelListener l)
    {
        listenerList.remove(TreeModelListener.class, l);
    }

    protected void fireTreeNodesChanged(Object source, Object[] path, int[] childIndices, Object[] children)
    {
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        for (int i = listeners.length-2; i>=0; i-=2)
        {
            if (listeners[i]==TreeModelListener.class)
            {
                if (e == null)
                    e = new TreeModelEvent(source, path, childIndices, children);
                ((TreeModelListener)listeners[i+1]).treeNodesChanged(e);
            }
        }
    }

    protected void fireTreeNodesInserted(Object source, Object[] path, int[] childIndices, Object[] children)
    {
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        for (int i = listeners.length-2; i>=0; i-=2)
        {
            if (listeners[i]==TreeModelListener.class)
            {
                if (e == null)
                    e = new TreeModelEvent(source, path, childIndices, children);
                ((TreeModelListener)listeners[i+1]).treeNodesInserted(e);
            }
        }
    }

    protected void fireTreeNodesRemoved(Object source, Object[] path, int[] childIndices, Object[] children)
    {
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        for (int i = listeners.length-2; i>=0; i-=2)
        {
            if (listeners[i]==TreeModelListener.class)
            {
                if (e == null)
                    e = new TreeModelEvent(source, path, childIndices, children);
                ((TreeModelListener)listeners[i+1]).treeNodesRemoved(e);
            }
        }
    }

    protected void fireTreeStructureChanged(Object source, Object[] path, int[] childIndices, Object[] children)
    {
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        for (int i = listeners.length-2; i>=0; i-=2)
        {
            if (listeners[i]==TreeModelListener.class)
            {
                if (e == null)
                    e = new TreeModelEvent(source, path, childIndices, children);
                ((TreeModelListener)listeners[i+1]).treeStructureChanged(e);
            }
        }
    }

    protected void fireModelChanged( Object source )
    {
        Object[] listeners = listenerList.getListenerList();
        ChangeEvent e = null;
        for (int i = listeners.length-2; i>=0; i-=2)
        {
            if (listeners[i]==ChangeListener.class)
            {
                if (e == null)
                    e = new ChangeEvent(source);
                ((ChangeListener)listeners[i+1]).stateChanged(e);
            }
        }
    }


    private void parseProperties( Element properties, PropNode currentNode )
    {
        if( GlobalData.getGlobalData().getDebugResponse() )
        {
            System.err.println( "PropModel::parseProperties" );
        }

        if( properties != null )
        {
            TreeEnumeration enumTree =  new TreeEnumeration( properties );
            Element current = (Element)enumTree.nextElement();
            Name currentTag = current.getTagName();
            if( currentTag != null )
            {
                try
                {
                    // create a tree of all property tags
                    Enumeration propValEnum = current.getElements();
                    while (propValEnum.hasMoreElements())
                    {
                        Element propValEl = (Element) propValEnum.nextElement();
                        Name tagname = propValEl.getTagName();
                        if (propValEl.getType() != Element.ELEMENT)
                            continue;
                        /**
                         * Namespace Handling
                         * Unfortunately, the 1997-era Microsoft parser does not properly
                         * handle namespaces. It should really be replaced with a modern
                         * DOM parser.
                         * Until then, we are stuck with code like this to get the
                         * actual namespace by walking up the tree.
                         */
                        String ns = tagname.getNameSpace().toString();
                        Element parent = propValEl;
                        while( parent != null )
                        {
                            Name name = Name.create(ns, "xmlns");
                            String attr = (String)parent.getAttribute(name);
                            if( attr == null )
                                parent = parent.getParent();
                            else
                            {
                                ns = attr;
                                break;
                            }
                        }
                        Element token = getChildElement(propValEl);
                        if( (token != null) && (token.getType() == Element.PCDATA || token.getType() == Element.CDATA) )
                        {
                            PropNode node = new PropNode( tagname.getName(), ns, getValue(propValEl) );
                            // add to tree
                            currentNode.addChild(node);
                        }
                        else
                        {
                            PropNode node = new PropNode( tagname.getName(), ns, null );
                            // add to tree
                            currentNode.addChild(node);
                            parseProperties( propValEl, node );     // add child nodes
                        }
                    }
                }
                catch( Exception e )
                {
                    GlobalData.getGlobalData().errorMsg("DAV Interpreter:\n\nError encountered \nwhile parsing PROPFIND Response.\n" + e);
                }
            }
        }
    }

    private Element getModified( PropNode node, Element parent )
    {
        Element prop = null;
        Element retval = null;
        if( parent == null )
        {
            // root
            if( childrenModified(node) )
            {
                AsGen namespace = WebDAVXML.findNamespace( new AsGen(), WebDAVProp.DAV_SCHEMA );
                if( namespace == null )
                    namespace = WebDAVXML.createNamespace( new AsGen(), WebDAVProp.DAV_SCHEMA );
                Element set = WebDAVXML.createElement( WebDAVXML.ELEM_SET, Element.ELEMENT, null, namespace, false, true );
                set.addChild( WebDAVXML.elemNewline, null );
                prop = WebDAVXML.createElement( WebDAVXML.ELEM_PROP, Element.ELEMENT, set, namespace, false, true );
                set.addChild( prop, null );
                set.addChild( WebDAVXML.elemNewline, null );
                retval = set;
                Object[] children = node.getChildren();
                if( children.length==0 )
                    set.addChild( WebDAVXML.elemNewline, null );
                else
                {
                    for( int i=0; i<children.length; i++ )
                    {
                        getModified( (PropNode)children[i], prop );
                    }
                }
            }
            else
                return null;
        }
        else if( childrenModified(node) )
        {
            AsGen namespace = WebDAVXML.findNamespace( new AsGen(), node.getNamespace() );
            if( namespace == null )
                namespace = WebDAVXML.createNamespace( new AsGen(), node.getNamespace() );
            prop = WebDAVXML.createElement( node.getTag(), Element.ELEMENT, parent, namespace, false, true );
            parent.addChild( WebDAVXML.elemNewline, null );
            parent.addChild( prop, null );
            parent.addChild( WebDAVXML.elemNewline, null );
            Object[] children = node.getChildren();
            if( (node.getValue().length()>0) )
            {
                // need to have PCDATA if there are no children
                Element value = WebDAVXML.createElement( null, Element.PCDATA, prop, namespace, false, true );
                value.setText(node. getValue() );
                prop.addChild( value, null );
            }
            retval = prop;
            for( int i=0; i<children.length; i++ )
            {
                getModified( (PropNode)children[i], prop );
            }
        }

        return retval;
    }

    private Element getRemoved( PropNode node, Element parent )
    {
        Element prop = null;
        Element retval = null;
        if( parent == null )
        {
            // root
            if( childrenRemoved(node) )
            {
                AsGen namespace = WebDAVXML.findNamespace( new AsGen(), WebDAVProp.DAV_SCHEMA );
                if( namespace == null )
                    namespace = WebDAVXML.createNamespace( new AsGen(), WebDAVProp.DAV_SCHEMA );
                Element set = WebDAVXML.createElement( WebDAVXML.ELEM_REMOVE, Element.ELEMENT, null, namespace, false, true );
                set.addChild( WebDAVXML.elemNewline, null );
                prop = WebDAVXML.createElement( WebDAVXML.ELEM_PROP, Element.ELEMENT, set, namespace, false, true );
                prop.addChild( WebDAVXML.elemNewline, null );
                set.addChild( prop, null );
                retval = set;
                Object[] children = node.getRemovedChildren();
                for( int i=0; i<children.length; i++ )
                {
                    getRemoved( (PropNode)children[i], prop );
                }
            }
            else
                return null;
        }
        else
        {
            AsGen namespace = WebDAVXML.findNamespace( new AsGen(), node.getNamespace() );
            if( namespace == null )
                namespace = WebDAVXML.createNamespace( new AsGen(), node.getNamespace() );
            prop = WebDAVXML.createElement( node.getTag(), Element.ELEMENT, parent, namespace, false, true );
            prop.addChild( WebDAVXML.elemNewline, null );
            parent.addChild( prop, null );
            retval = prop;
        }

        return retval;
    }

    private Element getChildElement( Element el )
    {
        TreeEnumeration treeEnum = new TreeEnumeration( el );
        while(treeEnum.hasMoreElements() )
        {
            Element current = (Element)treeEnum.nextElement();
            Name tag = current.getTagName();
            if( tag != null )
            {
                Element token = (Element)treeEnum.nextElement();
                return token;
            }
        }
        return null;
    }

    private String getValue( Element el )
    {
        Element token = getChildElement(el);
        if( token != null )
            return token.getText();
        return null;
    }


    private boolean childrenModified( PropNode node )
    {
        if( node.isModified() )
            return true;
        Object[] children = node.getChildren();
        for( int i=0; i<children.length; i++ )
        {
            if( childrenModified((PropNode)children[i]) )
                return true;
        }
        return false;
    }

    private boolean childrenRemoved( PropNode node )
    {
        Object[] children = node.getRemovedChildren();
        if( children.length > 0 )
            return true;
        children = node.getChildren();
        for( int i=0; i<children.length; i++ )
        {
            if( childrenRemoved((PropNode)children[i]) )
                return true;
        }
        return false;
    }

    private void clear( PropNode node )
    {
        node.clear();
        Object[] children = node.getChildren();
        for( int i=0; i<children.length; i++ )
        {
            clear((PropNode)children[i]);
        }
    }


    // column names
    static protected String[]  names = { "Tag", "Namespace", "Value" };

    // column types
    static protected Class[]  types = { TreeTableModel.class, String.class, String.class };

    private PropNode root;
    private JTree tree;
    private JButton saveButton;
    protected EventListenerList listenerList = new EventListenerList();
}


class PropNode
{
    public PropNode( String tag, String ns, String value, boolean modified )
    {
        this.tag = tag;
        this.ns = ns;
        this.value = value;
        this.modified = modified;
    }

    public PropNode( String tag, String ns, String value )
    {
        this.tag = tag;
        this.ns = ns;
        this.value = value;
        this.modified = false;
    }

    public String getTag()
    {
        return tag;
    }

    public String getNamespace()
    {
        return ns;
    }

    public void setNamespace( String ns )
    {
        this.ns = ns;
        modified = true;
    }

    public String getValue()
    {
        if( value == null )
            return "";
        return value;
    }

    public void setValue( String value )
    {
        this.value = value;
        modified = true;
    }

    public String toString()
    {
        return getTag();
    }

    public void addChild( Object child )
    {
        children.add( child );
    }

    public void removeChild( Object child )
    {
        children.remove(child);
        removedChildren.add(child);
    }

    public boolean isModified()
    {
        return modified;
    }

    public Object[] getChildren()
    {
        return children.toArray();
    }

    public Object[] getRemovedChildren()
    {
        return removedChildren.toArray();
    }

    public void clear()
    {
        modified = false;
        removedChildren.clear();
    }

    Vector children = new Vector();
    Vector removedChildren = new Vector();
    String tag;
    String ns;
    String value;
    JTree tree;
    boolean modified;
}
