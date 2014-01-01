package project.rayedchan.utilities;

import Thor.API.Exceptions.tcAPIException;
import Thor.API.Exceptions.tcBulkException;
import Thor.API.Exceptions.tcColumnNotFoundException;
import Thor.API.Operations.tcExportOperationsIntf;
import Thor.API.Operations.tcImportOperationsIntf;
import Thor.API.Operations.tcWorkflowDefinitionOperationsIntf;
import Thor.API.tcResultSet;
import com.thortech.xl.ddm.exception.DDMException;
import com.thortech.xl.ddm.exception.TransformationException;
import com.thortech.xl.vo.ddm.RootObject;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import javax.naming.NamingException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author rayedchan
 * This utility modifying the process xml to add process tasks.
 * Process Task name are case sensitive.
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
     * @param   wfDefOps    tcWorkflowDefinitionOperationsIntf service     
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
     * @param  wfDefOps         tcWorkflowDefinitionOperationsIntf service  
     * @param  adapterType      "T" for Task Adapter and "A" for Task Assignment Adapter
     * @param  adapterKey       adapter key
     */
    public static tcResultSet getAdapterVariableMappings(tcWorkflowDefinitionOperationsIntf wfDefOps, String adapterType, long adapterKey) throws tcAPIException
    {
        return wfDefOps.getAdapterMappings(adapterType, adapterKey);
    }
      
    /*
     * Export process object XML
     * @param   exportOps           tcExportOperationsIntf service object
     * @param   procDefName         Name of the process definition
     * @return  the XML of the process as a String
     */
    public static String exportProcessObject(tcExportOperationsIntf exportOps, String procDefName) throws tcAPIException
    {
         String type = "Process";
         String description = null;
         Collection processObject = exportOps.findObjects(type, procDefName);
         int numObjects = processObject.size();
         
         //enforce one resource object to be exported at a time
         if(numObjects == 1)
         {
             String resourceObjectXML = exportOps.getExportXML(processObject, description);
             return resourceObjectXML;
         }
         
         System.out.println("Only one object can be exported at a time.");
         return null;
    }
      
    /*
     * Import process object XML into OIM
     * @param   importOps               tcImportOperationsIntf service object
     * @param   newProcessObjectXML     xml content to be imported
     * @param   fileName                File name of the file being imported. For tracking purposes.
     * @return  the XML of the resource as a String
     */
    public static void importResourceObject(tcImportOperationsIntf importOps, String newProcessObjectXML, String fileName) throws SQLException, NamingException, DDMException, tcAPIException, TransformationException, tcBulkException
    {     
        importOps.acquireLock(true);
        Collection<RootObject> justImported = importOps.addXMLFile(fileName, newProcessObjectXML);
        importOps.performImport(justImported);
    }
    
    /*
     * Creates the xml data for a new update process task
     * which will be added to the process xml metadata.
     * NOTE: MAV and RML keys are dynamically generated by OIM. (This method only
     * puts a label with no key).
     * @param   document    Contains the content of an exsting process
     */
    public static void createUpdateProcessTask(Document document) throws XPathExpressionException
    {
        String adapterName = "adpFFUpdateUser"; //Provided by user input (ADP_Name) case insensitive
        String processName = "Flat File"; //Provided by user case insensitive
        String eventHandlerName = "adpADPFFUPDATEUSER"; //Query EVT JOIN ADP
        String resourceObjectName = "FLATFILERESOURCE"; //PKG JOIN OBJ
        
        //Attributes values must provide in file
        String attributeName = "Gender"; //file
        String itResourceColumnName = "UD_FLAT_FIL_SERVER"; //file
        String objectType = "User"; //file
        
        //Validate these variables with API call
        String itResourceAdapterName = "itResourceFieldName"; //must provide in file
        String processInstanceKeyAdapterName = "processInstanceKey"; //long
        String objectTypeAdapterName = "objectType"; //must provide in file
        String adapterReturnValueAdapterName = "Adapter return value"; //object
        String attributeFieldNameAdapterName = "attrFieldName"; ////must provide in file
        
        String processTaskName = attributeName + " Updated";
        String tosId = "TOS81"; //TOS_KEY (PKG) with TOS prefix
        String mavId = "MAV";
        
        //Locate proper level to add the new process task into the xml
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();
        NodeList nodes =  (NodeList) xpath.evaluate("xl-ddm-data/Process/AtomicProcess", document, XPathConstants.NODESET);
        
        //Parent Container of process task
        Element newProcessTask = document.createElement("ProcessTask");      
        newProcessTask.setAttribute("repo-type", "RDBMS"); 
        newProcessTask.setAttribute("name", processTaskName); 
        
        //General Properties of a process task (MIL)
        Element milAppEffect = document.createElement("MIL_APP_EFFECT");
        milAppEffect.setTextContent("NONE");
       
        Element milConditional = document.createElement("MIL_CONDITIONAL");
        milConditional.setTextContent("1");
        
        Element milDescription = document.createElement("MIL_DESCRIPTION");
        milDescription.setTextContent(String.format("This task is triggered when %s attribute of parent form gets updated.", attributeName));
        
        Element milDisableManualInsert = document.createElement("MIL_DISABLE_MANUAL_INSERT");
        milDisableManualInsert.setTextContent("0");
        
        Element milSeq = document.createElement("MIL_SEQUENCE");
        milSeq.setTextContent("0");
        
        Element milConst = document.createElement("MIL_CONSTANT");
        milConst.setTextContent("0");
        
        Element milCancelWhilePending = document.createElement("MIL_CANCEL_WHILE_PENDING");
        milCancelWhilePending.setTextContent("1");
        
        Element milCompOnRec = document.createElement("MIL_COMP_ON_REC");
        milCompOnRec.setTextContent("0");
           
        Element milUpdate = document.createElement("MIL_UPDATE");
        milUpdate.setTextContent("1386887934000");
        
        Element milRequiredComplete = document.createElement("MIL_REQUIRED_COMPLETE");
        milRequiredComplete.setTextContent("0");
        
        Element milCreateMultiple = document.createElement("MIL_CREATE_MULTIPLE");
        milCreateMultiple.setTextContent("1");
        
        Element milOfflined = document.createElement("MIL_OFFLINED");
        milOfflined.setTextContent("0");
        
        //Attach MIL tags to parent container
        newProcessTask.appendChild(milAppEffect);
        newProcessTask.appendChild(milConditional);
        newProcessTask.appendChild(milDescription);
        newProcessTask.appendChild(milDisableManualInsert);
        newProcessTask.appendChild(milSeq);
        newProcessTask.appendChild(milConst);
        newProcessTask.appendChild(milCancelWhilePending);
        newProcessTask.appendChild(milCompOnRec);
        newProcessTask.appendChild(milUpdate);
        newProcessTask.appendChild(milRequiredComplete);
        newProcessTask.appendChild(milCreateMultiple);
        newProcessTask.appendChild(milOfflined);
              
        //Event Handler-Adapter on process task     
        Element evtKey = document.createElement("EVT_KEY");
        evtKey.setAttribute("EventHandler", eventHandlerName);
        newProcessTask.appendChild(evtKey);
        
        //Task Adapter Mappings
        String mavUpdateValue = "1386887607000";
        Element itResourceFieldName_adapVarMap = document.createElement("TaskAdapterMapping");
        itResourceFieldName_adapVarMap.setAttribute("repo-type", "RDBMS");
        itResourceFieldName_adapVarMap.setAttribute("id", mavId);
        Element mavMapQualifer1  = document.createElement("MAV_MAP_QUALIFIER");
        mavMapQualifer1.setTextContent("String");     
        Element mavUpdate1  = document.createElement("MAV_UPDATE");
        mavUpdate1.setTextContent(mavUpdateValue);
        Element mavMapValue1  = document.createElement("MAV_MAP_VALUE");
        mavMapValue1.setTextContent(itResourceColumnName); //Dynamic
        Element mavMapTo1  = document.createElement("MAV_MAP_TO");
        mavMapTo1.setTextContent("Literal");
        Element mavMapOldValue1  = document.createElement("MAV_MAP_OLD_VALUE");
        mavMapOldValue1.setTextContent("0");
        Element advKey1 = document.createElement("ADV_KEY");
        advKey1.setAttribute("Adapter", adapterName);
        advKey1.setAttribute("AdapterVariable", itResourceAdapterName);
        advKey1.setAttribute("EventHandler", eventHandlerName);
        itResourceFieldName_adapVarMap.appendChild(mavMapQualifer1);     
        itResourceFieldName_adapVarMap.appendChild(mavUpdate1);
        itResourceFieldName_adapVarMap.appendChild(mavMapValue1);
        itResourceFieldName_adapVarMap.appendChild(mavMapTo1);
        itResourceFieldName_adapVarMap.appendChild(mavMapOldValue1);
        itResourceFieldName_adapVarMap.appendChild(advKey1);
        newProcessTask.appendChild(itResourceFieldName_adapVarMap);
        
              
        Element processIntsanceKey_adapVarMap = document.createElement("TaskAdapterMapping");
        processIntsanceKey_adapVarMap.setAttribute("repo-type", "RDBMS");
        processIntsanceKey_adapVarMap.setAttribute("id", mavId);
                
        Element mavFieldLength2  = document.createElement("MAV_FIELD_LENGTH");
        mavFieldLength2.setTextContent("19");  
        
        Element mavMapQualifer2 = document.createElement("MAV_MAP_QUALIFIER");
        mavMapQualifer2.setTextContent("Process Instance");   
        
        Element mavUpdate2  = document.createElement("MAV_UPDATE");
        mavUpdate2.setTextContent(mavUpdateValue);
        
        Element mavMapValue2  = document.createElement("MAV_MAP_VALUE");
        mavMapValue2.setTextContent("orc_key"); 
        
        Element mavMapTo2  = document.createElement("MAV_MAP_TO");
        mavMapTo2.setTextContent("Process Data");
        
        Element mavMapOldValue2  = document.createElement("MAV_MAP_OLD_VALUE");
        mavMapOldValue2.setTextContent("0");
        
        Element advKey2 = document.createElement("ADV_KEY");
        advKey2.setAttribute("Adapter", adapterName);
        advKey2.setAttribute("AdapterVariable", processInstanceKeyAdapterName);
        advKey2.setAttribute("EventHandler", eventHandlerName);
        
        processIntsanceKey_adapVarMap.appendChild(mavFieldLength2);  
        processIntsanceKey_adapVarMap.appendChild(mavMapQualifer2);     
        processIntsanceKey_adapVarMap.appendChild(mavUpdate2);
        processIntsanceKey_adapVarMap.appendChild(mavMapValue2);
        processIntsanceKey_adapVarMap.appendChild(mavMapTo2);
        processIntsanceKey_adapVarMap.appendChild(mavMapOldValue2);
        processIntsanceKey_adapVarMap.appendChild(advKey2);
        newProcessTask.appendChild(processIntsanceKey_adapVarMap);
        
   
        Element objectType_adapVarMap = document.createElement("TaskAdapterMapping");
        objectType_adapVarMap.setAttribute("repo-type", "RDBMS");
        objectType_adapVarMap.setAttribute("id", mavId);
                
        Element mavMapQualifer3 = document.createElement("MAV_MAP_QUALIFIER");
        mavMapQualifer3.setTextContent("String");   
        
        Element mavUpdate3  = document.createElement("MAV_UPDATE");
        mavUpdate3.setTextContent(mavUpdateValue);
        
        Element mavMapValue3  = document.createElement("MAV_MAP_VALUE");
        mavMapValue3.setTextContent(objectType); 
        
        Element mavMapTo3  = document.createElement("MAV_MAP_TO");
        mavMapTo3.setTextContent("Literal");
        
        Element mavMapOldValue3  = document.createElement("MAV_MAP_OLD_VALUE");
        mavMapOldValue3.setTextContent("0");
        
        Element advKey3 = document.createElement("ADV_KEY");
        advKey3.setAttribute("Adapter", adapterName);
        advKey3.setAttribute("AdapterVariable", objectTypeAdapterName);
        advKey3.setAttribute("EventHandler", eventHandlerName);
        
        objectType_adapVarMap.appendChild(mavMapQualifer3);     
        objectType_adapVarMap.appendChild(mavUpdate3);
        objectType_adapVarMap.appendChild(mavMapValue3);
        objectType_adapVarMap.appendChild(mavMapTo3);
        objectType_adapVarMap.appendChild(mavMapOldValue3);
        objectType_adapVarMap.appendChild(advKey3);
        newProcessTask.appendChild(objectType_adapVarMap);
        
      
        Element adapterReturnValue_adapVarMap = document.createElement("TaskAdapterMapping");
        adapterReturnValue_adapVarMap.setAttribute("repo-type", "RDBMS");
        adapterReturnValue_adapVarMap.setAttribute("id", mavId);
                
        Element mavFieldLength4 = document.createElement("MAV_FIELD_LENGTH");
        mavFieldLength4.setTextContent("0");   
        
        Element mavUpdate4  = document.createElement("MAV_UPDATE");
        mavUpdate4.setTextContent(mavUpdateValue);
        
        Element mavMapTo4  = document.createElement("MAV_MAP_TO");
        mavMapTo4.setTextContent("Response Code");
        
        Element mavMapOldValue4  = document.createElement("MAV_MAP_OLD_VALUE");
        mavMapOldValue4.setTextContent("0");
        
        Element advKey4 = document.createElement("ADV_KEY");
        advKey4.setAttribute("Adapter", adapterName);
        advKey4.setAttribute("AdapterVariable", adapterReturnValueAdapterName);
        advKey4.setAttribute("EventHandler", eventHandlerName);
         
        adapterReturnValue_adapVarMap.appendChild(mavFieldLength4);
        adapterReturnValue_adapVarMap.appendChild(mavUpdate4);
        adapterReturnValue_adapVarMap.appendChild(mavMapTo4);
        adapterReturnValue_adapVarMap.appendChild(mavMapOldValue4);
        adapterReturnValue_adapVarMap.appendChild(advKey4);
        newProcessTask.appendChild(adapterReturnValue_adapVarMap);
        
            
        Element attrFieldName_adapVarMap = document.createElement("TaskAdapterMapping");
        attrFieldName_adapVarMap.setAttribute("repo-type", "RDBMS");
        attrFieldName_adapVarMap.setAttribute("id", mavId);
                
        Element mavMapQualifier5 = document.createElement("MAV_MAP_QUALIFIER");
        mavMapQualifier5.setTextContent("String");   
        
        Element mavUpdate5  = document.createElement("MAV_UPDATE");
        mavUpdate5.setTextContent(mavUpdateValue);
        
        Element mavMapValue5  = document.createElement("MAV_MAP_VALUE");
        mavMapValue5.setTextContent(attributeName);
        
        Element mavMapTo5  = document.createElement("MAV_MAP_TO");
        mavMapTo5.setTextContent("Literal");
        
        Element mavMapOldValue5  = document.createElement("MAV_MAP_OLD_VALUE");
        mavMapOldValue5.setTextContent("0");
        
        Element advKey5 = document.createElement("ADV_KEY");
        advKey5.setAttribute("Adapter", adapterName);
        advKey5.setAttribute("AdapterVariable", attributeFieldNameAdapterName);
        advKey5.setAttribute("EventHandler", eventHandlerName);
         
        attrFieldName_adapVarMap.appendChild(mavMapQualifier5);
        attrFieldName_adapVarMap.appendChild(mavUpdate5);
        attrFieldName_adapVarMap.appendChild(mavMapValue5);
        attrFieldName_adapVarMap.appendChild(mavMapTo5);
        attrFieldName_adapVarMap.appendChild(mavMapOldValue5);
        attrFieldName_adapVarMap.appendChild(advKey5);
        newProcessTask.appendChild(attrFieldName_adapVarMap);
        
        //Task Status Permission
        //One to One Mapping between sta and upg 
        String msgUpdateValue = "1386887116000";
        //String atomicValueProcessValue = "TOS81";
        String[] sta = {"UC","X","C","W","C",
                        "R","X","P","S","PX",
                        "PX","UC","S","P","W",
                        "R"};
        String[] upg = {"ALL USERS","SYSTEM ADMINISTRATORS","ALL USERS","SYSTEM ADMINISTRATORS","SYSTEM ADMINISTRATORS",
                        "SYSTEM ADMINISTRATORS","ALL USERS","ALL USERS","SYSTEM ADMINISTRATORS","SYSTEM ADMINISTRATORS",
                        "ALL USERS","SYSTEM ADMINISTRATORS","ALL USERS","SYSTEM ADMINISTRATORS","ALL USERS",
                        "ALL USERS"};
        int numStatusMappings = sta.length;
        for(int i = 0; i < numStatusMappings; i++)
        {
            //Parent
            Element taskStatusPermission = document.createElement("TaskStatusPermission");
            taskStatusPermission.setAttribute("repo-type", "RDBMS");
            
            //Child
            Element msgUpdate = document.createElement("MSG_UPDATE");
            Element tosKey = document.createElement("TOS_KEY");
            Element staKey = document.createElement("STA_KEY");
            Element upgKey = document.createElement("UGP_KEY");
            
            msgUpdate.setTextContent(msgUpdateValue);
            tosKey.setAttribute("Process", processName);
            tosKey.setAttribute("AtomicProcess", tosId);
            staKey.setAttribute("Status", sta[i]);
            upgKey.setAttribute("UserGroup", upg[i]);
            
            //Append child to parent
            taskStatusPermission.appendChild(msgUpdate);
            taskStatusPermission.appendChild(tosKey);
            taskStatusPermission.appendChild(staKey);
            taskStatusPermission.appendChild(upgKey);
            
            //Append parent to process tag
            newProcessTask.appendChild(taskStatusPermission);
        }
        
        //Task To Object Status Mapping
        String[] tTOSM_sta = {"S","W","X","UT","P",
                              "C","R","PX","UCR","UC",
                              "XLR","MC"}; 
        String mstUpdateValue = "1386887115000";
        String objectStatusValue = "None";
        int numTTOSM = tTOSM_sta.length;
        for(int i = 0; i < numTTOSM; i++)
        {
            //Parent container       
            Element taskToObjectStatusMapping = document.createElement("TaskToObjectStatusMapping"); 
            taskToObjectStatusMapping.setAttribute("repo-type", "RDBMS");
            
            //Child
            Element mstUpdate = document.createElement("MST_UPDATE");
            Element staKey = document.createElement("STA_KEY");
            Element ostKey = document.createElement("OST_KEY");
                     
            mstUpdate.setTextContent(mstUpdateValue);
            staKey.setAttribute("Status", tTOSM_sta[i]);
            ostKey.setAttribute("Resource", resourceObjectName);
            ostKey.setAttribute("ObjectStatus", objectStatusValue);
            
            //Append child to parent
            taskToObjectStatusMapping.appendChild(mstUpdate);
            taskToObjectStatusMapping.appendChild(staKey);
            taskToObjectStatusMapping.appendChild(ostKey);
            
            //Append parent to process tag
            newProcessTask.appendChild(taskToObjectStatusMapping);
        }
        

        //Task Responses
        String[] responses = {"ERROR", "UNKNOWN", "SUCCESS"};
        String[] description = {"Error Occurred", "An unknown response was received", "Operation Completed"};
        String[] status = {"R", "R", "C"};
        String rscUpdateValue = "1386887934000";
        int numTaskResponses = responses.length;
        
        for(int i = 0; i < numTaskResponses; i++)
        {
            //Parent
            Element taskResponse = document.createElement("TaskResponse");
            taskResponse.setAttribute("repo-type", "RDBMS");
            taskResponse.setAttribute("name", responses[i]);
            
            //Child
            Element rscDesc = document.createElement("RSC_DESC");
            Element rscUpdate = document.createElement("RSC_UPDATE");
            Element staKey = document.createElement("STA_KEY");
            
            rscDesc.setTextContent(description[i]);
            rscUpdate.setTextContent(rscUpdateValue);
            staKey.setAttribute("Status", status[i]);
            
            //Append child to parent
            taskResponse.appendChild(rscDesc);
            taskResponse.appendChild(rscUpdate);
            taskResponse.appendChild(staKey);
            
            //Append parent to process tag
            newProcessTask.appendChild(taskResponse);
        }
        
        //Task Assignment Rule
        String ruleId = "RML";
        Element taskAssignmentRule = document.createElement("TaskAssignmentRule");
        taskAssignmentRule.setAttribute("repo-type", "RDBMS");
        taskAssignmentRule.setAttribute("id", ruleId);
        
        Element rmlUpdate = document.createElement("RML_UPDATE");
        Element rmlTargetType = document.createElement("RML_TARGET_TYPE");
        Element rmlPriority = document.createElement("RML_PRIORITY");
        Element rulKey = document.createElement("RUL_KEY");
        Element ugpKey = document.createElement("UGP_KEY");
        
        rmlUpdate.setTextContent("1386887115000");
        rmlTargetType.setTextContent("Group");
        rmlPriority.setTextContent("1");
        rulKey.setAttribute("Rule", "Default");
        ugpKey.setAttribute("UserGroup", "SYSTEM ADMINISTRATORS");
        
        taskAssignmentRule.appendChild(rmlUpdate);
        taskAssignmentRule.appendChild(rmlTargetType);
        taskAssignmentRule.appendChild(rmlPriority);
        taskAssignmentRule.appendChild(rulKey);
        taskAssignmentRule.appendChild(ugpKey);
        
        newProcessTask.appendChild(taskAssignmentRule);
   
        //Get the  node tag and insert reconField within the resource tag
        Node processNode = nodes.item(0); 
        processNode.appendChild(newProcessTask); 
        //document.appendChild(newProcessTask);
    }
}
