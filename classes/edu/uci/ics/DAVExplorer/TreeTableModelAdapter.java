/*
 * Copyright (c) 2001-2004 Regents of the University of California.
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
 * Title:       Property Dialog
 * Description: Dialog for viewing/modifying DAV properties
 * Copyright:   Copyright (c) 2001-2004 Regents of the University of California. All rights reserved.
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
 * @date        08 February 2004
 * Changes:     Added Javadoc templates
 */

package edu.uci.ics.DAVExplorer;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.TreePath;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;


/**
 * 
 */
public class TreeTableModelAdapter extends AbstractTableModel
{
    /**
     * Constructor
     * @param treeTableModel
     * @param tree
     */
    public TreeTableModelAdapter(TreeTableModel treeTableModel, JTree tree)
    {
        this.tree = tree;
        this.treeTableModel = treeTableModel;

        tree.addTreeExpansionListener(new TreeExpansionListener()
        {
            // Don't use fireTableRowsInserted() here; the selection model
            // would get updated twice.
            public void treeExpanded(TreeExpansionEvent event)
            {
                fireTableDataChanged();
            }
            public void treeCollapsed(TreeExpansionEvent event)
            {
                fireTableDataChanged();
            }
        });

        // Install a TreeModelListener that can update the table when
        // tree changes. We use delayedFireTableDataChanged as we can
        // not be guaranteed the tree will have finished processing
        // the event before us.
        treeTableModel.addTreeModelListener(new TreeModelListener()
        {
            public void treeNodesChanged(TreeModelEvent e)
            {
                delayedFireTableDataChanged();
            }
            public void treeNodesInserted(TreeModelEvent e)
            {
                delayedFireTableDataChanged();
            }
            public void treeNodesRemoved(TreeModelEvent e)
            {
                delayedFireTableDataChanged();
            }
            public void treeStructureChanged(TreeModelEvent e)
            {
                delayedFireTableDataChanged();
            }
        });
    }


    /**
     * TableModel interface
     * 
     * @return
     */
    public int getColumnCount()
    {
        return treeTableModel.getColumnCount();
    }


    /**
     * TableModel interface
     * @param column
     * 
     * @return
     */
    public String getColumnName(int column)
    {
        return treeTableModel.getColumnName(column);
    }


    /**
     * TableModel interface
     * @param column
     * 
     * @return
     */
    public Class getColumnClass(int column)
    {
        return treeTableModel.getColumnClass(column);
    }


    /**
     * TableModel interface
     * 
     * @return
     */
    public int getRowCount()
    {
        return tree.getRowCount();
    }


    /**
     * TableModel interface
     * @param row
     * 
     * @return
     */
    protected Object nodeForRow(int row)
    {
        TreePath treePath = tree.getPathForRow(row);
        return treePath.getLastPathComponent();
    }


    /**
     * TableModel interface
     * @param row
     * @param column
     * 
     * @return
     */
    public Object getValueAt(int row, int column)
    {
        return treeTableModel.getValueAt(nodeForRow(row), column);
    }


    /**
     * TableModel interface
     * @param row
     * @param column
     * 
     * @return
     */
    public boolean isCellEditable(int row, int column)
    {
        return treeTableModel.isCellEditable(nodeForRow(row), column);
    }


    /**
     * TableModel interface
     * @param value
     * @param row
     * @param column
     */
    public void setValueAt(Object value, int row, int column)
    {
        treeTableModel.setValueAt(value, nodeForRow(row), column);
    }

    /**
     * Invokes fireTableDataChanged after all the pending events have been
     * processed. SwingUtilities.invokeLater is used to handle this.
     */
    protected void delayedFireTableDataChanged()
    {
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                fireTableDataChanged();
            }
        });
    }


    private JTree tree;
    private TreeTableModel treeTableModel;
}
