package project.rayedchan.services;

import java.util.Hashtable;
import javax.security.auth.login.LoginException;
import oracle.iam.platform.OIMClient;
import oracle.iam.selfservice.uself.uselfmgmt.api.UnauthenticatedSelfService;
import project.rayedchan.constants.Constants;

/**
 * @author rayedchan
 * OIM client class to interact with an Oracle Identity Manager instance remotely.
 */
public class OIMClientResourceAttr 
{
    private OIMClient oimClient; 
     
    /*
     * Constructor used to test authentication of an OIM user
     * in a given OIM environment.
     * @param   oimServerURL        Uniform Resource Location of OIM environment
     * @param   username            OIM Administrator username
     * @param   password            OIM Administrator password
     */
    public OIMClientResourceAttr(String oimServerURL, String username, char[] password, String authwlPath) throws LoginException
    { 
        String ctxFactory = Constants.WL_CXTFACTORY; //WebLogic Context
        System.setProperty(Constants.PROPERTY_AUTHWL_CONFIG, authwlPath); //set the login configuration
        System.setProperty("APPSERVER_TYPE", Constants.APPSERVER_TYPE);  
        Hashtable<String,String> env = new Hashtable<String,String>(); //use to store OIM environment properties
        env.put(OIMClient.JAVA_NAMING_FACTORY_INITIAL, ctxFactory);
        env.put(OIMClient.JAVA_NAMING_PROVIDER_URL, oimServerURL);
        this.oimClient = new OIMClient(env); //Create an OIMClient object
        this.oimClient.getService(UnauthenticatedSelfService.class); //Test OIM service access in order to validate OIM URL.
        this.oimLogin(username, password); //Attempts to login user in OIM
    }
    
    /*
     * Get the OIMClient Object.
     */
    public OIMClient getOIMClient()
    {
        return this.oimClient;
    }
       
    /*
     * Login a user for the OIMClient.
     * @param   username    OIM Administrator username
     * @param   password    OIM Administrator password 
     */
    private void oimLogin(String username, char[] password) throws LoginException
    {
        oimClient.login(username, password); //login to OIM  
    }
       
    /*
     * Testing Constructor
     */
    public OIMClientResourceAttr() throws LoginException
    { 
        String ctxFactory = Constants.WL_CXTFACTORY; //WebLogic Context
        String oimServerURL = Constants.OIM_SERVER_URL; //OIM URL
        String authwlConfigPath = Constants.AUTHWL_CONFIG_PATH; //Path to login configuration
        
        System.setProperty(Constants.PROPERTY_AUTHWL_CONFIG, authwlConfigPath); //set the login configuration
        Hashtable<String,String> env = new Hashtable<String,String>(); //use to store OIM environment properties
        env.put(OIMClient.JAVA_NAMING_FACTORY_INITIAL, ctxFactory);
        env.put(OIMClient.JAVA_NAMING_PROVIDER_URL, oimServerURL);
        this.oimClient = new OIMClient(env);
        this.oimLogin(oimClient);
    }
        
    /*
     * Testing method 
     */
    private void oimLogin(OIMClient oimClient) throws LoginException
    {
        String username = Constants.OIM_USER; //OIM Administrator 
        String password = Constants.OIM_USER_PASSWORD; //Administrator Password
        oimClient.login(username, password.toCharArray()); //login to OIM  
    }
}
