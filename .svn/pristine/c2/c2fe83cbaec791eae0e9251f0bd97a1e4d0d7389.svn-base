package com.giosis.util.qdrive.barcodescanner.bluetooth;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;

import com.giosis.util.qdrive.singapore.R;


public class DataProcess extends PreferenceActivity {
	
    private static final String TAG = "DataProcess";	  
 
    ListPreference buttonPref;
    ListPreference formatPref;
    ListPreference terminatorPref;
    CheckBoxPreference duplicatePref;
    EditTextPreference prefixPref;
    EditTextPreference suffixPref;    
    ListPreference aimPref;
    EditTextPreference startPref;
    EditTextPreference noofcharPref;
    ListPreference actionPref;

    int	WedgeStoreBackup;
    int	BarcodeFormatBackup;
    int	TerminatorBackup;
    int	AimIDBackup;
    int	StartPositionBackup;
    int	NoOfCharsBackup;
    int	ActionBackup;
    
    String PrefixBackup, SuffixBackup;
    
    SharedPreferences.OnSharedPreferenceChangeListener prefListener;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
        
        Log.d(TAG, "DataProcess setting create");

        WedgeStoreBackup = KTSyncData.WedgeStore;
        BarcodeFormatBackup = KTSyncData.BarcodeFormat;
        TerminatorBackup = KTSyncData.Terminator;
        AimIDBackup = KTSyncData.AIM_ID;
        StartPositionBackup = KTSyncData.StartPosition;
        NoOfCharsBackup = KTSyncData.NoOfChars;
        ActionBackup = KTSyncData.Action;    
        PrefixBackup = KTSyncData.Prefix;
        SuffixBackup = KTSyncData.Suffix;
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
        			  if ( key.equals("button_preference") ) {
        				  String newvalue = buttonPref.getValue();
        				  KTSyncData.WedgeStore = buttonPref.findIndexOfValue(newvalue);
        				  buttonPref.setSummary(buttonPref.getEntry().toString());        				  
        			  } else
        			  if ( key.equals("format_preference") ) {
        				  String newvalue = formatPref.getValue();
        				  KTSyncData.BarcodeFormat = formatPref.findIndexOfValue(newvalue);
        				  formatPref.setSummary(formatPref.getEntry().toString());        				  
        			  } else
        			  if ( key.equals("terminator_preference") ) {         				  
        				  String newvalue = terminatorPref.getValue();
        				  KTSyncData.Terminator = terminatorPref.findIndexOfValue(newvalue);
        				  terminatorPref.setSummary(terminatorPref.getEntry().toString());   
        			  } else
        			  if ( key.equals("aim_preference") ) {         				  
        				  String newvalue = aimPref.getValue();
        				  KTSyncData.AIM_ID = aimPref.findIndexOfValue(newvalue);
        				  aimPref.setSummary(aimPref.getEntry().toString());   
        			  } else
        			  if ( key.equals("action_preference") ) {         				  
        				  String newvalue = actionPref.getValue();
        				  KTSyncData.Action = actionPref.findIndexOfValue(newvalue);
        				  actionPref.setSummary(actionPref.getEntry().toString());   
        			  } else
        			  if ( key.equals("prefix_preference") ) {
        				  if ( prefixPref.getText().toString().length() < 12 ) {
        					  KTSyncData.Prefix = prefixPref.getText().toString();
            				  prefixPref.setSummary(KTSyncData.Prefix);
        				  } else 
        					  prefixPref.setText(KTSyncData.Prefix);
        			  } else
        			  if ( key.equals("suffix_preference") ) { 
        				  if ( suffixPref.getText().toString().length() < 12 ) {
        					  KTSyncData.Suffix = suffixPref.getText().toString();
            				  suffixPref.setSummary(KTSyncData.Suffix); 
        				  } else 
        					  suffixPref.setText(KTSyncData.Suffix);
        			  } else
        			  if ( key.equals("start_preference") ) { 
        				  if ( Integer.parseInt(startPref.getText().toString()) < 1000 ) {
        					  KTSyncData.StartPosition = Integer.parseInt(startPref.getText().toString());
        					  startPref.setSummary(Integer.toString(KTSyncData.StartPosition));
        				  } else
        					  startPref.setText(Integer.toString(KTSyncData.StartPosition));
        			  } else
        			  if ( key.equals("noofchar_preference") ) {     
        				  if ( Integer.parseInt(noofcharPref.getText().toString()) < 1000 ) {
        					  KTSyncData.NoOfChars = Integer.parseInt(noofcharPref.getText().toString());
        					  noofcharPref.setSummary(Integer.toString(KTSyncData.NoOfChars));
        				  } else
        					  noofcharPref.setText(Integer.toString(KTSyncData.NoOfChars));
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
    	Log.d(TAG, "DataProcess setting onDestry");
    	
    	KTSyncData.mKScan.WakeupCommand();
    	
    	KTSyncData.KDCSettings &= (~KTSyncData.DUPLICATED_MASK);
    	if ( duplicatePref.isChecked() )	KTSyncData.KDCSettings |= KTSyncData.DUPLICATED_MASK;
    	
        if ( (KTSyncData.KDCSettings & KTSyncData.DUPLICATED_MASK) != (KTSyncData.KDCSettingsBackup & KTSyncData.DUPLICATED_MASK) )
            KTSyncData.mKScan.SendCommandWithValue("GnDS", ((KTSyncData.KDCSettings & KTSyncData.DUPLICATED_MASK) != 0) ? 1: 0  );    	
    	
    	if ( KTSyncData.WedgeStore != WedgeStoreBackup )
    		KTSyncData.mKScan.SendCommandWithValue("U", KTSyncData.WedgeStore);
    	if ( KTSyncData.BarcodeFormat != BarcodeFormatBackup )
    		KTSyncData.mKScan.SendCommandWithValue("w", KTSyncData.BarcodeFormat);    	
    	if ( KTSyncData.Terminator != TerminatorBackup )
    		KTSyncData.mKScan.SendCommandWithValue("GTS", KTSyncData.Terminator);
    	if ( KTSyncData.AIM_ID != AimIDBackup )
    		KTSyncData.mKScan.SendCommandWithValue("GESA", KTSyncData.AIM_ID);   
    	if ( KTSyncData.Action != ActionBackup )
    		KTSyncData.mKScan.SendCommandWithValue("GEST", KTSyncData.Action);
    	
    	if ( ! KTSyncData.Prefix.equals(PrefixBackup) )
    		KTSyncData.mKScan.SendCommandFixData("GESP", KTSyncData.Prefix);
    	
      	if ( ! KTSyncData.Suffix.equals(SuffixBackup) )
    		KTSyncData.mKScan.SendCommandFixData("GESS", KTSyncData.Suffix);
      	
    	if ( KTSyncData.StartPosition != StartPositionBackup )
    		KTSyncData.mKScan.SendCommandWithValue("GESO", KTSyncData.StartPosition);
    	if ( KTSyncData.NoOfChars != NoOfCharsBackup )
    		KTSyncData.mKScan.SendCommandWithValue("GESL", KTSyncData.NoOfChars);
    	
    	KTSyncData.mKScan.FinishCommand();
    	
        super.onDestroy();       
    }
     
