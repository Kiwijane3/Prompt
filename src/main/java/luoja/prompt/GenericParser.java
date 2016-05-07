package luoja.prompt;

import luoja.prompt.R;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import java.util.Date;

/**
 * Parser should receive an array starting with the option and omitting the command;
 */
public class GenericParser {

    protected Context context;

    public GenericParser(Context inContext){
        context = inContext;
    }

    public ResultBundle accept(String[] args){
        if (args.length > 0 && premium())
            return parse(args); //By default, parsers can only accept arguments in parse premium;
        else
            return needUpgradeBundle();
    }

    protected ResultBundle parse(String[] args){
        return new ResultBundle(false, context.getString(R.string.message_generic_instantiation));
    }

    protected void launch(Intent intent){
        context.startActivity(intent);
    }

    /**
     * Checks if premium features are available; i.e, has the app been installed
     * for less than 30 days, or has Prompt premium been purchased.
     * @return
     */
    protected boolean premium(){
        SharedPreferences sharedPreferences = context.getSharedPreferences(Val.PREFERENCES_NAME, 0);
        if (!sharedPreferences.getBoolean(Val.KEY_PREMIUM, false)) {
            long now = new Date().getTime();
            long endTrial = sharedPreferences.getLong(Val.KEY_TRIAL_END, now);
            if (now > endTrial) return false;
        }
        return true;
    }

    protected PackageManager packageManager(){
        return context.getPackageManager();
    }

    protected ResultBundle needUpgradeBundle(){
        return new ResultBundle(false, context.getString(R.string.message_upgrade_needed));
    }

    /**If end is zero, the subArray will contain all elements after start;
     * Range is inclusive.
     * @param inArray
     * @param start
     * @param end
     * @return
     */
    protected String[] subArray(String[] inArray, int start, int end){
        if (end <= 0) end = inArray.length - 1;
        String[] outArray = new String[end - (start - 1)];
        for (int i = start ; i<= end; i++){
            outArray[i - start] = inArray[i];
        }
        return outArray;
    }

    protected String concatenate(String[] args){
        String out = "";
        for (String arg : args){
            out = out + arg + " ";
        }
        return out;
    }

    protected ResultBundle success(){
        return new ResultBundle(true, context.getString(R.string.message_generic_ok));
    }

    /**Launches the app that resolves the intent by default. If there is no default chosen, shows the chooser.
     * The chooser will just launch the app according to the intent.
     */
    protected void launchAppFromIntent(Intent intent){
        PackageManager packageManager = packageManager();
        //Find the default app;
        ResolveInfo defaultApp = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        String packageName = defaultApp.activityInfo.packageName;
        Intent introIntent = packageManager.getLaunchIntentForPackage(packageName); //To the homescreen of the activity, or re-open;
        if (introIntent == null){ //Prevent a launch from a null intent;
            launch(intent);
        } else {
            launch(introIntent); //Launch the intent if the default app could not be launched to start page;
        }
    }

}
