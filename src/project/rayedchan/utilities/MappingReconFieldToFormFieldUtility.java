package project.rayedchan.utilities;

import Thor.API.Exceptions.tcAPIException;
import Thor.API.Exceptions.tcColumnNotFoundException;
import Thor.API.Exceptions.tcFormNotFoundException;
import Thor.API.Exceptions.tcObjectNotFoundException;
import Thor.API.Exceptions.tcProcessNotFoundException;
import Thor.API.Operations.tcFormDefinitionOperationsIntf;
import Thor.API.Operations.tcObjectOperationsIntf;
import Thor.API.tcResultSet;
import com.thortech.xl.dataaccess.tcDataProvider;
import com.thortech.xl.dataaccess.tcDataSet;
import com.thortech.xl.dataaccess.tcDataSetException;
import com.thortech.xl.dataobj.PreparedStatementUtil;
import com.thortech.xl.orb.dataaccess.tcDataAccessException;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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
import project.rayedchan.exception.BadFileFormatException;
import project.rayedchan.exception.MissingRequiredFieldException;
import project.rayedchan.exception.NoResourceObjForProcessDefException;
import project.rayedchan.exception.ProcessDefintionNotFoundException;
import project.rayedchan.exception.ProcessFormNotFoundException;
import project.rayedchan.exception.ResourceObjectNotFoundException;

/**
 * @author rayedchan
 * A utility to map reconciliation fields to process form fields. A flat file 
 * may be used as a data source to define this mappings. This does not support 
 * multivalued fields at the moment. Also, this utility does not support mapping
 * of special fields. (E.g. OIM_OBJECT_STATUS). 
 * 
 * Note: Only form fields on the current active process form will be mapped for 
 * this utility. You have to make the current form version active before using this utility.
 * 
 * Differences between Design Console and API:
 * The API allows creation of recon mapping by using fields from the current/latest process form. 
 * An error will occur if you create a mapping with a process form field 
 * that is not in the current active process form whenever you try to click "Create Reconciliation Profile"
 * button on the resource object form.
 * Design console only allows user to map form field on the active form.
 * 
 * TODO: Filter Multi-valued recon fields
 * Catch type mismatch between form field and recon field
 */
public class MappingReconFieldToFormFieldUtility 
{    
    //List of possible attribute names to be specified in file for adding a mapping between a recon filed and a form field
    public static String RECONFIELDNAME = "recon_field_name"; //required
    public static String FORMFIELDCOLUMNNAME = "form_field_column_name"; //required
    public static String ISKEYFIELD = "is_key_field"; //optional; default value = false
    public static String IS_CASE_INSENSITIVE = "is_case_insensitive"; //optional: default value = false
    
    /*
     * Print the current mappings of the reconciliation fields and process form
     * fields with sorting options.
     * PRF Table contains all the process field and reconcilation field mappings
     * @param   formDefOps      tcFormDefinitionOperationsIntf service object
     * @param   processKey      the resource process key (PKG.PKG_KEY or TOS.TOS_KEY)
     * @param   columnToSort    sort by recon field or form field column; 0 = Reconciliation fields, 1 = Process form fields 
     * @param   descOrAsc       sort by descending or ascending order; 0 = Descending order,  1 = Ascending order            
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
     * PRF Table contains all the process field and reconcilation field mappings
     * @param   formDefOps      tcFormDefinitionOperationsIntf service object
     * @param   processKey      the resource process key (PKG._PKG_KEY or TOS.TOS_KEY)
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
     * @param   formDefOps  tcFormDefinitionOperationsIntf service object
     * @param   formKey     the resource process key (PKG._PKG_KEY)
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
     * @param   resourceObjectOps   tcObjectOperationsIntf service object
     * @param   objKey              resource object key (OBJ.OBJ_KEY)
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
     * @param   formDefOps                  tcFormDefinitionOperationsIntf service object
     * @param   processKey                  the resource process key (PKG.PKG_KEY)
     * @param   objectKey                   the resource object key (OBJ.OBJ_KEY)
     * @param   reconField_FormField_Map    ReconFieldAndFormFieldMap object
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
     * @param   formDefOps      tcFormDefinitionOperationsIntf service object
     * @param   processKey      the resource process key (PKG.PKG_KEY)
     * @param   objectKey       the resource object key (OBJ.OBJ_KEY)
     * @param   reconFieldKey   reconciliation field key (ORF.ORF_KEY)
     */
    public static void removeReconFieldAndFormFieldMap(tcFormDefinitionOperationsIntf formDefOps, Long processKey, Long objectKey, String reconFieldKey) throws tcAPIException, tcProcessNotFoundException, tcObjectNotFoundException
    {
       formDefOps.removeReconDataFlowMapping(processKey, objectKey, reconFieldKey);
    }
    
