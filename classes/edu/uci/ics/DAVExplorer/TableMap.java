/*
 * Copyright (c) 1998-2004 Regents of the University of California.
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
 * Title:       Table Map
 * Description: Table model for main viewer
 * Copyright:   Copyright (c) 1998-2004 Regents of the University of California. All rights reserved.
 * @author      Undergraduate project team ICS 126B 1998
 * @date        1998
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        1 October 2001
 * Changes:     Change of package name
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        08 February 2004
 * Changes:     Added Javadoc templates
 */

package edu.uci.ics.DAVExplorer;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;


/**
 * 
 */
public class TableMap extends AbstractTableModel implements TableModelListener
{
    protected TableModel model;


    /**
     * @return
     */
    public TableModel getModel()
    {
        return model;
    }


    /**
     * 
     * @param model
     */
    public void  setModel(TableModel model)
    {
        this.model = model;
        model.addTableModelListener(this);
    }


    /**
     * By default, Implement TableModel by forwarding all messages
     * to the model.
     * @param aRow
     * @param aColumn
     * 
     * @return
     */
    public Object getValueAt(int aRow, int aColumn)
    {
        return model.getValueAt(aRow, aColumn);
    }

    /**
     * 
     * @param aValue
     * @param aRow
     * @param aColumn
     */
    public void setValueAt(Object aValue, int aRow, int aColumn)
    {
        model.setValueAt(aValue, aRow, aColumn);
    }


    /**
     *
     * @return 
     */
    public int getRowCount()
    {
        return (model == null) ? 0 : model.getRowCount();
    }


    /**
     *
     * @return 
     */
    public int getColumnCount()
    {
        return (model == null) ? 0 : model.getColumnCount();
    }


    /**
     * @param aColumn
     *
     * @return 
     */
    public String getColumnName(int aColumn)
    {
        return model.getColumnName(aColumn);
    }


    /**
     * @param aColumn
     *
     * @return 
     */
    public Class getColumnClass(int aColumn)
    {
        return model.getColumnClass(aColumn);
    }


    /**
     * @param row
     * @param column
     *
     * @return 
     */
    public boolean isCellEditable(int row, int column)
    {
         return model.isCellEditable(row, column);
    }


    /**
     * Implementation of the TableModelListener interface.
     * By default forward all events to all the listeners.
     * @return 
     */
    public void tableChanged(TableModelEvent e)
    {
        fireTableChanged(e);
    }
}
