package project.rayedchan.utilities;

import Thor.API.Exceptions.tcAPIException;
import Thor.API.Exceptions.tcColumnNotFoundException;
import Thor.API.tcResultSet;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

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
    
    
    
}
