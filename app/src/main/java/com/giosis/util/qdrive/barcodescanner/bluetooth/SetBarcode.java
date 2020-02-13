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

public class SetBarcode extends PreferenceActivity {
	
    private static final String TAG = "Setbarcode";	  

	private PreferenceScreen oneDPref;
	private PreferenceScreen twoDPref;
	private PreferenceScreen postPref;
	
    private CheckBoxPreference lean13Pref; 
    private CheckBoxPreference lean13bPref; 
    private CheckBoxPreference lean13aPref;
    private CheckBoxPreference lean8Pref;
    private CheckBoxPreference lean8aPref;
    private CheckBoxPreference lupcaPref;
    private CheckBoxPreference lupcaaPref;
    private CheckBoxPreference lupcePref;
    private CheckBoxPreference lupceaPref;    
    private CheckBoxPreference lcode128Pref;    
    private CheckBoxPreference lgs1128Pref;    
    private CheckBoxPreference lcode39Pref;   
    private CheckBoxPreference li2of5Pref;   
    private CheckBoxPreference lcodabarPref; 
    private CheckBoxPreference lcode93Pref;  
    private CheckBoxPreference lcode35Pref; 
    private CheckBoxPreference litf14Pref;	
	
	private PreferenceScreen root;

	//codabar
    private CheckBoxPreference codabarPref;
	//code11
    private CheckBoxPreference code11Pref;
	//code32
    private CheckBoxPreference code32Pref;
	//Code39
    private CheckBoxPreference code39Pref;
	//Code93
    private CheckBoxPreference code93Pref;
	//code128
    private CheckBoxPreference code128Pref;
	//ean8
    private CheckBoxPreference ean8Pref;
	//ean13
    private CheckBoxPreference ean13Pref;
	//gs1 composite
    private CheckBoxPreference gs1cPref;
	//i2of5
    private CheckBoxPreference i2of5Pref;
	//Matrix 2of5
    private CheckBoxPreference m2of5Pref;
	//MSI
    private CheckBoxPreference msiPref;
	//Plessy
    private CheckBoxPreference plessyPref;
	//PosiCode
    private CheckBoxPreference posiPref;
	//gs1 Omni
    private CheckBoxPreference gs1oPref;        
	//gs1 Limited
    private CheckBoxPreference gs1lPref;
	//gs1 Expanded
    private CheckBoxPreference gs1ePref;
	//Straight 2of5 Industrial
    private CheckBoxPreference s2of5Pref;
	//TLC39
    private CheckBoxPreference tlc39Pref;
	//Straight 2of5 IATA
    private CheckBoxPreference s2of5iPref;
	//Telepen
    private CheckBoxPreference telePref;
	//Triopic
    private CheckBoxPreference trioPref;
	//UPCA
    private CheckBoxPreference upcaPref;
	//UPCE0
    private CheckBoxPreference upc0Pref;
	//UPCE1
    private CheckBoxPreference upc1Pref;
    
	//Aztec
    private CheckBoxPreference aztecPref;
	//Aztec Runes
    private CheckBoxPreference aztecrPref;
	//Codablock F
    private CheckBoxPreference codaPref;
	//Code16K
    private CheckBoxPreference code16Pref;
	//Code49
    private CheckBoxPreference code49Pref;
	//Data matrix
    private CheckBoxPreference dataPref;
	//MaxiCode
    private CheckBoxPreference maxiPref;
	//Micro PDF417
    private CheckBoxPreference mpdfPref;
	//PDF417
    private CheckBoxPreference pdfPref;
	//QRCode
    private CheckBoxPreference qrPref;
	//Hanxin
    private CheckBoxPreference hanPref;
    
	//Postnet
    private CheckBoxPreference postnetPref;
	//Planet
    private CheckBoxPreference planetPref;
	//British
    private CheckBoxPreference britishPref;
	//Canadian
    private CheckBoxPreference canadaPref;
	//Japanese 
    private CheckBoxPreference japanPref;
	//Australian
    private CheckBoxPreference ausPref;
	//China
    private CheckBoxPreference chinaPref;
	//Kix
    private CheckBoxPreference kixPref;
	//Korea
    private CheckBoxPreference koreaPref; 
    
	private ListPreference ocrPref;
	
    private SharedPreferences.OnSharedPreferenceChangeListener prefListener;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
        
        Log.d(TAG, "Setbarcode setting create");
        
