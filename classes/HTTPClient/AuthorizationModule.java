/*
 * @(#)AuthorizationModule.java				0.3 30/01/1998
 *
 *  This file is part of the HTTPClient package
 *  Copyright (C) 1996-1998  Ronald Tschalaer
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Library General Public License for more details.
 *
 *  You should have received a copy of the GNU Library General Public
 *  License along with this library; if not, write to the Free
 *  Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 *  MA 02111-1307, USA
 *
 *  For questions, suggestions, bug-reports, enhancement-requests etc.
 *  I may be contacted at:
 *
 *  ronald@innovation.ch
 *  Ronald.Tschalaer@psi.ch
 *
 */

package HTTPClient;

import java.io.IOException;
import java.net.ProtocolException;
import java.util.Hashtable;


/**
 * This module handles authentication requests. Authentication info is
 * preemptively sent if any suitable candidate info is available. If a
 * request returns with an appropriate status (401 or 407) then the
 * necessary info is sought from the AuthenticationInfo class.
 *
 * @version	0.3  30/01/1998
 * @author	Ronald Tschal&auml;r
 */

class AuthorizationModule implements HTTPClientModule, GlobalConstants
{
    /** This holds the current Proxy-Authorization-Info for each
        HTTPConnection */
    private static Hashtable proxy_cntxt_list = new Hashtable();

    /** counters for challenge and auth-info lists */
    private int	auth_lst_idx,
		prxy_lst_idx,
		auth_scm_idx,
		prxy_scm_idx;

    /** the last auth info sent, if any */
    AuthorizationInfo auth_sent;
    AuthorizationInfo prxy_sent;

    /** is the info in auth_sent a preemtive guess or the result of a 4xx */
    boolean auth_guessed;
    boolean prxy_guessed;


    // Constructors

    /**
     * Initialize counters for challenge and auth-info lists.
     */
    AuthorizationModule()
    {
	auth_lst_idx = 0;
	prxy_lst_idx = 0;
	auth_scm_idx = 0;
	prxy_scm_idx = 0;

	auth_sent = null;
	prxy_sent = null;

	auth_guessed = false;
	prxy_guessed = false;
    }


    // Methods

    /**
     * Invoked by the HTTPClient.
     */
    public int requestHandler(Request req, Response[] resp)
    {
	HTTPConnection con = req.getConnection();
	AuthorizationHandler auth_handler = AuthorizationInfo.getAuthHandler();
	AuthorizationInfo guess;


	// Preemptively send proxy authorization info

	Proxy: if (con.getProxyHost() != null)
	{
	    Hashtable proxy_auth_list = Util.getList(proxy_cntxt_list,
					     req.getConnection().getContext());
	    guess = (AuthorizationInfo) proxy_auth_list.get(
				    con.getProxyHost()+":"+con.getProxyPort());
	    if (guess == null)  break Proxy;

	    if (auth_handler != null)
	    {
		try
		    { guess = auth_handler.fixupAuthInfo(guess, req, null, null); }
		catch (AuthSchemeNotImplException atnie)
		    { break Proxy; }
		if (guess == null) break Proxy;
	    }

	    int idx;
	    NVPair[] hdrs = req.getHeaders();
	    for (idx=0; idx<hdrs.length; idx++)
	    {
		if (hdrs[idx].getName().equalsIgnoreCase("Proxy-Authorization"))
		    break;	// already set
	    }
	    if (idx == hdrs.length) // add proxy-auth header
	    {
		hdrs = Util.resizeArray(hdrs, idx+1);
		req.setHeaders(hdrs);
	    }
	    else
		if (!prxy_guessed)  break Proxy;

	    hdrs[idx] = new NVPair("Proxy-Authorization", guess.toString());

	    prxy_sent    = guess;
	    prxy_guessed = true;

	    if (DebugMods)
		System.err.println("AuthM: Preemptively sending " +
				   "Proxy-Authorization '" + guess + "'");
	}


	// Preemptively send authorization info

	guess = AuthorizationInfo.findBest(req);
	Auth: if (guess != null)
	{
	    if (auth_handler != null)
	    {
		try
		    { guess = auth_handler.fixupAuthInfo(guess, req, null, null); }
		catch (AuthSchemeNotImplException atnie)
		    { break Auth; }
		if (guess == null) break Auth;
	    }

	    int idx;
	    NVPair[] hdrs = req.getHeaders();
	    for (idx=0; idx<hdrs.length; idx++)
	    {
		if (hdrs[idx].getName().equalsIgnoreCase("Authorization"))
		    break;		// already set
	    }
	    if (idx == hdrs.length)	// add auth header
	    {
		hdrs = Util.resizeArray(hdrs, idx+1);
		req.setHeaders(hdrs);
	    }
	    else
		if (!auth_guessed)  break Auth;

	    hdrs[idx] = new NVPair("Authorization", guess.toString());

	    auth_sent    = guess;
	    auth_guessed = true;

	    if (DebugMods)
		System.err.println("AuthM: Preemptively sending Authorization '"
				   + guess + "'");
	}

	return REQ_CONTINUE;
    }


