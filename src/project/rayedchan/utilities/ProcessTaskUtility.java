package project.rayedchan.utilities;

import Thor.API.Exceptions.tcAPIException;
import Thor.API.Exceptions.tcColumnNotFoundException;
import Thor.API.Operations.tcWorkflowDefinitionOperationsIntf;
import Thor.API.tcResultSet;

/**
 * @author rayedchan
 */
public class ProcessTaskUtility 
{
    /*
     * Prints all the adapters in OIM.
     * ADP_TYPE - P = Prepopulate, T = Task
     * ADP table contains info on the adapters 
     * EVT table contains info the event handler/method associated with an adapter
     *      Adapter Factory. Key (ADP_KEY)
     *      ADP_DESCRIPTION 
     *      Event Handler Manager.Key (EVT_KEY) - Event handler the adapter is associated with
     *      Event Handler Manager.Event Handler Name (EVT_NAME) - The event handler the adapter uses 
     * 
     * @param
     *      wfDefOps - tcWorkflowDefinitionOperationsIntf service     
     */
    public static void printAvailiableAdapters(tcWorkflowDefinitionOperationsIntf wfDefOps) throws tcAPIException, tcColumnNotFoundException
    {
        tcResultSet rs = wfDefOps.getAvailableAdapters();
        System.out.printf("%-20s%-50s%-20s%-30s\n", "Adapter Key", "Description", "Event Handler Key", "Event Handler Name");
        int numRows = rs.getTotalRowCount();
        
        for(int i = 0; i < numRows; i++)
        {
            rs.goToRow(i);
            String adapterKey = rs.getStringValue("Adapter Factory. Key");
            String description = rs.getStringValue("ADP_DESCRIPTION");
            String eventHandlerKey = rs.getStringValue("Event Handler Manager.Key");
            String eventHandlerName = rs.getStringValue("Event Handler Manager.Event Handler Name");
            System.out.printf("%-20s%-50s%-20s%-30s\n", adapterKey, description, eventHandlerKey, eventHandlerName);
        }  
    } 
    
    /*
     * Get all the adapter variable mappings of an adapter.
     * ADV table contains all the adapter variables
     * @params
     *      wfDefOps - tcWorkflowDefinitionOperationsIntf service  
     *      adapterType - "T" for Task Adapter and "A" for Task Assignment Adapter
     *      adapterKey - adapter key
     */
    public static tcResultSet getAdapterVariableMappings(tcWorkflowDefinitionOperationsIntf wfDefOps, String adapterType, long adapterKey) throws tcAPIException
    {
        return wfDefOps.getAdapterMappings(adapterType, adapterKey);
    }
}
