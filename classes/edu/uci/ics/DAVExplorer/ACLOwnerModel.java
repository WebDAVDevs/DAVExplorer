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

/**
 * Title:       ACL Owner Model
 * Description: Models the ACL Owner and Group properties
 * Copyright:   Copyright (c) 2005 Regents of the University of California. All rights reserved.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        18 January 2005
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

import javax.swing.tree.TreePath;
import com.ms.xml.om.Element;


/**
 * 
 */
public class ACLOwnerModel extends PropModel
{
    /**
     * Constructor
     * @param properties
     */
    public ACLOwnerModel( Element properties )
    {
        super( properties );
        // column names
        names = new String[] { "Tag", "Value" };

        // column types
        types = new Class[] { TreeTableModel.class, String.class };
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
                return true;
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
        return false;
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
}
