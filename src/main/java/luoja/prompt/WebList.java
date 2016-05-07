package luoja.prompt;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Parcel;

import java.util.HashMap;
import java.util.Map;

/**
 * Make sure the keys are all upper case and inputs are all upper case before checking against the map;
 */
public class WebList extends SQLiteOpenHelper {

    public static final String NAME = "WebList.sql";
    public static final int VERSION = 1;

    private Map<String, String> nameURLMap;

    private Context context;

    public WebList(Context inContext){
        super(inContext, NAME, null, VERSION);
        context = inContext;
        nameURLMap = new HashMap<>();
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.query(TABLE, null, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            String name = cursor.getString(cursor.getColumnIndex(KEY_NAME)).toUpperCase();
            String address = cursor.getString(cursor.getColumnIndex(KEY_ADDRESS));
            nameURLMap.put(name, address);
            cursor.moveToNext();
        }
        cursor.close();
        database.close();
    }

    public WebList(Context inContext, Map inMap){
        super(inContext, NAME, null, VERSION);
        nameURLMap = inMap;
    }

    public void register(String name, String address){
        nameURLMap.put(name.toUpperCase(), address);
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name);
        values.put(KEY_ADDRESS, address);
        SQLiteDatabase database = getWritableDatabase();
        database.insert(TABLE, null, values);
        database.close();
    }

    private static final String HTTP = "http://";
    private static final String HTTPS = "https://";

    public ResultBundle launch(String name){
        name = name.toUpperCase();
        if (nameURLMap.containsKey(name)){
            String url = nameURLMap.get(name);
            if (!url.startsWith(HTTP) || !url.startsWith(HTTPS))
                url = HTTP + url;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(intent);
            return new ResultBundle(true, "");
        } else return new ResultBundle(false, "");
    }

    protected HashMap getMap(){
        return (HashMap)nameURLMap;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CONSTRUCTOR);
        String[] defaultLinkNames = context.getResources().getStringArray(R.array.default_keywords_web);
        String[] defaultLinkAddresses = context.getResources().getStringArray(R.array.default_addresses_web);
        int i = 0;
        while (i < defaultLinkNames.length && i < defaultLinkAddresses.length){
            ContentValues values = new ContentValues();
            values.put(KEY_NAME, defaultLinkNames[i]);
            values.put(KEY_ADDRESS, defaultLinkAddresses[i]);
            db.insert(TABLE, null, values);
            ++i;
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private static final String COLON = " : " + System.getProperty("line.separator");

    /**
     * Gives a string of all links and their addresses.
     * @return
     */
    public String toString(){
        String out = "";
        for (String name : nameURLMap.keySet()){
            String link = nameURLMap.get(name);
            out = out + name + COLON + link + System.getProperty("line.separator");
        }
        return out;
    }

    private static final String TABLE = "webList";
    private static final String KEY_NAME = "name";
    private static final String KEY_ADDRESS = "address";

    private static final String CONSTRUCTOR =  "create table " + TABLE + "(_id integer primary key autoincrement, "
            + KEY_NAME + " string not null, " + KEY_ADDRESS + " string not null)";

}
