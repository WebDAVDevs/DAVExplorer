
import HTTPClient.HTTPConnection;
import HTTPClient.HTTPResponse;
import HTTPClient.RoRequest;
import HTTPClient.Response;
import HTTPClient.RoResponse;
import HTTPClient.AuthorizationInfo;
import HTTPClient.AuthorizationHandler;
import HTTPClient.AuthSchemeNotImplException;
import java.net.URL;
import java.io.IOException;


public class GetAuthInfo
{
    public static void main(String args[])  throws Exception
    {
	String pa_name = null, pa_pass = null;

	if (args.length == 4  &&  "-proxy_auth".startsWith(args[0]))
	{
	    pa_name = args[1];
	    pa_pass = args[2];

	    String[] tmp = { args[3] };
	    args = tmp;
	}

	if (args.length != 1  ||  args[0].equalsIgnoreCase("-help"))
	{
	    System.err.println("Usage: java GetAuthInfo [-proxy_auth <username> <password>] <url>");
	    System.exit(1);
	}

	URL url = new URL(args[0]);

	AuthorizationInfo.setAuthHandler(new MyAuthHandler(
			AuthorizationInfo.getAuthHandler(), pa_name, pa_pass));
	HTTPConnection con = new HTTPConnection(url);
	HTTPResponse   rsp = con.Head(url.getFile());

	int sts = rsp.getStatusCode();
	if (sts < 300)
	    System.out.println("No authorization required to access " + url);
	else if (sts >= 400  &&  sts != 401  &&  sts != 407)
	    System.out.println("Error trying to access " + url + ":\n" + rsp);
    }
}


class MyAuthHandler implements AuthorizationHandler
{
    private String pa_name, pa_pass;
    private AuthorizationHandler def_handler;
    private boolean been_here;

    public MyAuthHandler(AuthorizationHandler def, String pa_name,
			 String pa_pass)
    {
	def_handler  = def;
	this.pa_name = pa_name;
	this.pa_pass = pa_pass;
	been_here    = false;
    }


    public AuthorizationInfo getAuthorization(AuthorizationInfo challenge,
					      RoRequest req, RoResponse resp)
    {
	// create and send auth info if necessary

	try
	{
	    pa: if (resp.getStatusCode() == 407  &&  pa_name != null)
	    {
		if (been_here)
		{
		    System.out.println();
		    System.out.println("Proxy authorization failed");
		    return null;
		}

		been_here = true;

		if (challenge.getScheme().equalsIgnoreCase("Basic"))
		    AuthorizationInfo.addBasicAuthorization(
						       challenge.getHost(),
						       challenge.getPort(),
						       challenge.getRealm(),
						       pa_name, pa_pass);
		else if (challenge.getScheme().equalsIgnoreCase("Digest"))
		    AuthorizationInfo.addDigestAuthorization(
						       challenge.getHost(),
						       challenge.getPort(),
						       challenge.getRealm(),
						       pa_name, pa_pass);
		else
		    break pa;

		AuthorizationInfo info = AuthorizationInfo.getAuthorization(
				challenge.getHost(), challenge.getPort(),
				challenge.getScheme(), challenge.getRealm());
		return def_handler.fixupAuthInfo(info, req, challenge, resp);
	    }
	}
	catch (Exception e)
	{
	    System.out.println("Error reading response: " + e);
	    return null;
	}

	if (been_here)
	{
	    System.out.println();
	    System.out.println("Proxy authorization succeeded");
	}


	// print out all challenge info

	System.out.println();
	try
	{
	    if (resp.getStatusCode() == 407)
		System.out.println("The proxy requires authorization");
	    else
		System.out.println("The server requires authorization for this resource");
	}
	catch(IOException ioe)
	{
	    System.out.println("Error reading response: " + ioe);
	    return null;
	}

	System.out.println();
	System.out.println("Scheme: " + challenge.getScheme());
	System.out.println("Realm:  " + challenge.getRealm());

	System.out.println();
	System.out.println("Add the following line near the beginning of your application:");
	System.out.println();

	if (challenge.getScheme().equalsIgnoreCase("Basic"))
	    System.out.println("    AuthorizationInfo.addBasicAuthorization(\""+
			       challenge.getHost() + "\", " +
			       challenge.getPort() + ", \"" +
			       challenge.getRealm() + "\", " +
			       "<username>, <password>);");
	else if (challenge.getScheme().equalsIgnoreCase("Digest"))
	    System.out.println("    AuthorizationInfo.addDigestAuthorization(\"" +
			       challenge.getHost() + "\", " +
			       challenge.getPort() + ", \"" +
			       challenge.getRealm() + "\", " +
			       "<username>, <password>);");
	else
	    System.out.println("    AuthorizationInfo.addAuthorization(\"" +
			       challenge.getHost() + "\", " +
			       challenge.getPort() + ", \"" +
			       challenge.getScheme() + "\", \"" +
			       challenge.getRealm() + "\", " +
			       "...);");
	System.out.println();

	return null;
    }


    public AuthorizationInfo fixupAuthInfo(AuthorizationInfo info,
					   RoRequest req,
					   AuthorizationInfo challenge,
					   RoResponse resp)
	    throws AuthSchemeNotImplException
    {
	return def_handler.fixupAuthInfo(info, req, challenge, resp);
    }

    public void handleAuthHeaders(Response resp, RoRequest req,
				  AuthorizationInfo prev,
				  AuthorizationInfo prxy)
	    throws IOException
    {
	def_handler.handleAuthHeaders(resp, req, prev, prxy);
    }

    public void handleAuthTrailers(Response resp, RoRequest req,
				   AuthorizationInfo prev,
				   AuthorizationInfo prxy)
	    throws IOException
    {
	def_handler.handleAuthTrailers(resp, req, prev, prxy);
    }
}

