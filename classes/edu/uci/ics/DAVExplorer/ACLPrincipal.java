/*
 * Copyright (C) 2004 Regents of the University of California.
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
package edu.uci.ics.DAVExplorer;

import java.util.Vector;

/**
 * Title:       
 * Description: 
 * Copyright:   Copyright (c) 2004 Regents of the University of California. All rights reserved.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        
 */
public class ACLPrincipal
{
    /**
     * 
     */
    public ACLPrincipal( String principal )
    {
        this.principal = principal;
    }

    
    public void addGrant( String privilege )
    {
        if( !grant.contains( privilege ))
            grant.add( privilege );
    }
    
    
    public void addDeny( String privilege )
    {
        if( !deny.contains( privilege ))
            deny.add( privilege );
    }
    

    public String getPrincipal()
    {
        return principal;
    }
    
    
    public Vector getGrant()
    {
        return grant;
    }
    
    
    public Vector getDeny()
    {
        return deny;
    }
    
    
    private String principal;
    private Vector grant = new Vector();
    private Vector deny = new Vector();
}
