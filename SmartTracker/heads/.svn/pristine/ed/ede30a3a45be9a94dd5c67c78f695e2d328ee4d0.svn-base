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

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.util.Log;

public class ConnectionTask extends AsyncTask<Integer, Void, Boolean> {

	public static final int MODE_STD_SEC = 10;
	public static final int MODE_STD_UNSEC = 20;
	public static final int MODE_NAT_SEC = 30;
	public static final int MODE_NAT_UNSEC = 40;

	private static final String TAG = ConnectionTask.class.getSimpleName();
	private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	private BluetoothDevice device;
	private BluetoothSocket socket;
	private ConnectionTaskReceiver receiver;
	private String errorMessage;

	public interface ConnectionTaskReceiver {
		public void onConnectionSuccess(String name, String address);
		public void onConnectionFailure(String name, String address, String errorMessage);
	}

	public ConnectionTask (ConnectionTaskReceiver receiver, BluetoothDevice device) {
		this.receiver = receiver;
		this.device = device;
	}

	@Override
	protected Boolean doInBackground(Integer... params) {
		Log.i(TAG, "Connecting to " + device.getName() + " (" + device.getAddress() + ")...");
		return makeConnection(params[0]);
	}

	@Override
	protected void onPostExecute(Boolean connected) {
		String name = device.getName();
		String address = device.getAddress();
		if (connected) {
			Log.i(TAG, "... Connection success for " +  name + " (" + address + ")");
			receiver.onConnectionSuccess(name, address);
		} else {
			Log.e(TAG, "... Connection failure for " +  name + " (" + address + ")");
			receiver.onConnectionFailure(device.getName(), device.getAddress(), errorMessage);
		}
	}

	public BluetoothSocket getSocket() {	
		return socket;
	}

	private boolean makeConnection(int mode) {
		switch (mode) {
		case MODE_STD_SEC:
			Log.i(TAG, "Try to connect in secure mode with standard API...");
			try {
				socket = device.createRfcommSocketToServiceRecord(uuid);
			} catch (IOException e) {
				errorMessage = e.getMessage();
				Log.e(TAG, errorMessage);
				Log.e(TAG, "... Failed[1].");
				return false;
			}
			break;
		case MODE_NAT_SEC:
			Log.w(TAG, "Try to connect in secure mode with reflective method...");
			try {
				socket = (BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(device, 1);
			} catch (Exception e) {
				errorMessage = e.getMessage();
				Log.e(TAG, errorMessage);
				Log.e(TAG, "... Failed[1].");
				return false;
			}
			break;
		case MODE_STD_UNSEC:
			Log.i(TAG, "Try to connect in unsecure mode with standard API...");
			try {
				socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
			} catch (IOException e) {
				errorMessage = e.getMessage();
				Log.e(TAG, errorMessage);
				Log.e(TAG, "... Failed[1].");
				return false;
			}
			break;
		case MODE_NAT_UNSEC:
			Log.w(TAG, "Try to connect in unsecure mode with reflective method...");
			try {
				socket = (BluetoothSocket) device.getClass().getMethod("createInsecureRfcommSocket", new Class[] {int.class}).invoke(device, 1);
			} catch (Exception e) {
				errorMessage = e.getMessage();
				Log.e(TAG, errorMessage);
				Log.e(TAG, "... Failed[1].");
				return false;
			} 
			break;
		}
		if (socket == null) {
			Log.e(TAG, "Null socket");
			return false;
		}
		try {
			socket.connect();
		} catch (IOException e) {
			errorMessage = e.getMessage();
			Log.e(TAG, errorMessage);
			Log.e(TAG, "... Failed.");
			return false;
		}
		return true;
	}
}
