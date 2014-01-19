package project.rayedchan.constants;

/**
 * @author rayedchan
 * A constants class.
 * Adjust these constants according to your environment
 */
public class Constants 
{
    //OIM
    public static String OIM_SERVER_URL = "t3://localhost:14000"; 
    public static String OIM_USER = "xelsysadm"; //OIM Administrator
    public static String OIM_USER_PASSWORD = "Password1"; //OIM Administrator Password
    
    //Oracle Database 
    public static String ORACLE_DATABASE_URL = "jdbc:oracle:thin:@localhost:1521:orcl";
    public static String ORACLE_DATABASE_OIM_SCHEMA = "DEV_OIM";
    public static String ORACLE_DATABASE_OIM_SCHEMA_PASSWORD = "Password1";
    
    //WebLogic Configurations
    public static String AUTHWL_CONFIG_PATH = "resources/oimConfig/authwl.conf"; //can be found in "<IDM_HOME>/designconsole/config" directory
    public static String WL_CXTFACTORY = "weblogic.jndi.WLInitialContextFactory";
    public static String PROPERTY_AUTHWL_CONFIG ="java.security.auth.login.config";
    public static String APPSERVER_TYPE = "wls";
    
}
