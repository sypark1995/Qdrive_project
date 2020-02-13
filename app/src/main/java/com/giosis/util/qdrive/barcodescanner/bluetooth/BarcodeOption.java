package com.giosis.util.qdrive.barcodescanner.bluetooth;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.giosis.util.qdrive.singapore.R;

public class BarcodeOption extends PreferenceActivity {
	
    private static final String TAG = "BarcodeOption";	  

	private	PreferenceScreen codabarOptPref;
	private	PreferenceScreen code39OptPref;
	private	PreferenceScreen upcaOptPref;
	private	PreferenceScreen upceOptPref;
	private	PreferenceScreen ean8OptPref;
	private	PreferenceScreen ean13OptPref;
	private	PreferenceScreen gs1OptPref;    
	
	private int tmp;
	
	private PreferenceScreen root;
	
    private CheckBoxPreference ean8as13Pref;
    private CheckBoxPreference upcaas13Pref;    
    private CheckBoxPreference upceasaPref;     
    private CheckBoxPreference upceas13Pref;    
    private CheckBoxPreference ean13cPref;  
    private CheckBoxPreference ean8cPref;   
    private CheckBoxPreference upcacPref;   
    private CheckBoxPreference upcecPref;   
    private CheckBoxPreference code39vPref;    
    private CheckBoxPreference code39rPref;   
    private CheckBoxPreference i2of5vPref;      
    private CheckBoxPreference i2of5rPref;    
    private CheckBoxPreference codabarssPref;

    private CheckBoxPreference cbtrPref;  
    private ListPreference cbconPref;    
    private ListPreference cbvPref;
    
    private CheckBoxPreference c39apPref;  
    private CheckBoxPreference c39fPref;  
    private CheckBoxPreference c39tPref;   
    private ListPreference c39vPref;
    
    private CheckBoxPreference upcavPref;       
    private CheckBoxPreference upcanPref;    
    private CheckBoxPreference upca2Pref;    
    private CheckBoxPreference upca5Pref;   
    private CheckBoxPreference upcarPref;   
    private CheckBoxPreference upcasPref;  
    private CheckBoxPreference upcaePref;
    
    private CheckBoxPreference upcevPref;      
    private CheckBoxPreference upcenPref;   
    private CheckBoxPreference upce2Pref;  
    private CheckBoxPreference upce5Pref;  
    private CheckBoxPreference upcerPref;   
    private CheckBoxPreference upcesPref;  
    private CheckBoxPreference upceePref; 
    
    private CheckBoxPreference ean8vPref;       
    private CheckBoxPreference ean82Pref;   
    private CheckBoxPreference ean85Pref;   
    private CheckBoxPreference ean8rPref;   
    private CheckBoxPreference ean8sPref;    	

    private CheckBoxPreference ean13vPref;      
    private CheckBoxPreference ean132Pref;   
    private CheckBoxPreference ean135Pref;   
    private CheckBoxPreference ean13rPref;   
    private CheckBoxPreference ean13sPref;      
    private CheckBoxPreference ean13iPref;

    private CheckBoxPreference gs1uPref;   
    private ListPreference gs1eoptPref; 
    
    private ListPreference i2of5optPref;
    
    private CheckBoxPreference code11optPref;
    
    private CheckBoxPreference code128optPref;
    
    private ListPreference teleoptPref;  
    
    private CheckBoxPreference msioptPref;  
    
    private ListPreference posioptPref; 
    
    // PostNet Symbology Option
    private CheckBoxPreference postoptPref; 
    
    private CheckBoxPreference planetoptPref;
	
    private SharedPreferences.OnSharedPreferenceChangeListener prefListener;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
        
        Log.d(TAG, "BarcodeOption setting create");
    	
        KTSyncData.OptionsBackup = KTSyncData.Options;
        if ( KTSyncData.bIsKDC300 ) KTSyncData.OptionsExBackup = KTSyncData.OptionsEx;
    	
        setPreferenceScreen(createPreferenceHierarchy());

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
     			  
