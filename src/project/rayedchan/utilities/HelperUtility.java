package project.rayedchan.utilities;

import Thor.API.Exceptions.tcAPIException;
import Thor.API.Exceptions.tcColumnNotFoundException;
import Thor.API.tcResultSet;

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
    
}
