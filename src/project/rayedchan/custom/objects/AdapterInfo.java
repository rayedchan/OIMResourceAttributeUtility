package project.rayedchan.custom.objects;

/**
 * @author rayedchan
 * Data from ADP table
 */
public class AdapterInfo 
{
    private long adpKey;
    private String adpName;
    
    public AdapterInfo(long adpKey, String adpName)
    {
        this.setAdpKey(adpKey);
        this.setAdpName(adpName);
    }  
    
    public long getAdpKey()
    {
        return this.adpKey;
    }
    
    public String getAdpName()
    {
        return this.adpName;
    }
    
    public void setAdpKey(long adpKey)
    {
        this.adpKey = adpKey;
    }
    
    public void setAdpName(String adpName)
    {
        this.adpName = adpName;
    }
}