          			  if ( key.equals("cbcon_preference") ) {
        				  String newvalue = cbconPref.getValue();
        				  KTSyncData.Options &= ~KTSyncData.CB_CONCATENATE_MASK;
        				  KTSyncData.Options |= (cbconPref.findIndexOfValue(newvalue) << KTSyncData.CB_CONCATENATE_SHIFT);
        				  cbconPref.setSummary(cbconPref.getEntry().toString());        				  
        			  } 
          			  if ( key.equals("cbv_preference") ) {
        				  String newvalue = cbvPref.getValue();
        				  KTSyncData.Options &= ~KTSyncData.CB_CHKDGT_MASK;
        				  KTSyncData.Options |= (cbvPref.findIndexOfValue(newvalue) << KTSyncData.CB_CHKDGT_SHIFT);
        				  cbvPref.setSummary(cbvPref.getEntry().toString());        				  
        			  }
          			  if ( key.equals("c39v_preference") ) {
        				  String newvalue = c39vPref.getValue();
        				  KTSyncData.Options &= ~KTSyncData.C39_CHKDGT_MASK;
        				  KTSyncData.Options |= (c39vPref.findIndexOfValue(newvalue) << KTSyncData.C39_CHKDGT_SHIFT);
        				  c39vPref.setSummary(c39vPref.getEntry().toString());        				  
        			  }
          			  if ( key.equals("gs1eopt_preference") ) {
        				  String newvalue = gs1eoptPref.getValue();
        				  KTSyncData.OptionsEx &= ~KTSyncData.EANUCC_EMUL_MASK;
        				  KTSyncData.OptionsEx |= (gs1eoptPref.findIndexOfValue(newvalue) << KTSyncData.EANUCC_EMUL_SHIFT);
        				  gs1eoptPref.setSummary(gs1eoptPref.getEntry().toString());        				  
        			  }
          			  if ( key.equals("i2of5opt_preference") ) {
        				  String newvalue = i2of5optPref.getValue();
        				  KTSyncData.Options &= ~KTSyncData.I2OF5_OPTION_MASK;
        				  KTSyncData.Options |= (i2of5optPref.findIndexOfValue(newvalue) << KTSyncData.I2OF5_OPTION_SHIFT);
        				  i2of5optPref.setSummary(i2of5optPref.getEntry().toString());        				  
        			  }
          			  if ( key.equals("teleopt_preference") ) {
        				  String newvalue = teleoptPref.getValue();
        				  KTSyncData.Options &= ~KTSyncData.TELEPEN_OPTION_MASK;
        				  KTSyncData.Options |= (teleoptPref.findIndexOfValue(newvalue) << KTSyncData.TELEPEN_OPTION_SHIFT);
        				  teleoptPref.setSummary(teleoptPref.getEntry().toString());        				  
        			  }
          			  if ( key.equals("posiopt_preference") ) {
        				  String newvalue = posioptPref.getValue();
        				  KTSyncData.Options &= ~KTSyncData.POSICODE_OPTION_MASK;
        				  KTSyncData.Options |= (posioptPref.findIndexOfValue(newvalue) << KTSyncData.POSICODE_OPTION_SHIFT);
        				  posioptPref.setSummary(posioptPref.getEntry().toString());        				  
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
    	Log.d(TAG, "BarcodeOption setting onDestry");
    	
    	KTSyncData.mKScan.WakeupCommand();
    	
    	if ( ! KTSyncData.bIsKDC300 ) {
	    	KTSyncData.Options &= KTSyncData.OPTIONS_MASK;
	    	
	    	if ( ean8as13Pref.isChecked() )	KTSyncData.Options |= KTSyncData.EAN8AS13_MASK;
	    	if ( upcaas13Pref.isChecked() )	KTSyncData.Options |= KTSyncData.UPCAAS13_MASK;    
	    	if ( upceasaPref.isChecked() )	KTSyncData.Options |= KTSyncData.UPCEASA_MASK;     
	    	if ( upceas13Pref.isChecked() )	KTSyncData.Options |= KTSyncData.UPCEAS13_MASK;    
	    	if ( ean13cPref.isChecked() )	KTSyncData.Options |= KTSyncData.EAN13C_MASK;  
	    	if ( ean8cPref.isChecked() )	KTSyncData.Options |= KTSyncData.EAN8C_MASK;   
	    	if ( upcacPref.isChecked() )	KTSyncData.Options |= KTSyncData.UPCAC_MASK;   
	    	if ( upcecPref.isChecked() )	KTSyncData.Options |= KTSyncData.UPCEC_MASK;   
	    	if ( code39vPref.isChecked() )	KTSyncData.Options |= KTSyncData.CODE39V_MASK;    
	    	if ( code39rPref.isChecked() )	KTSyncData.Options |= KTSyncData.CODE39R_MASK;   
	    	if ( i2of5vPref.isChecked() )	KTSyncData.Options |= KTSyncData.I2OF5V_MASK;      
	    	if ( i2of5rPref.isChecked() )	KTSyncData.Options |= KTSyncData.I2OF5R_MASK;    
	    	if ( codabarssPref.isChecked() )	KTSyncData.Options |= KTSyncData.CODABARSS_MASK;
	    	
	        if ( KTSyncData.Options != KTSyncData.OptionsBackup  )
	            KTSyncData.mKScan.SendCommandWithValue("O", KTSyncData.Options );
    	} else {
   		
//Codabar	    	
    		KTSyncData.Options &= (~KTSyncData.CB_TXSTARTSTOP_MASK);
	        if ( cbtrPref.isChecked() )		KTSyncData.Options |= KTSyncData.CB_TXSTARTSTOP_MASK;
//Code39
    		KTSyncData.Options &= (~(KTSyncData.C39_TXSTARTSTOP_MASK | KTSyncData.C39_APPEND_MASK | KTSyncData.C39_FULLASCII_MASK ));
	        if ( c39tPref.isChecked() )		KTSyncData.Options |= KTSyncData.C39_TXSTARTSTOP_MASK;    
	        if ( c39fPref.isChecked() )		KTSyncData.Options |= KTSyncData.C39_FULLASCII_MASK;
	        if ( c39apPref.isChecked() )	KTSyncData.Options |= KTSyncData.C39_APPEND_MASK;
//UPCA	       
    		KTSyncData.OptionsEx &= (~KTSyncData.UPCA_OPTION_MASK);
    		if ( upcavPref.isChecked() )		KTSyncData.OptionsEx |= KTSyncData.UPCA_VERIFYCHKDGT_MASK;       
    		if ( upcanPref.isChecked() )		KTSyncData.OptionsEx |= KTSyncData.UPCA_NUMBERSYS_MASK;
    		if ( upca2Pref.isChecked() )		KTSyncData.OptionsEx |= KTSyncData.UPCA_2ADDENDA_MASK;
    		if ( upca5Pref.isChecked() )		KTSyncData.OptionsEx |= KTSyncData.UPCA_5ADDENDA_MASK;
    		if ( upcarPref.isChecked() )		KTSyncData.OptionsEx |= KTSyncData.UPCA_REQADDENDA_MASK;
    		if ( upcasPref.isChecked() )		KTSyncData.OptionsEx |= KTSyncData.UPCA_ADDENDASEP_MASK;
    		if ( upcaePref.isChecked() )		KTSyncData.OptionsEx |= KTSyncData.UPCA_COUPONCODE_MASK;
//UPCE	       
    		KTSyncData.OptionsEx &= (~KTSyncData.UPCE_OPTION_MASK);
    		if ( upcevPref.isChecked() )		KTSyncData.OptionsEx |= KTSyncData.UPCE_CHECKDGT_MASK;      
    	    if ( upcenPref.isChecked() )		KTSyncData.OptionsEx |= KTSyncData.UPCE_NUMBERSYS_MASK;
    	    if ( upce2Pref.isChecked() )		KTSyncData.OptionsEx |= KTSyncData.UPCE_2ADDENDA_MASK;
    	    if ( upce5Pref.isChecked() )		KTSyncData.OptionsEx |= KTSyncData.UPCE_5ADDENDA_MASK;
    	    if ( upcerPref.isChecked() )		KTSyncData.OptionsEx |= KTSyncData.UPCE_REQADDENDA_MASK;
    	    if ( upcesPref.isChecked() )		KTSyncData.OptionsEx |= KTSyncData.UPCE_ADDENDASEP_MASK;
    	    if ( upceePref.isChecked() )		KTSyncData.OptionsEx |= KTSyncData.UPCE_EXPAND_MASK;
//EAN8	       
    		KTSyncData.OptionsEx &= (~KTSyncData.EAN8_OPTION_MASK);
    	    if ( ean8vPref.isChecked() )		KTSyncData.OptionsEx |= KTSyncData.E8_VERIFYCHKDGT_MASK;       
    	    if ( ean82Pref.isChecked() )		KTSyncData.OptionsEx |= KTSyncData.E8_2ADDENDA_MASK;   
    	    if ( ean85Pref.isChecked() )		KTSyncData.OptionsEx |= KTSyncData.E8_5ADDENDA_MASK;   
    	    if ( ean8rPref.isChecked() )		KTSyncData.OptionsEx |= KTSyncData.E8_REQADDENDA_MASK;   
    	    if ( ean8sPref.isChecked() )		KTSyncData.OptionsEx |= KTSyncData.E8_ADDENDASEP_MASK;
//EAN13	       
    		KTSyncData.OptionsEx &= (~KTSyncData.EAN13_OPTION_MASK);
    	    if ( ean13vPref.isChecked() )		KTSyncData.OptionsEx |= KTSyncData.E13_VERIFYCHKDGT_MASK;      
    	    if ( ean132Pref.isChecked() )		KTSyncData.OptionsEx |= KTSyncData.E13_2ADDENDA_MASK;   
    	    if ( ean135Pref.isChecked() )		KTSyncData.OptionsEx |= KTSyncData.E13_5ADDENDA_MASK;   
    	    if ( ean13rPref.isChecked() )		KTSyncData.OptionsEx |= KTSyncData.E13_REQADDENDA_MASK;   
    	    if ( ean13sPref.isChecked() )		KTSyncData.OptionsEx |= KTSyncData.E13_ADDENDASEP_MASK;      
    	    if ( ean13iPref.isChecked() )		KTSyncData.OptionsEx |= KTSyncData.E13_ISBNTRANS_MASK;
//GS1128	       
    		KTSyncData.OptionsEx &= (~KTSyncData.UPC_EAN_VERSION_MASK);
    	    if ( gs1uPref.isChecked() )		KTSyncData.OptionsEx |= KTSyncData.UPC_EAN_VERSION_MASK; 
//Code11  
    		KTSyncData.Options &= (~KTSyncData.CODE11_OPTION_MASK);    	    
    		if ( code11optPref.isChecked() )		KTSyncData.Options |= KTSyncData.CODE11_OPTION_MASK;
//Code128    	    
    		KTSyncData.Options &= (~KTSyncData.CODE128_OPTION_MASK);    	    
    		if ( code128optPref.isChecked() )		KTSyncData.Options |= KTSyncData.CODE128_OPTION_MASK;
//MSI
    		KTSyncData.OptionsEx &= (~KTSyncData.MSI_OPTION_MASK);
    		if ( msioptPref.isChecked() )		KTSyncData.OptionsEx |= KTSyncData.MSI_OPTION_MASK; 
// PostNet 
    		KTSyncData.OptionsEx &= (~KTSyncData.POSTNET_OPTION_MASK);
    		if ( postoptPref.isChecked() )		KTSyncData.OptionsEx |= KTSyncData.POSTNET_OPTION_MASK; 
//Planet    	    
    		KTSyncData.OptionsEx &= (~KTSyncData.PLANET_OPTION_MASK);
    		if ( planetoptPref.isChecked() )		KTSyncData.OptionsEx |= KTSyncData.PLANET_OPTION_MASK;    		
//	        
	        if ( (KTSyncData.Options != KTSyncData.OptionsBackup) || 
	        	 (KTSyncData.OptionsEx != KTSyncData.OptionsExBackup)	)
	            KTSyncData.mKScan.SendCommandWithValueEx("O", KTSyncData.Options, KTSyncData.OptionsEx );
       
    	}

    	KTSyncData.mKScan.FinishCommand();
        super.onDestroy();       
    }
    
