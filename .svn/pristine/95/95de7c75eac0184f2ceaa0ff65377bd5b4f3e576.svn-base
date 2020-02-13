package com.giosis.util.qdrive.barcodescanner.bluetooth;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.giosis.util.qdrive.singapore.R;


public class MSRSetting extends PreferenceActivity {
	
    private static final String TAG = "MSR";	  

    ListPreference msrformatPref;
    ListPreference tracktermPref; 
    CheckBoxPreference encryptPref;
    CheckBoxPreference beeponerrorPref;
  
    CheckBoxPreference track1Pref;
    CheckBoxPreference track2Pref;
    CheckBoxPreference track3Pref;  
    
    int	MSRFormatBackup;
    int	TrackTerminatorBackup;
    
    SharedPreferences.OnSharedPreferenceChangeListener prefListener;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
        
        Log.d(TAG, "MSR setting create");
        
        MSRFormatBackup = KTSyncData.MSRFormat;
        TrackTerminatorBackup = KTSyncData.TrackTerminator;
        KTSyncData.KDCSettingsBackup = KTSyncData.KDCSettings;
        
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
        			  if ( key.equals("msrformat_preference") ) {
        				  String newvalue = msrformatPref.getValue();
        				  KTSyncData.MSRFormat = msrformatPref.findIndexOfValue(newvalue);
        				  msrformatPref.setSummary(msrformatPref.getEntry().toString());        				  
        			  } else
        			  if ( key.equals("trackterm_preference") ) {
        				  String newvalue = tracktermPref.getValue();
        				  KTSyncData.TrackTerminator = tracktermPref.findIndexOfValue(newvalue);
        				  tracktermPref.setSummary(tracktermPref.getEntry().toString());        				  
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
    	Log.d(TAG, "MSR setting onDestry");
    	
    	KTSyncData.mKScan.WakeupCommand();
    	
    	KTSyncData.KDCSettings &= (~KTSyncData.MSR_MASK);
    	
    	if ( encryptPref.isChecked() )	KTSyncData.KDCSettings |= KTSyncData.ENCRYPT_MASK;
    	if ( beeponerrorPref.isChecked() )	KTSyncData.KDCSettings |= KTSyncData.BEEPONERROR_MASK;
    	
    	if ( KTSyncData.MSRFormat != MSRFormatBackup )
    		KTSyncData.mKScan.SendCommandWithValue("GnMDS", KTSyncData.MSRFormat);
    	if ( KTSyncData.TrackTerminator != TrackTerminatorBackup )
    		KTSyncData.mKScan.SendCommandWithValue("GnMSS", KTSyncData.TrackTerminator);    	
 
//Encrypt data    	
        if ( (KTSyncData.KDCSettings & KTSyncData.ENCRYPT_MASK) != (KTSyncData.KDCSettingsBackup & KTSyncData.ENCRYPT_MASK) )
            KTSyncData.mKScan.SendCommandWithValue("GnMES", ((KTSyncData.KDCSettings & KTSyncData.ENCRYPT_MASK) != 0) ? 1: 0  ); 
        if ( (KTSyncData.KDCSettings & KTSyncData.BEEPONERROR_MASK) != (KTSyncData.KDCSettingsBackup & KTSyncData.BEEPONERROR_MASK) )
            KTSyncData.mKScan.SendCommandWithValue("GnMBS", ((KTSyncData.KDCSettings & KTSyncData.BEEPONERROR_MASK) != 0) ? 1: 0  );
//Track select
    	if ( track1Pref.isChecked() )	KTSyncData.KDCSettings |= KTSyncData.TRACK1_MASK;
    	if ( track2Pref.isChecked() )	KTSyncData.KDCSettings |= KTSyncData.TRACK2_MASK;
    	if ( track3Pref.isChecked() )	KTSyncData.KDCSettings |= KTSyncData.TRACK3_MASK;
    	
        int tracks = (KTSyncData.KDCSettings & KTSyncData.TRACKS_MASK) >> 4;
        int tracksbackup = (KTSyncData.KDCSettingsBackup & KTSyncData.TRACKS_MASK) >> 4;        
        
        if ( tracks != tracksbackup )   {
        	KTSyncData.mKScan.SendCommandWithValue("GnMTS", tracks);
        }
    	KTSyncData.mKScan.FinishCommand();
    	
        super.onDestroy();       
    }
    
