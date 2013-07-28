package com.alicegary.hackersonthego;

import java.util.Calendar;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Application;
import android.content.DialogInterface;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainApplication extends Application {
	
	public  static DBHelper 		db;
	
    private static String  			attendance_data;
    private static String			attendance_header;
    private static String			attendance_subheader;
    private static String			message_data;
    private static String			message_header;
    private static String			message_subheader;
    
    private static int				update_counter;
	
    private static MainApplication 	this_app;
    
	@Override
	public void onCreate() {
		super.onCreate();
		
		this_app = this;
    	attendance_data = "";
    	attendance_header = "";
    	message_data = "";
    	message_header = "";
    	message_subheader = "";
    	update_counter = 0;
    	
    	db = new DBHelper(this.getApplicationContext());
    	db.emptyTable();
    	
    	//AsyncTaskRunner runner = new AsyncTaskRunner();
        //runner.execute("getmessages");
        
    	Runnable r1 = new Runnable() { 
    		public void run() {
    			try {
    				//Thread.sleep(60000);
    				AsyncTaskRunner runner = new AsyncTaskRunner();
    				runner.execute("getmessages");
    			} 
    			catch (Exception e) {}
    		}
    	};
    	final Thread thr1 = new Thread(r1);
    	thr1.start();    	
	}
	
	public static void setAttendanceHeader(String in_val) {
		attendance_header = in_val;
	}	
	public static String getAttendanceHeader() {
		return attendance_header;
	}	
	
	public static void setAttendanceData(String in_val) {
		attendance_data = in_val;
	}	
	public static String getAttendanceData() {
		return attendance_data;
	}	

	public static void setMessageHeader(String in_val) {
		message_header = in_val;
	}	
	public static String getMessageHeader() {
		return message_header;
	}
	
	public static void setMessageSubheader(String in_val) {
		message_subheader = in_val;
	}	
	public static String getMessageSubheader() {
		return message_subheader;
	}
	
	public static void setMessageData(String in_val) {
		message_data = in_val;
	}	
	public static String getMessageData() {
		return message_data;
	}
	
	public void setUpdateCounter(int in_val) {
		update_counter = in_val;
	}	
	public int getUpdateCounter() {
		return update_counter;
	}
		
	public static int getNewMessageCount(String in_string) {
		String str = in_string;
		int lastIndex = 0;
		int new_msg_count = 0;

		while(lastIndex != -1) {
			lastIndex = str.indexOf("[*]",lastIndex);
			if( lastIndex != -1) {
				new_msg_count++;
				lastIndex += 3;
			}
		}
		return new_msg_count;
	}
	
	public static String getCurrentAttendanceStatus(String in_string) {
		String splitArray[] = in_string.split("\n");
		String latest = splitArray[0];
		String status = (latest.contains("in")) ? ("Signed in.") : ("Signed out.");
		return status;
	}
	
	public static void drawInboxWindow(String in_data) {

		// ADDING MESSAGES TO DB FROM SSH DATA
		String stringArray[] = in_data.split("_&&_MSG_BREAK_&&_");		
		for (int i=0; i < stringArray.length-1; i++) { //String thisString : stringArray) {			
			String thisString = stringArray[i];
			String subStringArray[] = thisString.split("_&&_COL_BREAK_&&_");
			db.addRow( (int) Integer.valueOf(subStringArray[0].trim()), subStringArray[1].trim(), subStringArray[2].trim(), subStringArray[3].trim(), subStringArray[4].trim(), subStringArray[5].trim(), subStringArray[6].trim());
		}

		// GETTING MESSAGES FROM DB
		Cursor c = MainApplication.db.getReadableDatabase().rawQuery("select * from messages", null);
		c.moveToFirst();
		in_data = "";
		while (!c.isAfterLast()) {
			String s = c.getInt(0) + "_&&_COL_BREAK_&&_" + c.getString(1) + "_&&_COL_BREAK_&&_" + c.getString(2) + "_&&_COL_BREAK_&&_" + c.getString(3) + "_&&_COL_BREAK_&&_" + c.getString(4) + "_&&_COL_BREAK_&&_" + c.getString(5) + "_&&_COL_BREAK_&&_" + c.getString(6);
			in_data += s + "_&&_MESSAGE_SPLIT_&&_";
			c.moveToNext();
		}
				
		// GET COMPONENTS FROM FRAGMENT VIEW
		View this_view = View.inflate(MainActivity.messageView.getContext(), R.layout.inbox_main, MainActivity.mViewPager);
		final LinearLayout outer_ll = (LinearLayout) this_view.findViewById(R.id.inbox_outer_ll);
		final LinearLayout inner_ll = (LinearLayout) this_view.findViewById(R.id.inbox_inner_ll);
		final TextView messageTopTextView = (TextView) this_view.findViewById(R.id.messages_top_textbox); 	
		final TextView messageBottomTextView = (TextView) this_view.findViewById(R.id.messages_bottom_textbox); 	
		final Button todayButton = (Button) this_view.findViewById(R.id.today_messages_button);
		final Button yesterdayButton = (Button) this_view.findViewById(R.id.yesterday_messages_button);
		final Button backButton = (Button) this_view.findViewById(R.id.back_button);
		inner_ll.removeAllViews();
		todayButton.setVisibility(View.VISIBLE);
		yesterdayButton.setVisibility(View.VISIBLE);
		backButton.setVisibility(View.GONE);
		
	
		// GET CURRENT TIMESTAMP AND NEW MESSAGE COUNT
		String mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
		int new_msg_count = getNewMessageCount(in_data);
		
		// SET TEXTVIEW DATA FOR MESSAGE WINDOW				
		message_data = in_data;
		message_header = (new_msg_count == 1) ? ("1 new message.") : (new_msg_count + " new messages.");	
		message_subheader = "Last checked at " + mydate;
		messageTopTextView.setText(message_header);
		messageBottomTextView.setText(message_subheader);
		setMessageHeader(message_header);
		setMessageSubheader(message_subheader);
		setMessageData(message_data);
		
		// ADD NEW TEXTVIEWS FOR ALL UNREAD MESSAGES
		String splitArray[] = in_data.split("_&&_MESSAGE_SPLIT_&&_");
		for (final String the_string : splitArray) {
			if (the_string.contains("[*]")) {
				MediaPlayer.create(this_app.getApplicationContext(), R.raw.chime).start();
				final TextView myTV = new TextView(MainActivity.messageView.getContext());
				final String button_str_array[] = the_string.split("_&&_COL_BREAK_&&_");
				if (button_str_array[5].indexOf("의") != -1) { button_str_array[5] = "Response to my comment/post in message\n<" + button_str_array[5].substring(button_str_array[5].indexOf("의") + 2, button_str_array[5].indexOf("글에")) + ">";}
				myTV.setText(button_str_array[1] + " " + button_str_array[2] + " (" + button_str_array[3] + ") " + button_str_array[4] + "\n" + button_str_array[5]);
				myTV.setOnClickListener(new OnClickListener() { public void onClick(View arg0) {						
					if (outer_ll.getChildCount() > 3) {  
						for (int i=3; i < outer_ll.getChildCount(); i++) {
							outer_ll.getChildAt(i).setVisibility(View.GONE);
						}
					}
					inner_ll.removeAllViews();
					messageTopTextView.setText("Loading Message...");
					messageBottomTextView.setText("...so chill the fuck out and wait. ");
					drawMessageWindow(button_str_array[6], button_str_array[0]);
				} });
				myTV.setBackgroundResource(R.drawable.border);
				myTV.setPadding(10,  10,  10,  10);
				LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			    ((MarginLayoutParams) params).setMargins(0, 10, 0, 10);
				inner_ll.addView(myTV, params); 			
			}
		}
		
		// CLICK HANDLER FOR TODAY'S MESSAGES BUTTON
		//final Button todayButton = (Button) outer_ll.findViewById(R.id.today_messages_button);
		todayButton.setOnClickListener(new OnClickListener() { public void onClick(View arg0) {
			drawDayMessageWindow("today");
		}});

		//final Button yesterdayButton = (Button) outer_ll.findViewById(R.id.yesterday_messages_button);
		yesterdayButton.setOnClickListener(new OnClickListener() { public void onClick(View arg0) {
			drawDayMessageWindow("yesterday");
		}});
		
	}
	
	public static void drawMessageWindow(String in_string, String in_messageid) {	
		View this_view = View.inflate(MainActivity.messageView.getContext(), R.layout.inbox_main, MainActivity.mViewPager);
		final LinearLayout outer_ll = (LinearLayout) this_view.findViewById(R.id.inbox_outer_ll);
		final LinearLayout inner_ll = (LinearLayout) this_view.findViewById(R.id.inbox_inner_ll);		
		inner_ll.removeAllViews();
		final TextView messageTopTextView = (TextView) this_view.findViewById(R.id.messages_top_textbox); 
		final TextView messageBottomTextView = (TextView) this_view.findViewById(R.id.messages_bottom_textbox);
		messageTopTextView.setText("Message " + in_messageid);
		messageBottomTextView.setText("From: " + db.getSenderByMsgID(in_messageid) + "\nSent: " + db.getTimeByMsgID(in_messageid));
		
		TextView myTV = new TextView(this_view.getContext());
		myTV.setText(in_string);
		myTV.setBackgroundResource(R.drawable.border);
		myTV.setPadding(10, 10, 10, 10);
		LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
	    ((MarginLayoutParams) params).setMargins(0, 10, 0, 10);
		inner_ll.addView(myTV, params);
		
		final Button thisButton = (Button) outer_ll.findViewById(R.id.back_button);
		thisButton.setVisibility(View.VISIBLE);
		thisButton.setOnClickListener(new OnClickListener() { public void onClick(View arg0) {				
			drawInboxWindow(getMessageData());
		}});
		db.markMessageRead(in_messageid);
	}

	public static void drawDayMessageWindow(String in_period) {
		
		// get views
		View this_view = View.inflate(MainActivity.messageView.getContext(), R.layout.inbox_main, MainActivity.mViewPager);
		final LinearLayout outer_ll = (LinearLayout) this_view.findViewById(R.id.inbox_outer_ll);
		final LinearLayout inner_ll = (LinearLayout) this_view.findViewById(R.id.inbox_inner_ll);		
		inner_ll.removeAllViews();
		final TextView messageTopTextView = (TextView) this_view.findViewById(R.id.messages_top_textbox); 
		final TextView messageBottomTextView = (TextView) this_view.findViewById(R.id.messages_bottom_textbox);	
		
		// gets time in korea and sets message header
		String timezone = (String) new java.text.SimpleDateFormat("Z", Locale.US).format(System.currentTimeMillis());
		int k_offset = ((900-(Integer.parseInt(timezone)))/100) * 1000 * 3600; // Converts local time to KST
		String the_date = (String) new java.text.SimpleDateFormat("yy-MM-dd", Locale.US).format(System.currentTimeMillis()+k_offset);		
		if (in_period.equals("today")) {
			setMessageHeader("Today's Messages");
		}
		else if (in_period.equals("yesterday")) {
			setMessageHeader("Yesterday's Msgs");
			the_date = (String) new java.text.SimpleDateFormat("yy-MM-dd", Locale.US).format(System.currentTimeMillis()+k_offset-(86400000));			
		}
		messageTopTextView.setText(getMessageHeader());
		
		// pares message data for messages from specified day
		String splitArray[] = getMessageData().split("\n");
		for (final String the_string : splitArray) {
			if (the_string.contains(the_date)) {
				final String button_str_array[] = the_string.split("_&&_COL_BREAK_&&_");
				TextView myTV = new TextView(this_view.getContext());
				myTV.setText(button_str_array[1] + " " + button_str_array[2] + " (" + button_str_array[3] + ") " + button_str_array[4] + "\n" + button_str_array[5]);
				myTV.setOnClickListener(new OnClickListener() { public void onClick(View arg0) {
					drawMessageWindow(button_str_array[5], String.valueOf(button_str_array[0]));
				} });
				myTV.setBackgroundResource(R.drawable.border);
				myTV.setPadding(10,  10,  10,  10);
				LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			    ((MarginLayoutParams) params).setMargins(0, 10, 0, 10);
				inner_ll.addView(myTV, params);			
			}
		}
		if (outer_ll.getChildCount() > 3) { outer_ll.removeViews(3, outer_ll.getChildCount()-3); }
		
		setMessageSubheader(inner_ll.getChildCount() + " messages from " + the_date);
		messageBottomTextView.setText(getMessageSubheader());		
	}
	
    public static void alertbox(String title, String mymessage) {
    	Builder myAlert = new AlertDialog.Builder(MainActivity.messageView.getContext()).setMessage(mymessage).setTitle(title);
    	myAlert.setCancelable(false);
    	myAlert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int whichButton) {}
    	});
    	myAlert.show();
    }
	
}
