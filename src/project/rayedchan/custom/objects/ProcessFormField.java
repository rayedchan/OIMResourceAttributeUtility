package project.rayedchan.custom.objects;

/**
 * @author rayedchan
 * An class-object representation of a process form field.
 */
public class ProcessFormField
{
    private long processFormKey;
    private int processFormVersion;
    private String fieldName; //Label Name and Column Name suffix
    private String fieldType = "TextField";
    private String variantType = "String";
    private int length = 100; //Specify zero if field does not have length attribute
    private int order = 50;
    private String defaultValue = null;
    private String profileEnabled = null; //Application Profile; "0" = false, "1" = true
    private boolean secure = false; //Encrypted
    private int lineNumber; //Line number in flat file
    
    /*
     * Constructors
     */
    public ProcessFormField(long processFormKey, int processFormVersion, String fieldName, String fieldType, String variantType, int length, int order, String defaultValue, String profileEnabled, boolean secure)
    {  
        this.processFormKey = processFormKey;
        this.processFormVersion = processFormVersion;
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.variantType = variantType;
        this.length = length;
        this.order = order;
        this.defaultValue = defaultValue;
        this.profileEnabled = profileEnabled;
        this.secure = secure;
    }
    
    public ProcessFormField(long processFormKey, int processFormVersion)
    {
        this.processFormKey = processFormKey;
        this.processFormVersion = processFormVersion;
    }
    
    /*
     * Getter methods
     */
    public long getProcessFormKey()
    {
        return this.processFormKey;
    }
    
    public int getProcessFormVersion()
    {
        return this.processFormVersion;
    }
    
    public String getFieldName()
    {
        return this.fieldName;
    }
    
    public String getFieldType()
    {
        return this.fieldType;
    }
    
    public String getVariantType()
    {
        return this.variantType;
    }
    
    public int getLength()
    {
        return this.length;
    }
    
    public int getOrder()
    {
        return this.order;
    }
    
    public String getDefaultValue()
    {
        return this.defaultValue;
    }
    
    public String getProfileEnabled()
    {
        return this.profileEnabled;
    }
    
    public boolean getSecure()
    {
        return this.secure;
    }
    
    public int getLineNumber()
    {
        return this.lineNumber;
    }
    
    /*
     * Setter Methods
     */
    public void setProcessFormKey(long processFormKey)
    {
        this.processFormKey = processFormKey;
    }
    
    public void setProcessFormVersion(int processFormVersion)
    {
        this.processFormVersion = processFormVersion;
    }
    
    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }
    
    public void setFieldType(String fieldType)
    {
        this.fieldType = fieldType;
    }
    
    public void setVariantType(String variantType)
    {
        this.variantType = variantType;
    }
    
    public void setLength(int length)
    {
        this.length = length;
    }
    
    public void setOrder(int order)
    {
        this.order = order;
    }
    
    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }
    
    public void setProfileEnabled(String profileEnabled)
    {
        this.profileEnabled = profileEnabled;
    }
    
    public void setSecure(boolean secure)
    {
        this.secure = secure;
    }
    
    public void setLineNumber(int lineNumber)
    {
        this.lineNumber = lineNumber;
    }
    
    /*
     * String representation of the object. Values of the object fields are included in 
     * the String.
     */
    @Override
    public String toString()
    {
        return String.format(
        "Form Key=%s, Version=%s, FieldName=%s, FieldType=%s, VariantType=%s, "
                + "Length=%s, Order=%s, DefaultValue=%s, ProfileEnabled=%s, secure=%s\n",        
        this.processFormKey, this.processFormVersion, this.fieldName,
        this.fieldType, this.variantType, this.length, this.order,
        this.defaultValue, this.profileEnabled, this.secure);
    }
}
