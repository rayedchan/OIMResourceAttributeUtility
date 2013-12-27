package project.rayedchan.testdriver;

import Thor.API.Exceptions.tcAPIException;
import Thor.API.Exceptions.tcAddFieldFailedException;
import Thor.API.Exceptions.tcBulkException;
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
import Thor.API.Exceptions.tcUpdateNotAllowedException;
import Thor.API.Operations.tcExportOperationsIntf;
import Thor.API.Operations.tcFormDefinitionOperationsIntf;
import Thor.API.Operations.tcImportOperationsIntf;
import Thor.API.Operations.tcLookupOperationsIntf;
import Thor.API.Operations.tcObjectOperationsIntf;
import Thor.API.Operations.tcObjectOperationsIntfExtended;
import Thor.API.Operations.tcWorkflowDefinitionOperationsIntf;
import Thor.API.tcResultSet;
import com.thortech.xl.client.dataobj.tcORFClient;
import com.thortech.xl.ddm.exception.DDMException;
import com.thortech.xl.ddm.exception.TransformationException;
//import com.thortech.xl.dataobj.util.ReconHorizontalTableConfigUtils;
import com.thortech.xl.ejb.interfaces.tcORF;
import com.thortech.xl.ejb.interfaces.tcORFDelegate;
import com.thortech.xl.orb.dataaccess.tcDataSetData;
import com.thortech.xl.vo.ddm.RootObject;
import com.thortech.xl.vo.workflow.AdapterMapping;
import com.thortech.xl.vo.workflow.ResponseDefinition;
import com.thortech.xl.vo.workflow.StatusMapping;
import com.thortech.xl.vo.workflow.TaskAssignment;
import com.thortech.xl.vo.workflow.TaskDefinition;
import com.thortech.xl.vo.workflow.WorkflowDefinition;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.security.auth.login.LoginException;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import oracle.iam.configservice.api.ConfigManager;
import oracle.iam.platform.OIMClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import project.rayedchan.custom.objects.ProcessFormField;
import project.rayedchan.custom.objects.ReconFieldAndFormFieldMap;
import project.rayedchan.custom.objects.ReconciliationField;
import project.rayedchan.services.OIMClientResourceAttr;
import project.rayedchan.services.OIMDatabaseConnection;
import project.rayedchan.swing.gui.LoginFrame;
import project.rayedchan.utilities.HelperUtility;
import project.rayedchan.utilities.LookupUtility;
import project.rayedchan.utilities.ProcessFormFieldUtility;
import project.rayedchan.utilities.MappingReconFieldToFormFieldUtility;
import project.rayedchan.utilities.ProcessTaskUtility;
import project.rayedchan.utilities.ReconFieldUtility;

/**
 *
 * @author rayedchan
 * //TODO: close resources
 * TODO: ProcessFormUtility 
 *      make active form function
 *      validation method on deleting form field
 *
 */
public class TestDriver 
{
    public static void main(String[] args)
    {
         new LoginFrame();
    }
    
