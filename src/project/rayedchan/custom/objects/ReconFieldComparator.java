package project.rayedchan.custom.objects;

import java.util.Comparator;

/**
 * @author rayedchan
 * A Comparator class for comparing the reconFieldName of two ReconFieldAndFormFieldMap objects.
 */
public class ReconFieldComparator implements Comparator<ReconFieldAndFormFieldMap> 
{
    @Override
    public int compare(ReconFieldAndFormFieldMap obj1, ReconFieldAndFormFieldMap obj2) 
    {
        return obj1.getReconFieldName().compareTo(obj2.getReconFieldName());
    }
}
    