        KTSyncData.SymbologiesBackup = KTSyncData.Symbologies;
        if ( KTSyncData.bIsKDC300 )	KTSyncData.SymbologiesExBackup = KTSyncData.SymbologiesEx;
        
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
          			  if ( key.equals("ocr_preference") ) {
        				  String newvalue = ocrPref.getValue();
        				  KTSyncData.SymbologiesEx &= ~KTSyncData.OCR_MASK;
        				  KTSyncData.SymbologiesEx |= (ocrPref.findIndexOfValue(newvalue) << KTSyncData.OCR_SHIFT);
        				  ocrPref.setSummary(ocrPref.getEntry().toString());        				  
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
    	Log.d(TAG, "Setbarcode setting onDestry");
    	
       	KTSyncData.mKScan.WakeupCommand();    	
    	if ( ! KTSyncData.bIsKDC300 ) {
	    	KTSyncData.Symbologies &= KTSyncData.SYMBOLOGIES_MASK;

	        if ( lean13Pref.isChecked() )	KTSyncData.Symbologies |= KTSyncData.EAN13_MASK; 
	        if ( lean13bPref.isChecked() )	KTSyncData.Symbologies |= KTSyncData.BooklandEAN_MASK; 
	        if ( lean13aPref.isChecked() )	KTSyncData.Symbologies |= KTSyncData.EAN13withAddon_MASK;
	        if ( lean8Pref.isChecked() )	KTSyncData.Symbologies |= KTSyncData.EAN8_MASK;
	        if ( lean8aPref.isChecked() )	KTSyncData.Symbologies |= KTSyncData.EAN8withAddon_MASK;
	        if ( lupcaPref.isChecked() )	KTSyncData.Symbologies |= KTSyncData.UPCA_MASK;
	        if ( lupcaaPref.isChecked() )	KTSyncData.Symbologies |= KTSyncData.UPCAwithAddon_MASK;
	        if ( lupcePref.isChecked() )	KTSyncData.Symbologies |= KTSyncData.UPCE_MASK;
	        if ( lupceaPref.isChecked() )	KTSyncData.Symbologies |= KTSyncData.UPCEwithAddon_MASK;    
	        if ( lcode128Pref.isChecked() )	KTSyncData.Symbologies |= KTSyncData.Code128_MASK;    
	        if ( lgs1128Pref.isChecked() )	KTSyncData.Symbologies |= KTSyncData.GS1128_MASK;    
	        if ( lcode39Pref.isChecked() )	KTSyncData.Symbologies |= KTSyncData.Code39_MASK;   
	        if ( li2of5Pref.isChecked() )	KTSyncData.Symbologies |= KTSyncData.I2of5_MASK;   
	        if ( lcodabarPref.isChecked() )	KTSyncData.Symbologies |= KTSyncData.Codabar_MASK; 
	        if ( lcode93Pref.isChecked() )	KTSyncData.Symbologies |= KTSyncData.Code93_MASK;  
	        if ( lcode35Pref.isChecked() )	KTSyncData.Symbologies |= KTSyncData.Code35_MASK; 
	        if ( litf14Pref.isChecked() )	KTSyncData.Symbologies |= KTSyncData.ITF14_MASK;
	    	
	        if ( KTSyncData.Symbologies != KTSyncData.SymbologiesBackup  )
	            KTSyncData.mKScan.SendCommandWithValue("S", KTSyncData.Symbologies );	        
    	} else {
    		
    		KTSyncData.Symbologies = 0;
    		KTSyncData.SymbologiesEx &= ~(KTSyncData.TWOD_SYMBOLOGY_MASK | KTSyncData.POSTALCODE_MASK );
    		
    		//codabar
    	    if ( codabarPref.isChecked() )	KTSyncData.Symbologies |= KTSyncData.ONED_CODABAR_MASK;
    		//code11
    	    if ( code11Pref.isChecked() )	KTSyncData.Symbologies |= KTSyncData.ONED_CODE11_MASK;
    		//code32
    	    if ( code32Pref.isChecked() )	KTSyncData.Symbologies |= KTSyncData.ONED_CODE32_MASK;
    		//Code39
    	    if ( code39Pref.isChecked() )	KTSyncData.Symbologies |= KTSyncData.ONED_CODE39_MASK;
    		//Code93
    	    if ( code93Pref.isChecked() )	KTSyncData.Symbologies |= KTSyncData.ONED_CODE93_MASK;
    		//code128
    	    if ( code128Pref.isChecked() )	KTSyncData.Symbologies |= KTSyncData.ONED_CODE128_MASK;
    		//ean8
    	    if ( ean8Pref.isChecked() )	KTSyncData.Symbologies |= KTSyncData.ONED_EAN8_MASK;
    		//ean13
    	    if ( ean13Pref.isChecked() )	KTSyncData.Symbologies |= KTSyncData.ONED_EAN13_MASK;
    		//gs1 composite
    	    if ( gs1cPref.isChecked() )	KTSyncData.Symbologies |= KTSyncData.ONED_EANUCC_MASK;
    		//i2of5
    	    if ( i2of5Pref.isChecked() )	KTSyncData.Symbologies |= KTSyncData.ONED_I2OF5_MASK;
    		//Matrix 2of5
    	    if ( m2of5Pref.isChecked() )	KTSyncData.Symbologies |= KTSyncData.ONED_MATRIX2OF5_MASK;
    		//MSI
    	    if ( msiPref.isChecked() )	KTSyncData.Symbologies |= KTSyncData.ONED_MSI_MASK;
    		//Plessy
    	    if ( plessyPref.isChecked() )	KTSyncData.Symbologies |= KTSyncData.ONED_PLESSEY_MASK;
    		//PosiCode
    	    if ( posiPref.isChecked() )	KTSyncData.Symbologies |= KTSyncData.ONED_POSICODE_MASK;
    		//gs1 Omni
    	    if ( gs1oPref.isChecked() )	KTSyncData.Symbologies |= KTSyncData.ONED_RSS14_MASK;        
    		//gs1 Limited
    	    if ( gs1lPref.isChecked() )	KTSyncData.Symbologies |= KTSyncData.ONED_RSSLIMIT_MASK;
    		//gs1 Expanded
    	    if ( gs1ePref.isChecked() )	KTSyncData.Symbologies |= KTSyncData.ONED_RSSEXPAND_MASK;
    		//Straight 2of5 Industrial
    	    if ( s2of5Pref.isChecked() )	KTSyncData.Symbologies |= KTSyncData.ONED_S2OF5ID_MASK;
    		//TLC39
    	    if ( tlc39Pref.isChecked() )	KTSyncData.Symbologies |= KTSyncData.ONED_S2OF5IA_MASK;
    		//Straight 2of5 IATA
    	    if ( s2of5iPref.isChecked() )	KTSyncData.Symbologies |= KTSyncData.ONED_TLC39_MASK;
    		//Telepen
    	    if ( telePref.isChecked() )	KTSyncData.Symbologies |= KTSyncData.ONED_TELEPEN_MASK;
    		//Triopic
    	    if ( trioPref.isChecked() )	KTSyncData.Symbologies |= KTSyncData.ONED_TRIOPTIC_MASK;
    		//UPCA
    	    if ( upcaPref.isChecked() )	KTSyncData.Symbologies |= KTSyncData.ONED_UPCA_MASK;
    		//UPCE0
    	    if ( upc0Pref.isChecked() )	KTSyncData.Symbologies |= KTSyncData.ONED_UPCE0_MASK;
    		//UPCE1
    	    if ( upc1Pref.isChecked() )	KTSyncData.Symbologies |= KTSyncData.ONED_UPCE1_MASK;
    	    
    		//Aztec
    	    if ( aztecPref.isChecked() )	KTSyncData.SymbologiesEx |= KTSyncData.TWOD_AZTECCODE_MASK;
    		//Aztec Runes
    	    if ( aztecrPref.isChecked() )	KTSyncData.SymbologiesEx |= KTSyncData.TWOD_AZTECRUNES_MASK;
    		//Codablock F
    	    if ( codaPref.isChecked() )	KTSyncData.SymbologiesEx |= KTSyncData.TWOD_CODABLOCKF_MASK;
    		//Code16K
    	    if ( code16Pref.isChecked() )	KTSyncData.SymbologiesEx |= KTSyncData.TWOD_CODE16K_MASK;
    		//Code49
    	    if ( code49Pref.isChecked() )	KTSyncData.SymbologiesEx |= KTSyncData.TWOD_CODE49_MASK;
    		//Data matrix
    	    if ( dataPref.isChecked() )	KTSyncData.SymbologiesEx |= KTSyncData.TWOD_DATAMATRIX_MASK;
    		//MaxiCode
    	    if ( maxiPref.isChecked() )	KTSyncData.SymbologiesEx |= KTSyncData.TWOD_MAXICODE_MASK;
    		//Micro PDF417
    	    if ( mpdfPref.isChecked() )	KTSyncData.SymbologiesEx |= KTSyncData.TWOD_MICROPDF_MASK;
    		//PDF417
    	    if ( pdfPref.isChecked() )	KTSyncData.SymbologiesEx |= KTSyncData.TWOD_PDF417_MASK;
    		//QRCode
    	    if ( qrPref.isChecked() )	KTSyncData.SymbologiesEx |= KTSyncData.TWOD_QRCODE_MASK;
    		//Hanxin
    	    if ( hanPref.isChecked() )	KTSyncData.SymbologiesEx |= KTSyncData.TWOD_HANSIN_MASK;
    	    
    		//Postnet
    	    if ( postnetPref.isChecked() )	KTSyncData.SymbologiesEx |= KTSyncData.POS_POSTNET_MASK;
    		//Planet
    	    if ( planetPref.isChecked() )	KTSyncData.SymbologiesEx |= KTSyncData.POS_PLANETCODE_MASK;
    		//British
    	    if ( britishPref.isChecked() )	KTSyncData.SymbologiesEx |= KTSyncData.POS_UKPOST_MASK;
    		//Canadian
    	    if ( canadaPref.isChecked() )	KTSyncData.SymbologiesEx |= KTSyncData.POS_CANADAPOST_MASK;
    		//Japanese 
    	    if ( japanPref.isChecked() )	KTSyncData.SymbologiesEx |= KTSyncData.POS_KIXPOST_MASK;
    		//Australian
    	    if ( ausPref.isChecked() )	KTSyncData.SymbologiesEx |= KTSyncData.POS_AUSPOST_MASK;
    		//China
    	    if ( chinaPref.isChecked() )	KTSyncData.SymbologiesEx |= KTSyncData.POS_JAPANPOST_MASK;
    		//Kix
    	    if ( kixPref.isChecked() )	KTSyncData.SymbologiesEx |= KTSyncData.POS_CHINAPOST_MASK;
    		//Korea
    	    if ( koreaPref.isChecked() )	KTSyncData.SymbologiesEx |= KTSyncData.POS_KOREAPOST_MASK;
    	    
	        if ( (KTSyncData.Symbologies != KTSyncData.SymbologiesBackup) || 
		        	 (KTSyncData.SymbologiesEx != KTSyncData.SymbologiesExBackup)	)
		            KTSyncData.mKScan.SendCommandWithValueEx("S", KTSyncData.Symbologies, KTSyncData.SymbologiesEx );
    	}
    	KTSyncData.mKScan.FinishCommand();
    	
        super.onDestroy();       
    }
    