    /*
     * Export the current mappings of the reconciliation fields and process form
     * fields in a file format for adding. Mulitvalued fields and nested fields will not be exported.
     * "Structure Utility.Table Name" will be used to determine if a mapping uses
     * a mulitvalued field.
     * PRF Table contains all the process field and reconcilation field mappings
     * @param   dbProvider      connection to the OIM Schema
     * @param   formDefOps      tcFormDefinitionOperationsIntf service object
     * @param   fileName        Name of file to write to
     * @param   processDefName  Process definition name (PKG.PKG_NAME)
     * @param   delimiter       Use to separate values from file
     */
    public static void exportReconFieldAndFormFieldMappingsAddDSFF(tcDataProvider dbProvider, tcFormDefinitionOperationsIntf formDefOps, String fileName, String processDefName, String delimiter) throws tcAPIException, tcProcessNotFoundException, tcColumnNotFoundException, tcDataSetException, tcDataAccessException, ProcessDefintionNotFoundException, FileNotFoundException, UnsupportedEncodingException
    {
        PrintWriter writer = null;
        try
        {
            writer = new PrintWriter(fileName, "UTF-8");
            
            long processKey = getProcessKeyByProcessDefinitionName(dbProvider, processDefName);
            tcResultSet mappingResultSet = formDefOps.getReconDataFlowForProcess(processKey);
            int numRows = mappingResultSet.getTotalRowCount();

            writer.printf("%s%s%s%s%s%s%s\n",RECONFIELDNAME, delimiter, FORMFIELDCOLUMNNAME, delimiter, ISKEYFIELD, delimiter, IS_CASE_INSENSITIVE);
            for(int i = 0; i < numRows; i++)
            {
                mappingResultSet.goToRow(i);
                String childTableName = mappingResultSet.getStringValue("Structure Utility.Table Name");
                String reconField = mappingResultSet.getStringValue("Objects.Reconciliation Fields.Name");
                String formField = mappingResultSet.getStringValue("Process Definition.Reconciliation Fields Mappings.ColumnName");
                String isKeyField = mappingResultSet.getStringValue("Process Definition.Reconciliation Fields Mappings.Iskey");
                String isCaseInsensitive = mappingResultSet.getStringValue("PRF_CASE_INSENSITIVE");

                if(childTableName == null || childTableName.isEmpty())
                {
                    writer.printf("%s%s%s%s%s%s%s\n", reconField, delimiter, formField, delimiter, isKeyField, delimiter, isCaseInsensitive);
                }
            } 
        }
            
        finally
        {
            if(writer != null)
            {
                writer.close();
            }
        }
    } 
    
    /*
     * Get the process key by process definition name.
     * Name of process definition is case insenstive.
     * @param processDefName    Name of the process deinition
     * @return process key (PKG.PKG_KEY)
     */
    public static Long getProcessKeyByProcessDefinitionName(tcDataProvider dbProvider, String processDefName) throws tcDataSetException, tcDataAccessException, ProcessDefintionNotFoundException
    {     
        tcDataSet procDefDataSet = null;
        PreparedStatementUtil ps = null;
        
        try 
        {
            String query = "SELECT PKG_KEY FROM PKG WHERE LOWER(PKG_NAME) = LOWER(?)";
            ps = new PreparedStatementUtil();
            ps.setStatement(dbProvider, query);
            ps.setString(1, processDefName);
            ps.execute();
            procDefDataSet = ps.getDataSet();
            int numRecord = procDefDataSet.getTotalRowCount();
            
            if(numRecord == 1)
            {         
                Long procKey = procDefDataSet.getLong("PKG_KEY");
                return procKey;
            }
            
            throw new ProcessDefintionNotFoundException(String.format("Process defintion '%s' does not exist.", processDefName));
        } 
        
        finally
        { 
        }  
    }
    
