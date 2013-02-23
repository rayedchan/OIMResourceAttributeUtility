package project.rayedchan.utilities;

import Thor.API.Exceptions.tcAPIException;
import Thor.API.Exceptions.tcBulkException;
import Thor.API.Operations.tcExportOperationsIntf;
import Thor.API.Operations.tcImportOperationsIntf;
import com.thortech.xl.ddm.exception.DDMException;
import com.thortech.xl.ddm.exception.TransformationException;
import com.thortech.xl.vo.ddm.RootObject;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import project.rayedchan.custom.objects.ReconciliationField;

/**
 * @author rayedchan
 * This utility allows you to add and delete a reconciliation field 
 * from an object resource. Removal and creation of a reconciliation field 
 * are done at the meta-data level.
 * 
 * Note: When using this utility to add reconciliation fields, 
 * the "Create Reconciliation Profile" is initiated by the import utility.
 * The import utility merges with the existing data. If data is removed from the
 * xml and imported back, that data will be removed if and only if there is 
 * no mapping for the reconciliation field.
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
    
    //Possible reconciliation field type
    public static String RECON_FIELD_TYPE_STRING = "String";
    public static String RECON_FIELD_TYPE_MULTI_VALUE = "Multi-Valued"; //Not supporttrd for this utility
    public static String RECON_FIELD_TYPE_NUMBER = "Number";
    public static String RECON_FIELD_TYPE_IT_RESOURCE = "IT Resource";
    public static String RECON_FIELD_TYPE_DATE = "Date";
    
    //List of possible reconciliation field attribute names to be specified in file for add
    public static String RECON_FIELD_ATTR_NAME = "ReconFieldName"; //required
    public static String RECON_FIELD_ATTR_TYPE = "FieldType"; //required
    public static String RECON_FIELD_ATTR_ISREQUIRED = "isRequired";

    /*
     * Add reconciliation fields specified from a flat file.
     * Reconciliation field name is case sensitive.
     * Multivalued fields is not supported at the moment.
     * 
     * Checks:
     * Validate the recon field types are valid
     * Validate there are no recon field duplications
     * 
     * File Format
     * <Resource Object Name>
     * <recon field attribute names [ReconFieldName FieldType isRequired]>
     * <reconFieldRecord1>
     * <reconFieldRecord2>
     * 
     * @params
     *       oimDBConnection - connection to the OIM Schema
     *       exportOps - tcExportOperationsIntf service object
     *       importOps - tcImportOperationsIntf service object
     *       fileName - file that contains the reconciliation fields to add
     */
    public static Boolean addReconFieldsDSFF(Connection oimDBConnection, tcExportOperationsIntf exportOps, tcImportOperationsIntf importOps, String fileName)
    {            
        FileInputStream fstream = null;
        DataInputStream in = null;
        BufferedReader br = null;
            
        try 
        {    
            fstream = new FileInputStream(fileName); //Open File
            in = new DataInputStream(fstream); //Get the object of DataInputStream
            br = new BufferedReader(new InputStreamReader(in));
            
            String strLine; //var to store a line of a file
            ArrayList<String> reconFieldAttributeNameArray = new ArrayList<String>(); //store the name of the recon field attributes 
            ArrayList<ReconciliationField> newReconFieldArray = new ArrayList<ReconciliationField>(); //store all recon form fields to be added
            
            //First line of the file should be the name resource object
            String resourceObjectName = br.readLine();
            if(doesResourceObjectExist(oimDBConnection, resourceObjectName) == false)
            {
                System.out.println("[Error]: Resource Object name "+ resourceObjectName + " does not exist.");
                return false;
            }
            
            Long resourceObjectKey = getResourceObjectKey(oimDBConnection, resourceObjectName);
                
            //Second line contains the attributes of a reconciliation field
            String rf_AttributeNames = br.readLine();
            StringTokenizer attributeNameToken = new StringTokenizer(rf_AttributeNames, "\t"); 
            
            while(attributeNameToken.hasMoreTokens())
            {
                String fieldAttributeName = attributeNameToken.nextToken(); 
                
                //Check if the name of the attribute is valid
                if(fieldAttributeName.equalsIgnoreCase(RECON_FIELD_ATTR_NAME))
                {
                    reconFieldAttributeNameArray.add(RECON_FIELD_ATTR_NAME);
                }
                
                else if(fieldAttributeName.equalsIgnoreCase(RECON_FIELD_ATTR_TYPE))
                {
                    reconFieldAttributeNameArray.add(RECON_FIELD_ATTR_TYPE);
                }
                
                else if(fieldAttributeName.equalsIgnoreCase(RECON_FIELD_ATTR_ISREQUIRED))
                {
                    reconFieldAttributeNameArray.add(RECON_FIELD_ATTR_ISREQUIRED);
                }
                
                else
                {
                    System.out.println("Field attribute name " + fieldAttributeName + "is invalid."
                    + "Here are all the possible attribute names:\n "
                    + RECON_FIELD_ATTR_NAME + "\n" +
                    RECON_FIELD_ATTR_TYPE + "\n" +
                    RECON_FIELD_ATTR_ISREQUIRED );
                    return false;
                }
   
            }
           
            //Validate that the "ReconFieldName" attribute name is specified the file
            if(!reconFieldAttributeNameArray.contains(RECON_FIELD_ATTR_NAME))
            {
                System.out.println("'"+ RECON_FIELD_ATTR_NAME + "' is a required attribute to be specified in file");
                return false;
            }
            
           //Validate that the "ReconFieldType" attribute name is specified the file
            if(!reconFieldAttributeNameArray.contains(RECON_FIELD_ATTR_TYPE))
            {
                System.out.println("'"+ RECON_FIELD_ATTR_TYPE + "' is a required attribute to be specified in file");
                return false;
            }
            
                     
            HashMap<String,String> reconFieldDuplicationValidator = new HashMap<String,String>(); //validate if a recon field has already been added to staging
                    
            //Read each recon field from file
            while ((strLine = br.readLine()) != null)  
            {
                StringTokenizer fieldAttributeValueToken = new StringTokenizer(strLine, "\t");
                int numFieldAttributeNames = reconFieldAttributeNameArray.size();
                int numTokens = fieldAttributeValueToken.countTokens();
                ReconciliationField reconFieldObj = new ReconciliationField();
                
                if(numFieldAttributeNames != numTokens)
                {
                    System.out.println("[Warning]: Size of row is invalid. Field will not be added:\n" + strLine);
                    continue;
                }
                
                boolean isFieldRecordFromFileValid = true;
                
                for(int i = 0; i < numFieldAttributeNames; i++)
                {
                    String fieldAttributeName = reconFieldAttributeNameArray.get(i);
                    
                    if(fieldAttributeName.equalsIgnoreCase(RECON_FIELD_ATTR_NAME))
                    {
                        String fieldName = fieldAttributeValueToken.nextToken();
                         
                        //Check if the recon field name exist
                        if(doesReconFieldNameExist(oimDBConnection, resourceObjectKey, fieldName) == true)
                        {
                            System.out.println("[Warning]: Recon Field '" + fieldName + "' exists. Field will not be added:\n" + strLine);
                            isFieldRecordFromFileValid = false;
                            break;
                        }
                        
                        //Check if reconciliation field has already been added to staging
                        if(reconFieldDuplicationValidator.containsKey(fieldName))
                        {
                            System.out.println("[Warning]: Recon Field '" + fieldName + "' exists in staging. Field will not be added:\n" + strLine);
                            isFieldRecordFromFileValid = false;
                            break;
                        }
                        
                        reconFieldDuplicationValidator.put(fieldName, null);
                        reconFieldObj.setReconFieldName(fieldName);
                    }

                    else if(fieldAttributeName.equalsIgnoreCase(RECON_FIELD_ATTR_TYPE))
                    {
                        String fieldType = fieldAttributeValueToken.nextToken();
                        
                        if(fieldType.equalsIgnoreCase(RECON_FIELD_TYPE_MULTI_VALUE))
                        {
                            System.out.println("[Warning]: Field type '" + fieldType + "' is not supported. Field will not be added:\n" + strLine);
                            isFieldRecordFromFileValid = false;
                            break; 
                        }
                        
                         //check if the variant type is valid
                        if(!isReconFieldTypeValid(fieldType))
                        {
                            System.out.println("[Warning]: Field type '" + fieldType + "' is not valid. Field will not be added:\n" + strLine);
                            isFieldRecordFromFileValid = false;
                            break; 
                        }
                        
                        reconFieldObj.setReconFieldType(fieldType);
                    }

                    else if(fieldAttributeName.equalsIgnoreCase(RECON_FIELD_ATTR_ISREQUIRED))
                    {
                        String isRequiredStr = fieldAttributeValueToken.nextToken();
                        
                        //check if the field type is valid
                        if(!HelperUtility.isBoolean(isRequiredStr))
                        {
                            System.out.println("[Warning]: Field type '" + isRequiredStr + "' is not valid. Field will not be added:\n" + strLine);
                            isFieldRecordFromFileValid = false;
                            break; 
                        }
                        
                        reconFieldObj.setIsRequired(Boolean.parseBoolean(isRequiredStr));
                    }
                }
                
                //add form field object if field record in file is valid
                if(isFieldRecordFromFileValid)
                {
                    newReconFieldArray.add(reconFieldObj); 
                }
                
            }
            
            System.out.println(newReconFieldArray);
            String resourceObjectXML = ReconFieldUtility.exportResourceObject(exportOps, resourceObjectName); //Export the resource metadata as a String
            Document document = HelperUtility.parseStringXMLIntoDocument(resourceObjectXML); //convert xml to a Document
            
            //Add reconciliation fields to the resource metadata
            for(ReconciliationField newReconFieldToAdd: newReconFieldArray)
            {
                try 
                {
                    addReconField(document, newReconFieldToAdd);
                } 
                
                catch (XPathExpressionException ex) 
                {
                    Logger.getLogger(ReconFieldUtility.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            String newObjectResourceXML = HelperUtility.parseDocumentIntoStringXML(document);
            System.out.println(newObjectResourceXML);
            importResourceObject(importOps, newObjectResourceXML, "TestReconFieldAdd");
            return true;
        } 
        
        catch (SQLException ex) 
        { 
            Logger.getLogger(ReconFieldUtility.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
        catch (NamingException ex) 
        {
            Logger.getLogger(ReconFieldUtility.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        catch (DDMException ex) 
        {
            Logger.getLogger(ReconFieldUtility.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
        catch (TransformationException ex) 
        {
            Logger.getLogger(ReconFieldUtility.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
        catch (tcBulkException ex) 
        {
            Logger.getLogger(ReconFieldUtility.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        catch (TransformerConfigurationException ex) 
        { 
            Logger.getLogger(ReconFieldUtility.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
        catch (TransformerException ex) 
        {
            Logger.getLogger(ReconFieldUtility.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        catch (ParserConfigurationException ex) 
        { 
            Logger.getLogger(ReconFieldUtility.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
        catch (SAXException ex) 
        {
            Logger.getLogger(ReconFieldUtility.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        catch (tcAPIException ex) 
        { 
            Logger.getLogger(ReconFieldUtility.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        catch (FileNotFoundException ex) 
        {
            Logger.getLogger(ReconFieldUtility.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        catch (IOException ex) 
        { 
            Logger.getLogger(ReconFieldUtility.class.getName()).log(Level.SEVERE, null, ex);
        }
       
        finally
        {
            if(br != null)
            {
                try {
                    br.close();
                } catch (IOException ex) {
                    Logger.getLogger(ProcessFormFieldUtility.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            if(in != null)
            {
                try {
                    in.close(); //Close the input stream
                } catch (IOException ex) {
                    Logger.getLogger(ProcessFormFieldUtility.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            if(fstream != null)
            {
                try {
                    fstream.close();
                } catch (IOException ex) {
                    Logger.getLogger(ProcessFormFieldUtility.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }      
        
        return false;
    }
    
    /*
     * Remove reconciliation fields specified in a flat file.
     * This method does not remove mulitvalued attributes.
     * 
     * Checks:
     * Check existence of resource object
     * Validate existence of reconciliation field in OIM
     * Make sure there exist no mapping for the reconciliation field
     * 
     * File Format
     * <Resource Object Name>
     * <reconFieldName1>
     * <reconFieldName2>
     * 
     * @params
     *       oimDBConnection - connection to the OIM Schema
     *       exportOps - tcExportOperationsIntf service object
     *       importOps - tcImportOperationsIntf service object
     *       fileName - file that contains the reconciliation fields to add
     */
    public static Boolean removeReconFieldDSFF(Connection oimDBConnection, tcExportOperationsIntf exportOps, tcImportOperationsIntf importOps, String fileName)
    {     
        FileInputStream fstream = null;
        DataInputStream in = null;
        BufferedReader br = null;
            
        try 
        {    
            fstream = new FileInputStream(fileName); //Open File
            in = new DataInputStream(fstream); //Get the object of DataInputStream
            br = new BufferedReader(new InputStreamReader(in));
            String strLine; //var to store a line of a file
           
            //First line of the file should be the name resource object
            String resourceObjectName = br.readLine();
            if(doesResourceObjectExist(oimDBConnection, resourceObjectName) == false)
            {
                System.out.println("[Error]: Resource Object name "+ resourceObjectName + " does not exist.");
                return false;
            }
            
            Long resourceObjectKey = getResourceObjectKey(oimDBConnection, resourceObjectName);         
            HashMap<String,String> reconFieldToRemove = new HashMap<String,String>(); //store the recon field names to be removed
                    
            //Read each recon field name from file
            while ((strLine = br.readLine()) != null)  
            {               
                String fieldName = strLine;
      
                //Check if the recon field name exist
                if(doesReconFieldNameExist(oimDBConnection, resourceObjectKey, fieldName) == false)
                {      
                    System.out.println("[Warning]: Recon Field '" + fieldName + "' does not exists.");
                    continue;
                }
                
                //Check if the recon field is a multivalued attribute
                if(isReconFieldMulitvalued(oimDBConnection, resourceObjectKey, fieldName) == true)
                {
                    System.out.println("[Warning]: Recon Field'" + fieldName + "' is a mulitvalued attribute.");
                    continue;
                }
                
                //Check if the recon field is a child attribute
                if(isReconFieldChildAttribute(oimDBConnection, resourceObjectKey , fieldName) == true)
                {
                    System.out.println("[Warning]: Recon Field'" + fieldName + "' is a child attribute.");
                    continue;   
                }
                        
                //Check if reconciliation field has already been added to staging
                if(reconFieldToRemove.containsKey(fieldName))
                {      
                    System.out.println("[Warning]: Recon Field '" + fieldName + "' exists in staging.");
                    continue;
                }
                
                String reconFieldKey = MappingReconFieldToFormFieldUtility.getReconFieldKey(oimDBConnection, resourceObjectKey, fieldName);
             
                //Validate if a recon field has a mapping
                if(MappingReconFieldToFormFieldUtility.isReconFieldMapped(oimDBConnection, reconFieldKey) == true)
                {   
                     System.out.println("[Warning]: Reconciliation field '"+ fieldName + "'cannot be removed until mapping is removed");
                     continue; 
                }
                
                reconFieldToRemove.put(fieldName, null);  
            }

            System.out.println(reconFieldToRemove);
            String resourceObjectXML = ReconFieldUtility.exportResourceObject(exportOps, resourceObjectName); //Export the resource metadata as a String
            Document document = HelperUtility.parseStringXMLIntoDocument(resourceObjectXML); //convert xml to a Document
            
            //Add reconciliation fields to the resource metadata
            for(Map.Entry<String,String> entry: reconFieldToRemove.entrySet())
            {
                String reconFieldName = entry.getKey();
                try 
                {
                    removeReconField(document,reconFieldName);
                } 
                
                catch (XPathExpressionException ex) 
                {
                    Logger.getLogger(ReconFieldUtility.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            String newObjectResourceXML = HelperUtility.parseDocumentIntoStringXML(document);
            System.out.println(newObjectResourceXML);
            importResourceObject(importOps, newObjectResourceXML, "TestReconFieldRemove");
            return true;
        } 
        
        catch (SQLException ex) 
        { 
            Logger.getLogger(ReconFieldUtility.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
        catch (NamingException ex) 
        {
            Logger.getLogger(ReconFieldUtility.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        catch (DDMException ex) 
        {
            Logger.getLogger(ReconFieldUtility.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
        catch (TransformationException ex) 
        {
            Logger.getLogger(ReconFieldUtility.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
        catch (tcBulkException ex) 
        {
            Logger.getLogger(ReconFieldUtility.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        catch (TransformerConfigurationException ex) 
        { 
            Logger.getLogger(ReconFieldUtility.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
        catch (TransformerException ex) 
        {
            Logger.getLogger(ReconFieldUtility.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        catch (ParserConfigurationException ex) 
        { 
            Logger.getLogger(ReconFieldUtility.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
        catch (SAXException ex) 
        {
            Logger.getLogger(ReconFieldUtility.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        catch (tcAPIException ex) 
        { 
            Logger.getLogger(ReconFieldUtility.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        catch (FileNotFoundException ex) 
        {
            Logger.getLogger(ReconFieldUtility.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        catch (IOException ex) 
        { 
            Logger.getLogger(ReconFieldUtility.class.getName()).log(Level.SEVERE, null, ex);
        }
       
        finally
        {
            if(br != null)
            {
                try {
                    br.close();
                } catch (IOException ex) {
                    Logger.getLogger(ProcessFormFieldUtility.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            if(in != null)
            {
                try {
                    in.close(); //Close the input stream
                } catch (IOException ex) {
                    Logger.getLogger(ProcessFormFieldUtility.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            if(fstream != null)
            {
                try {
                    fstream.close();
                } catch (IOException ex) {
                    Logger.getLogger(ProcessFormFieldUtility.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }      
        
        return false;
        
    }
    
    
    /*
     * Adds a reconciliation field to the resource xml.
     * Sample data added to xml
     * <ReconField repo-type="RDBMS" name="test3">
     * <ORF_UPDATE>1361032854000</ORF_UPDATE>
     * <ORF_FIELDTYPE>String</ORF_FIELDTYPE>
     * <ORF_REQUIRED>0</ORF_REQUIRED>
     * </ReconField>
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
     * Removes a reconciliation field from the resource xml.
     * The reconciliation field can only be removed if and only if the
     * reconciliation field has not been mapped to a form field.
     * 
     * @param 
     *      document - object representation of an object resource xml
     *      reconFieldName - reconciliation field to remove from document
     */
    public static void removeReconField(Document document, String reconFieldName) throws XPathExpressionException
    {  
        XPathFactory xpf = XPathFactory.newInstance();
        XPath xpath = xpf.newXPath();
        XPathExpression expression = xpath.compile("xl-ddm-data/Resource/ReconField[@name=\""+ reconFieldName +"\"]");
            
        Node reconFieldNode = (Node) expression.evaluate(document, XPathConstants.NODE); //pinpoint recon field to remove
        reconFieldNode.getParentNode().removeChild(reconFieldNode); //Get the parent node then remove target child node        
        //System.out.println(reconFieldNode.getTextContent());
            
        /*try 
        {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer t = tf.newTransformer();
            t.transform(new DOMSource(document), new StreamResult(System.out));
        } 
            
        catch (TransformerConfigurationException ex)
        {
            Logger.getLogger(ReconFieldUtility.class.getName()).log(Level.SEVERE, null, ex);
        }
            
        catch (TransformerException ex)
        {
            Logger.getLogger(ReconFieldUtility.class.getName()).log(Level.SEVERE, null, ex);
        }*/ 
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
     * Get resource object name 
     * @param 
     *      conn - connection to the OIM Schema 
     *      resourceObjectKey - resource object key (OBJ.OBJ_KEY)  
     * 
     * @return - corresponding resource object name
     */
    public static String getResourceObjectName(Connection conn, Long resourceObjectKey)
    {
        Statement st = null;
        ResultSet rs = null;
            
        try 
        {
            String query = "SELECT OBJ_NAME FROM OBJ WHERE OBJ_KEY = '" + resourceObjectKey + "'";
            st = conn.createStatement();
            rs = st.executeQuery(query);
            String resourceObjectName = null;
            
            if(rs.next())
            {
               resourceObjectName = rs.getString("OBJ_NAME"); 
            }    
            
            return resourceObjectName;
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
        
        return null;
    }
    
    /*
     * Get resource object key
     * @param 
     *      conn - connection to the OIM Schema 
     *      resourceObjectName - resource object key (OBJ.OBJ_NAME)  
     * 
     * @return - corresponding resource object name
     */
    public static Long getResourceObjectKey(Connection conn, String resourceObjectName)
    {
        Statement st = null;
        ResultSet rs = null;
            
        try 
        {
            String query = "SELECT OBJ_KEY FROM OBJ WHERE OBJ_NAME = '" + resourceObjectName + "'";
            st = conn.createStatement();
            rs = st.executeQuery(query);
            Long resourceObjectKey = null;
            
            if(rs.next())
            {
               String resourceObjectKeyStr = rs.getString("OBJ_KEY");
               resourceObjectKey = (resourceObjectKeyStr == null) ? null: Long.parseLong(resourceObjectKeyStr); 
            }    
            
            return resourceObjectKey;
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
        
        return null;
    }
    
    /* 
     * Determine if a resource object exists.
     * This method is case senstive.
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
     * Determine if the reconciliation field name exists.
     * @param 
     *      conn - connection to the OIM Schema 
     *      resourceObjectKey - resource object
     *      reconFieldName - recon field name to check
     * 
     * @return - true if the recon field name exists; false otherwise
     */
    public static Boolean doesReconFieldNameExist(Connection conn, Long resourceObjectKey ,String reconFieldName)
    {
        Statement st = null;
        ResultSet rs = null;
        
        try 
        {
            String query = "SELECT COUNT(*) AS numRows FROM ORF WHERE "
                    + "OBJ_KEY = '" + resourceObjectKey + "' AND "
                    + "ORF_FIELDNAME = '" + reconFieldName + "'" ;
            
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
     * Determine if the field type of a reconciliation field is valid.
     * @param
     *      fieldType - name of field type
     * 
     * @return - boolean value to indicate if a variant type is valid
     */
    public static boolean isReconFieldTypeValid(String fieldType)
    {
        return fieldType.equalsIgnoreCase(RECON_FIELD_TYPE_STRING) || fieldType.equalsIgnoreCase(RECON_FIELD_TYPE_MULTI_VALUE)  
        || fieldType.equalsIgnoreCase(RECON_FIELD_TYPE_NUMBER) || fieldType.equalsIgnoreCase(RECON_FIELD_TYPE_IT_RESOURCE) 
        || fieldType.equalsIgnoreCase(RECON_FIELD_TYPE_DATE);
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
    
     /*
     * Print all the reconciliation fields in a resource object.
     * Table ORF - contains all the reconciliation fields  
     * @param 
     *      conn - connection to the OIM Schema 
     *      objectKey - resource object key (ORF.OBJ_KEY)    
     */
    public static void printReconFieldsofResourceObjectFileFormatAdd(Connection conn, long objectKey) 
    {
        Statement st = null;
        ResultSet rs = null;
            
        try 
        {
            String query = "SELECT ORF_FIELDNAME, ORF_FIELDTYPE, ORF_REQUIRED FROM ORF WHERE OBJ_KEY = "+ objectKey + " ORDER BY ORF_FIELDNAME";
            st = conn.createStatement();
            rs = st.executeQuery(query);

            String resourceObjectName = getResourceObjectName(conn, objectKey);
            System.out.println(resourceObjectName);
            System.out.printf("%s\t%s\t%s\n", "ReconFieldName", "FieldType", "isRequired");
            while(rs.next())
            {
                String reconFieldName = rs.getString("ORF_FIELDNAME"); 
                String reconFieldType = rs.getString("ORF_FIELDTYPE");
                String isRequired = (rs.getString("ORF_REQUIRED").equalsIgnoreCase("0")) ? "false" : "true";
                System.out.printf("%s\t%s\t%s\n", reconFieldName, reconFieldType, isRequired); 
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
     * Determines if a reconciliation field is multivalued.
     * @params    
     *      conn - connection to the OIM Schema 
     *      resourceObjectKey - resource object
     *      reconFieldName - recon field name to check
     * 
     * @return - boolean value
     */
    public static Boolean isReconFieldMulitvalued(Connection conn, Long resourceObjectKey ,String reconFieldName)
    {
        Statement st = null;
        ResultSet rs = null;
        
        try 
        {
            String query = "SELECT ORF_FIELDTYPE FROM ORF WHERE "
                    + "OBJ_KEY = '" + resourceObjectKey + "' AND "
                    + "ORF_FIELDNAME = '" + reconFieldName + "'" ;
            
            st = conn.createStatement();
            rs = st.executeQuery(query);
            
            if(rs.next())
            {
               String type = rs.getString("ORF_FIELDTYPE");
               if(type.equalsIgnoreCase(RECON_FIELD_TYPE_MULTI_VALUE))
               {
                   return true;
               }
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
     * Determines if a reconciliation field is a child attribute.
     * @params    
     *      conn - connection to the OIM Schema 
     *      resourceObjectKey - resource object
     *      reconFieldName - recon field name to check
     * 
     * @return - boolean value
     */
    public static Boolean isReconFieldChildAttribute(Connection conn, Long resourceObjectKey ,String reconFieldName)
    {
        Statement st = null;
        ResultSet rs = null;
        
        try 
        {
            String query = "SELECT ORF_PARENT_ORF_KEY FROM ORF WHERE "
                    + "OBJ_KEY = '" + resourceObjectKey + "' AND "
                    + "ORF_FIELDNAME = '" + reconFieldName + "'" ;
            
            st = conn.createStatement();
            rs = st.executeQuery(query);
            
            if(rs.next())
            {
               String isChildAttribute = rs.getString("ORF_PARENT_ORF_KEY");
               if(isChildAttribute != null && !isChildAttribute.isEmpty())
               {
                   return true;
               }
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
    
    
}
