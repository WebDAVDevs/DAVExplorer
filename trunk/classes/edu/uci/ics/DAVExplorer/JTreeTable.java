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
 * Title:       TreeTable control
 * Description: Combines a JTree and a JTable for viewing
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
import javax.swing.JTable;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.ListSelectionModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.CellEditorListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.ChangeEvent;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.Enumeration;


/**
 * 
 */
public class JTreeTable extends JTable
{
    /**
     * Constructor
     * @param treeTableModel
     */
    public JTreeTable(PropModel treeTableModel)
    {
        super();
        // Create the tree. It will be used as a renderer and editor.
        tree = new TreeTableCellRenderer(treeTableModel);
        // Install a tableModel representing the visible rows in the tree.
        treeTableModel.setTree(tree);
        super.setModel( new TreeTableModelAdapter(treeTableModel, tree) );
        tree.setRootVisible( false );
        // Force the JTable and JTree to share their row selection models.
        ListToTreeSelectionModelWrapper selectionWrapper = new ListToTreeSelectionModelWrapper();
        tree.setSelectionModel(selectionWrapper);
        setSelectionModel(selectionWrapper.getListSelectionModel());

        // Install the tree editor renderer and editor.
        setDefaultRenderer(TreeTableModel.class, tree);
        setDefaultEditor(TreeTableModel.class, new TreeTableCellEditor());
        setShowGrid(false);
        setIntercellSpacing(new Dimension(0, 0));
        // Make the tree and table row heights the same.
        setRowHeight(18);
        // adjust the column sizes
        Enumeration enumeration = getColumnModel().getColumns();
        while( enumeration.hasMoreElements() )
        {
            TableColumn col = (TableColumn)enumeration.nextElement();
            col.setPreferredWidth( 200 );
        }
        tree.setVisibleRowCount(getRowCount());
    }


    /**
     * Overridden to message super and forward the method to the tree.
     * Since the tree is not actually in the component hierachy it will
     * never receive this unless we forward it in this manner.
     */
    public void updateUI()
    {
        super.updateUI();
        if(tree != null)
        {
            tree.updateUI();
        }
        // Use the tree's default foreground and background colors in the table.
        LookAndFeel.installColorsAndFont(this, "Tree.background", "Tree.foreground", "Tree.font");
    }


    /** Workaround for BasicTableUI anomaly. Make sure the UI never tries to
     * paint the editor. The UI currently uses different techniques to
     * paint the renderers and editors and overriding setBounds() below
     * is not the right thing to do for an editor. Returning -1 for the
     * editing row in this case, ensures the editor is never painted.
     * 
     * @return
     */
    public int getEditingRow()
    {
        return (getColumnClass(editingColumn) == TreeTableModel.class) ? -1 : editingRow;
    }


    /**
     * 
     * @return
     */
    public Dimension getPreferredScrollableViewportSize()
    {
        Dimension d = super.getPreferredSize();
        d.height = getRowCount() * (getRowHeight()+getRowMargin());
        Insets insets = getInsets();
        d.height += insets.top + insets.bottom;
        d.width += insets.left + insets.right;
        return d;
    }


    /**
     * Overridden to pass the new rowHeight to the tree.
     * @param rowHeight
     */
    public void setRowHeight(int rowHeight)
    {
        super.setRowHeight(rowHeight);
        if (tree != null && tree.getRowHeight() != rowHeight)
            tree.setRowHeight(getRowHeight());
    }


    /**
     * Returns the tree that is being shared between the models.
     * 
     * @return
     */
    public JTree getTree()
    {
        return tree;
    }


    /**
     * The TreeCellRenderer used to display the JTree nodes
     */
    public class TreeTableCellRenderer extends JTree implements TableCellRenderer
    {
        /**
         * 
         * @param model
         */
        public TreeTableCellRenderer(TreeModel model)
        {
            super(model);
        }


        /**
         * updateUI is overridden to set the colors of the Tree's renderer
         * to match that of the table.
         */
        public void updateUI()
        {
            super.updateUI();
            // Make the tree's cell renderer use the table's cell selection colors.
            TreeCellRenderer tcr = getCellRenderer();
            if( tcr instanceof DefaultTreeCellRenderer )
            {
                DefaultTreeCellRenderer dtcr = ((DefaultTreeCellRenderer)tcr);
                // For 1.1 uncomment this, 1.2 has a bug that will cause an
                // exception to be thrown if the border selection color is null.
                // dtcr.setBorderSelectionColor(null);
                dtcr.setTextSelectionColor(UIManager.getColor("Table.selectionForeground"));
                dtcr.setBackgroundSelectionColor(UIManager.getColor("Table.selectionBackground"));
            }
        }


