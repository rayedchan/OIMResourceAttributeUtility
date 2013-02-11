package project.rayedchan.utilities;

import Thor.API.Exceptions.tcAPIException;
import Thor.API.Exceptions.tcColumnNotFoundException;
import Thor.API.Exceptions.tcFormNotFoundException;
import Thor.API.Exceptions.tcObjectNotFoundException;
import Thor.API.Exceptions.tcProcessNotFoundException;
import Thor.API.Operations.tcFormDefinitionOperationsIntf;
import Thor.API.Operations.tcObjectOperationsIntf;
import Thor.API.tcResultSet;
import java.util.ArrayList;
import java.util.Collections;
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
    /*
     * Print the current mappings of the reconciliation fields and process form
     * fields with sorting options.
     * @params 
     *      formDefOps - tcFormDefinitionOperationsIntf service object
     *      processKey - the resource process key (PKG.PKG_KEY)
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
     * @params 
     *      formDefOps - tcFormDefinitionOperationsIntf service object
     *      processKey - the resource process key (PKG._PKG_KEY)
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
     *      processKey - the resource process key (PKG._PKG_KEY)
     */
    public static void getFormFields(tcFormDefinitionOperationsIntf formDefOps, long processKey) throws tcAPIException, tcFormNotFoundException, tcColumnNotFoundException
    {
        tcResultSet formVersionResultSet = formDefOps.getFormVersions(processKey); //get versions of current process form
        int numRows = formVersionResultSet.getTotalRowCount();
        Integer currentFormVersion = null;
        
        for(int i = 0; i < numRows; i++)
        {
            formVersionResultSet.goToRow(i);
            String formVersionStr = formVersionResultSet.getStringValue("SDL_CURRENT_VERSION");
            currentFormVersion = (formVersionStr == null ||  formVersionStr.isEmpty())? null : Integer.parseInt(formVersionStr);
        }
        
        //System.out.println(currentFormVersion);
        tcResultSet fieldResultSet = formDefOps.getFormFields(processKey, currentFormVersion);
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
}
