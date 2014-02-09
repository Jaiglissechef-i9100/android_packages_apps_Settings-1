/*
 * Copyright (C) 2013-2014 Dokdo Project - Gwon Hyeok
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

package com.android.settings.ose;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class PrefWeatherTile  extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static PreferenceCategory PreviewLayout;
    private static String WeatherTileIcon = "IconSet";

    private ListPreference mIconSet;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefweathertile);

	mIconSet = (ListPreference)findPreference(WeatherTileIcon);
	mIconSet.setSummary(mIconSet.getEntry());
	mIconSet.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
   public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
	if (preference == mIconSet) {
            int index = mIconSet.findIndexOfValue((String) newValue);
            Settings.System.putString(getContentResolver(), Settings.System.WEATHER_TILE_ICON, (String) newValue);
            mIconSet.setSummary(mIconSet.getEntries()[index]);
            return true;
	}
        return false;
    }


}
