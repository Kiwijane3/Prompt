package luoja.prompt.Parsers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import luoja.prompt.GenericParser;
import luoja.prompt.R;
import luoja.prompt.ResultBundle;

/**
 * Created by Jane on 15/02/16.
 */
public class WebParser extends GenericParser {

    public WebParser(Context inContext) {
        super(inContext);
    }

    @Override
    protected ResultBundle parse(String[] args) {
        launchAppFromIntent(webIntent());
        return new ResultBundle(true, context.getString(R.string.message_generic_ok));
    }

    protected Intent webIntent(){
        return new Intent(Intent.ACTION_VIEW, Uri.parse("http://"));
    }

}
