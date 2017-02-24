package org.terra.projects.terracli;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class Options implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private final Map<String, Option> longOptions = new LinkedHashMap<String, Option>();
	private final Map<String, Option> shortOptions = new LinkedHashMap<String, Option>();
	
	private final List<Object> requiredOpts = new ArrayList<Object>();

    private final Map<String, OptionGroup> optionGroups = new LinkedHashMap<String, OptionGroup>();
	
    public Options addOption(String option, String description) {
        addOption(option, null, false, description);
        return this;
    }
    
    public Options addOption(String opt, boolean hasArg, String description) {
        addOption(opt, null, hasArg, description);
        return this;
    }
    
    public Options addOption(String opt, String longOpt, boolean hasArg, String description) {
        addOption(new Option(opt, longOpt, hasArg, description));
        return this;
    }
    
    public Options addRequiredOption(String opt, String longOpt, boolean hasArg, String description) {
        Option option = new Option(opt, longOpt, hasArg, description);
        option.setRequired(true);
        addOption(option);
        return this;
    }
    
    public Options addOption(Option opt) {
        String key = opt.getKey();

        if (opt.hasLongOption())
            longOptions.put(opt.getLongOpt(), opt);

        if (opt.isRequired()) {
            if (requiredOpts.contains(key))
                requiredOpts.remove(requiredOpts.indexOf(key));
            requiredOpts.add(key);
        }

        shortOptions.put(key, opt);

        return this;
    }
    
    public Collection<Option> getOptions() {
        return Collections.unmodifiableCollection(helpOptions());
    }
    
    List<Option> helpOptions() {
        return new ArrayList<Option>(shortOptions.values());
    }

    public List<Object> getRequiredOptions() {
        return Collections.unmodifiableList(requiredOpts);
    }
    
    public Option getOption(String opt) {
        opt = stripLeadingHyphens(opt);

        if (shortOptions.containsKey(opt))
        {
            return shortOptions.get(opt);
        }

        return longOptions.get(opt);
    }
    
    public List<String> getMatchingOptions(String opt)
    {
        opt = stripLeadingHyphens(opt);
        
        List<String> matchingOpts = new ArrayList<String>();

        if (longOptions.keySet().contains(opt))
            return Collections.singletonList(opt);

        for (String longOpt : longOptions.keySet())
            if (longOpt.startsWith(opt))
                matchingOpts.add(longOpt);
        
        return matchingOpts;
    }
    
    public boolean hasOption(String opt) {
        opt = stripLeadingHyphens(opt);

        return shortOptions.containsKey(opt) || longOptions.containsKey(opt);
    }

    public boolean hasLongOption(String opt) {
        opt = stripLeadingHyphens(opt);

        return longOptions.containsKey(opt);
    }
    
    public boolean hasShortOption(String opt) {
        opt = stripLeadingHyphens(opt);

        return shortOptions.containsKey(opt);
    }

    public OptionGroup getOptionGroup(Option opt) {
        return optionGroups.get(opt.getKey());
    }
    
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        buf.append("[ Options: [ short ");
        buf.append(shortOptions.toString());
        buf.append(" ] [ long ");
        buf.append(longOptions);
        buf.append(" ]");

        return buf.toString();
    }
    
    private String stripLeadingHyphens(String str) {
        if (str == null)
            return null;

        if (str.startsWith("--"))
            return str.substring(2, str.length());

        else if (str.startsWith("-"))
            return str.substring(1, str.length());

        return str;
    }
    
    public Options addOptionGroup(OptionGroup group) {
        if (group.isRequired())
            requiredOpts.add(group);

        for (Option option : group.getOptions()) {
            option.setRequired(false);
            addOption(option);

            optionGroups.put(option.getKey(), group);
        }

        return this;
    }
    
    Collection<OptionGroup> getOptionGroups() {
        return new HashSet<OptionGroup>(optionGroups.values());
    }
}  
