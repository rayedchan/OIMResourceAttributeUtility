package project.rayedchan.services;

import Thor.API.Security.XLClientSecurityAssociation;
import com.thortech.xl.client.dataobj.tcDataBaseClient;
import com.thortech.xl.dataaccess.tcClientDataAccessException;
import com.thortech.xl.dataaccess.tcDataProvider;
import com.thortech.xl.dataaccess.tcDataSet;
import com.thortech.xl.dataaccess.tcDataSetException;
import com.thortech.xl.orb.dataaccess.tcDataAccessException;
import oracle.iam.platform.OIMClient;

/**
 * @author rayedchan
 * Connects to your OIM Schema by providing a OIM client with a user
 * who is logged in. 
 */
public class tcOIMDatabaseConnection 
{
    private tcDataProvider dbProvider;
        
    /*
     * Constructor
     * @param   oimClient   OIM Client with a user logged in
     */
    public tcOIMDatabaseConnection(OIMClient oimClient) throws tcDataSetException
    {           
        //Establish connection to OIM Schema through the OIMClient
        XLClientSecurityAssociation.setClientHandle(oimClient);
        dbProvider = new tcDataBaseClient();  
        validateConnection();
    }
    
    /*
     * Validate the database connection
     */
    public final void validateConnection() throws tcDataSetException
    {   
        String query = "SELECT 1 FROM USR"; //Query all OIM users 
        tcDataSet usersDataSet = new tcDataSet(); //store result set of query 
        usersDataSet.setQuery(this.dbProvider, query);
        usersDataSet.executeQuery();
        int numRecords = usersDataSet.getTotalRowCount();
        System.out.println("User records: " + numRecords);
    }
    
    /*
     * Get tcDataProvider object
     */
    public tcDataProvider getDbProvider()
    {
        return this.dbProvider;
    }
    
    /*
     * Clear database sessions
     */
    public void closeResource() throws tcDataAccessException, tcClientDataAccessException
    {
        if(this.dbProvider != null)
        {
            this.dbProvider.close();
        }
        
        XLClientSecurityAssociation.clearThreadLoginSession(); 
    }
}
