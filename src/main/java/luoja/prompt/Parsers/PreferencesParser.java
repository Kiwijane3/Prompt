package luoja.prompt.Parsers;

import android.content.Context;
import android.content.SharedPreferences;

import luoja.prompt.GenericParser;
import luoja.prompt.R;
import luoja.prompt.ResultBundle;
import luoja.prompt.Val;
import luoja.prompt.WebList;

/**
 * Created by Jane on 28/02/16.
 */
public class PreferencesParser extends GenericParser {

    private WebList webList;

    public PreferencesParser(Context inContext, WebList inWebList) {
        super(inContext);
        webList = inWebList;
    }

    @Override
    public ResultBundle accept(String[] args){
        return parse(args);
    }

    @Override
    protected ResultBundle parse(String[] args) {
        if (args.length > 1) {
            String option = args[0].toUpperCase();
            if (option.equals(nameOption())){
                editor().putString(Val.KEY_NAME, concatenate(subArray(args, 1, 0))).commit();
                return success();
            } else if (option.equals(webLinkOption())){
                if (args.length < 3)
                    return new ResultBundle(false, context.getString(R.string.message_insufficient_arguments));
                else {
                    webList.register(args[1], args[2]);
                    return new ResultBundle(true, context.getString(R.string.message_weblink_registered, args[1]));
                }
            } else if (option.equals(measurementSystemOption())){
                if (args.length < 2)
                    return new ResultBundle(false, context.getString(R.string.message_system_not_specified));
                else {
                    int val;
                    String parameter = args[1].toUpperCase();
                    if (parameter.equals(context.getString(R.string.parameter_imp)))
                        val = 1;
                    else if (parameter.equals(context.getString(R.string.parameter_metric)))
                        val = 0;
                    else if (parameter.equals(context.getString(R.string.parameter_si)))
                        val = 0;
                    else
                        return new ResultBundle(false, context.getString(R.string.message_system_not_specified));
                    //If option recognised:
                    editor().putInt(Val.KEY_MEASUREMENT_SYSTEM, val).commit();
                    return new ResultBundle(true, context.getString(R.string.message_system_change_success));
                }
            }
            return new ResultBundle(false, "Option not recognised" + System.getProperty("line.separator"));
        } else return new ResultBundle(false, "Not enough arguments" + System.getProperty("line.separator"));
    }

    protected String nameOption(){
        return context.getString(R.string.option_name).toUpperCase();
    }

    protected String webLinkOption(){
        return context.getString(R.string.option_weblink).toUpperCase();
    }

    protected String measurementSystemOption(){
        return context.getString(R.string.option_measurement_system).toUpperCase();
    }

    protected SharedPreferences.Editor editor(){
        return context.getSharedPreferences(Val.PREFERENCES_NAME, 0).edit();
    }



}
