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

// This is the class containing the main() function, which merely
// instantiates the Main JFrame.
// The Main class creates the user interface and adds the appropriate
// listeners.
//
// Version: 0.3.1
// Author:  Robert Emmery
// Date:    4/2/98
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
// 1. Changed treeView.fireSelectionEvent(); to treeView.initTree();
//    This is the same function, but with a better name.
// 2. Added Create Folder functionality
// 3. Added filename selection for export file
//
// Version: 0.5
// Changes by: Joe Feise
// Date: 12/3/99
//
// Change List:
// Removed the authentication dialog and listener, since authentication is now handled
// as AuthenticationHandler in HTTPClient

package DAVExplorer;

import HTTPClient.AuthorizationInfo;
import HTTPClient.CookieModule;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

public class Main extends JFrame
{
    static JFrame WebDAVFrame=null;
    WebDAVFileView fileView;
    WebDAVTreeView treeView;
    WebDAVRequestGenerator requestGenerator;
    WebDAVResponseInterpreter responseInterpreter;
    WebDAVManager webdavManager;
    WebDAVMenu CommandMenu;
    Hashtable authTable;
    String authHost;
    public final static String VERSION = "0.59";
    public final static String UserAgent = "UCI DAV Explorer/" + VERSION;
    String writeToDir;

