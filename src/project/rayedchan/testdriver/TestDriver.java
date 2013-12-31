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
    public static void main(String[] args)
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
    
    public static void main2(String[] args) throws LoginException, tcAPIException, tcInvalidLookupException, tcDuplicateLookupCodeException, tcColumnNotFoundException, tcInvalidValueException, tcInvalidAttributeException, tcFormNotFoundException, tcFormFieldNotFoundException, tcDeleteNotAllowedException, tcAddFieldFailedException, tcProcessNotFoundException, SQLException, tcObjectNotFoundException, tcProcessFormException, IOException, NamingException, TransformerConfigurationException, TransformerException, DDMException, TransformationException, tcBulkException, tcUpdateNotAllowedException, ParserConfigurationException, XPathExpressionException, SAXException, tcDataSetException, tcDataAccessException, ResourceObjectNameNotFoundException, ProcessDefintionNotFoundException, NoResourceObjForProcessDefException, ProcessFormNotFoundException
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
        
        HelperUtility.getAllProcessDefinitions(dbProvider);
    }
    
    
    public static void printStringList(String[] list)
    {
        for(String element: list)
        {
            System.out.println(element);
        }
    }
}