    private PreferenceScreen createPreferenceHierarchy() {
        // Root
        root = getPreferenceManager().createPreferenceScreen(this);
      
    	if ( KTSyncData.bIsKDC300 ) {
	
	        // 1D Symbology Selection
	        oneDPref = getPreferenceManager().createPreferenceScreen(this);
	        oneDPref.setKey("oneD_preference");
	        oneDPref.setTitle(R.string.title_oneD_preference);
	        root.addPreference(oneDPref);

	        OneDSymbologySelection();
	          
	        // 2D Symbology Selection
	        twoDPref = getPreferenceManager().createPreferenceScreen(this);
	        twoDPref.setKey("twoD_preference");
	        twoDPref.setTitle(R.string.title_twoD_preference);
	        root.addPreference(twoDPref);
	        
	        TwoDSymbologySelection();
      
	        // Postal Symbology Selection
	        postPref = getPreferenceManager().createPreferenceScreen(this);
	        postPref.setKey("post_preference");
	        postPref.setTitle(R.string.title_post_preference);
	        root.addPreference(postPref);
	        
	        PostalSymbologySelection();
	        
	        // OCR Symbology Selection
	        ocrPref = new ListPreference(this);
	        ocrPref.setEntries(R.array.entries_ocr_preference);
	        ocrPref.setEntryValues(R.array.entryvalues_six_preference);
	        ocrPref.setDialogTitle(R.string.dialog_title_preference);
	        ocrPref.setKey("ocr_preference");
	        ocrPref.setTitle(R.string.title_ocr_preference);
	        //ocrPref.setValueIndex(KTSyncData.RecordDelimiter);
	        root.addPreference(ocrPref);
	        int tmp = (KTSyncData.SymbologiesEx & KTSyncData.OCR_MASK) >> KTSyncData.OCR_SHIFT;
        			
        	ocrPref.setValueIndex(tmp);
			ocrPref.setSummary(ocrPref.getEntry().toString());	        
	        
    	} else
    		LaserSymbologySelection();      
       
        return root;
    }
    
