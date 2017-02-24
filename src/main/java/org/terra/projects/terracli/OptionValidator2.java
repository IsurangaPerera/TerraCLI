package org.terra.projects.terracli;

/**
 *
 * @author isurangaperera
 */
final class OptionValidator2 {

    static void validateOption(String option)throws IllegalArgumentException {
        if(option == null) return;
        
        if (option.length() == 1) {
            char ch = option.charAt(0);
            if (!isValidOpt(ch))
                throw new IllegalArgumentException("Illegal option name '" + ch + "'");
        }
       
        else
        	for (char ch : option.toCharArray())
                if (!isValidChar(ch))
                    throw new IllegalArgumentException("The option '" + option + "' contains an illegal "
                                                       + "character : '" + ch + "'");
    }
    
    private static boolean isValidOpt(char c) {
        return isValidChar(c) || c == '?' || c == '@';
    }
    
    private static boolean isValidChar(char c) {
        return Character.isJavaIdentifierPart(c);
    }
    
}
