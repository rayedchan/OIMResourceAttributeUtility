package project.rayedchan.constants;

/**
 * @author rayedchan
 * A constants class.
 * 
 */
public class Constants 
{
    //Adjust these constants according to your environment
    public static String AUTHWL_CONFIG_PATH = "/home/oracle/oimClient_lib/conf/authwl.conf"; //can be found in designconsole/conf
    public static String OIM_SERVER_URL = "t3://localhost:14000"; 
    public static String OIM_USER = "xelsysadm"; //OIM Administrator
    public static String OIM_USER_PASSWORD = "Password1"; //OIM Administrator Password
    
    public static String WL_CXTFACTORY = "weblogic.jndi.WLInitialContextFactory";
    public static String PROPERTY_AUTHWL_CONFIG ="java.security.auth.login.config";
    
}
