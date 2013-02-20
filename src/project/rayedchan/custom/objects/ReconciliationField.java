package project.rayedchan.custom.objects;

/**
 * @author rayedchan
 * A class to represent a reconciliation field in a resource object. 
 */
public class ReconciliationField
{
    private String reconFieldName = null;
    private String reconFieldType = null;
    private Boolean isRequired = null;   
    
    /*
     * Constructors
     */
    public ReconciliationField(String reconFieldName, String reconFieldType, Boolean isRequired)
    {  
        this.reconFieldName = reconFieldName;
        this.reconFieldType = reconFieldType;
        this.isRequired = isRequired;  
    }
    
    /*
     * Getter and Setter methods
     */
    public void setReconFieldName(String reconFieldName)
    {
        this.reconFieldName = reconFieldName;
    }
    
    public String getReconFieldName()
    {
        return this.reconFieldName;
    }
    
    public void setReconFieldType(String reconFieldType)
    {
        this.reconFieldType = reconFieldType;
    }
    
    public String getReconFieldType()
    {
        return this.reconFieldType;
    }
    
    public void setIsRequired(Boolean isRequired)
    {
        this.isRequired = isRequired;
    }
    
    public Boolean getIsRequired()
    {
        return this.isRequired;
    }
    
    /*
     * String representation of the object. Values of the object fields are included in 
     * the String.
     */
    @Override
    public String toString()
    {
        return String.format(
        "%-25s%-25s%-25s\n",        
        this.reconFieldName, this.reconFieldType, this.isRequired);
    }
}
