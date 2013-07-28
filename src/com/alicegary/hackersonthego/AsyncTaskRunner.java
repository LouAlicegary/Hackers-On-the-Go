package com.alicegary.hackersonthego;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import android.os.AsyncTask;
import android.widget.TextView;

	public class AsyncTaskRunner extends AsyncTask<String, String, String> { 
	    // < (passed to doInBackground), (passed to onProgressUpdate), (return value from doInBackground() passed to onPostExecute) >
		private String log_message;
		private String return_string;
		private String command_to_send;
		@Override
		protected String doInBackground(String... in_commands) {         	
			// return value sent to onPostExecute() as a parameter
			command_to_send = in_commands[0];
			log_message = "";
			return_string = "";
			final SSHClient ssh = new SSHClient();
	        try {
	        	// MAKE SSH CONNECTION
	        	log_message += "Attempting to connect via SSH...\n";
	        	publishProgress("0% complete");//(log_message);
	        	ssh.addHostKeyVerifier("78:0f:78:98:0e:9f:ff:e2:00:72:4e:a5:93:5e:a8:e3");
	        	ssh.connect("50.139.52.243");	            	
            	log_message += "Connection successful.\nAuthorizing SSH key...\n";
            	publishProgress("5% complete");//(log_message);	            	
            	ssh.authPublickey("loualicegary", ssh.loadKeys("/storage/emulated/0/id_rsa"));
            	final Session session = ssh.startSession();	            	
            	log_message += "SSH key authenticated.\nRunning '" + command_to_send + "' on server...\n";
            	publishProgress("10% complete");
            	
            	// EXECUTE COMMAND ON SERVER 
            	final Command cmd = session.exec("java -jar /home/loualicegary/Dropbox/Webdriver/hackers.jar " + command_to_send);
            	final InputStream inStream = cmd.getInputStream();

            	// THREAD 1: WAIT FOR COMMAND TO COMPLETE
                Runnable r1 = new Runnable() { 
            		public void run() {
                		try {
							cmd.join(60, TimeUnit.SECONDS);
							session.close();
						} 
                		catch (Exception e) {}
            		}
            	};
            	final Thread thr1 = new Thread(r1);
            	thr1.start();     
            	
            	// THREAD 2: UPDATE TEXTVIEWS WHILE THREAD 1 RUNS
            	Runnable r2 = new Runnable() { 
            		public void run() {
                		int num = 15;
            			while (thr1.isAlive()) {
                			try {
                				Thread.sleep(1000);
            					publishProgress(num + "% complete");
            					num = (num < 95) ? (num+5) : (num);
                				if (inStream.available() > 0) {
                					final byte[] buffer = new byte[8000];
                					inStream.read(buffer);
                					return_string += new String(buffer).trim() + "\n";
                				}		
							} 
	                		catch (Exception e) {}
                		}
            		}
            	};
            	new Thread(r2).start();

            	// join() blocks while waiting for a thread to finish execution...
            	thr1.join(); 
	            ssh.disconnect();
	            ssh.close();
	        }
	        catch (Exception e) {
	        	publishProgress("Connection error = " + e.getMessage());
	        }

	        // this pause is just here to make sure return_string is up-to-date. 
	        try {Thread.sleep(1000);} catch (Exception e) {}
	        
	        return return_string;
		}

		@Override
		protected void onProgressUpdate(String... in_text) {

			// UPDATE ATTENDANCE WINDOW
			if (command_to_send.contains("getattendance")) {
				TextView attendanceTopTextView = (TextView) MainActivity.attendanceView.findViewById(R.id.attendance_top_textbox);
				TextView attendanceBottomTextView = (TextView) MainActivity.attendanceView.findViewById(R.id.attendance_bottom_textbox);    					
				attendanceTopTextView.setText("UPDATING...");
				attendanceBottomTextView.setText(in_text[0]);	
			}
			
			// UPDATE MESSAGES WINDOW
			if (command_to_send.contains("getmessages") || command_to_send.contains("getmessagetext")) {
				TextView messagesTopTextView = (TextView) MainActivity.messageView.findViewById(R.id.messages_top_textbox);
				TextView messagesBottomTextView = (TextView) MainActivity.messageView.findViewById(R.id.messages_bottom_textbox);    					
				messagesTopTextView.setText("UPDATING...");
				messagesBottomTextView.setText(in_text[0]);	
			}

		}
		
		@Override
		protected void onPostExecute(String background_result) {
			String attendance_data = "";
			String attendance_header = "";
			String message_data = "";
			String message_header = "";
			
			// LINK TEXTVIEWS
			TextView attendanceTopTextView = (TextView) MainActivity.attendanceView.findViewById(R.id.attendance_top_textbox);
			TextView attendanceBottomTextView = (TextView) MainActivity.attendanceView.findViewById(R.id.attendance_bottom_textbox);
			final TextView messageTopTextView = (TextView) MainActivity.messageView.findViewById(R.id.messages_top_textbox);	
			final TextView messageBottomTextView = (TextView) MainActivity.messageView.findViewById(R.id.messages_bottom_textbox);
			
			// DO ATTENDANCE WINDOW
			if (command_to_send.contains("getattendance")) {
				attendance_data = background_result;
				attendance_header = MainApplication.getCurrentAttendanceStatus(attendance_data);
				attendanceTopTextView.setText(attendance_header);
				attendanceBottomTextView.setText(attendance_data);
				MainApplication.setAttendanceHeader(attendance_header);
				MainApplication.setAttendanceData(attendance_data);
			}
			
			// DO INBOX WINDOW
			if (command_to_send.contains("getmessages")) { MainApplication.drawInboxWindow(background_result); }
			
			// DO MESSAGE TEXT WINDOW
			if (command_to_send.contains("getmessagetext")) { MainApplication.drawMessageWindow(background_result, command_to_send.substring(15)); }
			
			return;
		}
	}