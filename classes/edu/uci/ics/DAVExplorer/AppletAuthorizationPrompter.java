package edu.uci.ics.DAVExplorer;

import HTTPClient.AuthorizationPrompter;
import HTTPClient.NVPair;
import HTTPClient.AuthorizationInfo;

/**
 * Created by IntelliJ IDEA.
 * User: Brian Johnson
 * Date: Mar 4, 2003
 * Time: 1:44:09 AM
 * To change this template use Options | File Templates.
 */
public class AppletAuthorizationPrompter implements AuthorizationPrompter {
    static int index = 0;

    public static void reset() {
        index = 0;
    }

    public NVPair getUsernamePassword(AuthorizationInfo challenge, boolean forProxy) {
        String[] siteInfo = GlobalData.getGlobalData().getInitialSites()[index];
        String username = siteInfo[1];
        String password = siteInfo[2];
        return new NVPair(username, password);
    }
}