    private PreferenceScreen createPreferenceHierarchy() {
        // Root
        PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);
                
        PreferenceCategory dataPrefCat = new PreferenceCategory(this);
        dataPrefCat.setTitle(R.string.data_preferences);
        root.addPreference(dataPrefCat);        
        
        buttonPref = (ListPreference)findPreference("button_preference");
        if ( buttonPref == null ) {
        	buttonPref = new ListPreference(this);
        	buttonPref.setEntries(R.array.entries_wedge_store_preference);
        	buttonPref.setEntryValues(R.array.entryvalues_five_preference);
        	buttonPref.setDialogTitle(R.string.dialog_title_preference);
        	buttonPref.setKey("button_preference");
        	buttonPref.setTitle(R.string.title_wedge_store_preference);
        	dataPrefCat.addPreference(buttonPref);     
        }
    	buttonPref.setValueIndex(KTSyncData.WedgeStore);
    	buttonPref.setSummary(buttonPref.getEntry().toString());
    	
        formatPref = (ListPreference)findPreference("format_preference");
        if ( formatPref == null ) {
        	formatPref = new ListPreference(this);
        	formatPref.setEntries(R.array.entries_data_format_preference);
        	formatPref.setEntryValues(R.array.entryvalues_two_preference);
        	formatPref.setDialogTitle(R.string.dialog_title_preference);
        	formatPref.setKey("format_preference");
        	formatPref.setTitle(R.string.title_data_format_preference);
        	dataPrefCat.addPreference(formatPref); 
        }
    	formatPref.setValueIndex(KTSyncData.BarcodeFormat);
    	formatPref.setSummary(formatPref.getEntry().toString());
    	
        terminatorPref = (ListPreference)findPreference("terminator_preference");
        if ( terminatorPref == null ) {
        	terminatorPref = new ListPreference(this);
        	terminatorPref.setEntries(R.array.entries_terminator_preference);
        	terminatorPref.setEntryValues(R.array.entryvalues_nine_preference);
        	terminatorPref.setDialogTitle(R.string.dialog_title_preference);
        	terminatorPref.setKey("terminator_preference");
        	terminatorPref.setTitle(R.string.title_terminator_preference);
        	dataPrefCat.addPreference(terminatorPref);
        }
    	terminatorPref.setValueIndex(KTSyncData.Terminator);
    	terminatorPref.setSummary(terminatorPref.getEntry().toString());
        
        duplicatePref = (CheckBoxPreference)findPreference("duplicate_preference");
        if ( duplicatePref == null ) {
        	duplicatePref = new CheckBoxPreference(this);
        	duplicatePref.setKey("duplicate_preference");
        	duplicatePref.setTitle(R.string.title_duplicate_preference);
        	dataPrefCat.addPreference(duplicatePref);  
        }
        duplicatePref.setChecked((KTSyncData.KDCSettings & KTSyncData.DUPLICATED_MASK) != 0);
        
