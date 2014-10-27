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
import java.util.Hashtable;

import org.thingml.chestbelt.driver.ChestBelt;

import android.os.Binder;

public class ChestBeltBinder extends Binder {
	
	public interface ChestBeltBinderCallback {
		public void deviceConnected(String address);
		public void deviceDisconnected(String address);
	}
	
	private Hashtable<String, ChestBelt> runningSessions;
	private Hashtable<String, ChestBeltGraphBufferizer> graphBufferizers;
	private ArrayList<ChestBeltBinderCallback> listenners = new ArrayList<ChestBeltBinder.ChestBeltBinderCallback>();
	
	public ChestBeltBinder(Hashtable<String, ChestBelt> runningSessions, Hashtable<String, ChestBeltGraphBufferizer> graphBufferizers) {
		this.runningSessions = runningSessions;
		this.graphBufferizers = graphBufferizers;
	}
	
	public boolean isConnected(String address) {
		return runningSessions.containsKey(address);
	}
	public ChestBelt getDriver(String address) {
		return runningSessions.get(address);
	}
    public ChestBeltGraphBufferizer getGraphBufferizers(String address) {
    	return graphBufferizers.get(address);
    }
    
    public void addListenner(ChestBeltBinderCallback listenner) {
    	listenners.add(listenner);
    }
    public void removeListenner(ChestBeltBinderCallback listenner) {
    	listenners.remove(listenner);
    }
    
    public void deviceConnected(String address) {
    	for (ChestBeltBinderCallback listenner : listenners) {
    		listenner.deviceConnected(address);
    	}
    }
    public void deviceDisconnected(String address) {
    	for (ChestBeltBinderCallback listenner : listenners) {
    		listenner.deviceDisconnected(address);
    	}
    }
}
