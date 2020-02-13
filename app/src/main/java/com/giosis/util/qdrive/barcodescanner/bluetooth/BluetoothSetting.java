package com.giosis.util.qdrive.barcodescanner.bluetooth;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.giosis.util.qdrive.singapore.R;

public class BluetoothSetting extends PreferenceActivity {
	
    private static final String TAG = "Bluetooth";	  
	
	ListPreference btdevicePref;    
	ListPreference btpowerontimePref;
	ListPreference btpowerofftimePref;
	CheckBoxPreference btwarningPref;    
	CheckBoxPreference btautopoweroffPref;    
	CheckBoxPreference btautoconnectPref;	
	CheckBoxPreference btautopoweronPref;
	CheckBoxPreference btpoweroffmsgPref;
    CheckBoxPreference btwakeupnullPref;	
    CheckBoxPreference bttogglePref; 
    
    int	ConnectDeviceBackup;
    int PowerOnTimeBackup;
    int PowerOffTimeBackup;
    
    AlertDialog alertDialog;
    
    SharedPreferences.OnSharedPreferenceChangeListener prefListener;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
        
        Log.d(TAG, "Bluetooth setting create");
        
        ConnectDeviceBackup = KTSyncData.ConnectDevice;
        PowerOnTimeBackup = KTSyncData.PowerOnTime;
        PowerOffTimeBackup = KTSyncData.PowerOffTime;
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
        			  if ( key.equals("btdevice_preference") ) {
        				  alertDialog.show();   				  
        			  } else
        			  if ( key.equals("btpowerontime_preference") ) {
        				  String newvalue = btpowerontimePref.getValue();
        				  KTSyncData.PowerOnTime = btpowerontimePref.findIndexOfValue(newvalue);
        				  btpowerontimePref.setSummary(btpowerontimePref.getEntry().toString());        				  
        			  } else
        			  if ( key.equals("btpowerofftime_preference") ) {         				  
        				  String newvalue = btpowerofftimePref.getValue();
        				  KTSyncData.PowerOffTime = btpowerofftimePref.findIndexOfValue(newvalue);
        				  btpowerofftimePref.setSummary(btpowerofftimePref.getEntry().toString());   
        			  }
        		  } 

        		}; 


        		sharedPrefs.registerOnSharedPreferenceChangeListener( prefListener);  

        		
        		MakeAlertDialog();
    } 
    
    public void MakeAlertDialog()
    {
	    alertDialog = new AlertDialog.Builder(this).create();
	    alertDialog.setTitle("Changing bluetooth profile");
	    alertDialog.setMessage("Are you sure you want to change?\n\nKTSync will be terminated with confirmation.");
	    alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
	        @Override
			public void onClick(DialogInterface dialog, int which) {
	   
	        	Log.d(TAG, "OK button");
	         
	        	String newvalue = btdevicePref.getValue();
	        	KTSyncData.ConnectDevice = btdevicePref.findIndexOfValue(newvalue);
	        	btdevicePref.setSummary(btdevicePref.getEntry().toString());

	        	KTSyncData.bForceTerminate = true;
	        	
	        	finish();
	   
	      } });
	    
	    alertDialog.setButton2("Cancel", new DialogInterface.OnClickListener() {
	        @Override
			public void onClick(DialogInterface dialog, int which) {
	   
	        	Log.d(TAG, "Cancel button");
	     		btdevicePref.setValueIndex(KTSyncData.ConnectDevice);
	     		btdevicePref.setSummary(btdevicePref.getEntry().toString());      
	   
	      } });
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
    	Log.d(TAG, "Bluetooth setting onDestry");
    	
    	if ( KTSyncData.bForceTerminate ) {
    		super.onDestroy();
    		return;
    	}
    	
    	KTSyncData.mKScan.WakeupCommand();

    	if ( KTSyncData.PowerOnTime != PowerOnTimeBackup )
    		KTSyncData.mKScan.SendCommandWithValue("bTO1", KTSyncData.PowerOnTime);    	
    	if ( KTSyncData.PowerOffTime != PowerOffTimeBackup )
    		KTSyncData.mKScan.SendCommandWithValue("bT71", KTSyncData.PowerOffTime+1);
    	
        KTSyncData.KDCSettings &= (~KTSyncData.BLUETOOTH_MASK);
        
        if ( btautoconnectPref.isChecked() )  KTSyncData.KDCSettings |= KTSyncData.AUTO_CONNECT; 
        if ( btautopoweronPref.isChecked() )  KTSyncData.KDCSettings |= KTSyncData.AUTO_POWER_ON; 
        if ( btautopoweroffPref.isChecked() ) KTSyncData.KDCSettings |= KTSyncData.AUTO_POWER_OFF; 
        if ( btwarningPref.isChecked() )      KTSyncData.KDCSettings |= KTSyncData.BEEP_WARNING;
        if ( btpoweroffmsgPref.isChecked() )  KTSyncData.KDCSettings |= KTSyncData.BT_POWER_MSG; 
        if ( btwakeupnullPref.isChecked() )   KTSyncData.KDCSettings |= KTSyncData.WAKEUP_NULLS; 
        if ( bttogglePref.isChecked() )   	  KTSyncData.KDCSettings |= KTSyncData.BT_TOGGLE;
        
        if ( (KTSyncData.KDCSettings & KTSyncData.AUTO_CONNECT) != (KTSyncData.KDCSettingsBackup & KTSyncData.AUTO_CONNECT) )
        	KTSyncData.mKScan.SendCommandWithValue("bT3", ((KTSyncData.KDCSettings & KTSyncData.AUTO_CONNECT) != 0) ? 1: 0  );
        if ( (KTSyncData.KDCSettings & KTSyncData.AUTO_POWER_ON) != (KTSyncData.KDCSettingsBackup & KTSyncData.AUTO_POWER_ON) )
        	KTSyncData.mKScan.SendCommandWithValue("bT4", ((KTSyncData.KDCSettings & KTSyncData.AUTO_POWER_ON) != 0) ? 1: 0  );
        if ( (KTSyncData.KDCSettings & KTSyncData.AUTO_POWER_OFF) != (KTSyncData.KDCSettingsBackup & KTSyncData.AUTO_POWER_OFF) )
        	KTSyncData.mKScan.SendCommandWithValue("bT5", ((KTSyncData.KDCSettings & KTSyncData.AUTO_POWER_OFF) != 0) ? 1: 0  );
        if ( (KTSyncData.KDCSettings & KTSyncData.BEEP_WARNING) != (KTSyncData.KDCSettingsBackup & KTSyncData.BEEP_WARNING) )
        	KTSyncData.mKScan.SendCommandWithValue("bT6", ((KTSyncData.KDCSettings & KTSyncData.BEEP_WARNING) != 0) ? 1: 0  );
        if ( (KTSyncData.KDCSettings & KTSyncData.BT_POWER_MSG) != (KTSyncData.KDCSettingsBackup & KTSyncData.BT_POWER_MSG) ) 
        	KTSyncData.mKScan.SendCommandWithValue("bT8", ((KTSyncData.KDCSettings & KTSyncData.BT_POWER_MSG) != 0) ? 1: 0  );        	
        if ( (KTSyncData.KDCSettings & KTSyncData.WAKEUP_NULLS) != (KTSyncData.KDCSettingsBackup & KTSyncData.WAKEUP_NULLS) ) 
        	KTSyncData.mKScan.SendCommandWithValue("bTW", ((KTSyncData.KDCSettings & KTSyncData.WAKEUP_NULLS) != 0) ? 1: 0  );        	
        if ( (KTSyncData.KDCSettings & KTSyncData.BT_TOGGLE) != (KTSyncData.KDCSettingsBackup & KTSyncData.BT_TOGGLE) )
        	KTSyncData.mKScan.SendCommandWithValue("bTHA", ((KTSyncData.KDCSettings & KTSyncData.BT_TOGGLE) != 0) ? 1: 0  );   

    	//if ( KTSyncData.ConnectDevice != ConnectDeviceBackup )
    	//	KTSyncData.mKScan.SendCommandWithValue("bTc", KTSyncData.ConnectDevice);
    	
    	KTSyncData.mKScan.FinishCommand();
    	
        super.onDestroy();       
    }
    
    private PreferenceScreen createPreferenceHierarchy() {
        // Root
        PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);      
        
        btdevicePref = (ListPreference)findPreference("btdevice_preference");
        if ( btdevicePref == null ) {
        	btdevicePref = new ListPreference(this);
        	btdevicePref.setEntries(R.array.entries_btdevice_preference);
        	btdevicePref.setEntryValues(R.array.entryvalues_five_preference);
        	btdevicePref.setDialogTitle(R.string.dialog_title_preference);
        	btdevicePref.setKey("btdevice_preference");
        	btdevicePref.setTitle(R.string.title_btdevice_preference);
        	root.addPreference(btdevicePref); 
        }
    	btdevicePref.setValueIndex(KTSyncData.ConnectDevice);
    	btdevicePref.setSummary(btdevicePref.getEntry().toString());
    	
        btautoconnectPref = (CheckBoxPreference)findPreference("btautoconnect_preference");
        if ( btautoconnectPref == null ) {
        	btautoconnectPref = new CheckBoxPreference(this);
        	btautoconnectPref.setKey("btautoconnect_preference");
        	btautoconnectPref.setTitle(R.string.title_btautoconnect_preference);
        	root.addPreference(btautoconnectPref); 
        }
        btautoconnectPref.setChecked((KTSyncData.KDCSettings & KTSyncData.AUTO_CONNECT) != 0);
        
        btautopoweronPref = (CheckBoxPreference)findPreference("btautopoweron_preference");
        if ( btautopoweronPref == null ) {
        	btautopoweronPref = new CheckBoxPreference(this);
        	btautopoweronPref.setKey("btautopoweron_preference");
        	btautopoweronPref.setTitle(R.string.title_btautopoweron_preference);
        	root.addPreference(btautopoweronPref);         
        }
        btautopoweronPref.setChecked((KTSyncData.KDCSettings & KTSyncData.AUTO_POWER_ON) != 0);
        
        btpowerontimePref = (ListPreference)findPreference("btpowerontime_preference");
        if ( btpowerontimePref == null ) {
        	btpowerontimePref = new ListPreference(this);
        	btpowerontimePref.setEntries(R.array.entries_btpowerontime_preference);
        	btpowerontimePref.setEntryValues(R.array.entryvalues_poweron_preference);
        	btpowerontimePref.setDialogTitle(R.string.dialog_title_preference);
        	btpowerontimePref.setKey("btpowerontime_preference");
        	btpowerontimePref.setTitle(R.string.title_btpowerontime_preference);
        	root.addPreference(btpowerontimePref); 
        }
    	btpowerontimePref.setValueIndex(KTSyncData.PowerOnTime);
    	btpowerontimePref.setSummary(btpowerontimePref.getEntry().toString());
  	
        btautopoweroffPref = (CheckBoxPreference)findPreference("btautopoweroff_preference");
        if ( btautopoweroffPref == null ) {
        	btautopoweroffPref = new CheckBoxPreference(this);
        	btautopoweroffPref.setKey("btautopoweroff_preference");
        	btautopoweroffPref.setTitle(R.string.title_btautopoweroff_preference);
        	root.addPreference(btautopoweroffPref);         
        }
        btautopoweroffPref.setChecked((KTSyncData.KDCSettings & KTSyncData.AUTO_POWER_OFF) != 0);
        
        btwarningPref = (CheckBoxPreference)findPreference("btwarning_preference");
        if ( btwarningPref == null ) {        
        	btwarningPref = new CheckBoxPreference(this);
        	btwarningPref.setKey("btwarning_preference");
        	btwarningPref.setTitle(R.string.title_btwarning_preference);
        	root.addPreference(btwarningPref); 
        }
        btwarningPref.setChecked((KTSyncData.KDCSettings & KTSyncData.BEEP_WARNING) != 0);
        
        btpowerofftimePref = (ListPreference)findPreference("btpowerofftime_preference");
        if ( btpowerofftimePref == null ) {        
        	btpowerofftimePref = new ListPreference(this);
        	btpowerofftimePref.setEntries(R.array.entries_btpowerofftime_preference);
        	btpowerofftimePref.setEntryValues(R.array.entryvalues_poweroff_preference);
        	btpowerofftimePref.setDialogTitle(R.string.dialog_title_preference);
        	btpowerofftimePref.setKey("btpowerofftime_preference");
        	btpowerofftimePref.setTitle(R.string.title_btpowerofftime_preference);
        	root.addPreference(btpowerofftimePref);  
        }
    	btpowerofftimePref.setValueIndex(KTSyncData.PowerOffTime);
    	btpowerofftimePref.setSummary(btpowerofftimePref.getEntry().toString());
    	
        btpoweroffmsgPref = (CheckBoxPreference)findPreference("btpoweroffmsg_preference");
        if ( btpoweroffmsgPref == null ) {        
        	btpoweroffmsgPref = new CheckBoxPreference(this);
        	btpoweroffmsgPref.setKey("btpoweroffmsg_preference");
        	btpoweroffmsgPref.setTitle(R.string.title_btpoweroffmsg_preference);
        	root.addPreference(btpoweroffmsgPref);     
        }
        btpoweroffmsgPref.setChecked((KTSyncData.KDCSettings & KTSyncData.BT_POWER_MSG) != 0);
        
        btwakeupnullPref = (CheckBoxPreference)findPreference("btwakeupnull_preference");
        if ( btwakeupnullPref == null ) {
        	btwakeupnullPref = new CheckBoxPreference(this);
        	btwakeupnullPref.setKey("btwakeupnull_preference");
        	btwakeupnullPref.setTitle(R.string.title_btwakeupnull_preference);
        	root.addPreference(btwakeupnullPref);    
        }
        btwakeupnullPref.setChecked((KTSyncData.KDCSettings & KTSyncData.WAKEUP_NULLS) != 0);
        
        bttogglePref = (CheckBoxPreference)findPreference("bttoggle_preference");
        if ( bttogglePref == null ) {
        	bttogglePref = new CheckBoxPreference(this);
        	bttogglePref.setKey("bttoggle_preference");
        	bttogglePref.setTitle(R.string.title_bttoggle_preference);
        	root.addPreference(bttogglePref);
        }
        bttogglePref.setChecked((KTSyncData.KDCSettings & KTSyncData.BT_TOGGLE) != 0);
               
        return root;
    }
}