        // data edit settings preferences 
        PreferenceCategory editPrefCat = new PreferenceCategory(this);
        editPrefCat.setTitle(R.string.edit_preferences);
        root.addPreference(editPrefCat);  
    
        // Prefix prefernce
        prefixPref = (EditTextPreference)findPreference("prefix_preference");
        if ( prefixPref == null ) {
        	prefixPref = new EditTextPreference(this);
        	prefixPref.setDialogTitle(R.string.dialog_title_prefix_preference);
        	prefixPref.setKey("prefix_preference");
        	prefixPref.setTitle(R.string.title_prefix_preference);
        	editPrefCat.addPreference(prefixPref);
        	EditText prefixEditText = prefixPref.getEditText();
        	InputFilter[] FilterArray = new InputFilter[1];
        	FilterArray[0] = new InputFilter.LengthFilter(11);
        	prefixEditText.setFilters(FilterArray);
        }
    	prefixPref.setSummary(KTSyncData.Prefix);
    	prefixPref.setText(KTSyncData.Prefix);
        // Prefix prefernce
        suffixPref = (EditTextPreference)findPreference("suffix_preference");
        if ( suffixPref == null ) {
        	suffixPref = new EditTextPreference(this);
        	suffixPref.setDialogTitle(R.string.dialog_title_suffix_preference);
        	suffixPref.setKey("suffix_preference");
        	suffixPref.setTitle(R.string.title_suffix_preference);
        	editPrefCat.addPreference(suffixPref);
        	EditText suffixEditText = suffixPref.getEditText();
        	InputFilter[] FilterArray = new InputFilter[1];
        	FilterArray[0] = new InputFilter.LengthFilter(11);
        	suffixEditText.setFilters(FilterArray);        	
        }
    	suffixPref.setSummary(KTSyncData.Suffix);
    	suffixPref.setText(KTSyncData.Suffix);
        //AIM ID
        aimPref = (ListPreference)findPreference("aim_preference");
        if ( aimPref == null ) {
        	aimPref = new ListPreference(this);
        	aimPref.setEntries(R.array.entries_aim_preference);
        	aimPref.setEntryValues(R.array.entryvalues_three_preference);
        	aimPref.setDialogTitle(R.string.dialog_title_preference);
        	aimPref.setKey("aim_preference");
        	aimPref.setTitle(R.string.title_aim_preference);
        	editPrefCat.addPreference(aimPref);
        }
        aimPref.setValueIndex(KTSyncData.AIM_ID);
    	aimPref.setSummary(aimPref.getEntry().toString());
    	
        // edit partial data settings preferences 
        PreferenceCategory partialPrefCat = new PreferenceCategory(this);
        partialPrefCat.setTitle(R.string.partial_preferences);
        root.addPreference(partialPrefCat);

        // start position prefernce
        startPref = (EditTextPreference)findPreference("start_preference");
        if ( startPref == null ) {
        	startPref = new EditTextPreference(this);
        	startPref.setDialogTitle(R.string.dialog_title_number_preference);
        	startPref.setKey("start_preference");
        	startPref.setTitle(R.string.title_start_preference);
        	partialPrefCat.addPreference(startPref);      
        	EditText startEditText = startPref.getEditText();
        	startEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        }
       	startPref.setSummary(Integer.toString(KTSyncData.StartPosition));
       	startPref.setText(Integer.toString(KTSyncData.StartPosition));
        // number of char position prefernce
        noofcharPref = (EditTextPreference)findPreference("noofchar_preference");
        if ( noofcharPref == null ) {
        	noofcharPref = new EditTextPreference(this);
        	noofcharPref.setDialogTitle(R.string.dialog_title_noofchar_preference);
        	noofcharPref.setKey("noofchar_preference");
        	noofcharPref.setTitle(R.string.title_noofchar_preference);
        	partialPrefCat.addPreference(noofcharPref);     
        	EditText noofcharEditText = noofcharPref.getEditText();
        	noofcharEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        }
    	noofcharPref.setSummary(Integer.toString(KTSyncData.NoOfChars));
       	noofcharPref.setText(Integer.toString(KTSyncData.NoOfChars));
        //Action
        actionPref = (ListPreference)findPreference("action_preference");
        if ( actionPref == null ) {
        	actionPref = new ListPreference(this);
        	actionPref.setEntries(R.array.entries_action_preference);
        	actionPref.setEntryValues(R.array.entryvalues_two_preference);
        	actionPref.setDialogTitle(R.string.dialog_title_preference);
        	actionPref.setKey("action_preference");
        	actionPref.setTitle(R.string.title_action_preference);
        	partialPrefCat.addPreference(actionPref);     
        }
    	actionPref.setValueIndex(KTSyncData.Action);
    	actionPref.setSummary(actionPref.getEntry().toString());
    	
        return root;
    }
}