    private PreferenceScreen createPreferenceHierarchy() {
        // Root
        
		root = getPreferenceManager().createPreferenceScreen(this);
		
    	if ( KTSyncData.bIsKDC300 ) {

	        // Codabar Symbology Option
	        codabarOptPref = getPreferenceManager().createPreferenceScreen(this);
	        codabarOptPref.setKey("codabarOpt_preference");
	        codabarOptPref.setTitle(R.string.title_codabarOpt_preference);
	        root.addPreference(codabarOptPref);

	        CodabarSymbologyOption();
        
	        // Code39 Symbology Option
	        code39OptPref = getPreferenceManager().createPreferenceScreen(this);
	        code39OptPref.setKey("code39Opt_preference");
	        code39OptPref.setTitle(R.string.title_code39Opt_preference);
	        root.addPreference(code39OptPref);

	        Code39SymbologyOption();
	        
	        // UPCA Symbology Option
	        upcaOptPref = getPreferenceManager().createPreferenceScreen(this);
	        upcaOptPref.setKey("upcaOpt_preference");
	        upcaOptPref.setTitle(R.string.title_upcaOpt_preference);
	        root.addPreference(upcaOptPref);

	        UPCASymbologyOption();
	        
	        // UPCE Symbology Option
	        upceOptPref = getPreferenceManager().createPreferenceScreen(this);
	        upceOptPref.setKey("upceOpt_preference");
	        upceOptPref.setTitle(R.string.title_upceOpt_preference);
	        root.addPreference(upceOptPref);

	        UPCESymbologyOption();
	        
	        // EAN8 Symbology Option
	        ean8OptPref = getPreferenceManager().createPreferenceScreen(this);
	        ean8OptPref.setKey("ean8Opt_preference");
	        ean8OptPref.setTitle(R.string.title_ean8Opt_preference);
	        root.addPreference(ean8OptPref);

	        EAN8SymbologyOption();

	        // EAN13 Symbology Option
	        ean13OptPref = getPreferenceManager().createPreferenceScreen(this);
	        ean13OptPref.setKey("ean13Opt_preference");
	        ean13OptPref.setTitle(R.string.title_ean13Opt_preference);
	        root.addPreference(ean13OptPref);

	        EAN13SymbologyOption();
	        
	        // GS1 Symbology Option
	        gs1OptPref = getPreferenceManager().createPreferenceScreen(this);
	        gs1OptPref.setKey("gs1Opt_preference");
	        gs1OptPref.setTitle(R.string.title_gs1Opt_preference);
	        root.addPreference(gs1OptPref);

	        GS1SymbologyOption();
    
	        //  I2Of5Symbology Option
	        i2of5optPref = new ListPreference(this);
	        i2of5optPref.setEntries(R.array.entries_verify_preference);
	        i2of5optPref.setEntryValues(R.array.entryvalues_three_preference);
	        i2of5optPref.setDialogTitle(R.string.dialog_title_preference);
	        i2of5optPref.setKey("i2of5opt_preference");
	        i2of5optPref.setTitle(R.string.title_i2of5_verify_preference);
	        root.addPreference(i2of5optPref);
	        tmp = (KTSyncData.Options & KTSyncData.I2OF5_OPTION_MASK) >> KTSyncData.I2OF5_OPTION_SHIFT;
        	i2of5optPref.setValueIndex(tmp);
			i2of5optPref.setSummary(i2of5optPref.getEntry().toString());
	        
	        code11optPref = new CheckBoxPreference(this);
	        code11optPref.setKey("code11opt_preference");
	        code11optPref.setTitle(R.string.title_code11_checkdigit_preference);
	        root.addPreference(code11optPref);
			code11optPref.setChecked((KTSyncData.Options & KTSyncData.CODE11_OPTION_MASK) != 0);
      
	        code128optPref = new CheckBoxPreference(this);
	        code128optPref.setKey("code128opt_preference");
	        code128optPref.setTitle(R.string.title_code128_isbt_preference);
	        root.addPreference(code128optPref);
			code128optPref.setChecked((KTSyncData.Options & KTSyncData.CODE128_OPTION_MASK) != 0);
				  	        
	        teleoptPref = new ListPreference(this);
	        teleoptPref.setEntries(R.array.entries_output_preference);
	        teleoptPref.setEntryValues(R.array.entryvalues_two_preference);
	        teleoptPref.setDialogTitle(R.string.dialog_title_preference);
	        teleoptPref.setKey("teleopt_preference");
	        teleoptPref.setTitle(R.string.title_output_preference);
	        root.addPreference(teleoptPref);
	        
	        tmp = (KTSyncData.Options & KTSyncData.TELEPEN_OPTION_MASK) >> KTSyncData.TELEPEN_OPTION_SHIFT;
        	teleoptPref.setValueIndex(tmp);
			teleoptPref.setSummary(teleoptPref.getEntry().toString());
	       
	        msioptPref = new CheckBoxPreference(this);
	        msioptPref.setKey("msiopt_preference");
	        msioptPref.setTitle(R.string.title_msi_verify_transmit_preference);
	        root.addPreference(msioptPref);
	        msioptPref.setChecked((KTSyncData.OptionsEx & KTSyncData.MSI_OPTION_MASK) != 0);
	        
	        
	        posioptPref = new ListPreference(this);
	        posioptPref.setEntries(R.array.entries_posi_option_preference);
	        posioptPref.setEntryValues(R.array.entryvalues_three_preference);
	        posioptPref.setDialogTitle(R.string.dialog_title_preference);
	        posioptPref.setKey("posiopt_preference");
	        posioptPref.setTitle(R.string.title_posi_option_preference);
	        root.addPreference(posioptPref);
	        
	        tmp = ((KTSyncData.Options & KTSyncData.POSICODE_OPTION_MASK) >> KTSyncData.POSICODE_OPTION_SHIFT);
	        tmp &= 0x00000003;
	        
        	posioptPref.setValueIndex(tmp);
			posioptPref.setSummary(posioptPref.getEntry().toString());
	        
	        // PostNet Symbology Option
	        postoptPref = new CheckBoxPreference(this);
	        postoptPref.setKey("postopt_preference");
	        postoptPref.setTitle(R.string.title_post_verify_transmit_preference);
	        root.addPreference(postoptPref); 
	        postoptPref.setChecked((KTSyncData.OptionsEx & KTSyncData.POSTNET_OPTION_MASK) != 0);
	        
	        planetoptPref = new CheckBoxPreference(this);
	        planetoptPref.setKey("planetopt_preference");
	        planetoptPref.setTitle(R.string.title_planet_verify_transmit_preference);
	        root.addPreference(planetoptPref); 
	        planetoptPref.setChecked((KTSyncData.OptionsEx & KTSyncData.PLANET_OPTION_MASK) != 0);
        
    	} else
    		LaserSymbologyOption();    	
   	
        return root;
    }
    
