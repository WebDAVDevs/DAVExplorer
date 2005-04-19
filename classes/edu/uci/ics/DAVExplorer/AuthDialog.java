/*
 * Copyright (c) 2001-2005 Regents of the University of California.
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
 * Title:       Authentication Dialog
 * Description: Wrapper around the login dialog
 * Copyright:   Copyright (c) 2001-2005 Regents of the University of California. All rights reserved.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        31 July 2001
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        1 October 2001
 * Changes:     Change of package name
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        14 January 2004
 * Changes:     Fixed using webpage-provided username/password for applet usage
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        08 February 2004
 * Changes:     Added Javadoc templates
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        18 April 2005
 * Changes:     Added function to get auth info without scheme/realm
 */

package edu.uci.ics.DAVExplorer;

import HTTPClient.AuthorizationPrompter;
import HTTPClient.AuthorizationInfo;
import HTTPClient.NVPair;


/**
 * 
 */
public class AuthDialog implements AuthorizationPrompter
{
    /**
     * Constructor 
     */
    public AuthDialog()
    {
    }

    /**
     * the method called by DefaultAuthHandler.
     *
     * @return the username/password pair
     */
    public NVPair getUsernamePassword(AuthorizationInfo info, boolean forProxy)
    {
		if( GlobalData.getGlobalData().isAppletMode() )
		{
			// as applet, we may be able to get username and pw from the initial webpage data
			String[][] initialSites = GlobalData.getGlobalData().getInitialSites();
			for (int i = 0; i < initialSites.length; i++)
			{
				String is[] = initialSites[i];
				if( is.length == 0 )
					continue;
				String initialSite = initialSites[i][0];
				if (initialSite.startsWith(GlobalData.WebDAVPrefixSSL))
					initialSite = initialSite.substring(GlobalData.WebDAVPrefixSSL.length());
				if (initialSite.startsWith(GlobalData.WebDAVPrefix))
					initialSite = initialSite.substring(GlobalData.WebDAVPrefix.length());
    
				// check for username/pw
				if( (initialSite.indexOf(info.getHost()) != -1) && (is.length == 3) && (i != lastIndex) )
				{
					lastIndex = i;
					return new NVPair( is[1], is[2] );
				}
			}
		}
		lastIndex = -1;
		
        WebDAVLoginDialog dlg = new WebDAVLoginDialog( "Login", info.getRealm(), info.getScheme(), true );
        if( dlg.getUsername().equals( "" ) || dlg.getUserPassword().equals( "" ) )
            return null;

        NVPair answer = new NVPair( dlg.getUsername(), dlg.getUserPassword() );
        dlg.clearData();
        dlg = null;

        return answer;
    }

    
    /**
     * Getting auth info without scheme and realm.
     * Called only from within this package to allow manual entering of auth info.
     *
     * @return the username/password pair
     */
    NVPair getUsernamePassword()
    {
        lastIndex = -1;
        
        WebDAVLoginDialog dlg = new WebDAVLoginDialog( "Login", null, null, true );
        if( dlg.getUsername().equals( "" ) || dlg.getUserPassword().equals( "" ) )
            return null;

        NVPair answer = new NVPair( dlg.getUsername(), dlg.getUserPassword() );
        dlg.clearData();
        dlg = null;

        return answer;
    }

    
    static int lastIndex = -1;
}
