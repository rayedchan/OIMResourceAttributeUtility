package project.rayedchan.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import project.rayedchan.constants.Constants;

/*
 *@author rayedchan
 *Establish connection to the OIM Schema.
 *The "ojdbc.jar" file is required.
 *Download Link: http://www.oracle.com/technetwork/database/enterprise-edition/jdbc-112010-090769.html
 *Since I am using Oracle Database 11g Release 2 (11.2.0.1.0), I'll be using the JDBC Driver that version. 
 */
public class OIMDatabaseConnection 
{
    private Connection oracleDBConnection;
    
    /*
     * Constructor
     */
    public OIMDatabaseConnection() throws SQLException
    {
         establishConnection();
    }
    
    public Connection getOracleDBConnction()
    {
        return this.oracleDBConnection;
    }
    
    private void establishConnection() throws SQLException
    {
        String url = Constants.ORACLE_DATABASE_URL;
        String user = Constants.ORACLE_DATABASE_OIM_SCHEMA;
        String password = Constants.ORACLE_DATABASE_OIM_SCHEMA_PASSWORD;
        
        DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());//Load and register Oracle JDBC Driver 
        this.oracleDBConnection = DriverManager.getConnection(url, user, password); //Connect to database
    }
   
}