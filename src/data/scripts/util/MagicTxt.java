/*
By Tartiflette
 */
package data.scripts.util;

import com.fs.starfarer.api.Global;

public class MagicTxt {   
    private static final String ML="magicLib";    
    
    public static String getString(String id){
        return Global.getSettings().getString(ML, id);
    }   
    
    public static String nullStringIfEmpty(String input) {
        return input != null && !input.isEmpty() ? input : null;
    }    
}