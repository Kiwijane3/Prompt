package luoja.prompt;

/**
 * Created by Jane on 20/02/16.
 */
public class ResultBundle {

    private boolean success; //Did the command "succeed"
    private String message; /* A message about the command's outcome, such as the reason for failure
        or the outcome of a text that returns text. */

    public ResultBundle(boolean inSuccess, String inMessage){
        success = inSuccess;
        message = inMessage;
    }

    public boolean success(){
        return success;
    }

    public String message(){
        return message;
    }

}
