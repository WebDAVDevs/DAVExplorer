/*
 * Copyright (c) 2001-2005 Regents of the University of California.
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

import java.util.Enumeration;
import java.util.Vector;


/**
 * Title:       Property Node
 * Description: Nodes for the property tree
 * Copyright:   Copyright (c) 2001-2005 Regents of the University of California. All rights reserved.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         5 October 2001
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         08 February 2004
 * Changes:     Added Javadoc templates
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         4 February 2005
 * Changes:     Some refactoring
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         22 April 2005
 * Changes:     Only having protected properties read-only
 */
public class PropNode
{
    /**
     * Constructor
     * 
     * @param tag the node tag
     * @param ns the node namespace
     * @param value the node value
     * @param modified flag to indicate if the node has to be saved
     */
    public PropNode( String tag, String ns, String value, boolean modified )
    {
        this.tag = tag;
        this.ns = ns;
        this.value = value;
        this.modified = modified;
    }


    /**
     * Constructor
     * 
     * @param tag the node tag
     * @param ns the node namespace
     * @param value the node value
     */
    public PropNode( String tag, String ns, String value )
    {
        this.tag = tag;
        this.ns = ns;
        this.value = value;
        this.modified = false;
    }


    /**
     * Get the node tag
     * 
     * @return the node tag
     */
    public String getTag()
    {
        return tag;
    }


    /**
     * Get the namespace identifier.
     *  
     * @return the namespace identifier
     */
    public String getNamespace()
    {
        return ns;
    }


    /**
     * Set the namespace identifier.
     * 
     * @param ns the new namespace identifier
     */
    public void setNamespace( String ns )
    {
        this.ns = ns;
        modified = true;
    }


    /**
     * Get the node value.
     * 
     * @return the node value
     */
    public String getValue()
    {
        if( value == null )
            return "";
        return value;
    }


    /**
     * Set the node value.
     * 
     * @param value the new node value
     */
    public void setValue( String value )
    {
        this.value = value;
        modified = true;
    }


    /**
     * Get a description of the node. Returns the node tag.
     * 
     * @return the node description
     */
    public String toString()
    {
        return getTag();
    }


    /**
     * Get the parent node.
     * 
     * @return the parent node
     */
    public PropNode getParent()
    {
        return parent;
    }


    /**
     * Set the parent node of this node.
     * 
     * @param parent the new parent node
     */
    public void setParent( PropNode parent )
    {
        this.parent = parent;
    }


    /**
     * Add a child node.
     *  
     * @param child the child node to be added 
     */
    public void addChild( Object child )
    {
        children.add( child );
    }


    /**
     * Remove a child node.
     * 
     * @param child the child node to be removed
     */
    public void removeChild( Object child )
    {
        children.remove(child);
        removedChildren.add(child);
    }


    /**
     * Check if the node is modified and needs to be saved.
     *  
     * @return true if the node is modified, false else
     */
    public boolean isModified()
    {
        return modified;
    }


    /**
     * Get an array of the child nodes.
     *  
     * @return the array of child nodes
     */
    public Object[] getChildren()
    {
        return children.toArray();
    }


    /**
     * Get an array of removed children. Needed for a proper PROPPATCH
     * 
     * @return the array of removed children
     */
    public Object[] getRemovedChildren()
    {
        return removedChildren.toArray();
    }


    /**
     * Clear the modified flag and delete all removed children
     */
    public void clear()
    {
        modified = false;
        removedChildren.clear();
    }


    /**
     * Check if the property is defined in RFC2518
     * @return true, if the property is in RFC2518, false else
     */
    public boolean isDAVProp()
    {
        // check if the property is defined in RFC2518 or if it is part of
        // a defined property hierarchy (e.g., lockdiscovery)
        if( (ns!=null) && ns.equals(WebDAVProp.DAV_SCHEMA) )
        {
            Enumeration props = WebDAVProp.getDAVProps();
            while( props.hasMoreElements() )
            {
                String prop = (String)props.nextElement();
                if( tag.equals(prop) )
                    return true;
                if( (parent!=null) && parent.isDAVProp() )
                    return true;
            }
        }
        return false;
    }


    /**
     * Check if the property is a protected WebDAV property as defined in RFC2518
     * @return true, if the property is protected, false else
     */
    public boolean isProtectedDAVProp()
    {
        // check if the property is protected in RFC2518 or if it is part of
        // a protected property hierarchy (e.g., lockdiscovery)
        if( (ns!=null) && ns.equals(WebDAVProp.DAV_SCHEMA) )
        {
            Enumeration props = WebDAVProp.getProtectedDAVProps();
            while( props.hasMoreElements() )
            {
                String prop = (String)props.nextElement();
                if( tag.equals(prop) )
                    return true;
                if( (parent!=null) && parent.isProtectedDAVProp() )
                    return true;
            }
        }
        return false;
    }


    private Vector children = new Vector();
    private Vector removedChildren = new Vector();
    private String tag;
    private String ns;
    private String value;
    private boolean modified;
    private PropNode parent;
}
