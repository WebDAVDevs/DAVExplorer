/*
 * Copyright (c) 2003-2004 Regents of the University of California.
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
 * Title:       DeltaV Properties
 * Description: Simple list of all DeltaV properties, based on RFC 3253
 * Copyright:   Copyright (c) 2003-2004 Regents of the University of California. All rights reserved.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        27 August 2003
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        08 February 2004
 * Changes:     Added Javadoc templates
 */

package edu.uci.ics.DAVExplorer;

import java.util.Vector;
import java.util.Enumeration;


/**
 * 
 * Simple list of DeltaV properties, based on RFC 3253.
 * Updated as necessary.
 * 
 * @see <a href="http://www.ietf.org/rfc/rfc3253.txt">RFC 3253</a>
 */
public class DeltaVProp extends WebDAVProp
{
    /// version-control properties
    public static final String PROP_COMMENT = "comment";
    public static final String PROP_CREATOR_DISPLAYNAME = "creator-displayname";
    public static final String PROP_SUPPORTED_METHOD_SET = "supported-method-set";
    public static final String PROP_SUPPORTED_LIVE_PROPERTY_SET = "supported-live-property-set";
    public static final String PROP_SUPPORTED_REPORT_SET = "supported-report-set";
    public static final String PROP_CHECKED_IN = "checked-in";
    public static final String PROP_AUTO_VERSION = "auto-version";
    public static final String PROP_CHECKED_OUT = "checked-out";
    public static final String PROP_PREDECESSOR_SET = "predecessor-set";
    public static final String PROP_SUCCESSOR_SET = "successor-set";
    public static final String PROP_CHECKOUT_SET = "checkout-set";
    public static final String PROP_VERSION_NAME = "version-name";

    /// checkout-in-place properties
    public static final String PROP_CHECKOUT_FORK = "checkout-fork";
    public static final String PROP_CHECKIN_FORK = "checkin-fork";

    /// version-history properties
    public static final String PROP_VERSION_SET = "version-set";
    public static final String PROP_ROOT_VERSION = "root-version";
    public static final String PROP_VERSION_HISTORY = "version-history";

    /// workspace properties
    public static final String PROP_WORKSPACE_CHECKOUT_SET = "workspace-checkout-set";
    public static final String PROP_WORKSPACE = "workspace";

    /// label properties
    public static final String PROP_LABEL_NAME_SET = "label-name-set";

    /// working-resource properties
    /// includes checkout-fork and checkin-fork (see checkout-in-place properties)
    public static final String PROP_AUTO_UPDATE = "auto-update";

    /// advanced versioning properties
    /// merge properties
    public static final String PROP_MERGE_SET = "merge-set";
    public static final String PROP_AUTO_MERGE_SET = "auto-merge-set";

    /// baseline properties
    public static final String PROP_BASELINE_CONTROLLED_COLLECTION = "baseline-controlled-collection";
    public static final String PROP_SUBBASELINE_SET = "subbaseline-set";
    public static final String PROP_BASELINE_COLLECTION = "baseline-collection";
    public static final String PROP_VERSION_CONTROLLED_CONFIGURATION = "version-controlled-configuration";
    public static final String PROP_BASELINE_CONTROLLED_COLLECTION_SET = "baseline-controlled-collection-set";
    
    // activity properties
    public static final String PROP_ACTIVITY_VERSION_SET = "activity-version-set";
    public static final String PROP_ACTIVITY_CHECKOUT_SET = "activity-checkout-set";
    public static final String PROP_SUBACTIVITY_SET = "subactivity-set";
    public static final String PROP_CURRENT_WORKSPACE_SET = "current-workspace-set";
    public static final String PROP_ACTIVITY_SET = "activity-set";
    public static final String PROP_UNRESERVED = "unreserved";
    public static final String PROP_CURRENT_ACTIVITY_SET = "current-activity-set";

