/*
 * @(#)IdempotentSequence.java				0.3 30/01/1998
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


/**
 * This class checks whether a sequence of requests is idempotent. This is
 * used to stall the pipeline of requests and when determining which requests
 * may be automatically retried.
 *
 * Note: unknown methods (i.e. a method which is not HEAD, GET, POST,
 * PUT, DELETE, OPTIONS or TRACE) is treated conservatively, meaning
 * it is assumed to have side effects and is not idempotent.
 *
 * @version	0.3  30/01/1998
 * @author	Ronald Tschal&auml;r
 */

class IdempotentSequence
{
    /** method number definitions */
    private static final int UNKNOWN = 0,
			     HEAD    = 1,
			     GET     = 2,
			     POST    = 3,
			     PUT     = 4,
			     DELETE  = 5,
			     OPTIONS = 6,
			     TRACE   = 7;

    /** these are the history of previous requests */
    private int[]    m_history;
    private String[] r_history;


    // Constructors

    /**
     * Start a new sequence of requests.
     */
    public IdempotentSequence()
    {
	m_history = new int[0];
	r_history = new String[0];
    }


    // Methods

    /**
     * Is the sequence of all previous requests and the new request
     * idempotent?
     *
     * @param req the next request
     */
    public boolean isIdempotent(Request req)
    {
	int    method = methodNum(req.getMethod());
	String resource = req.getRequestURI();


	try
	{
	    // if request has no side effects, then sequence stays idempotent

	    if (!methodHasSideEffects(method))
		return true;


	    // The method has side effects, so we check the history

	    for (int idx=0; idx<r_history.length; idx++)
	    {
		if (r_history[idx].equals(resource))
		{
		    if (!methodIsIdempotent(method) || m_history[idx] != method)
			return false;
		}
	    }


	    // if we got this far then sequence is still idempotent

	    return true;
	}
	finally    		// update history list
	{
	    m_history = Util.resizeArray(m_history, m_history.length+1);
	    m_history[m_history.length-1] = method;
	    r_history = Util.resizeArray(r_history, r_history.length+1);
	    r_history[r_history.length-1] = resource;
	}
    }


    public static boolean methodIsIdempotent(String method)
    {
	return methodIsIdempotent(methodNum(method));
    }


    private static boolean methodIsIdempotent(int method)
    {
	switch (method)
	{
	    case HEAD:
	    case GET:
	    case PUT:
	    case DELETE:
	    case OPTIONS:
	    case TRACE:
		return true;
	    default:
		return false;
	}
    }


    public static boolean methodHasSideEffects(String method)
    {
	return methodHasSideEffects(methodNum(method));
    }


    private static boolean methodHasSideEffects(int method)
    {
	switch (method)
	{
	    case HEAD:
	    case GET:
	    case OPTIONS:
	    case TRACE:
		return false;
	    default:
		return true;
	}
    }


    private static int methodNum(String method)
    {
	if (method.equals("GET"))
	    return GET;
	if (method.equals("POST"))
	    return POST;
	if (method.equals("HEAD"))
	    return HEAD;
	if (method.equals("PUT"))
	    return PUT;
	if (method.equals("DELETE"))
	    return DELETE;
	if (method.equals("OPTIONS"))
	    return OPTIONS;
	if (method.equals("TRACE"))
	    return TRACE;

	return UNKNOWN;
    }


    /**
     * produces a string.
     * @return a string containing the IdempotentSequence
     */
    public String toString()
    {
	return getClass().getName();
    }
}

