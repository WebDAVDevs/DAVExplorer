/*
 * Copyright (c) 2003 Th. Rickert.
 * Copyright (c) 2003 Regents of the University of California.
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

import java.util.Vector;


/**
 * This is a small container for URIs that are used by the user.
 * The URIs are persisted in the DAVExplorer.dat file in java.home.
 *
 * @author      Thoralf Rickert
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @version     0.1
 * @date        07 April 2003
 */

public class URIContainer
{
    protected static URIContainer instance = null;
    protected static Vector uris = new Vector();

    /** an object whose finalizer will persist the URIs */
    private static Object URISaver = null;


    static
    {
        // the nearest thing to atexit() in JDK 1.2 and below
        // JDK 1.3 and above have Runtime.getRuntime().addShutdownHook()
        URISaver = new Object()
        {
            public void finalize()
            {
                saveURIs();
            }
        };
        try
        {
            System.runFinalizersOnExit(true);
        }
        catch( Throwable t )
        {
        }
    }


    protected URIContainer()
    {
        loadURIs();
    }


    public static URIContainer getInstance()
    {
        if( instance == null )
            instance = new URIContainer();
        return instance;
    }


    public void loadURIs()
    {
        Vector data = GlobalData.getGlobalData().ReadConfigEntry( "uri", true );
        for( int i=0; i<data.size(); i++ )
            addURI( (String)data.elementAt(i) );
    }


    public static void saveURIs()
    {
        GlobalData.getGlobalData().WriteConfigEntry( "uri", uris );
    }


    public void addURI(String uri)
    {
        if( uri == null )
            return;
        if( uris.contains(uri) )
            return;
        uris.addElement(uri);
    }


    public Vector getURIs()
    {
        return uris;
    }
}
