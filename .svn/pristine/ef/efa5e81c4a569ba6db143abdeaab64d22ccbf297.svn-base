package com.giosis.util.qdrive.barcodescanner.bluetooth;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.giosis.util.qdrive.singapore.R;

public class HIDSetting extends PreferenceActivity {
	
    private static final String TAG = "HID";	  
    
    ListPreference hidautolockPref;
    ListPreference hidkbdPref;
    ListPreference hidinitdelayPref;
    ListPreference hidchardelayPref;
    ListPreference hidctrlcharPref;   
    
    int	AutoLockBackup;
    int	KeyboardBackup;
    int	InitDelayBackup;
    int	CharDelayBackup;
    int	CtrlCharBackup;
    
    SharedPreferences.OnSharedPreferenceChangeListener prefListener;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
        
        Log.d(TAG, "HID setting create");

        AutoLockBackup = KTSyncData.AutoLock;
        KeyboardBackup = KTSyncData.Keyboard;
        InitDelayBackup = KTSyncData.InitDelay;
        CharDelayBackup = KTSyncData.CharDelay;
        CtrlCharBackup = KTSyncData.CtrlChar;
        
        setPreferenceScreen(createPreferenceHierarchy());

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences( 
        		getApplicationContext() ); 

        		prefListener = new 
        		SharedPreferences.OnSharedPreferenceChangeListener() 
        		{ 
        		  @Override 
        		  public void onSharedPreferenceChanged( SharedPreferences 
        		sharedPreferences, String key ) 
        		  { 
         		     	Log.w( TAG, "Pref key: " + key );         			  
        			  if ( key.equals("hidautolock_preference") ) {
        				  String newvalue = hidautolockPref.getValue();
        				  KTSyncData.AutoLock = hidautolockPref.findIndexOfValue(newvalue);
        				  hidautolockPref.setSummary(hidautolockPref.getEntry().toString());        				  
        			  } else
        			  if ( key.equals("hidkbd_preference") ) {
        				  String newvalue = hidkbdPref.getValue();
        				  KTSyncData.Keyboard = hidkbdPref.findIndexOfValue(newvalue);
        				  hidkbdPref.setSummary(hidkbdPref.getEntry().toString());        				  
        			  } else
        			  if ( key.equals("hidinitdelay_preference") ) {
        				  String newvalue = hidinitdelayPref.getValue();
        				  KTSyncData.InitDelay = hidinitdelayPref.findIndexOfValue(newvalue);
        				  hidinitdelayPref.setSummary(hidinitdelayPref.getEntry().toString());        				  
        			  } else
        			  if ( key.equals("hidchardelay_preference") ) {
        				  String newvalue = hidchardelayPref.getValue();
        				  KTSyncData.CharDelay = hidchardelayPref.findIndexOfValue(newvalue);
        				  hidchardelayPref.setSummary(hidchardelayPref.getEntry().toString());        				  
        			  } else
        			  if ( key.equals("hidctrlchar_preference") ) {
        				  String newvalue = hidctrlcharPref.getValue();
        				  KTSyncData.CtrlChar = hidctrlcharPref.findIndexOfValue(newvalue);
        				  hidctrlcharPref.setSummary(hidctrlcharPref.getEntry().toString());        				  
        			  }
        		  } 

        		}; 

        		sharedPrefs.registerOnSharedPreferenceChangeListener( prefListener );         
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
    	Log.d(TAG, "HID setting onDestry");
    	
    	KTSyncData.mKScan.WakeupCommand();
    	
    	if ( KTSyncData.AutoLock != AutoLockBackup )
    		KTSyncData.mKScan.SendCommandWithValue("GHTS", KTSyncData.AutoLock);
    	if ( KTSyncData.Keyboard != KeyboardBackup )
    		KTSyncData.mKScan.SendCommandWithValue("GHKS", KTSyncData.Keyboard);    	
    	if ( KTSyncData.InitDelay != InitDelayBackup )
    		KTSyncData.mKScan.SendCommandWithValue("GndBS", KTSyncData.InitDelay);
    	if ( KTSyncData.CharDelay != CharDelayBackup )
    		KTSyncData.mKScan.SendCommandWithValue("GndCS", KTSyncData.CharDelay);   
    	if ( KTSyncData.CtrlChar != CtrlCharBackup )
    		KTSyncData.mKScan.SendCommandWithValue("GnCS", KTSyncData.CtrlChar);
    	
