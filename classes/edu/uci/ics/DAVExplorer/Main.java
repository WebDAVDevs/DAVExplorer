/*
 * Copyright (c) 1998-2005 Regents of the University of California.
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
 * Title:       Main
 * Description: This is the class containing the main() function, which merely
 *              instantiates the Main JFrame.
 *              The Main class creates the user interface and adds the appropriate
 *              listeners.
 * Copyright:   Copyright (c) 1998-2005 Regents of the University of California. All rights reserved.
 * @author      Robert Emmery (dav-exp@ics.uci.edu)
 * @date        2 April 1998
 * @author      Yuzo Kanomata, Joachim Feise (dav-exp@ics.uci.edu)
 * @date        17 March 1999
 * Changes:     Changed treeView.fireSelectionEvent(); to treeView.initTree();
 *              This is the same function, but with a better name.
 *              Added Create Folder functionality.
 *              Added filename selection for export file
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        3 December 1999
 * Changes:     Removed the authentication dialog and listener, since authentication
 *              is now handled as AuthenticationHandler in HTTPClient
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        20 June 2000
 * Changes:     Better reporting in case the connection is closed
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        12 January 2001
 * Changes:     Added support for https (SSL), moved properties loading to GlobalData
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        2 August 2001
 * Changes:     Using HTTPClient authentication module
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        29 September 2001
 * Changes:     Now sending Options request at initial connection to check for
 *              DAV support on the server. Only then the Propfind is sent.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        1 October 2001
 * Changes:     Change of package name
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        22 November 2001
 * Changes:     Improved copy and move operations
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        2 April 2002
 * Changes:     Updated for JDK 1.4
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        17 March 2003
 * Changes:     Integrated Brian Johnson's applet changes.
 *              Added better error reporting.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        31 March 2003
 * Changes:     Integrated Thoralf Rickert's progress bar changes.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        27 April 2003
 * Changes:     Added shared lock functionality.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        23 September 2003
 * Changes:     Integrated the DeltaV code from the Spring 2003 ICS125 team.
 * @author      John Barton (HP), Joachim Feise (dav-exp@ics.uci.edu)
 * @date        06 February 2004
 * Changes:     Integrated John Barton's refactoring changes and drop support.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        08 February 2004
 * Changes:     Added Javadoc templates
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        15 February 2005
 * Changes:     Added support for the WebDAV Access Control Protocol (RFC 3744)
 */


package edu.uci.ics.DAVExplorer;

import HTTPClient.DefaultAuthHandler;
import HTTPClient.CookieModule;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import javax.swing.event.EventListenerList;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.FileDialog;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Hashtable;
import java.io.File;
import java.awt.dnd.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.util.*;
import java.io.*;


/**
 * 
 */
public class Main extends JFrame
{
    public final static String VERSION = "0.92-dev";
    public final static String UserAgent = "UCI DAV Explorer/" + VERSION;
    public final static String COPYRIGHT = "Copyright (c) 1998-2005 Regents of the University of California";
    public final static String EMAIL = "EMail: dav-exp@ics.uci.edu";


    /**
     * 
     * @param frameName
     */
    public Main(String frameName)
    {
        super (frameName);

        setLookAndFeel();

        GlobalData.getGlobalData().setMainFrame( this );

        authTable = new Hashtable();

        treeView = new WebDAVTreeView();
        treeView.setUserAgent( UserAgent );

        fileView = createFileView();

        treeView.addViewSelectionListener( fileView );
        fileView.addViewSelectionListener( treeView );

        // Set the HTTPClient authentication handler
        DefaultAuthHandler.setAuthorizationPrompter(new AuthDialog());

        // allow all cookies
        CookieModule.setCookiePolicyHandler( null );

        requestGenerator = new ACLRequestGenerator();
        requestGenerator.addRequestListener(new RequestListener());
        requestGenerator.setUserAgent( UserAgent );

        responseInterpreter = new ACLResponseInterpreter( requestGenerator );
        responseInterpreter.addInsertionListener(new TreeInsertionListener());
        responseInterpreter.addMoveUpdateListener(new MoveUpdateListener());
        responseInterpreter.addLockListener(new LockListener());
        responseInterpreter.addActionListener(fileView); // Listens for a reset
                                                         // for an unsucessful
                                                         // Rename request
        // create listeners for DeltaV
        responseInterpreter.addVersionControlListener(new VersionControlListener());
        responseInterpreter.addCheckoutListener(new CheckoutListener());
        responseInterpreter.addUnCheckoutListener(new UnCheckoutListener());
        responseInterpreter.addCheckinListener(new CheckinListener());
        responseInterpreter.addMkActivityListener(new MkActivityListener());
        responseInterpreter.addMergeListener(new MergeListener());

        // Add the CopyEvent Listener
        responseInterpreter.addCopyResponseListener(treeView);
        responseInterpreter.addPutListener(treeView);

        webdavManager = new WebDAVManager();
        webdavManager.addResponseListener(new ResponseListener());

        /* To allow retries for PUT using HTTPOutputStream, this has
         * to be set.
         * See @link HTTPResponse#retryRequest() HTTPResponse.retryRequest
         */
        System.setProperty( "HTTPClient.deferStreamed", "true" );
        
        buildFrame(); // 08DEC03 John_Barton@hpl.hp.com factored to allow override

        try
        {
            // JDK 1.1.x doesn't have the drop classes, catching it here
            dropEnabler = new DropEnabler(fileView.table);
        }
        catch( NoClassDefFoundError e )
        {
            dropEnabler = null;
        }

        if (!GlobalData.getGlobalData().isAppletMode())
        {
            // if we're in applet mode, the web page will hold the
            // visible content so we don't want the frame to pop up.
            setVisible(true);

            // applets don't have a title bar so this isn't required
            addWindowListener(new WindowAdapter()
            {
                public void windowClosing(WindowEvent we_Event)
                {
                    System.exit(0);
                }
            } );
        }
    }


    /**
     * 04DEC03 John_Barton@hpl.hp.com Factored from ctor to aid reuse
     */
    public void setLookAndFeel()
    {
        /* Uncomment the following 8 lines if you want system's L&F
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception except)
        {
            System.out.println("Error Loading L&F");
        }
        */
    }


    /**
     * 04DEC03 John_Barton@hpl.hp.com Factored from ctor to aid reuse
     */
    public void buildFrame()
    {
      CommandMenu = new WebDAVMenu();
      setJMenuBar(CommandMenu);

      MenuListener_Gen menuListener = new MenuListener_Gen();

      CommandMenu.addWebDAVMenuListener( menuListener );

      WebDAVToolBar toolbar = new WebDAVToolBar();
      toolbar.addActionListener( menuListener );

      uribox = new URIBox();
      GlobalData.getGlobalData().setURIBox(uribox);
      uribox.addActionListener(new URIBoxListener_Gen());


      JScrollPane fileScrPane = fileView.getScrollPane();
      JScrollPane treeScrPane = treeView.getScrollPane();
      JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,treeScrPane,fileScrPane);
      splitPane.setContinuousLayout(true);

