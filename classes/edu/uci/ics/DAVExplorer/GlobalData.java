/*
 * Copyright (c) 1999 Regents of the University of California.
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

package DAVExplorer;

/**
 * This singleton class defines various global data structures
 *
 * @version 0.1  25 May 1999
 * @author  Joachim Feise
 * @since   V0.1
 */

class GlobalData
{
    /** Debug variables */
    boolean debugAll      = false;
    boolean debugRequest  = debugAll | false;
    boolean debugResponse = debugAll | false;
    boolean debugTreeView = debugAll | false;
    boolean debugTreeNode = debugAll | false;
    boolean debugFileView = debugAll | false;

    private static GlobalData globalData;

    protected GlobalData()
    {
        init();
    }

    static GlobalData getGlobalData()
    {
        if( globalData == null )
            globalData = new GlobalData();
        return globalData;
    }

    public boolean getDebugAll()
    {
        return debugAll;
    }

    public void setDebugAll( boolean debug )
    {
        debugAll = debug;
        init();
    }

    public boolean getDebugRequest()
    {
        return debugRequest;
    }

    public void setDebugRequest( boolean debug )
    {
        debugRequest = debug;
        init();
    }

    public boolean getDebugResponse()
    {
        return debugResponse;
    }

    public void setDebugResponse( boolean debug )
    {
        debugResponse = debug;
        init();
    }

    public boolean getDebugTreeView()
    {
        return debugTreeView;
    }

    public void setDebugTreeView( boolean debug )
    {
        debugTreeView = debug;
        init();
    }

    public boolean getDebugTreeNode()
    {
        return debugTreeNode;
    }

    public void setDebugTreeNode( boolean debug )
    {
        debugTreeNode = debug;
        init();
    }

    public boolean getDebugFileView()
    {
        return debugFileView;
    }

    public void setDebugFileView( boolean debug )
    {
        debugFileView = debug;
        init();
    }

    private void init()
    {
        debugRequest |= debugAll;
        debugResponse |= debugAll;
        debugTreeView |= debugAll;
        debugTreeNode |= debugAll;
        debugFileView |= debugAll;
    }
}