    //Handles the creation of the JFrame and all it's components
    private static void createGuiFrame()
    {
        JFrame guiFrame = new JFrame();
        
        //make sure the program exits when the frame closes
        guiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        guiFrame.setTitle("Dialog Box Example");
        guiFrame.setSize(500,300);
        
        //This will center the JFrame in the middle of the screen
        guiFrame.setLocationRelativeTo(null);
        guiFrame.setVisible(true);
        
        //Using a JPanel as the message for the JOptionPane
        JPanel userPanel = new JPanel();
        userPanel.setLayout(new GridLayout(2,2));

        //Labels for the textfield components        
        JLabel usernameLbl = new JLabel("User ID:");
        JLabel passwordLbl = new JLabel("Password:");

        //Input components
        JTextField usernameFld = new JTextField();
        JPasswordField passwordFld = new JPasswordField();

        //Add the components to the JPanel        
        userPanel.add(usernameLbl);
        userPanel.add(usernameFld);
        userPanel.add(passwordLbl);
        userPanel.add(passwordFld);

        //As the JOptionPane accepts an object as the message
        //it allows us to use any component we like - in this case 
        //a JPanel containing the dialog components we want
        String title = "Custom OIM Resource Console";
        int input = JOptionPane.showConfirmDialog(guiFrame, userPanel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            
        //User attempts to login
        if(input == JOptionPane.OK_OPTION)
        {
            //Get values from input field
            String username = usernameFld.getText();
            char[] password = passwordFld.getPassword(); 
            
            System.out.println(username);
            System.out.println(password);
            
            //Fill password array with zeroes
            Arrays.fill(password, '0');
        }
    }
    
    
    
    
    
    public static void others(String[] args) throws LoginException, tcAPIException, tcInvalidLookupException, tcDuplicateLookupCodeException, tcColumnNotFoundException, tcInvalidValueException, tcInvalidAttributeException, tcFormNotFoundException, tcFormFieldNotFoundException, tcDeleteNotAllowedException, tcAddFieldFailedException, tcProcessNotFoundException, SQLException, tcObjectNotFoundException, tcProcessFormException, IOException, NamingException, TransformerConfigurationException, TransformerException, DDMException, TransformationException, tcBulkException, tcUpdateNotAllowedException, ParserConfigurationException, XPathExpressionException, SAXException
    { 
        OIMClient oimClient = new OIMClientResourceAttr().getOIMClient(); //Get OIMClient logging as an administrator
        Connection oimDBConnection = new OIMDatabaseConnection().getOracleDBConnction(); //Get connection to OIM Schema
        
        //OIM service objects
        tcLookupOperationsIntf lookupOps = oimClient.getService(tcLookupOperationsIntf.class);
        tcFormDefinitionOperationsIntf formDefOps = oimClient.getService(tcFormDefinitionOperationsIntf.class);
        tcObjectOperationsIntf resourceObjectOps = oimClient.getService(tcObjectOperationsIntf.class);
        tcExportOperationsIntf exportOps = oimClient.getService(tcExportOperationsIntf.class);
        tcImportOperationsIntf importOps = oimClient.getService(tcImportOperationsIntf.class); 
        tcWorkflowDefinitionOperationsIntf wfDefOps = oimClient.getService(tcWorkflowDefinitionOperationsIntf.class);

        /*
         * ProcessTaskUtility
         */      
        //HelperUtility.printTcResultSetRecords(wfDefOps.getAvailableAdapters()); //ADP - Adapters
        //ProcessTaskUtility.printAvailiableAdapters(wfDefOps);
        //HelperUtility.printTcResultSetRecords(wfDefOps.getAdapterMappings("T", 31L)); //Info on the adapters, variable mappings, structure
        //HelperUtility.printTcResultSetRecords(ProcessTaskUtility.getAdapterVariableMappings(wfDefOps, "T", 31L));  
            
        WorkflowDefinition wfDefObj = wfDefOps.getWorkflowDefinition(45L); //Get the workflow of a resource object
        
        /*TaskDefinition newTaskDef = new TaskDefinition();
        newTaskDef.setName("Test Updated");
        newTaskDef.setDescription("Test Updated");
        newTaskDef.setConditionalTask(true);
        newTaskDef.setCancelWhilePending(true);
        newTaskDef.setRequiredComplete(true);
        newTaskDef.setAllowMultiple(true);
        newTaskDef.setTaskEffect("Field Change");
        
        ResponseDefinition newResponseDef = new ResponseDefinition();
        newResponseDef.setStatus("R");
        newResponseDef.setResponse("UNKNOWN");
        newResponseDef.setResponseDescription("An unknown response was received");
        HashMap<String, ResponseDefinition> responses = new HashMap<String, ResponseDefinition>();
        responses.put("UNKNOWN", newResponseDef);
        newTaskDef.setResponses(responses);
        //System.out.println(wfDefObj.getTaskList());
        //System.out.println(wfDefObj.getTasks());
        
        HashMap<String, StatusMapping> statusMapping = new HashMap<String,StatusMapping>();
        StatusMapping s1 = new StatusMapping();
        StatusMapping s2 = new StatusMapping();
        StatusMapping s3 = new StatusMapping();
        StatusMapping s4 = new StatusMapping();
        StatusMapping s5 = new StatusMapping();
        StatusMapping s6 = new StatusMapping();
        StatusMapping s7 = new StatusMapping();
        StatusMapping s8 = new StatusMapping();
        StatusMapping s9 = new StatusMapping();
        StatusMapping s10 = new StatusMapping();
        StatusMapping s11 = new StatusMapping();
        StatusMapping s12 = new StatusMapping();
        
        s1.setObjectStatus("None");
        s1.setTaskStatus("XLR");
        s1.setTaskStatusCategory("Rejected");
        
        s2.setObjectStatus("None");
        s2.setTaskStatus("W");
        s2.setTaskStatusCategory("Waiting");
        
        s3.setObjectStatus("None");
        s3.setTaskStatus("UT");
        s3.setTaskStatusCategory("Completed");
        
        s4.setObjectStatus("None");
        s4.setTaskStatus("P");
        s4.setTaskStatusCategory("Pending");
        
        s5.setObjectStatus("None");
        s5.setTaskStatus("UCR");
        s5.setTaskStatusCategory("Rejected");
        
        s6.setObjectStatus("None");
        s6.setTaskStatus("S");
        s6.setTaskStatusCategory("Suspended");
        
        s7.setObjectStatus("None");
        s7.setTaskStatus("R");
        s7.setTaskStatusCategory("Rejected");
        
        s8.setObjectStatus("None");
        s8.setTaskStatus("C");
        s8.setTaskStatusCategory("Completed");
        
        s9.setObjectStatus("None");
        s9.setTaskStatus("PX");
        s9.setTaskStatusCategory("Pending");
        
        s10.setObjectStatus("None");
        s10.setTaskStatus("MC");
        s10.setTaskStatusCategory("Completed");
        
        s11.setObjectStatus("None");
        s11.setTaskStatus("X");
        s11.setTaskStatusCategory("Cancelled");
        
        s12.setObjectStatus("None");
        s12.setTaskStatus("UC");
        s12.setTaskStatusCategory("Completed");
        
        statusMapping.put("XLR", s1);
        statusMapping.put("W", s2);
        statusMapping.put("UT", s3);
        statusMapping.put("P", s4);
        statusMapping.put("UCR", s5);
        statusMapping.put("S", s6);
        statusMapping.put("R", s7);
        statusMapping.put("C", s8);
        statusMapping.put("PX", s9);
        statusMapping.put("MC", s10);
        statusMapping.put("X", s11);
        statusMapping.put("UC", s12);

        newTaskDef.setStatusMappings(statusMapping);
        
        HashMap tasksAssign = new HashMap();
        TaskAssignment newTaskAssignment =  new TaskAssignment();
        newTaskAssignment.setRuleName("Default");
        newTaskAssignment.setTargetType("Group");
        newTaskAssignment.setGroupName("SYSTEM ADMINISTRATORS");
        newTaskAssignment.setPriority("1");
        tasksAssign.put(1, newTaskAssignment);
        newTaskDef.setTaskAssignments(tasksAssign);
        
        ArrayList taskList = wfDefObj.getTaskList();
        HashMap tasks = wfDefObj.getTasks();
        tasks.put("Test Updated", newTaskDef);
        taskList.add("Test Updated");
        wfDefObj.setTasks(tasks);
        wfDefObj.setTaskList(taskList);
        wfDefOps.updateWorkflow(wfDefObj);*/
        
        
        //System.out.println(wfDefObj.getTaskList()); //Gets all the process tasks 
        TaskDefinition  taskDef = wfDefObj.getTask("test1");
        //System.out.println(taskDef.getName());
        //printStringList(taskDef.getResponseList());
        //System.out.println(taskDef.getResponses());
        //ResponseDefinition responseDef = taskDef.getResponseDefinition("UNKNOWN");
        //System.out.println(responseDef.getResponseDescription());
        
        
        //System.out.println(taskDef.getStatusMappings());
        /*Iterator it = taskDef.getStatusMappings().entrySet().iterator();
        while(it.hasNext())
        {
             Map.Entry pairs = (Map.Entry) it.next();
             StatusMapping statusMap =  (StatusMapping) pairs.getValue();
             System.out.println(statusMap.getObjectStatusKey());
             System.out.println(statusMap.getTaskStatusKey());
             System.out.println(statusMap.getTaskStatus());
             System.out.println(statusMap.getTaskStatusCategory());
             System.out.println(statusMap.getObjectStatus());
             System.out.println();
        }*/
        
        //System.out.print(taskDef.getTaskAssignments());
        /*Iterator it = taskDef.getTaskAssignments().entrySet().iterator();
        while(it.hasNext())
        {
             Map.Entry pairs = (Map.Entry) it.next();
             TaskAssignment taskAssignment =  (TaskAssignment) pairs.getValue();
             System.out.println(taskAssignment.getRuleName());
             System.out.println(taskAssignment.getTargetType());
             System.out.println(taskAssignment.getGroupName());
              System.out.println(taskAssignment.getPriority());
              System.out.println();
             

        }*/
        
        taskDef.setDescription("hello");
        //System.out.println(taskDef.getName());
        HashMap tasks = wfDefObj.getTasks();
        System.out.println(tasks);
        tasks.put("test1", taskDef);
        wfDefObj.setTasks(tasks);
        wfDefOps.updateWorkflow(wfDefObj);
        
        
        //System.out.println(wfDefObj.getTask("Telephone Test"));
        
 
        //System.out.println(taskDef.getTaskAdapter());
        //System.out.println(taskDef.getTaskAdapterKey());
        //System.out.println(taskDef.getTaskAdapterMappings());
        
        /*
        HashSet<AdapterMapping> mappings = taskDef.getTaskAdapterMappings();    
        for(AdapterMapping attrMap : mappings)
        {
           System.out.println(attrMap.getAdapterVariableName());
        }
        */

       
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
        //ProcessFormFieldUtility.printProcessFormColumnNames(formDefOps);
        //ProcessFormFieldUtility.printAllProcessFormInfo(formDefOps);
        //ProcessFormFieldUtility.printProcessFormFieldColumnNames(formDefOps);
        //ProcessFormFieldUtility.printProcessFormFields(formDefOps, 47L, 1);
        //ProcessFormFieldUtility.printProcessFormFieldsFileFormatAdd(formDefOps, "UD_LDAP_USR");
        //ProcessFormFieldUtility.addFieldsToProcessFormDSFF(formDefOps, "/home/oracle/Desktop/testPFFieldAdd");
        //ProcessFormFieldUtility.removeFieldsFromProcessFormDSFF(formDefOps, "/home/oracle/Desktop/testPFFieldRemove");
        //formDefOps.updateFormField(formFieldKey, updateFormFieldMap);
        //ProcessFormField processFormFieldObj = new ProcessFormField (processFormKey, processFormVersion,  fieldName, fieldType, variantType, length, order, defaultValue, profileEnabled, secure);
        //ProcessFormFieldUtility.addFieldToProcessForm(formDefOps, processFormFieldObj);
        //ProcessFormFieldUtility.removeFormField(formDefOps, 162L);

        /*
         * ReconFieldMapToFormFieldUtility
         */
        /*long formKey = 47L;
        long objKey = 45L; //OBJ.obj_key from OIM schema            
        long processKey = 45L; //PKG_KEY
        String reconFieldKey = "181";
        String processFormFieldName = "UD_LDAP_USR_TEST2"; //need to test case sensitivity 
        Boolean isKeyField  = false;
        Boolean isCaseInsenstive = false;*/
        //ReconFieldAndFormFieldMap fieldMappings = new ReconFieldAndFormFieldMap(null,processFormFieldName,reconFieldKey,isKeyField,isCaseInsenstive); 
        //HelperUtility.getAllProcessDefinitions(oimDBConnection);
        //tcResultSet result = formDefOps.getReconDataFlowForProcess(processKey);
        //HelperUtility.printTcResultSetRecords(result);
        //MappingReconFieldToFormFieldUtility.printReconFieldAndFormFieldMappings(formDefOps, processKey);
        //MappingReconFieldToFormFieldUtility.printReconFieldAndFormFieldMappingsBySort(formDefOps, processKey, 1, 1);
        //MappingReconFieldToFormFieldUtility.getFormFields(formDefOps, formKey);
        /*MappingReconFieldToFormFieldUtility.getReconFields(resourceObjectOps, objKey);*/
        //HelperUtility.printTcResultSetRecords(formDefOps.getFormFields(47L, 3));
        //MappingReconFieldToFormFieldUtility.addReconFieldAndFormFieldMap(formDefOps, processKey, objKey, fieldMappings);
        //MappingReconFieldToFormFieldUtility.removeReconFieldAndFormFieldMap(formDefOps, processKey, objKey, reconFieldKey);
        //HelperUtility.printTcResultSetRecords(formDefOps.getObjects(45L));
        //MappingReconFieldToFormFieldUtility.printReconFieldAndFormFieldMappingsAddDSFF(oimDBConnection,formDefOps, processKey);
        //System.out.println(MappingReconFieldToFormFieldUtility.doesProcessExist(oimDBConnection, 100L));
        //System.out.println(MappingReconFieldToFormFieldUtility.doesObjectExist(oimDBConnection, 1L));
        //System.out.println(MappingReconFieldToFormFieldUtility.doesReconFieldExist(oimDBConnection, 45L, "Email"));
        //System.out.println(MappingReconFieldToFormFieldUtility.doesFormFieldExist(formDefOps, formKey, "UD_LDAP_USR_USERI"));
        //MappingReconFieldToFormFieldUtility.addReconFieldAndFormFieldMapDSFF(oimDBConnection, formDefOps, "/home/oracle/Desktop/testMapRfToPFF");
        //System.out.println(MappingReconFieldToFormFieldUtility.getFormKeyByObjAndProcKey(oimDBConnection, processKey, objKey));
        //System.out.println(MappingReconFieldToFormFieldUtility.doesPRFMappingExist(oimDBConnection, processKey, "User ID", "UD_LDAP_USR_USERID"));
        //System.out.println(Boolean.parseBoolean("false"));
        //MappingReconFieldToFormFieldUtility.removeReconFieldAndFormFieldMapDSFF(oimDBConnection, formDefOps, "/home/oracle/Desktop/testRemoveMapPRF");
     
        
        /*
         * ReconFieldUtility
         */   
        //String resourceObjectName = "LDAP User";
        //System.out.println(ReconFieldUtility.doesResourceObjectExist(oimDBConnection, resourceObjectName));
        //String resourceObjectXML = ReconFieldUtility.exportResourceObject(exportOps, resourceObjectName);
        //System.out.println(resourceObjectXML);
        //System.out.println(System.getProperty("file.encoding"));
        //ReconFieldUtility.printAllResourceObjects(oimDBConnection);
        //ReconFieldUtility.printReconFieldsofResourceObject(oimDBConnection, 45L);
        //ArrayList<ReconciliationField> reconFieldArray = new ArrayList<ReconciliationField>();
        //ReconFieldUtility.printReconFieldsofResourceObjectFileFormatAdd(oimDBConnection, 45L);
        //System.out.println(ReconFieldUtility.getResourceObjectName(oimDBConnection, 45L));
        //System.out.println(ReconFieldUtility.getResourceObjectKey(oimDBConnection, "DBAT_TEST_GTC"));
        //System.out.println(ReconFieldUtility.doesReconFieldNameExist(oimDBConnection, 45L, "User ID"));
        //System.out.println(ReconFieldUtility.isReconFieldMulitvalued(oimDBConnection, 45L, "Group Name"));
        //System.out.println(ReconFieldUtility.isReconFieldChildAttribute(oimDBConnection, 45L, "test6"));
 
        //ReconciliationField reconField = new ReconciliationField("test15", "String", false);
        //Document document = HelperUtility.parseStringXMLIntoDocument(resourceObjectXML);
        //ReconFieldUtility.addReconField(document, reconField);
        //ReconFieldUtility.removeReconField(document, "test7");
        //String newObjectResourceXML = HelperUtility.parseDocumentIntoStringXML(document);
        //ReconFieldUtility.addReconFieldsDSFF(oimDBConnection, exportOps, importOps, "/home/oracle/Desktop/testReconFieldAdd");
        //ReconFieldUtility.removeReconFieldDSFF(oimDBConnection, exportOps, importOps, "/home/oracle/Desktop/testReconFieldRemove");
            
        //System.out.println(newObjectResourceXML);
        //System.out.println(ReconFieldUtility.getResourceObjectUpdateTimestamp(document));
        //ReconFieldUtility.importResourceObject(importOps, newObjectResourceXML, "TestReconFieldRemove");
        //XPathExpression expr = xpath.compile("//ReconField"); //Get all tags with "ReconField" tag name regardless of depth
        //NodeList reconFieldNodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
        /*NodeList reconFieldNodes = (NodeList) xpath.evaluate("xl-ddm-data/Resource/ReconField", document, XPathConstants.NODESET); //do not get the mulitvalued reconField
          int numReconFieldNodes = reconFieldNodes.getLength();

          for(int i = 0; i < numReconFieldNodes; i++)
          {
              String reconFieldName = null;
              String reconFieldType = null;
              String isRequired = null;
              Boolean isMultiValued = false;
              Node reconFieldNode = reconFieldNodes.item(i);
              NodeList rfAttributeNodes = reconFieldNode.getChildNodes();
              Element rfElement = (Element) reconFieldNode;
              reconFieldName = rfElement.getAttribute("name");
              int numAttributes = rfAttributeNodes.getLength();
                
              //System.out.println(reconFieldName);
               
              //iterate all the reconciliation field attributes
              for(int j = 0; j < numAttributes; j++)
              {
                  Element attrElement = (Element) rfAttributeNodes.item(j);
                  String attributeName = attrElement.getTagName();
                        
                  if(attributeName.equalsIgnoreCase(ORF_FIELDTYPE_TAG))
                  {
                      reconFieldType = attrElement.getTextContent();
                      if(reconFieldType.equalsIgnoreCase("Multi-Valued"))
                      {
                          isMultiValued = true;
                          break;
                      }
                  }
                    
                  else if(attributeName.equalsIgnoreCase(ORF_REQUIRED_TAG))
                  {
                      isRequired = attrElement.getTextContent();
                  }
                   
              }
                
              if(isMultiValued == false)
              {
                  ReconciliationField reconField = new ReconciliationField(reconFieldName, reconFieldType, isRequired);
                  reconFieldArray.add(reconField);
              }                
          }*/
            
          //System.out.println(reconFieldArray);
    }
    
    
    public static void printStringList(String[] list)
    {
        for(String element: list)
        {
            System.out.println(element);
        }
    }
}