    /*
     * Map reconciliation fields and form fields specified in a flat file.
     * File must be tab delimited. Mulitvalued is not supported.
     * Process Key and Object Key must be associated with a process form.
     * Only form fields in the current active version of a process form will be looked at. 
     * 
     * By default all mappings will not be case insensitive and not a key field.
     * Form Field Column Name must be all uppercase or it will be deemed invalid
     * A reconciliation field can only map to one form field.
     * 
     * Checks:
     * Do not add existing mappings.
     * TODO: Check recon field and process form field are the same type.
     * Enforce reconciliation fields and process form field to be one to one.
     * Make sure process form field and recon field have not been used in a mapping. 
     * 
     * File format
     * <recon_field_name form_field_column_name	is_key_field[optional] is_case_insensitive[optional]>
     * <reconFieldName1>    <formFieldColumnName1>
     * <reconFieldName2>    <formFieldColumnName2>
     * 
     * @param   dbProvider      connection to the OIM Schema
     * @param   formDefOps      tcFormDefinitionOperationsIntf service
     * @param   fileName        path of file    
     * @param   processDefName  Process definition name
     * @param   delimiter       Used to separate values in file
     * @return  true for success; false otherwise 
     */
    public static Boolean addReconFieldAndFormFieldMapDSFF(tcDataProvider dbProvider, tcFormDefinitionOperationsIntf formDefOps, String fileName, String processDefName, String delimiter) throws tcAPIException, tcFormNotFoundException, tcColumnNotFoundException, tcDataSetException, tcDataAccessException, ProcessDefintionNotFoundException, NoResourceObjForProcessDefException, ResourceObjectNotFoundException, ProcessFormNotFoundException, MissingRequiredFieldException, BadFileFormatException, FileNotFoundException, IOException
    {        
        HashMap<String,String> mappings = new HashMap<String,String>(); //store all the mappings of a recon field and a form field in staging
        FileInputStream fstream = null;
        DataInputStream in = null;
        BufferedReader br = null;
        int lineNumber = 0;
        
        try 
        {     
            fstream = new FileInputStream(fileName); //Open File
            in = new DataInputStream(fstream); //Get the object of DataInputStream
            br = new BufferedReader(new InputStreamReader(in));
            ArrayList<ReconFieldAndFormFieldMap> mappingsToBeAdded = new ArrayList<ReconFieldAndFormFieldMap>();
            String strLine; //var to store a line of a file
           
            long processKey = getProcessKeyByProcessDefinitionName(dbProvider, processDefName);
            long objectKey = getObjKeyByProcKey(dbProvider, processKey);
            
            if(doesObjectExist(dbProvider, objectKey) == false)
            {
                System.out.println("[Error]: Object Key "+ objectKey + " does not exist.");
                throw new ResourceObjectNotFoundException(String.format("Object Key %s does not exist.", objectKey));
            }
            
            long formKey = getFormKeyByObjAndProcKey(dbProvider, processKey, objectKey); //get the process form key associated with the obj key and prco key
            
            //First line determines the ordering of the mappings attributes in the file
            ArrayList<String> mappingAttributeNameArray = new ArrayList<String>(); //store the name of each mapping attribute name 
            String mappingAttributeNames = br.readLine();
            StringTokenizer mappingAttributeNameTokens = new StringTokenizer(mappingAttributeNames, delimiter);
            lineNumber++;
            
            while(mappingAttributeNameTokens.hasMoreTokens())
            {
                String mappingAttributeName = mappingAttributeNameTokens.nextToken();
 
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
                    System.out.printf("Mapping attribute name %s is invalid."
                    + "Here are all the possible mapping attribute names: %s, %s, %s, %s",
                     mappingAttributeName ,RECONFIELDNAME, FORMFIELDCOLUMNNAME, ISKEYFIELD, IS_CASE_INSENSITIVE);
                    throw new BadFileFormatException(String.format("Mapping attribute name %s is invalid."
                    + "Here are all the possible mapping attribute names: %s, %s, %s, %s",
                     mappingAttributeName ,RECONFIELDNAME, FORMFIELDCOLUMNNAME, ISKEYFIELD, IS_CASE_INSENSITIVE));
                }
            }
            
            //Validate that "recon_field_name" is specified in the file.
            if(!mappingAttributeNameArray.contains(RECONFIELDNAME))
            {
                System.out.println("'"+ RECONFIELDNAME + "' is a required mapping attribute to be specified in file");
                throw new MissingRequiredFieldException("'"+ RECONFIELDNAME + "' is a required mapping attribute to be specified in file");
            }
            
            //Validate that "form_field_column_name" is specified in the file.
            if(!mappingAttributeNameArray.contains(FORMFIELDCOLUMNNAME))
            {
                System.out.println("'"+ FORMFIELDCOLUMNNAME + "' is a required mapping attribute to be specified in file");
                throw new MissingRequiredFieldException("'"+ FORMFIELDCOLUMNNAME + "' is a required mapping attribute to be specified in file");
            }
            
            //Rest of the file should be the mappings 
            while ((strLine = br.readLine()) != null)  
            {
                lineNumber++;
                String[] mappingToken = strLine.split(delimiter);
                int mappingAttrNameFileCount = mappingAttributeNameArray.size();
                int numTokens = mappingToken.length;
                ReconFieldAndFormFieldMap fieldMapping = new ReconFieldAndFormFieldMap();
                 
                //validate the line format
                if(numTokens != mappingAttrNameFileCount)
                {
                    System.out.println("[Warning] Line = " + lineNumber +" : Size of row is invalid. Mapping will not be added:\n" + strLine);
                    continue;
                }
                                
                boolean isMappingFromFileValid = true;
                String reconFieldName = null;
                String formFieldColumnName = null;
                String reconFieldKey = null;
                
                for(int i = 0; i < mappingAttrNameFileCount; i++)
                {
                    String mappingAttributeName = mappingAttributeNameArray.get(i);
                   
                    if(mappingAttributeName.equalsIgnoreCase(RECONFIELDNAME))
                    {
                        reconFieldName = mappingToken[i];
                        
                        //validate the existence of the recon field on current line
                        if(ReconFieldUtility.doesReconFieldNameExist(dbProvider, objectKey, reconFieldName) == false)
                        {
                            System.out.println("[Warning] Line = " + lineNumber +" : Reconciliation Field '" + reconFieldName + "' does not exist. Mapping will not be added:\n" + strLine);
                            isMappingFromFileValid = false;
                            break;
                        }
                        
                        //Check if the recon field is a multivalued attribute
                        if(ReconFieldUtility.isReconFieldMulitvalued(dbProvider, objectKey, reconFieldName) == true)
                        {
                            System.out.println("[Warning] Line = " + lineNumber +" : Recon Field'" + reconFieldName + "' is a mulitvalued attribute. Mapping will not be added:\n" + strLine);
                            isMappingFromFileValid = false;
                            break;
                        }

                        //Check if the recon field is a child attribute
                        if(ReconFieldUtility.isReconFieldChildAttribute(dbProvider, objectKey , reconFieldName) == true)
                        {
                            System.out.println("[Warning] Line = " + lineNumber +" : Recon Field'" + reconFieldName + "' is a child attribute. Mapping will not be added:\n" + strLine);
                            isMappingFromFileValid = false;
                            break; 
                        }
                        
                        reconFieldKey = getReconFieldKey(dbProvider, objectKey, reconFieldName);
                        fieldMapping.setReconFieldKey(reconFieldKey);
                        fieldMapping.setReconFieldName(reconFieldName);
                    }

                    else if(mappingAttributeName.equalsIgnoreCase(FORMFIELDCOLUMNNAME))
                    {  
                        formFieldColumnName = mappingToken[i];
                        
                        //Validate the existence of the form field
                        if(doesFormFieldExist(formDefOps, formKey, formFieldColumnName) == false)
                        {
                            System.out.println("[Warning] Line = " + lineNumber +" : Form Field '" + reconFieldName + "' does not exist on current active form. Mapping will not be added:\n" + strLine);
                            isMappingFromFileValid = false;
                            break;
                        }
                        
                        fieldMapping.setFormFieldColumnName(formFieldColumnName);
                    }

                    else if(mappingAttributeName.equalsIgnoreCase(ISKEYFIELD))
                    {
                        String isKeyFieldStr = mappingToken[i];
                        boolean isKeyField;
                        
                        //check if the type is valid
                        if(isKeyFieldStr.equalsIgnoreCase("1"))
                        {
                            isKeyField = true;
                        }
                        
                        else if(isKeyFieldStr.equalsIgnoreCase("0") || isKeyFieldStr.equalsIgnoreCase(""))
                        {
                            isKeyField = false;
                        }
                        
                        else
                        {
                            System.out.println("[Warning] Line = " + lineNumber +" : 'is_key_field' attibute type '" + isKeyFieldStr + "' is not valid (0 = false, 1 = true). Mapping will not be added:\n" + strLine);
                            isMappingFromFileValid = false;
                            break; 
                        }
                        
                        fieldMapping.setIsKeyField(isKeyField);
                    }

                    else if(mappingAttributeName.equalsIgnoreCase(IS_CASE_INSENSITIVE))
                    {                       
                        String isCaseInsensitiveStr = mappingToken[i];
                        boolean isCaseInsensitive;
                        
                        //check if the type is valid
                        if(isCaseInsensitiveStr.equalsIgnoreCase("1"))
                        {
                            isCaseInsensitive = true;
                        }
                        
                        else if(isCaseInsensitiveStr.equalsIgnoreCase("0") || isCaseInsensitiveStr.equalsIgnoreCase(""))
                        {
                            isCaseInsensitive = false;
                        }
                        
                        else
                        {
                            System.out.println("[Warning] Line = " + lineNumber +" : 'is_case_insensitive' attribute type '" + isCaseInsensitiveStr + "' is not a valid value (0 = false, 1 = true). Mapping will not be added:\n" + strLine);
                            isMappingFromFileValid = false;
                            break; 
                        }
                        
                        fieldMapping.setIsCaseInsensitive(isCaseInsensitive);
                    }

                }//end for loop
                
                
                if(isMappingFromFileValid == true)
                {                 
                    //Validate if the mapping already exist in OIM
                    if(doesPRFMappingExist(dbProvider, processKey, reconFieldName, formFieldColumnName) == true)
                    {  
                       System.out.println("[Warning] Line = " + lineNumber +" : Mapping already exists. Mapping will not be added:\n" + strLine);
                       continue; 
                    }
                    
                    //Validate if the process form field has already been mapped to another recon field
                    if(isFormFieldMapped(dbProvider, processKey, formFieldColumnName))
                    {
                       System.out.println("[Warning] Line = " + lineNumber +" : Process form field " + formFieldColumnName + " has been used in another mapping. Mapping will not be added:\n" + strLine);
                       continue; 
                    }
                    
                    //Validate if the recon form field has already been mapped
                    if(isReconFieldMapped(dbProvider, processKey, reconFieldKey))
                    {
                        System.out.println("[Warning] Line = " + lineNumber +" : Reconciliation Field '" + reconFieldName + "' has already been mapped. Mapping will not be added:\n" + strLine);
                        continue;
                    }
                    
                    //Enforce one to one mapping and make sure recon field or process form field are not duplicated
                    if(!mappings.containsValue(formFieldColumnName) && !mappings.containsKey(reconFieldName))
                    { 
                        mappings.put(reconFieldName, formFieldColumnName); 
                        mappingsToBeAdded.add(fieldMapping);
                    }
                    
                    else
                    {
                        System.out.println("[Warning] Line = " + lineNumber +" : Recon field or form field already exist in staging. Mapping will not be added:\n" + strLine);
                    }
                }
            }//end while loop
            
            System.out.println("Mappings to be added: " + mappingsToBeAdded.toString());
            for(ReconFieldAndFormFieldMap reconField_FormField_Map : mappingsToBeAdded)
            {
                try {
                    addReconFieldAndFormFieldMap(formDefOps, processKey, objectKey, reconField_FormField_Map);
                } catch (tcProcessNotFoundException ex) {
                    Logger.getLogger(MappingReconFieldToFormFieldUtility.class.getName()).log(Level.SEVERE, String.format("Failed to add %s\n", reconField_FormField_Map), ex);
                } catch (tcObjectNotFoundException ex) {
                    Logger.getLogger(MappingReconFieldToFormFieldUtility.class.getName()).log(Level.SEVERE, String.format("Failed to add %s\n", reconField_FormField_Map), ex);
                } catch(tcAPIException ex){
                    Logger.getLogger(MappingReconFieldToFormFieldUtility.class.getName()).log(Level.SEVERE, String.format("Failed to add %s\n", reconField_FormField_Map), ex);
                }
            }
            
            return true;    
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
     * <reconFieldName1>    
     * <reconFieldName2>  
     * 
     * @param   dbProvider      connection to the OIM Schema
     * @param   formDefOps      tcFormDefinitionOperationsIntf service
     * @param   fileName        path of file    
     * @param   processDefName  Name of the process definition to remove mappings
     * @return  true for success; false otherwise 
     */
    public static Boolean removeReconFieldAndFormFieldMapDSFF(tcDataProvider dbProvider, tcFormDefinitionOperationsIntf formDefOps, String fileName, String processDefName) throws FileNotFoundException, IOException, tcDataSetException, tcDataAccessException, ProcessDefintionNotFoundException, NoResourceObjForProcessDefException, ResourceObjectNotFoundException 
    {        
        HashMap<String,String> mappingsToRemove = new HashMap<String,String>(); //store all the mappings to be removed
        FileInputStream fstream = null;
        DataInputStream in = null;
        BufferedReader br = null;
        int lineNumber = 0;
        
        try 
        {     
            fstream = new FileInputStream(fileName); //Open File
            in = new DataInputStream(fstream); //Get the object of DataInputStream
            br = new BufferedReader(new InputStreamReader(in));
            String strLine; //var to store a line of a file
            
            long processKey = getProcessKeyByProcessDefinitionName(dbProvider, processDefName);
            long objectKey = getObjKeyByProcKey(dbProvider, processKey);
            
            if(doesObjectExist(dbProvider, objectKey) == false)
            {
                System.out.println("[Error]: Object Key "+ objectKey + " does not exist.");
                throw new ResourceObjectNotFoundException(String.format("Object Key %s does not exist.", objectKey));
            }
            
            //Rest of the file should be the the reconciliation field names
            while ((strLine = br.readLine()) != null)  
            {
                 lineNumber++;
                 String reconFieldName = strLine;
                                    
                 //validate the existence of the recon field on current line
                 if(ReconFieldUtility.doesReconFieldNameExist(dbProvider, objectKey, reconFieldName) == false)
                 {      
                     System.out.println("[Warning] Line = " + lineNumber +" : Reconciliation Field '" + reconFieldName + "' does not exist. Mapping will not be removed.");
                     continue;
                 }
                  
                 //Check if the recon field is a multivalued attribute
                 if(ReconFieldUtility.isReconFieldMulitvalued(dbProvider, objectKey, reconFieldName) == true)
                 {
                     System.out.println("[Warning] Line = " + lineNumber +" : Reconciliation Field '" + reconFieldName + "' is Multivalued. Mapping will not be removed.");
                     continue;
                 }

                 //Check if the recon field is a child attribute
                 if(ReconFieldUtility.isReconFieldChildAttribute(dbProvider, objectKey , reconFieldName) == true)
                 {
                     System.out.println("[Warning] Line = " + lineNumber +" : Reconciliation Field '" + reconFieldName + "' is a child field. Mapping will not be removed.");
                     continue;
                 }
                        
                 String reconFieldKey = getReconFieldKey(dbProvider, objectKey, reconFieldName);
                              
                 //Validate if the mapping exist in OIM
                 if(isReconFieldMapped(dbProvider, processKey, reconFieldKey) == false)
                 {   
                     System.out.println("[Warning] Line = " + lineNumber +" : Mapping for reconciliation field does not exists. Mapping will not be removed:\n" + strLine);
                     continue; 
                 }
                 
                 //Check if the reconciliation field already exist in staging 
                 if(!mappingsToRemove.containsValue(reconFieldName) && !mappingsToRemove.containsKey(reconFieldKey))
                 { 
                     mappingsToRemove.put(reconFieldKey, reconFieldName); 
                 }
                    
                 else
                 {
                     System.out.println("[Warning] Line = " + lineNumber +" : Mapping for reconciliation field has already been added to staging. Mapping will not be added:\n" + strLine);
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
                String reconFieldName = (String) pairs.getValue();
                
                try {
                    removeReconFieldAndFormFieldMap(formDefOps, processKey, objectKey, reconFieldKeyToRemove);
                } catch (tcAPIException ex) {
                    Logger.getLogger(MappingReconFieldToFormFieldUtility.class.getName()).log(Level.SEVERE, String.format("Failed to remove %s", reconFieldName), ex);
                } catch (tcProcessNotFoundException ex) {
                    Logger.getLogger(MappingReconFieldToFormFieldUtility.class.getName()).log(Level.SEVERE, String.format("Failed to remove %s", reconFieldName), ex);
                } catch (tcObjectNotFoundException ex) {
                    Logger.getLogger(MappingReconFieldToFormFieldUtility.class.getName()).log(Level.SEVERE, String.format("Failed to remove %s", reconFieldName), ex);
                }
               
                it.remove(); // avoids a ConcurrentModificationException
            }

            return true;     
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
    }
    
    /*
     * Determine if a process exist.
     * Queries from the PKG table. 
     * @param   dbProvider     connection to the OIM Schema
     * @param   processKey     process key to check existence (PKG_KEY)
     * @return  true for if it exists, false otherwise
     */
    public static Boolean doesProcessExist(tcDataProvider dbProvider, Long processKey) throws tcDataSetException, tcDataAccessException
    {        
        tcDataSet procDefDataSet = null;
        PreparedStatementUtil ps = null;
            
        try 
        {
            String query = "SELECT COUNT(*) AS numRows FROM PKG WHERE PKG_KEY = ?";   
            ps = new PreparedStatementUtil();
            ps.setStatement(dbProvider, query);
            ps.setLong(1, processKey);
            ps.execute();
            procDefDataSet = ps.getDataSet();

            if(procDefDataSet.getInt("numRows") == 1)
            {
               return true;  
            }    
        } 

        finally
        {
            
        }
        
        return false;
    }
    
    /*
     * Determine if a object exist.
     * Queries from the OBJ table.
     * @param   dbProvider  connection to the OIM Schema
     * @param   objectKey   object key to check existence (OBJ.OBJ_KEY)
     * @return  true for if it exists, false otherwise
     */
    public static Boolean doesObjectExist(tcDataProvider dbProvider, Long objectKey) throws tcDataSetException, tcDataAccessException
    {         
        tcDataSet objDataSet = null;
        PreparedStatementUtil ps = null;
            
        try 
        {
            String query = "SELECT COUNT(*) AS numRows FROM OBJ WHERE OBJ_KEY = ?";      
            ps = new PreparedStatementUtil();
            ps.setStatement(dbProvider, query);
            ps.setLong(1, objectKey);
            ps.execute();
            objDataSet = ps.getDataSet();
            
            if(objDataSet.getInt("numRows") == 1)
            {
               return true;  
            }    
        } 
        
        finally
        {
        }
        
        return false;
    }
        
     /*
     * Determine if a form field column name exist on the current active version of a process form.
     * This method does not consider any child forms (mulitvalued attributes) and
     * will return false on those child attributes.
     * 
     * SDC table in the OIM Schema contains all the form fields.
     * Structure Utility.Additional Columns.Name (SDC_NAME) - Field Column Name
     * 
     * @param   formDefOps          tcFormDefinitionOperationsIntf service object
     * @param   formKey             the resource process key (PKG._PKG_KEY)
     * @param   fieldColumnName     column name of a field form (SDC.SDC_NAME)
     */
    public static Boolean doesFormFieldExist(tcFormDefinitionOperationsIntf formDefOps, long formKey, String fieldColumnName) throws tcAPIException, tcFormNotFoundException, tcColumnNotFoundException
    {
        tcResultSet formVersionResultSet = formDefOps.getFormVersions(formKey); //get versions of current process form
        int numRows = formVersionResultSet.getTotalRowCount();
        Integer currentFormVersion = null;
        
        for(int i = 0; i < numRows; i++)
        {
            formVersionResultSet.goToRow(i);
            String formVersionStr = formVersionResultSet.getStringValue("Structure Utility.Active Version");
            currentFormVersion = (formVersionStr == null ||  formVersionStr.isEmpty())? null : Integer.parseInt(formVersionStr);
        }
       
        tcResultSet fieldResultSet = formDefOps.getFormFields(formKey, currentFormVersion);
        int numFields = fieldResultSet.getTotalRowCount();
        
        for(int i = 0; i < numFields; i++ )
        {
            fieldResultSet.goToRow(i);
            String fieldName = fieldResultSet.getStringValue("Structure Utility.Additional Columns.Name");
            
            if(fieldName.equalsIgnoreCase(fieldColumnName))
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
     * @param dbProvider    connection to the OIM Schema 
     * @param processKey    process key (TOS.TOS_KEY or PKG.PKG_KEY)
     * @param objectKey     resource object key (OBJ.OBJ_KEY)     
     * @return form key associated with given object key and process key
     */
    public static Long getFormKeyByObjAndProcKey(tcDataProvider dbProvider, Long processKey, Long objectKey) throws tcDataSetException, tcDataAccessException, ProcessFormNotFoundException
    {     
        tcDataSet objDataSet = null;
        PreparedStatementUtil ps = null;
        
        try 
        {
            String query = "SELECT SDK.SDK_KEY FROM "
             + "TOS RIGHT OUTER JOIN PKG ON PKG.PKG_KEY = TOS.PKG_KEY "
             + "LEFT OUTER JOIN SDK ON SDK.SDK_KEY = TOS.SDK_KEY "
             + "LEFT OUTER JOIN OBJ ON OBJ.OBJ_KEY = PKG.OBJ_KEY "
             + "WHERE PKG.PKG_KEY = ? AND OBJ.OBJ_KEY = ?";
        
            ps = new PreparedStatementUtil();
            ps.setStatement(dbProvider, query);
            ps.setLong(1, processKey);
            ps.setLong(2, objectKey);
            ps.execute();
            objDataSet = ps.getDataSet();
            Long formKey = objDataSet.getLong("SDK_KEY");  
            
            if(formKey == null || formKey == 0)
            {
                throw new ProcessFormNotFoundException(String.format("Object Key '%s' and Process Key '%s' are not associated with a form.", objectKey, processKey)); 
            }
            
            return formKey;
        } 
        
        finally
        {
            
        }
    }
    
     /*
     * Determine if a reconciliation field and form field mapping exist.
     * Queries from the ORF and PRF (mapping table) table.
     * Method is case sensitive for comparing reconciliation field names.
     * @param  dbProvider           connection to the OIM Schema
     * @param  processKey           resource process key (TOS.TOS_KEY)
     * @param  reconFieldName       reconciliation field name (ORF.ORF_FIELDNAME)
     * @param  formFieldColumnName  process form field (PRF_COLUMNNAME)    
     * @return true for if it exists, false otherwise
     */
    public static Boolean doesPRFMappingExist(tcDataProvider dbProvider, Long processKey, String reconFieldName, String formFieldColumnName) throws tcDataSetException, tcDataSetException, tcDataAccessException
    {             
        tcDataSet mappingDataSet = null;
        PreparedStatementUtil ps = null;
            
        try 
        {
            String query = "SELECT COUNT(*) AS numRows  FROM PRF INNER JOIN "
                    + "ORF ON PRF.ORF_KEY = ORF.ORF_KEY WHERE TOS_KEY = ? AND "
                    + "ORF_FIELDNAME = ? AND LOWER(PRF_COLUMNNAME) = LOWER(?)";
                  
            ps = new PreparedStatementUtil();
            ps.setStatement(dbProvider, query);
            ps.setLong(1, processKey);
            ps.setString(2, reconFieldName);
            ps.setString(3, formFieldColumnName);
            ps.execute();
            mappingDataSet = ps.getDataSet();
            
            if(mappingDataSet.getInt("numRows") >= 1)
            {
               return true;  
            }    
        } 
        
        finally
        {
        }
        
        return false;
    }
    
    /*
     * Determine if a reconciliation field has already been mapped.
     * Queries from PRF (process and reconcilaition field mapping table) table. 
     * @param   dbProvider          connection to the OIM Schema
     * @param   processKey          resource process key (TOS.TOS_KEY)
     * @param   reconFieldKey       reconciliation field name (ORF.ORF_FIELDNAME)
     * @return true for if it exists, false otherwise
     */
    public static Boolean isReconFieldMapped(tcDataProvider dbProvider, Long processKey, String reconFieldKey) throws tcDataSetException, tcDataAccessException
    {              
        tcDataSet mappingDataSet = null;
        PreparedStatementUtil ps = null;
            
        try 
        {
            String query = "SELECT COUNT(*) AS numRows FROM PRF "
                    + "WHERE TOS_KEY = ? AND ORF_KEY = ?";
        
            ps = new PreparedStatementUtil();
            ps.setStatement(dbProvider, query);
            ps.setLong(1, processKey);
            ps.setString(2, reconFieldKey);
            ps.execute();
            mappingDataSet = ps.getDataSet();  
            
            if(mappingDataSet.getInt("numRows") == 1)
            {
               return true;  
            }    
        } 
        
        finally
        {
        }
        
        return false;
    }
    
    /*
     * Determine if a reconciliation field has already been mapped.
     * Queries from PRF (process and reconcilaition field mapping table) table. 
     * @param   oimDBConnection     connection to the OIM Schema
     * @param   reconFieldKey       reconciliation field name (ORF.ORF_FIELDNAME)
     * @return  true for if it exists, false otherwise
     */
    public static Boolean isReconFieldMapped(tcDataProvider dbProvider, String reconFieldKey) throws tcDataSetException, tcDataAccessException
    {          
        tcDataSet prfDataSet = null;
        PreparedStatementUtil ps = null;
            
        try 
        {
            String query = "SELECT COUNT(*) AS numRows  FROM PRF WHERE ORF_KEY = ?";
            ps = new PreparedStatementUtil();
            ps.setStatement(dbProvider, query);
            ps.setString(1, reconFieldKey);
            ps.execute();
            prfDataSet = ps.getDataSet();
            int numRecords = prfDataSet.getInt("numRows"); 
            
            if(numRecords == 1)
            {
               return true;  
            }    
        } 
        
        finally
        { 
        }
        
        return false;
    }
    
    /*
     * Determine a if a process form field has been used in an existing mapping.
     * Queries from PRF (process and reconcilaition field mapping table) table. 
     * This method is case insensitive. 
     * @param   dbProvider              connection to the OIM Schema
     * @param   processKey              resource object key (TOS.TOS_KEY)
     * @param   formFieldColumnName     form field column name (PRF_COLUMNNAME)  
     * @return  true for if it exists, false otherwise
     */
    public static Boolean isFormFieldMapped(tcDataProvider dbProvider, Long processKey, String formFieldColumnName) throws tcDataSetException, tcDataAccessException
    {              
        tcDataSet prfDataSet = null;
        PreparedStatementUtil ps = null;
            
        try 
        {
            String query = "SELECT COUNT(*) AS numRows FROM PRF "
                    + "WHERE TOS_KEY = ? AND LOWER(PRF_COLUMNNAME) = LOWER(?)";
            
            ps = new PreparedStatementUtil();
            ps.setStatement(dbProvider, query);
            ps.setLong(1, processKey);
            ps.setString(2, formFieldColumnName);
            ps.execute();
            prfDataSet = ps.getDataSet();
           
            if(prfDataSet.getInt("numRows") >= 1)
            {
               return true;  
            }    
        } 
        
        finally
        {
        }
        
        return false;
    }
    
    
    /*
     * Get the corresponding object key associated with a process key. The process
     * definition must have a relationship with a process form and a resource object.
     * PKG = Process Definition, TOS = Process relationship with Process Form
     * @param   dbProvider  connection to the OIM Schema 
     * @param   processKey  process key (PKG.PKG_KEY or TOS.TOS_KEY)
     * @return  corresponding object key of process definition
     */
    public static Long getObjKeyByProcKey(tcDataProvider dbProvider, Long processKey) throws tcDataSetException, tcDataAccessException, NoResourceObjForProcessDefException
    {     
        tcDataSet procDefDataSet = null;
        PreparedStatementUtil ps = null;
        
        try 
        {
            String query = "SELECT OBJ.OBJ_KEY FROM "
             + "TOS RIGHT OUTER JOIN PKG ON PKG.PKG_KEY = TOS.PKG_KEY " //relationship of process def and form
             + "LEFT OUTER JOIN SDK ON SDK.SDK_KEY = TOS.SDK_KEY " //Process Definition process form
             + "LEFT OUTER JOIN OBJ ON OBJ.OBJ_KEY = PKG.OBJ_KEY " //Process Defintion Object
             + "WHERE PKG.PKG_KEY = ?";
            
            ps = new PreparedStatementUtil();
            ps.setStatement(dbProvider, query);
            ps.setLong(1, processKey);
            ps.execute();
            procDefDataSet = ps.getDataSet();
            String objKey = procDefDataSet.getString("OBJ_KEY");

            if(procDefDataSet.getTotalRowCount() == 0)
            {
                throw new NoResourceObjForProcessDefException(String.format("Process Key '%s' has no resource object association or process does not exist.", processKey));
            }
            
            return (objKey == null || objKey.isEmpty()) ? null : Long.parseLong(objKey) ;
        } 
        
        finally
        {
        }   
    }
    
     /*
     * Get the reconciliation field key.
     * @param   dbProvider      connection to the OIM Schema
     * @param   objectKey       resource object key (OBJ.OBJ_KEY)
     * @param   reconFieldName  reconciliation field name (ORF_FIELD_NAME)  
     * @return  recon field key; otherwise null
     */
    public static String getReconFieldKey(tcDataProvider dbProvider, Long objectKey, String reconFieldName) throws tcDataSetException, tcDataAccessException
    {            
        tcDataSet rfDataSet = null;
        PreparedStatementUtil ps = null;
            
        try 
        {
            String query = "SELECT ORF_KEY FROM ORF WHERE OBJ_KEY = ? AND ORF_FIELDNAME = ?"; 
            ps = new PreparedStatementUtil();
            ps.setStatement(dbProvider, query);
            ps.setLong(1, objectKey);
            ps.setString(2, reconFieldName);
            ps.execute();
            rfDataSet = ps.getDataSet();
            String reconKey = rfDataSet.getString("ORF_KEY");
            return reconKey;
        } 
        
        finally
        { 
        }
    }
}