      JPanel p = new JPanel();
      p.setSize(800,600);
      GridBagLayout gridbag = new GridBagLayout();
      p.setLayout(gridbag);
      GridBagConstraints c = new GridBagConstraints();
      c.anchor = GridBagConstraints.CENTER;
      c.fill = GridBagConstraints.HORIZONTAL;
      c.gridx = 0;
      c.gridy = 0;
      c.gridwidth = GridBagConstraints.REMAINDER;
      gridbag.setConstraints(toolbar,c);
      p.add(toolbar);

      c.gridy= GridBagConstraints.RELATIVE;
      c.gridheight = GridBagConstraints.RELATIVE;
      gridbag.setConstraints(uribox,c);
      p.add(uribox);

      c.fill = GridBagConstraints.BOTH;
      c.weighty = 1.0;
      c.weightx = 1.0;
      c.gridheight = GridBagConstraints.REMAINDER;
      gridbag.setConstraints(splitPane,c);
      p.add(splitPane);

      // 2003-March-26: Joachim Feise (dav-exp@ics.uci.edu) changed
      // for progress reporting
      getContentPane().setLayout( new BorderLayout() );
      getContentPane().add( p,BorderLayout.CENTER );
      getContentPane().add( new ProgressBar(),BorderLayout.SOUTH );

