package project.rayedchan.utilities;

import Thor.API.Exceptions.tcAPIException;
import Thor.API.Exceptions.tcColumnNotFoundException;
import Thor.API.Exceptions.tcProcessNotFoundException;
import Thor.API.Operations.tcFormDefinitionOperationsIntf;
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
}
