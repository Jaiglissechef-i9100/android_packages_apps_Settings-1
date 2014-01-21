/*
 * Copyright (C) 2013 The LiquidSmooth Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.liquid;

import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SeekBarPreference;
import android.provider.Settings;

import com.android.internal.util.liquid.DeviceUtils;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class LockscreenSettings extends SettingsPreferenceFragment
        implements OnPreferenceChangeListener {

    private static final String TAG = "LockscreenSettings";

    private static final String KEY_INTERFACE_SETTINGS = "lock_screen_interface";
    private static final String KEY_TARGET_SETTINGS = "lock_screen_targets";
    private static final String KEY_WIDGETS_SETTINGS = "lock_screen_widgets";
    private static final String KEY_GENERAL_CATEGORY = "general_category";
    private static final String KEY_BATTERY_AROUND_RING = "battery_around_ring";
    private static final String KEY_ALWAYS_BATTERY_PREF = "lockscreen_battery_status";
    private static final String KEY_LOCK_BEFORE_UNLOCK = "lock_before_unlock";
    private static final String KEY_QUICK_UNLOCK_CONTROL = "quick_unlock_control";
    private static final String KEY_MENU_UNLOCK_PREF = "menu_unlock";
    private static final String KEY_LOCKSCREEN_TORCH = "lockscreen_torch";
    private static final String KEY_SEE_TRHOUGH = "see_through";
    private static final String KEY_BLUR_BEHIND = "blur_behind";
    private static final String KEY_BLUR_RADIUS = "blur_radius";

    private DevicePolicyManager mDPM;
    private Preference mLockscreenWidgets;

    private CheckBoxPreference mLockRingBattery;
    private CheckBoxPreference mBatteryStatus;
    private CheckBoxPreference mLockBeforeUnlock;
    private CheckBoxPreference mLockQuickUnlock;
    private CheckBoxPreference mMenuUnlock;
    private CheckBoxPreference mGlowpadTorch;
    private CheckBoxPreference mSeeThrough;
    private CheckBoxPreference mBlurBehind;
    private SeekBarPreference mBlurRadius;

    // needed for menu unlock
    private Resources keyguardResource;
    private boolean mMenuUnlockDefault;
    private static final int KEY_MASK_MENU = 0x04;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.liquid_lockscreen_settings);

        PreferenceScreen prefs = getPreferenceScreen();

        mLockRingBattery = (CheckBoxPreference) prefs
                .findPreference(KEY_BATTERY_AROUND_RING);
        if (mLockRingBattery != null) {
            mLockRingBattery.setChecked(Settings.System.getInt(getContentResolver(),
                    Settings.System.BATTERY_AROUND_LOCKSCREEN_RING, 0) == 1);
        }

        mLockBeforeUnlock = (CheckBoxPreference) prefs
                .findPreference(KEY_LOCK_BEFORE_UNLOCK);
        if (mLockBeforeUnlock != null) {
            mLockBeforeUnlock.setChecked(Settings.Secure.getInt(getContentResolver(),
                    Settings.Secure.LOCK_BEFORE_UNLOCK, 0) == 1);
            mLockBeforeUnlock.setOnPreferenceChangeListener(this);
        }

        mLockQuickUnlock = (CheckBoxPreference) prefs
                .findPreference(KEY_QUICK_UNLOCK_CONTROL);
        if (mLockQuickUnlock != null) {
            mLockQuickUnlock.setChecked(Settings.System.getInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_QUICK_UNLOCK_CONTROL, 0) == 1);
        }

        Resources keyguardResources = null;
        try {
            keyguardResources = mPM.getResourcesForApplication("com.android.keyguard");
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMenuUnlockDefault = keyguardResources != null
            ? keyguardResources.getBoolean(keyguardResources.getIdentifier(
            "com.android.keyguard:bool/config_disableMenuKeyInLockScreen", null, null)) : false;

        mGlowpadTorch = (CheckBoxPreference) prefs
                .findPreference(KEY_LOCKSCREEN_TORCH);
        if (mGlowpadTorch != null) {
            mGlowpadTorch.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_GLOWPAD_TORCH, 0) == 1);
            mGlowpadTorch.setOnPreferenceChangeListener(this);
        }

        mBatteryStatus = (CheckBoxPreference) prefs
                .findPreference(KEY_ALWAYS_BATTERY_PREF);
        if (mBatteryStatus != null) {
            mBatteryStatus.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_ALWAYS_SHOW_BATTERY, 0) == 1);
            mBatteryStatus.setOnPreferenceChangeListener(this);
        }

        if (!DeviceUtils.deviceSupportsTorch(getActivity())) {
            prefs.removePreference(mGlowpadTorch);
        }

        mSeeThrough = (CheckBoxPreference) prefs.findPreference(KEY_SEE_TRHOUGH);

        mBlurBehind = (CheckBoxPreference) prefs.findPreference(KEY_BLUR_BEHIND);
        mBlurBehind.setChecked(Settings.System.getInt(getContentResolver(), 
                        Settings.System.LOCKSCREEN_BLUR_BEHIND, 0) == 1);
        mBlurBehind.setEnabled(mSeeThrough.isChecked());

        mBlurRadius = (SeekBarPreference) prefs.findPreference(KEY_BLUR_RADIUS);
        mBlurRadius.setProgress(Settings.System.getInt(getContentResolver(), 
                        Settings.System.LOCKSCREEN_BLUR_RADIUS, 12));
        mBlurRadius.setOnPreferenceChangeListener(this);
        mBlurRadius.setEnabled(mBlurBehind.isChecked() && mBlurBehind.isEnabled());

        PreferenceCategory generalCategory = (PreferenceCategory) prefs
                .findPreference(KEY_GENERAL_CATEGORY);

        if (generalCategory != null) {
            Preference lockInterfacePref = findPreference(KEY_INTERFACE_SETTINGS);
            Preference lockTargetsPref = findPreference(KEY_TARGET_SETTINGS);
            if (lockInterfacePref != null && lockTargetsPref != null) {
                if (!DeviceUtils.isPhone(getActivity())) {
                     generalCategory.removePreference(lockInterfacePref);
                } else {
                     generalCategory.removePreference(lockTargetsPref);
                }
            }
        }

        mLockscreenWidgets = prefs.findPreference(KEY_WIDGETS_SETTINGS);
        mDPM = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);

        if (mLockscreenWidgets != null) {
            if (ActivityManager.isLowRamDeviceStatic()) {
                if (generalCategory != null) {
                    generalCategory.removePreference(prefs
                            .findPreference(KEY_WIDGETS_SETTINGS));
                    mLockscreenWidgets = null;
                }
            } else {
                final boolean disabled = (0 != (mDPM.getKeyguardDisabledFeatures(null)
                        & DevicePolicyManager.KEYGUARD_DISABLE_WIDGETS_ALL));
                mLockscreenWidgets.setEnabled(!disabled);
            }
        }

        mMenuUnlock = (CheckBoxPreference) root.findPreference(MENU_UNLOCK_PREF);
        if (mMenuUnlock != null) {
            int deviceKeys = getResources().getInteger(
                    com.android.internal.R.integer.config_deviceHardwareKeys);
            boolean hasMenuKey = (deviceKeys & KEY_MASK_MENU) != 0;
            if (hasMenuKey) {
                boolean settingsEnabled = Settings.System.getIntForUser(
                        getContentResolver(),
                        Settings.System.MENU_UNLOCK_SCREEN, mMenuUnlockDefault ? 0 : 1,
                        UserHandle.USER_CURRENT) == 1;
                mMenuUnlock.setChecked(settingsEnabled);
                mMenuUnlock.setOnPreferenceChangeListener(this);
            } else {
                securityCategory.removePreference(mMenuUnlock);
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        if (preference == mLockBeforeUnlock) {
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.LOCK_BEFORE_UNLOCK,
                    ((Boolean) value) ? 1 : 0);
        } else if (preference == mBlurRadius) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_BLUR_RADIUS,
                    (Integer)value);
        } else if (preference == mGlowpadTorch) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_GLOWPAD_TORCH,
                    ((Boolean) value) ? 1 : 0);
        } else if (preference == mBatteryStatus) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_ALWAYS_SHOW_BATTERY,
                    ((Boolean) value) ? 1 : 0);
        } else if (preference == mMenuUnlock) {
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.MENU_UNLOCK_SCREEN,
                    ((Boolean) value) ? 1 : 0, UserHandle.USER_CURRENT);
        }
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mSeeThrough) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_SEE_THROUGH,
                    mSeeThrough.isChecked() ? 1 : 0);
            mBlurBehind.setEnabled(mSeeThrough.isChecked());
            mBlurRadius.setEnabled(mBlurBehind.isChecked() && mBlurBehind.isEnabled());
            return true;
        } else if (preference == mBlurBehind) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_BLUR_BEHIND,
                    mBlurBehind.isChecked() ? 1 : 0);
            mBlurRadius.setEnabled(mBlurBehind.isChecked());
            return true;
        } else if (preference == mLockQuickUnlock) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_QUICK_UNLOCK_CONTROL,
                    mLockQuickUnlock.isChecked() ? 1 : 0);
            return true;
        } else if (preference == mLockRingBattery) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.BATTERY_AROUND_LOCKSCREEN_RING,
                    mLockRingBattery.isChecked() ? 1 : 0);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
