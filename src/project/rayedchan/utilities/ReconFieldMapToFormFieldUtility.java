package project.rayedchan.utilities;

import Thor.API.Exceptions.tcAPIException;
import Thor.API.Exceptions.tcColumnNotFoundException;
import Thor.API.Exceptions.tcFormNotFoundException;
import Thor.API.Exceptions.tcObjectNotFoundException;
import Thor.API.Exceptions.tcProcessNotFoundException;
import Thor.API.Operations.tcFormDefinitionOperationsIntf;
import Thor.API.Operations.tcObjectOperationsIntf;
import Thor.API.tcResultSet;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import project.rayedchan.custom.objects.FormFieldColumnNameComparator;
import project.rayedchan.custom.objects.ProcessFormField;
import project.rayedchan.custom.objects.ReconFieldAndFormFieldMap;
import project.rayedchan.custom.objects.ReconFieldComparator;

/**
 *
 * @author rayedchan
 * A utility to map reconciliation fields to process form fields. A flat file 
 * may be used as a data source to define this mappings. This does not support 
 * multivalued fields at the moment.
 */
public class ReconFieldMapToFormFieldUtility 
{    
    /*
     * Print the current mappings of the reconciliation fields and process form
     * fields with sorting options.
     * 
     * PRF Table contains all the process field and reconcilation field mappings
     * 
     * @params 
     *      formDefOps - tcFormDefinitionOperationsIntf service object
     *      processKey - the resource process key (PKG.PKG_KEY or TOS.TOS_KEY)
     *      columnToSort - sort by recon field or form field column
     *              0 = Reconciliation fields
     *              1 = Process form fields 
     *      descOrAsc - sort by descending or ascending order
     *              0 = Descending order
     *              1 = Ascending order
     */
    public static void printReconFieldAndFormFieldMappingsBySort(tcFormDefinitionOperationsIntf formDefOps, Long processKey, int columnToSort, int descOrAsc) throws tcAPIException, tcProcessNotFoundException, tcColumnNotFoundException
    {        
        tcResultSet mappingResultSet = formDefOps.getReconDataFlowForProcess(processKey);
        int numRows = mappingResultSet.getTotalRowCount();
        ArrayList<ReconFieldAndFormFieldMap> mappingObjArray = new ArrayList<ReconFieldAndFormFieldMap>();
        
        System.out.printf("%-30s%-30s\n", "Reconciliation Field", "Form Field Column");
        System.out.printf("%-30s%-30s\n", "====================", "=================");
        
        //Add reconciliation field and form field as an object to array
        for(int i = 0; i < numRows; i++)
        {
            mappingResultSet.goToRow(i);
            String reconField = mappingResultSet.getStringValue("Objects.Reconciliation Fields.Name");
            String formField = mappingResultSet.getStringValue("Process Definition.Reconciliation Fields Mappings.ColumnName");
            ReconFieldAndFormFieldMap mappingObj = new ReconFieldAndFormFieldMap(reconField, formField);
            mappingObjArray.add(mappingObj);   
            System.out.printf("%-30s%-30s\n", reconField, formField);
        }
        
        //Sort by Reconciliation Fields
        if(columnToSort == 0)
        {
            //descending order
            if(descOrAsc == 0)
            {
                Collections.sort(mappingObjArray, Collections.reverseOrder(new ReconFieldComparator()));
            }
            
            //ascending order
            else
            {
                Collections.sort(mappingObjArray, new ReconFieldComparator());
            }
        }
        
        //Sort by Process Form Fields
        else
        {
            //descending order
            if(descOrAsc == 0)
            {
                Collections.sort(mappingObjArray, Collections.reverseOrder(new FormFieldColumnNameComparator()));
            }
            
            //ascending order
            else
            {
                Collections.sort(mappingObjArray, new FormFieldColumnNameComparator());
            }
            
        }
        
        
        //iterate mappingObjArray
        for(ReconFieldAndFormFieldMap mappingObj: mappingObjArray)
        {
            String reconField = mappingObj.getReconFieldName();
            String formField = mappingObj.getFormFieldColumnName();
            System.out.printf("%-30s%-30s\n", reconField, formField);
        }
        
    }
    
