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

//
//  This class is part of the client's GUI.
//  It relies on JFC 1.1
//
//  Version: 0.3
//  Author:  Robert Emmery 
//  Date:    4/2/98
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



package WebDAV;

import com.sun.java.swing.*;
import com.sun.java.swing.table.*;
import com.sun.java.swing.tree.*;
import com.sun.java.swing.event.*;
import com.sun.java.swing.border.*;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.zip.*;
import java.io.*;

public class WebDAVFileView implements ViewSelectionListener
{
    private static String jarPath = null;
    private final static String jarExtension =".jar";
    final static String WebDAVClassName = "WebDAV";
    final static String WebDAVPrefix = "http://";
    final static String IconDir = "icons";

    final String[] colNames = {   " ",
                                "  ",
    			"Name",
    			"Display",
    			"Type",
    			"Size",
    			"Last Modified" };

    private Vector data = new Vector();
    JTable table;
    JScrollPane scrPane;
    TableModel dataModel; 
    ListSelectionModel selectionModel = new DefaultListSelectionModel(); 
    TableSorter sorter; 
    Color headerColor;
    Vector selListeners = new Vector();
    Vector renameListeners = new Vector();
    Vector displayLockListeners = new Vector();
    ImageIcon FILE_ICON;
    ImageIcon LOCK_ICON;
    ImageIcon UNLOCK_ICON;
    ImageIcon FOLDER_ICON;
    WebDAVTreeNode parentNode;
    String parentPath = new String();
    String selectedResource;
    int selectedRow;
    int pressRow, releaseRow;
    JFrame mainFrame;

    public WebDAVFileView(JFrame mainFrame)
    {
        this.mainFrame = mainFrame;

        String iconPath = getIconPath();

        if (iconPath == null)
        {
            System.exit(0);
        }

        String folderIconPath =  iconPath + File.separatorChar + "TreeClosed.gif";
        String resPath = iconPath + File.separatorChar + "resource.gif";
        String lckPath = iconPath + File.separatorChar + "lck.gif";
        String unlckPath = iconPath + File.separatorChar + "unlck.gif";

        if( jarPath == null )
        {
            FOLDER_ICON = new ImageIcon(folderIconPath);
            FILE_ICON = new ImageIcon(resPath);
            LOCK_ICON = new ImageIcon(lckPath);
            UNLOCK_ICON = new ImageIcon(unlckPath);
        }
        else
        {
            try
            {
                folderIconPath = iconPath + "TreeClosed.gif";
                resPath = iconPath + "resource.gif";
                lckPath = iconPath + "lck.gif";
                unlckPath = iconPath + "unlck.gif";
                ZipFile file = new ZipFile( jarPath );
                ZipEntry entry = file.getEntry( folderIconPath );
                InputStream is = file.getInputStream( entry );
                int len = (int)entry.getSize();
                if( len != -1 )
		{
                    byte[] ba = new byte[len];
                    is.read( ba, 0, len );
                    FOLDER_ICON = new ImageIcon( ba );
		}

                entry = file.getEntry( resPath );
                is = file.getInputStream( entry );
                len = (int)entry.getSize();
                if( len != -1 )
		{
                    byte[] ba = new byte[len];
                    is.read( ba, 0, len );
                    FILE_ICON = new ImageIcon( ba );
		}

                entry = file.getEntry( lckPath );
                is = file.getInputStream( entry );
                len = (int)entry.getSize();
                if( len != -1 )
		{
                    byte[] ba = new byte[len];
                    is.read( ba, 0, len );
                    LOCK_ICON = new ImageIcon( ba );
		}

                entry = file.getEntry( unlckPath );
                is = file.getInputStream( entry );
                len = (int)entry.getSize();
                if( len != -1 )
		{
                    byte[] ba = new byte[len];
                    is.read( ba, 0, len );
                    UNLOCK_ICON = new ImageIcon( ba );
		}
            }
            catch( IOException e )
            {
                errorMsg("Icon load failure: " + e );
            }
        }


        dataModel = new AbstractTableModel()
        {
            public int getColumnCount()
            {
                return colNames.length;
            }
            
            public int getRowCount()
            {
                return data.size();
            }
            
            public Object getValueAt(int row, int col)
            {
                if (data.size() == 0)
                    return null; 
                return ((Vector)data.elementAt(row)).elementAt(col); 
            }
            
            public String getColumnName(int column)
            {
                return colNames[column];
            }
            
            public Class getColumnClass(int c)
            {
                if (data.size() == 0)
                    return null;
                return getValueAt(0,c).getClass();
            }
            
            public  boolean isCellEditable(int row, int col)
            {
                // only allow edit of name
                return (col == 2);
            }
            
            public void setValueAt(Object value, int row, int column)
            {
                if (column == 2)
                {
                    String val = null;
                    try
                    {
                        val = (String) table.getValueAt(row-1,2);
                    }
                    catch (Exception e)
                    {
                    }
                    if ( val != null)
                    {
	                Object obj[] = parentNode.getUserObjectPath();
                        if (parentPath == null || parentPath.equals("") ){
	                    for (int i = 0; i < obj.length; i++){
		                if (i > 0){
		                    parentPath = parentPath + obj[i];
		                 }
	                    }
                        }
			
                        if (!parentPath.startsWith(WebDAVPrefix))
                            return;

                        ((Vector)data.elementAt(row)).setElementAt(value,column);

                        if (value.equals(selectedResource))
                        {
                            return;
                        }
                        Vector ls;
                        synchronized (this)
                        {
                            ls = (Vector) renameListeners.clone();
                        }
                        ActionEvent e = new ActionEvent(this,0,value.toString());
                        for (int i=0; i<ls.size();i++)
                        {
                            ActionListener l = (ActionListener) ls.elementAt(i);
                            l.actionPerformed(e);
                        }        
                        return;
                    }
                }
                try
                {
                    ((Vector)data.elementAt(row)).setElementAt(value,column);
                }
                catch (Exception exc)
                {
                    System.out.println(exc);
                }
            }
        };

        sorter = new TableSorter(dataModel);
        table = new JTable(sorter);
        scrPane = JTable.createScrollPaneForTable(table);
        scrPane.setPreferredSize(new Dimension(750,400));

        scrPane.setBorder(new BevelBorder(BevelBorder.LOWERED)); 
        MouseListener ml = new MouseAdapter()
        {
            public void mouseClicked(MouseEvent e)
            {
                if(e.getClickCount() == 2)
                {
                    handleDoubleClick(e);
                }
            }
            
            public void mousePressed(MouseEvent e)
            {
                handlePress(e); 
            }
            
            public void mouseReleased(MouseEvent e)
            {
                handleRelease(e);
            }
        };
        table.addMouseListener(ml);
        setupTable(table); 
        table.updateUI();
    }