    public Main(String frameName)
    {
        super (frameName);
//        Uncomment the following 8 lines if you want system's L&F
//        try
//        {
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//        }
//        catch (Exception except)
//        {
//            System.out.println("Error Loading L&F");
//        }

        WebDAVFrame = this;

        authTable = new Hashtable();
        //authHost = null;

        treeView = new WebDAVTreeView(WebDAVFrame);
        treeView.setUserAgent( UserAgent );
        fileView = new WebDAVFileView(WebDAVFrame);

        CommandMenu = new WebDAVMenu();
        setJMenuBar(CommandMenu);

        MenuListener_Gen menuListener = new MenuListener_Gen();

        CommandMenu.addWebDAVMenuListener( menuListener );

        // Set the HTTPClient authentication handler
        AuthorizationInfo.setAuthHandler( new AuthHandler() );
        // allow all cookies
        CookieModule.setCookiePolicyHandler( null );

        WebDAVToolBar toolbar = new WebDAVToolBar();
        toolbar.addActionListener( menuListener );
        URIBox uribox = new URIBox();
        uribox.addActionListener(new URIBoxListener_Gen());

        // Yuzo: Radical design change
        treeView.addViewSelectionListener( fileView );
        fileView.addViewSelectionListener( treeView );

        requestGenerator = new WebDAVRequestGenerator(WebDAVFrame);
        requestGenerator.addRequestListener(new RequestListener());

        // Get the rename Event
        fileView.addRenameListener(new RenameListener());

        requestGenerator.setUserAgent( UserAgent );


        responseInterpreter = new WebDAVResponseInterpreter(WebDAVFrame, requestGenerator);
        responseInterpreter.addInsertionListener(new InsertionListener());
        responseInterpreter.addMoveUpdateListener(new MoveUpdateListener());
        responseInterpreter.addLockListener(new LockListener());
        responseInterpreter.addActionListener(fileView); // Listens for a reset
							// for a unsucessful
							// Rename request

        // Yuzo Add the CopyEvent Listener
        responseInterpreter.addCopyResponseListener(treeView);
        responseInterpreter.addPutListener(treeView);

        webdavManager = new WebDAVManager(WebDAVFrame);
        webdavManager.addResponseListener(new ResponseListener());

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

        String doLog = System.getProperty( "debug" );
        if( doLog != null )
        {
            if( doLog.equalsIgnoreCase("all") )
                GlobalData.getGlobalData().setDebugAll( true );
            else if( doLog.equalsIgnoreCase( "request" ) )
                GlobalData.getGlobalData().setDebugRequest( true );
            else if( doLog.equalsIgnoreCase( "response" ) )
                GlobalData.getGlobalData().setDebugResponse( true );
            else if( doLog.equalsIgnoreCase( "treeview" ) )
                GlobalData.getGlobalData().setDebugTreeView( true );
            else if( doLog.equalsIgnoreCase( "treenode" ) )
                GlobalData.getGlobalData().setDebugTreeNode( true );
            else if( doLog.equalsIgnoreCase( "fileview" ) )
                GlobalData.getGlobalData().setDebugFileView( true );
        }

        getContentPane().add(p);
        treeView.initTree();
        pack();
        setVisible(true);

        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent we_Event)
            {
                System.exit(0);
            }
        } );
    }


    public final static void main(String[] argv)
    {
        Main mFrame = new Main("DAV Explorer");
    }


    class URIBoxListener_Gen implements WebDAVURIBoxListener
    {
        public void actionPerformed(ActionEvent e)
        {
            String str = e.getActionCommand();
            if (!str.endsWith("/"))
                str += "/";
            requestGenerator.setExtraInfo("uribox");

            // 1999-June-08, Joachim Feise (jfeise@ics.uci.edu):
            // workaround for IBM's DAV4J, which does not handle propfind properly
            // with the prop tag. To use the workaround, run DAV Explorer with
            // 'java -jar -Dpropfind=allprop DAVExplorer.jar'
            String doAllProp = System.getProperty( "propfind" );
            if( (doAllProp != null) && doAllProp.equalsIgnoreCase("allprop") )
	    {
                if( requestGenerator.GeneratePropFind( str, "allprop", "one", null, null, false ) )
                {
                    requestGenerator.execute();
                }
            }
            else
            {
                String[] props = new String[6];
                props[0] = "displayname";
                props[1] = "resourcetype";
                props[2] = "getcontenttype";
                props[3] = "getcontentlength";
                props[4] = "getlastmodified";
                props[5] = "lockdiscovery";
                if( requestGenerator.GeneratePropFind( str, "prop", "one", props, null, false ) )
                {
                    requestGenerator.execute();
	        }
	    }
        }
    }

    class InsertionListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            String str = e.getActionCommand();
            if (str == null)
            {
                treeView.refresh();
            } else{
                treeView.addRowToRoot(str,false);

        }
        }
    }

    class MoveUpdateListener implements ActionListener
    {
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

    class TableSelectListener_Gen implements ViewSelectionListener
    {
        public void selectionChanged(ViewSelectionEvent e)
        {
            requestGenerator.tableSelectionChanged(e);
        }
    }

    class TableSelectListener_Tree implements ViewSelectionListener
    {
        public void selectionChanged(ViewSelectionEvent e)
        {
            treeView.tableSelectionChanged(e);
        }
    }

    class TreeSelectListener_Gen implements ViewSelectionListener
    {
        public void selectionChanged(ViewSelectionEvent e)
        {
            requestGenerator.treeSelectionChanged(e);
        }
    }

    class LockListener implements ActionListener
    {
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

    class DisplayLockListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            requestGenerator.DiscoverLock("display");
        }
    }

    class RenameListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            String str = e.getActionCommand();
            if (str != null)
            {
                String s = fileView.getOldSelectedResource();
                WebDAVTreeNode n = fileView.getParentNode();
                requestGenerator.setResource(s, n);

                //requestGenerator.GenerateRename( str, treeView.getCurrentPath() );
                //requestGenerator.GenerateRename( str, fileView.getParentPath() );
                boolean retval = false;
		if( fileView.isSelectedLocked() ){
		    retval = requestGenerator.GenerateMove(str, fileView.getParentPath(), false, true, fileView.getSelectedLockToken(), "rename:" );
		} else {
		    retval = requestGenerator.GenerateMove(str, fileView.getParentPath(), false, true, null , "rename:" );
		}
                if( retval ){
                    requestGenerator.execute();
		}
            }
        }
    }

    class TreeSelectListener_Table implements ViewSelectionListener
    {
        public void selectionChanged(ViewSelectionEvent e)
        {
            fileView.treeSelectionChanged(e);
        }
    }

    class RequestListener implements WebDAVRequestListener
    {
        public void requestFormed(WebDAVRequestEvent e)
        {
            webdavManager.sendRequest(e);
        }
    }

    class ResponseListener implements WebDAVResponseListener
    {
        public void responseFormed(WebDAVResponseEvent e)
        {

            // This call process the info from the server
            responseInterpreter.handleResponse(e);


        // Post processing
        // These are actions designed to take place after the
        // response has been loaded

            String extra = e.getExtraInfo();

            String method = e.getMethodName();


            if ( method.equals("COPY") )
            {
                // Skip
            }
            else if (method.equals("PUT"))
            {
                // Skip
            }
            else if (extra == null)
            {
                // Skip
            }
            else if( extra.equals("expand") || extra.equals("index") )
            {
                WebDAVTreeNode tn = e.getNode();
                if (tn != null)
                {
                    tn.finishLoadChildren();

                }
            }
            else if ( extra.equals("select") )
            {
                WebDAVTreeNode tn = e.getNode();
                if (tn != null)
                {
                    tn.finishLoadChildren();


                    treeView.setSelectedNode(tn);
                }

            }
            else if ( extra.equals("uribox") )
            {
                WebDAVTreeNode tn = e.getNode();
            }
            else if ( extra.equals("copy") )
            {
            }
            else if (extra.equals("delete"))
            {
            }
            else if (extra.equals("mkcol"))
            {
            }
        }
    }

    class MenuListener_Gen implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            String command = e.getActionCommand();
            if (command.equals("Exit"))
                System.exit(0);

            else if (command.equals("Get File"))
            {
                saveAsDocument();
            }
            else if (command.equals("Write File"))
            {
                FileDialog fd = new FileDialog(WebDAVFrame, "Write File" , FileDialog.LOAD);

                if (writeToDir != null)
                {
                    fd.setDirectory(writeToDir);
                }

                fd.setVisible(true);

                String dirName =fd.getDirectory();

                String fName = fd.getFile();

                if( (dirName!=null) && !dirName.equals("") && (fName != null ) && !fName.equals("") )
                {
                    writeToDir = dirName;
                    String fullPath = dirName + fName;
                    String token = treeView.getLockToken( fName );

                    // Get the current Node so that we can update it later
                    String s = "";

                    WebDAVTreeNode n2 = fileView.getSelectedCollection();

                    s = fileView.getSelected();
                    if (s == null)
                    {
                        s = "";
                    }
                    WebDAVTreeNode parent = fileView.getParentNode();

                    boolean retval = false;
                    if (n2 == null)
                    {
                        requestGenerator.setResource(s, parent);
                        retval = requestGenerator.GeneratePut(fullPath, s, token , null);
                    }
                    else
                    {
                        requestGenerator.setResource(s, n2);
                        retval = requestGenerator.GeneratePut( fullPath, s, token , parent);
                    }
                    if( retval ){
                        requestGenerator.execute();
		    }
                }
            }
            else if (command.equals("Lock"))
            {
                String s = fileView.getSelected();
                if( s == null )
                {
                    errorMsg( "No file selected." );
                }
                else
                {
                    WebDAVTreeNode n = fileView.getParentNode();
                    //WebDAVTreeNode n2 = fileView.getSelectedCollection();
                    requestGenerator.setResource(s, n);
                    lockDocument();
                }
            }
            else if (command.equals("Unlock"))
            {
                String s = fileView.getSelected();
                if( s == null )
                {
                    errorMsg( "No file selected." );
                }
                else
                {
                    WebDAVTreeNode n = fileView.getParentNode();
                    requestGenerator.setResource(s, n);
                    unlockDocument();
                }
            }
            else if (command.equals("Duplicate"))
            {
                // Yuzo: I have the semantics set so that
                // we get a string if something is selected in the
                // FileView.
                String s = fileView.getSelected();
                if( s == null )
                {
                    errorMsg( "No file selected." );
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

                    if( requestGenerator.GenerateCopy( null, true, true ) ){
                        requestGenerator.execute();
		    }
                }
            }
            else if (command.equals("Delete"))
            {

                String s = fileView.getSelected();
                if( s == null )
                {
                    errorMsg( "No file selected." );
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
            }
            else if (command.equals("Create Collection"))
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
                            requestGenerator.setExtraInfo("mkcol");
                            retval = requestGenerator.GenerateMkCol( fileView.getParentPath(), dirname );
                        }
                        else
                        {
                            requestGenerator.setNode( selected );
                            requestGenerator.setExtraInfo("mkcolbelow");
                            retval = requestGenerator.GenerateMkCol( fileView.getSelected(), dirname );
                        }
                        if( retval ){
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
            }
            else if (command.equals("Clear Auth Buffer"))
            {
                authTable.clear();
            }
            else if (command.equals("Edit Lock Info"))
            {
                WebDAVLockInfo lockInfo = new WebDAVLockInfo(WebDAVFrame, "Lock Info", true);
            }
            else if (command.equals("HTTP Logging"))
            {
                boolean logging = false;
                String logFilename = null;
                if( CommandMenu.getLogging() )
                {
                    JOptionPane pane = new JOptionPane();
                    String message = new String( "WARNING: The logfile may get very large,\nsince all data is logged.\n" +
                                                 "Hit Cancel now if you don't want to log the data." );
                    int opt = pane.showConfirmDialog( WebDAVFrame, message, "HTTP Logging", JOptionPane.OK_CANCEL_OPTION );
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
                        int val = fd.showDialog( WebDAVFrame, "Use Selected" );
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
            }
            else if (command.equals("View Properties"))
            {
                viewProperties();
            }
            else if (command.equals("View Lock Properties"))
            {
                // Yuzo test to stop repeat
                String s = fileView.getSelected();
                if( s == null )
                {
                    errorMsg( "No file selected." );
                }
                else
                {
                    WebDAVTreeNode n = fileView.getParentNode();
                    requestGenerator.setResource(s, n);

                    requestGenerator.DiscoverLock("display");
                }
            }
            else if (command.equals("Refresh"))
            {
                WebDAVTreeNode n = fileView.getParentNode();
                responseInterpreter.setRefresh( n );
            }
            else if (command.equals("About DAV Explorer..."))
            {
                JOptionPane pane = new JOptionPane( this );
                String message = new String("DAV Explorer Version "+ VERSION + "\n" +
                "Copyright (c) 1999 Regents of the University of California\n" +
                "Authors: Yuzo Kanomata, Joachim Feise\n" +
                "EMail: dav-exp@ics.uci.edu\n\n" +
                "Based on code from the UCI WebDAV Client Group\n" +
                "of the ICS126B class Winter 1998:\n" +
                "Gerair Balian, Mirza Baig, Robert Emmery, Thai Le, Tu Le\n");
                Object [] options = { "OK" };
                pane.showOptionDialog(WebDAVFrame, message, "About DAV Explorer", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
            }
        }
    }

    protected void viewDocument()
    {
        if( requestGenerator.GenerateGet("view") ){
            requestGenerator.execute();
	}
    }

    protected void saveAsDocument()
    {
        String s = fileView.getSelected();
        if( s == null )
        {
            errorMsg( "No file selected." );
        }
        else
        {
            WebDAVTreeNode n = fileView.getParentNode();
            requestGenerator.setResource(s, n);
            if( requestGenerator.GenerateGet("saveas") ){
                requestGenerator.execute();
	    }
        }
    }

    protected void deleteDocument( boolean collection )
    {
        String s = fileView.getSelected();
        if( !fileView.hasSelected() )
        {
            errorMsg( "No file selected." );
        }
        else
        {
            JOptionPane pane = new JOptionPane();
            String str = null;
            String title = null;
            if( collection )
            {
                title = "Delete Collection";
                str = "Delete the collection and all its contents:\nAre you sure?";
            }
            else
            {
                title = "Delete File";
                str = "Delete: Are you sure?";
            }
            int opt = pane.showConfirmDialog( WebDAVFrame, str, title, JOptionPane.YES_NO_OPTION );
            if (opt == JOptionPane.YES_OPTION)
            {
                if( treeView.isRemote( s ) )
                {
                    WebDAVTreeNode n = fileView.getParentNode();
                    requestGenerator.setResource(s, n);
                    //requestGenerator.DiscoverLock("delete");
                    requestGenerator.setExtraInfo("delete");
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
                        errorMsg( "Delete Error on local filesystem." );
                    treeView.refreshLocal( n );
                }
            }
        }
    }

    protected void lockDocument()
    {
        requestGenerator.DiscoverLock("lock");
    }

    protected void unlockDocument()
    {
        requestGenerator.DiscoverLock("unlock");
    }

    protected void viewProperties()
    {
        String s = fileView.getSelected();
        if( s == null )
        {
            errorMsg( "No file selected." );
        }
        else
        {
            requestGenerator.setResource(s, null);
            requestGenerator.setExtraInfo("properties");
            if( requestGenerator.GeneratePropFind(null,"allprop","zero",null,null,false) ){
                requestGenerator.execute();
	    }
        }
    }


    private String selectName( String title, String prompt )
    {
        JOptionPane pane = new JOptionPane();
        String ret = pane.showInputDialog( WebDAVFrame, prompt, title ,JOptionPane.QUESTION_MESSAGE );
        return ret;
    }

    private void errorMsg(String str)
    {
        JOptionPane pane = new JOptionPane();
        Object [] options = { "OK" };
        pane.showOptionDialog( WebDAVFrame, str, "Error Message", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null,options, options[0]);
    }

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

    static JFrame getMainFrame()
    {
        return WebDAVFrame;
    }
}