    /**
     * Invoked by the HTTPClient.
     */
    public void responsePhase1Handler(Response resp, RoRequest req)
		throws IOException
    {
	/* If auth info successful update path list. Note: if we
	 * preemptively sent auth info we don't actually know if
	 * it was necessary. Therefore we don't update the path
	 * list in this case; this prevents it from being 
	 * contaminated. If the info was necessary, then the next
	 * time we access this resource we will again guess the
	 * same info and send it.
	 */
	if (resp.getStatusCode() < 400)
	{
	    if (auth_sent != null  &&  !auth_guessed)
		auth_sent.addPath(req.getRequestURI());

	    auth_guessed = true;
	    prxy_guessed = true;
	}
    }


    /**
     * Invoked by the HTTPClient.
     */
    public int responsePhase2Handler(Response resp, Request req)
		throws IOException, AuthSchemeNotImplException
    {
	// Let the AuthHandler handle any Authentication headers.

	AuthorizationHandler h = AuthorizationInfo.getAuthHandler();
	if (h != null)
	    h.handleAuthHeaders(resp, req, auth_sent, prxy_sent);


	// handle 401 and 407 response codes

	int sts  = resp.getStatusCode();
	switch(sts)
	{
	    case 401: // Unauthorized
	    case 407: // Proxy Authentication Required

		if (DebugMods) System.err.println("AuthM: Handling status: " +
					    sts + " " + resp.getReasonLine());

		if (sts == 401)
		{
		    int[] idx_arr = { auth_lst_idx,	// hack to pass by ref
				      auth_scm_idx};

		    auth_sent = setAuthHeaders(
					  resp.getHeader("WWW-Authenticate"),
					  req, resp, "Authorization", idx_arr,
					  auth_sent);
		    auth_guessed = false;

		    auth_lst_idx = idx_arr[0];
		    auth_scm_idx = idx_arr[1];
		}
		else
		{
		    int[] idx_arr = { prxy_lst_idx,	// hack to pass by ref
				      prxy_scm_idx};

		    prxy_sent = setAuthHeaders(
					  resp.getHeader("Proxy-Authenticate"),
					  req, resp, "Proxy-Authorization",
					  idx_arr, prxy_sent);
		    prxy_guessed = false;

		    prxy_lst_idx = idx_arr[0];
		    prxy_scm_idx = idx_arr[1];

		    HTTPConnection con = req.getConnection();
		    Hashtable proxy_auth_list = Util.getList(proxy_cntxt_list,
							     con.getContext());
		    if (prxy_sent != null)
			proxy_auth_list.put(
				    con.getProxyHost()+":"+con.getProxyPort(),
				    prxy_sent);
		}

		if (req.getStream() == null  &&
		    ((sts == 401  &&  auth_sent != null)  ||
		     (sts == 407  &&  prxy_sent != null)))
		{
		    try { resp.getInputStream().close(); }
		    catch (IOException ioe) { }

		    if (DebugMods)
		    {
			if (auth_sent != null)
			    System.err.println("AuthM: Resending request " +
					       "with Authorization '" +
					       auth_sent + "'");
			else
			    System.err.println("AuthM: Resending request " +
					       "with Proxy-Authorization '" +
					       prxy_sent + "'");
		    }

		    return RSP_REQUEST;
		}

		if (DebugMods)
		    System.err.println("AuthM: No Auth Info found - status " +
					sts + " not handled");

		return RSP_CONTINUE;

	    default:

		return RSP_CONTINUE;
	}
    }


