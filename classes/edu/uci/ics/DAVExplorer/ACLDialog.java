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
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.ms.xml.om.Element;

/**
 * Title:       ACL dialog
 * Description: Views or modifies ACLs for a specific resource.
 * Copyright:   Copyright (c) 2005 Regents of the University of California. All rights reserved.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        10 Feb 2005
 */
public class ACLDialog extends JDialog
    implements ActionListener, ChangeListener, ListSelectionListener, WebDAVCompletionListener
{
    /**
     * 
     * @param properties
     * @param resource
     * @param hostname
     * @param locktoken
     */
    public ACLDialog( Element properties, String resource, String hostname, String locktoken )
    {
        super( GlobalData.getGlobalData().getMainFrame() );
        model = new ACLModel( properties );
        init( model, properties, resource, hostname, locktoken );
        pack();
        setSize( getPreferredSize() );
        GlobalData.getGlobalData().center( this );
        show();
    }


    /**
     * 
     * @param model
     * @param properties
     * @param resource
     * @param hostname
     * @param locktoken
     */
    protected void init( ACLModel model, Element properties, String resource, String hostname, String locktoken )
    {
        setTitle("View/Modify ACLs");
        this.hostname = hostname;
        this.resource = hostname + resource;
        this.locktoken = locktoken;
        JLabel label = new JLabel( this.resource, JLabel.CENTER );
        label.setForeground(Color.black);
        getContentPane().add( "North", label );

        addButton = new JButton("Add Principal");
        addButton.addActionListener(this);
        deleteButton = new JButton("Delete Principal");
        deleteButton.addActionListener(this);
        deleteButton.setEnabled( false );
        saveButton = new JButton("Save");
        saveButton.addActionListener(this);
        closeButton  = new JButton("Close");
        closeButton.addActionListener(this);
        buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(closeButton);
        getRootPane().setDefaultButton( closeButton );
        closeButton.grabFocus();
        saveButton.setEnabled( false );

        getContentPane().add( "South", buttonPanel );
        setBackground(Color.lightGray);

        table = new JTable( model );
        table.getSelectionModel().addListSelectionListener(this);
        // the minimum number of rows to see, expand as necessary
        // table.setMinimumSize(...);

        JScrollPane scrollpane = new JScrollPane();
        scrollpane.setViewportView( table );
        getContentPane().add( "Center", scrollpane );

        ((Main)GlobalData.getGlobalData().getMainFrame()).addWebDAVCompletionListener(this);
        addWindowListener(
            new WindowAdapter()
            {
                public void windowClosing(WindowEvent we_Event)
                {
                    cancel();
                }
            });
        MouseListener ml = new MouseAdapter()
        {
            public void mouseClicked(MouseEvent e)
            {
                if(e.getClickCount() == 2)
                {
                    handleDoubleClick(e);
                }
            }
        };
        table.addMouseListener(ml);
    }

    
    /**
     * 
     * @param e
     */
    public void stateChanged( ChangeEvent e )
    {
        setChanged( true );
    }


    /**
     * 
     * @param enable
     */
    public void setChanged( boolean enable )
    {
        changed = enable;
        saveButton.setEnabled( changed );
        if( !changed )
            model.clear();
    }


    /**
     * 
     * @param e
     */
    public void actionPerformed(ActionEvent e)
    {
        if( e.getActionCommand().equals("Add Principal") )
        {
            add();
        }
        else if( e.getActionCommand().equals("Delete Principal") )
        {
            remove();
        }
        else if( e.getActionCommand().equals("Save") )
        {
            save();
        }
        else if( e.getActionCommand().equals("Close") )
        {
            cancel();
        }
    }


    /**
     * 
     * @param e
     */
    public void valueChanged(ListSelectionEvent e)
    {
        int row = table.getSelectedRow();
        if( row >= 0 )
        {
            deleteButton.setEnabled( !model.getRow(row).isInherited() && table.getSelectedRow()>=0 );
        }
    }


    /**
     * 
     * @param e
     */
    public void completion( WebDAVCompletionEvent e )
    {
        if( waiting && e.isSuccessful() )
            setChanged( false );  // disable save button
        waiting = false;
    }


    /**
     * 
     * @param e
     */
    public void handleDoubleClick( MouseEvent e )
    {
        GlobalData.methodEnter( "handleDoubleClick", "ACLDialog", GlobalData.getGlobalData().getDebugFileView() );

        Point pt = e.getPoint();
        int col = table.columnAtPoint(pt);
        int row = table.rowAtPoint(pt);

        int principalColumn = table.convertColumnIndexToView(0); // lock
        if( principalColumn == -1 )
            principalColumn = 1;     // use default
        if( (col == principalColumn) && (row != -1) )
        {
            ACLNode node = model.getRow( row );
            if( !node.isInherited() )
            {
                ACLAddDialog add = new ACLAddDialog( resource, hostname, node );
                if( !add.isCanceled() )
                {
                    // check if the contents of the privileges vector have changed
                    // disregard changes in ordering
                    Vector privs = (Vector)node.getPrivileges().clone();
                    Vector newprivs = add.getPrivileges();
                    privs.removeAll( newprivs );
                    if( !privs.isEmpty() )
                    {
                        // node has changed
                        node.setPrincipalType( add.getPrincipalType() );
                        node.setPrivileges( add.getPrivileges() );
                        setChanged( true );
                    }
                    if( add.getGrant() != node.getGrant() )
                    {
                        node.setGrant( add.getGrant() );
                        setChanged( true );
                    }
                }
            }
        }
    }


    /**
     * 
     */
    public void add()
    {
        ACLAddDialog add = new ACLAddDialog( resource, hostname );
        if( !add.isCanceled() )
        {
            model.addRow( add.getPrincipal(), add.getPrincipalType(), add.getPrivileges(), add.getGrant() );
            setChanged( true );
        }
    }


    /**
    *
    */
   public void remove()
   {
       String title = "Delete Principal";
       String text = "Do you really want to delete the selected principal?";
       if( ConfirmationDialog( title, text ) )
       {
           model.removeRow( table.getSelectedRow() );
           table.updateUI();
           setChanged( true );
       }
   }


   public void save()
   {
       Vector ACLs = new Vector();
       for( int i=0; i< model.getRowCount(); i++ )
       {
           ACLNode node = model.getRow( i );
           if( !node.isInherited() )
               ACLs.add( node );
       }
       ACLRequestGenerator generator = (ACLRequestGenerator)ACLResponseInterpreter.getGenerator();
       generator.GenerateACL( ACLs );
       waiting = true;
       generator.execute();
   }


    /**
     *
     */
    public void cancel()
    {
        setVisible(false);
        dispose();
    }


    /**
     * 
     * @param title
     * @param text
     * @return
     */
    protected boolean ConfirmationDialog( String title, String text )
    {
        int opt = JOptionPane.showConfirmDialog( GlobalData.getGlobalData().getMainFrame(), text, title, JOptionPane.YES_NO_OPTION );
        if (opt == JOptionPane.YES_OPTION)
            return true;
        return false;
    }


    protected JTable table;
    protected ACLModel model;
    protected JPanel buttonPanel;
    protected JButton addButton;
    protected JButton deleteButton;
    protected JButton saveButton;
    protected JButton closeButton;
    protected String hostname;
    protected String resource;
    protected String locktoken;
    protected boolean changed = false;
    protected boolean waiting;
}
