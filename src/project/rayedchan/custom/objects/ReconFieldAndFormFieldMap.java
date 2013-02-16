package project.rayedchan.custom.objects;

/**
 * @author rayedchan
 * A class to represent a mapping between a reconciliation field and process
 * form field. The PRF table contains all the existing reconciliation field to form field
 * mappings. When creating a mapping, the reconciliation fields are taken from the ORF table 
 * and the form fields are taken from the SDC table.
 */
public class ReconFieldAndFormFieldMap
{
    private String reconFieldName; //ORF.ORF_FIELDNAME
    private String formFieldColumnName; //SDC.SDC_NAME
    private String reconFieldKey; //ORF.ORF_KEY
    private Boolean isKeyField = false;
    private Boolean isCaseInsensitive = false;
    
    /*
     * Constructors
     */
    public ReconFieldAndFormFieldMap()
    {
        
    }
    
    public ReconFieldAndFormFieldMap(String reconFieldName, String formFieldColumnName)
    {
        this.reconFieldName = reconFieldName;
        this.formFieldColumnName = formFieldColumnName;
    }
    
    public ReconFieldAndFormFieldMap(String reconFieldName, String formFieldColumnName, String reconFieldKey, Boolean isKeyField, Boolean isCaseInsensitive)
    {
        this.reconFieldName = reconFieldName;
        this.formFieldColumnName = formFieldColumnName;
        this.reconFieldKey = reconFieldKey;
        this.isKeyField = isKeyField;
        this.isCaseInsensitive = isCaseInsensitive;
    }
    
    /*
     * Getter and Setter Methods
     */
    public void setReconFieldName(String reconFieldName)
    {
        this.reconFieldName = reconFieldName;
    }
    
    public String getReconFieldName()
    {
        return this.reconFieldName;
    }
    
    public void setFormFieldColumnName(String formFieldColumnName)
    {
        this.formFieldColumnName = formFieldColumnName;
    }
    
    public String getFormFieldColumnName()
    {
        return this.formFieldColumnName;
    }
    
    public void setReconFieldKey(String reconFieldKey)
    {
        this.reconFieldKey = reconFieldKey;
    }
    
    public String getReconFieldKey()
    {
        return this.reconFieldKey;
    }
    
    public void setIsKeyField(Boolean isKeyField)
    {
        this.isKeyField = isKeyField;
    }
    
    public Boolean getIsKeyField()
    {
        return this.isKeyField;
    }
    
    public void setIsCaseInsensitive(Boolean isCaseInsensitive)
    {
        this.isCaseInsensitive = isCaseInsensitive;
    }
    
    public Boolean getIsCaseInsensitive()
    {
        return this.isCaseInsensitive;
    }
}