    private void LaserSymbologySelection()
    {

		lean13Pref = new CheckBoxPreference(this);
		lean13Pref.setKey("lean13_preference");
		lean13Pref.setTitle(R.string.title_ean13_preference);
		lean13Pref.setDefaultValue(true);
		root.addPreference(lean13Pref);    

        lean13bPref = new CheckBoxPreference(this);
        lean13bPref.setKey("lean13b_preference");
        lean13bPref.setTitle(R.string.title_ean13b_preference);
        lean13bPref.setDefaultValue(true);
        root.addPreference(lean13bPref); 

        lean13aPref = new CheckBoxPreference(this);
        lean13aPref.setKey("lean13a_preference");
        lean13aPref.setTitle(R.string.title_ean13a_preference);
        lean13aPref.setDefaultValue(true);
        root.addPreference(lean13aPref);
        
        lean8Pref = new CheckBoxPreference(this);
        lean8Pref.setKey("lean8_preference");
        lean8Pref.setTitle(R.string.title_ean8_preference);
        lean8Pref.setDefaultValue(true);
        root.addPreference(lean8Pref);
        
        lean8aPref = new CheckBoxPreference(this);
        lean8aPref.setKey("lean8a_preference");
        lean8aPref.setTitle(R.string.title_ean8a_preference);
        lean8aPref.setDefaultValue(true);
        root.addPreference(lean8aPref);
        
        lupcaPref = new CheckBoxPreference(this);
        lupcaPref.setKey("lupca_preference");
        lupcaPref.setTitle(R.string.title_upca_preference);
        lupcaPref.setDefaultValue(true);
        root.addPreference(lupcaPref);
        
        lupcaaPref = new CheckBoxPreference(this);
        lupcaaPref.setKey("lupcaa_preference");
        lupcaaPref.setTitle(R.string.title_upcaa_preference);
        lupcaaPref.setDefaultValue(true);
        root.addPreference(lupcaaPref);
        
        lupcePref = new CheckBoxPreference(this);
        lupcePref.setKey("lupce_preference");
        lupcePref.setTitle(R.string.title_upce_preference);
        lupcePref.setDefaultValue(true);
        root.addPreference(lupcePref);
        
        lupceaPref = new CheckBoxPreference(this);
        lupceaPref.setKey("lupcea_preference");
        lupceaPref.setTitle(R.string.title_upcea_preference);
        lupceaPref.setDefaultValue(true);
        root.addPreference(lupceaPref);
        
        lcode128Pref = new CheckBoxPreference(this);
        lcode128Pref.setKey("lcode128_preference");
        lcode128Pref.setTitle(R.string.title_code128_preference);
        lcode128Pref.setDefaultValue(true);
        root.addPreference(lcode128Pref);
        
        lgs1128Pref = new CheckBoxPreference(this);
        lgs1128Pref.setKey("lgs1128_preference");
        lgs1128Pref.setTitle(R.string.title_gs1128_preference);
        lgs1128Pref.setDefaultValue(true);
        root.addPreference(lgs1128Pref);
        
        lcode39Pref = new CheckBoxPreference(this);
        lcode39Pref.setKey("lcode39_preference");
        lcode39Pref.setTitle(R.string.title_code39_preference);
        lcode39Pref.setDefaultValue(true);
        root.addPreference(lcode39Pref);
        
        li2of5Pref = new CheckBoxPreference(this);
        li2of5Pref.setKey("li2of5_preference");
        li2of5Pref.setTitle(R.string.title_i2of5_preference);
        li2of5Pref.setDefaultValue(true);
        root.addPreference(li2of5Pref);
        
        lcodabarPref = new CheckBoxPreference(this);
        lcodabarPref.setKey("lcodabar_preference");
        lcodabarPref.setTitle(R.string.title_codabar_preference);
        lcodabarPref.setDefaultValue(true);
        root.addPreference(lcodabarPref);
        
        lcode93Pref = new CheckBoxPreference(this);
        lcode93Pref.setKey("lcode93_preference");
        lcode93Pref.setTitle(R.string.title_code93_preference);
        lcode93Pref.setDefaultValue(true);
        root.addPreference(lcode93Pref);
        
        lcode35Pref = new CheckBoxPreference(this);
        lcode35Pref.setKey("lcode35_preference");
        lcode35Pref.setTitle(R.string.title_code35_preference);
        lcode35Pref.setDefaultValue(true);
        root.addPreference(lcode35Pref);
        
        litf14Pref = new CheckBoxPreference(this);
        litf14Pref.setKey("litf14_preference");
        litf14Pref.setTitle(R.string.title_itf14_preference);
        litf14Pref.setDefaultValue(true);
        root.addPreference(litf14Pref);
        
        lean13Pref.setChecked((KTSyncData.Symbologies & KTSyncData.EAN13_MASK) != 0); 
        lean13bPref.setChecked((KTSyncData.Symbologies & KTSyncData.BooklandEAN_MASK) != 0); 
        lean13aPref.setChecked((KTSyncData.Symbologies & KTSyncData.EAN13withAddon_MASK) != 0);
        lean8Pref.setChecked((KTSyncData.Symbologies & KTSyncData.EAN8_MASK) != 0);
        lean8aPref.setChecked((KTSyncData.Symbologies & KTSyncData.EAN8withAddon_MASK) != 0);
        lupcaPref.setChecked((KTSyncData.Symbologies & KTSyncData.UPCA_MASK) != 0);
        lupcaaPref.setChecked((KTSyncData.Symbologies & KTSyncData.UPCAwithAddon_MASK) != 0);
        lupcePref.setChecked((KTSyncData.Symbologies & KTSyncData.UPCE_MASK) != 0);
        lupceaPref.setChecked((KTSyncData.Symbologies & KTSyncData.UPCEwithAddon_MASK) != 0);    
        lcode128Pref.setChecked((KTSyncData.Symbologies & KTSyncData.Code128_MASK) != 0); 
        lgs1128Pref.setChecked((KTSyncData.Symbologies & KTSyncData.GS1128_MASK) != 0);  
        lcode39Pref.setChecked((KTSyncData.Symbologies & KTSyncData.Code39_MASK) != 0);  
        li2of5Pref.setChecked((KTSyncData.Symbologies & KTSyncData.I2of5_MASK) != 0); 
        lcodabarPref.setChecked((KTSyncData.Symbologies & KTSyncData.Codabar_MASK) != 0); 
        lcode93Pref.setChecked((KTSyncData.Symbologies & KTSyncData.Code93_MASK) != 0); 
        lcode35Pref.setChecked((KTSyncData.Symbologies & KTSyncData.Code35_MASK) != 0);
        litf14Pref.setChecked((KTSyncData.Symbologies & KTSyncData.ITF14_MASK) != 0);
        
    }
    private void OneDSymbologySelection()
    {    	
    	//codabar
        codabarPref = new CheckBoxPreference(this);
        codabarPref.setKey("codabar_preference");
        codabarPref.setTitle(R.string.title_codabar_preference);
        oneDPref.addPreference(codabarPref);          
    	//code11
        code11Pref = new CheckBoxPreference(this);
        code11Pref.setKey("code11_preference");
        code11Pref.setTitle(R.string.title_code11_preference);
        oneDPref.addPreference(code11Pref);
    	//code32
        code32Pref = new CheckBoxPreference(this);
        code32Pref.setKey("code32_preference");
        code32Pref.setTitle(R.string.title_code32_preference);
        oneDPref.addPreference(code32Pref);
    	//Code39
        code39Pref = new CheckBoxPreference(this);
        code39Pref.setKey("code39_preference");
        code39Pref.setTitle(R.string.title_code39_preference);
        oneDPref.addPreference(code39Pref);
    	//Code93
        code93Pref = new CheckBoxPreference(this);
        code93Pref.setKey("code93_preference");
        code93Pref.setTitle(R.string.title_code93_preference);
        oneDPref.addPreference(code93Pref);
    	//code128
        code128Pref = new CheckBoxPreference(this);
        code128Pref.setKey("code128_preference");
        code128Pref.setTitle(R.string.title_code128_preference);
        oneDPref.addPreference(code128Pref);
    	//ean8
        ean8Pref = new CheckBoxPreference(this);
        ean8Pref.setKey("ean8_preference");
        ean8Pref.setTitle(R.string.title_ean8_preference);
        oneDPref.addPreference(ean8Pref);
    	//ean13
        ean13Pref = new CheckBoxPreference(this);
        ean13Pref.setKey("ean13_preference");
        ean13Pref.setTitle(R.string.title_ean13_preference);
        oneDPref.addPreference(ean13Pref);
    	//gs1 composite
        gs1cPref = new CheckBoxPreference(this);
        gs1cPref.setKey("gs1c_preference");
        gs1cPref.setTitle(R.string.title_gs1c_preference);
        oneDPref.addPreference(gs1cPref);
      
    	//i2of5
        i2of5Pref = new CheckBoxPreference(this);
        i2of5Pref.setKey("i2of5_preference");
        i2of5Pref.setTitle(R.string.title_i2of5_preference);
        oneDPref.addPreference(i2of5Pref);

        //Matrix 2of5
        m2of5Pref = new CheckBoxPreference(this);
        m2of5Pref.setKey("m2of5_preference");
        m2of5Pref.setTitle(R.string.title_m2of5_preference);
        oneDPref.addPreference(m2of5Pref);

        //MSI
        msiPref = new CheckBoxPreference(this);
        msiPref.setKey("msi_preference");
        msiPref.setTitle(R.string.title_msi_preference);
        oneDPref.addPreference(msiPref);
    	//Plessy
        plessyPref = new CheckBoxPreference(this);
        plessyPref.setKey("plessy_preference");
        plessyPref.setTitle(R.string.title_plessy_preference);
        oneDPref.addPreference(plessyPref);
    	//PosiCode
        posiPref = new CheckBoxPreference(this);
        posiPref.setKey("posi_preference");
        posiPref.setTitle(R.string.title_posi_preference);
        oneDPref.addPreference(posiPref);
   
        //gs1 Omni
        gs1oPref = new CheckBoxPreference(this);
        gs1oPref.setKey("gs1o_preference");
        gs1oPref.setTitle(R.string.title_gs1o_preference);
        oneDPref.addPreference(gs1oPref);        
    	//gs1 Limited
        gs1lPref = new CheckBoxPreference(this);
        gs1lPref.setKey("gs1l_preference");
        gs1lPref.setTitle(R.string.title_gs1l_preference);
        oneDPref.addPreference(gs1lPref);
    	//gs1 Expanded
        gs1ePref = new CheckBoxPreference(this);
        gs1ePref.setKey("gs1e_preference");
        gs1ePref.setTitle(R.string.title_gs1e_preference);
        oneDPref.addPreference(gs1ePref);
    	//Straight 2of5 Industrial
        s2of5Pref = new CheckBoxPreference(this);
        s2of5Pref.setKey("s2of5_preference");
        s2of5Pref.setTitle(R.string.title_s2of5_preference);
        oneDPref.addPreference(s2of5Pref);
    	//TLC39
        tlc39Pref = new CheckBoxPreference(this);
        tlc39Pref.setKey("tlc39_preference");
        tlc39Pref.setTitle(R.string.title_tlc39_preference);
        oneDPref.addPreference(tlc39Pref);
    	//Straight 2of5 IATA
        s2of5iPref = new CheckBoxPreference(this);
        s2of5iPref.setKey("s2of5i_preference");
        s2of5iPref.setTitle(R.string.title_s2of5i_preference);
        oneDPref.addPreference(s2of5iPref);
    	//Telepen
        telePref = new CheckBoxPreference(this);
        telePref.setKey("tele_preference");
        telePref.setTitle(R.string.title_tele_preference);
        oneDPref.addPreference(telePref);
    	//Triopic
        trioPref = new CheckBoxPreference(this);
        trioPref.setKey("trio_preference");
        trioPref.setTitle(R.string.title_trio_preference);
        oneDPref.addPreference(trioPref);
    	//UPCA
        upcaPref = new CheckBoxPreference(this);
        upcaPref.setKey("upca_preference");
        upcaPref.setTitle(R.string.title_upca_preference);
        oneDPref.addPreference(upcaPref);
    	//UPCE0
        upc0Pref = new CheckBoxPreference(this);
        upc0Pref.setKey("upc0_preference");
        upc0Pref.setTitle(R.string.title_upc0_preference);
        oneDPref.addPreference(upc0Pref);
    	//UPCE1
        upc1Pref = new CheckBoxPreference(this);
        upc1Pref.setKey("upc1_preference");
        upc1Pref.setTitle(R.string.title_upc1_preference);
        oneDPref.addPreference(upc1Pref);
 
	    codabarPref.setChecked((KTSyncData.Symbologies & KTSyncData.ONED_CODABAR_MASK) != 0);
		//code11
	    code11Pref.setChecked((KTSyncData.Symbologies & KTSyncData.ONED_CODE11_MASK) != 0);
		//code32
	    code32Pref.setChecked((KTSyncData.Symbologies & KTSyncData.ONED_CODE32_MASK) != 0);
		//Code39
	    code39Pref.setChecked((KTSyncData.Symbologies & KTSyncData.ONED_CODE39_MASK) != 0);
		//Code93
	    code93Pref.setChecked((KTSyncData.Symbologies & KTSyncData.ONED_CODE93_MASK) != 0);
		//code128
	    code128Pref.setChecked((KTSyncData.Symbologies & KTSyncData.ONED_CODE128_MASK) != 0);
		//ean8
	    ean8Pref.setChecked((KTSyncData.Symbologies & KTSyncData.ONED_EAN8_MASK) != 0);
		//ean13
	    ean13Pref.setChecked((KTSyncData.Symbologies & KTSyncData.ONED_EAN13_MASK) != 0);
		//gs1 composite
	    gs1cPref.setChecked((KTSyncData.Symbologies & KTSyncData.ONED_EANUCC_MASK) != 0);
		//i2of5
	    i2of5Pref.setChecked((KTSyncData.Symbologies & KTSyncData.ONED_I2OF5_MASK) != 0);
		//Matrix 2of5
	    m2of5Pref.setChecked((KTSyncData.Symbologies & KTSyncData.ONED_MATRIX2OF5_MASK) != 0);
		//MSI
	    msiPref.setChecked((KTSyncData.Symbologies & KTSyncData.ONED_MSI_MASK) != 0);
		//Plessy
	    plessyPref.setChecked((KTSyncData.Symbologies & KTSyncData.ONED_PLESSEY_MASK) != 0);
		//PosiCode
	    posiPref.setChecked((KTSyncData.Symbologies & KTSyncData.ONED_POSICODE_MASK) != 0);
		//gs1 Omni
	    gs1oPref.setChecked((KTSyncData.Symbologies & KTSyncData.ONED_RSS14_MASK) != 0);        
		//gs1 Limited
	    gs1lPref.setChecked((KTSyncData.Symbologies & KTSyncData.ONED_RSSLIMIT_MASK) != 0);
		//gs1 Expanded
	    gs1ePref.setChecked((KTSyncData.Symbologies & KTSyncData.ONED_RSSEXPAND_MASK) != 0);
		//Straight 2of5 Industrial
	    s2of5Pref.setChecked((KTSyncData.Symbologies & KTSyncData.ONED_S2OF5ID_MASK) != 0);
		//TLC39
	    tlc39Pref.setChecked((KTSyncData.Symbologies & KTSyncData.ONED_S2OF5IA_MASK) != 0);
		//Straight 2of5 IATA
	    s2of5iPref.setChecked((KTSyncData.Symbologies & KTSyncData.ONED_TLC39_MASK) != 0);
		//Telepen
	    telePref.setChecked((KTSyncData.Symbologies & KTSyncData.ONED_TELEPEN_MASK) != 0);
		//Triopic
	    trioPref.setChecked((KTSyncData.Symbologies & KTSyncData.ONED_TRIOPTIC_MASK) != 0);
		//UPCA
	    upcaPref.setChecked((KTSyncData.Symbologies & KTSyncData.ONED_UPCA_MASK) != 0);
		//UPCE0
	    upc0Pref.setChecked((KTSyncData.Symbologies & KTSyncData.ONED_UPCE0_MASK) != 0);
		//UPCE1
	    upc1Pref.setChecked((KTSyncData.Symbologies & KTSyncData.ONED_UPCE1_MASK) != 0);        
 
    }

