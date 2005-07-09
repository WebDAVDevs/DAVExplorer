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

/**
 * Title:       ACL Properties
 * Description: Simple list of all ACL properties, based on RFC 3744
 * Copyright:   Copyright (c) 2005 Regents of the University of California. All rights reserved.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 */

package edu.uci.ics.DAVExplorer;

import java.util.Enumeration;
import java.util.Vector;


/**
 * 
 * Simple list of ACL properties, based on RFC 3744.
 * Updated as necessary.
 * 
 * @see <a href="http://www.ietf.org/rfc/rfc3253.txt">RFC 3253</a>
 */
public class ACLProp extends DeltaVProp
{
    // Principal Properties (section 4)
    public static final String PROP_PRINCIPAL = "principal";
    public static final String PROP_ALTERNATE_URI_SET = "alternate-URI-set";
    public static final String PROP_PRINCIPAL_URL = "principal-URL";
    public static final String PROP_GROUP_MEMBER_SET = "group-member-set";
    public static final String PROP_GROUP_MEMBERSHIP = "group-membership";
    
    // Access Control Properties (section 5)
    public static final String PROP_OWNER = "owner";
    public static final String PROP_GROUP = "group";
    public static final String PROP_SUPPORTED_PRIVILEGE_SET = "supported-privilege-set";
    public static final String PROP_SUPPORTED_PRIVILEGE = "supported-privilege";
    public static final String PROP_PRIVILEGE = "privilege";
    public static final String PROP_ABSTRACT = "abstract";
    public static final String PROP_DESCRIPTION = "description";
    public static final String PROP_CURRENT_USER_PRIVILEGE_SET = "current-user-privilege-set";
    public static final String PROP_ACL = "acl";
    public static final String PROP_ACE = "ace";
    public static final String PROP_ALL = "all";
    public static final String PROP_AUTHENTICATED = "authenticated";
    public static final String PROP_UNAUTHENTICATED = "unauthenticated";
    public static final String PROP_PROPERTY = "property";
    public static final String PROP_SELF = "self";
    public static final String PROP_INVERT = "invert";
    public static final String PROP_GRANT = "grant";
    public static final String PROP_DENY = "deny";
    public static final String PROP_PROTECTED = "protected";
    public static final String PROP_INHERITED = "inherited";
    public static final String PROP_ACL_RESTRICTIONS = "acl-restrictions";
    public static final String PROP_GRANT_ONLY = "grant-only";
    public static final String PROP_NO_INVERT = "no-invert";
    public static final String PROP_DENY_BEFORE_GRANT = "deny-before-grant";
    public static final String PROP_REQUIRED_PRINCIPAL = "required-principal";
    public static final String PROP_INHERITED_ACL_SET = "inherited-acl-set";
    public static final String PROP_PRINCIPAL_COLLECTION_SET = "principal-collection-set";


    /**
     * Constructor
     */
    public ACLProp()
    {
        super();
    }


    /**
     * Get an enumeration of all ACL properties and the properties defined in the super classes
     *  
     * @return
     *      an emumeration of all ACL and superclass properties
     */
    public static Enumeration getDAVProps()
    {
        Vector prop_list = new Vector();

        // add the elements from the super class
        Enumeration propEnum = DeltaVProp.getDAVProps();
        while( propEnum.hasMoreElements() )
            prop_list.addElement( propEnum.nextElement() );

        prop_list.addElement( PROP_PRINCIPAL );
        prop_list.addElement( PROP_ALTERNATE_URI_SET );
        prop_list.addElement( PROP_PRINCIPAL_URL );
        prop_list.addElement( PROP_GROUP_MEMBER_SET );
        prop_list.addElement( PROP_GROUP_MEMBERSHIP );
        prop_list.addElement( PROP_OWNER );
        prop_list.addElement( PROP_GROUP );
        prop_list.addElement( PROP_SUPPORTED_PRIVILEGE_SET );
        prop_list.addElement( PROP_SUPPORTED_PRIVILEGE );
        prop_list.addElement( PROP_PRIVILEGE );
        prop_list.addElement( PROP_ABSTRACT );
        prop_list.addElement( PROP_DESCRIPTION );
        prop_list.addElement( PROP_CURRENT_USER_PRIVILEGE_SET );
        prop_list.addElement( PROP_ACL );
        prop_list.addElement( PROP_ACE );
        prop_list.addElement( PROP_ALL );
        prop_list.addElement( PROP_AUTHENTICATED );
        prop_list.addElement( PROP_UNAUTHENTICATED );
        prop_list.addElement( PROP_PROPERTY );
        prop_list.addElement( PROP_SELF );
        prop_list.addElement( PROP_INVERT );
        prop_list.addElement( PROP_GRANT );
        prop_list.addElement( PROP_DENY );
        prop_list.addElement( PROP_PROTECTED );
        prop_list.addElement( PROP_INHERITED );
        prop_list.addElement( PROP_ACL_RESTRICTIONS );
        prop_list.addElement( PROP_GRANT_ONLY );
        prop_list.addElement( PROP_NO_INVERT );
        prop_list.addElement( PROP_DENY_BEFORE_GRANT );
        prop_list.addElement( PROP_REQUIRED_PRINCIPAL );
        prop_list.addElement( PROP_INHERITED_ACL_SET );
        prop_list.addElement( PROP_PRINCIPAL_COLLECTION_SET );

        return (prop_list.elements());
    }


    /**
     * Get an enumeration of all protected live ACL, DeltaV and WebDAV properties.
     *  
     * @return
     *      an emumeration of all protected live properties
     */
    public static Enumeration getProtectedDAVProps()
    {
        Vector prop_list = new Vector();

        // add the elements from the super class
        Enumeration propEnum = DeltaVProp.getProtectedDAVProps();
        while( propEnum.hasMoreElements() )
            prop_list.addElement( propEnum.nextElement() );

        prop_list.addElement( PROP_PRINCIPAL );
        prop_list.addElement( PROP_ALTERNATE_URI_SET );
        prop_list.addElement( PROP_GROUP_MEMBERSHIP );
        prop_list.addElement( PROP_SUPPORTED_PRIVILEGE_SET );
        prop_list.addElement( PROP_SUPPORTED_PRIVILEGE );
        prop_list.addElement( PROP_CURRENT_USER_PRIVILEGE_SET );
        prop_list.addElement( PROP_ACL );
        prop_list.addElement( PROP_ACL_RESTRICTIONS );
        prop_list.addElement( PROP_INHERITED_ACL_SET );
        prop_list.addElement( PROP_PRINCIPAL_COLLECTION_SET );

        return (prop_list.elements());
    }
}
