/*
 * Copyright (C) 2005 Regents of the University of California.
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

/**
 * Title:       ACL Privilege
 * Description: Describes a privilege for access control
 * Copyright:   Copyright (c) 2005 Regents of the University of California. All rights reserved.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         6 July 2005
 */
public class ACLPrivilege
{
    /**
     * Default constructor
     */
    public ACLPrivilege()
    {
    }


    /**
     * Constructor
     * 
     * @param privilege
     *      the name of the privilege
     * @param principalns
     *      the namespace of the privilege
     */
    public ACLPrivilege(String privilege, String ns)
    {
        this.privilege = privilege;
        this.ns = ns;
    }


    /**
     * Returns the privilege name.
     *  
     * @return
     *      the privilege name
     */
    public String getPrivilege()
    {
        return privilege;
    }


    /**
     * Set the privilege name.
     *  
     * @param privilege
     *      the privilege name
     */
    public void setPrivilege(String privilege)
    {
        this.privilege = privilege;
    }


    /**
     * Returns the namespace.
     *  
     * @return
     *      the namespace of this privilege
     */
    public String getNamespace()
    {
        return ns;
    }


    /**
     * Set the namespace of this privilege.
     *  
     * @param ns
     *      the namespace
     */
    public void setNamespace(String ns)
    {
        this.ns = ns;
    }


    private String privilege;
    private String ns;
}
