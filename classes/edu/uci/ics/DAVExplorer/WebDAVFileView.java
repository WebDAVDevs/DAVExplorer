/*
 * Copyright (c) 1998-2003 Regents of the University of California.
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
 * Title:       WebDAVFileView
 * Description: This class is part of the client's GUI.
 * Copyright:   Copyright (c) 1998-2003 Regents of the University of California. All rights reserved.
 * @author      Robert Emmery (dav-exp@ics.uci.edu)
 * @date        2 April 1998
 * @author      Yuzo Kanomata, Joachim Feise (dav-exp@ics.uci.edu)
 * @date        17 March 1999
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        12 January 2001
 * Changes:     Added support for https (SSL)
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        1 October 2001
 * Changes:     Change of package name
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        2 April 2002
 * Changes:     Updated for JDK 1.4
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        25 June 2002
 * Changes:     Fixed the icon locator code to account for drive letters on Windows
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        17 March 2003
 * Changes:     Integrated Brian Johnson's applet changes.
 *              Added better error reporting.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        05 May 2003
 * Changes:     Fixed reordering of columns.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        13 May 2003
 * Changes:     Fixed column sorting.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        9 September 2003
 * Changes:     Fixed response to double click on lock icon.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        23 September 2003
 * Changes:     Integrated the DeltaV code from the Spring 2003 ICS125 team.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        15 November 2003
 * Changes:     Changed the justification of the size and last modified fields.
 */

package edu.uci.ics.DAVExplorer;

import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.table.TableModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.border.BevelBorder;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;
import java.util.Enumeration;
import java.io.File;

