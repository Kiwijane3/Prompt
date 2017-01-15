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

    private static final int PURCHASE_INTENT_CODE = 1011;

    private AppList appList;
    private ParserDirector parserDirector;
    private WebList webList;

    private IInAppBillingService billingService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

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
        appList.doBackgroundUpdate();
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
                    } else {
                        if (command.toUpperCase().equals(keywordList())) {
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
     * Keyword for listing all apps and registered links.
     * @return
     */
    private String keywordList() {
        return getString(R.string.keyword_list).toUpperCase();
    }

    private String keywordCredits() {
        return getString(R.string.keyword_credits).toUpperCase();
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

}
