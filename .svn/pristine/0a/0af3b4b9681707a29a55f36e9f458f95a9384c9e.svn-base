package com.giosis.util.qdrive.barcodescanner.bluetooth;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;

import com.giosis.util.qdrive.singapore.R;

public class ScanOption extends PreferenceActivity {
	
    private static final String TAG = "ScanOption";	  
	
	ListPreference rereadPref;
	ListPreference securityPref;
	CheckBoxPreference anglePref;
	CheckBoxPreference filterPref;
	ListPreference readingtimeoutPref;
	EditTextPreference minlenPref;
	CheckBoxPreference triggerPref;
	
	int ScanTimeoutBackup;
	int SecurityLevelBackup;
	int MinLengthBackup;
	int RereadDelayBackup;
	
    SharedPreferences.OnSharedPreferenceChangeListener prefListener;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
        
        Log.d(TAG, "ScanOption setting create "+ KTSyncData.Minlength + " " + MinLengthBackup);

    	ScanTimeoutBackup = KTSyncData.Timeout;
    	SecurityLevelBackup = KTSyncData.Security;
    	MinLengthBackup = KTSyncData.Minlength;
    	RereadDelayBackup = KTSyncData.RereadDelay;
    	KTSyncData.OptionsBackup = KTSyncData.Options;
    	KTSyncData.KDCSettingsBackup = KTSyncData.KDCSettings;
    	
        setPreferenceScreen(createPreferenceHierarchy());
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences( 
        		this ); 

        		prefListener = new 
        		SharedPreferences.OnSharedPreferenceChangeListener() 
        		{ 
        		  @Override 
        		  public void onSharedPreferenceChanged( SharedPreferences 
        		sharedPreferences, String key ) 
        		  { 
         		     	Log.w( TAG, "Pref key: " + key );         			  
        			  if ( key.equals("readingtimeout_preference") ) {
        				  String newvalue = readingtimeoutPref.getValue();
        				  KTSyncData.Timeout = readingtimeoutPref.findIndexOfValue(newvalue);
        				  readingtimeoutPref.setSummary(readingtimeoutPref.getEntry().toString());        				  
        			  } else
        			  if ( key.equals("security_preference") ) {
            		      String newvalue = securityPref.getValue();
            		      KTSyncData.Security = securityPref.findIndexOfValue(newvalue);
            			  securityPref.setSummary(securityPref.getEntry().toString());        				  
            		  } else        				  
        			  if ( key.equals("reread_preference") ) {         				  
        				  String newvalue = rereadPref.getValue();
        				  KTSyncData.RereadDelay = rereadPref.findIndexOfValue(newvalue);
        				  rereadPref.setSummary(rereadPref.getEntry().toString());   
        			  } 
        			  if ( key.equals("minlen_preference") ) { 
        				  if ( (Integer.parseInt(minlenPref.getText().toString()) < 37) &&
        						  (Integer.parseInt(minlenPref.getText().toString()) > 1)  ) {
        					  KTSyncData.Minlength = Integer.parseInt(minlenPref.getText().toString());
        					  minlenPref.setSummary(Integer.toString(KTSyncData.Minlength));
        				  } else
        					  minlenPref.setText(Integer.toString(KTSyncData.Minlength));
        			  } 
        		  } 

        		}; 


