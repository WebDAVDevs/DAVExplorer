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
 * Title:       
 * Description: 
 * Copyright:   Copyright (c) 2005 Regents of the University of California. All rights reserved.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        
 */
public class ACLModel extends AbstractTableModel
{
    /**
     * Constructor 
     * 
     * @param properties
     */
    public ACLModel( Element properties )
    {
        parseProperties( properties );
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


    /**
     * 
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount()
    {
        // hardcoded
        return 4;
    }

    /**
     * 
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount()
    {
        return rows.size();
    }

    /**
     * 
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt( int rowIndex, int columnIndex )
    {
        ACLNode node = (ACLNode)rows.get(rowIndex);
        switch( columnIndex )
        {
            case 0:
                return node.getPrincipal();
            case 1:
                Vector privs = node.getPrivileges();
                String retval = "";
                if( privs != null )
                {
                    for( int i=0; i<privs.size(); i++ )
                    {
                        if( i > 0 )
                            retval += ", ";
                        retval += privs.get(i);
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
     *
     */
    public void clear()
    {
        for( int i=0; i< rows.size(); i++ )
            ((ACLNode)rows.get(i)).clearModified();
    }


    /**
     * 
     * @param principal
     * @param principalType
     * @param privileges
     * @param grant
     */
    public void addRow( String principal, int principalType, Vector privileges, boolean grant )
    {
        int size = rows.size();
        ACLNode node = new ACLNode( principal, principalType, privileges, grant );
        rows.add( node );
        fireTableRowsInserted( size, size );
    }


    public ACLNode getRow( int index )
    {
        if( index < rows.size() )
        {
            return (ACLNode)rows.get( index );
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


    /**
     * 
     * @param properties
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
     * 
     * @param ace
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
     * 
     * @param principal
     * @param node
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
                        node.setPrincipal( GlobalData.getGlobalData().unescape( token.getText(), "UTF-8", null ) );
                        node.setPrincipalType( ACLNode.HREF );
                        break;
                    }
                }
                else if( currentTag.getName().equals( ACLXML.ELEM_PROPERTY ) )
                {
                    Element token = (Element)enumTree.nextElement();
                    while( token.getTagName() == null )
                        token = (Element)enumTree.nextElement();
                    node.setPrincipal( token.getTagName().getName() );
                    node.setPrincipalType( ACLNode.PROPERTY );
                    break;
                }
                else
                {
                    //Element token = (Element)enumTree.nextElement();
                    node.setPrincipal( currentTag.getName() );
                    node.setPrincipalType( ACLNode.GENERAL );
                    break;
                }
            }
        }
    }


    /**
     * 
     * @param privileges
     * @param node
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
                    node.addPrivilege( token.getTagName().getName() );
                }
            }
        }
        
    }


    /**
     * 
     * @param inherited
     * @param node
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
