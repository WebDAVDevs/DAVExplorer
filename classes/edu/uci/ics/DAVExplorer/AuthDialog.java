
/**
 * Title:        DAV Explorer<p>
 * Description:  <p>
 * Copyright:    Copyright (c) 1999-2001 U.C. Regents<p>
 * Company:      University of California, Irvine<p>
 * @author Joachim Feise
 * @version
 */
package DAVExplorer;

import HTTPClient.AuthorizationPrompter;
import HTTPClient.AuthorizationInfo;
import HTTPClient.NVPair;

public class AuthDialog implements AuthorizationPrompter
{

    public AuthDialog()
    {
    }

    /**
     * the method called by DefaultAuthHandler.
     *
     * @return the username/password pair
     */
    public NVPair getUsernamePassword(AuthorizationInfo info, boolean forProxy)
    {
        WebDAVLoginDialog dlg = new WebDAVLoginDialog( "Login", info.getRealm(), info.getScheme(), true );
        if( dlg.getUsername().equals( "" ) || dlg.getUserPassword().equals( "" ) )
            return null;

        NVPair answer;
        answer = new NVPair( dlg.getUsername(), dlg.getUserPassword() );
        return answer;
    }
}
