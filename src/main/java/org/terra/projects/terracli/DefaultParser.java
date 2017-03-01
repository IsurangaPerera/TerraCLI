package org.terra.projects.terracli;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

public class DefaultParser implements CommandLineParser
{*/
    protected CommandLine cmd;
    
    protected Options options;

    protected boolean stopAtNonOption;

    protected String currentToken;
 
    protected Option currentOption;
 
    protected boolean skipParsing;
 
    protected List expectedOpts;
 
    public CommandLine parse(Options options, String[] arguments) throws ParseException
    {
        return parse(options, arguments, null);
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
        this.options = options;
        this.stopAtNonOption = stopAtNonOption;
        skipParsing = false;
        currentOption = null;
        expectedOpts = new ArrayList(options.getRequiredOptions());

        // clear the data from the groups
        for (OptionGroup group : options.getOptionGroups())
        {
            group.setSelected(null);
        }

        cmd = new CommandLine();

        if (arguments != null)
        {
            for (String argument : arguments)
            {
                handleToken(argument);
            }
        }

        // check the arguments of the last option
        checkRequiredArgs();

        // add the default options
        handleProperties(properties);

        checkRequiredOptions();

        return cmd;
    }

    private void handleProperties(Properties properties) throws ParseException
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
                // get the value from the properties
                String value = properties.getProperty(option);

                if (opt.hasArg())
                {
                    if (opt.getValues() == null || opt.getValues().length == 0)
                    {
                        opt.addValueForProcessing(value);
                    }
                }
                else if (!("yes".equalsIgnoreCase(value)
                        || "true".equalsIgnoreCase(value)
                        || "1".equalsIgnoreCase(value)))
                {
                    // if the value is not yes, true or 1 then don't add the option to the CommandLine
                    continue;
                }

