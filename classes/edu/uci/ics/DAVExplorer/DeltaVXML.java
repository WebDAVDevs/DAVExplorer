/*
 * Copyright (c) 2004 Regents of the University of California.
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
 * Title:       DeltaVXML
 * Description: Defines the needed Generic DeltaV XML Elements from RFC 3253
 *              Updated as necessary.
 * Copyright:   Copyright (c) 2004 Regents of the University of California. All rights reserved.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        07 February 2004
 */

package edu.uci.ics.DAVExplorer;


/**
 * This class defines the needed Generic DeltaV XML Elements from RFC 3253.
 * Updated as necessary.
 * 
 * @see <a href="http://www.ietf.org/rfc/rfc3253.txt">RFC 3253</a>
 */
public class DeltaVXML extends WebDAVXML
{
    public static final String ELEM_OPTIONS = "options";
    public static final String ELEM_CHECKOUT = "checkout";
    public static final String ELEM_MERGE = "merge";
    
    public static final String ELEM_OPTIONS_RESPONSE = "options-response";
    
    public static final String ELEM_ACTIVITY_COLLECTION_SET = "activity-collection-set";
    public static final String ELEM_ACTIVITY_SET = "activity-set";
    public static final String ELEM_SOURCE = "source";
}