    /**
     * Invoked by the HTTPClient.
     */
    public void responsePhase3Handler(Response resp, RoRequest req)
    {
    }


    /**
     * Invoked by the HTTPClient.
     */
    public void trailerHandler(Response resp, RoRequest req)  throws IOException
    {
	// Let the AuthHandler handle any Authentication headers.

	AuthorizationHandler h = AuthorizationInfo.getAuthHandler();
	if (h != null)
	    h.handleAuthTrailers(resp, req, auth_sent, prxy_sent);
    }


    /**
     * Handles authentication requests and sets the authorization headers.
     * It tries to retrieve the neccessary parameters from AuthorizationInfo,
     * and failing that calls the AuthHandler. Handles multiple authentication
     * headers.
     *
     * @param  auth_str the authentication header field returned by the server.
     * @param  req      the Request used
     * @param  resp     the full Response received
     * @param  header   the header name to use in the new headers array.
     * @param  idx_arr  an array of indicies holding the state of where we
     *                  are when handling multiple authorization headers.
     * @param  prev     the previous auth info sent, or null if none
     * @return the new credentials, or null if none found
     * @exception ProtocolException if <var>auth_str</var> is null.
     * @exception AuthSchemeNotImplException if thrown by the AuthHandler.
     */
    private AuthorizationInfo setAuthHeaders(String auth_str, Request req,
					     RoResponse resp, String header,
					     int[] idx_arr,
					     AuthorizationInfo prev)
	throws ProtocolException, AuthSchemeNotImplException
    {
	if (auth_str == null)
	    throw new ProtocolException("Missing Authentication header");

	// get the list of challenges the server sent
	AuthorizationInfo[] challenge =
			AuthorizationInfo.parseAuthString(auth_str, req, resp);

	/* some servers expect a 401 to invalidate sent credentials.
	 * However, only do this for Basic scheme (because e.g. digest
	 * "stale" handling will fail otherwise)
	 */
	if (prev != null  &&  prev.getScheme().equalsIgnoreCase("Basic"))
	{
	    for (int idx=0; idx<challenge.length; idx++)
		if (prev.getRealm().equals(challenge[idx].getRealm())  &&
		    prev.getScheme().equalsIgnoreCase(challenge[idx].getScheme()))
		    AuthorizationInfo.removeAuthorization(prev,
					      req.getConnection().getContext());
	}

	AuthorizationInfo    credentials  = null;
	AuthorizationHandler auth_handler = AuthorizationInfo.getAuthHandler();

	// try next auth challenge in list
	while (credentials == null  &&  idx_arr[0] != -1)
	{
	    credentials = AuthorizationInfo.getAuthorization(
				    challenge[idx_arr[0]], req, resp, false);
	    if (auth_handler != null  &&  credentials != null)
		credentials = auth_handler.fixupAuthInfo(credentials, req,
						challenge[idx_arr[0]], resp);
	    if (++idx_arr[0] == challenge.length)
		idx_arr[0] = -1;
	}


	// if we don't have any credentials then prompt the user
	if (credentials == null)
	{
	    for (int idx=0; idx<challenge.length; idx++)
	    {
		try
		{
		    credentials = AuthorizationInfo.queryAuthHandler(
					    challenge[idx_arr[1]], req, resp);
		}
		catch (AuthSchemeNotImplException atnie)
		{
		    if (idx == challenge.length-1)
			throw atnie;
		    continue;
		}
		break;
	    }
	    if (++idx_arr[1] == challenge.length)
		idx_arr[1] = 0;
	}

	// if we still don't have any credentials then give up
	if (credentials == null)
	    return null;

	// find auth info
	int auth_idx;
	NVPair[] hdrs = req.getHeaders();
	for (auth_idx=0; auth_idx<hdrs.length; auth_idx++)
	{
	    if (hdrs[auth_idx].getName().equalsIgnoreCase(header))
		break;
	}

	// add credentials to headers
	if (auth_idx == hdrs.length)
	{
	    hdrs = Util.resizeArray(hdrs, auth_idx+1);
	    req.setHeaders(hdrs);
	}
	hdrs[auth_idx] = new NVPair(header, credentials.toString());

	return credentials;
    }
}

