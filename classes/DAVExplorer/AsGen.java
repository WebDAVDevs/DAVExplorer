/*
 * Copyright (c) 1999-2001 Regents of the University of California.
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
// This class is used to generate namespace aliases within
// an xml document.
//
// Version: 0.1
// Author:  Robert Emmery
// Date:    2/28/98
////////////////////////////////////////////////////////////////
// The code has been modified to include povisions for the final
// WebDAV xml namespaces.  A small number of program errors have
// been corrected.
//
// Please use the following contact:
//
// dav-exp@ics.uci.edu
//
// Version: 0.4
// Changes by: Yuzo Kanomata and Joe Feise
// Date: 3/17/99
//
// Change List:


package DAVExplorer;

class AsNode
{
    public AsNode( String schema, String alias, AsNode next )
    {
        m_schema = schema;
        m_alias = alias;
        if( next != null )
            next.m_next = this;
        m_prev = next;
    }

    public String getAlias()
    {
        return m_alias;
    }

    public String getSchema()
    {
        return m_schema;
    }

    public boolean isAttributeSet()
    {
        return m_attributeSet;
    }

    public void setAttribute()
    {
        m_attributeSet = true;
    }

    public AsNode getNext()
    {
        return m_next;
    }

    public AsNode getPrev()
    {
        return m_prev;
    }

    AsNode m_next = null;
    AsNode m_prev = null;
    String m_schema;
    String m_alias;
    boolean m_attributeSet = false;
};


public class AsGen
{
    public AsGen()
    {
    }

    protected AsGen( AsNode node )
    {
        if( m_first == null )
            m_first = node;
        if( m_last == null )
            m_last = node;
        m_current = node;
    }

    public void createNamespace( String schema )
    {
        AsNode node = new AsNode( schema, getNextAs(), m_last );
        m_last = node;
        if( m_first == null )
            m_first = node;
        m_current = node;
    }

    public AsGen getNext()
    {
        if( m_current == null )
            return null;
        else
            m_current = m_current.getNext();
        return new AsGen( m_current );
    }

    public AsGen getPrev()
    {
        if( m_current == null )
            return null;
        else
            m_current = m_current.getPrev();
        return new AsGen( m_current );
    }

    public AsGen getFirst()
    {
        if( m_first == null )
            return null;
        else
            return new AsGen( m_first );
    }

    public String getAlias()
    {
        if( m_current == null )
            return null;
        else
            return m_current.getAlias();
    }

    public String getSchema()
    {
        if( m_current == null )
            return null;
        else
            return m_current.getSchema();
    }

    public boolean isAttributeSet()
    {
        if( m_current == null )
            return false;
        else
            return m_current.isAttributeSet();
    }

    public void setAttribute()
    {
        // set when the namespace is declared
        if( m_current != null )
            m_current.setAttribute();
    }

    public static void clear()
    {
        m_current = m_first = m_last = null;
        m_lastGenerated = "@";
    }

    private String getNextAs()
    {
        String str = m_lastGenerated;

        int len = str.length();
        byte[] byte_str = str.getBytes();
        if (str.endsWith("Z"))
        {
            byte_str[len-1] = (byte)'A';
            boolean found = false;
            boolean append = true;
            int i = len-2;
            while( (i>=0) && (!found) )
            {
                if (byte_str[i] != 'Z') {
                    append = false;
                    found = true;
                    byte_str[i]++;
                }
                else
                {
                    byte_str[i] = (byte)'A';
                }
                i--;
            }
            str = new String(byte_str);
            if (append)
                str += 'A';
        }
        else
        {
            byte_str[len-1]++;
            str = new String(byte_str);
        }
        m_lastGenerated = str;
        return str;
    }

    private static AsNode m_first;
    private static AsNode m_last;
    private static AsNode m_current;
    private static String m_lastGenerated = "@";
}
