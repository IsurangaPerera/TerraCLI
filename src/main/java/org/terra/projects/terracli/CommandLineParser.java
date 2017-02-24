package org.terra.projects.terracli;

/**
 *
 * @author isurangaperera
 */
public interface CommandLineParser {
    
    CommandLine parse(Options options, String[] arguements)throws ParseException;
    
    CommandLine parse(Options options, String[] arguements, boolean noInterruptOption)throws ParseException;
}
