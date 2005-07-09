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
 * date         8 Feb 2005
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
     *      the principals for this entry
     * @param principalType
     *      the type of the principals
     * @param privileges
     *      the privileges for this entry
     * @param grant
     *      true if the privileges are granted, false if they are denied
     * @param inheritedHref
     *      if the entry is inherited, this parameter specifies from where
     *      it is inherited. If the entry is not inherited, this parameter is null.
     * @param modified
     *      indicates if the entry is considered modified
     */
    public ACLNode( String[] principal, int principalType, ACLPrivilege[] privileges, boolean grant, String inheritedHref, boolean modified )
    {
        init( principal, principalType, privileges, grant, inheritedHref, modified );
    }


    /**
     * Constructor
     * 
     * @param principal
     *      the principals for this entry
     * @param principalType
     *      the type of the principals
     * @param privileges
     *      the privileges for this entry
     * @param grant
     *      true if the privileges are granted, false if they are denied
     * @param inheritedHref
     *      if the entry is inherited, this parameter specifies from where
     *      it is inherited. If the entry is not inherited, this parameter is null.
     */
    public ACLNode( String[] principal, int principalType, ACLPrivilege[] privileges, boolean grant, String inheritedHref )
    {
        init( principal, principalType, privileges, grant, inheritedHref, false );
    }


    /**
     * Constructor
     * 
     * @param principal
     *      the principals for this entry
     * @param principalType
     *      the type of the principals
     * @param privileges
     *      the privileges for this entry
     * @param grant
     *      true if the privileges are granted, false if they are denied
     * @param modified
     *      indicates if the entry is considered modified
     */
    public ACLNode( String[] principal, int principalType, ACLPrivilege[] privileges, boolean grant, boolean modified )
    {
        init( principal, principalType, privileges, grant, null, modified );
    }


    /**
     * Constructor
     * 
     * @param principal
     *      the principals for this entry
     * @param principalType
     *      the type of the principals
     * @param privileges
     *      the privileges for this entry
     * @param grant
     *      true if the privileges are granted, false if they are denied
     */
    public ACLNode( String[] principal, int principalType, ACLPrivilege[] privileges, boolean grant )
    {
        init( principal, principalType, privileges, grant, null, false );
    }


    /**
     * Constructor
     * 
     * @param principal
     *      the principals for this entry
     * @param principalType
     *      the type of the principals
     * @param privileges
     *      the privileges for this entry
     * @param grant
     *      true if the privileges are granted, false if they are denied
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
     * Initializes the data.
     * 
     * @param principal
     *      the principals for this entry
     * @param principalType
     *      the type of the principals
     * @param privileges
     *      the privileges for this entry
     * @param grant
     *      true if the privileges are granted, false if they are denied
     * @param inheritedHref
     *      if the entry is inherited, this parameter specifies from where
     *      it is inherited. If the entry is not inherited, this parameter is null.
     * @param modified
     *      indicates if the entry is considered modified
     */
    protected void init( String[] _principal, int _principalType, ACLPrivilege[] _privileges, boolean _grant, String _inheritedHref, boolean _modified )
    {
        this.principal = new String[2];
        this.principal[0] = new String( _principal[0] );
        this.principal[1] = new String( _principal[1] );
        this.principalType = _principalType;
        setPrivileges( _privileges );
        this.grant = _grant;
        if( _inheritedHref != null && _inheritedHref.length()>0 )
        {
            this.inherited = true;
            this.inheritedHref = _inheritedHref;
        }
        else
            this.inherited = false;
        this.modified = _modified;
    }


    /**
     * Initializes the data.
     * 
     * @param principal
     *      the principals for this entry
     * @param principalType
     *      the type of the principals
     * @param privileges
     *      the privileges for this entry
     * @param grant
     *      true if the privileges are granted, false if they are denied
     * @param inheritedHref
     *      if the entry is inherited, this parameter specifies from where
     *      it is inherited. If the entry is not inherited, this parameter is null.
     * @param modified
     *      indicates if the entry is considered modified
     */
    protected void init( String[] _principal, int _principalType, Vector _privileges, boolean _grant, String _inheritedHref, boolean _modified )
    {
        this.principal = _principal;
        this.principalType = _principalType;
        setPrivileges( _privileges );
        this.grant = _grant;
        if( _inheritedHref != null && _inheritedHref.length()>0 )
        {
            this.inherited = true;
            this.inheritedHref = _inheritedHref;
        }
        else
            this.inherited = false;
        this.modified = _modified;
    }


    /**
     * Returns the principals.
     *  
     * @return
     *      an array of principal names
     */
    public String[] getPrincipal()
    {
        return principal;
    }


    /**
     * Set the principals.
     *  
     * @param principal
     *      an array of principal names
     */
    public void setPrincipal( String[] principal )
    {
        this.principal = principal;
        this.modified = true;
    }


    /**
     * Returns the principal type.
     *  
     * @return
     *      the principal type
     */
    public int getPrincipalType()
    {
        return principalType;
    }


    /**
     * Set the principal type.
     * 
     * @param principalType
     *      the new principal type
     */
    public void setPrincipalType( int principalType )
    {
        this.principalType = principalType;
    }


    /**
     * Returns the privileges
     *  
     * @return
     *      a vector of the privileges
     */
    public Vector getPrivileges()
    {
        return privileges;
    }


    /**
     * Set the privileges.
     * 
     * @param privileges
     *      the new array of privileges
     */
    public void setPrivileges( ACLPrivilege[] privileges )
    {
        this.privileges = new Vector();
        for( int i=0; i<privileges.length; i++ )
            this.privileges.add( privileges[i] );
        this.modified = true;
    }


    /**
     * Set the privileges.
     * 
     * @param privileges
     *      the new vector of privileges
     */
    public void setPrivileges( Vector privileges )
    {
        this.privileges = privileges;
        this.modified = true;
    }


    /**
     * Add a privilege to the existing set.
     *  
     * @param privilege
     *      the privilege to be added
     * @return
     *      true if the privilege was added, false if it already existed
     */
    public boolean addPrivilege( ACLPrivilege privilege )
    {
        for( int i=0; i<privileges.size(); i++)
        {
            if( ((ACLPrivilege)privileges.get(i)).getPrivilege().equals(privilege.getPrivilege()))
                return false;
        }
        privileges.add( privilege );
        this.modified = true;
        return true;
    }


    /**
     * Add privileges to the existing set.
     *  
     * @param privileges
     *      the privileges to be added
     */
    public void addPrivileges( Vector _privileges )
    {
        for( int i=0; i<_privileges.size(); i++ )
            addPrivilege( (ACLPrivilege)_privileges.get(i) );
    }


    /**
     * Add privileges to the existing set.
     *  
     * @param privileges
     *      the privileges to be added
     */
    public void addPrivileges( ACLPrivilege[] _privileges )
    {
        for( int i=0; i<_privileges.length; i++ )
            addPrivilege( (ACLPrivilege)_privileges[i] );
    }


    /**
     * Delete a privilege from the existing set.
     *  
     * @param privilege
     *      the privilege to be deleted
     * @return
     *      true if the privilege was deleted, false if it wasn't in the existing set
     */
    public boolean deletePrivilege( String privilege )
    {
        for( int i=0; i<privileges.size(); i++)
        {
            if( ((ACLPrivilege)privileges.get(i)).getPrivilege().equals(privilege))
            {
                privileges.removeElement( privilege );
                this.modified = true;
                return true;
            }
        }
        return false;
    }


    /**
     * Delete a privilege from the existing set.
     *  
     * @param privilege
     *      the privilege to be deleted
     * @return
     *      true if the privilege was deleted, false if it wasn't in the existing set
     */
    public boolean deletePrivilege( ACLPrivilege privilege )
    {
        for( int i=0; i<privileges.size(); i++)
        {
            if( ((ACLPrivilege)privileges.get(i)).getPrivilege().equals(privilege.getPrivilege()) )
            {
                privileges.removeElement( privilege );
                this.modified = true;
                return true;
            }
        }
        return false;
    }


    /**
     * Delete privileges from the existing set.
     *  
     * @param privileges
     *      the privileges to be deleted
     */
    public void deletePrivileges( Vector _privileges )
    {
        for( int i=0; i<_privileges.size(); i++ )
            deletePrivilege( (ACLPrivilege)_privileges.get(i) );
    }


    /**
     * Delete privileges from the existing set.
     *  
     * @param privileges
     *      the privileges to be deleted
     */
    public void deletePrivileges( ACLPrivilege[] _privileges )
    {
        for( int i=0; i<_privileges.length; i++ )
            deletePrivilege( (ACLPrivilege)_privileges[i] );
    }


    /**
     * Returns if the privileges are to be granted or denied.
     *  
     * @return
     *      true if the privileges are granted, false else
     */
    public boolean getGrant()
    {
        return grant;
    }


    /**
     * Grant or deny the privileges
     *  
     * @param grant
     *      true if the privileges are granted, false else
     */
    public void setGrant( boolean grant )
    {
        this.grant = grant;
    }


    /**
     * Returns if the entry is inherited.
     *  
     * @return
     *      true if the ACL entry is inherited, false else
     */
    public boolean isInherited()
    {
        return inherited;
    }


    /**
     * If the ACL entry is inherited, return the originating URI.
     *  
     * @return
     *      the originating URI if the entry is inherited, null else
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
     * Check if the entry is modified.
     *  
     * @return
     *      true if the entry is modified, false else
     */
    public boolean isModified()
    {
        return modified;
    }


    /**
     * Reset the entry to not modified. 
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
