package project.rayedchan.testdriver;

import Thor.API.Exceptions.tcAPIException;
import Thor.API.Exceptions.tcAddFieldFailedException;
import Thor.API.Exceptions.tcColumnNotFoundException;
import Thor.API.Exceptions.tcDeleteNotAllowedException;
import Thor.API.Exceptions.tcDuplicateLookupCodeException;
import Thor.API.Exceptions.tcFormFieldNotFoundException;
import Thor.API.Exceptions.tcFormNotFoundException;
import Thor.API.Exceptions.tcInvalidAttributeException;
import Thor.API.Exceptions.tcInvalidLookupException;
import Thor.API.Exceptions.tcInvalidValueException;
import Thor.API.Exceptions.tcObjectNotFoundException;
import Thor.API.Exceptions.tcProcessFormException;
import Thor.API.Exceptions.tcProcessNotFoundException;
import Thor.API.Operations.tcFormDefinitionOperationsIntf;
import Thor.API.Operations.tcLookupOperationsIntf;
import Thor.API.Operations.tcObjectOperationsIntf;
import Thor.API.Operations.tcObjectOperationsIntfExtended;
import Thor.API.Operations.tcWorkflowDefinitionOperationsIntf;
import Thor.API.tcResultSet;
import com.thortech.xl.client.dataobj.tcORFClient;
import com.thortech.xl.ejb.interfaces.tcORF;
import com.thortech.xl.ejb.interfaces.tcORFDelegate;
import com.thortech.xl.vo.workflow.AdapterMapping;
import com.thortech.xl.vo.workflow.TaskDefinition;
import com.thortech.xl.vo.workflow.WorkflowDefinition;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.login.LoginException;
import oracle.iam.configservice.api.ConfigManager;
import oracle.iam.platform.OIMClient;
import project.rayedchan.custom.objects.ProcessFormField;
import project.rayedchan.services.OIMClientResourceAttr;
import project.rayedchan.services.OIMDatabaseConnection;
import project.rayedchan.utilities.HelperUtility;
import project.rayedchan.utilities.LookupUtility;
import project.rayedchan.utilities.ProcessFormUtility;
import project.rayedchan.utilities.ReconFieldMapToFormFieldUtility;

/**
 *
 * @author rayedchan
 * //TODO: close resources
 */
