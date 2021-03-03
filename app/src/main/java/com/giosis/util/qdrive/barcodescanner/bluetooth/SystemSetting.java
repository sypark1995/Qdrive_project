package com.giosis.util.qdrive.barcodescanner.bluetooth;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.giosis.util.qdrive.singapore.R;


public class SystemSetting extends PreferenceActivity {

    private static final String TAG = "System";

    private PreferenceScreen factoryPref;
    private PreferenceScreen clearPref;
    private PreferenceScreen clockPref;

    private ListPreference sleeptimeoutPref;
    private CheckBoxPreference beepsoundPref;
    private CheckBoxPreference beepvolumePref;
    private CheckBoxPreference autoerasePref;
    private CheckBoxPreference menubarcodePref;

    private int SleepTimeoutBackup;

    private SharedPreferences.OnSharedPreferenceChangeListener prefListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "System setting create");

        SleepTimeoutBackup = KTSyncData.SleepTimeout;
        KTSyncData.KDCSettingsBackup = KTSyncData.KDCSettings;

        KTSyncData.mKScan.mSystemContext = this;

        setPreferenceScreen(createPreferenceHierarchy());

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(
                getApplicationContext());

        prefListener = new
                SharedPreferences.OnSharedPreferenceChangeListener() {
                    @Override
                    public void onSharedPreferenceChanged(SharedPreferences
                                                                  sharedPreferences, String key) {
                        Log.w(TAG, "Pref key: " + key);
                        if (key.equals("sleeptimeout_preference")) {
                            String newvalue = sleeptimeoutPref.getValue();
                            KTSyncData.SleepTimeout = sleeptimeoutPref.findIndexOfValue(newvalue);
                            sleeptimeoutPref.setSummary(sleeptimeoutPref.getEntry().toString());
                        }
                    }

                };

        sharedPrefs.registerOnSharedPreferenceChangeListener(prefListener);

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

        String temp = String.format("%X:%X", KTSyncData.KDCSettings, KTSyncData.KDCSettingsBackup);
        Log.d(TAG, "System setting onDestry " + temp);

        KTSyncData.mKScan.WakeupCommand();

        KTSyncData.KDCSettings &= (~KTSyncData.SYSTEM_MASK);
        if (beepsoundPref.isChecked()) KTSyncData.KDCSettings |= KTSyncData.BEEPSOUND_MASK;
        if (beepvolumePref.isChecked()) KTSyncData.KDCSettings |= KTSyncData.BEEPVOLUME_MASK;
        if (autoerasePref.isChecked()) KTSyncData.KDCSettings |= KTSyncData.AUTOERASE_MASK;

        if (KTSyncData.bIsKDC300) {
            if (menubarcodePref.isChecked()) KTSyncData.KDCSettings |= KTSyncData.MENUBARCODE_MASK;
        }

        if (KTSyncData.SleepTimeout != SleepTimeoutBackup)
            KTSyncData.mKScan.SendCommandWithValue("GnTS", KTSyncData.SleepTimeout);
//Beep sound    	
        if ((KTSyncData.KDCSettings & KTSyncData.BEEPSOUND_MASK) != (KTSyncData.KDCSettingsBackup & KTSyncData.BEEPSOUND_MASK))
            KTSyncData.mKScan.SendCommandWithValue("Gb", ((KTSyncData.KDCSettings & KTSyncData.BEEPSOUND_MASK) != 0) ? 1 : 0);

//Beep volume
        if ((KTSyncData.KDCSettings & KTSyncData.BEEPVOLUME_MASK) != (KTSyncData.KDCSettingsBackup & KTSyncData.BEEPVOLUME_MASK))
            KTSyncData.mKScan.SendCommandWithValue("GbV", ((KTSyncData.KDCSettings & KTSyncData.BEEPVOLUME_MASK) != 0) ? 1 : 0);
//Auto erase            		
        if ((KTSyncData.KDCSettings & KTSyncData.AUTOERASE_MASK) != (KTSyncData.KDCSettingsBackup & KTSyncData.AUTOERASE_MASK))
            KTSyncData.mKScan.SendCommandWithValue("GnES", ((KTSyncData.KDCSettings & KTSyncData.AUTOERASE_MASK) != 0) ? 1 : 0);
