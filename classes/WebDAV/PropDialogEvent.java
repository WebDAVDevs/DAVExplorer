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

package WebDAV;

public class PropDialogEvent extends java.util.EventObject
{
    protected String data;
    protected String initialData;
    protected String HostName;
    protected String ResourceName;

    public PropDialogEvent(Object source, String host, String resource, String initial, String new_data)
    {
        super(source);
        ResourceName = resource;
        HostName = host;
        data = new_data;
        initialData = initial;
    }
    
    public String getHost()
    {
        return HostName;
    }
    
    public String getResource()
    {
        return ResourceName;
    }
    
    public String getData()
    {
        return data;
    }
    
    public String getInitialData()
    {
        return initialData;
    }
}
