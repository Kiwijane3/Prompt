package luoja.prompt.Parsers;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import luoja.prompt.GenericParser;
import luoja.prompt.ResultBundle;

/**
 * Created by Jane on 20/02/16.
 */
public class EmailParser extends GenericParser {

    public EmailParser(Context inContext){
        super(inContext);
    }

    @Override
    protected ResultBundle parse(String[] args) {
        if (args.length  > 0){
            Intent intent = emailIntent();
            intent.putExtra(Intent.EXTRA_EMAIL, addressOfContact(args[0]));
            if (args.length > 1){
                //TODO: include body
            }
            launch(intent);
            return success();
        } else {
            launchAppFromIntent(emailIntent());
            return success();
        }
    }

    protected Intent emailIntent(){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setData(Uri.parse("mailto:"));
        intent.setType("text/plain");
        return intent;
    }

    protected String addressOfContact(String callSign){
        String selection = ContactsContract.CommonDataKinds.Email.DISPLAY_NAME + "='" + callSign + "'";
        String[] projection = new String[]{ContactsContract.CommonDataKinds.Email.ADDRESS};
        Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                projection, selection, null, null);
        if (cursor.moveToFirst()){
            String out = context.getString(0);
            cursor.close();
            return out;
        } else{
            cursor.close();
            return callSign;
        }
    }

}
