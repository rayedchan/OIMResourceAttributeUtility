/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Test;

import Thor.API.Exceptions.tcAPIException;
import Thor.API.Exceptions.tcAddFieldFailedException;
import Thor.API.Exceptions.tcColumnNotFoundException;
import Thor.API.Exceptions.tcFormNotFoundException;
import Thor.API.Exceptions.tcInvalidAttributeException;
import Thor.API.Exceptions.tcObjectNotFoundException;
import Thor.API.Exceptions.tcProcessFormException;
import Thor.API.Exceptions.tcProcessNotFoundException;
import Thor.API.Operations.tcFormDefinitionOperationsIntf;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import javax.security.auth.login.LoginException;
import oracle.iam.configservice.api.ConfigManager;
import oracle.iam.platform.OIMClient;

/**
 *
 * @author oracle
 */
public class Test 
{
    public static void main(String[] args) throws LoginException, tcAPIException, tcObjectNotFoundException, tcColumnNotFoundException, tcFormNotFoundException, tcInvalidAttributeException, tcAddFieldFailedException, tcProcessNotFoundException, tcProcessFormException
    {
        String ctxFactory = "weblogic.jndi.WLInitialContextFactory"; //WebLogic Context 
        String oimServerURL = "t3://localhost:14000"; //OIM URL
        String authwlConfigPath = "/home/oracle/oimClient_lib/conf/authwl.conf"; //Path to login configuration
        String username = "xelsysadm"; //OIM Administrator 
        String password = "Password1"; //Administrator Password
       
        System.setProperty("java.security.auth.login.config", authwlConfigPath); //set the login configuration
        Hashtable<String,String> env = new Hashtable<String,String>(); //use to store OIM environment properties
        env.put(OIMClient.JAVA_NAMING_FACTORY_INITIAL, ctxFactory);
        env.put(OIMClient.JAVA_NAMING_PROVIDER_URL, oimServerURL);
        OIMClient oimClient = new OIMClient(env);
        oimClient.login(username, password.toCharArray()); //login to OIM
        
        int objKey = 45; //OBJ.obj_key from OIM schema
        int objKeyDBAT = 61;
        tcObjectOperationsIntf resourceObjectOps = oimClient.getService(tcObjectOperationsIntf.class);
        tcResultSet reconFieldsResultSet = resourceObjectOps.getReconciliationFields(objKeyDBAT);
        //printTcResultSetRecords(reconFieldsResultSet);
        
        long processKey = 45L; //TOS_KEY =? OBJ_KEY
        long processKeyDBAT = 61L;
        String reconFieldKey = "162";
        String processFormFieldName = "UD_LDAP_USR_TEST1";
        
         
        //tcFormDefinitionOperationsIntf formDefOps = oimClient.getService(tcFormDefinitionOperationsIntf.class);
        //formDefOps.addReconDataFlow(processKey, objKey, reconFieldKey, processFormFieldName, true, true);
       
        //PRF -Process Field and Reconcilation Fields Mapping
        //formDefOps.removeReconDataFlowMapping(processKey, objKey, reconFieldKey);
         
        //TOS - Info on all the Process Definitions
        //Process info process key [Process.Process Definition.Process Key], form key [Structure Utility.Key]
        //printTcResultSetRecords(resourceObjectOps.getProcessesForObject(45L));
         
        tcWorkflowDefinitionOperationsIntf wfDefOps = oimClient.getService(tcWorkflowDefinitionOperationsIntf.class);
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
        
        
        
        /*
         * Table - ORF 
         * Columns for a Reconciliation Field
         * 
         * Objects.Key
         * Objects.Reconciliation Fields.Key
         * Objects.Reconciliation Fields.Name
         * ORF_FIELDTYPE
         * ORF_REQUIRED
         * PARENTFIELD
         * Objects.Reconciliation Fields.Row Version
         */
        
       /* for(String columnName : columnNames)
        {
            System.out.println(columnName);
        }
        
        for(int i = 0; i < reconFieldsResultSet.getTotalRowCount(); i++)
        {
            reconFieldsResultSet.goToRow(i);
            System.out.println(reconFieldsResultSet.getStringValue("Objects.Key"));
            System.out.println(reconFieldsResultSet.getStringValue("Objects.Reconciliation Fields.Key"));
            System.out.println(reconFieldsResultSet.getStringValue("Objects.Reconciliation Fields.Name"));
            System.out.println(reconFieldsResultSet.getStringValue("ORF_FIELDTYPE"));
            System.out.println(reconFieldsResultSet.getStringValue("ORF_REQUIRED"));
            System.out.println(reconFieldsResultSet.getStringValue("PARENTFIELD"));
            System.out.println(reconFieldsResultSet.getStringValue("Objects.Reconciliation Fields.Row Version"));
            System.out.println("==================================================");
        }*/
         

        
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
