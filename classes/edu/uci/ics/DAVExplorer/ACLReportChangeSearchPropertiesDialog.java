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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


/**
 * Title:       Report property change dialog
 * Description: Dialog to select data for some ACL reports
 * Copyright:   Copyright (c) 2005 Regents of the University of California. All rights reserved.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         15 Feb 2005
 */
public class ACLReportChangeSearchPropertiesDialog extends
        ACLReportPropertiesDialog implements DocumentListener
{
    /**
     * Constructor
     * 
     * @param resource
     *      the resource this dialog shows
     * @param showMatch
     *      true if the match textfield is shown
     */
    public ACLReportChangeSearchPropertiesDialog( String resource, boolean showMatch )
    {
        super( resource, "Select Search Criteria", true, false );
        this.showMatch = showMatch;
        init( resource, hostname, selected, "Select Search Criteria", true );
        pack();
        setSize( getPreferredSize() );
        GlobalData.getGlobalData().center( this );
        show();
    }


    /**
     * Modify the dialog panel created by the superclass.
     * 
     * @param panel
     *      the panel
     */
    protected void changePanel( JPanel panel )
    {
        if( showMatch )
        {
            JLabel sepLabel = new JLabel( " " );
            sepLabel.setHorizontalAlignment( JLabel.RIGHT );
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1.0;
            c.gridwidth = GridBagConstraints.REMAINDER;
            GridBagLayout gridbag = (GridBagLayout)panel.getLayout();
            gridbag.setConstraints( sepLabel, c );
            panel.add( sepLabel );
            JLabel matchLabel = new JLabel( "Match: " );
            matchLabel.setHorizontalAlignment( JLabel.RIGHT );
            c.gridwidth = 1;
            gridbag.setConstraints( matchLabel, c );
            panel.add( matchLabel );
            match = new JTextField();
            match.getDocument().addDocumentListener( this );
            match.setActionCommand( "match" );
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1.0;
            c.gridwidth = GridBagConstraints.REMAINDER;
            gridbag.setConstraints( match, c );
            panel.add( match );
        }
        super.changePanel( panel );
    }


    /**
     * From the DocumentListener interface
     * Gives notification that there was an insert into the match textfield.
     *
     * @param e
     *      the document event
     */
    public void insertUpdate( DocumentEvent e )
    {
        okButton.setEnabled( match.getText().length() > 0 );
    }


    /**
     * From the DocumentListener interface
     * Gives notification that there was data removed from the match textfield.
     *
     * @param e
     *      the document event
     */
    public void removeUpdate( DocumentEvent e )
    {
        okButton.setEnabled( match.getText().length() > 0 );
    }


    /**
     * From the DocumentListener interface
     * Gives notification that the match textfield changed.
     *
     * @param e
     *      the document event
     */
    public void changedUpdate( DocumentEvent e )
    {
        okButton.setEnabled( match.getText().length() > 0 );
    }


    /**
     * Act on changes to the dialog, i.e., enable or disable the Save button
     */
    public void setChanged()
    {
        changed = true;
        boolean enableOk = (selected.size() > 0) && changed;
        if( showMatch )
            enableOk = enableOk && (match.getText().length() > 0);
        okButton.setEnabled( enableOk );
    }


    /**
     * Returns the value in the match textfield.
     * 
     * @return
     *      the value of the match textfield
     */
    public String getMatch()
    {
        if( showMatch )
            return match.getText();
        return null;
    }


    protected JTextField match;
    protected boolean showMatch;
}
