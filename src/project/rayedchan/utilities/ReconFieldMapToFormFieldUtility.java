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
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import project.rayedchan.custom.objects.FormFieldColumnNameComparator;
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
    //List of possible attribute names to be specified in file for adding a mapping between a recon filed and a form field
    public static String RECONFIELDNAME = "recon_field_name"; //required
    public static String FORMFIELDCOLUMNNAME = "form_field_column_name"; //required
    public static String ISKEYFIELD = "is_key_field"; //optional; default value = false
    public static String IS_CASE_INSENSITIVE = "is_case_insensitive"; //optional: default value = false
    
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
     *      oimDBConnection - connection to the OIM Schema
     *      formDefOps - tcFormDefinitionOperationsIntf service object
     *      processKey - the resource process key (PKG._PKG_KEY or TOS.TOS_KEY)
     */
    public static void printReconFieldAndFormFieldMappingsAddDSFF(Connection oimDBConnection,tcFormDefinitionOperationsIntf formDefOps, Long processKey) throws tcAPIException, tcProcessNotFoundException, tcColumnNotFoundException
    {
        tcResultSet mappingResultSet = formDefOps.getReconDataFlowForProcess(processKey);
        int numRows = mappingResultSet.getTotalRowCount();
        Long objKey = getObjKeyByProcKey(oimDBConnection, processKey); //get corresponding object key
        
        System.out.println(processKey);
        System.out.println(objKey);
        System.out.printf("%s\t%s\t%s\t%s\n",RECONFIELDNAME, FORMFIELDCOLUMNNAME, ISKEYFIELD, IS_CASE_INSENSITIVE);
        for(int i = 0; i < numRows; i++)
        {
            mappingResultSet.goToRow(i);
            String childTableName = mappingResultSet.getStringValue("Structure Utility.Table Name");
            String reconField = mappingResultSet.getStringValue("Objects.Reconciliation Fields.Name");
            String formField = mappingResultSet.getStringValue("Process Definition.Reconciliation Fields Mappings.ColumnName");
            boolean isKeyField = ((mappingResultSet.getStringValue("Process Definition.Reconciliation Fields Mappings.Iskey").equalsIgnoreCase("1"))? true : false);
            boolean isCaseInsensitive = ((mappingResultSet.getStringValue("PRF_CASE_INSENSITIVE").equalsIgnoreCase("1"))? true : false);
            
            if(childTableName == null || childTableName.isEmpty())
            {
                System.out.printf("%s\t%s\t%s\t%s\n", reconField, formField, isKeyField, isCaseInsensitive);
            }
        }
               
    } 
    
    /*
     * Map reconciliation fields and form fields specified in a flat file.
     * File must be tab delimited. Mulitvalued is not supported.
     * Process Key and Object Key must be associated with a process form.
     * Only form fields in the current version of a process form will be looked at. 
     * 
     * By default all mappings will not be case insensitive and a key field.
     * Form Field Column Name must be all uppercase or it will be deemed invalid
     * A reconciliation field can only map to one form field.
     * 
     * Checks:
     * Do not add existing mappings.
     * TODO: Check recon field and process form field are the same type.
     * Enforce reconciliation fields and process form field to be one to one.
     * 
     * File format
     * <ProcessKey>
     * <ObjectKey>
     * <recon_field_name form_field_column_name	is_key_field[optional] is_case_insensitive[optional]>
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
    public static Boolean addReconFieldAndFormFieldMapDSFF(Connection oimDBConnection, tcFormDefinitionOperationsIntf formDefOps, String fileName) throws IOException, tcAPIException, tcFormNotFoundException, tcColumnNotFoundException
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
            ArrayList<ReconFieldAndFormFieldMap> mappingsToBeAdded = new ArrayList<ReconFieldAndFormFieldMap>();
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
            
            Long formKey = getFormKeyByObjAndProcKey(oimDBConnection, processKey, objectKey); //get the process form key associated with the obj key and prco key
            if(formKey == null)
            {
                System.out.println("[Error]: Object Key '"+ objectKey + "' and Process Key '" + processKey + " are not associated with a form.");
                return false;
            }
            
            //Third line determines the ordering of the mappings attributes in the file
            ArrayList<String> mappingAttributeNameArray = new ArrayList<String>(); //store the name of each mapping attribute name 
            String mappingAttributeNames = br.readLine();
            StringTokenizer mappingAttributeNameTokens = new StringTokenizer(mappingAttributeNames, "\t");
            
            while(mappingAttributeNameTokens.hasMoreTokens())
            {
                String mappingAttributeName = mappingAttributeNameTokens.nextToken().toLowerCase();
 
                //Check if the name of the attribute is valid
                if(mappingAttributeName.equalsIgnoreCase(RECONFIELDNAME))
                {
                    mappingAttributeNameArray.add(RECONFIELDNAME);
                }
                
                else if(mappingAttributeName.equalsIgnoreCase(FORMFIELDCOLUMNNAME))
                {
                    mappingAttributeNameArray.add(FORMFIELDCOLUMNNAME);
                }
                
                else if(mappingAttributeName.equalsIgnoreCase(ISKEYFIELD))
                {
                    mappingAttributeNameArray.add(ISKEYFIELD);
                }
                          
                else if(mappingAttributeName.equalsIgnoreCase(IS_CASE_INSENSITIVE))
                {
                    mappingAttributeNameArray.add(IS_CASE_INSENSITIVE);
                }
                        
                else
                {
                    System.out.println("Mapping attribute name " + mappingAttributeName + "is invalid."
                    + "Here are all the possible mapping attribute names:\n "
                    + RECONFIELDNAME + "\n" +
                    FORMFIELDCOLUMNNAME + "\n" +
                    ISKEYFIELD + "\n" +
                    IS_CASE_INSENSITIVE + "\n");
                    return false;
                }
   
            }
            
            //Validate that "recon_field_name" is specified in the file.
            if(!mappingAttributeNameArray.contains(RECONFIELDNAME))
            {
                System.out.println("'"+ RECONFIELDNAME + "' is a required mapping attribute to be specified in file");
                return false; 
            }
            
            //Validate that "form_field_column_name" is specified in the file.
            if(!mappingAttributeNameArray.contains(FORMFIELDCOLUMNNAME))
            {
                System.out.println("'"+ FORMFIELDCOLUMNNAME + "' is a required mapping attribute to be specified in file");
                return false; 
            }
            
            //Rest of the file should be the mappings 
            while ((strLine = br.readLine()) != null)  
            {
                StringTokenizer mappingToken = new StringTokenizer(strLine, "\t");
                int mappingAttrNameFileCount = mappingAttributeNameArray.size();
                int numTokens = mappingToken.countTokens();
                ReconFieldAndFormFieldMap fieldMapping = new ReconFieldAndFormFieldMap();
                 
                //validate the line format
                if(numTokens != mappingAttrNameFileCount)
                {
                    System.out.println("[Warning]: Size of row is invalid. Mapping will not be added:\n" + strLine);
                    continue;
                }
                                
                boolean isMappingFromFileValid = true;
                String reconFieldName = null;
                String formFieldColumnName = null;
                
                for(int i = 0; i < mappingAttrNameFileCount; i++)
                {
                    String mappingAttributeName = mappingAttributeNameArray.get(i);
                   
                    if(mappingAttributeName.equalsIgnoreCase(RECONFIELDNAME))
                    {
                        reconFieldName = mappingToken.nextToken();
                        //validate the existence of the recon field on current line
                        if(doesReconFieldExist(oimDBConnection, objectKey, reconFieldName) == false)
                        {
                            System.out.println("[Warning]: Reconciliation Field '" + reconFieldName + "' does not exist. Mapping will not be added:\n" + strLine);
                            isMappingFromFileValid = false;
                            break;
                        }
                        
                        String reconFieldKey = getReconFieldKey(oimDBConnection, objectKey, reconFieldName);
                        fieldMapping.setReconFieldKey(reconFieldKey);
                        fieldMapping.setReconFieldName(reconFieldName);
                    }

                    else if(mappingAttributeName.equalsIgnoreCase(FORMFIELDCOLUMNNAME))
                    {  
                        formFieldColumnName = mappingToken.nextToken();
                        //Validate the existence of the form field
                        if(doesFormFieldExist(formDefOps, formKey, formFieldColumnName) == false)
                        {
                            System.out.println("[Warning]: Form Field '" + reconFieldName + "' does not exist. Mapping will not be added:\n" + strLine);
                            isMappingFromFileValid = false;
                            break;
                        }
                        
                        fieldMapping.setFormFieldColumnName(formFieldColumnName);
                    }

                    else if(mappingAttributeName.equalsIgnoreCase(ISKEYFIELD))
                    {
                        String isKeyField = mappingToken.nextToken();
                        
                        //check if the type is valid
                        if(!HelperUtility.isBoolean(isKeyField))
                        {
                            System.out.println("[Warning]: 'is_key_field' attibute type '" + isKeyField + "' is not a boolean value. Mapping will not be added:\n" + strLine);
                            isMappingFromFileValid = false;
                            break; 
                        }
                        
                        fieldMapping.setIsKeyField(Boolean.parseBoolean(isKeyField));
                    }

                    else if(mappingAttributeName.equalsIgnoreCase(IS_CASE_INSENSITIVE))
                    {                       
                        String isCaseInsensitive = mappingToken.nextToken();
                        
                        //check if the type is valid
                        if(!HelperUtility.isBoolean(isCaseInsensitive))
                        {
                            System.out.println("[Warning]: 'is_case_insensitive' attribute type '" + isCaseInsensitive + "' is not a boolean value. Mapping will not be added:\n" + strLine);
                            isMappingFromFileValid = false;
                            break; 
                        }
                        
                        fieldMapping.setIsCaseInsensitive(Boolean.parseBoolean(isCaseInsensitive));
                    }

                }//end for loop
                
                
                if(isMappingFromFileValid == true)
                {                 
                    //Validate if the mapping already exist in OIM
                    if(doesPRFMappingExist( oimDBConnection, processKey, reconFieldName, formFieldColumnName) == true)
                    {  
                       System.out.println("[Warning]: Mapping already exists. Mapping will not be added:\n" + strLine);
                       continue; 
                    }
                    
                    //Enforce one to one mapping
                    if(!mappings.containsValue(formFieldColumnName) && !mappings.containsKey(reconFieldName))
                    { 
                        mappings.put(reconFieldName, formFieldColumnName); 
                        mappingsToBeAdded.add(fieldMapping);
                    }
                    
                    else
                    {
                        System.out.println("[Warning]: Recon field or form field already exist in staging.Mapping will not be added:\n" + strLine);
                    }
                }
                   
            }//end while loop
            
            
            System.out.println("Mappings to be added: " + mappingsToBeAdded.toString());
            for(ReconFieldAndFormFieldMap reconField_FormField_Map : mappingsToBeAdded)
            {
                try {
                    addReconFieldAndFormFieldMap(formDefOps, processKey, objectKey, reconField_FormField_Map);
                } catch (tcProcessNotFoundException ex) {
                    Logger.getLogger(ReconFieldMapToFormFieldUtility.class.getName()).log(Level.SEVERE, null, ex);
                } catch (tcObjectNotFoundException ex) {
                    Logger.getLogger(ReconFieldMapToFormFieldUtility.class.getName()).log(Level.SEVERE, null, ex);
                } catch(tcAPIException ex){
                    Logger.getLogger(ReconFieldMapToFormFieldUtility.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }
            
            return true;
                 
        } 
        
        catch (FileNotFoundException ex) 
        {
            Logger.getLogger(ReconFieldMapToFormFieldUtility.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }
    
     /*
     * Remove reconciliation fields and form fields mappings specified in a flat file.
     * Removal will be based on the reconciliation fields.
     * Removal of mulitvalued is not supported.
     * Process Key and Object Key must be associated with a process form.
     * 
     * Checks:
     * Cannot remove mappings that do not exist.
     * HashMap is used to prevent duplication of recon fields in file.
     * 
     * File format
     * <ProcessKey>
     * <ObjectKey>
     * <reconFieldName1>    
     * <reconFieldName2>  
     * 
     * @params
     *      oimDBConnection - connection to the OIM Schema
     *      formDefOps - tcFormDefinitionOperationsIntf service
     *      fileName - path of file    
     * 
     * @return - true for success; false otherwise 
     */
    public static Boolean removeReconFieldAndFormFieldMapDSFF(Connection oimDBConnection, tcFormDefinitionOperationsIntf formDefOps, String fileName) throws IOException
    {        
        HashMap<String,String> mappingsToRemove = new HashMap<String,String>(); //store all the mappings to be removed
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

            //Rest of the file should be the the reconciliation field names
            while ((strLine = br.readLine()) != null)  
            {
                 String reconFieldName = strLine;
                                    
                 //validate the existence of the recon field on current line
                 if(doesReconFieldExist(oimDBConnection, objectKey, reconFieldName) == false)
                 {      
                     System.out.println("[Warning]: Reconciliation Field '" + reconFieldName + "' does not exist. Mapping will not be removed.");
                     continue;
                 }
                        
                 String reconFieldKey = getReconFieldKey(oimDBConnection, objectKey, reconFieldName);
                              
                 //Validate if the mapping exist in OIM
                 if(doesPRFMappingExist(oimDBConnection, processKey, reconFieldKey) == false)
                 {   
                     System.out.println("[Warning]: Mapping for reconciliation field does not exists. Mapping will not be added:\n" + strLine);
                     continue; 
                 }
                    
                 //Check if the reconciliation field already exist in mappingsToRemove 
                 if(!mappingsToRemove.containsValue(reconFieldName) && !mappingsToRemove.containsKey(reconFieldKey))
                 { 
                     mappingsToRemove.put(reconFieldKey, reconFieldName); 
                 }
                    
                 else
                 {
                     System.out.println("[Warning]: Mapping for reconciliation field has already been added . Mapping will not be added:\n" + strLine);
                     continue; 
                 }                          
            }
            
            System.out.println("Mappings to remove: " + mappingsToRemove.toString());
            
            //Remove fields from the process form 
            Iterator it = mappingsToRemove.entrySet().iterator();
    
            while (it.hasNext()) 
            {   
                Map.Entry pairs = (Map.Entry)it.next();
                String reconFieldKeyToRemove = (String) pairs.getKey();
                try {
                    removeReconFieldAndFormFieldMap(formDefOps, processKey, objectKey, reconFieldKeyToRemove);
                } catch (tcAPIException ex) {
                    Logger.getLogger(ReconFieldMapToFormFieldUtility.class.getName()).log(Level.SEVERE, null, ex);
                } catch (tcProcessNotFoundException ex) {
                    Logger.getLogger(ReconFieldMapToFormFieldUtility.class.getName()).log(Level.SEVERE, null, ex);
                } catch (tcObjectNotFoundException ex) {
                    Logger.getLogger(ReconFieldMapToFormFieldUtility.class.getName()).log(Level.SEVERE, null, ex);
                }
               
                it.remove(); // avoids a ConcurrentModificationException
            }

            return true;     
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
    
    
    /*
     * Get the corresponding process form associated with a given process key and object key.
     * TOS = Process Definition
     * SDK = Structure Utility (Process Form)
     * OBJ = Resource Object Definition
     * PKG = Service Processes
     * 
     * @param 
     *      conn - connection to the OIM Schema 
     *      processKey - process key (TOS.TOS_KEY or PKG.PKG_KEY)
     *      objectKey - resource object key (OBJ.OBJ_KEY)
     *      
     * @return - form key associated with given object key and process key
     */
    public static Long getFormKeyByObjAndProcKey(Connection conn, Long processKey, Long objectKey)
    {
        Statement st = null;
        ResultSet rs = null;
        
        try 
        {
            String query = "SELECT SDK.SDK_KEY FROM "
             + "TOS RIGHT OUTER JOIN PKG ON PKG.PKG_KEY = TOS.PKG_KEY "
             + "LEFT OUTER JOIN SDK ON SDK.SDK_KEY = TOS.SDK_KEY "
             + "LEFT OUTER JOIN OBJ ON OBJ.OBJ_KEY = PKG.OBJ_KEY "
             + "WHERE PKG.PKG_KEY = " + processKey + " AND OBJ.OBJ_KEY = " + objectKey;
            //System.out.println(query);
            
            st = conn.createStatement(); //Create a statement
            rs = st.executeQuery(query);

            if(rs.next())
            {
                String formKey = rs.getString("SDK_KEY");
                return (formKey == null || formKey.isEmpty()) ? null : Long.parseLong(formKey) ;
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
        
        return null;
        
    }
    
     /*
     * Determine a reconciliation field and form field mapping exist.
     * Queries from the ORF and PRF (mapping table) table.
     * Method is case senstive for comparing field names.
     * 
     * @params
     *      oimDBConnection - connection to the OIM Schema
     *      processKey - resource object key (TOS.TOS_KEY)
     *      reconFieldName - reconciliation field name (ORF.ORF_FIELDNAME)
     *      formFieldColumnName - process form field (PRF_COLUMNNAME)
     *      
     * @return - true for if it exists, false otherwise
     */
    public static Boolean doesPRFMappingExist(Connection oimDBConnection, Long processKey, String reconFieldName, String formFieldColumnName)
    {        
        Statement st = null;
        ResultSet rs = null;
            
        try 
        {
            String query = "SELECT COUNT(*) AS numRows  FROM PRF INNER JOIN "
                    + "ORF ON PRF.ORF_KEY = ORF.ORF_KEY WHERE TOS_KEY = '" + processKey + "' AND "
                    + "ORF_FIELDNAME = '" + reconFieldName + "' AND PRF_COLUMNNAME = '" + formFieldColumnName + "'"  ;
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
     * Determine a reconciliation field and form field mapping exist.
     * Queries from PRF (process and reconcilaition field mapping table) table. 
     * 
     * @params
     *      oimDBConnection - connection to the OIM Schema
     *      processKey - resource object key (TOS.TOS_KEY)
     *      reconFieldKey - reconciliation field name (ORF.ORF_FIELDNAME)
     *      
     * @return - true for if it exists, false otherwise
     */
    public static Boolean doesPRFMappingExist(Connection oimDBConnection, Long processKey, String reconFieldKey)
    {        
        Statement st = null;
        ResultSet rs = null;
            
        try 
        {
            String query = "SELECT COUNT(*) AS numRows  FROM PRF "
                    + "WHERE TOS_KEY = '" + processKey + "' AND "
                    + "ORF_KEY = '" + reconFieldKey + "'";
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
     * Get the corresponding object key associated with a process key.
     * 
     * @param 
     *      conn - connection to the OIM Schema 
     *      processKey - process key (TOS.TOS_KEY or PKG.PKG_KEY)
     * 
     * @return - corresponding object key
     */
    public static Long getObjKeyByProcKey(Connection conn, Long processKey)
    {
        Statement st = null;
        ResultSet rs = null;
        
        try 
        {
            String query = "SELECT OBJ.OBJ_KEY FROM "
             + "TOS RIGHT OUTER JOIN PKG ON PKG.PKG_KEY = TOS.PKG_KEY "
             + "LEFT OUTER JOIN SDK ON SDK.SDK_KEY = TOS.SDK_KEY "
             + "LEFT OUTER JOIN OBJ ON OBJ.OBJ_KEY = PKG.OBJ_KEY "
             + "WHERE PKG.PKG_KEY = " + processKey;
            //System.out.println(query);
            
            st = conn.createStatement(); //Create a statement
            rs = st.executeQuery(query);

            if(rs.next())
            {
                String objKey = rs.getString("OBJ_KEY");
                return (objKey == null || objKey.isEmpty()) ? null : Long.parseLong(objKey) ;
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
        
        return null;
        
    }
    
     /*
     * Get the reconciliation field key.
     * 
     * @params
     *      oimDBConnection - connection to the OIM Schema
     *      objectKey - resource object key (OBJ.OBJ_KEY)
     *      reconFieldName - reconciliation field name (ORF_FIELD_NAME)
     *      
     * @return - recon field key; otherwise null
     */
    public static String getReconFieldKey(Connection oimDBConnection, Long objectKey, String reconFieldName)
    {        
        Statement st = null;
        ResultSet rs = null;
            
        try 
        {
            String query = "SELECT ORF_KEY FROM ORF WHERE OBJ_KEY = " + objectKey + " AND ORF_FIELDNAME = '" + reconFieldName + "'";
            //System.out.println(query);
            
            st = oimDBConnection.createStatement(); //Create a statement
            rs = st.executeQuery(query);

            if(rs.next())
            {
                String reconKey = rs.getString("ORF_KEY");
                return reconKey;
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
        
        return null;
    }
}
