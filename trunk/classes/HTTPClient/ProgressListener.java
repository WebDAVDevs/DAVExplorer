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

/**
 * This interface gives an application the possibility to retreive the 
 * progress state of a running HTTP Request or Response. A ProgressListener
 * receives the number of bytes that are already written or read to or from
 * the socket.<br>
 * Note: Only body data will be counted. A ProgressListener will never be
 * informed about HTTP Header transfers.
 *
 * @author      Thoralf Rickert
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @version     0.1
 */


public interface ProgressListener
{
    /**
     * Will be called if the program has transferred some data...<br>
     * If the transfer is complete then a additional call is done, to inform
     * all ProgressListener, that the operation is completed. You can see this
     * because <code>writtenBytes</code> and <code>fullLength</code> have the
     * same value...
     *
     * @param writtenBytes is the number of written or read bytes from a
     *                     Request or Response (only body data)
     * @param fullLength   is the full length of a Request or Response 
     *                     body. If it is -1, then the size is unknown
     *                     (for example if the transfer encoding is
     *                     chunked).
     * @param method       is the HTTP method (GET, PUT, ...)
     */
    public void progressAchieved( long writtenBytes, long fullLength, String method );
}
