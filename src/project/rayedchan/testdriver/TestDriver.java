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
import Thor.API.Operations.TaskDefinitionOperationsIntf;
import Thor.API.Operations.tcExportOperationsIntf;
import Thor.API.Operations.tcFormDefinitionOperationsIntf;
import Thor.API.Operations.tcImportOperationsIntf;
import Thor.API.Operations.tcLookupOperationsIntf;
import Thor.API.Operations.tcObjectOperationsIntf;
import Thor.API.Operations.tcObjectOperationsIntfExtended;
import Thor.API.Operations.tcWorkflowDefinitionOperationsIntf;
import Thor.API.tcResultSet;
import com.thortech.xl.client.dataobj.tcORFClient;
import com.thortech.xl.dataaccess.tcDataProvider;
import com.thortech.xl.dataaccess.tcDataSetException;
import com.thortech.xl.ddm.exception.DDMException;
import com.thortech.xl.ddm.exception.TransformationException;
//import com.thortech.xl.dataobj.util.ReconHorizontalTableConfigUtils;
import com.thortech.xl.ejb.interfaces.tcORF;
import com.thortech.xl.ejb.interfaces.tcORFDelegate;
import com.thortech.xl.orb.dataaccess.tcDataAccessException;
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
import javax.swing.SwingUtilities;
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
import project.rayedchan.exception.NoResourceObjForProcessDefException;
import project.rayedchan.exception.ProcessDefintionNotFoundException;
import project.rayedchan.exception.ProcessFormNotFoundException;
import project.rayedchan.exception.ResourceObjectNameNotFoundException;
import project.rayedchan.services.OIMClientResourceAttr;
import project.rayedchan.services.OIMDatabaseConnection;
import project.rayedchan.services.tcOIMDatabaseConnection;
import project.rayedchan.swing.gui.LoginJFrame;
import project.rayedchan.utilities.HelperUtility;
import project.rayedchan.utilities.LookupUtility;
import project.rayedchan.utilities.ProcessFormFieldUtility;
import project.rayedchan.utilities.MappingReconFieldToFormFieldUtility;
import project.rayedchan.utilities.ProcessTaskUtility;
import project.rayedchan.utilities.ReconFieldUtility;

/**
 * @author rayedchan
 * Starts the GUI for this application.
 */
