package project.rayedchan.utilities;

import Thor.API.Exceptions.tcAPIException;
import Thor.API.Exceptions.tcColumnNotFoundException;
import Thor.API.tcResultSet;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author rayedchan
 * A class that contains helpful miscellaneous methods.
 */
public class HelperUtility 
{
    /*
     * Prints the column names of a tcResultSet.
     * @param 
     *      tcResultSetObj - tcResultSet Object
     */
    public static void printTcResultSetColumnNames(tcResultSet tcResultSetObj)
    {
        String[] columnNames = tcResultSetObj.getColumnNames();
        
        for(String columnName: columnNames)
        {
            System.out.println(columnName);
        }
    }
    
    /*
     * Prints the records of a tcResultSet.
     * @param -
     *      tcResultSetObj - tcResultSetObject
     */
    public static void printTcResultSetRecords(tcResultSet tcResultSetObj) throws tcAPIException, tcColumnNotFoundException
    {
        String[] columnNames = tcResultSetObj.getColumnNames();
        int numRows = tcResultSetObj.getTotalRowCount();
        
        for(int i = 0; i < numRows; i++)
        {
            tcResultSetObj.goToRow(i);
            for(String columnName: columnNames)
            {
                System.out.println(columnName + " = " + tcResultSetObj.getStringValue(columnName));
            }
            System.out.println();
        }
    }
    
    /*
     * Print the records of the ResultSet. 
     * @param -
     *      resultSet - the result set of a query
     */
    public static void printResultSetRecords(ResultSet resultSet) throws SQLException
    {
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int columnCount = resultSetMetaData.getColumnCount();
        
        //iterate all the records from the result set
        while(resultSet.next())
        {
            //iterate all the columns of a record and print value. Column count starts at one.
            for(int i = 1; i < columnCount + 1; i++)
            {
                String columnName = resultSetMetaData.getColumnName(i);
                String columnValue = resultSet.getString(columnName);
                System.out.println(columnName + " = " + columnValue);
            }
            
            System.out.println();
           
        }
  
    }
    
    /*
     * Print all the Objects in OIM. Queries from the OBJ table.
     * @param 
     *      conn - connection to the OIM Schema 
     */
    public static void printAllOIMObjects(Connection conn) 
    {
        Statement st = null;
        ResultSet rs = null;
            
        try 
        {
            String query = "SELECT OBJ_KEY, SDK_KEY, OBJ_TYPE, OBJ_NAME FROM OBJ ORDER BY OBJ_NAME";
            st = conn.createStatement();
            rs = st.executeQuery(query);

            System.out.printf("%-25s%-25s%-25s%-25s\n", "Object Key", "Object Name", "Object Type", "SDK_KEY");
            while(rs.next())
            {
                String objectKey = rs.getString("OBJ_KEY");
                String objectName = rs.getString("OBJ_NAME");
                String objectType = rs.getString("OBJ_TYPE"); 
                String formKey = rs.getString("SDK_KEY");
                System.out.printf("%-25s%-25s%-25s%-25s\n", objectKey, objectName, objectType, formKey); 
            
            }
        } 
        
        catch (SQLException ex) {
            Logger.getLogger(HelperUtility.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        finally
        {
            if(rs != null)
            {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    Logger.getLogger(HelperUtility.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            if(st != null)
            {
                try {
                    st.close();
                } catch (SQLException ex) {
                    Logger.getLogger(HelperUtility.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        }

    }
    
    /*
     * Print all process definitons with their corresponding keys.
     * TOS = Process Definition
     * SDK = Structure Utility (Process Form)
     * OBJ = Resource Object Definition
     * PKG = Service Processes
     * 
     * @param 
     *      conn - connection to the OIM Schema 
     */
    public static void getAllProcessDefinitions(Connection conn)
    {
        Statement st = null;
        ResultSet rs = null;
        
        try 
        {
            String query = "SELECT PKG.PKG_KEY, TOS.TOS_KEY, SDK.SDK_KEY, PKG.PKG_NAME, SDK.SDK_NAME, OBJ.OBJ_KEY, OBJ.OBJ_NAME FROM "
             + "TOS RIGHT OUTER JOIN PKG ON PKG.PKG_KEY = TOS.PKG_KEY "
             + "LEFT OUTER JOIN SDK ON SDK.SDK_KEY = TOS.SDK_KEY "
             + "LEFT OUTER JOIN OBJ ON OBJ.OBJ_KEY = PKG.OBJ_KEY ORDER BY PKG.PKG_NAME";
            st = conn.createStatement(); //Create a statement
            rs = st.executeQuery(query);
            
            System.out.printf("%-25s%-25s%-25s%-25s%-25s%-25s%-25s\n", "Package Key", "Package Name","Process Key", "Object Key", "Object Name", "Form Key", "Form Name");
            while(rs.next())
            {
                String packageKey = rs.getString("PKG_KEY");
                String packageName = rs.getString("PKG_NAME");
                String processKey = rs.getString("TOS_KEY");
                String objectKey = rs.getString("OBJ_KEY");
                String objectName = rs.getString("OBJ_NAME");
                String formName = rs.getString("SDK_NAME");
                String formKey = rs.getString("SDK_KEY");
                
                System.out.printf("%-25s%-25s%-25s%-25s%-25s%-25s%-25s\n",packageKey, packageName, processKey, objectKey, objectName, formKey, formName);
            }
        } 
        
        catch (SQLException ex) {
            Logger.getLogger(HelperUtility.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        finally
        {
            if(rs != null)
            {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    Logger.getLogger(HelperUtility.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            if(st != null)
            {
                try {
                    st.close();
                } catch (SQLException ex) {
                    Logger.getLogger(HelperUtility.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        }
        
    }
    
    /*
     * Determine if a string can be parse into an integer.
     * @param 
     *       strValue - validate if string value can be parsed 
     * 
     * @return - boolean value to indicate if string is an integer
     */
    public static boolean isInteger(String strValue)
    {
        try 
        {
            Integer.parseInt(strValue);
            return true;
        }
        
        catch (NumberFormatException nfe)
        {
            return false;
        }
    }
    
     /*
     * Determine if a string can be parse into an boolean.
     * @param 
     *       strValue - validate if string value can be parsed 
     * 
     * @return - boolean value to indicate if string is a boolean
     */
    public static boolean isBoolean(String strValue)
    {
        strValue = strValue.toLowerCase();
        return "true".equalsIgnoreCase(strValue) || "false".equalsIgnoreCase(strValue);
    }
     
     
}
