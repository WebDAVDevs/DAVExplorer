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

import javax.swing.table.AbstractTableModel;

import java.util.Vector;
import com.ms.xml.om.Element;
import com.ms.xml.om.TreeEnumeration;
import com.ms.xml.util.Name;


/**
 * Title:       ACL Model
 * Description: The model for displaying the ACLs.
 * Copyright:   Copyright (c) 2005 Regents of the University of California. All rights reserved.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         8 Feb 2005
 */
public class ACLModel extends AbstractTableModel
{
    /**
     * Constructor 
     * 
     * @param properties
     *      the properties specifying the ACL list
     */
    public ACLModel( Element properties )
    {
        parseProperties( properties );
    }


    /**
     *  Returns the name of a column.
     *
     * @param column
     *      the column being queried
     * @return
     *      a string containing the name of the column 
     */
    public String getColumnName( int column )
    {
        if( column < names.length )
            return names[column];
        return super.getColumnName( column );
    }


    /**
     *  Returns the class of the values shown in a column.
     *
     *  @param column
     *      the column being queried
     *  @return
     *      the Object.class
     */
    public Class getColumnClass( int column )
    {
        if( column < names.length )
            return types[column];
        return super.getColumnClass( column );
    }


    /**
     * Returns the number of columns.
     * 
     * @return
     *      the number of columns
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount()
    {
        // hardcoded
        return 4;
    }

    /**
     * Returns the number of rows.
     * 
     * @return
     *      the number of rows 
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount()
    {
        return rows.size();
    }

    /**
     * Returns the value at a specific cell in the table.
     * 
     * @param rowIndex
     *      the row position
     * @param columnIndex
     *      the column position
     * @return
     *      the value in the cell specified by the parameters 
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt( int rowIndex, int columnIndex )
    {
        ACLNode node = (ACLNode)rows.get(rowIndex);
        switch( columnIndex )
        {
            case 0:
                return node.getPrincipal()[0];  // use the href value
            case 1:
                Vector privs = node.getPrivileges();
                String retval = "";
                if( privs != null )
                {
                    for( int i=0; i<privs.size(); i++ )
                    {
                        if( i > 0 )
                            retval += ", ";
                        retval += ((ACLPrivilege)privs.get(i)).getPrivilege();
                    }
                }
                return retval;
            case 2:
                if( node.getGrant() )
                    return "Grant";
                else
                    return "Deny";
            case 3:
                if( node.isInherited() )
                    return node.getInherited();
                else
                    return "";
            default:
                return null;
        }
    }


    /**
     * Reset all ACL entries to non-modified.
     */
    public void clear()
    {
        for( int i=0; i< rows.size(); i++ )
            ((ACLNode)rows.get(i)).clearModified();
    }


    /**
     * Add a new ACL entry.
     * 
     * @param principal
     *      the principals this entry applies to
     * @param principalType
     *      the type of the principal values
     * @param privileges
     *      the privileges this entry specifies
     * @param grant
     *      true if the privileges are granted, false if they are denied
     */
    public void addRow( String[] principal, int principalType, Vector privileges, boolean grant )
    {
        int size = rows.size();
        ACLNode node = new ACLNode( principal, principalType, privileges, grant );
        rows.add( node );
        fireTableRowsInserted( size, size );
    }


    /**
     * Returns a datanode for a specific row in the list.
     * 
     * @param index
     *      the row index
     * @return
     *      the datanode for the row
     */
    public ACLNode getRow( int index )
    {
        if( index < rows.size() )
        {
            return (ACLNode)rows.get( index );
        }
        return null;
    }


    /**
     * Removes a specific row from the data model
     * 
     * @param index
     *      the row to be removed
     */
    public void removeRow( int index )
    {
        if( index < rows.size() )
        {
            rows.remove( index );
            fireTableRowsDeleted( index, index );
        }
    }


    /**
     * Parses the properties as received from the server, building
     * up the data model.
     * 
     * @param properties
     *      the properties received from the server
     */
    private void parseProperties( Element properties )
    {
        if( GlobalData.getGlobalData().getDebugResponse() )
        {
            System.err.println( "ACLModel::parseProperties" );
        }

        if( properties != null )
        {
            TreeEnumeration enumTree =  new TreeEnumeration( properties );
            while( enumTree.hasMoreElements() )
            {
                Element current = (Element)enumTree.nextElement();
                Name currentTag = current.getTagName();
                if( currentTag != null )
                {
                    if( currentTag.getName().equals( ACLXML.ELEM_ACE ) )
                    {
                        parseACE( current );
                    }
                }
            }
        }
    }


