package com.alicegary.hackersonthego;

import java.util.Locale;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class MainActivity extends FragmentActivity {

    public 			SectionsPagerAdapter 	mSectionsPagerAdapter; 	// The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the sections. We use a {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will keep every loaded fragment in memory. If this becomes too memory intensive, it may be best to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}
    public static 	ViewPager		 		mViewPager; 			// The {@link ViewPager} that will host the section contents
    public static	View					attendanceView;
    public static	View					messageView;
    public static 	MainActivity			this_activity;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this_activity = this;
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);                      
    } 

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
        
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
        	super(fm);	
        }

        @Override
        public Fragment getItem(int position) {
        	return SectionFragment.get(position);
        }

        @Override
        public int getCount() {
        	return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }

    public static class SectionFragment extends Fragment {
    	
    	public static SectionFragment get(int position) {
    		SectionFragment sf = new SectionFragment();
    		Bundle bundle = new Bundle();
    		bundle.putInt("position", position);
    		sf.setArguments(bundle);
    		return sf;    		
    	}
    	   	
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        	View rootView = null;
            TextView attendanceTopTextView, attendanceBottomTextView, messageTopTextView, messageBottomTextView; 
            int position = this.getArguments().getInt("position");

            switch (position) {
        	case 1: 
        		rootView = inflater.inflate(R.layout.attendance_main, container, false);
        		attendanceTopTextView = (TextView) rootView.findViewById(R.id.attendance_top_textbox);
        		attendanceBottomTextView = (TextView) rootView.findViewById(R.id.attendance_bottom_textbox);
        		String attendance_header = MainApplication.getAttendanceHeader();
        		String attendance_data = MainApplication.getAttendanceData();
        		if (!(attendance_header.isEmpty())) {
        			attendanceTopTextView.setText(attendance_header);
        			attendanceBottomTextView.setText(attendance_data);     			
        		}
        		attendanceView = rootView;
        		break;
        	case 0:
        		rootView = inflater.inflate(R.layout.inbox_main, container, false);        		
        		messageTopTextView = (TextView) rootView.findViewById(R.id.messages_top_textbox);
        		messageBottomTextView = (TextView) rootView.findViewById(R.id.messages_bottom_textbox);
        		String message_header = MainApplication.getMessageHeader();
        		String message_subheader = MainApplication.getMessageSubheader();
        		String message_data = MainApplication.getMessageData();
        		if (!(message_header.isEmpty())) {
        			messageTopTextView.setText(message_header);
        			messageBottomTextView.setText(message_subheader);        			
        		}
        		messageView = rootView;
        		break;
        	case 2:
        		rootView = inflater.inflate(R.layout.options_main, container, false);
        		break;
        	}
       
        	return rootView;
        }    
    }
     
}