    /*
     * Prints the current mappings of the reconciliation fields and process form
     * fields.
     * 
     * PRF Table contains all the process field and reconcilation field mappings
     * 
     * @params 
     *      formDefOps - tcFormDefinitionOperationsIntf service object
     *      processKey - the resource process key (PKG._PKG_KEY or TOS.TOS_KEY)
     */
    public static void printReconFieldAndFormFieldMappings(tcFormDefinitionOperationsIntf formDefOps, Long processKey) throws tcAPIException, tcProcessNotFoundException, tcColumnNotFoundException
    {
        tcResultSet mappingResultSet = formDefOps.getReconDataFlowForProcess(processKey);
        int numRows = mappingResultSet.getTotalRowCount();
        
        System.out.printf("%-30s%-30s%-30s\n", "ReconFieldKey" ,"Reconciliation Field", "Form Field Column");
        for(int i = 0; i < numRows; i++)
        {
            mappingResultSet.goToRow(i);
            String reconField = mappingResultSet.getStringValue("Objects.Reconciliation Fields.Name");
            String formField = mappingResultSet.getStringValue("Process Definition.Reconciliation Fields Mappings.ColumnName");
            String reconFieldKey = mappingResultSet.getStringValue("Objects.Reconciliation Fields.Key");
            System.out.printf("%-30s%-30s%-30s\n", reconFieldKey ,reconField, formField);
        }
               
    }   
    
    /*
     * Print all the fields in a given process form. Only the current version
     * of the process form will be looked at.
     * 
     * SDC table in the OIM Schema contains all the form fields.
     * Structure Utility.Additional Columns.Key (SDC_KEY) - Form Field Key
     * Structure Utility.Additional Columns.Name (SDC_NAME) - Field Column Name
     * Structure Utility.Additional Columns.Variant Type (SDC_VARIANT_TYPE) - Field Variant Type
     * Structure Utility.Additional Columns.Field Label (SDC_LABEL) - Field Label
     * Structure Utility.Additional Columns.Field Type (SDC_FIELD_TYPE) -Field Type
     * 
     * @params 
     *      formDefOps - tcFormDefinitionOperationsIntf service object
     *      formKey - the resource process key (PKG._PKG_KEY)
     */
    public static void getFormFields(tcFormDefinitionOperationsIntf formDefOps, long formKey) throws tcAPIException, tcFormNotFoundException, tcColumnNotFoundException
    {
        tcResultSet formVersionResultSet = formDefOps.getFormVersions(formKey); //get versions of current process form
        int numRows = formVersionResultSet.getTotalRowCount();
        Integer currentFormVersion = null;
        
        for(int i = 0; i < numRows; i++)
        {
            formVersionResultSet.goToRow(i);
            String formVersionStr = formVersionResultSet.getStringValue("SDL_CURRENT_VERSION");
            currentFormVersion = (formVersionStr == null ||  formVersionStr.isEmpty())? null : Integer.parseInt(formVersionStr);
        }
        
        //System.out.println(currentFormVersion);
        tcResultSet fieldResultSet = formDefOps.getFormFields(formKey, currentFormVersion);
        int numFields = fieldResultSet.getTotalRowCount();
        
        System.out.printf("%-20s%-30s%-30s%-25s%-20s\n", "Field Key", "Field Name","Field Label","Field Variant Type", "Field Type" );
        for(int i = 0; i < numFields; i++ )
        {
            fieldResultSet.goToRow(i);
            String fieldKey = fieldResultSet.getStringValue("Structure Utility.Additional Columns.Key");
            String fieldName = fieldResultSet.getStringValue("Structure Utility.Additional Columns.Name");
            String fieldLabel = fieldResultSet.getStringValue("Structure Utility.Additional Columns.Field Label");
            String fieldVariantType = fieldResultSet.getStringValue("Structure Utility.Additional Columns.Variant Type");
            String fieldType = fieldResultSet.getStringValue("Structure Utility.Additional Columns.Field Type");
            System.out.printf("%-20s%-30s%-30s%-25s%-20s\n", fieldKey, fieldName, fieldLabel, fieldVariantType, fieldType);
        }
       
    }
    
