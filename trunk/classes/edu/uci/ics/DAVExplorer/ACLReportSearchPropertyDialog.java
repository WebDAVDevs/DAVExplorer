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
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Title:       
 * Description: 
 * Copyright:   Copyright (c) 2005 Regents of the University of California. All rights reserved.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        
 */
public class ACLReportSearchPropertyDialog extends JDialog
implements ActionListener, ChangeListener, ListSelectionListener, WebDAVCompletionListener
{
    public ACLReportSearchPropertyDialog( String resource )
    {
        super( GlobalData.getGlobalData().getMainFrame(), true );
        init( resource );
        pack();
        setSize( getPreferredSize() );
        GlobalData.getGlobalData().center( this );
        show();
    }


    /**
     * Initialization
     * 
     * @param resource
     * @param node
     */
    protected void init( String resource )
    {
        GlobalData.getGlobalData().setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );
        this.resource = resource;
        setTitle("Select Search Criteria");
        ((Main)GlobalData.getGlobalData().getMainFrame()).addWebDAVCompletionListener(this);

        JLabel label = new JLabel( this.resource, JLabel.CENTER );
        label.setForeground(Color.black);
        getContentPane().add( "North", label );

        okButton = new JButton("OK");
        okButton.addActionListener(this);
        okButton.setMnemonic( KeyEvent.VK_O );
        okButton.setEnabled( false );
        cancelButton  = new JButton("Cancel");
        cancelButton.addActionListener(this);
        cancelButton.setMnemonic( KeyEvent.VK_C );
        buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        getRootPane().setDefaultButton( cancelButton );
        cancelButton.grabFocus();

        getContentPane().add( "South", buttonPanel );
        setBackground(Color.lightGray);

        JTabbedPane tabbedPane = new JTabbedPane();
        JPanel panel1 = makeCriteriaPanel();
        tabbedPane.addTab( "Search Criteria", panel1 );
        tabbedPane.setMnemonicAt( 0, KeyEvent.VK_S );
        JPanel panel2 = makePropertiesPanel();
        tabbedPane.addTab( "Properties", panel2 );
        tabbedPane.setMnemonicAt( 1, KeyEvent.VK_P );
        getContentPane().add( "Center", tabbedPane );

        addWindowListener(
            new WindowAdapter()
            {
                public void windowClosing(WindowEvent we_Event)
                {
                    close( true );
                }
            });
        GlobalData.getGlobalData().resetCursor();
    }


    protected JPanel makePrincipalPanel()
    {
        Vector entries = new Vector();
        JPanel panel = new JPanel(false);
        if( addACL )
        {
            String prefix;
            if( GlobalData.getGlobalData().getSSL() )
                prefix =  GlobalData.WebDAVPrefixSSL;
            else
                prefix = GlobalData.WebDAVPrefix;
            ACLRequestGenerator generator = (ACLRequestGenerator)ACLResponseInterpreter.getGenerator();
            generator.setResource( prefix+resource, null );
            waiting = true;
            generator.GetPrincipalCollections();
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
            if( interpreter != null )
            {
                Vector principals = interpreter.getPrincipalCollectionSet();
                if( principals != null )
                {
                    for( int i=0; i < principals.size(); i++ )
                    {
                        interpreter = null;
                        generator.setResource( prefix+hostname+(String)principals.get(i), null );
                        waiting = true;
                        generator.GetPrincipalNames();
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
                        if( interpreter != null && interpreter.getPrincipalNames() != null )
                            entries.addAll( interpreter.getPrincipalNames() );
                    }
                }
            }
        }
        else
        {
            entries.add( principal );
        }
        JComboBox combo = new JComboBox( entries );
        combo.setRenderer( 
            new DefaultListCellRenderer()
            {
                public Component getListCellRendererComponent(
                        JList list,
                        Object value,
                        int index,
                        boolean isSelected,
                        boolean cellHasFocus)
                    {
                        setText( ((String[])value)[1] );
                        setBackground(isSelected ? Color.gray : Color.lightGray);
                        setForeground(Color.black);
                        return this;
                    }
            });

        combo.setEditable( false );
        combo.setActionCommand( "Principal" );
        combo.addActionListener( this );
        principal = (String[])combo.getSelectedItem();
        principalType = ACLNode.HREF;

        JLabel label = new JLabel( "Principal: " );
        label.setHorizontalAlignment( JLabel.RIGHT );
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        panel.setLayout( gridbag );
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.gridwidth = 1;
        gridbag.setConstraints( label, c );
        panel.add( label );
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints( combo, c );
        panel.add( combo );
        JLabel hrefLabel = new JLabel( "href: " );
        hrefLabel.setHorizontalAlignment( JLabel.RIGHT );
        c.gridwidth = 1;
        c.ipady = 10;
        c.anchor = GridBagConstraints.CENTER;
        gridbag.setConstraints( hrefLabel, c );
        panel.add( hrefLabel );
        href = new JLabel( principal[0] );
        href.setHorizontalAlignment( JLabel.LEFT );
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints( href, c );
        panel.add( href );
        return panel;
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
        okButton.setEnabled( (privileges!=null) && changed );
    }


    /**
     * 
     * @param e
     */
    public void actionPerformed(ActionEvent e)
    {
        if( e.getActionCommand().equals("OK") )
        {
            close( false );
        }
        else if( e.getActionCommand().equals("Change") )
        {
        }
        else if( e.getActionCommand().equals("Cancel") )
        {
            close( true );
        }
    }


    /**
     * 
     * @param e
     */
    public void valueChanged(ListSelectionEvent e)
    {
    }


    /**
     * 
     * @param e
     */
    public void completion( WebDAVCompletionEvent e )
    {
        if( waiting )
        {
            interpreter = (ACLResponseInterpreter)e.getSource();
            synchronized( this )
            {
                waiting = false;
                notify();
            }
        }
    }


    /**
     * 
     * @param cancel
     */
    protected void close( boolean cancel )
    {
        setVisible(false);
        canceled = cancel;
    }


    protected String resource;
    protected JPanel buttonPanel;
    protected JButton okButton;
    protected JButton cancelButton;
    protected JList privilegesList;
    protected JLabel href;
    protected boolean changed = false;
    protected boolean waiting;
    protected boolean canceled;
    protected ACLResponseInterpreter interpreter;
}
