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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;


/**
 * Title:      Report properties dialog 
 * Description: Dialog to select data for some ACL reports
 * Copyright:   Copyright (c) 2005 Regents of the University of California. All rights reserved.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         11 Feb 2005
 */
public class ACLReportPropertiesDialog extends ACLChangePrivilegesDialog
{
    /**
     * Constructor
     * 
     * @param resource
     *      the resource the privileges are applied to
     * @param flag
     *      a general-purpose flag, useful for derived classes
     */
    public ACLReportPropertiesDialog( String resource, boolean flag )
    {
        super( resource, null, null, "Select Properties", flag );
    }


    /**
     * Constructor
     * 
     * @param resource
     *      the resource the privileges are applied to
     * @param title
     *      the dialog title
     * @param flag
     *      a general-purpose flag, useful for derived classes
     */
    public ACLReportPropertiesDialog( String resource, String title, boolean flag )
    {
        super( resource, null, null, title, flag );
    }


    /**
     * Returns the name of the panel.
     * 
     * @return
     *      the name of the panel
     */
    protected String getPanelTitle()
    {
        return  "Properties";
    }


    /**
     * From the ActionListener interface.
     * Handles user actions, i.e., button clicks.
     * 
     * @param e
     *      the event describing the action
     */
    public void actionPerformed( ActionEvent e )
    {
        if( e.getActionCommand().equals("=>") )
        {
            DefaultListModel privModel = (DefaultListModel)privList.getModel();
            DefaultListModel curModel = (DefaultListModel)curList.getModel();
            int[] indices = privList.getSelectedIndices();
            for( int i=indices.length-1; i>=0; i-- )
            {
                Object obj = privModel.getElementAt( indices[i] );
                curModel.addElement( obj );
                selected.add( obj );
                privModel.remove( indices[i] );
                setChanged();
            }
        }
        else if( e.getActionCommand().equals("<=") )
        {
            DefaultListModel privModel = (DefaultListModel)privList.getModel();
            DefaultListModel curModel = (DefaultListModel)curList.getModel();
            int[] indices = curList.getSelectedIndices();
            for( int i=indices.length-1; i>=0; i-- )
            {
                Object obj = curModel.getElementAt( indices[i] );
                privModel.addElement( obj );
                curModel.remove( indices[i] );
                selected.remove( obj );
                setChanged();
            }
        }
        else
            super.actionPerformed( e );
    }


    /**
     * Get the available property names from the server
     */
    protected void getAvailable()
    {
        String prefix = "";
        if( GlobalData.getGlobalData().getSSL() )
        {
            if( !resource.startsWith( GlobalData.WebDAVPrefixSSL ) )
                prefix =  GlobalData.WebDAVPrefixSSL;
        }
        else
        {
            if( !resource.startsWith( GlobalData.WebDAVPrefix ) )
                prefix = GlobalData.WebDAVPrefix;
        }
        ACLRequestGenerator generator = (ACLRequestGenerator)ACLResponseInterpreter.getGenerator();
        generator.setResource( prefix+resource, null );
        waiting = true;
        generator.GetPropertyNames();
        try
        {
            synchronized( this )
            {
                wait(30000);
            }
        }
        catch( InterruptedException e )
        {
        }
        if( interpreter != null && interpreter.getPropertyNames() != null )
            available.addAll( interpreter.getPropertyNames() );
    }


    /**
     * Modify the dialog panel created by the superclass.
     * Changes the renderers.
     * 
     * @param panel
     *      the panel
     */
    protected void changePanel( JPanel panel )
    {
        privList.setCellRenderer(
            new DefaultListCellRenderer()
            {
                public Component getListCellRendererComponent(
                        JList list,
                        Object value,
                        int index,
                        boolean isSelected,
                        boolean cellHasFocus)
                    {
                        setText( ((String[])value)[0] );    // property name
                        setBackground( isSelected ? Color.black : Color.white );
                        setForeground( isSelected ? Color.white : Color.black );
                        return this;
                    }
            });
        curList.setCellRenderer(
            new DefaultListCellRenderer()
            {
                public Component getListCellRendererComponent(
                        JList list,
                        Object value,
                        int index,
                        boolean isSelected,
                        boolean cellHasFocus)
                    {
                        setText( ((String[])value)[0] );    // property name
                        setBackground( isSelected ? Color.black : Color.white );
                        setForeground( isSelected ? Color.white : Color.black );
                        return this;
                    }
            });
    }
}
