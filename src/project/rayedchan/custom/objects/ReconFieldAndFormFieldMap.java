package project.rayedchan.custom.objects;

/**
 * @author rayedchan
 * A class to represent a mapping between a reconciliation field and process
 * form field.
 */
public class ReconFieldAndFormFieldMap
{
    private String reconFieldName;
    private String formFieldColumnName;
    
    public ReconFieldAndFormFieldMap(String reconFieldName, String formFieldColumnName)
    {
        this.reconFieldName = reconFieldName;
        this.formFieldColumnName = formFieldColumnName;
    }
    
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
}