    /// version-controlled collection properties
    public static final String PROP_ECLIPSED_SET = "eclipsed-set";
    public static final String PROP_VERSION_CONTROLLED_BINDING_SET = "version-controlled-binding-set";


    /**
     * Constructor 
     */
    public DeltaVProp()
    {
    }


    /**
     * 
     * @return
     */
    public static Enumeration getDeltaVProps()
    {
        Vector prop_list = new Vector();

        // add the elements from the super class
        Enumeration enum = getDavProps();
        while( enum.hasMoreElements() )
            prop_list.addElement( enum.nextElement() );

        prop_list.addElement( PROP_COMMENT );
        prop_list.addElement( PROP_CREATOR_DISPLAYNAME );
        prop_list.addElement( PROP_SUPPORTED_METHOD_SET );
        prop_list.addElement( PROP_SUPPORTED_LIVE_PROPERTY_SET );
        prop_list.addElement( PROP_SUPPORTED_REPORT_SET );
        prop_list.addElement( PROP_CHECKED_IN );
        prop_list.addElement( PROP_AUTO_VERSION );
        prop_list.addElement( PROP_CHECKED_OUT );
        prop_list.addElement( PROP_PREDECESSOR_SET );
        prop_list.addElement( PROP_SUCCESSOR_SET );
        prop_list.addElement( PROP_CHECKOUT_SET );
        prop_list.addElement( PROP_VERSION_NAME );
        prop_list.addElement( PROP_CHECKOUT_FORK );
        prop_list.addElement( PROP_CHECKIN_FORK );
        prop_list.addElement( PROP_VERSION_SET );
        prop_list.addElement( PROP_ROOT_VERSION );
        prop_list.addElement( PROP_VERSION_HISTORY );
        prop_list.addElement( PROP_WORKSPACE_CHECKOUT_SET );
        prop_list.addElement( PROP_WORKSPACE );
        prop_list.addElement( PROP_LABEL_NAME_SET );
        prop_list.addElement( PROP_AUTO_UPDATE );
        prop_list.addElement( PROP_MERGE_SET );
        prop_list.addElement( PROP_AUTO_MERGE_SET );
        prop_list.addElement( PROP_BASELINE_CONTROLLED_COLLECTION );
        prop_list.addElement( PROP_SUBBASELINE_SET );
        prop_list.addElement( PROP_BASELINE_COLLECTION );
        prop_list.addElement( PROP_VERSION_CONTROLLED_CONFIGURATION );
        prop_list.addElement( PROP_BASELINE_CONTROLLED_COLLECTION_SET );
        prop_list.addElement( PROP_ACTIVITY_VERSION_SET );
        prop_list.addElement( PROP_ACTIVITY_CHECKOUT_SET );
        prop_list.addElement( PROP_SUBACTIVITY_SET );
        prop_list.addElement( PROP_CURRENT_WORKSPACE_SET );
        prop_list.addElement( PROP_ACTIVITY_SET );
        prop_list.addElement( PROP_UNRESERVED );
        prop_list.addElement( PROP_CURRENT_ACTIVITY_SET );
        prop_list.addElement( PROP_ECLIPSED_SET );
        prop_list.addElement( PROP_VERSION_CONTROLLED_BINDING_SET );

        return (prop_list.elements());
    }


    /**
     * 
     * @param tag
     * @param value
     * @param schema
     */
    public DeltaVProp( String tag, String value, String schema )
    {
        this.tag = tag;
        this.value = value;
        this.schema = schema;
        this.children = null;
        this.leaf = true;
    }


    /**
     * 
     * @param tag
     * @param schema
     * @param children
     */
    public DeltaVProp( String tag, String schema, DeltaVProp[] children )
    {
        this.tag = tag;
        this.value = null;
        this.schema = schema;
        this.children = children;
        this.leaf = false;
    }


    /**
     * 
     */
    public WebDAVProp[] getChildren()
    {
        return children;
    }


    protected DeltaVProp[] children;
}