public class TestDriver 
{
    public static void main2(String[] args)
    {
         /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(LoginJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(LoginJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(LoginJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(LoginJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() 
        {
            public void run() 
            {
                new LoginJFrame().setVisible(true);
            }
        });
    }
    
    public static void main(String[] args) throws LoginException, tcAPIException, tcInvalidLookupException, tcDuplicateLookupCodeException, tcColumnNotFoundException, tcInvalidValueException, tcInvalidAttributeException, tcFormNotFoundException, tcFormFieldNotFoundException, tcDeleteNotAllowedException, tcAddFieldFailedException, tcProcessNotFoundException, SQLException, tcObjectNotFoundException, tcProcessFormException, IOException, NamingException, TransformerConfigurationException, TransformerException, DDMException, TransformationException, tcBulkException, tcUpdateNotAllowedException, ParserConfigurationException, XPathExpressionException, SAXException, tcDataSetException, tcDataAccessException, ResourceObjectNameNotFoundException, ProcessDefintionNotFoundException, NoResourceObjForProcessDefException, ProcessFormNotFoundException
    { 
        OIMClient oimClient = new OIMClientResourceAttr().getOIMClient(); //Get OIMClient logging as an administrator
        tcOIMDatabaseConnection connection =  new tcOIMDatabaseConnection(oimClient);
        tcDataProvider dbProvider = connection.getDbProvider();
        
        //OIM service objects
        tcLookupOperationsIntf lookupOps = oimClient.getService(tcLookupOperationsIntf.class);
        tcFormDefinitionOperationsIntf formDefOps = oimClient.getService(tcFormDefinitionOperationsIntf.class);
        tcObjectOperationsIntf resourceObjectOps = oimClient.getService(tcObjectOperationsIntf.class);
        tcExportOperationsIntf exportOps = oimClient.getService(tcExportOperationsIntf.class);
        tcImportOperationsIntf importOps = oimClient.getService(tcImportOperationsIntf.class);
        TaskDefinitionOperationsIntf taskOps = oimClient.getService(TaskDefinitionOperationsIntf.class);
        tcWorkflowDefinitionOperationsIntf wfDefOps =  oimClient.getService(tcWorkflowDefinitionOperationsIntf.class);
               
        //System.out.println(ProcessTaskUtility.exportProcessObject(exportOps, "Flat File"));
        //HelperUtility.printTcResultSetRecords(wfDefOps.getAvailableAdapters());
        //HelperUtility.printTcResultSetRecords(ProcessTaskUtility.getAdapterVariableMappings(wfDefOps, "T", 65));
        	
        //DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        //DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	//Document document = docBuilder.newDocument();
        
        String processObjectXML = ProcessTaskUtility.exportProcessObject(exportOps, "Flat File");
        Document document =  HelperUtility.parseStringXMLIntoDocument(processObjectXML); //convert xml to a Document
        ProcessTaskUtility.createUpdateProcessTask(document);
        String newProcessObjectXML = HelperUtility.parseDocumentIntoStringXML(document);
        System.out.println(newProcessObjectXML);
        ProcessTaskUtility.importResourceObject(importOps, newProcessObjectXML, "CustomProcessTaskUtilAdd");
        
    }
}


/*
 * <ProcessTask repo-type="RDBMS" name="FirstName Updated">
 *      <MIL_APP_EFFECT>NONE</MIL_APP_EFFECT>
 *      <MIL_CONDITIONAL>1</MIL_CONDITIONAL>
 *      <MIL_DESCRIPTION>This task is triggered when FirstName attribute of parent form gets updated.</MIL_DESCRIPTION>
 *      <MIL_DISABLE_MANUAL_INSERT>0</MIL_DISABLE_MANUAL_INSERT>
 *      <MIL_SEQUENCE>0</MIL_SEQUENCE>
 *      <MIL_CONSTANT>0</MIL_CONSTANT>
 *      <MIL_CANCEL_WHILE_PENDING>1</MIL_CANCEL_WHILE_PENDING>
 *      <MIL_COMP_ON_REC>0</MIL_COMP_ON_REC>
 *      <MIL_UPDATE>1386887934000</MIL_UPDATE>
 *      <MIL_REQUIRED_COMPLETE>0</MIL_REQUIRED_COMPLETE>
 *      <MIL_CREATE_MULTIPLE>1</MIL_CREATE_MULTIPLE>
 *      <MIL_OFFLINED>0</MIL_OFFLINED>
 *      
 *      <EVT_KEY EventHandler="adpADPFFUPDATEUSER"/>
 *          <TaskToObjectStatusMapping repo-type="RDBMS">
 *              <MST_UPDATE>1386887115000</MST_UPDATE>
 *              <STA_KEY Status="W"/>
 *              <OST_KEY Resource="FLATFILERESOURCE" ObjectStatus="None"/>
 *          </TaskToObjectStatusMapping>
 *          <TaskToObjectStatusMapping repo-type="RDBMS">
 *              <MST_UPDATE>1386887115000</MST_UPDATE>
 *              <STA_KEY Status="S"/>
 *              <OST_KEY Resource="FLATFILERESOURCE" ObjectStatus="None"/>
 *          </TaskToObjectStatusMapping>
 *          <TaskToObjectStatusMapping repo-type="RDBMS">
 *              <MST_UPDATE>1386887115000</MST_UPDATE>
 *              <STA_KEY Status="X"/>
 *              <OST_KEY Resource="FLATFILERESOURCE" ObjectStatus="None"/>
 *          </TaskToObjectStatusMapping>
 *          <TaskStatusPermission repo-type="RDBMS">
 *              <MSG_UPDATE>1386887116000</MSG_UPDATE>
 *              <TOS_KEY Process="Flat File" AtomicProcess="TOS81"/>
 *              <STA_KEY Status="S"/>
 *              <UGP_KEY UserGroup="SYSTEM ADMINISTRATORS"/>
 *          </TaskStatusPermission>
 *          <TaskToObjectStatusMapping repo-type="RDBMS">
 *              <MST_UPDATE>1386887115000</MST_UPDATE>
 *              <STA_KEY Status="C"/>
 *              <OST_KEY Resource="FLATFILERESOURCE" ObjectStatus="None"/>
 *          </TaskToObjectStatusMapping>
 *          <TaskStatusPermission repo-type="RDBMS">
 *              <MSG_UPDATE>1386887116000</MSG_UPDATE>
 *              <TOS_KEY Process="Flat File" AtomicProcess="TOS81"/>
 *              <STA_KEY Status="W"/>
 *              <UGP_KEY UserGroup="ALL USERS"/>
 *          </TaskStatusPermission>
 *          <TaskStatusPermission repo-type="RDBMS">
 *              <MSG_UPDATE>1386887116000</MSG_UPDATE>
 *              <TOS_KEY Process="Flat File" AtomicProcess="TOS81"/>
 *              <STA_KEY Status="P"/>
 *              <UGP_KEY UserGroup="ALL USERS"/>
 *          </TaskStatusPermission>
 *          <TaskStatusPermission repo-type="RDBMS">
 *              <MSG_UPDATE>1386887116000</MSG_UPDATE>
 *              <TOS_KEY Process="Flat File" AtomicProcess="TOS81"/>
 *              <STA_KEY Status="X"/>
 *              <UGP_KEY UserGroup="ALL USERS"/>
 *          </TaskStatusPermission>
 *          <TaskStatusPermission repo-type="RDBMS">
 *              <MSG_UPDATE>1386887116000</MSG_UPDATE>
 *              <TOS_KEY Process="Flat File" AtomicProcess="TOS81"/>
 *              <STA_KEY Status="R"/>
 *              <UGP_KEY UserGroup="SYSTEM ADMINISTRATORS"/>
 *          </TaskStatusPermission>
 * 
 *          <TaskAdapterMapping repo-type="RDBMS" id="MAV619">
 *              <MAV_MAP_QUALIFIER>String</MAV_MAP_QUALIFIER>
 *              <MAV_UPDATE>1386887635000</MAV_UPDATE>
 *              <MAV_MAP_VALUE>FirstName</MAV_MAP_VALUE>
 *              <MAV_MAP_TO>Literal</MAV_MAP_TO>
 *              <MAV_MAP_OLD_VALUE>0</MAV_MAP_OLD_VALUE>
 *              <ADV_KEY EventHandler="adpADPFFUPDATEUSER" AdapterVariable="attrFieldName" Adapter="adpFFUpdateUser"/>
 *          </TaskAdapterMapping>
 * 
 *          <TaskToObjectStatusMapping repo-type="RDBMS">
 *              <MST_UPDATE>1386887115000</MST_UPDATE>
 *              <STA_KEY Status="P"/>
 *              <OST_KEY Resource="FLATFILERESOURCE" ObjectStatus="None"/>
 *          </TaskToObjectStatusMapping>
 * 
 *          <TaskStatusPermission repo-type="RDBMS">
 *              <MSG_UPDATE>1386887116000</MSG_UPDATE>
 *              <TOS_KEY Process="Flat File" AtomicProcess="TOS81"/>
 *              <STA_KEY Status="W"/>
 *              <UGP_KEY UserGroup="SYSTEM ADMINISTRATORS"/>
 *          </TaskStatusPermission>
 * 
 *          <TaskToObjectStatusMapping repo-type="RDBMS">
 *              <MST_UPDATE>1386887115000</MST_UPDATE>
 *              <STA_KEY Status="UC"/>
 *              <OST_KEY Resource="FLATFILERESOURCE" ObjectStatus="None"/>
 *          </TaskToObjectStatusMapping>
 * 
 *          <TaskAdapterMapping repo-type="RDBMS" id="MAV618">
 *              <MAV_MAP_QUALIFIER>String</MAV_MAP_QUALIFIER>
 *              <MAV_UPDATE>1386887623000</MAV_UPDATE>
 *              <MAV_MAP_VALUE>UD_FLAT_FIL_SERVER</MAV_MAP_VALUE>
 *              <MAV_MAP_TO>Literal</MAV_MAP_TO>
 *              <MAV_MAP_OLD_VALUE>0</MAV_MAP_OLD_VALUE>
 *              <ADV_KEY EventHandler="adpADPFFUPDATEUSER" AdapterVariable="itResourceFieldName" Adapter="adpFFUpdateUser"/>
 *          </TaskAdapterMapping>
 * 
 *          <TaskAdapterMapping repo-type="RDBMS" id="MAV615">
 *              <MAV_FIELD_LENGTH>0</MAV_FIELD_LENGTH>
 *              <MAV_UPDATE>1386887592000</MAV_UPDATE>
 *              <MAV_MAP_TO>Response Code</MAV_MAP_TO>
 *              <MAV_MAP_OLD_VALUE>0</MAV_MAP_OLD_VALUE>
 *              <ADV_KEY EventHandler="adpADPFFUPDATEUSER" AdapterVariable="Adapter return value" Adapter="adpFFUpdateUser"/>
 *          </TaskAdapterMapping>
 * 
 *          <TaskStatusPermission repo-type="RDBMS">
 *              <MSG_UPDATE>1386887116000</MSG_UPDATE>
 *              <TOS_KEY Process="Flat File" AtomicProcess="TOS81"/>
 *              <STA_KEY Status="C"/>
 *              <UGP_KEY UserGroup="SYSTEM ADMINISTRATORS"/>
 *          </TaskStatusPermission>
 * 
 *          <TaskToObjectStatusMapping repo-type="RDBMS">
 *              <MST_UPDATE>1386887115000</MST_UPDATE>
 *              <STA_KEY Status="MC"/>
 *              <OST_KEY Resource="FLATFILERESOURCE" ObjectStatus="None"/>
 *          </TaskToObjectStatusMapping>
 * 
 *          <TaskToObjectStatusMapping repo-type="RDBMS">
 *              <MST_UPDATE>1386887115000</MST_UPDATE>
 *              <STA_KEY Status="PX"/>
 *              <OST_KEY Resource="FLATFILERESOURCE" ObjectStatus="None"/>
 *          </TaskToObjectStatusMapping>
 * 
 *          <TaskStatusPermission repo-type="RDBMS">
 *              <MSG_UPDATE>1386887116000</MSG_UPDATE>
 *              <TOS_KEY Process="Flat File" AtomicProcess="TOS81"/>
 *              <STA_KEY Status="PX"/>
 *              <UGP_KEY UserGroup="ALL USERS"/>
 *          </TaskStatusPermission>
 * 
 *          <TaskStatusPermission repo-type="RDBMS">
 *              <MSG_UPDATE>1386887116000</MSG_UPDATE>
 *              <TOS_KEY Process="Flat File" AtomicProcess="TOS81"/>
 *              <STA_KEY Status="UC"/>
 *              <UGP_KEY UserGroup="SYSTEM ADMINISTRATORS"/>
 *          </TaskStatusPermission>
 * 
 *          <TaskResponse repo-type="RDBMS" name="ERROR">
 *              <RSC_DESC>Error Occurred</RSC_DESC>
 *              <RSC_UPDATE>1386887934000</RSC_UPDATE>
 *              <STA_KEY Status="R"/>
 *          </TaskResponse>
 * 
 *          <TaskResponse repo-type="RDBMS" name="SUCCESS">
 *              <RSC_DESC>Operation Completed</RSC_DESC>
 *              <RSC_UPDATE>1386887933000</RSC_UPDATE>
 *              <STA_KEY Status="C"/>
 *          </TaskResponse>
 *          
 *          <TaskToObjectStatusMapping repo-type="RDBMS">
 *              <MST_UPDATE>1386887115000</MST_UPDATE>
 *              <STA_KEY Status="XLR"/>
 *              <OST_KEY Resource="FLATFILERESOURCE" ObjectStatus="None"/>
 *          </TaskToObjectStatusMapping>
 * 
 *          <TaskAdapterMapping repo-type="RDBMS" id="MAV616">
 *              <MAV_MAP_QUALIFIER>String</MAV_MAP_QUALIFIER>
 *              <MAV_UPDATE>1386887602000</MAV_UPDATE>
 *              <MAV_MAP_VALUE>User</MAV_MAP_VALUE>
 *              <MAV_MAP_TO>Literal</MAV_MAP_TO>
 *              <MAV_MAP_OLD_VALUE>0</MAV_MAP_OLD_VALUE>
 *              <ADV_KEY EventHandler="adpADPFFUPDATEUSER" AdapterVariable="objectType" Adapter="adpFFUpdateUser"/>
 *          </TaskAdapterMapping>
 * 
 *          <TaskStatusPermission repo-type="RDBMS">
 *              <MSG_UPDATE>1386887116000</MSG_UPDATE>
 *              <TOS_KEY Process="Flat File" AtomicProcess="TOS81"/>
 *              <STA_KEY Status="S"/><UGP_KEY UserGroup="ALL USERS"/>
 *          </TaskStatusPermission>
 * 
 *          <TaskToObjectStatusMapping repo-type="RDBMS">
 *              <MST_UPDATE>1386887115000</MST_UPDATE>
 *              <STA_KEY Status="UT"/>
 *              <OST_KEY Resource="FLATFILERESOURCE" ObjectStatus="None"/>
 *          </TaskToObjectStatusMapping>
 * 
 *          <TaskToObjectStatusMapping repo-type="RDBMS">
 *              <MST_UPDATE>1386887115000</MST_UPDATE>
 *              <STA_KEY Status="UCR"/><OST_KEY Resource="FLATFILERESOURCE" ObjectStatus="None"/>
 *          </TaskToObjectStatusMapping>
 * 
 *          <TaskStatusPermission repo-type="RDBMS">
 *              <MSG_UPDATE>1386887116000</MSG_UPDATE>
 *              <TOS_KEY Process="Flat File" AtomicProcess="TOS81"/>
 *              <STA_KEY Status="X"/><UGP_KEY UserGroup="SYSTEM ADMINISTRATORS"/>
 *          </TaskStatusPermission>
 * 
 *          <TaskAdapterMapping repo-type="RDBMS" id="MAV617">
 *              <MAV_FIELD_LENGTH>19</MAV_FIELD_LENGTH>
 *              <MAV_MAP_QUALIFIER>Process Instance</MAV_MAP_QUALIFIER>
 *              <MAV_UPDATE>1386887607000</MAV_UPDATE>
 *              <MAV_MAP_VALUE>orc_key</MAV_MAP_VALUE>
 *              <MAV_MAP_TO>Process Data</MAV_MAP_TO>
 *              <MAV_MAP_OLD_VALUE>0</MAV_MAP_OLD_VALUE>
 *              <ADV_KEY EventHandler="adpADPFFUPDATEUSER" AdapterVariable="processInstanceKey" Adapter="adpFFUpdateUser"/>
 *          </TaskAdapterMapping>
 * 
 *          <TaskStatusPermission repo-type="RDBMS">
 *              <MSG_UPDATE>1386887116000</MSG_UPDATE>
 *              <TOS_KEY Process="Flat File" AtomicProcess="TOS81"/>
 *              <STA_KEY Status="UC"/>
 *              <UGP_KEY UserGroup="ALL USERS"/>
 *          </TaskStatusPermission>
 * 
 *          <TaskStatusPermission repo-type="RDBMS">
 *              <MSG_UPDATE>1386887116000</MSG_UPDATE>
 *              <TOS_KEY Process="Flat File" AtomicProcess="TOS81"/>
 *              <STA_KEY Status="P"/>
 *              <UGP_KEY UserGroup="SYSTEM ADMINISTRATORS"/>
 *          </TaskStatusPermission>
 * 
 *          <TaskStatusPermission repo-type="RDBMS">
 *              <MSG_UPDATE>1386887116000</MSG_UPDATE>
 *              <TOS_KEY Process="Flat File" AtomicProcess="TOS81"/>
 *              <STA_KEY Status="C"/><UGP_KEY UserGroup="ALL USERS"/>
 *          </TaskStatusPermission>
 * 
 *          <TaskStatusPermission repo-type="RDBMS">
 *              <MSG_UPDATE>1386887116000</MSG_UPDATE>
 *              <TOS_KEY Process="Flat File" AtomicProcess="TOS81"/>
 *              <STA_KEY Status="PX"/>
 *              <UGP_KEY UserGroup="SYSTEM ADMINISTRATORS"/>
 *          </TaskStatusPermission>
 * 
 *          <TaskResponse repo-type="RDBMS" name="UNKNOWN">
 *              <RSC_DESC>An unknown response was received</RSC_DESC>
 *              <RSC_UPDATE>1386887115000</RSC_UPDATE>
 *              <STA_KEY Status="R"/>
 *          </TaskResponse>
 * 
 *          <TaskStatusPermission repo-type="RDBMS">
 *              <MSG_UPDATE>1386887116000</MSG_UPDATE>
 *              <TOS_KEY Process="Flat File" AtomicProcess="TOS81"/>
 *              <STA_KEY Status="R"/>
 *              <UGP_KEY UserGroup="ALL USERS"/>
 *          </TaskStatusPermission>
 * 
 *          <TaskToObjectStatusMapping repo-type="RDBMS">
 *              <MST_UPDATE>1386887115000</MST_UPDATE>
 *              <STA_KEY Status="R"/>
 *              <OST_KEY Resource="FLATFILERESOURCE" ObjectStatus="None"/>
 *          </TaskToObjectStatusMapping>
 * 
 *          <TaskAssignmentRule repo-type="RDBMS" id="RML245">
 *              <RML_UPDATE>1386887115000</RML_UPDATE>
 *              <RML_TARGET_TYPE>Group</RML_TARGET_TYPE>
 *              <RML_PRIORITY>1</RML_PRIORITY>
 *              <RUL_KEY Rule="Default"/>
 *              <UGP_KEY UserGroup="SYSTEM ADMINISTRATORS"/>
 *          </TaskAssignmentRule>
 * 
 * </ProcessTask>
 */