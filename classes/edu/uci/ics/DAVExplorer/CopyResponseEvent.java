/*
 * Copyright (c) 1999-2001 Regents of the University of California.
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
// This code was originally written by an undergraduate project
// team at UCI.
//
// This class creates an event object which carries the path
// and node to the recieving listener.
//
////////////////////////////////////////////////////////////////
// The code has been modified to include povisions for the final
// WebDAV xml namespaces.  A small number of program errors have
// been corrected.
//
// Please use the following contact:
//
// dav-exp@ics.uci.edu
//
// Version: 0.4
// Changes by: Yuzo Kanomata and Joe Feise
// Date: 7April99
//
// Change List:


package DAVExplorer;

import java.util.*;

public class CopyResponseEvent extends EventObject
{
    WebDAVTreeNode Node;

    public CopyResponseEvent(Object module, WebDAVTreeNode n)
    {
        super(module);
        Node = n;
    }

    public WebDAVTreeNode getNode()
    {
        return Node;
    }
}