        		sharedPrefs.registerOnSharedPreferenceChangeListener( prefListener);  
    } 
    @Override     
    protected void onResume() {
        super.onResume();          
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(prefListener);     
    }



    @Override     
    protected void onPause() {         
        super.onPause();          
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(prefListener);

    }    
    @Override
    public void onDestroy() {
    	Log.d(TAG, "ScanOption setting onDestry " + KTSyncData.Minlength + " " + MinLengthBackup);
 
    	KTSyncData.mKScan.WakeupCommand();        
    	
        if ( ! KTSyncData.bIsKDC300 ) {
        
        	KTSyncData.Options &= (~(KTSyncData.WIDE_ANGLE_MASK | KTSyncData.HIGH_FILTER_MASK));
        	if ( anglePref.isChecked() )	KTSyncData.Options |= KTSyncData.WIDE_ANGLE_MASK;
        	if ( filterPref.isChecked() )	KTSyncData.Options |= KTSyncData.HIGH_FILTER_MASK;
        	
            if ( KTSyncData.Options != KTSyncData.OptionsBackup  )
                KTSyncData.mKScan.SendCommandWithValue("O", KTSyncData.Options ); 
            
            if ( KTSyncData.Security != SecurityLevelBackup )
        		KTSyncData.mKScan.SendCommandWithValue("Z", KTSyncData.Security+1);
        }
    	if ( KTSyncData.Timeout != ScanTimeoutBackup )
    		KTSyncData.mKScan.SendCommandWithValue("T", (KTSyncData.Timeout+1)*1000);
    	
    	if ( KTSyncData.Minlength != MinLengthBackup ) {
    		Log.d(TAG, "ScanOption:MinLength " + KTSyncData.Minlength + " " + MinLengthBackup);
    		KTSyncData.mKScan.SendCommandWithValue("L", KTSyncData.Minlength);
    	}
    	if ( KTSyncData.RereadDelay != RereadDelayBackup )
    		KTSyncData.mKScan.SendCommandWithValue("GtSD", KTSyncData.RereadDelay);
    	
    	KTSyncData.KDCSettings &= (~KTSyncData.AUTO_TRIGGER_MASK);
    	if ( triggerPref.isChecked() )	KTSyncData.KDCSettings |= KTSyncData.AUTO_TRIGGER_MASK;
        if ( (KTSyncData.KDCSettings & KTSyncData.AUTO_TRIGGER_MASK) != (KTSyncData.KDCSettingsBackup & KTSyncData.AUTO_TRIGGER_MASK) )
            KTSyncData.mKScan.SendCommandWithValue("GtSM", ((KTSyncData.KDCSettings & KTSyncData.AUTO_TRIGGER_MASK) != 0) ? 1: 0  );     	
    	
    	KTSyncData.mKScan.FinishCommand();
    	
        super.onDestroy();       
    }
    
    private PreferenceScreen createPreferenceHierarchy() {
        // Root
        PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);

    	if ( ! KTSyncData.bIsKDC300 ) {

    		anglePref = (CheckBoxPreference)findPreference("angle_preference");
    		if ( anglePref == null ) {
    			anglePref = new CheckBoxPreference(this);
    			anglePref.setKey("angle_preference");
    			anglePref.setTitle(R.string.title_angle_preference);
    			root.addPreference(anglePref);          
    		}
            anglePref.setChecked((KTSyncData.Options & KTSyncData.WIDE_ANGLE_MASK) != 0);
 
    		filterPref = (CheckBoxPreference)findPreference("filter_preference");
    		if ( filterPref == null ) {
    			filterPref = new CheckBoxPreference(this);
    			filterPref.setKey("filter_preference");
    			filterPref.setTitle(R.string.title_filter_preference);
    			root.addPreference(filterPref);          
    		}
            filterPref.setChecked((KTSyncData.Options & KTSyncData.HIGH_FILTER_MASK) != 0);
            
    		securityPref = (ListPreference)findPreference("security_preference");
    		if ( securityPref == null ) {
    			securityPref = new ListPreference(this);
    			securityPref.setEntries(R.array.entries_security_preference);
    			securityPref.setEntryValues(R.array.entryvalues_security_preference);
    			securityPref.setDialogTitle(R.string.dialog_title_preference);
    			securityPref.setKey("security_preference");
    			securityPref.setTitle(R.string.title_security_preference);
    			root.addPreference(securityPref);     	
    		}
    		securityPref.setValueIndex(KTSyncData.Security);
    		securityPref.setSummary(securityPref.getEntry().toString());            
    	}

		readingtimeoutPref = (ListPreference)findPreference("readingtimeout_preference");
		if ( readingtimeoutPref == null ) {
			readingtimeoutPref = new ListPreference(this);
			readingtimeoutPref.setEntries(R.array.entries_readingtimeout_preference);
			readingtimeoutPref.setEntryValues(R.array.entryvalues_readingtimeout_preference);
			readingtimeoutPref.setDialogTitle(R.string.dialog_title_preference);
			readingtimeoutPref.setKey("readingtimeout_preference");
			readingtimeoutPref.setTitle(R.string.title_readingtimeout_preference);
			root.addPreference(readingtimeoutPref);     	
		}
    	readingtimeoutPref.setValueIndex(KTSyncData.Timeout);
    	readingtimeoutPref.setSummary(readingtimeoutPref.getEntry().toString());
   	
    	// number of char position prefernce
    	minlenPref = (EditTextPreference)findPreference("minlen_preference");
    	if ( minlenPref == null ) {
    		minlenPref = new EditTextPreference(this);
    		minlenPref.setDialogTitle(R.string.dialog_title_number_preference);
    		minlenPref.setKey("minlen_preference");
    		minlenPref.setTitle(R.string.title_minlen_preference);
    		minlenPref.setSummary(R.string.summary_minlen_preference);
    		root.addPreference(minlenPref);
        	EditText minEditText = minlenPref.getEditText();
        	minEditText.setInputType(InputType.TYPE_CLASS_NUMBER); 
    	}
    	minlenPref.setSummary(Integer.toString(KTSyncData.Minlength));
       	minlenPref.setText(Integer.toString(KTSyncData.Minlength));
    	
		triggerPref = (CheckBoxPreference)findPreference("trigger_preference");
		if ( triggerPref == null ) {
			triggerPref = new CheckBoxPreference(this);
			triggerPref.setKey("trigger_preference");
			triggerPref.setTitle(R.string.title_trigger_preference);
			root.addPreference(triggerPref);          
		}
		triggerPref.setChecked((KTSyncData.KDCSettings & KTSyncData.AUTO_TRIGGER_MASK) != 0);
        //ListPreference rereadPref = new ListPreference(this);
        rereadPref = (ListPreference)findPreference("reread_preference");
		if ( rereadPref == null ) {
			rereadPref = new ListPreference(this);
			rereadPref.setEntries(R.array.entries_reread_preference);
			rereadPref.setEntryValues(R.array.entryvalues_five_preference);
			rereadPref.setDialogTitle(R.string.dialog_title_preference);
			rereadPref.setKey("reread_preference");
			rereadPref.setTitle(R.string.title_reread_preference);
			rereadPref.setSummary(R.string.summary_reread_preference);        
			root.addPreference(rereadPref);        
		}
    	rereadPref.setValueIndex(KTSyncData.RereadDelay);
    	rereadPref.setSummary(rereadPref.getEntry().toString());
    	
        return root;
    }
}