    private void LaserSymbologyOption()
    {
    	//ean8as13Pref = (CheckBoxPreference)findPreference("ean8as13_preference");
		//if ( ean8as13Pref == null ) {
			ean8as13Pref = new CheckBoxPreference(this);
			ean8as13Pref.setKey("ean8as13_preference");
			ean8as13Pref.setTitle(R.string.title_ean8as13_preference);
			root.addPreference(ean8as13Pref);    	
		//}

    	//upcaas13Pref = (CheckBoxPreference)findPreference("upcaas13_preference");
		//if ( upcaas13Pref == null ) {
			upcaas13Pref = new CheckBoxPreference(this);
			upcaas13Pref.setKey("upcaas13_preference");
			upcaas13Pref.setTitle(R.string.title_upcaas13_preference);
			root.addPreference(upcaas13Pref);  
		//}
		
		//upceasaPref = (CheckBoxPreference)findPreference("upceasa_preference");
		//if ( upceasaPref == null ) {
			upceasaPref = new CheckBoxPreference(this);
			upceasaPref.setKey("upceasa_preference");
			upceasaPref.setTitle(R.string.title_upceasa_preference);
			root.addPreference(upceasaPref);  
		//}
		
		//upcaas13Pref = (CheckBoxPreference)findPreference("upcaas13_preference");
		//if ( upcaas13Pref == null ) {
			upceas13Pref = new CheckBoxPreference(this);
			upceas13Pref.setKey("upceas13_preference");
			upceas13Pref.setTitle(R.string.title_upceas13_preference);
			root.addPreference(upceas13Pref);  
		//}
		
		//ean13cPref = (CheckBoxPreference)findPreference("ean13c_preference");
		//if ( ean13cPref == null ) {
			ean13cPref = new CheckBoxPreference(this);
			ean13cPref.setKey("ean13c_preference");
			ean13cPref.setTitle(R.string.title_ean13c_preference);
			root.addPreference(ean13cPref); 
		//}
		
		//ean8cPref = (CheckBoxPreference)findPreference("ean8c_preference");
		//if ( ean8cPref == null ) {
			ean8cPref = new CheckBoxPreference(this);
			ean8cPref.setKey("ean8c_preference");
			ean8cPref.setTitle(R.string.title_ean8c_preference);
			root.addPreference(ean8cPref); 
		//}
		
		//upcacPref = (CheckBoxPreference)findPreference("upcac_preference");
		//if ( upcacPref == null ) {
			upcacPref = new CheckBoxPreference(this);
			upcacPref.setKey("upcac_preference");
        	upcacPref.setTitle(R.string.title_upcac_preference);
        	root.addPreference(upcacPref); 
		//}
        
        //upcecPref = (CheckBoxPreference)findPreference("upcec_preference");
		//if ( upcecPref == null ) {
			upcecPref = new CheckBoxPreference(this);
			upcecPref.setKey("upcec_preference");
        	upcecPref.setTitle(R.string.title_upcec_preference);
        	root.addPreference(upcecPref); 
		//}
        
        //code39vPref = (CheckBoxPreference)findPreference("code39v_preference");
		//if ( code39vPref == null ) {
			code39vPref = new CheckBoxPreference(this);
			code39vPref.setKey("code39v_preference");
        	code39vPref.setTitle(R.string.title_code39v_preference);
        	root.addPreference(code39vPref); 
		//}
        
        //code39rPref = (CheckBoxPreference)findPreference("code39r_preference");
		//if ( code39rPref == null ) {
			code39rPref = new CheckBoxPreference(this);
			code39rPref.setKey("code39r_preference");
        	code39rPref.setTitle(R.string.title_code39r_preference);
        	root.addPreference(code39rPref); 
		//}
        
        //i2of5vPref = (CheckBoxPreference)findPreference("i2of5v_preference");
		//if ( i2of5vPref == null ) {
			i2of5vPref = new CheckBoxPreference(this);
			i2of5vPref.setKey("i2of5v_preference");
        	i2of5vPref.setTitle(R.string.title_i2of5v_preference);
        	root.addPreference(i2of5vPref);    
		//}
		
        //i2of5rPref = (CheckBoxPreference)findPreference("i2of5r_preference");
		//if ( i2of5rPref == null ) {
			i2of5rPref = new CheckBoxPreference(this);
			i2of5rPref.setKey("i2of5r_preference");
        	i2of5rPref.setTitle(R.string.title_i2of5r_preference);
        	root.addPreference(i2of5rPref); 
		//}
        
        //codabarssPref = (CheckBoxPreference)findPreference("codabarss_preference");
		//if ( codabarssPref == null ) {
			codabarssPref = new CheckBoxPreference(this);
			codabarssPref.setKey("codabarss_preference");
			codabarssPref.setTitle(R.string.title_codabarss_preference);
			root.addPreference(codabarssPref); 
		//}
			
	    	ean8as13Pref.setChecked((KTSyncData.Options & KTSyncData.EAN8AS13_MASK) != 0);
	    	upcaas13Pref.setChecked((KTSyncData.Options & KTSyncData.UPCAAS13_MASK) != 0);    
	    	upceasaPref.setChecked((KTSyncData.Options & KTSyncData.UPCEASA_MASK) != 0);     
	    	upceas13Pref.setChecked((KTSyncData.Options & KTSyncData.UPCEAS13_MASK) != 0);    
	    	ean13cPref.setChecked((KTSyncData.Options & KTSyncData.EAN13C_MASK) != 0);  
	    	ean8cPref.setChecked((KTSyncData.Options & KTSyncData.EAN8C_MASK) != 0);   
	    	upcacPref.setChecked((KTSyncData.Options & KTSyncData.UPCAC_MASK) != 0);   
	    	upcecPref.setChecked((KTSyncData.Options & KTSyncData.UPCEC_MASK) != 0);   
	    	code39vPref.setChecked((KTSyncData.Options & KTSyncData.CODE39V_MASK) != 0);    
	    	code39rPref.setChecked((KTSyncData.Options & KTSyncData.CODE39R_MASK) != 0);   
	    	i2of5vPref.setChecked((KTSyncData.Options & KTSyncData.I2OF5V_MASK) != 0);      
	    	i2of5rPref.setChecked((KTSyncData.Options & KTSyncData.I2OF5R_MASK) != 0);    
	    	codabarssPref.setChecked((KTSyncData.Options & KTSyncData.CODABARSS_MASK) != 0);			
	}
    
