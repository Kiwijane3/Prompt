package luoja.prompt;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.vending.billing.IInAppBillingService;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

public class Launcher extends AppCompatActivity {


    private static final String KEY_APPLIST = "appList";
    private static final String KEY_WEBLIST = "webList";

    private static final String PREMIUM_SKU = "prompt_premium";
    private static final int PURCHASE_INTENT_CODE = 1011;

    private AppList appList;
    private ParserDirector parserDirector;
    private WebList webList;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private IInAppBillingService billingService;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            billingService = IInAppBillingService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            billingService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        setTrialEndDate();

        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        if (savedInstanceState != null) {
            appList = new AppList(this, (HashMap) savedInstanceState.getSerializable(KEY_APPLIST));
            webList = new WebList(this, (HashMap) savedInstanceState.getSerializable(KEY_WEBLIST));
            // Make sure these are the maps extracted from the previous variables.
        } else {
            appList = new AppList(this);
            webList = new WebList(this);
        }

        parserDirector = new ParserDirector(this, webList);
        inputText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    parse(inputText().getText().toString());
                    return true;
                } else return false;
            }
        });

    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        //Make sure these are the maps from the current variables.
        outState.putSerializable(KEY_APPLIST, appList.getMap());
        outState.putSerializable(KEY_WEBLIST, webList.getMap());
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        inputText().requestFocus();
        if (true /*#Placeholder: Check for hard keyboard */) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
        new Greeter(this, outputText()).greet();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (billingService != null) unbindService(serviceConnection);
    }

    /**
     * Tries to execute the command. If a parser responds, its message will be printed,
     * otherwise it will only be printed if launching an app or web site does not succeed.
     * (This is so that a fail message won't be shown if a user has associated a parser name with another app.)
     * If they do succeed, their message will be printed.
     * Will catch any security exceptions and inform the user.
     *
     * @param command
     * @return
     */
    public void parse(String command) {
        try {
            command = command.trim();
            String out = "";
            ResultBundle result;
            result = parserDirector.accept(command); //Send command to parsers
            out = result.message();
            if (!result.success()) { //Otherwise try to launch the name
                result = appList.launch(command);
                if (result.success()) out = result.message();
                else {
                    result = webList.launch(command);
                    if (result.success()) {
                        out = result.message();
                    } else { //Check for the inbuilt commands.
                        if (command.toUpperCase().equals(keywordUpgrade())) {
                            upgrade();
                            out = "";
                        }
                        else if (command.toUpperCase().equals(keywordList())) {
                            list();
                            out = "";
                        }
                        else if (command.toUpperCase().equals(keywordCredits()))
                            out = getString(R.string.credits);
                    }
                }
            }
            print(out);
            inputText().setText("");
        } catch (SecurityException se) {
            print(getString(R.string.message_security_exception));
        }
    }

    protected void print(String out){
        TextView outputText = outputText();
        outputText.setText(out + System.getProperty("line.separator") + outputText.getText());
        scrollView().fullScroll(ScrollView.FOCUS_UP);
    }

    protected void list(){
        print(appList.toString() + webList.toString());
    }

    /**
     * Keyword for purchasing parse premium.
     * @return
     */
    private String keywordUpgrade(){
        return getString(R.string.keyword_upgrade).toUpperCase();
    }

    /**
     * Keyword for listing all apps and registered links.
     * @return
     */
    private String keywordList() {
        return getString(R.string.keyword_list).toUpperCase();
    }

    private String keywordCredits() {
        return getString(R.string.keyword_credits).toUpperCase();
    }

    private void setTrialEndDate(){
        SharedPreferences sharedPreferences = sharedPreferences();
        if (sharedPreferences.getLong(Val.KEY_TRIAL_END, 0) == 0){ //See if it this is unavailable
            long now = new Date().getTime();
            long trialEnd = now + Val.TRIAL_PERIOD;
            sharedPreferences.edit().putLong(Val.KEY_TRIAL_END, trialEnd).apply();
        }

    }

    private void makePremium(){
        sharedPreferences().edit().putBoolean(Val.KEY_PREMIUM, true).commit();
    }

    private void upgrade(){
        try {
            Bundle ownedItems = billingService.getPurchases(3, getPackageName(), "inapp", null);
            if (ownedItems.getInt("RESPONSE_CODE") == 0) {
                List<String> ownedSkus = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                if (ownedSkus.contains(PREMIUM_SKU))
                    makePremium();
                else {
                    Bundle upgradeIntentBundle = billingService.getBuyIntent(3, getPackageName(), PREMIUM_SKU,
                            "inapp", null);
                    PendingIntent intent = upgradeIntentBundle.getParcelable("BUY_INTENT");
                    startIntentSenderForResult(intent.getIntentSender(), PURCHASE_INTENT_CODE,
                            new Intent(), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0));
                }
            } else outputText().append(getString(R.string.purchases_not_available));
        } catch (RemoteException re){
            outputText().append(getString(R.string.purchase_failed));
        } catch (IntentSender.SendIntentException sie){}
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PURCHASE_INTENT_CODE){
            if (resultCode == RESULT_OK){

            }
        }
    }

    private EditText inputText() {
        return (EditText) findViewById(R.id.input);
    }

    private ScrollView scrollView(){
        return (ScrollView) findViewById(R.id.scrollView);
    }

    private TextView outputText() {
        return (TextView) findViewById(R.id.output);
    }

    private SharedPreferences sharedPreferences() {
        return getSharedPreferences(Val.PREFERENCES_NAME, 0);
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Launcher Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://luoja.prompt/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Launcher Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://luoja.prompt/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
