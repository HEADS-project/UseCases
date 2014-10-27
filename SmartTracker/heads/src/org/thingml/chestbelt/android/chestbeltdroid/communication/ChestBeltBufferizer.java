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
package org.thingml.chestbelt.android.chestbeltdroid.communication;

import java.util.ArrayList;

public class ChestBeltBufferizer {

	private ArrayList<Integer> values = new ArrayList<Integer>();
	private StringBuilder string = new StringBuilder();
	private long startTime;
	private long interval;
	private boolean ready = false;
	
	public ChestBeltBufferizer() {
	}
	
	public ChestBeltBufferizer(long startTime, long interval) {
		this.startTime = startTime;
		this.interval = interval;
	}
	
	public long getStartTime() {
		return startTime;
	}
	
	public boolean isReady() {
		return ready;
	}
	
	public ArrayList<Integer> getValues() {
		return values;
	}
	
	public void reset(long startTime, long interval) {
		this.startTime = startTime;
		this.interval = interval;
		ready = false;
		values.clear();
		string = new StringBuilder();
	}
	
	public void addMeasure(int value, long time) {
		values.add(value);
		string.append(value + ";");
		if (time >= startTime + interval) {
			ready = true;
		}
	}
	
	@Override
	public String toString() {
		return new String(string);
	}
}
