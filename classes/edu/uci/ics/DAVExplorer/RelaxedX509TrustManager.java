/*
 * Copyright (c) 2001-2004 Regents of the University of California.
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
 * Title:       Trust manager
 * Description: Very relaxed trust manager for SSL
 * Copyright:   Copyright (c) 2001-2004 Regents of the University of California. All rights reserved.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        19 November 2001
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        08 February 2004
 * Changes:     Added Javadoc templates
 */

package edu.uci.ics.DAVExplorer;

import com.sun.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;


/**
 * 
 */
public class RelaxedX509TrustManager implements X509TrustManager
{
    /**
     * Constructor
     */
    public RelaxedX509TrustManager()
    {
    }


    /**
     * 
     * @return
     */
    public X509Certificate[] getAcceptedIssuers()
    {
        return null;
    }


    /**
     *
     * @param parm1
     *  
     * @return
     */
    public boolean isClientTrusted( X509Certificate[] parm1 )
    {
        return true;
    }


    /**
     *
     * @param parm1
     *  
     * @return
     */
    public boolean isServerTrusted( X509Certificate[] parm1 )
    {
        return true;
    }


    /**
     *
     * @param chain
     */
    public void checkClientTrusted( X509Certificate[] chain )
    {
    }


    /**
     *
     * @param chain
     */
    public void checkServerTrusted( X509Certificate[] chain )
    {
    }
}
