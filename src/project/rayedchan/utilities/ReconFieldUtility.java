package project.rayedchan.utilities;

import Thor.API.Exceptions.tcAPIException;
import Thor.API.Exceptions.tcBulkException;
import Thor.API.Operations.tcExportOperationsIntf;
import Thor.API.Operations.tcImportOperationsIntf;
import com.thortech.xl.ddm.exception.DDMException;
import com.thortech.xl.ddm.exception.TransformationException;
import com.thortech.xl.vo.ddm.RootObject;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import project.rayedchan.custom.objects.ReconciliationField;

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
           
    public static String RECON_FIELD_TAG = "ReconField"; //xml reconfield tag name
        
    //ReconField Attribute tags
    public static String ORF_UPDATE_TAG = "ORF_UPDATE";
    public static String ORF_FIELDTYPE_TAG = "ORF_FIELDTYPE";
    public static String ORF_REQUIRED_TAG = "ORF_REQUIRED";
    
    /*
     * Adds a reconciliation field to the resource xml.
     * Sample data added to xml
     * <ReconField repo-type="RDBMS" name="test3">
     * <ORF_UPDATE>1361032854000</ORF_UPDATE>
     * <ORF_FIELDTYPE>String</ORF_FIELDTYPE>
     * <ORF_REQUIRED>0</ORF_REQUIRED>
     * </ReconField
     * 
     * Set the ORF_UPDATE to the date of when the object was last updated (OBJ_UPDATE).
     * 
     * @param 
     *      document - object representation of an object resource xml
     *      newReconFieldToAdd - reconciliation field to add to document
     */
    public static void addReconField(Document document, ReconciliationField newReconFieldToAdd) throws XPathExpressionException
    {          
        String reconFieldName = newReconFieldToAdd.getReconFieldName();
        String reconFieldType = newReconFieldToAdd.getReconFieldType();
        Boolean isRequired = newReconFieldToAdd.getIsRequired();
        String reconFieldUpdateTimestamp = getResourceObjectUpdateTimestamp(document);
                
        //Locate proper level to add the new recon field into the xml
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();
        NodeList nodes =  (NodeList) xpath.evaluate("xl-ddm-data/Resource", document, XPathConstants.NODESET);
            
        //ReconField tag and its properties
        Element newReconField = document.createElement(RECON_FIELD_TAG);
        Element rfAttrUpdate = document.createElement(ORF_UPDATE_TAG);
        Element rfAttrFieldType = document.createElement(ORF_FIELDTYPE_TAG);
        Element rfAttrIsRequired = document.createElement(ORF_REQUIRED_TAG);
            
        //Set ReconField tag attributes and properties
        newReconField.setAttribute("repo-type", "RDBMS"); 
        newReconField.setAttribute("name", reconFieldName); 
        rfAttrUpdate.setTextContent(reconFieldUpdateTimestamp);
        rfAttrFieldType.setTextContent(reconFieldType);
        String isRequiredString = isRequired ? "1" : "0";
        rfAttrIsRequired.setTextContent(isRequiredString);
        
        //Append properties to the ReconField tag
        newReconField.appendChild(rfAttrUpdate);
        newReconField.appendChild(rfAttrFieldType);
        newReconField.appendChild(rfAttrIsRequired);
        
        //Get the resource node tag and insert reconField within the resource tag
        Node resourceNode = nodes.item(0); 
        resourceNode.appendChild(newReconField); 
    }
    
    
    /*
     * Export a resource object XML
     * @params 
     *      exportOps - tcExportOperationsIntf service object
     *      resourceObjectName - name of resource object to export
     * 
     * @return - the XML of the resource as a String
     */
    public static String exportResourceObject(tcExportOperationsIntf exportOps, String resourceObjectName) throws tcAPIException
    {
         String type = "Resource";
         String description = null;
         Collection resourceObject = exportOps.findObjects(type, resourceObjectName);
         int numObjects = resourceObject.size();
         
         //enforce one resource object to be exported at a time
         if(numObjects == 1)
         {
             String resourceObjectXML = exportOps.getExportXML(resourceObject, description);
             return resourceObjectXML;
         }
         
         System.out.println("Only one object can be exported at a time.");
         return null;
    }
    
    /*
     * Import resource object XML into OIM
     * @params 
     *      importOps - tcImportOperationsIntf service object
     *      newObjectResourceXML - xml content to be imported
     *      fileName - File name of the file being imported. For tracking purposes.
     * 
     * @return - the XML of the resource as a String
     */
    public static void importResourceObject(tcImportOperationsIntf importOps, String newObjectResourceXML, String fileName) throws SQLException, NamingException, DDMException, tcAPIException, TransformationException, tcBulkException
    {     
        importOps.acquireLock(true);
        Collection<RootObject> justImported = importOps.addXMLFile(fileName, newObjectResourceXML);
        importOps.performImport(justImported);
    }
    
    /*
     * Get the update timestamp of a resource object.
     * @param
     *        document - object representation of an object resource xml
     * 
     * return - timestmap given in the resource object xml
     */
    public static String getResourceObjectUpdateTimestamp(Document document) throws XPathExpressionException
    {         
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();
        XPathExpression expr = xpath.compile("//OBJ_UPDATE"); //Get all tags with "ReconField" tag name regardless of depth
        NodeList nodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
        int numReconFieldNodes = nodes.getLength();
 
        for(int i = 0; i < numReconFieldNodes; i++)
        {
            Node node = nodes.item(i);
            String textContent = node.getTextContent();
            
            if(textContent != null || !textContent.isEmpty())
            {
                return textContent;
            }
        }
        
        return null;
    }
    
    
    /*
     * Determine if a resource object exists.
     * This method is case senstive.
     * 
     * @params 
     *      conn - connection to the OIM Schema 
     *      resourceObjectName - resource object name (OBJ.OBJ_NAME)  
     * 
     * @return - true if resource object exists; false otherwise
     */
    public static Boolean doesResourceObjectExist(Connection conn, String resourceObjectName)
    {
        Statement st = null;
        ResultSet rs = null;
            
        try 
        {
            String query = "SELECT COUNT(*) AS numRows FROM OBJ WHERE OBJ_NAME = '" + resourceObjectName + "'";
            st = conn.createStatement();
            rs = st.executeQuery(query);
            rs.next();
            
            if(Integer.parseInt(rs.getString("numRows")) == 1)
            {
               return true;  
            }    
        } 
        
        catch (SQLException ex)
        {
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
        
        return false;
    }
    
    /*
     * Print all the Resource Objects in OIM. Queries from the OBJ table.
     * @param 
     *      conn - connection to the OIM Schema 
     */
    public static void printAllResourceObjects(Connection conn) 
    {
        Statement st = null;
        ResultSet rs = null;
            
        try 
        {
            String query = "SELECT OBJ_KEY, OBJ_TYPE, OBJ_NAME FROM OBJ ORDER BY OBJ_NAME";
            st = conn.createStatement();
            rs = st.executeQuery(query);

            System.out.printf("%-25s%-25s%-25s\n", "Object Key", "Object Name", "Object Type");
            while(rs.next())
            {
                String objectKey = rs.getString("OBJ_KEY");
                String objectName = rs.getString("OBJ_NAME");
                String objectType = rs.getString("OBJ_TYPE"); 
                System.out.printf("%-25s%-25s%-25s\n", objectKey, objectName, objectType); 
            
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