    /*
     * Print all the reconciliation fields of the given resource object.
     * 
     * ORF table in the OIM Schema contains all the reconciliation fields.
     * Objects.Reconciliation Fields.Key (ORF_KEY) - recon field key
     * Objects.Reconciliation Fields.Name (ORF_FIELDNAME) - recon field name
     * ORF_FIELDTYPE - recon field type
     * 
     * @params 
     *      resourceObjectOps - tcObjectOperationsIntf service object
     *      objKey - resource object key (OBJ.OBJ_KEY)
     */
    public static void getReconFields(tcObjectOperationsIntf resourceObjectOps, long objKey) throws tcAPIException, tcObjectNotFoundException, tcColumnNotFoundException
    {
        tcResultSet trs = resourceObjectOps.getReconciliationFields(objKey);
        int numRows = trs.getTotalRowCount();
        
        System.out.printf("%-25s%-25s%-25s\n", "Recon Field Key", "Recon Field Name", "Recon Field Type");
        for(int i = 0; i < numRows ; i++)
        {
            trs.goToRow(i);
            String reconFieldKey = trs.getStringValue("Objects.Reconciliation Fields.Key");
            String reconFieldName = trs.getStringValue("Objects.Reconciliation Fields.Name");
            String reconFieldType = trs.getStringValue("ORF_FIELDTYPE");
            System.out.printf("%-25s%-25s%-25s\n", reconFieldKey, reconFieldName, reconFieldType);
        }
    }
    
    /*
     * Add a mapping between a process form field and a reconciliation field.
     * @params -
     *      formDefOps - tcFormDefinitionOperationsIntf service object
     *      processKey - the resource process key (PKG.PKG_KEY)
     *      objectKey - the resource object key (OBJ.OBJ_KEY)
     *      reconField_FormField_Map - ReconFieldAndFormFieldMap object
     * 
     * 
     * Note: Mapping is one to one. Once a reconciliation field or a process 
     * form field is defined in a mapping, it can no longer be used for another mapping.  
     */
    public static void addReconFieldAndFormFieldMap(tcFormDefinitionOperationsIntf formDefOps, Long processKey, Long objectKey, ReconFieldAndFormFieldMap reconField_FormField_Map) throws tcAPIException, tcProcessNotFoundException, tcObjectNotFoundException
    {
        String reconFieldKey = reconField_FormField_Map.getReconFieldKey();
        String formFieldColumnName = reconField_FormField_Map.getFormFieldColumnName(); //process form field column name
        Boolean isKeyField = reconField_FormField_Map.getIsKeyField();
        Boolean isCaseInsensitive = reconField_FormField_Map.getIsCaseInsensitive();
        
        formDefOps.addReconDataFlow(processKey, objectKey, reconFieldKey, formFieldColumnName, isKeyField, isCaseInsensitive);
    }
    
      /*
     * Removes a mapping between a process form field and a reconciliation field.
     * @params -
     *      formDefOps - tcFormDefinitionOperationsIntf service object
     *      processKey - the resource process key (PKG.PKG_KEY)
     *      objectKey - the resource object key (OBJ.OBJ_KEY)
     *      reconFieldKey - reconciliation field key (ORF.ORF_KEY)
     */
    public static void removeReconFieldAndFormFieldMap(tcFormDefinitionOperationsIntf formDefOps, Long processKey, Long objectKey, String reconFieldKey) throws tcAPIException, tcProcessNotFoundException, tcObjectNotFoundException
    {
       formDefOps.removeReconDataFlowMapping(processKey, objectKey, reconFieldKey);
    }
    
    /*
     * Prints the current mappings of the reconciliation fields and process form
     * fields in a file format for adding. Mulitvalued fields will not be printed.
     * "Structure Utility.Table Name" will be used to determine if a mapping uses
     * a mulitvalued field.
     * 
     * PRF Table contains all the process field and reconcilation field mappings
     * 
     * @params 
     *      formDefOps - tcFormDefinitionOperationsIntf service object
     *      processKey - the resource process key (PKG._PKG_KEY or TOS.TOS_KEY)
     */
    public static void printReconFieldAndFormFieldMappingsAddDSFF(tcFormDefinitionOperationsIntf formDefOps, Long processKey) throws tcAPIException, tcProcessNotFoundException, tcColumnNotFoundException
    {
        tcResultSet mappingResultSet = formDefOps.getReconDataFlowForProcess(processKey);
        int numRows = mappingResultSet.getTotalRowCount();
        
        System.out.println(processKey);
        System.out.println("<Object Key>");
        for(int i = 0; i < numRows; i++)
        {
            mappingResultSet.goToRow(i);
            String childTableName = mappingResultSet.getStringValue("Structure Utility.Table Name");
            String reconField = mappingResultSet.getStringValue("Objects.Reconciliation Fields.Name");
            String formField = mappingResultSet.getStringValue("Process Definition.Reconciliation Fields Mappings.ColumnName");
            
            if(childTableName == null || childTableName.isEmpty())
            {
                System.out.printf("%s\t%s\n", reconField, formField);
            }
        }
               
    } 
    
