package project.rayedchan.custom.objects;

import java.util.Comparator;

/**
 * @author rayedchan
 * A Comparator class for comparing the formFieldColumnName of two ReconFieldAndFormFieldMap objects.
 */
public class FormFieldColumnNameComparator implements Comparator<ReconFieldAndFormFieldMap> 
{
    @Override
    public int compare(ReconFieldAndFormFieldMap obj1, ReconFieldAndFormFieldMap obj2) 
    {
        return obj1.getFormFieldColumnName().compareTo(obj2.getFormFieldColumnName());
    }
}