        /**
         * Sets the row height of the tree, and forwards the row height to the table.
         * @param rowHeight
         */
        public void setRowHeight(int rowHeight)
        {
            if (rowHeight > 0)
            {
                super.setRowHeight(rowHeight);
                if (JTreeTable.this != null && JTreeTable.this.getRowHeight() != rowHeight)
                    JTreeTable.this.setRowHeight(getRowHeight());
            }
        }


        /**
         * This is overridden to set the height to match that of the JTable.
         * @param x
         * @param y
         * @param w
         * @param h
         */
        public void setBounds(int x, int y, int w, int h)
        {
            super.setBounds(x, 0, w, JTreeTable.this.getHeight());
        }


        /**
         * Sublcassed to translate the graphics such that the last visible
         * row will be drawn at 0,0.
         * @param g
         */
        public void paint(Graphics g)
        {
            g.translate(0, -visibleRow * getRowHeight());
            super.paint(g);
        }


        /**
         * TreeCellRenderer method. Overridden to update the visible row.
         * @param table
         * @param value
         * @param isSelected
         * @param hasFocus
         * @param row
         * @param column
         * 
         * @return
         */
        public Component getTableCellRendererComponent(JTable table, Object value,
                                   boolean isSelected, boolean hasFocus, int row, int column)
        {
            if(isSelected)
                setBackground(table.getSelectionBackground());
            else
                setBackground(table.getBackground());

            visibleRow = row;
            return this;
        }

        /** Last table/tree row asked to renderer. */
        protected int visibleRow;
    }


    /**
     * TreeTableCellEditor implementation
     */
    public class TreeTableCellEditor implements TableCellEditor
    {
        /**
         * 
         * @param table
         * @param value
         * @param isSelected
         * @param r
         * @param c
         * 
         * @return
         */
        public Component getTableCellEditorComponent(JTable table, Object value,
                                 boolean isSelected, int r, int c)
        {
            return tree;
        }

        /**
         * Overridden to return false, and if the event is a mouse event
         * it is forwarded to the tree.<p>
         * The behavior for this is debatable, and should really be offered
         * as a property. By returning false, all keyboard actions are
         * implemented in terms of the table. By returning true, the
         * tree would get a chance to do something with the keyboard
         * events. For the most part this is ok. But for certain keys,
         * such as left/right, the tree will expand/collapse where as
         * the table focus should really move to a different column. Page
         * up/down should also be implemented in terms of the table.
         * By returning false this also has the added benefit that clicking
         * outside of the bounds of the tree node, but still in the tree
         * column will select the row, whereas if this returned true
         * that wouldn't be the case.
         * <p>By returning false we are also enforcing the policy that
         * the tree will never be editable (at least by a key sequence).
         * @param e
         * 
         * @return
         */
        public boolean isCellEditable(EventObject e)
        {
            if( e instanceof MouseEvent )
            {
                for (int counter = getColumnCount() - 1; counter >= 0; counter--)
                {
                    if (getColumnClass(counter) == TreeTableModel.class)
                    {
                        MouseEvent me = (MouseEvent)e;
                        MouseEvent newME = new MouseEvent(tree, me.getID(),
                               me.getWhen(), me.getModifiers(),
                               me.getX() - getCellRect(0, counter, true).x,
                               me.getY(), me.getClickCount(),
                               me.isPopupTrigger());
                        tree.dispatchEvent(newME);
                        break;
                    }
                }
            }
            return false;
        }


        /**
         * CellEditor interface
         * 
         * @return
         */
        public Object getCellEditorValue()
        {
            return null;
        }


        /**
         * CellEditor interface 
         * @param anEvent
         * 
         * @return
         */
        public boolean shouldSelectCell(EventObject anEvent)
        {
            return false;
        }


        /**
         * CellEditor interface
         * 
         * @return
         */
        public boolean stopCellEditing()
        {
            return true;
        }


