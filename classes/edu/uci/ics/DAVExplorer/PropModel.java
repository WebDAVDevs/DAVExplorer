/*
 * Copyright (c) 2001-2005 Regents of the University of California.
 * All rights reserved.
 *
 * This software was developed at the University of California, Irvine.
 *
 * Redistribution and use in source and binary forms are permitted
 * provided that the above copyright notice and this paragraph are
 * duplicated in all such forms and that any documentation,
 * advertising materials, and other materials related to such
 * distribution and use acknowledge that the software was developed
 * by the University of California, Irvine.  The name of the
 * University may not be used to endorse or promote products derived
 * from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 * WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 */

/**
 * Title:       Property Model
 * Description: Models the hierarchical nature of WebDAV properties
 * Copyright:   Copyright (c) 2001-2005 Regents of the University of California. All rights reserved.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        29 September 2001
 *
 * Based on the JTreeTable examples provided by Sun Microsystems, Inc.:
 * http://java.sun.com/products/jfc/tsc/articles/treetable1/index.html
 * http://java.sun.com/products/jfc/tsc/articles/treetable2/index.html
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        1 October 2001
 * Changes:     Change of package name
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        17 December 2001
 * Changes:     Fixed handling of adding and removing nested properties
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        08 February 2004
 * Changes:     Added Javadoc templates
 */

package edu.uci.ics.DAVExplorer;

import java.util.Enumeration;
import javax.swing.JButton;
import javax.swing.JTree;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.TreePath;
import javax.swing.event.EventListenerList;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeExpansionEvent;
import com.ms.xml.om.Element;
import com.ms.xml.om.TreeEnumeration;
import com.ms.xml.util.Name;


/**
 * 
 */
public class PropModel extends AbstractTableModel implements TreeTableModel
{
    protected PropModel()
    {
        root = new PropNode( "Properties", null, null );
    }


    /**
     * Constructor
     * @param properties
     */
    public PropModel( Element properties )
    {
        root = new PropNode( "Properties", null, null );
        parseProperties( properties, root );
    }

    
    /**
     * 
     * @param tree
     */
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


    /**
     * 
     * @return
     */
    public int getRowCount()
    {
        if( tree != null )
            return tree.getRowCount();
        return 0;
    }


    /**
     * 
     * @param removed
     * @return
     */
    public Element getModified( boolean removed )
    {
        if( removed )
            return getRemoved( root, null );
        else
            return getModified( root, null, false );
    }


