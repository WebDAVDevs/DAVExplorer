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

import HTTPClient.AuthorizationHandler;
import HTTPClient.AuthorizationInfo;
import HTTPClient.RoRequest;
import HTTPClient.RoResponse;
import HTTPClient.Response;
import HTTPClient.AuthSchemeNotImplException;
import HTTPClient.Codecs;
import HTTPClient.NVPair;

import java.io.IOException;


class AuthHandler implements AuthorizationHandler
{
    /**
     * Only Basic Authorization supported
     */
    public AuthorizationInfo fixupAuthInfo( AuthorizationInfo info,
                                            RoRequest req, AuthorizationInfo challenge,
                                            RoResponse resp )
        throws AuthSchemeNotImplException
    {
        // nothing to do for Basic schema
        if( info.getScheme().equalsIgnoreCase("Basic") )
            return info;
        else throw new AuthSchemeNotImplException( info.getScheme() );
    }



    /**
     * returns the requested authorization, or null if none was given.
     *
     * @param info the parsed challenge from the server.
     * @param req the request which solicited this response
     * @param resp the full response received
     * @return a structure containing the necessary authorization info,
     *         or null
     * @exception AuthSchemeNotImplException if the authentication scheme
     *             in the challenge cannot be handled.
     */
    public AuthorizationInfo getAuthorization( AuthorizationInfo info,
                                               RoRequest req, RoResponse resp )
        throws AuthSchemeNotImplException
    {
	AuthorizationInfo cred;

        // we only handle Basic authentication
        if( !info.getScheme().equalsIgnoreCase("Basic") )
            throw new AuthSchemeNotImplException( info.getScheme() );

        // Ask the user for username/password
        if( !req.allowUI() )
	    return null;

	inp = new WebDAVLoginDialog( "Login", true );

        if( inp.getUsername().equals( "" ) || inp.getUserPassword().equals( "" ) )
            return null;

        NVPair answer;
        answer = new NVPair( inp.getUsername(), inp.getUserPassword() );

	// Now process the username/password
        cred = new AuthorizationInfo( info.getHost(), info.getPort(), info.getScheme(),
                                      info.getRealm(),
                                      Codecs.base64Encode( answer.getName() + ":" +
                                                           answer.getValue() ) );

        // try to get rid of any unencoded passwords in memory
        answer = null;
        System.gc();

        return cred;
    }


    public void handleAuthHeaders( Response resp, RoRequest req, AuthorizationInfo prev,
                                   AuthorizationInfo prxy )
        throws IOException
    {
    }



    public void handleAuthTrailers( Response resp, RoRequest req, AuthorizationInfo prev,
                                    AuthorizationInfo prxy )
        throws IOException
    {
    }


    protected WebDAVLoginDialog inp = null;
}