    /*
     * Map reconciliation fields and form fields specified in a flat file.
     * File must be tab delimited. Mulitvalued is not supported.
     * 
     * By default all mappings will not be case insensitive and a key field.
     * A hashmap will be used to enforce reconciliation fields and process form fields
     * are mapped one to one.
     * 
     * TODO: Check recon field and process form field are the same type.
     * Form Field Column Name must be all uppercase or it will be deemed invalid.
     * 
     * File format
     * <ProcessKey>
     * <ObjectKey>
     * <reconFieldName1>    <formFieldColumnName1>
     * <reconFieldName2>    <formFieldColumnName2>
     * 
     * @params
     *      oimDBConnection - connection to the OIM Schema
     *      formDefOps - tcFormDefinitionOperationsIntf service
     *      fileName - path of file    
     * 
     * @return - true for success; false otherwise 
     */
    public static Boolean addReconFieldAndFormFieldMapDSFF(Connection oimDBConnection, tcFormDefinitionOperationsIntf formDefOps, String fileName) throws IOException
    {        
        HashMap<String,String> mappings = new HashMap<String,String>(); //store all the mappings of a recon field and a form field
        FileInputStream fstream = null;
        DataInputStream in = null;
        BufferedReader br = null;
        
        try 
        {     
            fstream = new FileInputStream(fileName); //Open File
            in = new DataInputStream(fstream); //Get the object of DataInputStream
            br = new BufferedReader(new InputStreamReader(in));
            
            String strLine; //var to store a line of a file
            
            //First line of the file should be the process key
            Long processKey = Long.parseLong(br.readLine());
            if(doesProcessExist(oimDBConnection, processKey) == false)
            {
                System.out.println("[Error]: Process Key "+ processKey + " does not exist.");
                return false;
            }
            
            //Second line of the file should be the name of the object key
            Long objectKey = Long.parseLong(br.readLine());
            if(doesObjectExist(oimDBConnection, objectKey) == false)
            {
                System.out.println("[Error]: Object Key "+ objectKey + " does not exist.");
                return false;
            }
            
            //Rest of the file should be the mappings 
            while ((strLine = br.readLine()) != null)  
            {
                //System.out.println(strLine);
                StringTokenizer fieldToken = new StringTokenizer(strLine, "\t");
                int numTokens = fieldToken.countTokens();
                
                if(numTokens != 2)
                {
                    System.out.println("[Warning]: Size of row is invalid. Mapping will not be added:\n" + strLine);
                    continue;
                }
                
                String reconFieldName = fieldToken.nextToken();
                
                if(doesReconFieldExist(oimDBConnection, objectKey, reconFieldName) == false)
                {
                    System.out.println("[Warning]: Reconciliation Field '" + reconFieldName + "' does not exist. Mapping will not be added:\n" + strLine);
                    continue;
                }
                
                String formFieldColumnName = fieldToken.nextToken();
                
                
            }
            
            
            
        } 
        
        catch (FileNotFoundException ex) 
        {
            Logger.getLogger(ReconFieldMapToFormFieldUtility.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }
    
    /*
     * Determine if a process exist.
     * Queries from the PKG table. 
     * 
     * @params
     *      oimDBConnection - connection to the OIM Schema
     *      processKey - process key to check existence (PKG_KEY ?= Process Key) or (TOS_KEY ?= Process Key)
     * 
     * @return - true for if it exists, false otherwise
     */
    public static Boolean doesProcessExist(Connection oimDBConnection, Long processKey)
    {        
        Statement st = null;
        ResultSet rs = null;
            
        try 
        {
            String query = "SELECT COUNT(*) AS numRows FROM PKG WHERE PKG_KEY = " + processKey;
            //System.out.println(query);
            
            st = oimDBConnection.createStatement(); //Create a statement
            rs = st.executeQuery(query);
            rs.next();
            
            if(Integer.parseInt(rs.getString("numRows")) == 1)
            {
               return true;  
            }    
 
        } 
        
        catch (SQLException ex) {
            Logger.getLogger(ReconFieldMapToFormFieldUtility.class.getName()).log(Level.SEVERE, null, ex);
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
     * Determine if a object exist.
     * Queries from the OBJ table.
     * 
     * @params
     *      oimDBConnection - connection to the OIM Schema
     *      objectKey - process key to check existence (OBJ.OBJ_KEY)
     * 
     * @return - true for if it exists, false otherwise
     */
    public static Boolean doesObjectExist(Connection oimDBConnection, Long objectKey)
    {        
        Statement st = null;
        ResultSet rs = null;
            
        try 
        {
            String query = "SELECT COUNT(*) AS numRows FROM OBJ WHERE OBJ_KEY = " + objectKey;
            //System.out.println(query);
            
            st = oimDBConnection.createStatement(); //Create a statement
            rs = st.executeQuery(query);
            rs.next();
            
            if(Integer.parseInt(rs.getString("numRows")) == 1)
            {
               return true;  
            }    
 
        } 
        
        catch (SQLException ex) {
            Logger.getLogger(ReconFieldMapToFormFieldUtility.class.getName()).log(Level.SEVERE, null, ex);
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
     * Determine if a reconciliation field.
     * Queries from the ORF table.
     * 
     * @params
     *      oimDBConnection - connection to the OIM Schema
     *      objectKey - resource object key (OBJ.OBJ_KEY)
     *      reconFieldName - reconciliation field name (ORF_FIELD_NAME)
     *      
     * @return - true for if it exists, false otherwise
     */
    public static Boolean doesReconFieldExist(Connection oimDBConnection, Long objectKey, String reconFieldName)
    {        
        Statement st = null;
        ResultSet rs = null;
            
        try 
        {
            String query = "SELECT COUNT(*) AS numRows FROM ORF WHERE OBJ_KEY = " + objectKey + " AND ORF_FIELDNAME = '" + reconFieldName + "'";
            System.out.println(query);
            
            st = oimDBConnection.createStatement(); //Create a statement
            rs = st.executeQuery(query);
            rs.next();
            
            if(Integer.parseInt(rs.getString("numRows")) == 1)
            {
               return true;  
            }    
 
        } 
        
        catch (SQLException ex) {
            Logger.getLogger(ReconFieldMapToFormFieldUtility.class.getName()).log(Level.SEVERE, null, ex);
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
     * Determine if a form field column name exist on the current version of a process form.
     * This method does not consider any child forms (mulitvalued attributes) and
     * will return false on those child attributes.
     * 
     * SDC table in the OIM Schema contains all the form fields.
     * Structure Utility.Additional Columns.Name (SDC_NAME) - Field Column Name
     * 
     * @params 
     *      formDefOps - tcFormDefinitionOperationsIntf service object
     *      formKey - the resource process key (PKG._PKG_KEY)
     *      fieldFormColumnName - column name of a field form (SDC.SDC_NAME)
     */
    public static Boolean doesFormFieldExist(tcFormDefinitionOperationsIntf formDefOps, long formKey, String fieldColumnName) throws tcAPIException, tcFormNotFoundException, tcColumnNotFoundException
    {
        tcResultSet formVersionResultSet = formDefOps.getFormVersions(formKey); //get versions of current process form
        int numRows = formVersionResultSet.getTotalRowCount();
        Integer currentFormVersion = null;
        
        for(int i = 0; i < numRows; i++)
        {
            formVersionResultSet.goToRow(i);
            String formVersionStr = formVersionResultSet.getStringValue("SDL_CURRENT_VERSION");
            currentFormVersion = (formVersionStr == null ||  formVersionStr.isEmpty())? null : Integer.parseInt(formVersionStr);
        }
       
        tcResultSet fieldResultSet = formDefOps.getFormFields(formKey, currentFormVersion);
        int numFields = fieldResultSet.getTotalRowCount();
        
        for(int i = 0; i < numFields; i++ )
        {
            fieldResultSet.goToRow(i);
            String fieldName = fieldResultSet.getStringValue("Structure Utility.Additional Columns.Name");
            
            if(fieldName.equals(fieldColumnName))
            {
                return true;
            }
        }
        
        return false;
       
    }
    
    
}
