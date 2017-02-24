package org.terra.projects.terracli;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class OptionGroup implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private final Map<String, Option> optionMap = new LinkedHashMap<String, Option>();
	
	private String selected;
	
	private boolean required;
	
	public OptionGroup addOption(Option option) {
        optionMap.put(option.getKey(), option);

        return this;
    }
	
	public Collection<String> getNames() {
        return optionMap.keySet();
    }
	
	public Collection<Option> getOptions() {
        return optionMap.values();
    }
	
	public void setSelected(Option option) throws AlreadySelectedException {
        if (option == null) {
            selected = null;
            return;
        }
        
        if (selected == null || selected.equals(option.getKey()))
            selected = option.getKey();

        else
            throw new AlreadySelectedException(this, option);
    }
	
	public String getSelected() {
        return selected;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isRequired() {
        return required;
    }
    
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();
        
        Iterator<Option> iter = getOptions().iterator();

        buff.append("[");

        while (iter.hasNext()) {
            Option option = iter.next();

            if (option.getOpt() != null) {
                buff.append("-");
                buff.append(option.getOpt());
            }
            else {
                buff.append("--");
                buff.append(option.getLongOpt());
            }
            
            if (option.getDescription() != null) {
                buff.append(" ");
                buff.append(option.getDescription());
            }
            
            if (iter.hasNext())
                buff.append(", ");
        }

        buff.append("]");

        return buff.toString();
    }

}
