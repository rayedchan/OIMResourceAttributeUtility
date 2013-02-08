package project.rayedchan.services;

import java.util.Hashtable;
import javax.security.auth.login.LoginException;
import oracle.iam.platform.OIMClient;
import project.rayedchan.constants.Constants;

/**
 * @author rayedchan
 * OIMClient class
 */
public class OIMClientResourceAttr 
{
    private OIMClient oimClient; 
    
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
     * Login to OIM with a client.
     */
    private void oimLogin(OIMClient oimClient) throws LoginException
    {
        String username = Constants.OIM_USER; //OIM Administrator 
        String password = Constants.OIM_USER_PASSWORD; //Administrator Password
        oimClient.login(username, password.toCharArray()); //login to OIM  
    }
    
    /*
     * Get the OIMClient Object.
     */
    public OIMClient getOIMClient()
    {
        return this.oimClient;
    }
     
}
