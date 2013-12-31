package project.rayedchan.utilities;

import Thor.API.Exceptions.tcAPIException;
import Thor.API.Exceptions.tcColumnNotFoundException;
import Thor.API.Exceptions.tcDuplicateLookupCodeException;
import Thor.API.Exceptions.tcInvalidAttributeException;
import Thor.API.Exceptions.tcInvalidLookupException;
import Thor.API.Exceptions.tcInvalidValueException;
import Thor.API.Operations.tcLookupOperationsIntf;
import Thor.API.tcResultSet;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import project.rayedchan.exception.BadFileFormatException;
import project.rayedchan.exception.LookupNameNotFoundException;

/**
 * @author rayedchan
 * This utility supports create, delete, and update operations related to lookups.
 * Mainly used for adding and deleting lookup entries specified from a flat file.
 * 
 * Differences between API and Design Console:
 * Design Console allows duplicate code key; API restricts duplicate code key.
 * Lookup definition name cannot be changed in Design Console, but through API the name can be changed.
 */
public class LookupUtility 
{
    /*
     * Add entries from a flat file to an existing lookup. 
     * Sanity Check: Name of lookup definition must exist. File format must be correct.
     * If a code key already exist in the lookup, it will be skipped and reported to the user.
     * If there are duplicates code keys in the file, the first one in the file will be added.
     * Beginning and trailing whitespaces are removed from code key and decode.
     * 
     * File Format - the file is tab delimited and a newline for each record
     * <code key>   <decode>
     * <code key>   <decode>
     * 
     * Example
     * codeKey1 decode2
     * codeKey2 decode2
     * 
     * @param   lookupOps    tcLookupOperationsIntf service object
     * @param   lookupName   Name of existing lookup
     * @param   fileName     Path of file on local machine that contains the data
     * @param   delimiter    Delimiter that separates each field in file
     * @return boolean value to indicate success or failure
     */
    public static boolean addEntriesToLookupDSFF(tcLookupOperationsIntf lookupOps, String lookupName, String fileName, String delimiter) throws tcAPIException, tcColumnNotFoundException, tcInvalidLookupException, LookupNameNotFoundException, FileNotFoundException, IOException, BadFileFormatException
    {
        FileInputStream fstream = null;
        DataInputStream in = null;
        BufferedReader br = null;
        int lineNumber = 0;
         
        try
        {
           fstream = new FileInputStream(fileName); //Open File
           in = new DataInputStream(fstream); //Get the object of DataInputStream
           br = new BufferedReader(new InputStreamReader(in));
           String strLine;
           HashMap<String,String> entries = new HashMap<String,String>(); //stores all the entries 
           
           //Validate if the lookup exists
           if(doesLookupExist(lookupOps, lookupName) == false)
           {
               System.out.println("[ERROR]: Lookup Definition does not exist.");
               throw new LookupNameNotFoundException();
           }
           
           //Read the entries from file
           while ((strLine = br.readLine()) != null)  
           {
                lineNumber++;
                
                //System.out.println(strLine);
                StringTokenizer st = new StringTokenizer(strLine, delimiter); 
                String key = null;
                String value = null;
                int counter = 0;
                
                //Get the code key and decode of an entry
                while (st.hasMoreTokens()) 
                {
                    if(counter == 0)
                    {
                        key = st.nextToken(); //Get code Key
                    }
                       
                    else if(counter == 1)
                    {
                        value = st.nextToken(); //Get decode
                    }
                    
                    else
                    {
                        System.out.println("[Error]: File format is incorrect. Fix Line["+ lineNumber +"]: " + strLine);
                        throw new BadFileFormatException(String.format("File format is incorrect. Fix Line[%s]: %s ", lineNumber,  strLine));
                    }
                    
                    counter++;
                }
                
                if(key != null && value != null)   
                {
                    //check if the code key exist in the lookup
                    if(doesEntryExist(lookupOps, lookupName, "Lookup Definition.Lookup Code Information.Code Key", key))
                    {
                        System.out.println("[Warning]: Entry ["+ key + ", " + value +"] will not be added. Code key exists in lookup.");
                    }
                    
                    //duplicate code key exist in file
                    else if(entries.containsKey(key))
                    {
                        System.out.println("[Warning]: Entry ["+ key + ", " + value +"] will not be added. Duplicate Code key in file.");
                    }
                    
                    //code key does not exist in lookup -> add to hashmap
                    else
                    {
                        entries.put(key, value); //add lookup entry values to map
                    }
                }
                
                else
                {    
                     System.out.println("[Error]: File format is incorrect. Fix Line["+ lineNumber +"]: " + strLine);
                     throw new BadFileFormatException(String.format("File format is incorrect. Fix Line[%s]: %s ", lineNumber,  strLine));
                }  
           }
           
           System.out.println("[Info]: Entries to be added: " + entries); 
           Iterator it = entries.entrySet().iterator();

           while (it.hasNext()) 
           {
               Map.Entry pairs = (Map.Entry)it.next();
                try {
                    addEntryToLookup(lookupOps, lookupName, pairs.getKey().toString(), pairs.getValue().toString(), "", "");
                } catch (tcInvalidLookupException ex) {
                    Logger.getLogger(LookupUtility.class.getName()).log(Level.SEVERE, null, ex);
                } catch (tcInvalidValueException ex) {
                    Logger.getLogger(LookupUtility.class.getName()).log(Level.SEVERE, null, ex);
                } catch (tcAPIException ex) {
                    Logger.getLogger(LookupUtility.class.getName()).log(Level.SEVERE, null, ex);
                }
                
               it.remove(); // avoids a ConcurrentModificationException
           }
           
           return true;
        } 
         
        finally
        {
            if(br != null)
            {
                try {
                    br.close();
                } catch (IOException ex) {
                    Logger.getLogger(LookupUtility.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            if(in != null)
            {
                try {
                    in.close(); //Close the input stream
                } catch (IOException ex) {
                    Logger.getLogger(LookupUtility.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            if(fstream != null)
            {
                try {
                    fstream.close();
                } catch (IOException ex) {
                    Logger.getLogger(LookupUtility.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }      
    }
    
    /*
     * Deletes entries from lookup. Data source comes from a flat file. 
     * Sanity Check: Name of lookup definition must exist. File format must be correct.
     * Checks for existence of code key in lookup.
     * If there are duplicate code key in the lookup definition, only the first 
     * will be deleted regardless of code key appearing more than once in the flat file.
     * Beginning and trailing whitespaces are removed from code key and decode.
     * 
     * File Format
     * <code Key>
     * <code Key>
     * 
     * Example
     * code1
     * code2
     * 
     *@param   lookupOps    tcLookupOperationsIntf service object
     *@param   lookupName   Name of existing lookup
     *@param   fileName     name of file that contains the data 
     *@return  boolean value to indicate success or failure
     */
    public static boolean deleteEntriesFromLookupDSFF(tcLookupOperationsIntf lookupOps, String lookupName, String fileName) throws tcAPIException, tcInvalidLookupException, tcColumnNotFoundException, LookupNameNotFoundException, BadFileFormatException, FileNotFoundException, IOException 
    {
        FileInputStream fstream = null;
        DataInputStream in = null;
        BufferedReader br = null;
        int lineNumber = 0;
        
        try
        {
           fstream = new FileInputStream(fileName); //Open File
           in = new DataInputStream(fstream); //Get the object of DataInputStream
           br = new BufferedReader(new InputStreamReader(in));
           String strLine;
           HashMap<String,String> entries = new HashMap<String,String>(); //stores all the entries 
        
           //Validate if the lookup exists
           if(doesLookupExist(lookupOps, lookupName) == false)
           {
               System.out.println("[ERROR]: Lookup Definition does not exist.");
               throw new LookupNameNotFoundException();
           }
           
           //Read the code key from file
           while ((strLine = br.readLine()) != null)  
           {
                lineNumber++;
                String codeKey = strLine;
                
                if(codeKey != null && !codeKey.equalsIgnoreCase(""))   
                {
                    if(entries.containsKey(codeKey))
                    {
                        System.out.println("[Warning]: Duplicate Code key '" + codeKey  +" on line " + lineNumber);
                    }
                    
                    //check if the code key exist in the lookup
                    else if(doesEntryExist(lookupOps, lookupName, "Lookup Definition.Lookup Code Information.Code Key", codeKey))
                    {
                        entries.put(codeKey, null); //add lookup code key; Keys are unique in a hashmap
                    }
                    
                    //code key does not exist in lookup
                    else
                    {
                        System.out.println("[Warning]: Code key '" + codeKey  +"' does not exist in lookup.");
                    }
                }
                
                else
                {    
                    System.out.println("[Error]: File format is incorrect. Fix Line["+ lineNumber +"]: " + strLine);
                    throw new BadFileFormatException(String.format("File format is incorrect. Fix Line[%s]: %s ", lineNumber,  strLine));
                }      
           }
           
           System.out.println("[Info]: Entries to be removed: " + entries); 
           Iterator it = entries.entrySet().iterator();
    
           while (it.hasNext()) 
           {
                Map.Entry pairs = (Map.Entry)it.next();
                try {
                    removeEntryFromLookup(lookupOps, lookupName, pairs.getKey().toString());
                } catch (tcAPIException ex) {
                    Logger.getLogger(LookupUtility.class.getName()).log(Level.SEVERE, null, ex);
                } catch (tcInvalidLookupException ex) {
                    Logger.getLogger(LookupUtility.class.getName()).log(Level.SEVERE, null, ex);
                } catch (tcInvalidValueException ex) {
                    Logger.getLogger(LookupUtility.class.getName()).log(Level.SEVERE, null, ex);
                }
                it.remove(); // avoids a ConcurrentModificationException
           }
           
           return true;
        } 
        
        finally
        {
            if(br != null)
            {
                try {
                    br.close();
                } catch (IOException ex) {
                    Logger.getLogger(LookupUtility.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            if(in != null)
            {
                try {
                    in.close(); //Close the input stream
                } catch (IOException ex) {
                    Logger.getLogger(LookupUtility.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            if(fstream != null)
            {
                try {
                    fstream.close();
                } catch (IOException ex) {
                    Logger.getLogger(LookupUtility.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }      
    }
    
    /*
     * Print all the columns of a lookup entry as defined by the tcLookupOperationsIntf services.
     * For other methods in tcLookupOperationsIntf, you may specify the name defined by the tcLookupOperationsIntf
     * services or the actual table column name. The latter is preferred.
     * 
     * Important OIM Schemas
     * LKU - contains all the lookup definition
     * LKV - conatins all the lookup entries
     * 
     * Important Columns for a Lookup Entry
     * Lookup Definition.Key [LKU_KEY] = Lookup Key to which the entry belongs
     * Lookup Definition.Code [LKU_TYPE_STRING_KEY] = Name of Lookup to which the entry belongs
     * Lookup Definition.Lookup Code Information.Key [LKV_KEY] = Entry(CodeKey/Decode) key
     * Lookup Definition.Lookup Code Information.Code Key [LKV_ENCODED] = Code Key
     * Lookup Definition.Lookup Code Information.Decode [LKV_DECODED] = Decode
     * Lookup Definition.Lookup Code Information.Language [LKV_LANGUAGE] = Language of the entry
     * Lookup Definition.Lookup Code Information.Country [LKV_COUNTRY] = Country of the entry
     * 
     * @param   lookupOps   tcLookupOperationsIntf service object 
     * @param   lookupName  Name of the lookup definition    
     */
    public static void printLookupEntryColumns(tcLookupOperationsIntf lookupOps, String lookupName) throws tcAPIException, tcInvalidLookupException
    {
        tcResultSet lookupResultSet = lookupOps.getLookupValues(lookupName); //get all the entries of a lookup
        String [] columnNames = lookupResultSet.getColumnNames();
        for(String columnName : columnNames)
        {
            System.out.println(columnName);
        }
    }
    
    /*
     * Print all the entries of a lookup.
     * @param   lookupOps   tcLookupOperationsIntf service object
     * @param   lookupName  Name of the lookup definition
     */
    public static void printLookupEntries(tcLookupOperationsIntf lookupOps, String lookupName) throws tcAPIException, tcInvalidLookupException, tcColumnNotFoundException
    {
        tcResultSet lookupResultSet = lookupOps.getLookupValues(lookupName); //get all the entries of a lookup
        
        System.out.printf("%-25s%-25s%-25s\n", "Entry Key" ,"Code Key", "Decode");
        System.out.printf("%-25s%-25s%-25s\n", "==========", "==========", "==========");
        for(int i = 0; i < lookupResultSet.getTotalRowCount(); i++)
        {
            lookupResultSet.goToRow(i);
            String entryKey = lookupResultSet.getStringValue("Lookup Definition.Lookup Code Information.Key");
            String code = lookupResultSet.getStringValue("Lookup Definition.Lookup Code Information.Code Key");
            String decode = lookupResultSet.getStringValue("Lookup Definition.Lookup Code Information.Decode");
            System.out.printf("%-25s%-25s%-25s\n",entryKey,code,decode);
        }
    }
    
    /*
     * Export lookup name and its entries in a format used for this utility.
     * Tab delimited for entries.
     * @param   lookupOps   tcLookupOperationsIntf service object
     * @param   lookupName  Name of the lookup definition
     * @param   fileName    Absolute path of file on local machine
     * @param   delimiter   Delimiter that separates each field in file
     */
    public static void exportLookupFileFormat(tcLookupOperationsIntf lookupOps, String lookupName, String fileName, String delimiter) throws tcAPIException, tcInvalidLookupException, tcColumnNotFoundException, FileNotFoundException, UnsupportedEncodingException, LookupNameNotFoundException
    {
        PrintWriter writer = null;
        
        try
        {      
            //Validate if the lookup exists
            if(doesLookupExist(lookupOps, lookupName) == false)
            {
                System.out.println("[ERROR]: Lookup Definition does not exist.");
                throw new LookupNameNotFoundException();
            }
           
            tcResultSet lookupResultSet = lookupOps.getLookupValues(lookupName); //get all the entries of a lookup
            writer = new PrintWriter(fileName, "UTF-8");
            
            for(int i = 0; i < lookupResultSet.getTotalRowCount(); i++)
            {
                lookupResultSet.goToRow(i);
                String code = lookupResultSet.getStringValue("Lookup Definition.Lookup Code Information.Code Key");
                String decode = lookupResultSet.getStringValue("Lookup Definition.Lookup Code Information.Decode");
                writer.printf("%s%s%s\n",code, delimiter, decode);
            }
        }
        
        finally
        {
            if(writer != null)
            {
                writer.close();
            }
        }
    }
           
    /*
     * Creates a lookup definition.
     * @param   lookupOps    tcLookupOperationsIntf service object
     * @param   lookupName   Name of the lookup definition to be created
     * Note: "Group" Field must be specified for lookup definition when using the Design Console.
     * May need to call update operation on Group field.
     */
    public static void addLookup(tcLookupOperationsIntf lookupOps, String lookupName) throws tcAPIException, tcDuplicateLookupCodeException
    {
        lookupOps.addLookupCode(lookupName); //adds a lookup
    }
    
    /*
     * Updates a lookup definition. 
     * @param     lookupOps - tcLookupOperationsIntf service object
     * @param     lookupName - Name of the lookup definition to be updated
     * @param     field - Name of field to be updated
     * @param     fieldValue - New value for field 
     * 
     * Fields
     * LKU_GROUP - Lookup Group (Description) Field
     * LKU_TYPE_STRING_KEY - Lookup Name 
     * 
     * Note: The API used here will create the lookup if "LKU_TYPE_STRING_KEY" is specified and lookup name does not exist.
     */
    public static void updateLookup(tcLookupOperationsIntf lookupOps, String lookupName, String field, String fieldValue) throws tcInvalidLookupException, tcInvalidAttributeException, tcAPIException
    {
        HashMap<String,String> updateField = new HashMap<String,String>();
        updateField.put(field, fieldValue);
        lookupOps.updateLookupCode(lookupName, updateField);   
    }
    
    /*
     * Adds an entry to an existing lookup.
     * @param   lookupOps   tcLookupOperationsIntf service object
     * @param   lookupName  Name of the lookup definition 
     * @param   codeKey     Value of an entry's code key
     * @param   decode      Value of an entry's decode
     * @param   language    language of entry
     * @param   country     country of entry
     */
    public static void addEntryToLookup(tcLookupOperationsIntf lookupOps, String lookupName, String codeKey, String decode, String language, String country) throws tcAPIException, tcInvalidLookupException, tcInvalidValueException
    {
        lookupOps.addLookupValue(lookupName, codeKey, decode, language, country);
    }
    
    /*
     * Removes an entry from a lookup. Delete is determined by an entry's code key.
     * @param   lookupOps    tcLookupOperationsIntf service object
     * @param   lookupName   Name of the lookup definition  
     * @param   codeKey      Value of an entry's code key 
     * Note: If there are duplicates of the code key in your lookup, only one of them will be removed.
     */
    public static void removeEntryFromLookup(tcLookupOperationsIntf lookupOps, String lookupName, String codeKey) throws tcAPIException, tcInvalidLookupException, tcInvalidValueException
    {
         lookupOps.removeLookupValue(lookupName, codeKey);
    }
    
    /*
     * Update an existing entry in a lookup. Update is determined by entry's code key.
     * @param   lookupOps           tcLookupOperationsIntf service object
     * @param   lookupName          Name of the lookup definition 
     * @param   existingCodeKey     An exisiting entry's code key
     * @param   newCodeKey          code key to be changed to
     * @param   newDecode           decode value to be changed to
     * Note: If there are duplicates of the code key in your lookup, only one of them will be updated.
     */
    public static void updateEntryFromLookup(tcLookupOperationsIntf lookupOps, String lookupName, String existingCodeKey, String newCodeKey, String newDecode) throws tcAPIException, tcInvalidLookupException, tcInvalidLookupException, tcInvalidAttributeException, tcInvalidValueException
    {
        HashMap<String,String> update = new HashMap<String,String>();
        update.put("LKV_ENCODED", newCodeKey);
        update.put("LKV_DECODED", newDecode);
        lookupOps.updateLookupValue(lookupName, existingCodeKey, update);
    }
   
    /*
     * Determines if an entry in a lookup exists.
     * @param   lookupOps           tcLookupOperationsIntf service object
     * @param   lookupName          Name of the lookup definition 
     * @param   columnNameFilter    Name of one the entry columns
     * @param   columnValueFilter   Value of the entry column to be searched by
     * @return  boolean value to indicates if entry exist in the specified lookup
     */
    public static boolean doesEntryExist(tcLookupOperationsIntf lookupOps, String lookupName, String columnNameFilter, String columnValueFilter) throws tcAPIException, tcInvalidLookupException, tcColumnNotFoundException
    {
        HashMap<String,String> entryFilter = new HashMap<String,String>();
        entryFilter.put(columnNameFilter, columnValueFilter);
        tcResultSet searchResultSet = lookupOps.getLookupValues(lookupName, entryFilter);   
        
        if(searchResultSet.getTotalRowCount() >= 1)
        {
            return true;
        }
        
        return false;
    }
    
    /*
     * Checks if a lookup exists
     * @param   lookupOps   tcLookupOperationsIntf service object
     * @param   lookupName  Name of the lookup definition
     * @return  boolean value to indicate if lookup exists.
     */
    public static boolean doesLookupExist(tcLookupOperationsIntf lookupOps, String lookupName)
    {
        try 
        {
            lookupOps.getLookupValues(lookupName);
            return true;
        }
        
        catch (tcAPIException ex) {}
        catch (tcInvalidLookupException ex) {} 
        return false;
    }
}