public class WebDAVFileView implements ViewSelectionListener, ActionListener
{
    final String[] colNames = { " ",
                                "Lock",
                                "Versions",
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
    Vector displayVersionListeners = new Vector();
    ImageIcon FILE_ICON;
    ImageIcon LOCK_ICON;
    ImageIcon UNLOCK_ICON;
    ImageIcon FOLDER_ICON;
    ImageIcon VERSION_ICON;
    ImageIcon NOVERSION_ICON;
    WebDAVTreeNode parentNode;
    String parentPath = new String();
    String selectedResource;
    int selectedRow = -1;
    int pressRow, releaseRow;

    public WebDAVFileView()
    {
        FOLDER_ICON = GlobalData.getGlobalData().getImageIcon("TreeClosed.gif", "");
        FILE_ICON = GlobalData.getGlobalData().getImageIcon("resource.gif", "");
        LOCK_ICON = GlobalData.getGlobalData().getImageIcon("lck.gif", "");
        UNLOCK_ICON = GlobalData.getGlobalData().getImageIcon("unlck.gif", "");
        VERSION_ICON = GlobalData.getGlobalData().getImageIcon("version.gif", "");
        NOVERSION_ICON = GlobalData.getGlobalData().getImageIcon("noversion.gif", "");

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


            public Class getColumnClass( int col )
            {
                if (data.size() == 0)
                    return null;
                // try to find a cell in the column that has a value
                // using that value as representative for the column
                Object cell = null;
                int count = 0;
                while( cell == null && count < data.size() )
                {
                    cell = getValueAt( count++, col);
                    if( cell != null )
                        break;
                }

                if( cell == null )
                    return null;

                return cell.getClass();
            }

            public  boolean isCellEditable( int row, int col )
            {
                // only allow edit of name
                return (col == 3);
            }


            public void setValueAt( Object value, int row, int col )
            {
                if( GlobalData.getGlobalData().getDebugFileView() )
                {
                    System.err.println( "WebDAVFileView::AbstractTableModel::setValueAt" );
                }

                if (col == 3)
                {
                    // name
                    String val = null;
                    try
                    {
                        int column = table.convertColumnIndexToView(col);
                        if( column == -1 )
                            column = col;     // use default
                        val = (String)table.getValueAt( selectedRow, column );
                    }
                    catch (Exception e)
                    {
                    }

                    if ( val != null)
                    {
                        if( !parentPath.startsWith(GlobalData.WebDAVPrefix) && !parentPath.startsWith(GlobalData.WebDAVPrefixSSL) )
                            return;

                        ((Vector)data.elementAt(row)).setElementAt( value,col );

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
                    ((Vector)data.elementAt(row)).setElementAt( value, col );
                }
                catch (Exception exc)
                {
                    System.out.println(exc);
                }
            }
        };

        sorter = new TableSorter(dataModel);
        table = new JTable(sorter);
        sorter.addMouseListenerToHeaderInTable(table);
        scrPane = new JScrollPane( table );
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


    // Returns the path to the parentNode
    public String getParentPath()
    {
        return parentPath;
    }


    ////////////
    // Implements the Action Listener.
    // Purpose: to listen for a reset to the old selected name
    // for the case of a failure of Rename.
    // Need to do this because Response Interpreter can't access the
    // resetName method because there teh FileView is not accessible
    public void actionPerformed( ActionEvent e )
    {
        resetName();
    }


    ////////////
    // This implements the View Selection Listener Interface
    // The purpose of this listners is to respond to the
    // Selection of a node on the TreeView.  This means
    // that the Table should become populated with the directories
    // and files in the Selected Node.
    public synchronized void selectionChanged(ViewSelectionEvent e)
    {
        if( GlobalData.getGlobalData().getDebugFileView() )
        {
            System.err.println( "WebDAVFileView::selectionChanged" );
        }

        table.clearSelection();
        clearTable();
        selectedRow = -1;

        parentNode = (WebDAVTreeNode)e.getNode();

        // set Parent Path

        TreePath tp = e.getPath();

        Object pathString[] = tp.getPath();
        parentPath = "";
        if( pathString.length > 0 ){
            for (int i = 1; i < pathString.length; i++){
                parentPath += pathString[i].toString() + "/";
            }
        }

        if (table.getRowCount() != 0)
        {
            return;
        }

        TreePath path = e.getPath();
        WebDAVTreeNode tn = (WebDAVTreeNode)path.getLastPathComponent();


        WebDAVTreeNode pn = (WebDAVTreeNode)tn.getParent();

        GlobalData.getGlobalData().setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );

        addDirToTable(tn);

        DataNode dn = tn.getDataNode();
        if (dn == null)
        {
            table.updateUI();
            GlobalData.getGlobalData().resetCursor(); //reset to original cursor
            return;
        }

        Vector sub = dn.getSubNodes();

        if (sub == null)
        {
            //System.out.println(" No  resource");
        }
        else
        {
            for (int i=0; i < sub.size(); i++)
            {
                DataNode d_node = (DataNode)sub.elementAt(i);

        }
            addFileToTable(sub);

        }

        //table.updateUI();
        GlobalData.getGlobalData().resetCursor(); //reset to original cursor

    }


    protected void addDirToTable(WebDAVTreeNode n)
    {
        if( GlobalData.getGlobalData().getDebugFileView() )
        {
            System.err.println( "WebDAVFileView::addDirToTable" );
        }

        // Add the directories to the Table

        int count = n.getChildCount();
        for (int i=0; i < count; i++)
        {
            WebDAVTreeNode child = (WebDAVTreeNode)n.getChildAt(i);
            DataNode d_node = child.getDataNode();
            if (d_node != null)
            {
                Object[] rowObj = new Object[8];
                if( d_node instanceof DeltaVDataNode )
                {
                    rowObj[2] = new Boolean( ((DeltaVDataNode)d_node).hasVersions() );
                }
                else
                {
                    rowObj[2] = "false";
                }
                rowObj[0] = "true";
                rowObj[1] = new Boolean(d_node.isLocked());
                rowObj[3] = d_node.getName();
                rowObj[4] = d_node.getDisplay();
                rowObj[5] = d_node.getType();
                rowObj[6] = new Long(d_node.getSize());
                rowObj[7] = d_node.getDate();

                addRow(rowObj);
            }
        }
        fireTableModuleEvent();
    }


    protected void addFileToTable(Vector v)
    {
        if( GlobalData.getGlobalData().getDebugFileView() )
        {
            System.err.println( "WebDAVFileView::addFileToTable" );
        }

        for (int i=0; i < v.size(); i++)
        {
            DataNode d_node = (DataNode)v.elementAt(i);

            Object[] rowObj = new Object[8];
            if( d_node instanceof DeltaVDataNode )
            {
                rowObj[2] = new Boolean( ((DeltaVDataNode)d_node).hasVersions() );
            }
            else
            {
                rowObj[2] = "false";
            }
            rowObj[0] = "false";
            rowObj[1] = new Boolean(d_node.isLocked());
            rowObj[3] = d_node.getName();
            rowObj[4] = d_node.getDisplay();
            rowObj[5] = d_node.getType();
            rowObj[6] = new Long(d_node.getSize());
            rowObj[7] = d_node.getDate();

            addRow(rowObj);
        }
        fireTableModuleEvent();
    }


    public WebDAVTreeNode getParentNode()
    {
        return parentNode;
    }


    protected String getParentPathString()
    {
        String s = "";
        if( parentNode == null )
            return s;

        TreePath tp = new TreePath(parentNode.getPath());

        if (tp.getPathCount() > 1) {
            for ( int i = 1; i < tp.getPathCount(); i++ )
            {
                s = s + tp.getPathComponent(i);
                if( s.startsWith( GlobalData.WebDAVPrefix ) || s.startsWith( GlobalData.WebDAVPrefixSSL ) )
                    s += "/";
                else if( !s.endsWith( String.valueOf(File.separatorChar) ) )
                    s += File.separatorChar;
            }
        }
        return s;
   }


   /* Yuzo Added: purpose, to get Selected Resource which is the
      old full name of a renamed item. */

    public String getOldSelectedResource()
    {
        return getParentPathString() + selectedResource;
    }


    // Added to return the currently selected collection, null if none of resource
    // null if no collection selected
    public WebDAVTreeNode getSelectedCollection()
    {
        if( GlobalData.getGlobalData().getDebugFileView() )
        {
            System.err.println( "WebDAVFileView::getSelectedCollection" );
        }

        try
        {
            if (selectedRow < 0)
            {
                return null;
            }
            int column = table.convertColumnIndexToView(0); // collection/file
            if( column == -1 )
                column = 0;     // use default
            boolean isCollection =
                new Boolean(table.getValueAt(selectedRow, column).toString()).booleanValue();

            if (isCollection)
            {
                boolean found = false;
                WebDAVTreeNode node = null;
                Enumeration enum = parentNode.children();
        
                while(!found && enum.hasMoreElements())
                {
                    node = (WebDAVTreeNode)enum.nextElement();
        
                    String s = (String) node.getUserObject();
                    column = table.convertColumnIndexToView(3); // name
                    if( column == -1 )
                        column = 3;     // use default
                    if (s.equals(table.getValueAt( selectedRow, column)))
                    {
                        found = true;
                    }
                }
                if (found)
                {
                    return node;
                }
                else
                {
                    return null;
                }
            }
            else
            {
                // a resource so send back null
                return null;
            }
        }
        catch (Exception exc)
        {
            System.out.println("Exception getSelectedCollection");
            exc.printStackTrace();
            return null;
        }
    }


    public boolean isSelectedLocked()
    {
        int column = table.convertColumnIndexToView(1);
        if( column == -1 )
            column = 1;     // use default
        boolean b = new Boolean( table.getValueAt(selectedRow, column).toString()).booleanValue();
        return b;
    }


    // Attempt to get at the selected item's dataNode
    public String getSelectedLockToken()
    {
        if( GlobalData.getGlobalData().getDebugFileView() )
        {
            System.err.println( "WebDAVFileView::getSelectedLockToken" );
        }

        if (selectedRow < 0)
        {
            return null;
        }

        //Get the TreeNode and return dataNode's lockTocken
        WebDAVTreeNode n = getSelectedCollection();

        if (n != null)
        {
            // return lockToken from the Node's dataNode
            DataNode dn = n.getDataNode();
            // Get the lockToken
            return dn.getLockToken();
        }
        // Must be resource

        // 1. Find the resource's data Node
        DataNode dn = parentNode.getDataNode();

        // Do a search of the subNodes
        boolean found = false;
        Vector sub = dn.getSubNodes();
        String token = null;
        DataNode node;

        for( int i = 0; i < sub.size() && !found; i++)
        {
            node = (DataNode)sub.elementAt(i);
            String s = node.getName();
            if(selectedResource.equals( s ))
            {
                found = true;
                token = node.getLockToken();
            }
        }


        // 2. Get the token
        if (token == null)
        {
            System.out.println("Error: getSelectedCollection, dataNode not found for selected item");
            return null;
        }
        else
            return token;
    }


    public boolean hasSelected()
    {
        if( selectedRow >= 0 )
            return true;
        else
            return false;
    }


    public String getSelected()
    {
        if( GlobalData.getGlobalData().getDebugFileView() )
        {
            System.err.println( "WebDAVFileView::getSelected" );
        }

        String s = "";
        if ( selectedRow >= 0 )
        {
            TreePath tp = new TreePath(parentNode.getPath());

            int column = table.convertColumnIndexToView(3); // name
            if( column == -1 )
                column = 3;     // use default
            s =  getParentPathString() + (String)table.getValueAt( selectedRow , column);

            column = table.convertColumnIndexToView(0); // collection/file
            if( column == -1 )
                column = 0;     // use default
            boolean isCollection =
                new Boolean(table.getValueAt(selectedRow, column).toString()).booleanValue();

            if( isCollection )
                return s + "/";
            else
                return s;
        }
        else
        {
            // Return the parent node
            return getParentPathString();
        }
    }


    public void resetName()
    {
        int column = table.convertColumnIndexToView(3);
        if( column == -1 )
            column = 3;     // use default
        table.setValueAt( selectedResource, selectedRow, column );
        update();
    }


    public String getName()
    {
        return selectedResource;
    }


    public synchronized void setLock()
    {
        int row = selectedRow;
        try
        {
            int column = table.convertColumnIndexToView(1);
            if( column == -1 )
                column = 1;     // use default
            table.setValueAt( new Boolean("true"), row, column );
        }
        catch (Exception exc)
        {
        }
        update();
    }


    public synchronized void resetLock()
    {
        int row = selectedRow;
        try
        {
            int column = table.convertColumnIndexToView(1);
            if( column == -1 )
                column = 1;     // use default
            table.setValueAt( new Boolean("false"), row, column );
        }
        catch (Exception exc)
        {
        }
        update();
    }


    public synchronized void update()
    {
        if( GlobalData.getGlobalData().getDebugFileView() )
        {
            System.err.println( "WebDAVFileView::update" );
        }

        table.clearSelection();
        updateTable(data);
    }


    public void setupTable(JTable table)
    {
        if( GlobalData.getGlobalData().getDebugFileView() )
        {
            System.err.println( "WebDAVFileView::setupTable" );
        }

        table.clearSelection();
        table.setSelectionModel(selectionModel);
        selectionModel.addListSelectionListener(new SelectionChangeListener());
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setIntercellSpacing(new Dimension(0,0));
        table.setCellSelectionEnabled(false);
        table.setColumnSelectionAllowed(false);
        table.setShowGrid( false );
        DefaultTableCellRenderer ren;

        // dir/file indicator
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
                    setHorizontalAlignment( SwingConstants.CENTER );
                }
                catch (Exception e)
                {
                    // ignore
                }
            }
        };
        resizeCol.setCellRenderer(resIconRenderer);

        // lock indicator
        resizeCol = table.getColumn(colNames[1]);
        resizeCol.setMaxWidth(30);
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
                    setHorizontalAlignment( SwingConstants.CENTER );
                }
                catch (Exception e)
                {
                    // ignore
                }
            }
        };
        resizeCol.setCellRenderer(lockIconRenderer);

        // version indicator
        resizeCol = table.getColumn(colNames[2]);
        resizeCol.setMaxWidth(50);
        resizeCol.setMinWidth(25);
        DefaultTableCellRenderer versionIconRenderer = new DefaultTableCellRenderer()
        {
            public void setValue(Object value)
            {
                try
                {
                    boolean isVersioned = new Boolean(value.toString()).booleanValue();
                    if (isVersioned)
                        setIcon(VERSION_ICON);
                    else
                        setIcon(NOVERSION_ICON);
                    setHorizontalAlignment( SwingConstants.CENTER );
                }
                catch (Exception e)
                {
                    // ignore
                }
            }
        };
        resizeCol.setCellRenderer(versionIconRenderer);
        
        // name
        resizeCol = table.getColumn(colNames[3]);
        resizeCol.setMinWidth(100);

		// displayname
        resizeCol = table.getColumn(colNames[4]);
        resizeCol.setMinWidth(100);

		// type
        resizeCol = table.getColumn(colNames[5]);
        resizeCol.setMinWidth(100);
        ren = new DefaultTableCellRenderer();
        ren.setHorizontalAlignment(JLabel.CENTER);
        resizeCol.setCellRenderer(ren);

		// size
        resizeCol = table.getColumn(colNames[6]);
        resizeCol.setMinWidth(50);
        ren = new DefaultTableCellRenderer();
        ren.setHorizontalAlignment(JLabel.RIGHT);
        resizeCol.setCellRenderer(ren);

		// last modified
        resizeCol = table.getColumn(colNames[7]);
        resizeCol.setMinWidth(210);
        ren = new DefaultTableCellRenderer();
        // it is bad to use left alignment when the
        // column to the left is right-aligned
        ren.setHorizontalAlignment(JLabel.RIGHT);
        resizeCol.setCellRenderer(ren);
    }


    public JScrollPane getScrollPane()
    {
        return scrPane;
    }


    public void fireTableModuleEvent()
    {
        TableModelEvent e = new TableModelEvent(dataModel);
        synchronized( this )
        {
            if( sorter != null )
                sorter.tableChanged(e);
        }
    }


    public void updateTable(Vector newdata)
    {
        if( GlobalData.getGlobalData().getDebugFileView() )
        {
            System.err.println( "WebDAVFileView::selectionChanged" );
        }

        this.data = newdata;
        fireTableModuleEvent();
    }


    private void addRow(Object[] rowData)
    {
        Vector newRow = new Vector();

        int numOfCols = table.getColumnCount();
        for (int i=0;i<numOfCols;i++)
            newRow.addElement(rowData[i]);

        data.addElement(newRow);
    }

    private void removeRow(int row)
    {
        data.removeElementAt(row);
        fireTableModuleEvent();
    }


    private int getColumn( String columnName )
    {
        for( int i=0; i<table.getColumnCount(); i++ )
        {
            if( table.getColumnName(i).equalsIgnoreCase(columnName) )
                return i;
        }
        return -1;  // column name not found
    }
    
    
    public void clearTable()
    {
        updateTable(new Vector());
        fireTableModuleEvent();
        synchronized( this )
        {
            if( sorter != null )
            {
                // sort by name by default
                sorter.sortByColumn( 3, true );
            }
        }

        selectedRow = -1;
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


    public synchronized void addDisplayVersionListener(ActionListener l)
    {
        displayVersionListeners.addElement(l);
    }


    public synchronized void removeDisplayVersionListener(ActionListener l)
    {
        displayVersionListeners.removeElement(l);
    }


    public void handlePress(MouseEvent e)
    {
        Point cursorPoint = new Point(e.getX(),e.getY());
        pressRow = table.rowAtPoint(cursorPoint);
        selectedRow = pressRow;
    }


    public void handleRelease(MouseEvent e)
    {
        Point cursorPoint = new Point(e.getX(),e.getY());
        releaseRow = table.rowAtPoint(cursorPoint);
        selectedRow = releaseRow;

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
        synchronized( this )
        {
            ls = (Vector)displayLockListeners.clone();
        }
        ActionEvent e = new ActionEvent( this, 0, getParentPathString() + selectedResource );
        for( int i=0; i<ls.size(); i++ )
        {
            ActionListener l = (ActionListener)ls.elementAt(i);
            l.actionPerformed(e);
        }
    }


    public void displayVersions()
    {
        Vector ls;
        synchronized( this )
        {
            ls = (Vector)displayVersionListeners.clone();
        }
        ActionEvent e = new ActionEvent( this, 0, getParentPathString() + selectedResource );
        for( int i=0; i<ls.size(); i++ )
        {
            ActionListener l = (ActionListener)ls.elementAt(i);
            l.actionPerformed(e);
        }
    }


    public void handleDoubleClick(MouseEvent e)
    {
        if( GlobalData.getGlobalData().getDebugFileView() )
        {
            System.err.println( "WebDAVFileView::handleDoubleClick" );
        }

        Point pt = e.getPoint();
        int col = table.columnAtPoint(pt);
        int row = table.rowAtPoint(pt);

        int lockColumn = table.convertColumnIndexToView(1); // lock
        if( lockColumn == -1 )
            lockColumn = 1;     // use default
        int versionColumn = table.convertColumnIndexToView(2); // version
        if( versionColumn == -1 )
        versionColumn = 2;     // use default
        if( (col == lockColumn) && (row != -1) )
        {
            Boolean locked = null;
            try
            {
                locked = (Boolean) table.getValueAt( row, col );
            }
            catch( Exception exc )
            {
                System.out.println(exc);
                return;
            }
            if( (locked != null) && (locked.booleanValue()) )
            {
                displayLock();
                return;
            }
        }
        else if( (col == versionColumn) && (row != -1) )
        {
            Boolean versions = null;
            try
            {
                versions = (Boolean) table.getValueAt( row, col );
            }
            catch( Exception exc )
            {
                System.out.println(exc);
                return;
            }
            if( (versions != null) && (versions.booleanValue()) )
            {
                // TODO: Handle double click on version column: get report and display in grid
                displayVersions();
                return;
            }
        }

        if( selListeners == null )
            return;

        Vector ls;
        synchronized( this )
        {
            ls = (Vector) selListeners.clone();
        }

        int selRow = selectionModel.getMaxSelectionIndex();
        if( selRow != -1 )
        {
            if( sorter == null )
                return;
            int origRow = sorter.getTrueRow(selRow);
            if (origRow == -1)
                return;
            if (origRow > parentNode.getChildCount()-1)
                return;

            WebDAVTreeNode tempNode = (WebDAVTreeNode)parentNode.getChildAt(origRow);
            TreePath path = new TreePath(tempNode.getPath());

            ViewSelectionEvent selEvent = new ViewSelectionEvent(this, tempNode, path );
            for( int i=0; i<ls.size(); i++ )
            {
                ViewSelectionListener l = (ViewSelectionListener) ls.elementAt(i);
                l.selectionChanged(selEvent);
            }
        }
    }


    class SelectionChangeListener implements ListSelectionListener
    {
        public void valueChanged(ListSelectionEvent e)
        {
            if( GlobalData.getGlobalData().getDebugFileView() )
            {
                System.err.println( "WebDAVFileView::SelectionChangeListener::valueChanged" );
            }

            Vector ls;
            synchronized (this)
            {
                ls = (Vector) selListeners.clone();
            }
            int selRow = selectionModel.getMaxSelectionIndex();
            if ((selRow >= 0) && (data.size() > 0) )
            {
                int column = table.convertColumnIndexToView(3); // name
                if( column == -1 )
                    column = 3;     // use default
                selectedResource = (String) table.getValueAt( selRow, column );
                String selResource = new String(selectedResource);
                selectedRow = selRow; // set the class global variable to
                                      // be used later for Select Node
                try
                {
                    column = table.convertColumnIndexToView(0); // collection/file
                    if( column == -1 )
                        column = 0;     // use default
                    boolean isColl = new Boolean(table.getValueAt(selRow, column).toString()).booleanValue();
                    if (isColl)
                    {
                        if( parentPath.startsWith(GlobalData.WebDAVPrefix) || parentPath.startsWith(GlobalData.WebDAVPrefixSSL) ||
                            selResource.startsWith(GlobalData.WebDAVPrefix) || selResource.startsWith(GlobalData.WebDAVPrefixSSL) )
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
}