    ////////////
    // This implements the View Selection Listener Interface
    // The purpose of this listners is to respond to the 
    // Selection of a node on the TreeView.  This means 
    // that the Table should become populated with the directories 
    // and files in the Selected Node.  
    public void selectionChanged(ViewSelectionEvent e)
    {
        table.clearSelection();
        clearTable();

        parentNode = (WebDAVTreeNode)e.getNode();

        if (table.getRowCount() != 0)
        {
            return;
        }

        TreePath path = e.getPath();
        WebDAVTreeNode tn = (WebDAVTreeNode)path.getLastPathComponent();

        Cursor c = mainFrame.getCursor(); // save original cursor
        mainFrame.setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );

        addDirToTable(tn);

        DataNode dn = tn.getDataNode();
        if (dn == null)
        {
            mainFrame.setCursor( c ); //reset to original cursor
            return;
        }

        Vector sub = dn.getSubNodes();

        if (sub == null)
        {
        }
        else
        {
    	    addFileToTable(sub);
        }
        table.updateUI(); 
        mainFrame.setCursor( c ); //reset to original cursor
    }

    protected void addDirToTable(WebDAVTreeNode n)
    {
        // Add the directories to the Table

        int count = n.getChildCount();
        for (int i=0; i < count; i++)
        {
            WebDAVTreeNode child = (WebDAVTreeNode) n.getChildAt(i);
            DataNode d_node = child.getDataNode();
            if (d_node == null)
                    return;
            Object[] rowObj = new Object[7];
            rowObj[0] = "true";
            rowObj[1] = new Boolean(d_node.isLocked());
            rowObj[2] = d_node.getName();
            rowObj[3] = d_node.getDisplay();
            rowObj[4] = d_node.getType();
            rowObj[5] = (new Long(d_node.getSize())).toString();
            rowObj[6] = d_node.getDate();

            addRow(rowObj);
        }
    }

    protected void addFileToTable(Vector v)
    {
        for (int i=0; i < v.size(); i++)
        {
            Object[] rowObj = new Object[7];
            DataNode d_node = (DataNode)v.elementAt(i);

            rowObj[0] = "false";
            rowObj[1] = new Boolean(d_node.isLocked());
            rowObj[2] = d_node.getName();
            rowObj[3] = d_node.getDisplay();
            rowObj[4] = d_node.getType();
            rowObj[5] = (new Long(d_node.getSize())).toString();
            rowObj[6] = d_node.getDate();
            addRow(rowObj);
        }
    }

   public WebDAVTreeNode getParentNode(){
	return parentNode;
   }

   protected String getParentPathString(){
	String s = "";
	TreePath tp = new TreePath(parentNode.getPath());

	if (tp.getPathCount() > 1) {
		for ( int i = 1; i < tp.getPathCount(); i++ ) {
		    s = s + tp.getPathComponent(i) + "/";
		}

	}
	return s;

   }


   /* Yuzo Added: purpose, to get Selected Resource which is the 
      old full name of a renamed item. */

    public String getOldSelectedResource(){
	return getParentPathString() + selectedResource;
    }
   

    public String getSelected(){
        String s = "";

        if ( selectedRow >= 0 )
        {
            TreePath tp = new TreePath(parentNode.getPath());

	    s =  getParentPathString() + (String)table.getValueAt( selectedRow , 2);
	    return s;
	} else{
	    return null;
	}
    }

    public void resetName()
    {
        table.setValueAt(selectedResource, selectedRow,2);
        update();
    }
    
    public String getName()
    {
        return selectedResource;
    }
    
    public void setLock()
    {
        int row = selectedRow;
        try
        {
            table.setValueAt(new Boolean("true"),row,1);
        }
        catch (Exception exc)
        {
        };
        update();
    }
    
    public void resetLock()
    {
        int row = selectedRow;
        try
        {
            table.setValueAt(new Boolean("false"),row,1);
        }
        catch (Exception exc)
        {
        };
        update();
    }

    public void update()
    {
        table.clearSelection();
        updateTable(data);
    }
    
    private static String getIconPath()
    {
        String icons = WebDAVClassName + File.separatorChar + IconDir;
        String classPath = System.getProperty("java.class.path");
        if (classPath == null)
        {
            errorMsg("Toolbar:\nNo Classpath set." );
            return null;
        }
        
        StringTokenizer paths = new StringTokenizer(classPath,":;");

        while (paths.hasMoreTokens())
        {
            String nextPath = paths.nextToken();
            String lowerPath = nextPath.toLowerCase();
            if( lowerPath.endsWith( jarExtension ) )
            {
                jarPath = nextPath;
                int pos = lowerPath.indexOf( jarExtension );
                nextPath = nextPath.substring( 0, pos );
            }
            if (!nextPath.endsWith(new Character(File.separatorChar).toString()))
                nextPath += File.separatorChar;
            nextPath += icons;
            File iconDirFile = new File(nextPath);
            if (iconDirFile.exists())
                return nextPath;
            if( jarPath != null )
            {
                try
                {
                    ZipFile zfile = new ZipFile( jarPath );
                    icons = WebDAVClassName + "/" + IconDir + "/";
                    ZipEntry entry = zfile.getEntry( icons );
                    if( entry != null )
                    {
                        return icons;
                    }
                    else
                        jarPath = null;
                }
                catch( IOException e )
                {
                }
            }
        }
        errorMsg("Toolbar:\nPath to icons not found." );
        return null;
    }

    public void setupTable(JTable table)
    {
        table.clearSelection();
        table.setSelectionModel(selectionModel);
        selectionModel.addListSelectionListener(new SelectionChangeListener());
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); 
        table.setIntercellSpacing(new Dimension(0,0));
        table.setCellSelectionEnabled(false);
        table.setColumnSelectionAllowed(false);
        DefaultTableCellRenderer ren;

        TableColumn resizeCol = table.getColumn(colNames[0]);
        resizeCol.setMaxWidth(25);
        resizeCol.setMinWidth(25);

        DefaultTableCellRenderer resIconRenderer = new DefaultTableCellRenderer()
        {
            public void setValue(Object value)
            {
                try
                {
                    boolean isColl = new Boolean(value.toString()).booleanValue();
                    if (isColl)
                        setIcon(FOLDER_ICON);
                    else
                        setIcon(FILE_ICON);
                }
                catch (Exception e)
                {
                }
            }
        };
        resizeCol.setCellRenderer(resIconRenderer);

        resizeCol = table.getColumn(colNames[1]);
        resizeCol.setMaxWidth(25);
        resizeCol.setMinWidth(25);
        DefaultTableCellRenderer lockIconRenderer = new DefaultTableCellRenderer()
        {
            public void setValue(Object value)
            {
                try
                {
                    boolean isLocked = new Boolean(value.toString()).booleanValue();
                    if (isLocked)
                        setIcon(LOCK_ICON);
                    else
                        setIcon(UNLOCK_ICON);
                }
                catch (Exception e)
                {
                }
            }
        };
        resizeCol.setCellRenderer(lockIconRenderer);

        resizeCol = table.getColumn(colNames[2]);
        resizeCol.setMinWidth(100);

        resizeCol = table.getColumn(colNames[3]);
        resizeCol.setMinWidth(100);

        resizeCol = table.getColumn(colNames[4]);
        resizeCol.setMinWidth(100);
        ren = new DefaultTableCellRenderer();
        ren.setHorizontalAlignment(JLabel.CENTER);
        resizeCol.setCellRenderer(ren);

        resizeCol = table.getColumn(colNames[5]);
        resizeCol.setMinWidth(50);
        ren = new DefaultTableCellRenderer();
        ren.setHorizontalAlignment(JLabel.CENTER);
        resizeCol.setCellRenderer(ren);

        resizeCol = table.getColumn(colNames[6]);
        resizeCol.setMinWidth(150);
        ren = new DefaultTableCellRenderer();
        ren.setHorizontalAlignment(JLabel.CENTER);
        resizeCol.setCellRenderer(ren);
    }

    public JScrollPane getScrollPane()
    {
        return scrPane;
    }

    public void fireTableModuleEvent()
    {
        TableModelEvent e = new TableModelEvent(dataModel);
        sorter.tableChanged(e);
    } 

    public void updateTable(Vector newdata)
    {
        this.data = newdata;
        fireTableModuleEvent();
        sorter.sortByColumn(2,true);
        table.updateUI(); 
    }

    public void addRow(Object[] rowData)
    {
        Vector newRow = new Vector();

        int numOfCols = table.getColumnCount();
        for (int i=0;i<numOfCols;i++)
            newRow.addElement(rowData[i]); 

        data.addElement(newRow);
        fireTableModuleEvent();
        sorter.sortByColumn(2,true);  
        table.updateUI();
    }
    
    public void removeRow(int row)
    {
        data.removeElementAt(row);  
        fireTableModuleEvent();
        sorter.sortByColumn(2,true);
        table.updateUI();
    }

    public void clearTable()
    {
        updateTable(new Vector());

	selectedRow = -1;
    }

    public void treeSelectionChanged(ViewSelectionEvent e)
    {
        table.clearSelection();
        clearTable();
        WebDAVTreeNode t_node = (WebDAVTreeNode) e.getNode();
        parentNode = t_node;
        parentPath = e.getPath().toString();
        int cnt = t_node.getChildCount();
        if (table.getRowCount() != 0)
            return;
        for (int i=0;i<cnt;i++)
        {
            WebDAVTreeNode child = (WebDAVTreeNode) t_node.getChildAt(i);
            DataNode d_node = child.getDataNode();
            if (d_node == null)
	            return;
            Object[] rowObj = new Object[7];
            rowObj[0] = "true";
            rowObj[1] = new Boolean(d_node.isLocked());
            rowObj[2] = d_node.getName();
            rowObj[3] = d_node.getDisplay();
            rowObj[4] = d_node.getType();
            rowObj[5] = (new Long(d_node.getSize())).toString();
            rowObj[6] = d_node.getDate();
            addRow(rowObj);
        }
        DataNode this_data_node = t_node.getDataNode();
        if (this_data_node == null) 
            return;
        Vector subs = this_data_node.getSubNodes();
        if (subs == null)
            return;
        for (int i=0;i<subs.size();i++)
        {
            Object[] rowObj = new Object[7];
            DataNode d_node = (DataNode) subs.elementAt(i);

            rowObj[0] = "false";
            rowObj[1] = new Boolean(d_node.isLocked());
            rowObj[2] = d_node.getName();
            rowObj[3] = d_node.getDisplay();
            rowObj[4] = d_node.getType();
            rowObj[5] = (new Long(d_node.getSize())).toString();
            rowObj[6] = d_node.getDate();
            addRow(rowObj);
        }
    }

    public synchronized void addViewSelectionListener(ViewSelectionListener l)
    {
        selListeners.addElement(l);
    }
    
    public synchronized void removeViewSelectionListener(ViewSelectionListener l)
    {
        selListeners.removeElement(l);
    }
    
    public synchronized void addRenameListener(ActionListener l)
    {
        renameListeners.addElement(l);
    }
    
    public synchronized void removeRenameListener(ActionListener l)
    {
        renameListeners.removeElement(l);
    }
    
    public synchronized void addDisplayLockListener(ActionListener l)
    {
        displayLockListeners.addElement(l);
    }
    
    public synchronized void removeDisplayLockListener(ActionListener l)
    {
        displayLockListeners.removeElement(l);
    }

    public void handlePress(MouseEvent e)
    {
        Point cursorPoint = new Point(e.getX(),e.getY());
        pressRow = table.rowAtPoint(cursorPoint); 
    }

    public void handleRelease(MouseEvent e)
    {
        Point cursorPoint = new Point(e.getX(),e.getY());
        releaseRow = table.rowAtPoint(cursorPoint);
        if (pressRow != -1)
        {
            if (releaseRow != -1)
            {
                if (pressRow != releaseRow){
                    //System.out.println("WebDAVFileView: Got Drag");
		}
            }
            else
            {
                //System.out.println("dragged outside");
            }
        }
    }

    public void displayLock()
    {
        Vector ls;
        synchronized (this)
        {
            ls = (Vector) displayLockListeners.clone();
        }
        ActionEvent e = new ActionEvent(this,0,null);
        for (int i=0; i<ls.size();i++)
        {
            ActionListener l = (ActionListener) ls.elementAt(i);
            l.actionPerformed(e);
        }
    }
    
    public void handleDoubleClick(MouseEvent e)
    {
        Point pt = e.getPoint();
        int col = table.columnAtPoint(pt);
        int row = table.rowAtPoint(pt);

        if ( (col == 1) && (row != -1) )
        {
            Boolean locked = null;
            try
            {
                locked = (Boolean) table.getValueAt(row,col);
            }
            catch (Exception exc)
            {
		System.out.println(exc);
                return;
            }
            if ( (locked != null) && (locked.booleanValue()) )
            {
                displayLock();
                return;
            }
        }

        Vector ls;
        if (selListeners == null)
        {
            return;
        }
        synchronized (this)
        {
            ls = (Vector) selListeners.clone();
        }

        int selRow = selectionModel.getMaxSelectionIndex();
        if (selRow != -1)
        {
            int origRow = sorter.getTrueRow(selRow);
            if (origRow == -1)
            {
	            return;
            }

            if (origRow > parentNode.getChildCount()-1)
                return;

            WebDAVTreeNode tempNode = (WebDAVTreeNode)parentNode.getChildAt(origRow);
            TreePath path = new TreePath(tempNode.getPath());

            ViewSelectionEvent selEvent = new ViewSelectionEvent(this, tempNode, path );
            for (int i=0; i<ls.size();i++)
            {
                ViewSelectionListener l = (ViewSelectionListener) ls.elementAt(i);
                l.selectionChanged(selEvent);
            }

	    selectionChanged( selEvent );
        }
    }

    class SelectionChangeListener implements ListSelectionListener
    {
        public void valueChanged(ListSelectionEvent e)
        {
            Vector ls;
            synchronized (this)
            {
                ls = (Vector) selListeners.clone();
            }
            int selRow = selectionModel.getMaxSelectionIndex();
            if ((selRow >= 0) && (data.size() > 0) )
            {
                selectedResource = (String) table.getValueAt(selRow,2);
                String selResource = new String(selectedResource);
                selectedRow = selRow;
                try
                {
                    boolean isColl = new Boolean(table.getValueAt(selRow,0).toString()).booleanValue();
                    if (isColl)
                    {
                        if (parentPath.startsWith(WebDAVPrefix) || selResource.startsWith(WebDAVPrefix) )
                        {
                            if( !selResource.endsWith( "/" ) )
                                selResource += "/";
                        }
                        else
                            selResource += new Character(File.separatorChar).toString();
			
                    }
                }
                catch (Exception exc)
                {
                    exc.printStackTrace();
                    return;
                }



                ViewSelectionEvent selEvent = new ViewSelectionEvent(this,null,null);
                for (int i=0; i<ls.size();i++)
                {
                    ViewSelectionListener l = (ViewSelectionListener) ls.elementAt(i);
                }
            }
        }
    }
    
    private static void errorMsg(String str)
    {
        JOptionPane pane = new JOptionPane();
        Object[] options = { "OK" };
        pane.showOptionDialog( null, str,"Error Message", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
    }
}
