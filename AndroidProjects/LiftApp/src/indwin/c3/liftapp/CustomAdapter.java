package indwin.c3.liftapp;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomAdapter extends BaseAdapter {
	 
    private ArrayList<MessageDetails> _data;
    Context _c;
    
    CustomAdapter (ArrayList<MessageDetails> data, Context c){
        _data = data;
        _c = c;
    }
   
    public int getCount() {
        // TODO Auto-generated method stub
        return _data.size();
    }
    
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return _data.get(position);
    }
 
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }
    private int lastPosition = -1;

    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
         View v = convertView;
         if (v == null)
         {
            LayoutInflater vi = (LayoutInflater)_c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.requests_list, null);
         }
 
           ImageView image = (ImageView) v.findViewById(R.id.userimage1);
           TextView nameView = (TextView)v.findViewById(R.id.name1);
           TextView ridesView = (TextView)v.findViewById(R.id.ridessofar);
           TextView fromView = (TextView)v.findViewById(R.id.from);
           TextView toView = (TextView)v.findViewById(R.id.to);
           TextView timeView = (TextView)v.findViewById(R.id.time);
           TextView reqTime=(TextView)v.findViewById(R.id.requesttime);
           MessageDetails msg = _data.get(position);
           image.setImageResource(msg.userimage);
           nameView.setText(msg.name);
           ridesView.setText("Rides so Far: "+msg.ridessofar);
           fromView.setText(msg.from);                             
           toView.setText(msg.to); 
           timeView.setText(msg.time);
           reqTime.setText(msg.reqtime);
           Animation animation = AnimationUtils.loadAnimation(parent.getContext(), (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
           v.startAnimation(animation);
           lastPosition = position;

        return v;
}

}