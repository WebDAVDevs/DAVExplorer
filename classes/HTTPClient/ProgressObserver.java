/*
 * Copyright (c) 2003 Th. Rickert.
 * Copyright (c) 2003 Regents of the University of California.
 * All rights reserved.
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


package HTTPClient;

import java.util.Vector;

/**
 * This class is a container for all registered {@link ProgressListener}.
 * If a progress event is received via {@link #fireProgressEvent(int,int,String)}
 * then this method fires "events" to all known {@link ProgressListener}s.<br>
 * This class is a singleton. Please use {@link #getInstance()} to get the only
 * instance of this class.
 *
 * @author      Thoralf Rickert
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @version     0.1
 */


public class ProgressObserver
{
    protected static ProgressObserver instance = null;
    /** contains all registered ProgressListeners */
    protected Vector progressListeners = new Vector();


    /**
     * This class is a singleton. Please use {@link #getInstance()} to get
     * the only instance of this class.
     */
    protected ProgressObserver()
    {
    }


    /**
     * Returns the only instance of this class.
     */
    public static ProgressObserver getInstance()
    {
        // if it doesnt exist, create one instance...there are some problems
        // with concurrency, but that isn't important in this app...
        if( instance == null )
            instance = new ProgressObserver();
        return instance;
    }


    /**
     * Use this method to add your ProgressListener implementation to the
     * observer.
     */
    public void addProgressListener( ProgressListener listener )
    {
        if( listener == null )
            return;
        progressListeners.addElement( listener );
    }


    /**
     * Returns all known ProgressListeners.
     */
    public Vector getProgressListeners()
    {
        return progressListeners;
    }


    /**
     * Takes every ProgressListener and calls the method 
     * {@link ProgressListener#progressAchieved(long,long,String)} to inform
     * the listener about the progress state.
     *
     * @param writtenBytes is the number of written or read bytes from a
     *                     Request or Response (only body data)
     * @param len          is the full length of a Request or Response 
     *                     body. If it is -1, then the size is unknown
     *                     (for example if the transfer encoding is
     *                     chunked).
     * @param method       is the HTTP method (GET, PUT, ...)
     */
    public void fireProgressEvent( long writtenBytes, long len, String method )
    {
        for( int i=0; i<progressListeners.size(); i++ )
        {
            ((ProgressListener)progressListeners.elementAt(i)).progressAchieved( writtenBytes, len, method );
        }
    }
}
