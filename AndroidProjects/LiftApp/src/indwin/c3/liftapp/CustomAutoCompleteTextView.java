package indwin.c3.liftapp;

import java.util.HashMap;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
 
/** Customizing AutoCompleteTextView to return Place Description
 * corresponding to the selected item
 */

public class CustomAutoCompleteTextView extends AutoCompleteTextView {
	private CustomAutoCompleteInterface callback;
    public CustomAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
 
    /** Returns the place description corresponding to the selected item */
    @Override
    protected CharSequence convertSelectionToString(Object selectedItem) {
        /** Each item in the autocompetetextview suggestion list is a hashmap object */
        HashMap<String, String> hm = (HashMap<String, String>) selectedItem;
        String str=hm.get("description");
        return hm.get("description");
    }
    
    @Override
    protected void onFocusChanged (boolean focused, int direction, Rect previouslyFocusedRect) {
      /*  if(!focused)
        {
        	Toast.makeText(
					this.getContext(),
					"Auto Complete away",
					Toast.LENGTH_LONG).show();
        	callback.focusChanged();
        	
        }*/
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
    }
    
    public void setCallback(CustomAutoCompleteInterface callback){

        this.callback = callback;
    }
    public interface CustomAutoCompleteInterface{
    	public void focusChanged();
    }
}