      treeView.initTree();
      pack();
    }


    /**
     * 
     * @return
     */
    protected WebDAVFileView createFileView()
    {
        fileView = new WebDAVFileView();
    
        // Get the rename Event
        fileView.addRenameListener(new RenameListener());
        fileView.addDisplayLockListener(new DisplayLockListener());
        fileView.addDisplayVersionListener(new DisplayVersionListener());
    
        return fileView;
    }


    /**
     * 04DEC03 John_Barton@hpl.hp.com Factored from ctor to aid reuse
     * @param argv
     */
    public static void main(String[] argv)
    {
        String property = System.getProperty( "help", "no" );
        if( property.equalsIgnoreCase("no") )
        {
            property = System.getProperty( "version", "no" );
            if( !property.equalsIgnoreCase("no") )
            {
                System.out.println( "DAV Explorer Version "+ VERSION );
                System.out.println( COPYRIGHT );
                System.out.println( EMAIL );
                return;
            }

            new Main("DAV Explorer");
        }
        else
        {
            System.out.println( "DAV Explorer Version "+ VERSION );
            System.out.println( COPYRIGHT );
            System.out.println( "Authors: Yuzo Kanomata, Joachim Feise" );
            System.out.println( EMAIL );
            System.out.println( "Based on code from the UCI WebDAV Client Group of the ICS126B class" );
            System.out.println( "Winter 1998: Gerair Balian, Mirza Baig, Robert Emmery, Thai Le, Tu Le." );
            System.out.println( "Basic DeltaV support based on code from the DeltaV Team of the ICS125" );
            System.out.println( "class Spring 2003: Max Slabyak, Matt Story, Hyung Kim." );
            System.out.println( "Uses the HTTPClient library (http://www.innovation.ch/java/HTTPClient/)." );
            System.out.println( "Uses Microsoft's XML parser published in June 1997.\n" );
            System.out.println( "For other contributors see the contributors.txt file.\n" );
            System.out.println( "Options:" );
            System.out.println( "-Dhelp=yes" );
            System.out.println( "  This help message.\n" );
            System.out.println( "-Ddebug=option" );
            System.out.println( "  where option is one of:" );
            System.out.println( "  all          all function traces are enabled" );
            System.out.println( "  request      function traces related to HTTP requests are enabled" );
            System.out.println( "  response     function traces related to HTTP responses are enabled" );
            System.out.println( "  treeview     function traces related to the tree view on the left side" );
            System.out.println( "               of the DAVExplorer window are enabled" );
            System.out.println( "  treenode     function traces related to each node in the tree view are" );
            System.out.println( "               enabled" );
            System.out.println( "  fileview     function traces related to the file view on the right side" );
            System.out.println( "               of the DAVExplorer window are enabled\n" );
            System.out.println( "-Dpropfind=allprop" );
            System.out.println( "  This option results in using the <allprop> tag in PROPFIND.\n" );
            System.out.println( "-DSSL=yes" );
            System.out.println( "  This option enables the use of SSL.\n" );
            System.out.println( "-DSharePoint=yes" );
            System.out.println( "  This option enables a workaround for a bug in Microsoft's SharePoint" );
            System.out.println( "  server which allows tags to start with a digit.\n" );
            System.out.println( "-DApache=yes" );
            System.out.println( "  This option enables a workaround for a bug in Apache 1.3.x, which returns" );
            System.out.println( "  a 500 error in response to a PROPPATCH if the Host: header contains a port" );
            System.out.println( "  number.\n" );
            System.out.println( "-Dlocal=no" );
            System.out.println( "  This option prevents showing the local directory structure in the main" );
            System.out.println( "  DAV Explorer window.\n" );
            System.out.println( "-Dcompress=no" );
            System.out.println( "  This option prevents the use of compression when transferring data." );
        }
    }


    /**
     * 04DEC03 John_Barton@hpl.hp.com Factored from ctor to aid reuse 
     */
    public class URIBoxListener_Gen implements WebDAVURIBoxListener
    {
        /**
         * 
         * @param e
         */
        public void actionPerformed(ActionEvent e)
        {
            String str = e.getActionCommand();
            if (!str.endsWith("/"))
                str += "/";
            requestGenerator.setExtendedInfo( WebDAVResponseEvent.URIBOX, null );
            // For easier debugging, the commandline option "options=no"
            // disables the initial sending of an OPTIONS request
            String options = System.getProperty( "options", "yes" );
            if( options.equalsIgnoreCase("no") )
            {
                requestGenerator.DoPropFind( str, true );
            }
            else
            {

                if( requestGenerator.GenerateOptions( str ) )
                {
                    requestGenerator.execute();
                }
            }
        }
    }


    /**
     * 04DEC03 John_Barton@hpl.hp.com move to public to allow Main to be subclassed 
     */
    public class TreeInsertionListener implements InsertionListener
    {
        /**
         * 
         * @param e
         */
        public void actionPerformed(ActionEvent e)
        {
            actionPerformed( e, false );
        }

        /**
         * 
         * @param e
         * @param deltaV
         */
        public void actionPerformed(ActionEvent e, boolean deltaV )
        {
            String str = e.getActionCommand();
            if (str == null)
            {
                treeView.refresh();
            }
            else
            {
                treeView.addRowToRoot( str, false, deltaV );
            }
        }
    }


    /**
     * 04DEC03 John_Barton@hpl.hp.com move to public to allow Main to be subclassed
     */
    public class MoveUpdateListener implements ActionListener
    {
        /**
         * 
         * @param e
         */
        public void actionPerformed(ActionEvent e)
        {
            String str = e.getActionCommand();
            if (str == null)
                fileView.update();
            else
            {
                fileView.resetName();
            }
        }
    }


    /**
     * 04DEC03 John_Barton@hpl.hp.com move to public to allow Main to be subclassed
     */
    public class TableSelectListener_Gen implements ViewSelectionListener
    {
        /**
         * 
         * @param e
         */
        public void selectionChanged(ViewSelectionEvent e)
        {
            requestGenerator.tableSelectionChanged(e);
        }
    }


    /**
     * 04DEC03 John_Barton@hpl.hp.com move to public to allow Main to be subclassed
     */
    public class TableSelectListener_Tree implements ViewSelectionListener
    {
        /**
         * 
         * @param e
         */
        public void selectionChanged(ViewSelectionEvent e)
        {
            treeView.tableSelectionChanged(e);
        }
    }


    /**
     * 04DEC03 John_Barton@hpl.hp.com move to public to allow Main to be subclassed
     */
    public class TreeSelectListener_Gen implements ViewSelectionListener
    {
        /**
         * 
         * @param e
         */
        public void selectionChanged(ViewSelectionEvent e)
        {
            requestGenerator.treeSelectionChanged(e);
        }
    }


    /**
     * 04DEC03 John_Barton@hpl.hp.com move to public to allow Main to be subclassed
     */
    public class LockListener implements ActionListener
    {
        /**
         * 
         * @param e
         */
        public void actionPerformed(ActionEvent e)
        {
            String token = e.getActionCommand();
            if ( e.getID() == 0 )
            {
                fileView.setLock();
                treeView.setLock( fileView.getName(), token );
            }
            else
            {
                fileView.resetLock();
                treeView.resetLock( fileView.getName() );
            }
        }
    }


    /**
     * 04DEC03 John_Barton@hpl.hp.com move to public to allow Main to be subclassed
     */
    public class VersionControlListener implements ActionListener
    {
        /**
         * 
         * @param e
         */
        public void actionPerformed(ActionEvent e)
        {
            String res = e.getActionCommand();
            int code = e.getID();
            String str;
            if( code >=200 && code < 300 )
                str = "Version control enabled for " + res + ".";
            else
                str = "Version control command failed. Error: " + res;

            JOptionPane.showMessageDialog( GlobalData.getGlobalData().getMainFrame(), str );
        }
    }


    /**
     * 04DEC03 John_Barton@hpl.hp.com move to public to allow Main to be subclassed
     */
    public class CheckoutListener implements ActionListener
    {
        /**
         * 
         * @param e
         */
        public void actionPerformed(ActionEvent e)
        {
            String res = e.getActionCommand();
            int code = e.getID();
            String str;
            if( code >=200 && code < 300 )
                str = "Checkout for " + res + " successful.";
            else
                str = "Checkout failed. Error: " + res;

            JOptionPane.showMessageDialog( GlobalData.getGlobalData().getMainFrame(), str );
        }
    }


    /**
     * 04DEC03 John_Barton@hpl.hp.com move to public to allow Main to be subclassed
     */
    public class UnCheckoutListener implements ActionListener
    {
        /**
         * 
         * @param e
         */
        public void actionPerformed(ActionEvent e)
        {
            String res = e.getActionCommand();
            int code = e.getID();
            String str;
            if( code >=200 && code < 300 )
            str = "Uncheckout for " + res + " successful.";
            else
                str = "Uncheckout failed. Error: " + res;

            JOptionPane.showMessageDialog( GlobalData.getGlobalData().getMainFrame(), str );
        }
    }


    /**
     * 04DEC03 John_Barton@hpl.hp.com move to public to allow Main to be subclassed
     */
    public class CheckinListener implements ActionListener
    {
        /**
         * 
         * @param e
         */
        public void actionPerformed(ActionEvent e)
        {
            String res = e.getActionCommand();
            int code = e.getID();
            String str;
            if( code >=200 && code < 300 )
            str = "Checkin for " + res + " successful.";
            else
                str = "Checkin failed. Error: " + res;

            JOptionPane.showMessageDialog( GlobalData.getGlobalData().getMainFrame(), str );
        }
    }


    /**
     * 
     */
    public class MkActivityListener implements ActionListener
    {
        /**
         * 
         * @param e
         */
        public void actionPerformed(ActionEvent e)
        {
            String res = e.getActionCommand();
            int code = e.getID();
            String str;
            if( code >=200 && code < 300 )
            str = "Make Activity for " + res + " successful.";
            else
                str = "Make Activity failed. Error: " + res;

            JOptionPane.showMessageDialog( GlobalData.getGlobalData().getMainFrame(), str );
        }
    }


    /**
     * 
     */
    public class MergeListener implements ActionListener
    {
        /**
         * 
         * @param e
         */
        public void actionPerformed(ActionEvent e)
        {
            String res = e.getActionCommand();
            int code = e.getID();
            String str;
            if( code >=200 && code < 300 )
            str = "Merge for " + res + " successful.";
            else
                str = "Merge failed. Error: " + res;

            JOptionPane.showMessageDialog( GlobalData.getGlobalData().getMainFrame(), str );
        }
    }


    /**
     * 04DEC03 John_Barton@hpl.hp.com move to public to allow Main to be subclassed 
     */
    public class DisplayLockListener implements ActionListener
    {
        /**
         * 
         * @param e
         */
        public void actionPerformed(ActionEvent e)
        {
            if( e.getActionCommand() != null )
                requestGenerator.setResource( e.getActionCommand(), null );
            requestGenerator.DiscoverLock( WebDAVResponseEvent.DISPLAY, null );
        }
    }


    /**
     * 04DEC03 John_Barton@hpl.hp.com move to public to allow Main to be subclassed
     */
    public class DisplayVersionListener implements ActionListener
    {
        /**
         * 
         * @param e
         */
        public void actionPerformed(ActionEvent e)
        {
            if( e.getActionCommand() != null )
                requestGenerator.setResource( e.getActionCommand(), null );
            if( requestGenerator.GenerateVersionHistory( WebDAVResponseEvent.DISPLAY ) )
                requestGenerator.execute();
        }
    }


    /**
     * 04DEC03 John_Barton@hpl.hp.com move to public to allow Main to be subclassed
     */
    public class RenameListener implements ActionListener
    {
        /**
         * 
         * @param e
         */
        public void actionPerformed(ActionEvent e)
        {
            String str = e.getActionCommand();
            if (str != null)
            {
                String s = fileView.getOldSelectedResource();
                WebDAVTreeNode n = fileView.getParentNode();
                requestGenerator.setResource(s, n);

                boolean retval = false;
                if( fileView.isSelectedLocked() )
                {
                    retval = requestGenerator.GenerateMove( str,
                                                            fileView.getParentPath(),
                                                            false, true,
                                                            fileView.getSelectedLockToken(),
                                                            WebDAVResponseEvent.RENAME );
                }
                else
                {
                    retval = requestGenerator.GenerateMove( str,
                                                            fileView.getParentPath(),
                                                            false, true, null ,
                                                            WebDAVResponseEvent.RENAME );
                }
                if( retval )
                {
                    requestGenerator.execute();
                }
            }
        }
    }


    /**
     * 04DEC03 John_Barton@hpl.hp.com move to public to allow Main to be subclassed
     */
    public class RequestListener implements WebDAVRequestListener
    {
        /**
         * 
         * @param e
         */    
        public void requestFormed(WebDAVRequestEvent e)
        {
            webdavManager.sendRequest(e);
        }
    }


    /**
     * 04DEC03 John_Barton@hpl.hp.com move to public to allow Main to be subclassed
     */
    public class ResponseListener implements WebDAVResponseListener
    {
        /**
         * 
         * @param e
         */
        public void responseFormed(WebDAVResponseEvent e)
        {
            boolean retval = false;

            // This call process the info from the server
            try
            {
                retval = responseInterpreter.handleResponse(e);
            }
            catch( ResponseException ex )
            {
                GlobalData.getGlobalData().errorMsg( "HTTP error or Server timeout,\nplease retry the last operation" );
                fireWebDAVCompletion( responseInterpreter, false );
                return;
            }
            // don't trigger completion info if there is a retry
            if( retval )
                fireWebDAVCompletion( responseInterpreter, true );

            // Post processing
            // These are actions designed to take place after the
            // response has been loaded
            int extendedCode = e.getExtendedCode();
            String method = e.getMethodName();

            if ( method.equals("COPY") || method.equals("PUT") )
            {
                // skip
            }
            else
            {
                switch( extendedCode )
                {
                    case WebDAVResponseEvent.EXPAND:
                    case WebDAVResponseEvent.INDEX:
                    {
                        WebDAVTreeNode tn = e.getNode();
                        if (tn != null)
                        {
                            tn.finishLoadChildren();
                        }
                        break;
                    }
                    
                    case WebDAVResponseEvent.SELECT:
                    {
                        WebDAVTreeNode tn = e.getNode();
                        if (tn != null)
                        {
                            tn.finishLoadChildren();
                            treeView.setSelectedNode(tn);
                        }
                        break;
                    }
                    
                    case WebDAVResponseEvent.URIBOX: 
                    {
                        //???? TODO: not needed????
                        WebDAVTreeNode tn = e.getNode();
                        break;
                    }
                }
            }
        }
    }


    /**
     * 04DEC03 John_Barton@hpl.hp.com move to public to allow Main to be subclassed
     */
    public class MenuListener_Gen implements ActionListener
    {
        /**
         * 
         * @param e
         */
        public void actionPerformed(ActionEvent e)
        {
            switch( e.getID() )
            {
                case WebDAVMenu.EXIT:
                    // never allow exit from inside an applet
                    if( !GlobalData.getGlobalData().isAppletMode() )
                        System.exit(0);
                    break;
                    
                case WebDAVMenu.GET_FILE:
                    saveAsDocument();
                    break;

                case WebDAVMenu.WRITE_FILE:
                {
                    FileDialog fd = new FileDialog(GlobalData.getGlobalData().getMainFrame(), "Write File" , FileDialog.LOAD);
                    if (writeToDir != null)
                    {
                        fd.setDirectory(writeToDir);
                    }
                    fd.setVisible(true);

                    String dirName = fd.getDirectory();
                    String fName = fd.getFile();
                    doWriteFile(dirName, fName);  // 08DEC03 John_Barton@hpl.hp.com factored into doWriteFile()
                    break;
                }
                
                case WebDAVMenu.EXCLUSIVE_LOCK:
                {
                    String s = fileView.getSelected();
                    if( s == null )
                    {
                        GlobalData.getGlobalData().errorMsg( "No resource selected." );
                    }
                    else
                    {
                        WebDAVTreeNode n = fileView.getParentNode();
                        requestGenerator.setResource(s, n);
                        lockDocument( true );
                    }
                    break;
                }
                
                case WebDAVMenu.SHARED_LOCK:
                {
                    String s = fileView.getSelected();
                    if( s == null )
                    {
                        GlobalData.getGlobalData().errorMsg( "No resource selected." );
                    }
                    else
                    {
                        WebDAVTreeNode n = fileView.getParentNode();
                        requestGenerator.setResource(s, n);
                        lockDocument( false );
                    }
                    break;
                }
                
                case WebDAVMenu.UNLOCK:
                {
                    String s = fileView.getSelected();
                    if( s == null )
                    {
                        GlobalData.getGlobalData().errorMsg( "No resource selected." );
                    }
                    else
                    {
                        WebDAVTreeNode n = fileView.getParentNode();
                        requestGenerator.setResource(s, n);
                        unlockDocument();
                    }
                    break;
                }
                
                case WebDAVMenu.COPY:
                {
                    // Yuzo: I have the semantics set so that
                    // we get a string if something is selected in the
                    // FileView.
                    String s = fileView.getSelected();
                    if( (s == null) || (s.length() == 0) )
                    {
                        GlobalData.getGlobalData().errorMsg( "No resource selected." );
                    }
                    else
                    {
                        WebDAVTreeNode n = fileView.getParentNode();
    
                        // This sets the resource name to s and the node to n
                        // This is neccessary so that n is passed to
                        // response interpretor, whc then routes a
                        // message to the Tree Model.
                        // This will then call for a rebuild of the Model at the
                        // Parent node n.
                        requestGenerator.setResource(s, n);
    
                        String prompt = "Enter the name of the copy:";
                        String title = "Copy Resource";
                        String defaultName = requestGenerator.getDefaultName( "_copy" );
                        //String overwrite = "Overwrite existing resource?";
                        String fname = selectName( title, prompt, defaultName );
                        if( fname != null )
                        {
                            if( requestGenerator.GenerateCopy( fname, true, true ) )
                            {
                                requestGenerator.execute();
                            }
                        }
                    }
                    break;
                }
                
                case WebDAVMenu.MOVE:
                {
                    String s = fileView.getSelected();
                    if( (s == null) || (s.length() == 0) )
                    {
                        GlobalData.getGlobalData().errorMsg( "No resource selected." );
                    }
                    else
                    {
                        WebDAVTreeNode n = fileView.getParentNode();
    
                        // This sets the resource name to s and the node to n
                        // This is neccessary so that n is passed to
                        // response interpretor, which then routes a
                        // message to the Tree Model.
                        // This will then call for a rebuild of the Model at the
                        // Parent node n.
                        requestGenerator.setResource(s, n);
    
                        String prompt = "Enter the new name of the resource:";
                        String title = "Move Resource";
                        String defaultName = requestGenerator.getDefaultName( null );
                        //String overwrite = "Overwrite existing resource?";
                        String fname = selectName( title, prompt, defaultName );
                        if( fname != null )
                        {
                            boolean retval = false;
                            if( fileView.isSelectedLocked() )
                                retval = requestGenerator.GenerateMove( fname, null,
                                                                        false, true,
                                                                        fileView.getSelectedLockToken(),
                                                                        WebDAVResponseEvent.RENAME );
                            else
                                retval = requestGenerator.GenerateMove( fname, null,
                                                                        false, true,
                                                                        null ,
                                                                        WebDAVResponseEvent.RENAME );
    
                            if( retval )
                                requestGenerator.execute();
                        }
                    }
                    break;
                }
                
                case WebDAVMenu.DELETE:
                {
                    String s = fileView.getSelected();
                    if( s == null )
                    {
                        GlobalData.getGlobalData().errorMsg( "No resource selected." );
                    }
                    else
                    {
                        //deleteDocument( treeView.isCollection( s ) );
                        WebDAVTreeNode n = fileView.getSelectedCollection();
                        if ( n == null)
                        {
                            deleteDocument( false );
                        }
                        else
                        {
                            deleteDocument(  true );
                        }
                    }
                    break;
                }
                
                case WebDAVMenu.CREATE_COLLECTION:
                {
                    WebDAVTreeNode n = fileView.getParentNode();
                    String prompt = new String( "Enter collection name:" );
                    String title = new String( "Create Collection" );
                    String dirname = selectName( title, prompt );
    
                    if( dirname != null )
                    {
                        WebDAVTreeNode selected = fileView.getSelectedCollection();
                        if( treeView.isRemote( fileView.getParentPath() ) )
                        {
                            boolean retval = false;
                            if (selected == null)
                            {
                                requestGenerator.setNode(n);
                                requestGenerator.setExtendedInfo( WebDAVResponseEvent.MKCOL, null );
                                retval = requestGenerator.GenerateMkCol( fileView.getParentPath(), dirname );
                            }
                            else
                            {
                                requestGenerator.setNode( selected );
                                requestGenerator.setExtendedInfo( WebDAVResponseEvent.MKCOLBELOW, null );
                                retval = requestGenerator.GenerateMkCol( fileView.getSelected(), dirname );
                            }
                            if( retval )
                            {
                                requestGenerator.execute();
                            }
                        }
                        else
                        {
                            if( !treeView.getCurrentPath().endsWith( String.valueOf(File.separatorChar) ) )
                                dirname = fileView.getSelected() + File.separatorChar + dirname;
                            else
                                dirname = fileView.getSelected() + dirname;
                            File f = new File( dirname );
                            boolean result = f.mkdir();
                            if ( selected == null )
                            {
                                treeView.refreshLocal( n );
                            }
                            else
                            {
                                treeView.refreshLocalNoSelection(selected);
                            }
                        }
                    }
                    break;
                }
                
                case WebDAVMenu.CLEAR_AUTH_BUFFER:
                    authTable.clear();
                    break;
                    
                case WebDAVMenu.EDIT_PROXY_INFO:
                    WebDAVProxyInfo proxyInfo = new WebDAVProxyInfo(GlobalData.getGlobalData().getMainFrame(), "Proxy Info", true);
                    break;
                    
                case WebDAVMenu.EDIT_LOCK_INFO:
                    WebDAVLockInfo lockInfo = new WebDAVLockInfo(GlobalData.getGlobalData().getMainFrame(), "Lock Info", true);
                    break;
                
                case WebDAVMenu.INIT_VERSION_CONTROL:
                {
                    String s = fileView.getSelected();
                    if( (s == null) || (s.length() == 0) )
                    {
                        GlobalData.getGlobalData().errorMsg( "No resource selected." );
                        return;
                    }
                    WebDAVTreeNode n = fileView.getParentNode();
                    requestGenerator.setResource( s, n );
                    if( requestGenerator.GenerateEnableVersioning() )
                        requestGenerator.execute();
                    break;
                }
                
                case WebDAVMenu.VERSION_REPORT:
                {
                    String s = fileView.getSelected();
                    if( (s == null) || (s.length() == 0) )
                    {
                        GlobalData.getGlobalData().errorMsg( "No resource selected." );
                        return;
                    }
                    WebDAVTreeNode n = fileView.getParentNode();
                    requestGenerator.setResource( s, n );
                    if( requestGenerator.GenerateVersionHistory( WebDAVResponseEvent.DISPLAY ) )
                        requestGenerator.execute();
                    break;
                }
                
                case WebDAVMenu.CHECKOUT:
                {
                    String s = fileView.getSelected();
                    if( (s == null) || (s.length() == 0) )
                    {
                        GlobalData.getGlobalData().errorMsg( "No resource selected." );
                        return;
                    }
                    WebDAVTreeNode n = fileView.getParentNode();
                    requestGenerator.setResource( s, n );
                    if( requestGenerator.GenerateCheckOut() )
                        requestGenerator.execute();
                    break;
                }
                
                case WebDAVMenu.UNCHECKOUT:
                {
                    String s = fileView.getSelected();
                    if( (s == null) || (s.length() == 0) )
                    {
                        GlobalData.getGlobalData().errorMsg( "No resource selected." );
                        return;
                    }
                    WebDAVTreeNode n = fileView.getParentNode();
                    requestGenerator.setResource( s, n );
                    if( requestGenerator.GenerateUnCheckOut() )
                        requestGenerator.execute();
                    break;
                }
                
                case WebDAVMenu.CHECKIN:
                {
                    String s = fileView.getSelected();
                    if( (s == null) || (s.length() == 0) )
                    {
                        GlobalData.getGlobalData().errorMsg( "No resource selected." );
                        return;
                    }
                    WebDAVTreeNode n = fileView.getParentNode();
                    requestGenerator.setResource( s, n );
                    if( requestGenerator.GenerateCheckIn() )
                        requestGenerator.execute();
                    break;
                }
                
                case WebDAVMenu.MAKE_ACTIVITY:
                {
                    String s = fileView.getSelected();
                    if( (s == null) || (s.length() == 0) )
                    {
                        GlobalData.getGlobalData().errorMsg( "No resource selected." );
                        return;
                    }
                    WebDAVTreeNode n = fileView.getParentNode();
                    requestGenerator.setResource( s, n );
                    if( requestGenerator.GenerateMkActivity() )
                        requestGenerator.execute();
                    break;
                }
                
                case WebDAVMenu.MERGE:
                {
                    String s = fileView.getSelected();
                    if( (s == null) || (s.length() == 0) )
                    {
                        GlobalData.getGlobalData().errorMsg( "No resource selected." );
                        return;
                    }
                    WebDAVTreeNode n = fileView.getParentNode();
                    requestGenerator.setResource( s, n );
                    if( requestGenerator.GenerateMerge() )
                        requestGenerator.execute();
                    break;
                }

                /* ACL, RFC 3744 Section 5.1 */
                case WebDAVMenu.VIEW_OWNER:
                {
                    String s = fileView.getSelected();
                    if( (s == null) || (s.length() == 0) )
                    {
                        GlobalData.getGlobalData().errorMsg( "No resource selected." );
                        return;
                    }
                    WebDAVTreeNode n = fileView.getParentNode();
                    requestGenerator.setResource( s, n );
                    requestGenerator.GetOwner();
                    break;
                }
                
                /* ACL, RFC 3744 Section 5.2 */
                case WebDAVMenu.VIEW_GROUP:
                {
                    String s = fileView.getSelected();
                    if( (s == null) || (s.length() == 0) )
                    {
                        GlobalData.getGlobalData().errorMsg( "No resource selected." );
                        return;
                    }
                    WebDAVTreeNode n = fileView.getParentNode();
                    requestGenerator.setResource( s, n );
                    requestGenerator.GetGroup();
                    break;
                }
                
                /* ACL, RFC 3744 Section 5.4 */
                case WebDAVMenu.GET_USER_PRIVILEGES:
                {
                    String s = fileView.getSelected();
                    if( (s == null) || (s.length() == 0) )
                    {
                        GlobalData.getGlobalData().errorMsg( "No resource selected." );
                        return;
                    }
                    WebDAVTreeNode n = fileView.getParentNode();
                    requestGenerator.setResource( s, n );
                    requestGenerator.GetUserPrivileges();
                    break;
                }
                
                /* ACL, RFC 3744 Section 5.5 */
                case WebDAVMenu.VIEW_ACL:
                {
                    String s = fileView.getSelected();
                    if( (s == null) || (s.length() == 0) )
                    {
                        GlobalData.getGlobalData().errorMsg( "No resource selected." );
                        return;
                    }
                    WebDAVTreeNode n = fileView.getParentNode();
                    requestGenerator.setResource( s, n );
                    requestGenerator.GetACL();
                    break;
                }

                /* ACL, RFC 3744 Section 5.3 */
                case WebDAVMenu.GET_SUPPORTED_ACL:
                {
                    String s = fileView.getSelected();
                    if( (s == null) || (s.length() == 0) )
                    {
                        GlobalData.getGlobalData().errorMsg( "No resource selected." );
                        return;
                    }
                    WebDAVTreeNode n = fileView.getParentNode();
                    requestGenerator.setResource( s, n );
                    requestGenerator.GetACLRestrictions();
                    break;
                }
                
                /* ACL, RFC 3744 Section 5.7 */
                case WebDAVMenu.GET_INHERITED_ACL:
                {
                    String s = fileView.getSelected();
                    if( (s == null) || (s.length() == 0) )
                    {
                        GlobalData.getGlobalData().errorMsg( "No resource selected." );
                        return;
                    }
                    WebDAVTreeNode n = fileView.getParentNode();
                    requestGenerator.setResource( s, n );
                    requestGenerator.GetInheritedACLs();
                    break;
                }

                /* ACL, RFC 3744 Section 9.2 */
                case WebDAVMenu.ACL_PRINCIPAL_PROP_SET_REPORT:
                {
                    String s = fileView.getSelected();
                    if( (s == null) || (s.length() == 0) )
                    {
                        GlobalData.getGlobalData().errorMsg( "No resource selected." );
                        return;
                    }
                    ACLReportPropertiesDialog dlg = new ACLReportPropertiesDialog( s, true );
                    if( !dlg.isCanceled() )
                    {
                        Vector props = dlg.getSelected(); 
                        WebDAVTreeNode n = fileView.getParentNode();
                        requestGenerator.setResource( s, n );
                        if( requestGenerator.GetPrincipalPropSetReport( props ) )
                            requestGenerator.execute();
                    }
                    break;
                }
                
                /* ACL, RFC 3744 Section 9.3 */
                case WebDAVMenu.PRINCIPAL_MATCH_REPORT:
                {
                    String s = fileView.getSelected();
                    if( (s == null) || (s.length() == 0) )
                    {
                        GlobalData.getGlobalData().errorMsg( "No resource selected." );
                        return;
                    }
                    ACLReportSearchPropertyDialog dlg = new ACLReportSearchPropertyDialog( s, false );
                    if( !dlg.isCanceled() )
                    {
                        Vector criteria = dlg.getSearchCriteria(); 
                        Vector props = dlg.getSelected();
                        boolean self = dlg.isSelf();
                        WebDAVTreeNode n = fileView.getParentNode();
                        requestGenerator.setResource( s, n );
                        if( requestGenerator.GetPrincipalMatchReport( criteria, props, self ) )
                            requestGenerator.execute();
                    }
                    break;
                }
                
                /* ACL, RFC 3744 Section 9.4 */
                case WebDAVMenu.PRINCIPAL_PROPERTY_SEARCH_REPORT:
                {
                    String s = fileView.getSelected();
                    if( (s == null) || (s.length() == 0) )
                    {
                        GlobalData.getGlobalData().errorMsg( "No resource selected." );
                        return;
                    }
                    ACLReportSearchPropertyDialog dlg = new ACLReportSearchPropertyDialog( s, true );
                    if( !dlg.isCanceled() )
                    {
                        Vector criteria = dlg.getSearchCriteria(); 
                        Vector props = dlg.getSelected(); 
                        WebDAVTreeNode n = fileView.getParentNode();
                        requestGenerator.setResource( s, n );
                        if( requestGenerator.GetPrincipalPropertySearchReport( criteria, props ) )
                            requestGenerator.execute();
                    }
                    break;
                }
                
                /* ACL, RFC 3744 Section 9.5 */
                case WebDAVMenu.PRINCIPAL_SEARCH_PROPERTY_SET_REPORT:
                {
                    String s = fileView.getSelected();
                    if( (s == null) || (s.length() == 0) )
                    {
                        GlobalData.getGlobalData().errorMsg( "No resource selected." );
                        return;
                    }
                    WebDAVTreeNode n = fileView.getParentNode();
                    requestGenerator.setResource( s, n );
                    if( requestGenerator.GetPrincipalSearchPropertySetReport() )
                        requestGenerator.execute();
                    break;
                }
                
                case WebDAVMenu.HTTP_LOGGING:
                {
                    boolean logging = false;
                    String logFilename = null;
                    if( CommandMenu.getLogging() )
                    {
                        String message = new String( "WARNING: The logfile may get very large,\nsince all data is logged.\n" +
                                                     "Hit Cancel now if you don't want to log the data." );
                        int opt = JOptionPane.showConfirmDialog( GlobalData.getGlobalData().getMainFrame(), message, "HTTP Logging", JOptionPane.OK_CANCEL_OPTION );
                        if( opt == JOptionPane.OK_OPTION )
                        {
                            JFileChooser fd = new JFileChooser();
                            fd.setDialogType( JFileChooser.SAVE_DIALOG );
                            fd.setFileSelectionMode( JFileChooser.FILES_ONLY );
                            fd.setDialogTitle( "Select Logging File" );
                            String os = (System.getProperty( "os.name" )).toLowerCase();
                            String dirName = null;
                            if( os.indexOf( "windows" ) == -1 )
                                dirName = System.getProperty("user.home");
                            if( dirName == null )
                                dirName = new Character(File.separatorChar).toString();
                            fd.setCurrentDirectory( new File(dirName) );
                            fd.setApproveButtonMnemonic( 'U' );
                            fd.setApproveButtonToolTipText( "Use the selected file for logging" );
                            int val = fd.showDialog( GlobalData.getGlobalData().getMainFrame(), "Logging" );
                            if( val == JFileChooser.APPROVE_OPTION)
                            {
                                logFilename = fd.getSelectedFile().getAbsolutePath();
                                try
                                {
                                    File f = fd.getSelectedFile();
                                    if( f.exists() )
                                        f.delete();
                                    logging = true;
                                }
                                catch( Exception exception )
                                {
                                    System.out.println( "File could not be deleted.\n" + exception );
                                    logFilename = null;
                                }
                            }
                        }
                    }
    
                    CommandMenu.setLogging( logging );
                    webdavManager.setLogging( logging, logFilename );
                    break;
                }

                case WebDAVMenu.USE_SSL:
                    boolean use_ssl = CommandMenu.getSSL();
                    GlobalData.getGlobalData().setSSL( use_ssl );
                    // TODO: update screen
                    uribox.invalidate();
                    break;

                case WebDAVMenu.VIEW_MODIFY_PROPS:
                    viewProperties();
                    break;
                    
                case WebDAVMenu.VIEW_LOCK_PROPS:
                {
                    String s = fileView.getSelected();
                    if( s == null )
                    {
                        GlobalData.getGlobalData().errorMsg( "No file selected." );
                    }
                    else
                    {
                        WebDAVTreeNode n = fileView.getParentNode();
                        requestGenerator.setResource(s, n);
    
                        requestGenerator.DiscoverLock( WebDAVResponseEvent.DISPLAY, null );
                    }
                    break;
                }
                
                case WebDAVMenu.REFRESH:
                {
                    WebDAVTreeNode n = fileView.getParentNode();
                    responseInterpreter.setRefresh( n );
                    break;
                }
                
                case WebDAVMenu.ABOUT:
                {
                    String message = new String("DAV Explorer Version "+ VERSION + "\n" +
                    COPYRIGHT + "\n" +
                    "Authors: Yuzo Kanomata, Joachim Feise\n" +
                    EMAIL + "\n\n" +
                    "Based on code from the UCI WebDAV Client Group of the ICS126B class\n" +
                    "Winter 1998: Gerair Balian, Mirza Baig, Robert Emmery, Thai Le, Tu Le.\n" +
                    "Basic DeltaV support based on code from the DeltaV Team of the ICS125\n" +
                    "class Spring 2003: Max Slabyak, Matt Story, Hyung Kim.\n" +
                    "Uses the HTTPClient library (http://www.innovation.ch/java/HTTPClient/).\n" +
                    "Uses Microsoft's XML parser published in June 1997.\n" +
                    "For other contributors see the contributors.txt file.");
                    Object [] options = { "OK" };
    				JOptionPane.showOptionDialog(GlobalData.getGlobalData().getMainFrame(), message, "About DAV Explorer", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
                    break;
                }
            }
        }
    }


    /**
     * 
     * @param l
     */
    public void addWebDAVCompletionListener( WebDAVCompletionListener l )
    {
        listenerList.add( WebDAVCompletionListener.class, l );
    }


    /**
     * 
     * @param l
     */
    public void removeWebDAVCompletionListener( WebDAVCompletionListener l )
    {
        listenerList.remove(WebDAVCompletionListener.class, l );
    }


    /**
     * 
     * @param source
     * @param success
     */
    protected void fireWebDAVCompletion( Object source, boolean success )
    {
        Object[] listeners = listenerList.getListenerList();
        WebDAVCompletionEvent e = null;
        for (int i = listeners.length-2; i>=0; i-=2)
        {
            if (listeners[i]==WebDAVCompletionListener.class)
            {
                if (e == null)
                    e = new WebDAVCompletionEvent( source, success );
                ((WebDAVCompletionListener)listeners[i+1]).completion(e);
            }
        }
    }


    /**
     * 
     */
    protected void viewDocument()
    {
        if( requestGenerator.GenerateGet( WebDAVResponseEvent.VIEW ) )
        {
            requestGenerator.execute();
        }
    }


    /**
     * 
     */
    protected void saveAsDocument()
    {
        String s = fileView.getSelected();
        if( s == null )
        {
            GlobalData.getGlobalData().errorMsg( "No file selected." );
        }
        else
        {
            WebDAVTreeNode n = fileView.getParentNode();
            requestGenerator.setResource(s, n);
            if( requestGenerator.GenerateGet( WebDAVResponseEvent.SAVE_AS ) )
            {
                requestGenerator.execute();
            }
        }
    }


    /**
     * 
     * @param collection
     */
    protected void deleteDocument( boolean collection )
    {
        String s = fileView.getSelected();
        if( !fileView.hasSelected() )
        {
            GlobalData.getGlobalData().errorMsg( "No file selected." );
        }
        else
        {
            String str = null;
            String title = null;
            String defaultName = null;
            if( treeView.isRemote( s ) )
            {
                WebDAVTreeNode n = fileView.getParentNode();
                requestGenerator.setResource(s, n);
                defaultName = requestGenerator.getDefaultName( null );
            }
            else
                defaultName = s;
            if( collection )
            {
                title = "Delete Collection";
                str = "Delete the collection " + defaultName + " and all its contents:\nAre you sure?";
            }
            else
            {
                title = "Delete File";
                str = "Delete " + defaultName + ":\nAre you sure?";
            }
            int opt = JOptionPane.showConfirmDialog( GlobalData.getGlobalData().getMainFrame(), str, title, JOptionPane.YES_NO_OPTION );
            if (opt == JOptionPane.YES_OPTION)
            {
                if( treeView.isRemote( s ) )
                {
                    WebDAVTreeNode n = fileView.getParentNode();
                    requestGenerator.setResource(s, n);
                    requestGenerator.setExtendedInfo( WebDAVResponseEvent.DELETE, null );
                    boolean retval = false;
                    if( fileView.isSelectedLocked() ){
                    retval = requestGenerator.GenerateDelete(fileView.getSelectedLockToken());
                    } else {
                                retval = requestGenerator.GenerateDelete(null);
                            }
                            if( retval ){
                                requestGenerator.execute();
                    }
                }
                else
                {
                    WebDAVTreeNode n = fileView.getParentNode();
                    File f = new File( s );
                    if( !deleteLocal( f ) )
                        GlobalData.getGlobalData().errorMsg( "Delete Error on local filesystem." );
                    treeView.refreshLocal( n );
                }
            }
        }
    }


    /**
     * 
     * @param exclusive
     */
    protected void lockDocument( boolean exclusive )
    {
        if( exclusive )
            requestGenerator.DiscoverLock( WebDAVResponseEvent.EXCLUSIVE_LOCK, null );
        else
            requestGenerator.DiscoverLock( WebDAVResponseEvent.SHARED_LOCK, null );
    }


    /**
     * 
     */
    protected void unlockDocument()
    {
        requestGenerator.DiscoverLock( WebDAVResponseEvent.UNLOCK, null );
    }


    /**
     *
     */
    protected void viewProperties()
    {
        String s = fileView.getSelected();
        if( s == null )
        {
            GlobalData.getGlobalData().errorMsg( "No file selected." );
        }
        else
        {
            requestGenerator.setResource(s, null);
            requestGenerator.setExtendedInfo( WebDAVResponseEvent.PROPERTIES, null );
            if( requestGenerator.GeneratePropFind(null,"allprop","zero",null,null,false) )
            {
                requestGenerator.execute();
            }
        }
    }


    /**
     * 08DEC03 John_Barton@hpl.hp.com extracted from Write File branch of event handler.
     * @param dirName
     * @param fName
     */
    protected void doWriteFile(String dirName, String fName)
    {
        if( (dirName != null) && !dirName.equals("") && (fName != null) &&
                !fName.equals("") )
        {
            writeToDir = dirName;
            String fullPath = dirName + fName;
            String token = treeView.getLockToken(fName);
            
            // Get the current Node so that we can update it later
            String s = "";
            
            WebDAVTreeNode n2 = fileView.getSelectedCollection();
            
            s = fileView.getSelected();
            if( s == null )
            {
                s = "";
            }
            WebDAVTreeNode parent = fileView.getParentNode();
            
            boolean retval = false;
            if( n2 == null )
            {
                requestGenerator.setResource(s, parent);
                retval = requestGenerator.GeneratePut(fullPath, s, token, null);
            }
            else
            {
                requestGenerator.setResource(s, n2);
                retval = requestGenerator.GeneratePut(fullPath, s, token, parent);
            }
            if( retval ) 
            {
                requestGenerator.execute();
            }
        }
    }


    /**
     * 08DEC03 John_Barton@hpl.hp.com Add support for drop (and a bit for drag)
     */
    public class DropEnabler
        implements DropTargetListener, DragSourceListener, DragGestureListener
    {
        Component drop_enabled;
        DropTarget dropTarget = new DropTarget(drop_enabled, this);
        DragSource dragSource = DragSource.getDefaultDragSource();
        int drop_action_allowed = DnDConstants.ACTION_COPY;
        String os_name;

        /**
         * Constructor 
         * @param enabled
         */
        public DropEnabler(Component enabled)
        {
            this.drop_enabled = enabled;
            int drop_action_allowed = DnDConstants.ACTION_COPY;
            dropTarget = new DropTarget(drop_enabled, drop_action_allowed, this, true);
            dragSource.createDefaultDragGestureRecognizer( drop_enabled, DnDConstants.ACTION_COPY, this );
            os_name = System.getProperty("os.name");
        }



        /**
         * DragSourceListener 
         * @param DragSourceDropEvent
         */
        public void dragDropEnd(DragSourceDropEvent DragSourceDropEvent)
        {
            /* ignored */
        }


        /**
         * 
         * @param DragSourceDragEvent
         */
        public void dragEnter(DragSourceDragEvent DragSourceDragEvent)
        {
            /* ignored */
        }


        /**
         * 
         * @param DragSourceEvent
         */
        public void dragExit(DragSourceEvent DragSourceEvent)
        {
            /* ignored */
        }


        /**
         * 
         * @param DragSourceDragEvent
         */
        public void dragOver(DragSourceDragEvent DragSourceDragEvent)
        {
            /* ignored */
        }


        /**
         * 
         * @param DragSourceDragEvent
         */
        public void dropActionChanged(DragSourceDragEvent DragSourceDragEvent)
        {
            /* ignored */
        }


        /**
         * DropTargetListener
         * @param dropTargetDragEvent
         */
        public void dragEnter(DropTargetDragEvent dropTargetDragEvent)
        {
            dropTargetDragEvent.acceptDrag(drop_action_allowed);
        }
        

        /**
         * 
         * @param dropTargetEvent
         */
        public void dragExit(DropTargetEvent dropTargetEvent)
        {
            /* ignored */
        }


        /**
         * 
         * @param dropTargetDragEvent
         */
        public void dragOver(DropTargetDragEvent dropTargetDragEvent)
        {
            /* ignored */
        }


        /**
         * 
         * @param dropTargetDragEvent
         */
        public void dropActionChanged(DropTargetDragEvent dropTargetDragEvent)
        {
            /* ignored */
        }


        /**
         * 
         * @param dropTargetDropEvent
         */
        public synchronized void drop(DropTargetDropEvent dropTargetDropEvent)
        {
            try 
            {
                Transferable tr = dropTargetDropEvent.getTransferable();
                if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
                {
                    dropTargetDropEvent.acceptDrop( DnDConstants.ACTION_COPY_OR_MOVE );
                    java.util.List fileList = (java.util.List)tr.getTransferData(DataFlavor.javaFileListFlavor);
                    Iterator iterator = fileList.iterator();
                    while (iterator.hasNext())
                    {
                        File file = (File) iterator.next();
                        doWriteFile(file.getParent()+File.separator,file.getName());
                    }
                    dropTargetDropEvent.getDropTargetContext().dropComplete(true);
                }
                else
                {
                    DataFlavor incoming_flavors[] = tr.getTransferDataFlavors();
                    String say_flavors = new String("Dropped object has no acceptable type; they are ");
                    for (int i = 0; i < incoming_flavors.length; i++)
                    {
                        say_flavors = say_flavors + "\n"+ incoming_flavors[i].getHumanPresentableName();
                    }
                    GlobalData.getGlobalData().errorMsg(say_flavors);
                    dropTargetDropEvent.rejectDrop();
                }
            }
            catch (IOException io)
            {
                io.printStackTrace();
                dropTargetDropEvent.rejectDrop();
            }
            catch (UnsupportedFlavorException ufe)
            {
                ufe.printStackTrace();
                dropTargetDropEvent.rejectDrop();
            }
        }


        /**
         * 
         * @param dragGestureEvent
         */
        public void dragGestureRecognized(DragGestureEvent dragGestureEvent)
        {
            String obj = fileView.getSelected();
            if (obj == null)
            {
                // Nothing selected, nothing to drag
                getToolkit().beep();
            }
            else
            {
                if (os_name.substring(0,3).compareToIgnoreCase("win") == 0)
                {
                    // drag only for Windows
                    try
                    {
                        Runtime.getRuntime().exec("cmd /c start " + obj);
                    }
                    catch (Exception e)
                    {
                        GlobalData.getGlobalData().errorMsg("Cannot start "+obj);
                        System.out.println(e);
                    }
                }
                else
                {
                    // ignore for any other OS
                }
            }
        }
    }


    /**
     * 
     * @param title
     * @param prompt
     * @return
     */
    private String selectName( String title, String prompt )
    {
        return selectName( title, prompt, null );
    }


    /**
     * 
     * @param title
     * @param prompt
     * @param defaultValue
     * @return
     */
    private String selectName( String title, String prompt, String defaultValue )
    {
        String ret = (String)JOptionPane.showInputDialog( GlobalData.getGlobalData().getMainFrame(), prompt, title, JOptionPane.QUESTION_MESSAGE, null, null, defaultValue );
        return ret;
    }


    /**
     * 
     * @param f
     * @return
     */
    private boolean deleteLocal( File f )
    {
        try
        {
            if( f.isDirectory() )
            {
                String[] flist = f.list();
                for( int i=0; i<flist.length; i++ )
                {
                    if( !deleteLocal( new File(flist[i]) ) )
                        return false;
                }
                return f.delete();
            }
            else
                return f.delete();
        }
        catch( Exception e )
        {
        }
        return false;
    }


    protected DropEnabler dropEnabler;
    protected WebDAVFileView fileView;
    protected WebDAVTreeView treeView;
    protected ACLRequestGenerator requestGenerator;
    protected ACLResponseInterpreter responseInterpreter;
    protected WebDAVManager webdavManager;
    protected WebDAVMenu CommandMenu;
    protected Hashtable authTable;
    protected String writeToDir;
    protected EventListenerList listenerList = new EventListenerList();
    protected URIBox uribox;
}