                handleOption(opt);
                currentOption = null;
            }
        }
    }

    /**
     * Throws a {@link MissingOptionException} if all of the required options
     * are not present.
     *
     * @throws MissingOptionException if any of the required Options
     * are not present.
     */
    private void checkRequiredOptions() throws MissingOptionException
    {
        // if there are required options that have not been processed
        if (!expectedOpts.isEmpty())
        {
            throw new MissingOptionException(expectedOpts);
        }
    }

    private void checkRequiredArgs() throws ParseException
    {
        if (currentOption != null && currentOption.requiresArg())
        {
            throw new MissingArgumentException(currentOption);
        }
    }

    private void handleToken(String token) throws ParseException
    {
        currentToken = token;

        if (skipParsing)
        {
            cmd.addArg(token);
        }
        else if ("--".equals(token))
        {
            skipParsing = true;
        }
        else if (currentOption != null && currentOption.acceptsArg() && isArgument(token))
        {
            currentOption.addValueForProcessing(Util.stripLeadingAndTrailingQuotes(token));
        }
        else if (token.startsWith("--"))
        {
            handleLongOption(token);
        }
        else if (token.startsWith("-") && !"-".equals(token))
        {
            handleShortAndLongOption(token);
        }
        else
        {
            handleUnknownToken(token);
        }

        if (currentOption != null && !currentOption.acceptsArg())
        {
            currentOption = null;
        }
    }

    private boolean isArgument(String token)
    {
        return !isOption(token) || isNegativeNumber(token);
    }

    private boolean isNegativeNumber(String token)
    {
        try
        {
            Double.parseDouble(token);
            return true;
        }
        catch (NumberFormatException e)
        {
            return false;
        }
    }

    private boolean isOption(String token)
    {
        return isLongOption(token) || isShortOption(token);
    }

    private boolean isShortOption(String token)
    {
        // short options (-S, -SV, -S=V, -SV1=V2, -S1S2)
        if (!token.startsWith("-") || token.length() == 1)
        {
            return false;
        }

        // remove leading "-" and "=value"
        int pos = token.indexOf("=");
        String optName = pos == -1 ? token.substring(1) : token.substring(1, pos);
        if (options.hasShortOption(optName))
        {
            return true;
        }
        // check for several concatenated short options
        return optName.length() > 0 && options.hasShortOption(String.valueOf(optName.charAt(0)));
    }

    private boolean isLongOption(String token)
    {
        if (!token.startsWith("-") || token.length() == 1)
        {
            return false;
        }

        int pos = token.indexOf("=");
        String t = pos == -1 ? token : token.substring(0, pos);

        if (!options.getMatchingOptions(t).isEmpty())
        {
            // long or partial long options (--L, -L, --L=V, -L=V, --l, --l=V)
            return true;
        }
        else if (getLongPrefix(token) != null && !token.startsWith("--"))
        {
            // -LV
            return true;
        }

        return false;
    }

    private void handleUnknownToken(String token) throws ParseException
    {
        if (token.startsWith("-") && token.length() > 1 && !stopAtNonOption)
        {
            throw new UnrecognizedOptionException("Unrecognized option: " + token, token);
        }

        cmd.addArg(token);
        if (stopAtNonOption)
        {
            skipParsing = true;
        }
    }

    private void handleLongOption(String token) throws ParseException
    {
        if (token.indexOf('=') == -1)
        {
            handleLongOptionWithoutEqual(token);
        }
        else
        {
            handleLongOptionWithEqual(token);
        }
    }

    private void handleLongOptionWithoutEqual(String token) throws ParseException
    {
        List<String> matchingOpts = options.getMatchingOptions(token);
        if (matchingOpts.isEmpty())
        {
            handleUnknownToken(currentToken);
        }
        else if (matchingOpts.size() > 1)
        {
            throw new AmbiguousOptionException(token, matchingOpts);
        }
        else
        {
            handleOption(options.getOption(matchingOpts.get(0)));
        }
    }

    private void handleLongOptionWithEqual(String token) throws ParseException
    {
        int pos = token.indexOf('=');

        String value = token.substring(pos + 1);

        String opt = token.substring(0, pos);

        List<String> matchingOpts = options.getMatchingOptions(opt);
        if (matchingOpts.isEmpty())
        {
            handleUnknownToken(currentToken);
        }
        else if (matchingOpts.size() > 1)
        {
            throw new AmbiguousOptionException(opt, matchingOpts);
        }
        else
        {
            Option option = options.getOption(matchingOpts.get(0));

            if (option.acceptsArg())
            {
                handleOption(option);
                currentOption.addValueForProcessing(value);
                currentOption = null;
            }
            else
            {
                handleUnknownToken(currentToken);
            }
        }
    }

    private void handleShortAndLongOption(String token) throws ParseException
    {
        String t = Util.stripLeadingHyphens(token);

        int pos = t.indexOf('=');

        if (t.length() == 1)
        {
            // -S
            if (options.hasShortOption(t))
            {
                handleOption(options.getOption(t));
            }
            else
            {
                handleUnknownToken(token);
            }
        }
        else if (pos == -1)
        {
            // no equal sign found (-xxx)
            if (options.hasShortOption(t))
            {
                handleOption(options.getOption(t));
            }
            else if (!options.getMatchingOptions(t).isEmpty())
            {
                // -L or -l
                handleLongOptionWithoutEqual(token);
            }
            else
            {
                // look for a long prefix (-Xmx512m)
                String opt = getLongPrefix(t);

                if (opt != null && options.getOption(opt).acceptsArg())
                {
                    handleOption(options.getOption(opt));
                    currentOption.addValueForProcessing(t.substring(opt.length()));
                    currentOption = null;
                }
                else if (isJavaProperty(t))
                {
                    // -SV1 (-Dflag)
                    handleOption(options.getOption(t.substring(0, 1)));
                    currentOption.addValueForProcessing(t.substring(1));
                    currentOption = null;
                }
                else
                {
                    // -S1S2S3 or -S1S2V
                    handleConcatenatedOptions(token);
                }
            }
        }
        else
        {
            // equal sign found (-xxx=yyy)
            String opt = t.substring(0, pos);
            String value = t.substring(pos + 1);

            if (opt.length() == 1)
            {
                // -S=V
                Option option = options.getOption(opt);
                if (option != null && option.acceptsArg())
                {
                    handleOption(option);
                    currentOption.addValueForProcessing(value);
                    currentOption = null;
                }
                else
                {
                    handleUnknownToken(token);
                }
            }
            else if (isJavaProperty(opt))
            {
                // -SV1=V2 (-Dkey=value)
                handleOption(options.getOption(opt.substring(0, 1)));
                currentOption.addValueForProcessing(opt.substring(1));
                currentOption.addValueForProcessing(value);
                currentOption = null;
            }
            else
            {
                // -L=V or -l=V
                handleLongOptionWithEqual(token);
            }
        }
    }

    private String getLongPrefix(String token)
    {
        String t = Util.stripLeadingHyphens(token);

        int i;
        String opt = null;
        for (i = t.length() - 2; i > 1; i--)
        {
            String prefix = t.substring(0, i);
            if (options.hasLongOption(prefix))
            {
                opt = prefix;
                break;
            }
        }
        
        return opt;
    }

    private boolean isJavaProperty(String token)
    {
        String opt = token.substring(0, 1);
        Option option = options.getOption(opt);

        return option != null && (option.getArgs() >= 2 || option.getArgs() == Option.UNLIMITED_VALUES);
    }

    private void handleOption(Option option) throws ParseException
    {
        // check the previous option before handling the next one
        checkRequiredArgs();

        option = (Option) option.clone();

        updateRequiredOptions(option);

        cmd.addOption(option);

        if (option.hasArg())
        {
            currentOption = option;
        }
        else
        {
            currentOption = null;
        }
    }

    private void updateRequiredOptions(Option option) throws AlreadySelectedException
    {
        if (option.isRequired())
        {
            expectedOpts.remove(option.getKey());
        }

        // if the option is in an OptionGroup make that option the selected option of the group
        if (options.getOptionGroup(option) != null)
        {
            OptionGroup group = options.getOptionGroup(option);

            if (group.isRequired())
            {
                expectedOpts.remove(group);
            }

            group.setSelected(option);
        }
    }

    protected void handleConcatenatedOptions(String token) throws ParseException
    {
        for (int i = 1; i < token.length(); i++)
        {
            String ch = String.valueOf(token.charAt(i));

            if (options.hasOption(ch))
            {
                handleOption(options.getOption(ch));

                if (currentOption != null && token.length() != i + 1)
                {
                    // add the trail as an argument of the option
                    currentOption.addValueForProcessing(token.substring(i + 1));
                    break;
                }
            }
            else
            {
                handleUnknownToken(stopAtNonOption && i > 1 ? token.substring(i) : token);
                break;
            }
        }
    }
}