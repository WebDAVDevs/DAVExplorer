/*
 * Copyright (c) 1998-2003 Regents of the University of California.
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
 * Title:       URI Box
 * Description: This class creates an extension of JPanel which creates the
 *              URI entry box on the WebDAVExplorer.  This box contains
 *              the text field in which the user enters the dav server's URI.
 * Copyright:   Copyright (c) 1998-2003 Regents of the University of California. All rights reserved.
 * @author      Undergraduate project team ICS 126B 1998
 * @date        1998
 * @author      Yuzo Kanomata, Joachim Feise (dav-exp@ics.uci.edu)
 * @date        17 March 1999
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        12 January 2001
 * Changes:     Added support for https (SSL)
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        1 October 2001
 * Changes:     Change of package name
 * @author      Joachim Feise (dav-exp@ics.uci.edu), Karen Schuchardt
 * @date        2 April 2002
 * Changes:     Incorporated Karen Schuchardt's changes to improve the loading of
 *              images. Thanks!
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        17 March 2003
 * Changes:     Integrated Brian Johnson's applet changes.
 *              Added better error reporting.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        7 April 2003
 * Changes:     Integrated Thoralf Rickert's change to combobox.
 *              Cleaned up.
 */

package edu.uci.ics.DAVExplorer;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Vector;
import javax.swing.JPanel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import java.awt.Dimension;


public class URIBox extends JPanel implements ActionListener
{
    private Vector URIBoxListener;
    private JComboBox urlField;
    private JLabel prefix;
    private JButton okButton;


    public URIBox()
    {
        super();
        JPanel panel = new JPanel();

        okButton = new JButton(GlobalData.getGlobalData().getImageIcon("connect.gif", "Connect"));
        okButton.setActionCommand("Connect");
        okButton.addActionListener(this);
        okButton.setToolTipText("Connect");

        panel.add(okButton);

        urlField = new JComboBox( (Vector)URIContainer.getInstance().getURIs().clone() );
        urlField.setEditable(true);
        Dimension d = urlField.getPreferredSize();
        d.width = 500;  // resonable width
        urlField.setPreferredSize(d);
        urlField.addActionListener(new EnterPressedListener());
        prefix = new JLabel();
        if( GlobalData.getGlobalData().getSSL() )
            prefix.setText( GlobalData.WebDAVPrefixSSL );
        else
            prefix.setText( GlobalData.WebDAVPrefix );
        prefix.setHorizontalAlignment( SwingConstants.RIGHT );
        prefix.setForeground( Color.black );

        panel.add(prefix);
        panel.add(urlField);

        add("Center", panel);
        URIBoxListener = new Vector();

        if (GlobalData.getGlobalData().hideURIBox())
        {
            super.setVisible(false);
        }
    }


    private ImageIcon loadImageIcon(String filename, String description)
    {
        try
        {
            return new ImageIcon(getClass().getResource("icons/" + filename),description);
        }
        catch (Exception ex)
        {
            GlobalData.getGlobalData().errorMsg("Toolbar:\nIcon load error." );
            return null;
        }
    }


    public void actionPerformed(ActionEvent evt)
    {
        notifyListener();
    }


    public void setText(String uri)
    {
        urlField.setSelectedItem(uri);
    }


    public String getText()
    {
        return urlField.getSelectedItem().toString().trim();
    }


    public synchronized void addActionListener(ActionListener l)
    {
        URIBoxListener.addElement(l);
    }


    /**
     * If we are say don't include then never let it be visible,
     * otherwise have it work normally.
     * @param visible
     */
    public void setVisible(boolean visible)
    {
        if (GlobalData.getGlobalData().hideURIBox())
        {
            return;
        }
        super.setVisible(visible);
    }


    public synchronized void removeActionListener(ActionListener l)
    {
        URIBoxListener.removeElement(l);
    }


    protected void notifyListener()
    {
        ActionEvent evt = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, getText());
        Vector v;
        synchronized(this)
        {
            v = (Vector)URIBoxListener.clone();
        }

        URIContainer.getInstance().addURI(getText());

        for( int i=0; i< v.size(); i++ )
        {
            WebDAVURIBoxListener client = (WebDAVURIBoxListener)v.elementAt(i);
            client.actionPerformed(evt);
        }
    }


    class EnterPressedListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            // ignore events resulting from changing the selection
            if( e.getActionCommand().compareToIgnoreCase("comboBoxChanged")!=0 )
                notifyListener();
        }
    }
}
