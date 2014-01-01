package project.rayedchan.custom.objects;

import java.util.HashMap;

/**
 * @author rayedchan
 */
public class ProcessTaskInfo 
{
    private HashMap adapterVarMapping;
    private String adapterName;
    private String eventHandlerName;
    private String processDefName;
    private String resourceObjectName;
    private String processTaskName;
    private String tosId;
    private String adapterReturnValueAdapterVarName;
    private String processInstanceKeyAdapterVarName;
    private String itResourceAdapterVarName;
    private String objectTypeAdapterVarName;
    private String attributeFieldNameAdapterVarName;
    
    public ProcessTaskInfo(String processTaskName, String processDefName, String resourceObjectName, String adapterName, String eventHandlerName, HashMap adapterVarMapping, String tosId, String adapterReturnValueAdapterVarName, String processInstanceKeyAdapterVarName, String itResourceAdapterVarName, String objectTypeAdapterVarName, String attributeFieldNameAdapterVarName)
    {
        this.setAdapterVarMapping(adapterVarMapping);
        this.setAdapterName(adapterName);
        this.setEventHandlerName(eventHandlerName);
        this.setProcessDefName(processDefName);
        this.setResourceObjectName(resourceObjectName);
        this.setProcessTaskName(processTaskName);
        this.setTosId(tosId);
        this.setAdapterReturnValueAdapterVarName(adapterReturnValueAdapterVarName);
        this.setProcessInstanceKeyAdapterVarName(processInstanceKeyAdapterVarName);
        this.setItResourceAdapterVarName(itResourceAdapterVarName);
        this.setObjectTypeAdapterVarName(objectTypeAdapterVarName);
        this.setAttributeFieldNameAdapterVarName(attributeFieldNameAdapterVarName);
    }
    
    public String getAttributeFieldNameAdapterVarName()
    {
        return this.attributeFieldNameAdapterVarName; 
    }
    
    public void setAttributeFieldNameAdapterVarName(String attributeFieldNameAdapterVarName)
    {
        this.attributeFieldNameAdapterVarName = attributeFieldNameAdapterVarName;
    }
    
    public String getObjectTypeAdapterVarName()
    {
        return this.objectTypeAdapterVarName; 
    }
    
    public void setObjectTypeAdapterVarName(String objectTypeAdapterVarName)
    {
        this.objectTypeAdapterVarName = objectTypeAdapterVarName;
    }
    
    public String getItResourceAdapterVarName()
    {
        return this.itResourceAdapterVarName; 
    }
    
    public void setItResourceAdapterVarName(String itResourceAdapterVarName)
    {
        this.itResourceAdapterVarName = itResourceAdapterVarName;
    }
    
    public HashMap getAdapterVarMapping()
    {
        return this.adapterVarMapping; 
    }
    
    public void setAdapterVarMapping(HashMap adapterVarMapping)
    {
        this.adapterVarMapping = adapterVarMapping;
    }
    
    public String getAdapterName()
    {
        return this.adapterName; 
    }
    
    public void setAdapterName(String adapterName)
    {
        this.adapterName = adapterName;
    }
    
    public String getEventHandlerName()
    {
        return this.eventHandlerName; 
    }
    
    public void setEventHandlerName(String eventHandlerName)
    {
        this.eventHandlerName = eventHandlerName;
    }
    
    public String getProcessDefName()
    {
        return this.processDefName; 
    }
    
    public void setProcessDefName(String processDefName)
    {
        this.processDefName = processDefName;
    }
    
    public String getResourceObjectName()
    {
        return this.resourceObjectName; 
    }
    
    public void setResourceObjectName(String resourceObjectName)
    {
        this.resourceObjectName = resourceObjectName;
    }
        
    public String getProcessTaskName()
    {
        return this.processTaskName; 
    }
    
    public void setProcessTaskName(String processTaskName)
    {
        this.processTaskName = processTaskName;
    }
    
    public String getTosId()
    {
        return this.tosId; 
    }
    
    public void setTosId(String tosId)
    {
        this.tosId = tosId;
    }
    
    public String getAdapterReturnValueAdapterVarName()
    {
        return this.adapterReturnValueAdapterVarName; 
    }
    
    public void setAdapterReturnValueAdapterVarName(String adapterReturnValueAdapterVarName)
    {
        this.adapterReturnValueAdapterVarName = adapterReturnValueAdapterVarName;
    }
     
    public String getProcessInstanceKeyAdapterVarName()
    {
        return this.processInstanceKeyAdapterVarName; 
    }
    
    public void setProcessInstanceKeyAdapterVarName(String processInstanceKeyAdapterVarName)
    {
        this.processInstanceKeyAdapterVarName = processInstanceKeyAdapterVarName;
    }
      
    /*
     * 
     * String representation of the object. Values of the object fields are included in 
     * the String.
     */
    @Override
    public String toString()
    {
        return String.format(
        "Process Task Name: %s\n Process Defintion Name: %s\n Resource Object Name: %s\n Adapter Name: %s\n Event Handler: %s\n Adapter Variable Mapping: %s\n Tos Id Label: %s\n Return Value Var Name: %s\n Process Instance Key Var Name: %s\n IT Resource Var Name: %s\n Object Type Var Name: %s\n Attribute Field Namwe: %s\n", 
        this.processTaskName, this.processDefName, this.resourceObjectName, this.adapterName, this.eventHandlerName, 
        this.adapterVarMapping,this.tosId, this.adapterReturnValueAdapterVarName, this.processInstanceKeyAdapterVarName, this.itResourceAdapterVarName,
        this.objectTypeAdapterVarName, this.attributeFieldNameAdapterVarName);
    }
}
