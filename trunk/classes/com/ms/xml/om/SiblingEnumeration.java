/*
 * @(#)SiblingEnumeration.java 1.0 7/11/97
 *
 * Copyright (c) 1997 Microsoft, Corp. All Rights Reserved.
 *
 */

package com.ms.xml.om;

import java.util.Enumeration;

/**
 * An Enumeration for iterating over the siblings of
 * a given node in the XML tree
 *
 * @version 1.0, 8/27/97
 * @see Element
 * @see Name
 */
public class SiblingEnumeration implements Enumeration
{
    /**
     * Creates new iterator for iterating over all of the children
     * of the given root node.
     */
    public SiblingEnumeration(Element node)
    {
        Initialize( node, true );
    }
    public SiblingEnumeration(Element node, boolean enumDir )
    {
        Initialize( node, enumDir );
    }

    /**
     * 03 Jan 2001: Assignment to this.next missing. Note that reverse traversal wasn't tested
     * @author : Microsoft, Joachim Feise
     * @version 1.1
     */
    private void Initialize( Element node, boolean enumDir )
    {
        this.originalNode   = node;
        this.enumerationDir = enumDir;
        this.next           = null;
        this.parent         = null;
        this.siblings       = null;
        this.siblingIndex   = 0;

        if( node != null )
        {
            this.parent = node.getParent();
            if( this.parent!=null )
            {
                if( !enumDir )  // backwards traversal
                {
                    // locate the given node in the children list
                    int i = 0;

                    Enumeration en = this.parent.getElements();
                    while( en.hasMoreElements() && (Element)en.nextElement() != node )
                        i++;

                    // If (i >= numElements) then node was not found.
                    // This should never happen, since parent = node.getParent()
                    this.siblingIndex = (i < this.parent.numElements() ) ? i : 0;
                }
                else // default to forward traversal
                {
                    // Advance siblings enumerator to correct position
                    this.siblings = this.parent.getElements();

                    while( siblings.hasMoreElements() && (Element)siblings.nextElement() != node )
                        ;
                    this.next = node;
                }
            }
        }
    }

    /**
     * Reset the iterator so you can iterate through the elements again.
     */
    public void reset()
    {
        Initialize( originalNode, enumerationDir );
    }

    /**
     * Return whether or not there are any more matching elements.
     * @return true if the next call to nextElement will return
     * non null result.
     */
    public boolean hasMoreElements()
    {
        if (next == null)
        {
            next = next();
        }
        return (next != null) ? true : false;
    }

    /**
     * Return the next matching element.
     * @return Element or null of there are no more matching elements.
     */
    public Object nextElement()
    {
        if (next != null)
        {
            Element result = next;
            next = null;
            return result;
        }
        return next();
    }

    /**
     * Internal method for getting next element.
     */
    Element next()
    {
        if( !enumerationDir ) // backwards traversal
            return prevSibling();
        else // default to forward traversal
            return nextSibling();
    }

    /**
     * 23 May 2000: Check for hasMoreElements() was missing
     * @author : Microsoft, Joachim Feise
     * @version 1.1
     */
    Element nextSibling()
    {
        if (siblings != null && siblings.hasMoreElements())
        {
            Element e = (Element)siblings.nextElement();
            return e;
        }
        return null;
    }

    Element prevSibling()
    {
        if( parent != null )
        {
            Element e = parent.getChild( --siblingIndex );
            return e;
        }
        return null;
    }

    Element originalNode;

    Element next;

    // Sibling enumeration
    Element parent;
    Enumeration siblings;
    int siblingIndex;

    boolean enumerationDir;  // true = forwards  -  false = backwards
}