        /**
         * CellEditor interface
         */
        public void cancelCellEditing()
        {
        }


        /**
         * CellEditor interface
         * @param l
         */
        public void addCellEditorListener(CellEditorListener l)
        {
            listenerList.add(CellEditorListener.class, l);
        }


        /**
         * CellEditor interface
         * @param l
         */
        public void removeCellEditorListener(CellEditorListener l)
        {
            listenerList.remove(CellEditorListener.class, l);
        }


        /**
         * Notify all listeners that have registered interest for
         * notification on this event type.
         */
        protected void fireEditingStopped()
        {
            Object[] listeners = listenerList.getListenerList();
            for( int i = listeners.length-2; i>=0; i-=2 )
            {
                if (listeners[i]==CellEditorListener.class)
                {
                    ((CellEditorListener)listeners[i+1]).editingStopped(new ChangeEvent(this));
                }
            }
        }


        /**
         * Notify all listeners that have registered interest for
         * notification on this event type.
         */
        protected void fireEditingCanceled()
        {
            Object[] listeners = listenerList.getListenerList();
            for( int i = listeners.length-2; i>=0; i-=2 )
            {
                if (listeners[i]==CellEditorListener.class)
                {
                    ((CellEditorListener)listeners[i+1]).editingCanceled(new ChangeEvent(this));
                }
            }
        }

        protected EventListenerList listenerList = new EventListenerList();
    }


    /**
     * ListToTreeSelectionModelWrapper extends DefaultTreeSelectionModel
     * to listen for changes in the ListSelectionModel it maintains. Once
     * a change in the ListSelectionModel happens, the paths are updated
     * in the DefaultTreeSelectionModel.
     */
    class ListToTreeSelectionModelWrapper extends DefaultTreeSelectionModel
    {
        /**
         * 
         */
        public ListToTreeSelectionModelWrapper()
        {
            super();
            getListSelectionModel().addListSelectionListener(createListSelectionListener());
        }


        /**
         * Returns the list selection model. ListToTreeSelectionModelWrapper
         * listens for changes to this model and updates the selected paths
         * accordingly.
         * 
         * @return
         */
        ListSelectionModel getListSelectionModel()
        {
            return listSelectionModel;
        }


        /**
         * This is overridden to set <code>updatingListSelectionModel</code>
         * and message super. This is the only place DefaultTreeSelectionModel
         * alters the ListSelectionModel.
         */
        public void resetRowSelection()
        {
            if(!updatingListSelectionModel)
            {
                updatingListSelectionModel = true;
                try
                {
                    super.resetRowSelection();
                }
                finally
                {
                    updatingListSelectionModel = false;
                }
            }
        }


        /**
         * Creates and returns an instance of ListSelectionHandler.
         * 
         * @return
         */
        protected ListSelectionListener createListSelectionListener()
        {
            return new ListSelectionHandler();
        }


        /**
         * If <code>updatingListSelectionModel</code> is false, this will
         * reset the selected paths from the selected rows in the list
         * selection model.
         */
        protected void updateSelectedPathsFromSelectedRows()
        {
            if(!updatingListSelectionModel)
            {
                updatingListSelectionModel = true;
                try
                {
                    int min = listSelectionModel.getMinSelectionIndex();
                    int max = listSelectionModel.getMaxSelectionIndex();
                    clearSelection();
                    if(min != -1 && max != -1)
                    {
                        for(int counter = min; counter <= max; counter++)
                        {
                            if(listSelectionModel.isSelectedIndex(counter))
                            {
                                TreePath selPath = tree.getPathForRow(counter);
                                if(selPath != null)
                                    addSelectionPath(selPath);
                            }
                        }
                    }
                }
                finally
                {
                    updatingListSelectionModel = false;
                }
            }
        }

        /** Set to true when we are updating the ListSelectionModel. */
        protected boolean updatingListSelectionModel;


        /**
         * Class responsible for calling updateSelectedPathsFromSelectedRows
         * when the selection of the list changes
         */
        class ListSelectionHandler implements ListSelectionListener
        {
            /**
             * 
             * @param e
             */
            public void valueChanged(ListSelectionEvent e)
            {
                updateSelectedPathsFromSelectedRows();
            }
        }
    }

    protected TreeTableCellRenderer tree;
 }
