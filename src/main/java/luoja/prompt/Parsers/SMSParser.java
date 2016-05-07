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
 * Created by Jane on 3/03/16.
 */
public class SMSParser extends GenericParser {

    public SMSParser(Context inContext) {
        super(inContext);
    }

    @Override
    protected ResultBundle parse(String[] args) {
        if (args.length == 0){
            launchAppFromIntent(sendToIntent("0123456789"));
        } else if (args.length == 1){
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", numberOfContact(args[0]), null));
            launch(intent);
        } else if (args.length > 1){
            Intent intent = sendToIntent(numberOfContact(args[0]));
            intent.putExtra("sms_body", concatenate(subArray(args, 1, 0)));
            launch(intent);
        }
        return success();
    }

    protected static final String SMS_TO = "smsto:";

    protected Intent sendToIntent(String number){
        return new Intent(Intent.ACTION_VIEW, Uri.parse(SMS_TO + number));
    }

    protected String numberOfContact(String callsign){
        String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + "='"
                + callsign + "'";
        String[] projection = new String[] {ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER};
        Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection, selection, null, null);
        if (cursor.moveToFirst()){
            String out = cursor.getString(0);
            cursor.close();
            return out;
        } else {
            cursor.close();
            return callsign;
        }
    }
}
