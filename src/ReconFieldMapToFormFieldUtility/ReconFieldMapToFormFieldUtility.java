package ReconFieldMapToFormFieldUtility;

import Thor.API.Exceptions.tcAPIException;
import Thor.API.Exceptions.tcColumnNotFoundException;
import Thor.API.Exceptions.tcProcessNotFoundException;
import Thor.API.Operations.tcFormDefinitionOperationsIntf;
import Thor.API.tcResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import javax.security.auth.login.LoginException;
import oracle.iam.platform.OIMClient;

/**
 *
 * @author rayedchan
 * A utility to map reconciliation fields to process form fields. A flat file 
 * may be used as a data source to define this mappings. This does not support 
 * multivalued fields at the moment.
 */
public class ReconFieldMapToFormFieldUtility 
{
    public static void main(String[] args) throws LoginException, tcAPIException, tcProcessNotFoundException, tcColumnNotFoundException
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
        
        tcFormDefinitionOperationsIntf formDefOps = oimClient.getService(tcFormDefinitionOperationsIntf.class);
        tcResultSet result = formDefOps.getReconDataFlowForProcess(45L);
        //printTcResultSetRecords(result);
        //printReconFieldAndFormFieldMappings(formDefOps, 45L);
        //printReconFieldAndFormFieldMappingsBySort(formDefOps, 45L, 1, 1);
    }
    
    /*
     * Print the current mappings of the reconciliation fields and process form
     * fields with sorting options.
     * @params 
     *      formDefOps - tcFormDefinitionOperationsIntf service object
     *      processKey - the resource process key (TOS.TOS_KEY)
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
            //System.out.printf("%-30s%-30s\n", reconField, formField);
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
     * @params 
     *      formDefOps - tcFormDefinitionOperationsIntf service object
     *      processKey - the resource process key (TOS.TOS_KEY)
     */
    public static void printReconFieldAndFormFieldMappings(tcFormDefinitionOperationsIntf formDefOps, Long processKey) throws tcAPIException, tcProcessNotFoundException, tcColumnNotFoundException
    {
        tcResultSet mappingResultSet = formDefOps.getReconDataFlowForProcess(processKey);
        int numRows = mappingResultSet.getTotalRowCount();
        
        System.out.printf("%-30s%-30s\n", "Reconciliation Field", "Form Field Column");
        for(int i = 0; i < numRows; i++)
        {
            mappingResultSet.goToRow(i);
            String reconField = mappingResultSet.getStringValue("Objects.Reconciliation Fields.Name");
            String formField = mappingResultSet.getStringValue("Process Definition.Reconciliation Fields Mappings.ColumnName");
            System.out.printf("%-30s%-30s\n", reconField, formField);
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

/*
 * A class to represent a mapping between a reconciliation field and process
 * form field.
 */
class ReconFieldAndFormFieldMap
{
    private String reconFieldName;
    private String formFieldColumnName;
    
    public ReconFieldAndFormFieldMap(String reconFieldName, String formFieldColumnName)
    {
        this.reconFieldName = reconFieldName;
        this.formFieldColumnName = formFieldColumnName;
    }
    
    public void setReconFieldName(String reconFieldName)
    {
        this.reconFieldName = reconFieldName;
    }
    
    public String getReconFieldName()
    {
        return this.reconFieldName;
    }
    
    public void setFormFieldColumnName(String formFieldColumnName)
    {
        this.formFieldColumnName = formFieldColumnName;
    }
    
    public String getFormFieldColumnName()
    {
        return this.formFieldColumnName;
    }
}

/*
 * For comparing the reconField of two ReconFieldAndFormFieldMap objects.
 */
class ReconFieldComparator implements Comparator<ReconFieldAndFormFieldMap> 
{
    @Override
    public int compare(ReconFieldAndFormFieldMap obj1, ReconFieldAndFormFieldMap obj2) 
    {
        return obj1.getReconFieldName().compareTo(obj2.getReconFieldName());
    }
}

/*
 * For comparing the formFieldColumnName of two ReconFieldAndFormFieldMap objects.
 */
class FormFieldColumnNameComparator implements Comparator<ReconFieldAndFormFieldMap> 
{
    @Override
    public int compare(ReconFieldAndFormFieldMap obj1, ReconFieldAndFormFieldMap obj2) 
    {
        return obj1.getFormFieldColumnName().compareTo(obj2.getFormFieldColumnName());
    }
}