    private void TwoDSymbologySelection()
    { 
    	//Aztec
        aztecPref = new CheckBoxPreference(this);
        aztecPref.setKey("aztec_preference");
        aztecPref.setTitle(R.string.title_aztec_preference);
        twoDPref.addPreference(aztecPref);
    	//Aztec Runes
        aztecrPref = new CheckBoxPreference(this);
        aztecrPref.setKey("aztecr_preference");
        aztecrPref.setTitle(R.string.title_aztecr_preference);
        twoDPref.addPreference(aztecrPref);
    	//Codablock F
        codaPref = new CheckBoxPreference(this);
        codaPref.setKey("coda_preference");
        codaPref.setTitle(R.string.title_coda_preference);
        twoDPref.addPreference(codaPref);
    	//Code16K
        code16Pref = new CheckBoxPreference(this);
        code16Pref.setKey("code16_preference");
        code16Pref.setTitle(R.string.title_code16_preference);
        twoDPref.addPreference(code16Pref);
    	//Code49
        code49Pref = new CheckBoxPreference(this);
        code49Pref.setKey("code49_preference");
        code49Pref.setTitle(R.string.title_code49_preference);
        twoDPref.addPreference(code49Pref);
    	//Data matrix
        dataPref = new CheckBoxPreference(this);
        dataPref.setKey("data_preference");
        dataPref.setTitle(R.string.title_datam_preference);
        twoDPref.addPreference(dataPref);
    	//MaxiCode
        maxiPref = new CheckBoxPreference(this);
        maxiPref.setKey("maxi_preference");
        maxiPref.setTitle(R.string.title_maxi_preference);
        twoDPref.addPreference(maxiPref);
    	//Micro PDF417
        mpdfPref = new CheckBoxPreference(this);
        mpdfPref.setKey("mpdf_preference");
        mpdfPref.setTitle(R.string.title_mpdf_preference);
        twoDPref.addPreference(mpdfPref);
    	//PDF417
        pdfPref = new CheckBoxPreference(this);
        pdfPref.setKey("pdf_preference");
        pdfPref.setTitle(R.string.title_pdf_preference);
        twoDPref.addPreference(pdfPref);
    	//QRCode
        qrPref = new CheckBoxPreference(this);
        qrPref.setKey("qr_preference");
        qrPref.setTitle(R.string.title_qr_preference);
        twoDPref.addPreference(qrPref);
    	//Hanxin
        hanPref = new CheckBoxPreference(this);
        hanPref.setKey("han_preference");
        hanPref.setTitle(R.string.title_han_preference);
        twoDPref.addPreference(hanPref);
        
		//Aztec
	    aztecPref.setChecked((KTSyncData.SymbologiesEx & KTSyncData.TWOD_AZTECCODE_MASK) != 0);
		//Aztec Runes
	    aztecrPref.setChecked((KTSyncData.SymbologiesEx & KTSyncData.TWOD_AZTECRUNES_MASK) != 0);
		//Codablock F
	    codaPref.setChecked((KTSyncData.SymbologiesEx & KTSyncData.TWOD_CODABLOCKF_MASK) != 0);
		//Code16K
	    code16Pref.setChecked((KTSyncData.SymbologiesEx & KTSyncData.TWOD_CODE16K_MASK) != 0);
		//Code49
	    code49Pref.setChecked((KTSyncData.SymbologiesEx & KTSyncData.TWOD_CODE49_MASK) != 0);
		//Data matrix
	    dataPref.setChecked((KTSyncData.SymbologiesEx & KTSyncData.TWOD_DATAMATRIX_MASK) != 0);
		//MaxiCode
	    maxiPref.setChecked((KTSyncData.SymbologiesEx & KTSyncData.TWOD_MAXICODE_MASK) != 0);
		//Micro PDF417
	    mpdfPref.setChecked((KTSyncData.SymbologiesEx & KTSyncData.TWOD_MICROPDF_MASK) != 0);
		//PDF417
	    pdfPref.setChecked((KTSyncData.SymbologiesEx & KTSyncData.TWOD_PDF417_MASK) != 0);
		//QRCode
	    qrPref.setChecked((KTSyncData.SymbologiesEx & KTSyncData.TWOD_QRCODE_MASK) != 0);
		//Hanxin
	    hanPref.setChecked((KTSyncData.SymbologiesEx & KTSyncData.TWOD_HANSIN_MASK) != 0);
    }
    private void PostalSymbologySelection()
    { 
    	//Postnet
        postnetPref = new CheckBoxPreference(this);
        postnetPref.setKey("postnet_preference");
        postnetPref.setTitle(R.string.title_postnet_preference);
        postPref.addPreference(postnetPref);
    	//Planet
        planetPref = new CheckBoxPreference(this);
        planetPref.setKey("planet_preference");
        planetPref.setTitle(R.string.title_planet_preference);
        postPref.addPreference(planetPref);
    	//British
        britishPref = new CheckBoxPreference(this);
        britishPref.setKey("british_preference");
        britishPref.setTitle(R.string.title_british_preference);
        postPref.addPreference(britishPref);
    	//Canadian
        canadaPref = new CheckBoxPreference(this);
        canadaPref.setKey("canada_preference");
        canadaPref.setTitle(R.string.title_canada_preference);
        postPref.addPreference(canadaPref);
    	//Japanese 
        japanPref = new CheckBoxPreference(this);
        japanPref.setKey("japan_preference");
        japanPref.setTitle(R.string.title_japan_preference);
        postPref.addPreference(japanPref);
    	//Australian
        ausPref = new CheckBoxPreference(this);
        ausPref.setKey("aus_preference");
        ausPref.setTitle(R.string.title_aus_preference);
        postPref.addPreference(ausPref);
    	//China
        chinaPref = new CheckBoxPreference(this);
        chinaPref.setKey("china_preference");
        chinaPref.setTitle(R.string.title_china_preference);
        postPref.addPreference(chinaPref);
    	//Kix
        kixPref = new CheckBoxPreference(this);
        kixPref.setKey("kix_preference");
        kixPref.setTitle(R.string.title_kix_preference);
        postPref.addPreference(kixPref);
    	//Korea
        koreaPref = new CheckBoxPreference(this);
        koreaPref.setKey("korea_preference");
        koreaPref.setTitle(R.string.title_korea_preference);
        postPref.addPreference(koreaPref); 
        
		//Postnet
	    postnetPref.setChecked((KTSyncData.SymbologiesEx & KTSyncData.POS_POSTNET_MASK) != 0);
		//Planet
	    planetPref.setChecked((KTSyncData.SymbologiesEx & KTSyncData.POS_PLANETCODE_MASK) != 0);
		//British
	    britishPref.setChecked((KTSyncData.SymbologiesEx & KTSyncData.POS_UKPOST_MASK) != 0);
		//Canadian
	    canadaPref.setChecked((KTSyncData.SymbologiesEx & KTSyncData.POS_CANADAPOST_MASK) != 0);
		//Japanese 
	    japanPref.setChecked((KTSyncData.SymbologiesEx & KTSyncData.POS_KIXPOST_MASK) != 0);
		//Australian
	    ausPref.setChecked((KTSyncData.SymbologiesEx & KTSyncData.POS_AUSPOST_MASK) != 0);
		//China
	    chinaPref.setChecked((KTSyncData.SymbologiesEx & KTSyncData.POS_JAPANPOST_MASK) != 0);
		//Kix
	    kixPref.setChecked((KTSyncData.SymbologiesEx & KTSyncData.POS_CHINAPOST_MASK) != 0);
		//Korea
	    koreaPref.setChecked((KTSyncData.SymbologiesEx & KTSyncData.POS_KOREAPOST_MASK) != 0);
    }    
}

