/*
 * @(#)TreeEnumeration.java 1.0 7/11/97
 *
 * Copyright (c) 1997 Microsoft, Corp. All Rights Reserved.
 *
 */

package com.ms.xml.om;

import com.ms.xml.util.EnumWrapper;
import com.ms.xml.util.Queue;
import java.util.Enumeration;
import java.util.Stack;

/**
 * An Enumeration for iterating over the entire subtree of a given node
 * in the XML tree, DFS or BFS
 *
 * @version 1.0, 8/27/97
 * @see Element
 * @see Name
 */
public class TreeEnumeration implements Enumeration
{
    /**
     * Creates new iterator for iterating over all of the children
     * of the given root node.
     */
    public TreeEnumeration(Element node)
    {
        Initialize( node, true );
    }
    public TreeEnumeration(Element node, boolean enumDir )
    {
        Initialize( node, enumDir );
    }

    private void Initialize( Element node, boolean enumDir )
    {
        this.originalNode   = node;
        this.enumerationDir = enumDir;
        this.next           = null;

        this.DFSstack       = null;
        this.BFSqueue       = null;

        this.curr           = node;
        this.children       = new EnumWrapper(node);

        if( !enumDir )  // BFS traversal
        {
            this.BFSqueue   = new Queue();
        }
        else  // default to DFS traversal
        {
            this.next       = node;
            children.nextElement();
            this.DFSstack   = new Stack();
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
        if( !enumerationDir ) // BFS traversal
            return nextElementTreeBFS();
        else // default to DFS traversal
            return nextElementTreeDFS();
    }

    Element nextElementTreeDFS()
    {
        while (curr != null)
        {
            // try to go down a level
            DFSstack.push(children);
            children = curr.getElements();
            // try next at this level
            if (children.hasMoreElements())
            {
                curr = (Element)children.nextElement();
                return curr;
            }
            // go back up
            for (curr = curr.getParent(); curr != null; curr = curr.getParent())
            {
                if(DFSstack.empty())
                    return null;  // If the original node is not the root of tree...

                children = (Enumeration)DFSstack.pop();
                if (children.hasMoreElements())
                {
                    curr = (Element)children.nextElement();
                    return curr;
                }
            }
        }
        return null;
    }

    Element nextElementTreeBFS()
    {
        while (curr != null)
        {
            while( !children.hasMoreElements() )
            {
                children = (Enumeration)BFSqueue.pull();
                if( children == null )
                    return null;
            }

            // go to the next child
            curr = (Element)children.nextElement();
            BFSqueue.push(curr.getElements());
            return curr;
        }
        return null;
    }

    Element originalNode;

    Element next;

    Enumeration children;

    // DFSTree enumeration
    Stack DFSstack;
    Element curr;

    // BFSTree enumeration
    Queue BFSqueue;

    boolean enumerationDir;  // true = DFS tree  -  false = BFS tree
}
