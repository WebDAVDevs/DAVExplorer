/*
 * Copyright (c) 2004 Regents of the University of California.
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
 * Title:       ContentEncoding
 * Description: HTTPClient Content Encoding module that does not allow compression
 *              Based on and borrowing code from HTTPClient.ContentEncodingModule
 * Copyright:   Copyright (C) 2004 Regents of the University of California. All rights reserved.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        06 February 2004
 */
package edu.uci.ics.DAVExplorer;

import HTTPClient.ContentEncodingModule;
import HTTPClient.HttpHeaderElement;
import HTTPClient.Request;
import HTTPClient.RoRequest;
import HTTPClient.Response;
import HTTPClient.NVPair;
import HTTPClient.Util;
import HTTPClient.Log;
import HTTPClient.ModuleException;
import HTTPClient.ParseException;
import java.io.IOException;
import java.util.Vector;


public class ContentEncoding extends ContentEncodingModule
{
    /**
     * Invoked by the HTTPClient.
     */
    public int requestHandler(Request req, Response[] resp)
        throws ModuleException
    {
        int retval = super.requestHandler( req, resp );

        // parse Accept-Encoding header
        int idx;
        NVPair[] hdrs = req.getHeaders();
        for (idx=0; idx<hdrs.length; idx++)
            if (hdrs[idx].getName().equalsIgnoreCase("Accept-Encoding"))
                break;
    
        Vector pae;
        if (idx == hdrs.length)
        {
            // shouldn't happen, since the superclass creates it
            return retval;
        }
        else
        {
            try
            {
                pae = Util.parseHeader(hdrs[idx].getValue());
            }
            catch (ParseException pe)
            {
                throw new ModuleException(pe.toString());
            }
        }
    
        // done if "*;q=1.0" present
        HttpHeaderElement all = Util.getElement(pae, "*");
        if (all != null)
        {
            NVPair[] params = all.getParams();
            for (idx=0; idx<params.length; idx++)
                if (params[idx].getName().equalsIgnoreCase("q"))
                    break;
    
            if (idx == params.length)   // no qvalue, i.e. q=1.0
                return retval;
    
            if (params[idx].getValue() == null  ||
            params[idx].getValue().length() == 0)
                throw new ModuleException("Invalid q value for \"*\" in " +
                          "Accept-Encoding header: ");
    
            try
            {
                if (Float.valueOf(params[idx].getValue()).floatValue() > 0.)
                    return retval;
            }
            catch (NumberFormatException nfe)
            {
                throw new ModuleException("Invalid q value for \"*\" in " +
                        "Accept-Encoding header: " + nfe.getMessage());
            }
        }
    
        // remove gzip, deflate and compress tokens from the Accept-Encoding header
        if (pae.contains(new HttpHeaderElement("deflate")))
            pae.removeElement(new HttpHeaderElement("deflate"));
        if (pae.contains(new HttpHeaderElement("gzip")))
            pae.removeElement(new HttpHeaderElement("gzip"));
        if (pae.contains(new HttpHeaderElement("x-gzip")))
            pae.removeElement(new HttpHeaderElement("x-gzip"));
        if (pae.contains(new HttpHeaderElement("compress")))
            pae.removeElement(new HttpHeaderElement("compress"));
        if (pae.contains(new HttpHeaderElement("x-compress")))
            pae.removeElement(new HttpHeaderElement("x-compress"));
    
        hdrs[idx] = new NVPair("Accept-Encoding", Util.assembleHeader(pae));
    
        return retval;
    }


    /**
     * Invoked by the HTTPClient.
     */
    public void responsePhase3Handler(Response resp, RoRequest req)
        throws IOException, ModuleException
    {
        String ce = resp.getHeader("Content-Encoding");
        if (ce == null  ||  req.getMethod().equals("HEAD")  ||
            resp.getStatusCode() == 206)
            return;
    
        Vector pce;
        try
            { pce = Util.parseHeader(ce); }
        catch (ParseException pe)
            { throw new ModuleException(pe.toString()); }
    
        if (pce.size() == 0)
            return;
    
        String encoding = ((HttpHeaderElement) pce.firstElement()).getName();
        // Only allow identity content encoding
        if (encoding.equalsIgnoreCase("identity"))
        {
            // just let the superclass handle it
            super.responsePhase3Handler( resp, req );
            return;
        }
        else
        {
            Log.write(Log.MODS, "CEM:   Unknown content encoding '" +
                    encoding + "'");
        }
    
        if (pce.size() > 0)
            resp.setHeader("Content-Encoding", Util.assembleHeader(pce));
        else
            resp.deleteHeader("Content-Encoding");
    }
}
