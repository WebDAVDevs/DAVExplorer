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

package WebDAV;

// We are importing JFC packages (these will be included in JDK 1.2)
import com.sun.java.swing.*;
import com.sun.java.swing.tree.*;
import com.sun.java.swing.event.*;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;


public class WebDAVTreeView
{
    JTree tree;
    final static String WebDAVRoot = "WebDAV Explorer";
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

// Constructor

    public WebDAVTreeView(JFrame mainFrame)
    {
        tree = new JTree(treeModel);
        tree.setSelectionModel(selectionModel);
        selectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        selectionModel.addTreeSelectionListener(new SelectionChangeListener());
        tree.setRowHeight(-1);
        this.mainFrame = mainFrame;
        startDirName = System.getProperty("user.home");
        if (startDirName == null){
            startDirName = new Character(File.separatorChar).toString();
	}

// The items below have been removed because they interfered with the 
// tree selection events.  They would cause a secondary tree selection
// event in the cases where the X and Y position of the clicked mouse
// would move over another directory when the screen was redrawn after 
// an initial tree selection.
//
// We're interested in single clicks
//        MouseListener ml = new MouseAdapter()
//        {
//            public void mouseClicked(MouseEvent e)
//            {
//                int selRow = tree.getRowForLocation(e.getX(), e.getY());
//                TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
//                if(selRow != -1)
//                {
//                    if(e.getClickCount() == 1)
//                    {
//                        handleSingleClick(selPath);
//                    }
//                    else if(e.getClickCount() == 2)
//                    {
//                        handleDoubleClick(selPath);
//                    }
//                }
//            }
//            
//            public void mouseExited(MouseEvent e)
//            {
//            }
//        };
    }
  
//    public void handleSingleClick(TreePath selPath)
//    {
//        // Select the clicked item.
//
//        if ( (selPath != null) && (!tree.isPathSelected(selPath)) )
//        {
//            selectionModel.setSelectionPath(selPath);
//        }
//    }
//
//    public void handleDoubleClick(TreePath selPath)
//    {
//        // Nothing to do here.
//    }


    public JScrollPane getScrollPane()
    {
        // We package the whole TreeView inside a Scroll Pane, returned
        // by this function.

        JScrollPane sp = new JScrollPane();
        sp.getViewport().add(tree); 
        sp.setPreferredSize(new Dimension(240,400));
        return(sp);
    }


    public void tableSelectionChanged(ViewSelectionEvent e)
    {
    //  This is where selection event from the table View are routed to.
    //  The Event includes the information needed to expand/select
    //  the particular row. 

        Object item = e.getNode();
        if (item == null)  
            return; 
        else
        {
            int index = (new Integer(item.toString())).intValue(); 
            int row = tree.getRowForPath(currPath);
            tree.expandRow(row);
            int childRow = row + index + 1;
            for (int i=row+1;i<=childRow;i++)
                tree.collapseRow(i);
            tree.clearSelection();
            tree.setSelectionRow(childRow);
            tree.expandRow(childRow);
        }
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

    class SelectionChangeListener implements TreeSelectionListener
    {
        // This is where we handle the tree selection event. 
        // We send the event to all the registered listeners.

        public void valueChanged(TreeSelectionEvent e)
        {
            Vector ls;
            synchronized (this)
            {
                ls = (Vector) selListeners.clone();
            }
            TreePath path = e.getPath();
            if (tree.isPathSelected(path))
            {
                currPath = path;
                String strPath = constructPath(path);
                currNode = (WebDAVTreeNode) path.getLastPathComponent();
                ViewSelectionEvent selEvent = new ViewSelectionEvent(this,currNode,strPath);

		// The following 2 lines change the cursor in the JFrame 
		// so that the user has feedback that the events are
		// being processed.
		// Note: this depends on mainFrame being set.
		Cursor c = mainFrame.getCursor(); // save original cursor
		mainFrame.setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );

                for (int i=0; i<ls.size();i++)
                {
                    ViewSelectionListener l = (ViewSelectionListener) ls.elementAt(i);
                    l.selectionChanged(selEvent);
                }

		mainFrame.setCursor( c ); //reset to original cursor
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
        tree.clearSelection();
        tree.setSelectionRow(1);
        tree.expandPath(tree.getPathForRow(0));
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
}
