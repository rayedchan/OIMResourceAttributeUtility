package project.rayedchan.utilities;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author rayedchan
 * This utility allows you to add and delete a reconciliation field 
 * from an object resource. Removal and creation of a reconciliation field 
 * are done at the meta-data level.
 * 
 * Note: When using this utility to add or remove reconciliation fields, 
 * the "Create Reconciliation Profile" button in design console may have to be
 * clicked.(Need to verify this)
 * 
 * Add "xlDDM.jar" is required to use tcImportOperationsIntf service 
 * "/home/oracle/Oracle/Middleware/Oracle_IDM1/designconsole/lib" 
 * directory to classpath in order to use 
 */
public class ReconFieldUtility 
{
    /*
     * Print all the reconciliation fields in a resource object.
     * Table ORF - contains all the reconciliation fields  
     * @param 
     *      conn - connection to the OIM Schema 
     *      objectKey - resource object key (ORF.OBJ_KEY)    
     */
    public static void printReconFieldsofResourceObject(Connection conn, long objectKey) 
    {
        Statement st = null;
        ResultSet rs = null;
            
        try 
        {
            String query = "SELECT ORF_KEY, ORF_FIELDNAME, ORF_FIELDTYPE FROM ORF WHERE OBJ_KEY = "+ objectKey + " ORDER BY ORF_FIELDNAME";
            st = conn.createStatement();
            rs = st.executeQuery(query);

            System.out.printf("%-25s%-25s%-25s\n", "ReconFieldKey", "ReconFieldName", "FieldType");
            while(rs.next())
            {
                String reconFieldKey = rs.getString("ORF_KEY");
                String reconFieldName = rs.getString("ORF_FIELDNAME"); 
                String reconFieldType = rs.getString("ORF_FIELDTYPE");
                System.out.printf("%-25s%-25s%-25s\n", reconFieldKey, reconFieldName, reconFieldType); 
            
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
}
