/*
 * Copyright (C) 2005 Regents of the University of California.
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
package edu.uci.ics.DAVExplorer;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

/**
 * Title:       Property search model
 * Description: Datamodel to use in the ACL property dialog
 * Copyright:   Copyright (c) 2005 Regents of the University of California. All rights reserved.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        11 Feb 2005
 */
public class ACLPropertySearchModel extends AbstractTableModel
{
    /**
     * Constructor 
     */
    public ACLPropertySearchModel()
    {
        super();
        this.match = true;
    }


    /**
     * Constructor 
     */
    public ACLPropertySearchModel( boolean match )
    {
        super();
        this.match = match;
    }


    /**
     * 
     */
    public String getColumnName( int column )
    {
        if( column < names.length )
            return names[column];
        return super.getColumnName( column );
    }


    /**
     * 
     */
    public Class getColumnClass( int column )
    {
        if( column < names.length )
            return types[column];
        return super.getColumnClass( column );
    }


    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount()
    {
        return match ? 2 : 1;
    }


    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount()
    {
        // always show at least one row
        return (rows.size()>1) ? rows.size() : 1;
    }


    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt( int rowIndex, int columnIndex )
    {
        if( rowIndex >= rows.size() )
            return null;
        ACLPropertySearchNode node = (ACLPropertySearchNode)rows.get( rowIndex );
        switch( columnIndex )
        {
            case 0:
                Vector props = node.getProperties();
                String retval = "";
                if( props != null )
                {
                    for( int i=0; i<props.size(); i++ )
                    {
                        if( i > 0 )
                            retval += ", ";
                        String [] n = (String[])props.get(i);
                        retval += n[0];     // property name
                    }
                }
                return retval;
            case 1:
                return node.getMatch();
            default:
                return null;
        }
    }


    /**
     * 
     * @return
     */
    public int getRealRowCount()
    {
        return rows.size();
    }


    /**
     * 
     * @param properties
     * @param match
     */
    public void addRow( Vector properties, String match )
    {
        int size = rows.size();
        ACLPropertySearchNode node = new ACLPropertySearchNode( properties, match );
        rows.add( node );
        fireTableRowsInserted( size, size );
    }


    /**
     * 
     * @param index
     * @return
     */
    public ACLPropertySearchNode getRow( int index )
    {
        if( index < rows.size() )
        {
            return (ACLPropertySearchNode)rows.get( index );
        }
        return null;
    }


    /**
     * 
     * @param index
     */
    public void removeRow( int index )
    {
        if( index < rows.size() )
        {
            rows.remove( index );
            fireTableRowsDeleted( index, index );
        }
    }


    // column names
    protected String[] names = { "Properties", "Match" };
    // column types
    protected Class[] types = { String.class, String.class };

    protected Vector rows = new Vector();
    protected boolean match;
}
