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
 
           TextView reqView = (TextView)v.findViewById(R.id.reqid);
           TextView reqTime=(TextView)v.findViewById(R.id.requesttime);
          // TextView reqStatus=(TextView)v.findViewById(R.id.requeststatus);
           ImageView reqStatus=(ImageView)v.findViewById(R.id.reqstatus);
           TextView reqType=(TextView)v.findViewById(R.id.requesttype);
           MessageDetails msg = _data.get(position);
           if(msg.getType().equals("sent")){
        	   
           reqType.setText("You sent a lift request to "+msg.name);
           }else{
        	   reqType.setText("You received a lift request from "+msg.name);   
           }
           String reqId=msg.getRequestId();
           reqView.setText(msg.getRequestId());
           reqTime.setText(msg.getReqtime());
           
           if(msg.getStatus().equals("accepted")){
        	   reqStatus.setImageResource(R.drawable.accepted);
           }else if(msg.getStatus().equals("pending")){
        	   reqStatus.setImageResource(R.drawable.pending);
           }else if(msg.getStatus().equals("cancelled")){
        	   reqStatus.setImageResource(R.drawable.cancelled);
           }else if(msg.getStatus().equals("expired")){
        	   reqStatus.setImageResource(R.drawable.expired);
           }else if(msg.getStatus().equals("rejected")){
        	   reqStatus.setImageResource(R.drawable.rejected);
           }else if(msg.getStatus().equals("completed")){
        	   reqStatus.setImageResource(R.drawable.completed);
           }else if(msg.getStatus().equals("forfeited")){
        	   reqStatus.setImageResource(R.drawable.forfeited);
           }else{
        	   reqStatus.setImageResource(R.drawable.expired);
           }
           
           Animation animation = AnimationUtils.loadAnimation(parent.getContext(), (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
           v.startAnimation(animation);
           lastPosition = position;

        return v;

    }

}