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
 * Title:       TransferEncoding
 * Description: HTTPClient Transfer Encoding module that does not allow compression
 *              Based on and borrowing code from HTTPClient.TransferEncodingModule
 * Copyright:   Copyright (C) 2004 Regents of the University of California. All rights reserved.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        06 February 2004
 */

package edu.uci.ics.DAVExplorer;

import HTTPClient.TransferEncodingModule;
import HTTPClient.HttpHeaderElement;
import HTTPClient.ChunkedInputStream;
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


public class TransferEncoding extends TransferEncodingModule
{
    /**
     * Invoked by the HTTPClient.
     */
    public int requestHandler(Request req, Response[] resp)
        throws ModuleException
    {
        int retval = super.requestHandler( req, resp );
        
        // Parse TE header
        int idx;
        NVPair[] hdrs = req.getHeaders();
        for (idx=0; idx<hdrs.length; idx++)
            if (hdrs[idx].getName().equalsIgnoreCase("TE"))
                break;

        Vector pte;
        if (idx == hdrs.length)
        {
            // shouldn't happen, since the superclass creates it
            return REQ_CONTINUE;
        }
        else
        {
            try
            {
                pte = Util.parseHeader(hdrs[idx].getValue());
            }
            catch (ParseException pe)
            {
                throw new ModuleException(pe.toString());
            }
        }


        // done if "*;q=1.0" present
 
        HttpHeaderElement all = Util.getElement(pte, "*");
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
                throw new ModuleException("Invalid q value for \"*\" in TE " +
                          "header: ");

            try
            {
                if (Float.valueOf(params[idx].getValue()).floatValue() > 0.)
                    return retval;
            }
            catch (NumberFormatException nfe)
            {
                throw new ModuleException("Invalid q value for \"*\" in TE " +
                          "header: " + nfe.getMessage());
            }
        }
 

        // Don't allow gzip, deflate, and compress tokens in the TE header
        if (pte.contains(new HttpHeaderElement("deflate")))
            pte.removeElement(new HttpHeaderElement("deflate"));
        if (pte.contains(new HttpHeaderElement("gzip")))
            pte.removeElement(new HttpHeaderElement("gzip"));
        if (pte.contains(new HttpHeaderElement("compress")))
            pte.removeElement(new HttpHeaderElement("compress"));

        hdrs[idx] = new NVPair("TE", Util.assembleHeader(pte));

        return retval;
    }


    /**
     * Invoked by the HTTPClient.
     */
    public void responsePhase3Handler(Response resp, RoRequest req)
        throws IOException, ModuleException
    {
        String te = resp.getHeader("Transfer-Encoding");
        if (te == null  ||  req.getMethod().equals("HEAD"))
            return;
    
        Vector pte;
        try
        { pte = Util.parseHeader(te); }
        catch (ParseException pe)
        { throw new ModuleException(pe.toString()); }
    
        while (pte.size() > 0)
        {
            String encoding = ((HttpHeaderElement) pte.lastElement()).getName();
            // only allow chunked and identity transfer encoding
            if (encoding.equalsIgnoreCase("chunked"))
            {
                Log.write(Log.MODS, "TEM:   pushing chunked-input-stream");
                resp.inp_stream = new ChunkedInputStream(resp.inp_stream);
            }
            else if (encoding.equalsIgnoreCase("identity"))
            {
                Log.write(Log.MODS, "TEM:   ignoring 'identity' token");
            }
            else
            {
                Log.write(Log.MODS, "TEM:   Unknown transfer encoding '" +
                            encoding + "'");
                break;
            }
    
            pte.removeElementAt(pte.size()-1);
        }
    
        if (pte.size() > 0)
            resp.setHeader("Transfer-Encoding", Util.assembleHeader(pte));
        else
            resp.deleteHeader("Transfer-Encoding");
    }
}
