/*
 * Copyright (c) 1999 Regents of the University of California.
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

//        This class is part of the GUI module for the WebDAV
//        Client. It provides the user with a Windows Explorer
//        like interface.
//
//        Version:    0.3
//        Author:     Robert Emmery 
//        Date:       4/2/98
////////////////////////////////////////////////////////////////
// The code has been modified to include povisions for the final
// WebDAV xml namespaces.  A small number of program errors have
// been corrected.
//
// Please use the following contact:
//
// dav-exp@ics.uci.edu
//
// Version: 0.4
// Changes by: Yuzo Kanomata and Joe Feise
// Date: 3/17/99
//
// Change List:
// 1. method fireSelectionEvent() has been renames to initTree()
//    This more acurately describes its purpose and function.
// 2. The MouseListener section of code which includes the methods
//    handleSingleClick and handleDoubleClick have been commented out.
//    This code is redundent to the functionality of tree selection,
//    and caused a side effect tree selection event to be caused
//    when the X,Y portion of the cursor was repositioned on a 
//    repaint by the display.
// 3. In class SelectionChangeListener, in method 
//    valueChanged(TreeSelectionEvent e), the Cursor is changed 
//    to the WAIT_CURSOR while the the Event of a Tree Selection
//    are being processed by the various Listeners.

package DAVExplorer;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

// Yuzo: This should be a Model
public class WebDAVTreeView implements ViewSelectionListener, CopyResponseListener
{
    JTree tree;
    final static String WebDAVRoot = "DAV Explorer";
    final static String WebDAVPrefix = "http://";

    DefaultMutableTreeNode root = new WebDAVTreeNode(WebDAVRoot,true);
    DefaultTreeModel treeModel = new DefaultTreeModel(root); 
    DefaultMutableTreeNode currNode = root;
    Vector rootElements = new Vector(); 
    TreePath currPath;
    TreeSelectionModel selectionModel = new DefaultTreeSelectionModel();
    Vector selListeners = new Vector();
    public static String homeDirName;
    public String startDirName;

    JFrame mainFrame;

    JScrollPane sp;

    // Yuzo: Changing from this to an new Selection Listener which does no 
    SelectionChangeListener treeSelectionListener = 
				new SelectionChangeListener();
    // Adding an Expansion Event Listener so as to expand a node
    // without having to select it.
    treeExpansionListener treeExpListener =
    				new treeExpansionListener();

    private boolean simpleNodeExpand = false;

    // Constructor
    public WebDAVTreeView(JFrame mainFrame)
    {
        tree = new JTree(treeModel);

	    tree.putClientProperty("JTree.lineStyle", "Angled");

        tree.setSelectionModel(selectionModel);
        selectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(treeSelectionListener);

        tree.setRowHeight(-1);

        this.mainFrame = mainFrame;

        sp = new JScrollPane(tree);
        //sp.getViewport().add(tree); 
        sp.setPreferredSize(new Dimension(240,400));

        startDirName = System.getProperty("user.home");
        if (startDirName == null){
            startDirName = new Character(File.separatorChar).toString();
    	}

        // Listen for expansion Events
        // Yuzo Adding Expansion Event listner for testing purposes:
        tree.addTreeExpansionListener( treeExpListener);


        treeModel.addTreeModelListener( new TreeModelListener()
        {
        	public void treeNodesChanged(TreeModelEvent e)
        	{
        	}

        	public void treeNodesInserted(TreeModelEvent e)
        	{
        	}

        	public void treeNodesRemoved(TreeModelEvent e)
        	{
        	}

        	public void treeStructureChanged(TreeModelEvent e)
        	{
        	}
        });
    }
  
    class treeExpansionListener implements TreeExpansionListener
    {
        public void treeExpanded( TreeExpansionEvent evt )
        {
            TreePath selectedPath = selectionModel.getSelectionPath();
            Cursor c = mainFrame.getCursor(); // save original cursor
            mainFrame.setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );

            TreePath expansionPath = evt.getPath();
            currPath = expansionPath;
            WebDAVTreeNode tn = (WebDAVTreeNode)expansionPath.getLastPathComponent();
            if(!tn.hasLoadedChildren())
            {
                tn.loadChildren();

                tn.setHasLoadedChildren(true);
                treeModel.nodeStructureChanged(tn);
            }
            else
            {
    	    }

            mainFrame.setCursor( c );
        }

        public void treeCollapsed( TreeExpansionEvent evt )
        {
        }
    }

    //Yuzo: Added Copy ResposeListner stuff
    public void CopyEventResponse(CopyResponseEvent e)
    {
        WebDAVTreeNode tn = e.getNode();

        TreeNode path[] = tn.getPath();

        String s = new String();
        for (int i = 1; i < path.length; i++)
        {
            s = s + path[i] + "/";
        }

        // Now then reload the Tree from this node
        // This means that we have to unload this node
        // then reload load it with the updated info making 
        // a call to the server

        tn.removeAllChildren();

        TreePath tp = new TreePath(path);

        // Load all the Children of this Node.
        tn.loadChildren();
        treeModel.nodeStructureChanged(tn);
        tn.setHasLoadedChildren(true);

        ViewSelectionEvent event = new ViewSelectionEvent(this, tn, tp);
        for (int i=0; i<selListeners.size();i++)
        {
            ViewSelectionListener l = (ViewSelectionListener)selListeners.elementAt(i);
            l.selectionChanged(event);
        }
    }


    public JScrollPane getScrollPane()
    {
        // We package the whole TreeView inside a Scroll Pane, returned
        // by this function.
        return(sp);
    }

    //Yuzo Added: Get the Selection Event from TableView
    // This means that a folder(dir) was double clicked.
    // Now handle this event as an open of that partictular dir
    public void selectionChanged(ViewSelectionEvent e)
    {
        tableSelectionChanged(e);
    }

    //  This is where selection event from the table View are routed to.
    //  The Event includes the information needed to expand/select
    //  the particular row. 
    protected void tableSelectionChanged(ViewSelectionEvent e)
    {
        WebDAVTreeNode tn = (WebDAVTreeNode)e.getNode();
        TreePath tp = (TreePath)e.getPath();

        tree.removeTreeExpansionListener(treeExpListener);
        tree.removeTreeSelectionListener(treeSelectionListener);

        Cursor c = mainFrame.getCursor(); // save original cursor
        mainFrame.setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );

        if(!tn.hasLoadedChildren())
        {
            tn.loadChildren();

            tn.setHasLoadedChildren(true);
            treeModel.nodeStructureChanged(tn);
        }

        if (!tree.isExpanded(tp))
        {
            tree.expandPath(tp);
        }

        tree.setSelectionPath(tp);
        tree.revalidate();          // Attempt to validate the new tree
                                    // This does not work becuase the vaildate
                                    // is invoked later.  This causes
                                    // the scrollPathToVisible not to work
        tree.makeVisible(tp);
        tree.scrollPathToVisible(tp);
        tree.scrollPathToVisible(tp);   // Sun Bug Id 4180658  -- does not work
                                        // but this second call is a "fix"
                                        // according to the bug report
        tree.treeDidChange();

        currPath = tp;

        mainFrame.setCursor( c );

        tree.addTreeExpansionListener(treeExpListener);
        tree.addTreeSelectionListener(treeSelectionListener);
    }

    public void refresh()
    {
        // Make sure the directory structure is current.
        int row = tree.getRowForPath(currPath);
        tree.clearSelection();
        tree.setSelectionRow(row);
    }

    public synchronized void addViewSelectionListener(ViewSelectionListener l)
    {
        // Register a listener 
        selListeners.addElement(l);
    }

    public synchronized void removeViewSelectionListener(ViewSelectionListener l)
    {
        selListeners.removeElement(l);
    }

    //Yuzo New Selection Listener
    class SelectionChangeListener implements TreeSelectionListener
    {
        // This is where we handle the tree selection event. 
        public void valueChanged(TreeSelectionEvent e)
        {
            //Need to make sure that the newly selected node (dir)
            //has its children's children loaded.  This is needed to
            // ensure that handles on the files are correct.
            TreePath tp = e.getPath();

            currPath = tp;

            // Get the last node, then check if all the Children are
            // loaded.  
            WebDAVTreeNode tn = (WebDAVTreeNode)tp.getLastPathComponent();
            if (!tn.hasLoadedChildren())
            {
                // Load all the Children of this Node.
                tn.loadChildren();
                treeModel.nodeStructureChanged(tn);
                tn.setHasLoadedChildren(true);
            }

            ViewSelectionEvent event = new ViewSelectionEvent(this,
				            (WebDAVTreeNode)tp.getLastPathComponent(), tp);
            for (int i=0; i<selListeners.size();i++)
            {
                ViewSelectionListener l = (ViewSelectionListener)selListeners.elementAt(i);
                    l.selectionChanged(event);
            }
        }
    }

    public void initTree()
    {
        // For initialization purposes. 
        // This function is called when the client starts.
        if (startDirName != null)
        addRowToRoot(startDirName,true);
    }

    public String constructPath(TreePath the_path)
    {
        // This will iterate through the path array, and construct
        // the appropriate path for method generation purposes.
        Object[] path = the_path.getPath(); 
        String newPath = "";
        if (path.length == 1) 
            return (newPath);
        String firstComp = path[1].toString();

        if (!firstComp.startsWith(WebDAVPrefix))
        {
            // We're constructing local filename path
            newPath += startDirName;
            if (newPath.endsWith(new Character(File.separatorChar).toString()))
                newPath = newPath.substring(0,newPath.length() - 1);
            for (int i=2;i<path.length;i++)
            {
                newPath += File.separator + path[i].toString(); 
            }
            if (!newPath.endsWith(new Character(File.separatorChar).toString()) )
                newPath += new Character(File.separatorChar).toString();
        }
        else
        {
            // Construct WebDAV path
            for (int i=1;i<path.length;i++)
            {
                newPath += path[i].toString();
                if (!newPath.endsWith("/"))
                    newPath += "/";
            }
            if (!newPath.endsWith("/"))
                newPath += "/";          
        }
        return (newPath);
    }

    public boolean addRowToRoot(String name, boolean local)
    {
        // Add item to the tree. If local == true, the item is
        // considered to be a file on a local file system.
        String newName = "";

        if ( (name == null) || (name.equals("")) )
            return false;

        if (local)
        {
            if (name.endsWith(new Character(File.separatorChar).toString()))
                name = name.substring(0,name.length() - 1);
            if (name.length() == 0)
                name = new Character(File.separatorChar).toString();
            newName = name;
            File file = null;
            file = new File(newName);
            if (file == null)
                return false;
            if (!file.isDirectory())
            {
                errorMsg("TreeView Error:\n\nFile is not a directory.");
                return false;
            }
        }
        else
        {
            if (name.endsWith("/"))
                name = name.substring(0,name.length() - 1);
            newName = WebDAVPrefix + name;
        }

        if (rootElements.contains(newName))
        {
            errorMsg("TreeView Error:\n\nNode already exists!");
            return false;
        }

        rootElements.addElement(newName); 
        WebDAVTreeNode newNode = new WebDAVTreeNode(newName);
        treeModel.insertNodeInto(newNode,root,0);

        if (local)
        {
            tree.clearSelection();
            tree.setSelectionRow(1);
            tree.expandPath(tree.getPathForRow(0));
        }
        else
        {
            // Now finish the Processing for the root.
            newNode.finishLoadChildren();
            newNode.setHasLoadedChildren(true);
            tree.setSelectionRow(1);
        }

        return true;
    }
  
    public void errorMsg(String str)
    {
        JOptionPane pane = new JOptionPane();
        Object[] options = { "OK" };
        pane.showOptionDialog(mainFrame,str, "Error Message", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
    }
    
    public String getCurrentPath()
    {
        return constructPath( currPath );
    }

    // Yuzo: This is to allow Main to add fileView as a Tree Listener
    public void addTreeSelectionListener(TreeSelectionListener tsl)
    {
	    tree.addTreeSelectionListener(tsl);
    }

    public String getLockToken( String curFile )
    {
        currNode = (WebDAVTreeNode) currPath.getLastPathComponent();
        DataNode node = ((WebDAVTreeNode)currNode).getDataNode();
        return getLockToken( node, curFile );
    }
    
    public void setLock( String curFile, String token )
    {
        currNode = (WebDAVTreeNode) currPath.getLastPathComponent();
        DataNode node = ((WebDAVTreeNode)currNode).getDataNode();
        node = getCurrentDataNode( node, curFile );
        if( node != null )
        {
            node.lock( token );
        }
    }
    
    public void resetLock( String curFile )
    {
        currNode = (WebDAVTreeNode) currPath.getLastPathComponent();
        DataNode node = ((WebDAVTreeNode)currNode).getDataNode();
        node = getCurrentDataNode( node, curFile );
        if( node != null )
        {
            node.unlock();
        }
    }
    

    private String getLockToken( DataNode node, String curFile )
    {
        if( node == null )
            return null;
            
        if( node.isCollection() )
        {
            if( node.getSubNodes() == null )
                return null;
            for( Enumeration e = node.getSubNodes().elements(); e.hasMoreElements(); )
            {
                DataNode current = (DataNode)e.nextElement();
                String token = getLockToken( current, curFile );
                if( token != null )
                    return token;
            }
        }
        else if( node.getName().equals( curFile ) )
            return node.getLockToken();
        return null;
    }

    private DataNode getCurrentDataNode( DataNode node, String curFile )
    {
        if( node == null )
            return null;
            
        if( node.isCollection() )
        {
            if( node.getSubNodes() == null )
                return null;
            for( Enumeration e = node.getSubNodes().elements(); e.hasMoreElements(); )
            {
                DataNode current = (DataNode)e.nextElement();
                DataNode curNode = getCurrentDataNode( current, curFile );
                if( curNode != null )
                    return curNode;
            }
        }
        else if( node.getName().equals( curFile ) )
            return node;
        return null;
    }
}
