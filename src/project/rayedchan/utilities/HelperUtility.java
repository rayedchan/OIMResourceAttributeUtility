package project.rayedchan.utilities;

import Thor.API.Exceptions.tcAPIException;
import Thor.API.Exceptions.tcColumnNotFoundException;
import Thor.API.tcResultSet;
import com.thortech.xl.dataaccess.tcDataProvider;
import com.thortech.xl.dataaccess.tcDataSet;
import com.thortech.xl.dataaccess.tcDataSetException;
import com.thortech.xl.dataobj.PreparedStatementUtil;
import com.thortech.xl.orb.dataaccess.tcDataAccessException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author rayedchan
 * A class that contains helpful miscellaneous methods.
 */
public class HelperUtility 
{
    /*
     * Prints the column names of a tcResultSet.
     * @param   tcResultSetObj  tcResultSet Object
     */
    public static void printTcResultSetColumnNames(tcResultSet tcResultSetObj)
    {
        String[] columnNames = tcResultSetObj.getColumnNames();
        
        for(String columnName: columnNames)
        {
            System.out.println(columnName);
        }
    }
    
    /*
     * Prints the records of a tcResultSet.
     * @param   tcResultSetObj  tcResultSetObject
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
    
    /*
     * Print the records of the ResultSet. 
     * @param   resultSet   the result set of a query
     */
    public static void printResultSetRecords(ResultSet resultSet) throws SQLException
    {
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int columnCount = resultSetMetaData.getColumnCount();
        
        //iterate all the records from the result set
        while(resultSet.next())
        {
            //iterate all the columns of a record and print value. Column count starts at one.
            for(int i = 1; i < columnCount + 1; i++)
            {
                String columnName = resultSetMetaData.getColumnName(i);
                String columnValue = resultSet.getString(columnName);
                System.out.println(columnName + " = " + columnValue);
            }
            
            System.out.println();
        }
    }
    
    /*
     * Print all process definitons with their corresponding keys.
     * TOS = Process Definition
     * SDK = Structure Utility (Process Form)
     * OBJ = Resource Object Definition
     * PKG = Service Processes
     * @param   dbProvider   connection to the OIM Schema 
     */
    public static void getAllProcessDefinitions(tcDataProvider dbProvider) throws tcDataSetException, tcDataAccessException
    {   
        tcDataSet pkgDataSet = null;
        PreparedStatementUtil ps = null;
        
        try 
        {
            String query = "SELECT PKG.PKG_KEY, TOS.TOS_KEY, SDK.SDK_KEY, PKG.PKG_NAME, SDK.SDK_NAME, OBJ.OBJ_KEY, OBJ.OBJ_NAME FROM "
             + "TOS RIGHT OUTER JOIN PKG ON PKG.PKG_KEY = TOS.PKG_KEY "
             + "LEFT OUTER JOIN SDK ON SDK.SDK_KEY = TOS.SDK_KEY "
             + "LEFT OUTER JOIN OBJ ON OBJ.OBJ_KEY = PKG.OBJ_KEY ORDER BY PKG.PKG_NAME";
            
            ps = new PreparedStatementUtil();
            ps.setStatement(dbProvider, query);
            ps.execute();
            pkgDataSet = ps.getDataSet();
            int numRecords = pkgDataSet.getTotalRowCount();
            
            System.out.printf("%-25s%-25s%-25s%-25s%-25s%-25s%-25s\n", "Package Key", "Package Name","Process Key", "Object Key", "Object Name", "Form Key", "Form Name");
            for(int i = 0; i < numRecords; i++)
            {
                pkgDataSet.goToRow(i);
                String packageKey = pkgDataSet.getString("PKG_KEY");
                String packageName = pkgDataSet.getString("PKG_NAME");
                String processKey = pkgDataSet.getString("TOS_KEY");
                String objectKey = pkgDataSet.getString("OBJ_KEY");
                String objectName = pkgDataSet.getString("OBJ_NAME");
                String formName = pkgDataSet.getString("SDK_NAME");
                String formKey = pkgDataSet.getString("SDK_KEY");
                System.out.printf("%-25s%-25s%-25s%-25s%-25s%-25s%-25s\n",packageKey, packageName, processKey, objectKey, objectName, formKey, formName);
            }
        } 
        
        finally
        {   
        }
    }
    
    /*
     * Determine if a string can be parse into an integer.
     * @param   strValue    validate if string value can be parsed 
     * @return  boolean value to indicate if string is an integer
     */
    public static boolean isInteger(String strValue)
    {
        try 
        {
            Integer.parseInt(strValue);
            return true;
        }
        
        catch (NumberFormatException nfe)
        {
            return false;
        }
    }
    
     /*
     * Determine if a string can be parse into an boolean.
     * @param   strValue    validate if string value can be parsed 
     * @return  boolean value to indicate if string is a boolean
     */
    public static boolean isBoolean(String strValue)
    {
        strValue = strValue.toLowerCase();
        return "true".equalsIgnoreCase(strValue) || "false".equalsIgnoreCase(strValue);
    }
     
    /*
     * Parses a String Object, which stores xml content, into a Document Object.
     * @param   xmlContent   xml content
     * @return  document representation of the given xml  
     */
    public static Document parseStringXMLIntoDocument(String xmlContent) throws ParserConfigurationException, SAXException, IOException
    {    
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = (Document) builder.parse(new InputSource(new StringReader(xmlContent)));
        return document;   
    }
    
    /*
     * Converts a Document into String representation
     * UTF-8 conversion
     * @param   document    Document object to be parsed
     * @return  String representation of xml content
     */
    public static String parseDocumentIntoStringXML(Document document) throws TransformerConfigurationException, TransformerException
    {
        StringWriter output = new StringWriter();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(new DOMSource(document), new StreamResult(output));
        String newObjectResourceXML = output.toString();
        return newObjectResourceXML;
    }
    
     /*
     * Converts a Document into String representation
     * UTF-16 conversion
     * @param   document Document object to be parsed
     * @return  String representation of xml content
     */
    public static String parseDocumentIntoStringXMLVersion2(Document document) throws TransformerConfigurationException, TransformerException
    {  
        DOMImplementationLS domImplementation = (DOMImplementationLS)  document.getImplementation();
        LSSerializer lsSerializer = domImplementation.createLSSerializer();
        String newResourceObjectXML = lsSerializer.writeToString(document);
        return newResourceObjectXML;
    }
}
