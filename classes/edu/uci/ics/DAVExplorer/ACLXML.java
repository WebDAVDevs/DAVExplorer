/*
 * Copyright (C) 2004-2005 Regents of the University of California.
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
 * Title:       ACLXML
 * Description: Defines the needed Generic ACL XML Elements from RFC 3744
 *              Updated as necessary.
 * Copyright:   Copyright (c) 2004-2005 Regents of the University of California. All rights reserved.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        14 Feb 2005
 */

package edu.uci.ics.DAVExplorer;

/**
 * This class defines the needed Generic ACL XML Elements from RFC 3744.
 * Updated as necessary.
 * 
 * @see <a href="http://www.ietf.org/rfc/rfc3744.txt">RFC 3744</a>
 */
public class ACLXML extends DeltaVXML
{
    public static final String ELEM_ACL = "acl";
    public static final String ELEM_ACE = "ace";
    public static final String ELEM_PRINCIPAL = "principal";
    public static final String ELEM_PRINCIPAL_COLLECTION_SET = "principal-collection-set";
    public static final String ELEM_GRANT = "grant";
    public static final String ELEM_DENY = "deny";
    public static final String ELEM_PRIVILEGE = "privilege";
    public static final String ELEM_SUPPORTED_PRIVILEGE_SET = "supported-privilege-set";
    public static final String ELEM_INHERITED = "inherited";
    public static final String ELEM_PROPERTY = "property";
    public static final String ELEM_RESPONSEDESCRIPTION = "responsedescription";
    public static final String ELEM_ACL_PRINCIPAL_PROP_SET = "acl-principal-prop-set";
    public static final String ELEM_PRINCIPAL_MATCH = "principal-match";
    public static final String ELEM_PRINCIPAL_PROPERTY = "principal-property";
    public static final String ELEM_PRINCIPAL_PROPERTY_SEARCH = "principal-property-search";
    public static final String ELEM_PROPERTY_SEARCH = "property-search";
    public static final String ELEM_MATCH = "match";
    public static final String ELEM_SELF = "self";
    public static final String ELEM_PRINCIPAL_SEARCH_PROPERTY = "principal-search-property";
    public static final String ELEM_PRINCIPAL_SEARCH_PROPERTY_SET = "principal-search-property-set";
}