public class TestDriver 
{
    public static void main(String[] args) throws LoginException, tcAPIException, tcInvalidLookupException, tcDuplicateLookupCodeException, tcColumnNotFoundException, tcInvalidValueException, tcInvalidAttributeException, tcFormNotFoundException, tcFormFieldNotFoundException, tcDeleteNotAllowedException, tcAddFieldFailedException, tcProcessNotFoundException, SQLException, tcObjectNotFoundException
    { 
        OIMClient oimClient = new OIMClientResourceAttr().getOIMClient(); //Get OIMClient logging as an administrator
        Connection oimDBConnection = new OIMDatabaseConnection().getOracleDBConnction(); //Get connection to OIM Schema
        
        //OIM service objects
        tcLookupOperationsIntf lookupOps = oimClient.getService(tcLookupOperationsIntf.class);
        tcFormDefinitionOperationsIntf formDefOps = oimClient.getService(tcFormDefinitionOperationsIntf.class);
        tcObjectOperationsIntf resourceObjectOps = oimClient.getService(tcObjectOperationsIntf.class);
        
        /*
         * Test Oracle Database connection
         */
        /*String query = "SELECT PKG.PKG_KEY, TOS.TOS_KEY, SDK.SDK_KEY, PKG.PKG_NAME, SDK.SDK_NAME, OBJ.OBJ_KEY, OBJ.OBJ_NAME FROM "
         + "TOS RIGHT OUTER JOIN PKG ON PKG.PKG_KEY = TOS.PKG_KEY "
         + "LEFT OUTER JOIN SDK ON SDK.SDK_KEY = TOS.SDK_KEY "
         + "LEFT OUTER JOIN OBJ ON OBJ.OBJ_KEY = PKG.OBJ_KEY ORDER BY PKG.PKG_NAME";
        Statement statement = oimDBConnection.createStatement(); //Create a statement
        ResultSet resultSet = statement.executeQuery(query);
        HelperUtility.printResultSetRecords(resultSet);*/
            
        /*
         * Lookup Utility method calls
         */
        /*String lookupName =  "Lookup.Test";
        String newCodeKeyToAdd = "codeKeyTest1";
        String newDecodeToAdd = "decodeTest";
        String language = "en";
        String country = "US";
        String lookupField = "LKU_GROUP"; //Lookup Group Field
        String lookupUpdateFieldValue = "testLookupDescription";
        String codeKeyToRemove = "test1";
        String codeKeyToUpdate = "newCode2";
        String newCodeKey = "code";
        String newDecode = "decode";
        String fileNameAdd = "/home/oracle/Desktop/testAdd";
        String fileNameDelete = "/home/oracle/Desktop/testDelete";*/
        //LookupUtility.addLookup(lookupOps, lookupName);
        //LookupUtility.printLookupEntryColumns(lookupOps, lookupName);
        //LookupUtility.printLookupEntries(lookupOps, lookupName);
        //LookupUtility.addEntryToLookup(lookupOps, lookupName, newCodeKeyToAdd, newDecodeToAdd, language, country);
        //LookupUtility.updateLookup(lookupOps, lookupName, lookupField, lookupUpdateFieldValue);
        //LookupUtility.removeEntryFromLookup(lookupOps,lookupName, codeKeyToRemove); //remove the entry with the given code key           
        //LookupUtility.printLookupFileFormat(lookupOps,lookupName);
        //LookupUtility.updateEntryFromLookup(lookupOps,lookupName, codeKeyToUpdate, newCodeKey, newDecode);
        //LookupUtility.addEntriesToLookupDSFF(lookupOps,fileNameAdd);
        //LookupUtility.deleteEntriesFromLookupDSFF(lookupOps, fileNameDelete);


        /*
         * ProcessFormUtility method calls 
         */
        /*HashMap<String,String> updateFormFieldMap = new HashMap();
        updateFormFieldMap.put("Structure Utility.Additional Columns.Field Label", "test5");
        Long formFieldKey = 277L;
        long processFormKey = 47L;
        int processFormVersion = 2;
        String fieldName = "test1";
        String fieldType = "TextField";
        String variantType = "String";
        int length = 100;
        int order = 19;
        String defaultValue = null;
        String profileEnabled = null;
        boolean secure = false;*/
        //ProcessFormUtility.printProcessFormColumnNames(formDefOps);
        //ProcessFormUtility.printAllProcessFormInfo(formDefOps);
        //ProcessFormUtility.printProcessFormFieldColumnNames(formDefOps);
        //ProcessFormUtility.printProcessFormFields(formDefOps, 47L, 1);
        //ProcessFormUtility.printProcessFormFieldsFileFormatAdd(formDefOps, "UD_LDAP_USR");
        //ProcessFormUtility.addFieldsToProcessFormDSFF(formDefOps, "/home/oracle/Desktop/testPFFieldAdd");
        //ProcessFormUtility.removeFieldsFromProcessFormDSFF(formDefOps, "/home/oracle/Desktop/testPFFieldRemove");
        //formDefOps.updateFormField(formFieldKey, updateFormFieldMap);
        //ProcessFormField processFormFieldObj = new ProcessFormField (processFormKey, processFormVersion,  fieldName, fieldType, variantType, length, order, defaultValue, profileEnabled, secure);
        //ProcessFormUtility.addFieldToProcessForm(formDefOps, processFormFieldObj);
        

        
        /*
         * ReconFieldMapToFormFieldUtility
         */
        long packageKey = 45L;
        long processFormKeyRf = 47L;
        //HelperUtility.getAllProcessDefinitions(oimDBConnection);
        //tcResultSet result = formDefOps.getReconDataFlowForProcess(packageKey);
        //HelperUtility.printTcResultSetRecords(result);
        //ReconFieldMapToFormFieldUtility.printReconFieldAndFormFieldMappings(formDefOps, 45L);
        //ReconFieldMapToFormFieldUtility.printReconFieldAndFormFieldMappingsBySort(formDefOps, 45L, 1, 1);
        //ReconFieldMapToFormFieldUtility.getFormFields(formDefOps, processFormKeyRf);
        //ReconFieldMapToFormFieldUtility.getReconFields(resourceObjectOps, packageKey);
        //HelperUtility.printTcResultSetRecords(formDefOps.getFormFields(47L, 3));
        
        
        
            //int objKey = 45; //OBJ.obj_key from OIM schema
            //int objKeyDBAT = 61;
            //tcResultSet reconFieldsResultSet = resourceObjectOps.getReconciliationFields(objKeyDBAT);
            //printTcResultSetRecords(reconFieldsResultSet);
            
            //long processKey = 45L; //TOS_KEY =? OBJ_KEY
            //long processKeyDBAT = 61L;
            //String reconFieldKey = "162";
            //String processFormFieldName = "UD_LDAP_USR_TEST1";
            
             
            //tcFormDefinitionOperationsIntf formDefOps = oimClient.getService(tcFormDefinitionOperationsIntf.class);
            //formDefOps.addReconDataFlow(processKey, objKey, reconFieldKey, processFormFieldName, true, true);
           
            //PRF -Process Field and Reconcilation Fields Mapping
            //formDefOps.removeReconDataFlowMapping(processKey, objKey, reconFieldKey);
             
        
            //TOS - Info on all the Process Definitions
            //Process info process key [Process.Process Definition.Process Key], form key [Structure Utility.Key]
            //printTcResultSetRecords(resourceObjectOps.getProcessesForObject(45L));
             
            //tcWorkflowDefinitionOperationsIntf wfDefOps = oimClient.getService(tcWorkflowDefinitionOperationsIntf.class);
            //printTcResultSetRecords(wfDefOps.getAvailableAdapters()); //ADP - Adapters
            //printTcResultSetRecords(wfDefOps.getAdapterMappings("T", 31L)); //Info on the adapters, variable mappings, structure
            
            
            //WorkflowDefinition wfDefObj = wfDefOps.getWorkflowDefinition(45L); //Get the workflow of a resource object
            /*System.out.println(wfDefObj.getTaskList()); //Gets all the process tasks 
            TaskDefinition  taskDef = wfDefObj.getTask("Telephone Updated");
            System.out.println(taskDef.getTaskAdapter());
            System.out.println(taskDef.getTaskAdapterKey());
            System.out.println(taskDef.getTaskAdapterMappings());
            HashSet<AdapterMapping> mappings = taskDef.getTaskAdapterMappings();
            
            for(AdapterMapping attrMap : mappings)
            {
                System.out.println(attrMap.getAdapterVariableName());
            }*/
         
    }
}
