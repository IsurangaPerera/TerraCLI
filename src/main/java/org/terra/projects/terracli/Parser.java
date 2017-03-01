package org.terra.projects.terracli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;

public abstract class Parser implements CommandLineParser
{
    /** commandline instance */
    protected CommandLine cmd;

    /** current Options */
    private Options options;

    /** list of required options strings */
    private List requiredOptions;

    protected void setOptions(Options options)
    {
        this.options = options;
        this.requiredOptions = new ArrayList(options.getRequiredOptions());
    }

    protected Options getOptions()
    {
        return options;
    }

    protected List getRequiredOptions()
    {
        return requiredOptions;
    }

    protected abstract String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption)
            throws ParseException;

    public CommandLine parse(Options options, String[] arguments) throws ParseException
    {
        return parse(options, arguments, null, false);
    }

    public CommandLine parse(Options options, String[] arguments, Properties properties) throws ParseException
    {
        return parse(options, arguments, properties, false);
    }

    public CommandLine parse(Options options, String[] arguments, boolean stopAtNonOption) throws ParseException
    {
        return parse(options, arguments, null, stopAtNonOption);
    }

    public CommandLine parse(Options options, String[] arguments, Properties properties, boolean stopAtNonOption)
            throws ParseException
    {
        // clear out the data in options in case it's been used before (CLI-71)
        for (Option opt : options.helpOptions())
        {
            opt.clearValues();
        }
        
        // clear the data from the groups
        for (OptionGroup group : options.getOptionGroups())
        {
            group.setSelected(null);
        }        

        // initialise members
        setOptions(options);

        cmd = new CommandLine();

        boolean eatTheRest = false;

        if (arguments == null)
        {
            arguments = new String[0];
        }

        List<String> tokenList = Arrays.asList(flatten(getOptions(), arguments, stopAtNonOption));

        ListIterator<String> iterator = tokenList.listIterator();

        // process each flattened token
        while (iterator.hasNext())
        {
            String t = iterator.next();

            // the value is the double-dash
            if ("--".equals(t))
            {
                eatTheRest = true;
            }

            // the value is a single dash
            else if ("-".equals(t))
            {
                if (stopAtNonOption)
                {
                    eatTheRest = true;
                }
                else
                {
                    cmd.addArg(t);
                }
            }

            // the value is an option
            else if (t.startsWith("-"))
            {
                if (stopAtNonOption && !getOptions().hasOption(t))
                {
                    eatTheRest = true;
                    cmd.addArg(t);
                }
                else
                {
                    processOption(t, iterator);
                }
            }

            // the value is an argument
            else
            {
                cmd.addArg(t);

                if (stopAtNonOption)
                {
                    eatTheRest = true;
                }
            }

            // eat the remaining tokens
            if (eatTheRest)
            {
                while (iterator.hasNext())
                {
                    String str = iterator.next();

                    // ensure only one double-dash is added
                    if (!"--".equals(str))
                    {
                        cmd.addArg(str);
                    }
                }
            }
        }

        processProperties(properties);
        checkRequiredOptions();

        return cmd;
    }

    protected void processProperties(Properties properties) throws ParseException
    {
        if (properties == null)
        {
            return;
        }

        for (Enumeration<?> e = properties.propertyNames(); e.hasMoreElements();)
        {
            String option = e.nextElement().toString();
            
            Option opt = options.getOption(option);
            if (opt == null)
            {
                throw new UnrecognizedOptionException("Default option wasn't defined", option);
            }
            
            // if the option is part of a group, check if another option of the group has been selected
            OptionGroup group = options.getOptionGroup(opt);
            boolean selected = group != null && group.getSelected() != null;
            
            if (!cmd.hasOption(option) && !selected)
            {
                // get the value from the properties instance
                String value = properties.getProperty(option);

                if (opt.hasArg())
                {
                    if (opt.getValues() == null || opt.getValues().length == 0)
                    {
                        try
                        {
                            opt.addValueForProcessing(value);
                        }
                        catch (RuntimeException exp) //NOPMD
                        {
                            // if we cannot add the value don't worry about it
                        }
                    }
                }
                else if (!("yes".equalsIgnoreCase(value)
                        || "true".equalsIgnoreCase(value)
                        || "1".equalsIgnoreCase(value)))
                {
                    // if the value is not yes, true or 1 then don't add the
                    // option to the CommandLine
                    continue;
                }

                cmd.addOption(opt);
                updateRequiredOptions(opt);
            }
        }
    }

    protected void checkRequiredOptions() throws MissingOptionException
    {
        // if there are required options that have not been processed
        if (!getRequiredOptions().isEmpty())
        {
            throw new MissingOptionException(getRequiredOptions());
        }
    }

    public void processArgs(Option opt, ListIterator<String> iter) throws ParseException
    {
        // loop until an option is found
        while (iter.hasNext())
        {
            String str = iter.next();
            
            // found an Option, not an argument
            if (getOptions().hasOption(str) && str.startsWith("-"))
            {
                iter.previous();
                break;
            }

            // found a value
            try
            {
                opt.addValueForProcessing(Util.stripLeadingAndTrailingQuotes(str));
            }
            catch (RuntimeException exp)
            {
                iter.previous();
                break;
            }
        }

        if (opt.getValues() == null && !opt.hasOptionalArg())
        {
            throw new MissingArgumentException(opt);
        }
    }

    protected void processOption(String arg, ListIterator<String> iter) throws ParseException
    {
        boolean hasOption = getOptions().hasOption(arg);

        // if there is no option throw an UnrecognizedOptionException
        if (!hasOption)
        {
            throw new UnrecognizedOptionException("Unrecognized option: " + arg, arg);
        }

        // get the option represented by arg
        Option opt = (Option) getOptions().getOption(arg).clone();
        
        // update the required options and groups
        updateRequiredOptions(opt);
        
        // if the option takes an argument value
        if (opt.hasArg())
        {
            processArgs(opt, iter);
        }
        
        // set the option on the command line
        cmd.addOption(opt);
    }

    private void updateRequiredOptions(Option opt) throws ParseException
    {
        // if the option is a required option remove the option from
        // the requiredOptions list
        if (opt.isRequired())
        {
            getRequiredOptions().remove(opt.getKey());
        }

        // if the option is in an OptionGroup make that option the selected
        // option of the group
        if (getOptions().getOptionGroup(opt) != null)
        {
            OptionGroup group = getOptions().getOptionGroup(opt);

            if (group.isRequired())
            {
                getRequiredOptions().remove(group);
            }

            group.setSelected(opt);
        }
    }
}
