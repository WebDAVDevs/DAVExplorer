/*
 * Copyright (c) 2005 Regents of the University of California.
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

import com.ms.xml.om.Element;


/**
 * Title:       ACL Property Model
 * Description: Models the ACL properties for owner, group, privileges
 * Copyright:   Copyright (c) 2005 Regents of the University of California. All rights reserved.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         4 Feb 2005
 */
public class ACLPropModel extends PropModel
{
    /**
     * Constructor
     * 
     * @param properties
     *      the root of the properties tree
     */
    public ACLPropModel( Element properties )
    {
        super( properties );
        // column names
        names = new String[] { "Tag", "Value" };

        // column types
        types = new Class[] { TreeTableModel.class, String.class };
    }


    /**
     * From the TreeTableModel interface
     * Returns the value of a specific cell.
     * 
     * @param node
     *      the tree node that defines the row
     * @param column
     *      the desired column
     * @return
     *      the value for the desired column
     */
    public Object getValueAt( Object node, int column )
    {
        try
        {
            switch( column )
            {
                case 0:
                    return ((PropNode)node).getTag();
                case 1:
                    return ((PropNode)node).getValue();
            }
        }
        catch( SecurityException se )
        {
        }
        return null;
    }


    /**
     * From the TreeTableModel interface
     * Checks if the specified cell is editable.
     * 
     * @param node
     *      the tree node that defines the row
     * @param column
     *      the desired column
     * @return
     *      true if the cell is editable, false else
     */
    public boolean isCellEditable( Object node, int column )
    {
        try
        {
            switch( column )
            {
                case 0:
                    break;
                case 1:
                    return true;
            }
        }
        catch( SecurityException se )
        {
        }
        return false;
    }


    /**
     * From the TreeTableModel interface
     * Checks if a node (i.e., row) can be removed.
     *  
     * @param node
     *      the node to be checked
     * @return
     *      always false
     */
    public boolean isNodeRemovable( Object node )
    {
        return false;
    }


    /**
     * From the TreeTableModel interface
     * Set the value of a cell
     * 
     * @param aValue
     *      the new value
     * @param node
     *      the tree node that defines the row
     * @param column
     *      the desired column
     */
    public void setValueAt( Object aValue, Object node, int column )
    {
        String oldValue;
        try
        {
            switch(column)
            {
                case 0:
                    break;
                case 1:
                    oldValue = ((PropNode)node).getValue();
                    if( !oldValue.equals((String)aValue) )
                    {
                        ((PropNode)node).setValue((String)aValue);
                        fireModelChanged(node);
                    }
                    break;
            }
        }
        catch( SecurityException se )
        {
        }
    }
}
