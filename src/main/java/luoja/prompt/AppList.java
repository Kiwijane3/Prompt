package luoja.prompt;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Jane on 12/02/16.
 */
public class AppList {

    /**
     * Remember to convert all labels to uppercase to prevent case-sensitivity errors.
     */

    private Map<String, String> namePackageMap; //Label to package name;

    private Context context;

    public AppList(Context inContext){
        context = inContext;
        namePackageMap = new HashMap<>();
        PackageManager packageManager = context.getPackageManager();
        Intent intent  = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps = packageManager.queryIntentActivities(intent, 0);
        for(ResolveInfo app : apps){
            String label = (String) app.loadLabel(packageManager);
            label = label.toUpperCase();
            String packageName = app.activityInfo.packageName;
            namePackageMap.put(label, packageName);
        }
    }

    public AppList(Context inContext, Map inMap){
        context = inContext;
        namePackageMap = inMap;
    }

    /**
     * Attempts to launch an app with a provided label
     * Returns true if such an app is found, and false otherwise.
     * @param label
     * @return
     */
    public ResultBundle launch(String label){
        label = label.toUpperCase();
        if (namePackageMap.keySet().contains(label)){
            String packageName = namePackageMap.get(label);
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            context.startActivity(intent);
            return new ResultBundle(true, context.getString(R.string.message_generic_ok));
        } else return new ResultBundle(false, "");
    }

    /**
     * Check if any new apps have been added since the AppList was created.
     */
    public void doBackgroundUpdate(){
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                PackageManager packageManager = context.getPackageManager();
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                List<ResolveInfo> apps = packageManager.queryIntentActivities(intent, 0);
                for (ResolveInfo app : apps)
                {
                    String label = (String) app.loadLabel(packageManager);
                    label = label.toUpperCase();
                    if (namePackageMap.containsKey(label)) {
                        String packageName = app.activityInfo.packageName;
                        namePackageMap.put(label, packageName);
                    }
                }
                return null;
            }
        }.execute();
    }

    protected HashMap getMap(){
        return (HashMap) namePackageMap;
    }

    public String toString(){
        String out = "";
        for (String name : namePackageMap.keySet()){
            out = out  + name + System.getProperty("line.separator");
        }
        return out;
    }

}
