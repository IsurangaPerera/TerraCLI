package org.terra.projects.terracli;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Option implements Cloneable, Serializable {

    public static final int UNINITIALIZED = -1;

    public static final int UNLIMITED_VALUES = -2;

    private static final long serialVersionUID = 1L;

    private final String opt;
    private String longOpt;
    private String argName;
    private String description;
    private boolean isRequired;
    private boolean argOptional;
    private int numOfArgs = UNINITIALIZED;
    private Class<?> type = String.class;
    private List<String> values = new ArrayList<String>();
    private char valueSeperator;

    private Option(Builder builder) {

        this.argName = builder.argName;
        this.description = builder.description;
        this.longOpt = builder.longOpt;
        this.numOfArgs = builder.numOfArgs;
        this.opt = builder.opt;
        this.argOptional = builder.argOptional;
        this.isRequired = builder.isRequired;
        this.type = builder.type;
        this.valueSeperator = builder.valueSeperator;
    }

    public Option(String opt, String description) throws IllegalArgumentException {
        this(opt, null, false, description);
    }

    public Option(String opt, boolean hasArg, String description) throws IllegalArgumentException {
        this(opt, null, hasArg, description);
    }

    public Option(String opt, String longOpt, boolean hasArg, String description)
            throws IllegalArgumentException {

        OptionValidator.validateOption(opt);

        this.opt = opt;
        this.longOpt = longOpt;

        if (hasArg) {
            this.numOfArgs = 1;
        }

        this.description = description;
    }

    public boolean hasArg() {
        return (numOfArgs > 1 || numOfArgs == UNLIMITED_VALUES);
    }

    public boolean hasValueSeperator() {
        return (valueSeperator > 0);
    }

    public int getId() {
        return getKey().charAt(0);
    }

    public String getKey() {
        return (opt == null) ? longOpt : opt;
    }

    public String getOpt() {
        return opt;
    }

    public Object getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public void setLongOpt(String longOpt) {
        this.longOpt = longOpt;
    }

    public String getLongOpt() {
        return this.longOpt;
    }

    public void setArgOptional(boolean argOptional) {
        this.argOptional = argOptional;
    }

    public boolean getArgOptional() {
        return this.argOptional;
    }

    public boolean hasLongOption() {
        return this.longOpt != null;
    }

    public String getDescription() {
        return description;
    }
    
    public boolean hasArgOptional() {
        return argOptional;
    }


    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public void setRequired(boolean required) {
        this.isRequired = required;
    }

    public boolean hasArgName() {
        return argName != null && argName.length() > 0;
    }
    
    public boolean hasArgs()
    {
        return numOfArgs > 1 || numOfArgs == UNLIMITED_VALUES;
    }

    public void setArgName(String argName) {
        this.argName = argName;
    }

    public String getArgName() {
        return argName;
    }

    public void setArgs(int num) {
        this.numOfArgs = num;
    }

    public void setValueSeperator(char seperator) {
        this.valueSeperator = seperator;
    }

    public char getValueSeperator() {
        return valueSeperator;
    }

    void addValueForProcessing(String value) {
        if (numOfArgs == UNINITIALIZED) {
            throw new RuntimeException("NO_ARGS_ALLOWED");
        }

        processValue(value);
    }

    private void processValue(String value) {
        if (hasValueSeperator()) {
            add(value.trim().split(Character.toString(this.valueSeperator)));
        }
    }

    boolean acceptsArg() {
        return (hasArg() || hasArgs() || hasArgOptional()) && (numOfArgs <= 0 || values.size() < numOfArgs);
    }

    private void add(String[] values) {
        if (!acceptsArg()) {
            throw new RuntimeException("Cannot add value, list full.");
        }

        this.values.addAll(Arrays.asList(values));
    }

    public String getValue(String defaultValue) {
        String value = getValue();

        return (value != null) ? value : defaultValue;
    }
    
    public String getValue() {
        return hasNoValues() ? null : values.get(0);
    }

    public String[] getValues() {
        return hasNoValues() ? null : values.toArray(new String[values.size()]);
    }

    public List<String> getValuesList() {
        return values;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder().append("[ option: ");

        buf.append(opt);

        if (longOpt != null) 
            buf.append(" ").append(longOpt);

        buf.append(" ");

        if (hasArgs()) 
            buf.append("[ARG...]");
        else if (hasArg())
            buf.append(" [ARG]");

        buf.append(" :: ").append(description);

        if (type != null) 
            buf.append(" :: ").append(type);

        buf.append(" ]");

        return buf.toString();
    }

    private boolean hasNoValues() {
        return values.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) 
            return true;

        if (o == null || getClass() != o.getClass()) 
            return false;

        Option option = (Option) o;

        if (opt != null ? !opt.equals(option.opt) : option.opt != null) 
            return false;

        return !(longOpt != null ? !longOpt.equals(option.longOpt) : option.longOpt != null);
    }

    @Override
    public int hashCode() {
        int result;
        result = opt != null ? opt.hashCode() : 0;
        result = 31 * result + (longOpt != null ? longOpt.hashCode() : 0);
        return result;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        try {
            Option option = (Option) super.clone();
            option.values = new ArrayList<String>(values);
            return option;
        } catch (CloneNotSupportedException cnse) {
            throw new RuntimeException("A CloneNotSupportedException was thrown: " + cnse.getMessage());
        }
    }

    void clearValues() {
        values.clear();
    }
    
    
    public static final class Builder 
    {
        private final String opt;

        private String description;

        private String longOpt;

        private String argName;

        private boolean isRequired;

        private boolean argOptional;

        private int numOfArgs = UNINITIALIZED;

        private Class<?> type = String.class;

        private char valueSeperator;

        private Builder(final String opt) throws IllegalArgumentException
        {
            OptionValidator.validateOption(opt);
            this.opt = opt;
        }
        
        public Builder argName(final String argName)
        {
            this.argName = argName;
            return this;
        }

        public Builder desc(final String description)
        {
            this.description = description;
            return this;
        }
       
        public Builder longOpt(final String longOpt)
        {
            this.longOpt = longOpt;
            return this;
        }
                
        public Builder numberOfArgs(final int numberOfArgs)
        {
            this.numOfArgs = numberOfArgs;
            return this;
        }
        
        public Builder optionalArg(final boolean isOptional)
        {
            this.argOptional = isOptional;
            return this;
        }
        
        public Builder required()
        {
            return required(true);
        }

        public Builder required(final boolean required)
        {
            this.isRequired = required;
            return this;
        }
        
        public Builder type(final Class<?> type)
        {
            this.type = type;
            return this;
        }

        public Builder valueSeparator()
        {
            return valueSeparator('=');
        }

        public Builder valueSeparator(final char seperator)
        {
            valueSeperator= seperator;
            return this;
        }
        
        public Builder hasArg()
        {
            return hasArg(true);
        }

        public Builder hasArg(final boolean hasArg)
        {
            numOfArgs = hasArg ? 1 : Option.UNINITIALIZED;
            return this;
        }

        public Builder hasArgs()
        {
            numOfArgs = Option.UNLIMITED_VALUES;
            return this;
        }

        public Option build()
        {
            if (opt == null && longOpt == null)
            {
                throw new IllegalArgumentException("Either opt or longOpt must be specified");
            }
            return new Option(this);
        }
    }
}
