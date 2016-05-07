package luoja.prompt.Parsers;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;

import luoja.prompt.GenericParser;
import luoja.prompt.R;
import luoja.prompt.ResultBundle;

/**
 * Created by Jane on 22/02/16.
 */
public class SearchParser extends GenericParser {

    public SearchParser(Context inContext) {
        super(inContext);
    }

    @Override
    protected ResultBundle parse(String[] args) {
        if (args.length > 0){
            Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
            intent.putExtra(SearchManager.QUERY, concatenateArray(args));
            launch(intent);
            return success();
        } else return new ResultBundle(false, context.getString(R.string.message_no_search_argument));
    }

    String SPACE = " ";

    protected String concatenateArray(String[] array){
        String out = "";
        for (String string : array){
            out = out + string + SPACE;
        }
        return out;
    }

}
