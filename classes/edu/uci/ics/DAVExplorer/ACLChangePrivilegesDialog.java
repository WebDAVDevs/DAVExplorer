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
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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
public class ACLChangePrivilegesDialog extends JDialog
implements ActionListener, ChangeListener, ListSelectionListener, WebDAVCompletionListener
{

    public ACLChangePrivilegesDialog( String resource, String hostname, Vector selected )
    {
        super( GlobalData.getGlobalData().getMainFrame(), true );
        init( resource, hostname, selected, "Edit Privileges" );
        pack();
        setSize( getPreferredSize() );
        GlobalData.getGlobalData().center( this );
        show();
    }


    public ACLChangePrivilegesDialog( String resource, String hostname, Vector selected, String title )
    {
        super( GlobalData.getGlobalData().getMainFrame(), true );
        init( resource, hostname, selected, title );
        pack();
        setSize( getPreferredSize() );
        GlobalData.getGlobalData().center( this );
        show();
    }


    protected void init( String resource, String hostname, Vector selected, String title )
    {
        GlobalData.getGlobalData().setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );
        this.hostname = hostname;
        this.resource = resource;
        if( selected == null )
            this.selected = new Vector();
        else
            this.selected = new Vector( selected );
        available = new Vector();
        setTitle( title );
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
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        getRootPane().setDefaultButton( okButton );
        cancelButton.grabFocus();
        getContentPane().add( "South", buttonPanel );
        setBackground(Color.lightGray);

        JPanel panel = makePanel();
        getContentPane().add( "Center", panel );

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


    /**
     * 
     * @return
     */
    public boolean isCanceled()
    {
        return canceled;
    }


    /**
     * 
     * @param e
     */
    public void stateChanged( ChangeEvent e )
    {
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
        else if( e.getActionCommand().equals("Cancel") )
        {
            close( true );
        }
        else if( e.getActionCommand().equals("=>") )
        {
            DefaultListModel privModel = (DefaultListModel)privList.getModel();
            DefaultListModel curModel = (DefaultListModel)curList.getModel();
            int[] indices = privList.getSelectedIndices();
            for( int i=indices.length-1; i>=0; i-- )
            {
                Object obj = privModel.getElementAt( indices[i] );
                curModel.addElement( obj );
                selected.add( obj.toString() );
                privModel.remove( indices[i] );
                setChanged( true );
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
                selected.remove( obj.toString() );
                setChanged( true );
            }
        }
    }


    /**
     * 
     * @param e
     */
    public void valueChanged(ListSelectionEvent e)
    {
        if( e.getSource() == privList )
        {
            rightButton.setEnabled( !privList.isSelectionEmpty() );
        }
        else
        {
            leftButton.setEnabled( !curList.isSelectionEmpty() );
        }
    }


    /**
     * 
     * @param enable
     */
    public void setChanged( boolean enable )
    {
        changed = enable;
        okButton.setEnabled( changed );
    }


    /**
     * 
     * @param e
     */
    public void completion( WebDAVCompletionEvent e )
    {
        interpreter = (ACLResponseInterpreter)e.getSource();
        synchronized( this )
        {
            waiting = false;
            notify();
        }
    }


    public Vector getSelected()
    {
        return selected;
    }


    protected String getPanelTitle()
    {
        return  "Privileges";
    }


    protected JPanel makePanel()
    {
        JPanel panel = new JPanel(false);
        getAvailable();
        DefaultListModel model = new DefaultListModel();
        for( int i=0; i<available.size(); i++ )
        {
            if( !selected.contains( available.get(i) ) )
                    model.addElement( available.get(i) );
        }
        privList = new JList( model );
        privList.setFixedCellWidth(200);    // wild guess, but looks ok here
        JScrollPane privScroll = new JScrollPane();
        privScroll.setViewportView( privList );
        privList.addListSelectionListener( this );
        model = new DefaultListModel();
        for( int i=0; i<selected.size(); i++ )
        {
            model.addElement( selected.get(i) );
        }
        curList = new JList( model ); 
        curList.setFixedCellWidth(200);
        JScrollPane curScroll = new JScrollPane();
        curScroll.setViewportView( curList );
        curList.addListSelectionListener( this );

        JLabel topLabel = new JLabel( getPanelTitle() );
        topLabel.setHorizontalAlignment( JLabel.CENTER );
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        panel.setLayout( gridbag );
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints( topLabel, c );
        panel.add( topLabel );
        JLabel leftLabel = new JLabel( "Available" );
        topLabel.setHorizontalAlignment( JLabel.CENTER );
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.gridwidth = 1;
        gridbag.setConstraints( leftLabel, c );
        panel.add( leftLabel );
        JLabel rightLabel = new JLabel( "Selected" );
        topLabel.setHorizontalAlignment( JLabel.CENTER );
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.gridx = 2;
        c.gridwidth = 1;
        gridbag.setConstraints( rightLabel, c );
        panel.add( rightLabel );

        c.gridx = 0;
        c.gridwidth = 1;
        gridbag.setConstraints( privScroll, c );
        panel.add( privScroll );

        rightButton = new JButton("=>");
        rightButton.addActionListener(this);
        rightButton.setMnemonic( KeyEvent.VK_GREATER );
        rightButton.setEnabled( false );
        leftButton  = new JButton("<=");
        leftButton.addActionListener(this);
        leftButton.setMnemonic( KeyEvent.VK_LESS );
        leftButton.setEnabled( false );
        JPanel arrowPanel = new JPanel();
        GridBagLayout arrowGridbag = new GridBagLayout();
        arrowPanel.setLayout( arrowGridbag );
        c.gridx = 0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.NONE;
        arrowGridbag.setConstraints( rightButton, c );
        arrowPanel.add( rightButton );
        arrowGridbag.setConstraints( rightButton, c );
        arrowPanel.add( leftButton );

        c.gridx = 1;
        c.gridy = 2;
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.RELATIVE;
        gridbag.setConstraints( arrowPanel, c );
        panel.add( arrowPanel );

        c.gridx = 2;
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints( curScroll, c );
        panel.add( curScroll );
        changePanel( panel );
        return panel;
    }


    protected void changePanel( JPanel panel )
    {
    }


    protected void getAvailable()
    {
        String prefix;
        if( GlobalData.getGlobalData().getSSL() )
            prefix =  GlobalData.WebDAVPrefixSSL;
        else
            prefix = GlobalData.WebDAVPrefix;
        ACLRequestGenerator generator = (ACLRequestGenerator)ACLResponseInterpreter.getGenerator();
        generator.setResource( prefix+resource, null );
        waiting = true;
        generator.GetSupportedPrivilegeSet();
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
        if( interpreter != null && interpreter.getSupportedPrivilegeSet() != null )
            available.addAll( interpreter.getSupportedPrivilegeSet() );
    }


    protected void close( boolean cancel )
    {
        setVisible(false);
        canceled = cancel;
    }


    protected String hostname;
    protected String resource;
    protected JList privList;
    protected JList curList;
    protected JButton okButton;
    protected JButton cancelButton;
    protected JButton leftButton;
    protected JButton rightButton;
    protected boolean changed = false;
    protected boolean waiting;
    protected boolean canceled;
    protected String principal;
    protected int principalType;
    protected Vector available;
    protected Vector selected;
    protected boolean grant;
    protected ACLResponseInterpreter interpreter;
    protected String panelTitle;
}