    	KTSyncData.mKScan.FinishCommand();
    	
        super.onDestroy();       
    }
    
    private PreferenceScreen createPreferenceHierarchy() {
        // Root
        PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);

    	hidautolockPref =(ListPreference)findPreference("hidautolock_preference");
    	if ( hidautolockPref == null )	{
    		hidautolockPref = new ListPreference(this);
    		hidautolockPref.setEntries(R.array.entries_hidautolock_preference);
    		hidautolockPref.setEntryValues(R.array.entryvalues_eight_preference);
    		hidautolockPref.setDialogTitle(R.string.dialog_title_preference);
    		hidautolockPref.setKey("hidautolock_preference");
    		hidautolockPref.setTitle(R.string.title_hidautolock_preference);
    		root.addPreference(hidautolockPref);
    	}
    	hidautolockPref.setValueIndex(KTSyncData.AutoLock);
    	hidautolockPref.setSummary(hidautolockPref.getEntry().toString());
    	
    	hidkbdPref =(ListPreference)findPreference("hidkbd_preference");
    	if ( hidkbdPref == null )	{    	
    		hidkbdPref = new ListPreference(this);
    		hidkbdPref.setEntries(R.array.entries_hidkbd_preference);
    		hidkbdPref.setEntryValues(R.array.entryvalues_five_preference);
    		hidkbdPref.setDialogTitle(R.string.dialog_title_preference);
    		hidkbdPref.setKey("hidkbd_preference");
    		hidkbdPref.setTitle(R.string.title_hidkbd_preference);
    		root.addPreference(hidkbdPref);        
    	}
    	hidkbdPref.setValueIndex(KTSyncData.Keyboard);
    	hidkbdPref.setSummary(hidkbdPref.getEntry().toString());
    	
    	hidinitdelayPref =(ListPreference)findPreference("hidinitdelay_preference");
    	if ( hidinitdelayPref == null )	{
    		hidinitdelayPref = new ListPreference(this);
    		hidinitdelayPref.setEntries(R.array.entries_hidinitdelay_preference);
    		hidinitdelayPref.setEntryValues(R.array.entryvalues_six_preference);
    		hidinitdelayPref.setDialogTitle(R.string.dialog_title_preference);
    		hidinitdelayPref.setKey("hidinitdelay_preference");
    		hidinitdelayPref.setTitle(R.string.title_hidinitdelay_preference);
    		root.addPreference(hidinitdelayPref);        
    	}
    	hidinitdelayPref.setValueIndex(KTSyncData.InitDelay);
    	hidinitdelayPref.setSummary(hidinitdelayPref.getEntry().toString());
    	
    	hidchardelayPref =(ListPreference)findPreference("hidchardelay_preference");
    	if ( hidchardelayPref == null )	{
    		hidchardelayPref = new ListPreference(this);
    		hidchardelayPref.setEntries(R.array.entries_hidchardelay_preference);
    		hidchardelayPref.setEntryValues(R.array.entryvalues_six_preference);
    		hidchardelayPref.setDialogTitle(R.string.dialog_title_preference);
    		hidchardelayPref.setKey("hidchardelay_preference");
    		hidchardelayPref.setTitle(R.string.title_hidchardelay_preference);
    		root.addPreference(hidchardelayPref);         
    	}
    	hidchardelayPref.setValueIndex(KTSyncData.CharDelay);
    	hidchardelayPref.setSummary(hidchardelayPref.getEntry().toString());
    	
    	hidctrlcharPref =(ListPreference)findPreference("hidctrlchar_preference");
    	if ( hidctrlcharPref == null )	{
    		hidctrlcharPref = new ListPreference(this);
    		hidctrlcharPref.setEntries(R.array.entries_hidctrlchar_preference);
    		hidctrlcharPref.setEntryValues(R.array.entryvalues_three_preference);
    		hidctrlcharPref.setDialogTitle(R.string.dialog_title_preference);
    		hidctrlcharPref.setKey("hidctrlchar_preference");
    		hidctrlcharPref.setTitle(R.string.title_hidctrlchar_preference);
    		root.addPreference(hidctrlcharPref);         
    	}
    	hidctrlcharPref.setValueIndex(KTSyncData.CtrlChar);
    	hidctrlcharPref.setSummary(hidctrlcharPref.getEntry().toString());
    	
        return root;
    }
}