    private void CodabarSymbologyOption()
    {
        cbtrPref = new CheckBoxPreference(this);
        cbtrPref.setKey("cbtr_preference");
        cbtrPref.setTitle(R.string.title_transmit_preference);
        codabarOptPref.addPreference(cbtrPref);
        cbtrPref.setChecked((KTSyncData.Options & KTSyncData.CB_TXSTARTSTOP_MASK) != 0);
        
        cbconPref = new ListPreference(this);
        cbconPref.setEntries(R.array.entries_cbcon_preference);
        cbconPref.setEntryValues(R.array.entryvalues_three_preference);
        cbconPref.setDialogTitle(R.string.dialog_title_preference);
        cbconPref.setKey("cbcon_preference");
        cbconPref.setTitle(R.string.title_cbcon_preference);
        codabarOptPref.addPreference(cbconPref);
        tmp = (KTSyncData.Options & KTSyncData.CB_CONCATENATE_MASK) >> KTSyncData.CB_CONCATENATE_SHIFT;
		cbconPref.setValueIndex(tmp);
		cbconPref.setSummary(cbconPref.getEntry().toString());
        
        cbvPref = new ListPreference(this);
        cbvPref.setEntries(R.array.entries_verify_preference);
        cbvPref.setEntryValues(R.array.entryvalues_three_preference);
        cbvPref.setDialogTitle(R.string.dialog_title_preference);
        cbvPref.setKey("cbv_preference");
        cbvPref.setTitle(R.string.title_verify_preference);
        codabarOptPref.addPreference(cbvPref);
        tmp = (KTSyncData.Options & KTSyncData.CB_CHKDGT_MASK) >> KTSyncData.CB_CHKDGT_SHIFT;
		cbvPref.setValueIndex(tmp);
		cbvPref.setSummary(cbvPref.getEntry().toString());
    }
    private void Code39SymbologyOption()
    {
        c39apPref = new CheckBoxPreference(this);
        c39apPref.setKey("c39ap_preference");
        c39apPref.setTitle(R.string.title_append_preference);
        code39OptPref.addPreference(c39apPref);
        c39apPref.setChecked((KTSyncData.Options & KTSyncData.C39_APPEND_MASK) != 0);
        
        c39fPref = new CheckBoxPreference(this);
        c39fPref.setKey("c39f_preference");
        c39fPref.setTitle(R.string.title_fullascii_preference);
        code39OptPref.addPreference(c39fPref);
        c39fPref.setChecked((KTSyncData.Options & KTSyncData.C39_FULLASCII_MASK) != 0);
        
        c39tPref = new CheckBoxPreference(this);
        c39tPref.setKey("c39t_preference");
        c39tPref.setTitle(R.string.title_transmit_preference);
        code39OptPref.addPreference(c39tPref);
        c39tPref.setChecked((KTSyncData.Options & KTSyncData.C39_TXSTARTSTOP_MASK) != 0);
        
        c39vPref = new ListPreference(this);
        c39vPref.setEntries(R.array.entries_verify_preference);
        c39vPref.setEntryValues(R.array.entryvalues_three_preference);
        c39vPref.setDialogTitle(R.string.dialog_title_preference);
        c39vPref.setKey("c39v_preference");
        c39vPref.setTitle(R.string.title_verify_preference);
        code39OptPref.addPreference(c39vPref);
        tmp = (KTSyncData.Options & KTSyncData.C39_CHKDGT_MASK) >> KTSyncData.C39_CHKDGT_SHIFT;
		c39vPref.setValueIndex(tmp);
		c39vPref.setSummary(c39vPref.getEntry().toString());
    }
    private void UPCASymbologyOption()
    {
        upcavPref = new CheckBoxPreference(this);
        upcavPref.setKey("upcav_preference");
        upcavPref.setTitle(R.string.title_checkdigit_preference);
        upcaOptPref.addPreference(upcavPref);   
        
        upcanPref = new CheckBoxPreference(this);
        upcanPref.setKey("upcan_preference");
        upcanPref.setTitle(R.string.title_number_preference);
        upcaOptPref.addPreference(upcanPref);
        
        upca2Pref = new CheckBoxPreference(this);
        upca2Pref.setKey("upca2_preference");
        upca2Pref.setTitle(R.string.title_2digit_preference);
        upcaOptPref.addPreference(upca2Pref);
        
        upca5Pref = new CheckBoxPreference(this);
        upca5Pref.setKey("upca5_preference");
        upca5Pref.setTitle(R.string.title_5digit_preference);
        upcaOptPref.addPreference(upca5Pref);
        
        upcarPref = new CheckBoxPreference(this);
        upcarPref.setKey("upcar_preference");
        upcarPref.setTitle(R.string.title_addrequired_preference);
        upcaOptPref.addPreference(upcarPref);
        
        upcasPref = new CheckBoxPreference(this);
        upcasPref.setKey("upcas_preference");
        upcasPref.setTitle(R.string.title_addseparator_preference);
        upcaOptPref.addPreference(upcasPref);
        
        upcaePref = new CheckBoxPreference(this);
        upcaePref.setKey("upcae_preference");
        upcaePref.setTitle(R.string.title_extcoupon_preference);
        upcaOptPref.addPreference(upcaePref);
        
        c39tPref.setChecked((KTSyncData.Options & KTSyncData.C39_TXSTARTSTOP_MASK) != 0);
        
        upcavPref.setChecked((KTSyncData.OptionsEx & KTSyncData.UPCA_VERIFYCHKDGT_MASK) != 0);       
        upcanPref.setChecked((KTSyncData.OptionsEx & KTSyncData.UPCA_NUMBERSYS_MASK) != 0);
        upca2Pref.setChecked((KTSyncData.OptionsEx & KTSyncData.UPCA_2ADDENDA_MASK) != 0);
        upca5Pref.setChecked((KTSyncData.OptionsEx & KTSyncData.UPCA_5ADDENDA_MASK) != 0);
        upcarPref.setChecked((KTSyncData.OptionsEx & KTSyncData.UPCA_REQADDENDA_MASK) != 0);
        upcasPref.setChecked((KTSyncData.OptionsEx & KTSyncData.UPCA_ADDENDASEP_MASK) != 0);
        upcaePref.setChecked((KTSyncData.OptionsEx & KTSyncData.UPCA_COUPONCODE_MASK) != 0);
    }
    private void UPCESymbologyOption()
    {
        upcevPref = new CheckBoxPreference(this);
        upcevPref.setKey("upcev_preference");
        upcevPref.setTitle(R.string.title_checkdigit_preference);
        upceOptPref.addPreference(upcevPref);   
        
        upcenPref = new CheckBoxPreference(this);
        upcenPref.setKey("upcen_preference");
        upcenPref.setTitle(R.string.title_number_preference);
        upceOptPref.addPreference(upcenPref);
        
        upce2Pref = new CheckBoxPreference(this);
        upce2Pref.setKey("upce2_preference");
        upce2Pref.setTitle(R.string.title_2digit_preference);
        upceOptPref.addPreference(upce2Pref);
        
        upce5Pref = new CheckBoxPreference(this);
        upce5Pref.setKey("upce5_preference");
        upce5Pref.setTitle(R.string.title_5digit_preference);
        upceOptPref.addPreference(upce5Pref);
        
        upcerPref = new CheckBoxPreference(this);
        upcerPref.setKey("upcer_preference");
        upcerPref.setTitle(R.string.title_addrequired_preference);
        upceOptPref.addPreference(upcerPref);
        
        upcesPref = new CheckBoxPreference(this);
        upcesPref.setKey("upces_preference");
        upcesPref.setTitle(R.string.title_addseparator_preference);
        upceOptPref.addPreference(upcesPref);
        
        upceePref = new CheckBoxPreference(this);
        upceePref.setKey("upcee_preference");
        upceePref.setTitle(R.string.title_expand_preference);
        upceOptPref.addPreference(upceePref);    	
        
		upcevPref.setChecked((KTSyncData.OptionsEx & KTSyncData.UPCE_CHECKDGT_MASK) != 0);      
	    upcenPref.setChecked((KTSyncData.OptionsEx & KTSyncData.UPCE_NUMBERSYS_MASK) != 0);
	    upce2Pref.setChecked((KTSyncData.OptionsEx & KTSyncData.UPCE_2ADDENDA_MASK) != 0);
	    upce5Pref.setChecked((KTSyncData.OptionsEx & KTSyncData.UPCE_5ADDENDA_MASK) != 0);
	    upcerPref.setChecked((KTSyncData.OptionsEx & KTSyncData.UPCE_REQADDENDA_MASK) != 0);
	    upcesPref.setChecked((KTSyncData.OptionsEx & KTSyncData.UPCE_ADDENDASEP_MASK) != 0);
	    upceePref.setChecked((KTSyncData.OptionsEx & KTSyncData.UPCE_EXPAND_MASK) != 0);
    }
    private void EAN8SymbologyOption()
    {
        ean8vPref = new CheckBoxPreference(this);
        ean8vPref.setKey("ean8v_preference");
        ean8vPref.setTitle(R.string.title_checkdigit_preference);
        ean8OptPref.addPreference(ean8vPref);   
        
        ean82Pref = new CheckBoxPreference(this);
        ean82Pref.setKey("ean82_preference");
        ean82Pref.setTitle(R.string.title_2digit_preference);
        ean8OptPref.addPreference(ean82Pref);
        
        ean85Pref = new CheckBoxPreference(this);
        ean85Pref.setKey("ean85_preference");
        ean85Pref.setTitle(R.string.title_5digit_preference);
        ean8OptPref.addPreference(ean85Pref);
        
        ean8rPref = new CheckBoxPreference(this);
        ean8rPref.setKey("ean8r_preference");
        ean8rPref.setTitle(R.string.title_addrequired_preference);
        ean8OptPref.addPreference(ean8rPref);
        
        ean8sPref = new CheckBoxPreference(this);
        ean8sPref.setKey("ean8s_preference");
        ean8sPref.setTitle(R.string.title_addseparator_preference);
        ean8OptPref.addPreference(ean8sPref);
        
	    ean8vPref.setChecked((KTSyncData.OptionsEx & KTSyncData.E8_VERIFYCHKDGT_MASK) != 0);       
	    ean82Pref.setChecked((KTSyncData.OptionsEx & KTSyncData.E8_2ADDENDA_MASK) != 0);   
	    ean85Pref.setChecked((KTSyncData.OptionsEx & KTSyncData.E8_5ADDENDA_MASK) != 0);   
	    ean8rPref.setChecked((KTSyncData.OptionsEx & KTSyncData.E8_REQADDENDA_MASK) != 0);   
	    ean8sPref.setChecked((KTSyncData.OptionsEx & KTSyncData.E8_ADDENDASEP_MASK) != 0);
    }
    private void EAN13SymbologyOption()
    {
        ean13vPref = new CheckBoxPreference(this);
        ean13vPref.setKey("ean13v_preference");
        ean13vPref.setTitle(R.string.title_checkdigit_preference);
        ean13OptPref.addPreference(ean13vPref);   
        
        ean132Pref = new CheckBoxPreference(this);
        ean132Pref.setKey("ean132_preference");
        ean132Pref.setTitle(R.string.title_2digit_preference);
        ean13OptPref.addPreference(ean132Pref);
        
        ean135Pref = new CheckBoxPreference(this);
        ean135Pref.setKey("ean135_preference");
        ean135Pref.setTitle(R.string.title_5digit_preference);
        ean13OptPref.addPreference(ean135Pref);
        
        ean13rPref = new CheckBoxPreference(this);
        ean13rPref.setKey("ean13r_preference");
        ean13rPref.setTitle(R.string.title_addrequired_preference);
        ean13OptPref.addPreference(ean13rPref);
        
        ean13sPref = new CheckBoxPreference(this);
        ean13sPref.setKey("ean13s_preference");
        ean13sPref.setTitle(R.string.title_addseparator_preference);
        ean13OptPref.addPreference(ean13sPref);   
        
        ean13iPref = new CheckBoxPreference(this);
        ean13iPref.setKey("ean13i_preference");
        ean13iPref.setTitle(R.string.title_isbn_preference);
        ean13OptPref.addPreference(ean13iPref);
        
	    ean13vPref.setChecked((KTSyncData.OptionsEx & KTSyncData.E13_VERIFYCHKDGT_MASK) != 0);      
	    ean132Pref.setChecked((KTSyncData.OptionsEx & KTSyncData.E13_2ADDENDA_MASK) != 0);   
	    ean135Pref.setChecked((KTSyncData.OptionsEx & KTSyncData.E13_5ADDENDA_MASK) != 0);   
	    ean13rPref.setChecked((KTSyncData.OptionsEx & KTSyncData.E13_REQADDENDA_MASK) != 0);   
	    ean13sPref.setChecked((KTSyncData.OptionsEx & KTSyncData.E13_ADDENDASEP_MASK) != 0);      
	    ean13iPref.setChecked((KTSyncData.OptionsEx & KTSyncData.E13_ISBNTRANS_MASK) != 0);
    }

    private void GS1SymbologyOption()
    {
        gs1uPref = new CheckBoxPreference(this);
        gs1uPref.setKey("gs1u_preference");
        gs1uPref.setTitle(R.string.title_upce_ean_preference);
        gs1OptPref.addPreference(gs1uPref);
	    gs1uPref.setChecked((KTSyncData.OptionsEx & KTSyncData.UPC_EAN_VERSION_MASK) != 0);
        
        gs1eoptPref = new ListPreference(this);
        gs1eoptPref.setEntries(R.array.entries_emulation_preference);
        gs1eoptPref.setEntryValues(R.array.entryvalues_two_preference);
        gs1eoptPref.setDialogTitle(R.string.dialog_title_preference);
        gs1eoptPref.setKey("gs1eopt_preference");
        gs1eoptPref.setTitle(R.string.title_emulation_preference);
        gs1OptPref.addPreference(gs1eoptPref);
        tmp = (KTSyncData.OptionsEx & KTSyncData.EANUCC_EMUL_MASK) >> KTSyncData.EANUCC_EMUL_SHIFT;
		gs1eoptPref.setValueIndex(tmp);
		gs1eoptPref.setSummary(gs1eoptPref.getEntry().toString());
    }    
}

