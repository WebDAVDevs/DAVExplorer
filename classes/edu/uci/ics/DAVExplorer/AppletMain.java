package edu.uci.ics.DAVExplorer;

import java.util.StringTokenizer;

/**
 * Applet version of the DAV Explorer
 */
public class AppletMain extends javax.swing.JApplet {

    public void init()
    {
    }


    public void start()
    {
        // clear tree
        GlobalData.reset();
        GlobalData.getGlobalData().setAddStartDir(false);
        GlobalData.getGlobalData().setAppletMode(parseBooleanParameter("appletMode"));
        GlobalData.getGlobalData().setHideURIBox(parseBooleanParameter("hideURIBox"));

        // register the initial tree nodes and get 'em going
        GlobalData.getGlobalData().setInitialSites(getUriUnPwParameters());
        setRootPane(new Main("DAV Explorer Applet").getRootPane());
        GlobalData.getGlobalData().getTree().initTree();
    }


    public void stop()
    {
        GlobalData.reset();
    }


    private boolean parseBooleanParameter(String name)
    {
        String appletMode = getParameter(name);
        if (appletMode != null) {
            return Boolean.valueOf(appletMode).booleanValue();
        }
        return true;
    }


    private String[][] getUriUnPwParameters()
    {
        // array of triplets: URI, Username, Password
        String[][] retVal = new String[][] { {} };

//        retVal = new String[][] {
//            { "http://dav.somewhere.com/webdav", "username", "password"}
//        };

        String uri = getParameter("uri");
        String username = getParameter("username");
        String password = getParameter("password");

        if( uri == null )
            return retVal;

        StringTokenizer uriST = new StringTokenizer(uri, "||");

        if (uriST.countTokens() == 0)
            return retVal;

        // allocate the first level of the array
        retVal = new String[uriST.countTokens()][];
        int index = 0;
        while (uriST.hasMoreElements()) {
            retVal[index] = new String[3];
            retVal[index][0] = uriST.nextToken();
            retVal[index][1] = username;
            retVal[index][2] = password;
            index++;
        }

        return retVal;
    }
}
