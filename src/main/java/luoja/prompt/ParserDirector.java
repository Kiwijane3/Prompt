package luoja.prompt;

import android.content.Context;
import android.content.pm.PackageManager;

import java.util.HashMap;
import java.util.Map;

import luoja.prompt.Parsers.CallParser;
import luoja.prompt.Parsers.EmailParser;
import luoja.prompt.Parsers.PreferencesParser;
import luoja.prompt.Parsers.SMSParser;
import luoja.prompt.Parsers.SearchParser;
import luoja.prompt.Parsers.WebParser;

/**
 * #Contract: passes only arguments to the parsers, not the command at the start;
 */
public class ParserDirector {

    protected Context context;

    private Map<String, GenericParser> keywordActionMap;

    public ParserDirector(Context inContext, WebList webList){
        context = inContext;
        keywordActionMap = new HashMap<>();
        keywordActionMap.put(keywordWeb(), new WebParser(context));
        keywordActionMap.put(keywordEmail(), new EmailParser(context));
        keywordActionMap.put(keywordSearch(), new SearchParser(context));
        keywordActionMap.put(keywordPhone(), new CallParser(context));
        keywordActionMap.put(keywordSet(), new PreferencesParser(context, webList));
        keywordActionMap.put(keywordSMS(), new SMSParser(context));
    }

    public ResultBundle accept(String command){
        String[] args = command.split(" ");
        String key = args[0].toUpperCase();
        if (keywordActionMap.keySet().contains(key))
            return keywordActionMap.get(key).accept(subArray(args));
        else return new ResultBundle(false, context.getString(R.string.message_no_such_command)); //Also covers apps and links, as the bundle from the
    }                                                                                             //director is used if Web and App lists return
                                                                                                  //failed bundles.
    private PackageManager packageManager(){
        return context.getPackageManager();
    }

    private String[] subArray(String[] array){
        if (array.length < 2) return new String[0];
        String[] subArray = new String[array.length - 1];
        for (int i = 0 ; i < subArray.length ; i++){
            subArray[i] = array[i + 1];
        }
        return subArray;
    }

    private String keywordWeb(){
        return context.getString(R.string.keyword_web_browser).toUpperCase();
    }

    private String keywordEmail(){
        return context.getString(R.string.keyword_email).toUpperCase();
    }

    private String keywordSearch(){
        return context.getString(R.string.keyword_search).toUpperCase();
    }

    private String keywordPhone(){
        return context.getString(R.string.keyword_phone).toUpperCase();
    }

    private String keywordSet(){
        return context.getString(R.string.keyword_set).toUpperCase();
    }

    private String keywordSMS() { return context.getString(R.string.keyword_sms).toUpperCase(); }

}
