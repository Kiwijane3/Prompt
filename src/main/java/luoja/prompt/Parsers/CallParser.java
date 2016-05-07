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
 * Created by Jane on 23/02/16.
 */
public class CallParser extends GenericParser {


    public CallParser(Context inContext) {
        super(inContext);
    }

    @Override
    protected ResultBundle parse(String[] args) {
        if (args.length > 0){
            String number;
            try {
                number = numberOfContact(concatenate(args));
            } catch (Resources.NotFoundException rnfe){
                number = args[0];
            }
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + number));
            try { launch(intent);
            } catch (Exception e){ return new ResultBundle(false, "Couldn't make call"); }
            return success();
        } else {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:"));
            launchAppFromIntent(intent);
            return success();
        }
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
            throw new Resources.NotFoundException("Contact not found");
        }
    }

}
