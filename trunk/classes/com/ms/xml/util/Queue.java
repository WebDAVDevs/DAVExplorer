/*
 * @(#)Queue.java 1.0 7/29/97
 *
 * Copyright (c) 1997 Microsoft, Corp. All Rights Reserved.
 *
 */

package com.ms.xml.util;

import java.util.Vector;

public class Queue extends Vector
{

    /**
     * Creates a new queue with no elements.
     */
    public Queue()
    {
        super();
    }

    public boolean empty()
    {
        return isEmpty();
    }

    /**
     * Looks at the object at the front of this queue without removing it
     */
    public Object peek()
    {
        return firstElement();
    }

    /**
     * Removes the object at the front of queue and returns that object
     */
    public Object pull()
    {
        if( isEmpty() )
            return null;

        Object first = firstElement();
        removeElementAt(0);
        return first;
    }

    /**
     * Pushes an item onto the end of this queue
     */
    public Object push(Object  item)
    {
        addElement( item );
        return item;
    }

    /**
     * Determines if an object is in this queue.
     */
    public int search(Object  o)
    {
        return lastIndexOf(o);
    }
}