    /**
     * Parse one entry of the ACLs
     *  
     * @param ace
     *      the root property defining the element
     */
    private void parseACE( Element ace )
    {
        ACLNode node = new ACLNode();
        TreeEnumeration enumTree =  new TreeEnumeration( ace );
        while( enumTree.hasMoreElements() )
        {
            Element current = (Element)enumTree.nextElement();
            Name currentTag = current.getTagName();
            if( currentTag != null )
            {
                if( currentTag.getName().equals( ACLXML.ELEM_PRINCIPAL ) )
                {
                    parsePrincipal( current, node );
                }
                else if( currentTag.getName().equals( ACLXML.ELEM_GRANT ) )
                {
                    node.setGrant( true );
                    parsePrivileges( current, node );
                }
                else if( currentTag.getName().equals( ACLXML.ELEM_DENY ) )
                {
                    node.setGrant( false );
                    parsePrivileges( current, node );
                }
                else if( currentTag.getName().equals( ACLXML.ELEM_INHERITED ) )
                {
                    parseInherited( current, node );
                }
            }
        }
        node.clearModified();
        rows.add( node );
    }


    /**
     * Parse the principal information in the properties.
     *  
     * @param principal
     *      the root element of the principal property
     * @param node
     *      the data node where the information is stored
     */
    private void parsePrincipal( Element principal, ACLNode node )
    {
        TreeEnumeration enumTree =  new TreeEnumeration( principal );
        while( enumTree.hasMoreElements() )
        {
            Element current = (Element)enumTree.nextElement();
            Name currentTag = current.getTagName();
            if( currentTag != null )
            {
                if( currentTag.getName().equals( ACLXML.ELEM_PRINCIPAL ) )
                    continue;
                if( currentTag.getName().equals( ACLXML.ELEM_HREF ) )
                {
                    Element token = (Element)enumTree.nextElement();
                    if( (token != null) && (token.getType() == Element.PCDATA || token.getType() == Element.CDATA) )
                    {
                        String[] p = new String[2];
                        p[0] = p[1] = GlobalData.getGlobalData().unescape( token.getText(), "UTF-8", null );
                        node.setPrincipal( p );
                        node.setPrincipalType( ACLNode.HREF );
                        break;
                    }
                }
                else if( currentTag.getName().equals( ACLXML.ELEM_PROPERTY ) )
                {
                    Element token = (Element)enumTree.nextElement();
                    while( token.getTagName() == null )
                        token = (Element)enumTree.nextElement();
                    String[] p = new String[2];
                    p[0] = "property";
                    p[1] = token.getTagName().getName();
                    node.setPrincipal( p );
                    node.setPrincipalType( ACLNode.PROPERTY );
                    break;
                }
                else
                {
                    //Element token = (Element)enumTree.nextElement();
                    String[] p = new String[2];
                    p[0] = p[1] = currentTag.getName();
                    node.setPrincipal( p );
                    node.setPrincipalType( ACLNode.GENERAL );
                    break;
                }
            }
        }
    }


    /**
     * Parse the privilege information in the properties.
     *  
     * @param privileges
     *      the root element of the privilege properties
     * @param node
     *      the data node where the information is stored
     */
    private void parsePrivileges( Element privileges, ACLNode node )
    {
        TreeEnumeration enumTree =  new TreeEnumeration( privileges );
        while( enumTree.hasMoreElements() )
        {
            Element current = (Element)enumTree.nextElement();
            Name currentTag = current.getTagName();
            if( currentTag != null )
            {
                if( currentTag.getName().equals( ACLXML.ELEM_GRANT ) )
                    continue;
                if( currentTag.getName().equals( ACLXML.ELEM_DENY ) )
                    continue;
                if( currentTag.getName().equals( ACLXML.ELEM_PRIVILEGE ) )
                {
                    Element token = (Element)enumTree.nextElement();
                    while( token.getTagName() == null )
                        token = (Element)enumTree.nextElement();
                    ACLPrivilege privilege = new ACLPrivilege();
                    privilege.setPrivilege(token.getTagName().getName());
                    String ns = WebDAVProp.locateNamespace(token, token.getTagName());
                    privilege.setNamespace(ns);
                    node.addPrivilege( privilege );
                }
            }
        }
        
    }


    /**
     * Parse the inherited information in the properties.
     * 
     * @param inherited
     *      the root element of the privilege properties
     * @param node
     *      the data node where the information is stored
     */
    private void parseInherited( Element inherited, ACLNode node )
    {
        TreeEnumeration enumTree =  new TreeEnumeration( inherited );
        while( enumTree.hasMoreElements() )
        {
            Element current = (Element)enumTree.nextElement();
            Name currentTag = current.getTagName();
            if( currentTag != null )
            {
                if( currentTag.getName().equals( ACLXML.ELEM_INHERITED ) )
                    continue;
                if( currentTag.getName().equals( ACLXML.ELEM_HREF ) )
                {
                    Element token = (Element)enumTree.nextElement();
                    if( (token != null) && (token.getType() == Element.PCDATA || token.getType() == Element.CDATA) )
                    {
                        node.setInherited( GlobalData.getGlobalData().unescape( token.getText(), "UTF-8", null ) );
                        break;
                    }
                }
            }
        }
    }


    // column names
    protected String[] names = { "Principals", "Privileges", "Grant/Deny", "Inherited From" };
    // column types
    protected Class[] types = { String.class, String.class, String.class, String.class };

    protected Vector rows = new Vector();
}
