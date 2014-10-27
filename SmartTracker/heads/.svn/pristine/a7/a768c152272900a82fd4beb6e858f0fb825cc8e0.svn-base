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

import org.thingml.chestbelt.driver.ChestBeltMode;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;

public class ChestBeltPrefFragment extends PreferenceFragment {
	
	private ListPreference dataMode;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		addPreferencesFromResource(R.xml.pref_chestbelt_fragment);
		dataMode = (ListPreference) findPreference(getString(R.string.pref_datamode_key));
		
		// Can't set integers as preference list values so use strings. Careful to the order!
		String[] modeValues = new String[]{
				String.valueOf(ChestBeltMode.Extracted.getCode()),
				String.valueOf(ChestBeltMode.FullECG.getCode()),
				String.valueOf(ChestBeltMode.Raw.getCode()),
				String.valueOf(ChestBeltMode.RawAccelerometer.getCode()),
				String.valueOf(ChestBeltMode.RawGyroMode.getCode()),
				String.valueOf(ChestBeltMode.Test.getCode())};
		dataMode.setEntryValues(modeValues);
		dataMode.setDefaultValue(String.valueOf(ChestBeltMode.Extracted.getCode()));
	}
}