    /**
     * 
     * @param parentNode
     * @param node
     * @param root
     */
    public void addNode( PropNode parentNode, PropNode node, boolean root )
    {
        parentNode.addChild( node );
        node.setParent( parentNode );
        Object[] pathToRoot = null;
        if( root )
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


    /**
     * 
     * @param path
     */
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


    /**
     *
     */
    public void clear()
    {
        clear( root );
    }


    /**
     * 
     * @param row
     * @return
     */
    protected Object nodeForRow(int row)
    {
        if( tree != null )
        {
            TreePath treePath = tree.getPathForRow(row);
            return treePath.getLastPathComponent();
         }
         return null;
    }


    /**
     * The TreeTableModel interface
     * 
     * @return
     */
    public int getColumnCount()
    {
        return getNames().length;
    }


    /**
     * The TreeTableModel interface
     * @param column
     *  
     * @return
     */
    public String getColumnName( int column )
    {
        return getNames()[column];
    }


    /**
     * The TreeTableModel interface
     * @param column
     * 
     * @return
     */
    public Class getColumnClass( int column )
    {
        return getTypes()[column];
    }


    /**
     * The TreeTableModel interface
     * @param row
     * @param column
     * 
     * @return
     */
    public Object getValueAt(int row, int column)
    {
        return getValueAt(nodeForRow(row), column);
    }

    /**
     * The TreeTableModel interface
     * @param node
     * @param column
     * 
     * @return
     */
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


    /**
     * The TreeTableModel interface
     * @param node
     * @param column
     * 
     * @return
     */
    public boolean isCellEditable( Object node, int column )
    {
        try {
            switch(column) {
            case 0:
                break;
            case 1:
            case 2:
                // modification of DAV properties not allowed
                return !((PropNode)node).isDAVProp();
            }
        }
        catch  (SecurityException se)
        {
        }
        return true;
    }


    /**
     * The TreeTableModel interface
     * @param node
     * 
     * @return
     */
    public boolean isNodeRemovable( Object node )
    {
        // removal of DAV properties not allowed
        return !((PropNode)node).isDAVProp();
    }


    /**
     * The TreeTableModel interface
     * @param aValue
     * @param node
     * @param column
     * 
     * @return
     */
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


    /**
     * The TreeModel interface
     * 
     * @return
     */
    public Object getRoot()
    {
        return root;
    }


    /**
     * The TreeModel interface
     * @param node
     * 
     * @return
     */
    public int getChildCount(Object node)
    {
        Object[] children = ((PropNode)node).getChildren();
        return (children == null) ? 0 : children.length;
    }


    /**
     * The TreeModel interface
     * @param node
     * @param i
     * 
     * @return
     */
    public Object getChild(Object node, int i)
    {
        return ((PropNode)node).getChildren()[i];
    }


    /**
     * The TreeModel interface
     * @param node
     * 
     * @return
     */
    public boolean isLeaf(Object node)
    {
        return getChildCount(node) == 0;
    }


    /**
     * The TreeModel interface
     * @param path
     * @param newValue
     * 
     * @return
     */
    public void valueForPathChanged(TreePath path, Object newValue)
    {
    }


    /**
     * The TreeModel interface
     * @param parent
     * @param child
     * 
     * @return
     */
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


    /**
     * The TreeModel interface
     * @param l
     * 
     * @return
     */
    public void addChangeListener(ChangeListener l)
    {
        listenerList.add(ChangeListener.class, l);
    }


    /**
     * The TreeModel interface
     * @param l
     * 
     * @return
     */
    public void removeChangeListener(ChangeListener l)
    {
        listenerList.remove(ChangeListener.class, l);
    }


    /**
     * The TreeModel interface
     * @param l
     * 
     * @return
     */
    public void addTreeModelListener(TreeModelListener l)
    {
        listenerList.add(TreeModelListener.class, l);
    }


    /**
     * The TreeModel interface
     * @param l
     * 
     * @return
     */
    public void removeTreeModelListener(TreeModelListener l)
    {
        listenerList.remove(TreeModelListener.class, l);
    }


    /**
     * The TreeModel interface
     * @param source
     * @param path
     * @param childIndices
     * @param children
     * 
     * @return
     */
    protected void fireTreeNodesChanged( Object source, Object[] path, int[] childIndices,
                                         Object[] children )
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


    /**
     * The TreeModel interface
     * @param source
     * @param path
     * @param childIndices
     * @param children
     * 
     * @return
     */
    protected void fireTreeNodesInserted( Object source, Object[] path, int[] childIndices, 
                                          Object[] children )
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


    /**
     * The TreeModel interface
     * @param source
     * @param path
     * @param childIndices
     * @param children
     * 
     * @return
     */
    protected void fireTreeNodesRemoved( Object source, Object[] path, int[] childIndices,
                                         Object[] children )
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


    /**
     * The TreeModel interface
     * @param source
     * @param path
     * @param childIndices
     * @param children
     * 
     * @return
     */
    protected void fireTreeStructureChanged( Object source, Object[] path, int[] childIndices,
                                             Object[] children )
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


    /**
     * The TreeModel interface
     * @param source
     * 
     * @return
     */
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


    /**
     * 
     * @param properties
     * @param currentNode
     */
    protected void parseProperties( Element properties, PropNode currentNode )
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
                        String ns = WebDAVProp.locateNamespace( propValEl, tagname );
                        Element token = getChildElement(propValEl);
                        if( (token != null) && (token.getType() == Element.PCDATA || token.getType() == Element.CDATA) )
                        {
                            PropNode node = new PropNode( tagname.getName(), ns, getValue(propValEl) );
                            // add to tree
                            currentNode.addChild( node );
                            node.setParent( currentNode );
                        }
                        else
                        {
                            PropNode node = new PropNode( tagname.getName(), ns, null );
                            // add to tree
                            currentNode.addChild( node );
                            node.setParent( currentNode );
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


    /**
     * 
     * @param node
     * @param parent
     * @param ignoreModifiedFlag
     * @return
     */
    private Element getModified( PropNode node, Element parent, boolean ignoreModifiedFlag )
    {
        Element prop = null;
        Element retval = null;
        if( parent == null )
        {
            // root
            if( childrenModified(node) || childrenRemoved(node) )
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
                        getModified( (PropNode)children[i], prop, false );
                    }
                }
            }
            else
                return null;
        }
        else if( childrenModified(node) || childrenRemoved(node) || ignoreModifiedFlag )
        {
            // if there are modified or removed children, we need to set all of the tree
            // here, since the server just replaces the tree
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
                getModified( (PropNode)children[i], prop, true );
            }
        }

        return retval;
    }


    /**
     * 
     * @param node
     * @param parent
     * @return
     */
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
                    // the child node we are looking at and all its children are deleted
                    getRemoved( (PropNode)children[i], prop );
                }
                children = node.getChildren();
                for( int i=0; i<children.length; i++ )
                {
                    if( childrenRemoved((PropNode)children[i]) )
                    {
                        // some node below the current child node is deleted
                        getRemoved( (PropNode)children[i], prop );
                    }
                }
            }
            else
                return null;
        }
        else
        {
            // delete the whole tree from the child on. If only parts of the tree
            // were deleted, the getModified() function above makes sure that the
            // data tree gets its correct shape
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


    /**
     * 
     * @param el
     * @return
     */
    protected Element getChildElement( Element el )
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


    /**
     * 
     * @param el
     * @return
     */
    protected String getValue( Element el )
    {
        Element token = getChildElement(el);
        if( token != null )
            return token.getText();
        return null;
    }


    /**
     * 
     * @param node
     * @return
     */
    protected boolean childrenModified( PropNode node )
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


    /**
     * 
     * @param node
     * @return
     */
    protected boolean childrenRemoved( PropNode node )
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


    /**
     * 
     * @param node
     */
    protected void clear( PropNode node )
    {
        node.clear();
        Object[] children = node.getChildren();
        for( int i=0; i<children.length; i++ )
        {
            clear((PropNode)children[i]);
        }
    }

    
    protected String[] getNames()
    {
        return names;
    }


    protected Class[] getTypes()
    {
        return types;
    }

    
    // column names
    protected String[] names = { "Tag", "Namespace", "Value" };

    // column types
    protected Class[] types = { TreeTableModel.class, String.class, String.class };

    protected EventListenerList listenerList = new EventListenerList();

    protected PropNode root;
    protected JTree tree;
    protected JButton saveButton;
}