    private PreferenceScreen createPreferenceHierarchy() {
        // Root
        PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);

    	msrformatPref =(ListPreference)findPreference("msrformat_preference");
    	if ( msrformatPref == null )	{        
    		msrformatPref = new ListPreference(this);
    		msrformatPref.setEntries(R.array.entries_msr_format_preference);
    		msrformatPref.setEntryValues(R.array.entryvalues_two_preference);
    		msrformatPref.setDialogTitle(R.string.dialog_title_preference);
    		msrformatPref.setKey("msrformat_preference");
    		msrformatPref.setTitle(R.string.title_msr_format_preference);
    		root.addPreference(msrformatPref); 
    	}
    	msrformatPref.setValueIndex(KTSyncData.MSRFormat);
    	msrformatPref.setSummary(msrformatPref.getEntry().toString());
    	
        // Track terminator prefernce
    	tracktermPref =(ListPreference)findPreference("trackerm_preference");
    	if ( tracktermPref == null )	{     
    		tracktermPref = new ListPreference(this);
    		tracktermPref.setEntries(R.array.entries_trackterm_preference);
    		tracktermPref.setEntryValues(R.array.entryvalues_eight_preference);
    		tracktermPref.setDialogTitle(R.string.dialog_title_preference);
    		tracktermPref.setKey("trackterm_preference");
    		tracktermPref.setTitle(R.string.title_trackterm_preference);
    		root.addPreference(tracktermPref);         
    	}
    	tracktermPref.setValueIndex(KTSyncData.TrackTerminator);
    	tracktermPref.setSummary(tracktermPref.getEntry().toString());
    	
    	encryptPref = (CheckBoxPreference)findPreference("encrypt_preference");
    	if ( encryptPref == null ) {
    		encryptPref = new CheckBoxPreference(this);
    		encryptPref.setKey("encrypt_preference");
    		encryptPref.setTitle(R.string.title_encrypt_preference);
    		root.addPreference(encryptPref);    
    	}
        encryptPref.setChecked((KTSyncData.KDCSettings & KTSyncData.ENCRYPT_MASK) != 0); 
        
        beeponerrorPref = (CheckBoxPreference)findPreference("beeponerror_preference");
    	if ( beeponerrorPref == null ) {
    		beeponerrorPref = new CheckBoxPreference(this);
    		beeponerrorPref.setKey("beeponerror_preference");
    		beeponerrorPref.setTitle(R.string.title_beeponerror_preference);
    		root.addPreference(beeponerrorPref);    
    	}
        beeponerrorPref.setChecked((KTSyncData.KDCSettings & KTSyncData.BEEPONERROR_MASK) != 0);   
        
        // Select tracks

        PreferenceCategory trackPrefCat = new PreferenceCategory(this);
        trackPrefCat.setTitle(R.string.track_preferences);
        root.addPreference(trackPrefCat);

        track1Pref = (CheckBoxPreference)findPreference("track1_preference");
        if ( track1Pref == null ) {
        	track1Pref = new CheckBoxPreference(this);
        	track1Pref.setKey("track1_preference");
        	track1Pref.setTitle(R.string.title_track1_preference);
        	trackPrefCat.addPreference(track1Pref);   
        }
        track1Pref.setChecked((KTSyncData.KDCSettings & KTSyncData.TRACK1_MASK) != 0);        

        track2Pref = (CheckBoxPreference)findPreference("track2_preference");
        if ( track2Pref == null ) {        
        	track2Pref = new CheckBoxPreference(this);
        	track2Pref.setKey("track2_preference");
        	track2Pref.setTitle(R.string.title_track2_preference);
        	trackPrefCat.addPreference(track2Pref); 
        }
        track2Pref.setChecked((KTSyncData.KDCSettings & KTSyncData.TRACK2_MASK) != 0);

        track3Pref = (CheckBoxPreference)findPreference("track3_preference");
        if ( track3Pref == null ) {        
        	track3Pref = new CheckBoxPreference(this);
        	track3Pref.setKey("track3_preference");
        	track3Pref.setTitle(R.string.title_track3_preference);
        	trackPrefCat.addPreference(track3Pref); 
        }
        track3Pref.setChecked((KTSyncData.KDCSettings & KTSyncData.TRACK3_MASK) != 0);
        
        return root;
    }
}
