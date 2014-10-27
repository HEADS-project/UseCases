/**
 * Copyright (C) 2012 SINTEF <fabien@fleurey.com>
 *
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingml.chestbelt.android.chestbeltdroid.preferences;


import no.tellu.tracker.heads.R;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;

public class ConnectionPrefFragment extends PreferenceFragment {
	
	private EditTextPreference duration;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		addPreferencesFromResource(R.xml.pref_connection_fragment);
		CheckBoxPreference autoReconnect = (CheckBoxPreference) findPreference(getString(R.string.pref_autoreconnect_key));
		duration = (EditTextPreference) findPreference(getString(R.string.pref_autoreconnect_duration_key));
		duration.setDependency(autoReconnect.getKey());
		duration.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				duration.setSummary(newValue + " minutes");
				return true;
			}
		});
	}
	
	@Override
	public void onResume() {
		super.onResume();
		duration.setSummary(duration.getText() + " minutes");
	}
}