//Menu barcode
        if ((KTSyncData.KDCSettings & KTSyncData.MENUBARCODE_MASK) != (KTSyncData.KDCSettingsBackup & KTSyncData.MENUBARCODE_MASK))
            KTSyncData.mKScan.SendCommandWithValue("GnBS", ((KTSyncData.KDCSettings & KTSyncData.MENUBARCODE_MASK) != 0) ? 1 : 0);

        KTSyncData.mKScan.FinishCommand();

        super.onDestroy();
    }

    private PreferenceScreen createPreferenceHierarchy() {
        // Root
        PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);

        sleeptimeoutPref = (ListPreference) findPreference("sleeptimeout_preference");
        if (sleeptimeoutPref == null) {
            sleeptimeoutPref = new ListPreference(this);
            sleeptimeoutPref.setEntries(R.array.entries_sleeptimeout_preference);
            sleeptimeoutPref.setEntryValues(R.array.entryvalues_sleeptimeout_preference);
            sleeptimeoutPref.setDialogTitle(R.string.dialog_title_preference);
            sleeptimeoutPref.setKey("sleeptimeout_preference");
            sleeptimeoutPref.setTitle(R.string.title_sleeptimeout_preference);
            root.addPreference(sleeptimeoutPref);
        }
        sleeptimeoutPref.setValueIndex(KTSyncData.SleepTimeout);
        sleeptimeoutPref.setSummary(sleeptimeoutPref.getEntry().toString());

        beepsoundPref = (CheckBoxPreference) findPreference("beepsound_preference");
        if (beepsoundPref == null) {
            beepsoundPref = new CheckBoxPreference(this);
            beepsoundPref.setKey("beepsound_preference");
            beepsoundPref.setTitle(R.string.title_beepsound_preference);
            root.addPreference(beepsoundPref);
        }
        beepsoundPref.setChecked((KTSyncData.KDCSettings & KTSyncData.BEEPSOUND_MASK) != 0);

        beepvolumePref = (CheckBoxPreference) findPreference("beepvolume_preference");
        if (beepvolumePref == null) {
            beepvolumePref = new CheckBoxPreference(this);
            beepvolumePref.setKey("beepvolume_preference");
            beepvolumePref.setTitle(R.string.title_beepvolume_preference);
            root.addPreference(beepvolumePref);
        }
        beepvolumePref.setChecked((KTSyncData.KDCSettings & KTSyncData.BEEPVOLUME_MASK) != 0);

        autoerasePref = (CheckBoxPreference) findPreference("autoerase_preference");
        if (autoerasePref == null) {
            autoerasePref = new CheckBoxPreference(this);
            autoerasePref.setKey("autoerase_preference");
            autoerasePref.setTitle(R.string.title_autoerase_preference);
            root.addPreference(autoerasePref);
        }
        autoerasePref.setChecked((KTSyncData.KDCSettings & KTSyncData.AUTOERASE_MASK) != 0);

        if (KTSyncData.bIsKDC300) {
            menubarcodePref = (CheckBoxPreference) findPreference("menubarcode_preference");
            if (menubarcodePref == null) {
                menubarcodePref = new CheckBoxPreference(this);
                menubarcodePref.setKey("menubarcode_preference");
                menubarcodePref.setTitle(R.string.title_menubarcode_preference);
                root.addPreference(menubarcodePref);
            }
            menubarcodePref.setChecked((KTSyncData.KDCSettings & KTSyncData.MENUBARCODE_MASK) != 0);
        }

        // action preferences 
        PreferenceCategory actionPrefCat = new PreferenceCategory(this);
        actionPrefCat.setTitle("Other KDC commands");
        root.addPreference(actionPrefCat);

        // Clear memory
        clearPref = getPreferenceManager().createPreferenceScreen(this);
        clearPref.setKey("clear_preference");
        clearPref.setTitle("Clear memory");
        clearPref.setSummary("Erase whole data in KDC memory");
        actionPrefCat.addPreference(clearPref);
        clearMemory();

        // Sync clock
        clockPref = getPreferenceManager().createPreferenceScreen(this);
        clockPref.setKey("clock_preference");
        clockPref.setTitle("Synchronize KDC clock");
        String temp = String.format("%d/%d/%d:%d:%d:%d",
                KTSyncData.DateTime[0] + 2000,
                KTSyncData.DateTime[1],
                KTSyncData.DateTime[2],
                KTSyncData.DateTime[3],
                KTSyncData.DateTime[4],
                KTSyncData.DateTime[5]);

        clockPref.setSummary(temp);
        actionPrefCat.addPreference(clockPref);
        syncClock();

        // Factory Default
        factoryPref = getPreferenceManager().createPreferenceScreen(this);
        factoryPref.setKey("factory_preference");
        factoryPref.setTitle("Factory default");
        factoryPref.setSummary("Set KDC to factory default state");
        actionPrefCat.addPreference(factoryPref);
        factoryDefault();

        return root;
    }

    private void clearMemory() {
        clearPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Log.d(TAG, "clear memory");
                if (KTSyncData.bIsConnected) KTSyncData.mKScan.ClearMemory();
                return true;
            }
        });
    }

    private void syncClock() {
        clockPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Log.d(TAG, "sync clock");
                if (!KTSyncData.bIsConnected) return true;
                KTSyncData.mKScan.SyncClock();
                String temp = String.format("%d/%d/%d:%d:%d:%d",
                        KTSyncData.DateTime[0] + 2000,
                        KTSyncData.DateTime[1],
                        KTSyncData.DateTime[2],
                        KTSyncData.DateTime[3],
                        KTSyncData.DateTime[4],
                        KTSyncData.DateTime[5]);

                clockPref.setSummary(temp);
                return true;
            }
        });
    }

    private void factoryDefault() {
        factoryPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Log.d(TAG, "factory default");
                if (KTSyncData.bIsConnected) KTSyncData.mKScan.FactoryDefault();
                return true;
            }
        });
    }
}