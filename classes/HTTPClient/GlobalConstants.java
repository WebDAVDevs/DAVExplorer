/*
 * @(#)GlobalConstants.java				0.3 30/01/1998
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
 * This interface defines various global constants.
 *
 * @version	0.3  30/01/1998
 * @author	Ronald Tschal&auml;r
 * @since	V0.3
 */

interface GlobalConstants
{
    /** Debug variables */
    boolean DebugAll   = false;
    boolean DebugConn  = DebugAll | false;
    boolean DebugResp  = DebugAll | false;
    boolean DebugDemux = DebugAll | false;
    boolean DebugAuth  = DebugAll | false;
    boolean DebugSocks = DebugAll | false;
    boolean DebugMods  = DebugAll | false;
    boolean DebugURLC  = DebugAll | false;

    /** possible http protocols we (might) handle */
    int     HTTP       = 0; 	// plain http
    int     HTTPS      = 1; 	// http on top of SSL
    int     SHTTP      = 2; 	// secure http
    int     HTTP_NG    = 3; 	// http next-generation

    /** some known http versions */
    int     HTTP_1_0   = (1 << 16) + 0;
    int     HTTP_1_1   = (1 << 16) + 1;

    /** Content delimiters */
    int     CL_HDRS    = 0; 	// still reading/parsing headers
    int     CL_0       = 1; 	// no body
    int     CL_CLOSE   = 2; 	// by closing connection
    int     CL_CONTLEN = 3; 	// via the Content-Length header
    int     CL_CHUNKED = 4; 	// via chunked transfer encoding
    int     CL_MP_BR   = 5; 	// via multipart/byteranges
}

