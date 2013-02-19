package project.rayedchan.custom.objects;

/**
 *
 * @author rayedchan
 * A class to represent a reconciliation field in a resource object. 
 */
class ReconciliationField
{
    private String reconFieldName = null;
    private String reconFieldType = null;
    private String isRequired = null;   
    
    public ReconciliationField(String reconFieldName, String reconFieldType, String isRequired)
    {  
        this.reconFieldName = reconFieldName;
        this.reconFieldType = reconFieldType;
        this.isRequired = isRequired;  
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
