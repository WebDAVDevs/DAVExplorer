/*
 * Copyright (c) 1998-2001 Regents of the University of California.
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
 * Title:       DataNode
 * Description: Node holding information about resources and collections
 * Copyright:   Copyright (c) 1998-2001 Regents of the University of California. All rights reserved.
 * @author      Undergraduate project team ICS 126B 1998
 * @date        1998
 * @author      Yuzo Kanomata, Joachim Feise (dav-exp@ics.uci.edu)
 * @date        17 March 1999
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        1 October 2001
 * Changes:     Change of package name
 */

package edu.uci.ics.DAVExplorer;

import java.util.Vector;

public class DataNode
{
    protected String name;
    protected String display;
    protected String type;
    protected long size;
    protected String lastModified;
    protected boolean locked;
    protected String lockToken;
    protected boolean collection;
    protected Vector subNodes = null;

    public DataNode(boolean collection, boolean locked, String lockToken, String name, String display, String type,
                  long size, String date, Vector subNodes)
    {
        this.name = name;
        this.display = display;
        this.type = type;
        this.size = size;
        this.lastModified = date;
        this.locked = locked;
        this.lockToken = lockToken;
        this.collection = collection;
        this.subNodes = subNodes;
    }

    public void setSubNodes(Vector subNodes)
    {
        this.subNodes = subNodes;
    }

    public Vector getSubNodes()
    {
        return subNodes;
    }

    public void setName(String newName)
    {
        name = newName;
    }

    public void setDisplay(String newDisplay)
    {
        display = newDisplay;
    }

    public void setType(String newType)
    {
        type = newType;
    }

    public void setSize(long newSize)
    {
        size = newSize;
    }

    public void setDate(String newDate)
    {
        lastModified = newDate;
    }

    public void lock( String lockToken )
    {
        locked = true;
        this.lockToken = lockToken;
    }

    public void unlock()
    {
        locked = false;
        lockToken = null;
    }

    public void makeCollection()
    {
        collection = true;
    }

    public void makeNonCollection()
    {
        collection = false;
    }

    public String getName()
    {
        return new String(name);
    }

    public String getDisplay()
    {
        return new String(display);
    }

    public String getType()
    {
        return new String(type);
    }

    public String getLockToken()
    {
        return lockToken;
    }

    public long getSize()
    {
        return size;
    }

    public String getDate()
    {
        return new String(lastModified);
    }

    public boolean isLocked()
    {
        return locked;
    }

    public boolean isCollection()
    {
        return collection;
    }
}
