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


/**
 * Title:       ACL Node
 * Description: Describes one access control entry
 * Copyright:   Copyright (c) 2005 Regents of the University of California. All rights reserved.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        8 Feb 2005
 */
public class ACLNode
{
    /**
     * Principal types
     */
    public final static int GENERAL = 1;
    public final static int PROPERTY = 2;
    public final static int HREF = 3;


    /**
     * Constructor
     * 
     * @param principal
     * @param principalType
     * @param privileges
     * @param grant
     * @param inheritedHref
     * @param modified
     */
    public ACLNode( String[] principal, int principalType, String[] privileges, boolean grant, String inheritedHref, boolean modified )
    {
        init( principal, principalType, privileges, grant, inheritedHref, modified );
    }


    /**
     * Constructor
     * 
     * @param principal
     * @param principalType
     * @param privileges
     * @param grant
     * @param inheritedHref
     */
    public ACLNode( String[] principal, int principalType, String[] privileges, boolean grant, String inheritedHref )
    {
        init( principal, principalType, privileges, grant, inheritedHref, false );
    }


    /**
     * Constructor
     * 
     * @param principal
     * @param principalType
     * @param privileges
     * @param grant
     * @param modified
     */
    public ACLNode( String[] principal, int principalType, String[] privileges, boolean grant, boolean modified )
    {
        init( principal, principalType, privileges, grant, null, modified );
    }


    /**
     * Constructor
     * 
     * @param principal
     * @param principalType
     * @param privileges
     * @param grant
     */
    public ACLNode( String[] principal, int principalType, String[] privileges, boolean grant )
    {
        init( principal, principalType, privileges, grant, null, false );
    }


    /**
     * Constructor
     * 
     * @param principal
     * @param principalType
     * @param privileges
     * @param grant
     */
    public ACLNode( String[] principal, int principalType, Vector privileges, boolean grant )
    {
        init( principal, principalType, privileges, grant, null, false );
    }


    /**
     * Constructor
     */
    public ACLNode()
    {
        this.privileges = new Vector();
        this.modified = false;
    }


    /**
     * 
     * @param principal
     * @param principalType
     * @param privileges
     * @param grant
     * @param inheritedHref
     * @param modified
     */
    protected void init( String[] principal, int principalType, String[] privileges, boolean grant, String inheritedHref, boolean modified )
    {
        this.principal = new String[2];
        this.principal[0] = new String( principal[0] );
        this.principal[1] = new String( principal[1] );
        this.principalType = principalType;
        setPrivileges( privileges );
        this.grant = grant;
        if( inheritedHref != null && inheritedHref.length()>0 )
        {
            this.inherited = true;
            this.inheritedHref = inheritedHref;
        }
        else
            this.inherited = false;
        this.modified = modified;
    }


    /**
     * 
     * @param principal
     * @param principalType
     * @param privileges
     * @param grant
     * @param inheritedHref
     * @param modified
     */
    protected void init( String[] principal, int principalType, Vector privileges, boolean grant, String inheritedHref, boolean modified )
    {
        this.principal = principal;
        this.principalType = principalType;
        setPrivileges( privileges );
        this.grant = grant;
        if( inheritedHref != null && inheritedHref.length()>0 )
        {
            this.inherited = true;
            this.inheritedHref = inheritedHref;
        }
        else
            this.inherited = false;
        this.modified = modified;
    }


    /**
     * 
     * @return
     */
    public String[] getPrincipal()
    {
        return principal;
    }


    /**
     * 
     * @param principal
     */
    public void setPrincipal( String[] principal )
    {
        this.principal = principal;
        this.modified = true;
    }


    /**
     * 
     * @return
     */
    public int getPrincipalType()
    {
        return principalType;
    }


    /**
     * 
     * @param principalType
     */
    public void setPrincipalType( int principalType )
    {
        this.principalType = principalType;
    }


    /**
     * 
     * @return
     */
    public Vector getPrivileges()
    {
        return privileges;
    }


    /**
     * 
     * @param privileges
     */
    public void setPrivileges( String[] privileges )
    {
        this.privileges = new Vector();
        for( int i=0; i<privileges.length; i++ )
            this.privileges.add( privileges[i] );
        this.modified = true;
    }


    /**
     * 
     * @param privileges
     */
    public void setPrivileges( Vector privileges )
    {
        this.privileges = privileges;
        this.modified = true;
    }


    /**
     * 
     * @param privilege
     * @return
     */
    public boolean addPrivilege( String privilege )
    {
        if( privileges.contains( privilege ) )
            return false;
        privileges.add( privilege );
        this.modified = true;
        return true;
    }


    /**
     * 
     * @param privileges
     */
    public void addPrivileges( Vector privileges )
    {
        for( int i=0; i<privileges.size(); i++ )
            addPrivilege( (String)privileges.get(i) );
    }


    /**
     * 
     * @param privileges
     */
    public void addPrivileges( String[] privileges )
    {
        for( int i=0; i<privileges.length; i++ )
            addPrivilege( (String)privileges[i] );
    }


    /**
     * 
     * @param privilege
     * @return
     */
    public boolean deletePrivilege( String privilege )
    {
        if( privileges.contains( privilege ) )
        {
            privileges.removeElement( privilege );
            this.modified = true;
            return true;
        }
        return false;
    }


    /**
     * 
     * @param privileges
     */
    public void deletePrivileges( Vector privileges )
    {
        for( int i=0; i<privileges.size(); i++ )
            deletePrivilege( (String)privileges.get(i) );
    }


    /**
     * 
     * @param privileges
     */
    public void deletePrivileges( String[] privileges )
    {
        for( int i=0; i<privileges.length; i++ )
            deletePrivilege( (String)privileges[i] );
    }


    /**
     * 
     * @return
     */
    public boolean getGrant()
    {
        return grant;
    }


    /**
     * 
     * @param grant
     */
    public void setGrant( boolean grant )
    {
        this.grant = grant;
    }


    /**
     * 
     * @return
     */
    public boolean isInherited()
    {
        return inherited;
    }


    /**
     * 
     * @return
     */
    public String getInherited()
    {
        if( isInherited() )
            return inheritedHref;
        return null;
    }


    /**
     * 
     * @param inheritedHref
     */
    public void setInherited( String inheritedHref )
    {
        this.inherited = true;
        this.inheritedHref = inheritedHref;
    }


    /**
     * 
     * @return
     */
    public boolean isModified()
    {
        return modified;
    }


    /**
     * 
     *
     */
    public void clearModified()
    {
        modified = false;
    }


    private String[] principal;
    private int principalType;
    private Vector privileges;
    private boolean grant;
    private boolean inherited;
    private String inheritedHref;
    private boolean modified;
}
