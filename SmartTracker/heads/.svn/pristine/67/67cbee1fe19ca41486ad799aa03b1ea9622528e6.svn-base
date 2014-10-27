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
package org.thingml.chestbelt.android.chestbeltdroid.devices;

import java.util.ArrayList;

/**
 * Define the object model for one bluetooth device.
 * @author Fabien Fleurey
 */

public class Device {
	
	public static final String EXTRA_DEVICE_NAME = Device.class.getName() + ".EXTRA_DEVICE_NAME";
	public static final String EXTRA_DEVICE_ADDRESS = Device.class.getName() + ".EXTRA_DEVICE_ADDRESS";
		
	private String name;
	private String address;
	private boolean connected;
	private boolean available;
	
	public Device(String name, String adress) {
		this.name = name;
		this.address = adress;
	}
	
	public Device(String name, String address, boolean connected, boolean available) {
		this.name = name;
		this.address = address;
		this.connected = connected;
		this.available = available;
	}
	
	public String getName() {
		return name;
	}
	
	public String getAddress() {
		return address;
	}
	
	public void setConnected(boolean status) {
		connected = status;
	}
	
	public boolean isConnected() {
		return connected;
	}
	
	public boolean isAvailable() {
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}

	@Override
	public String toString() {
		return "name: " + name + " (" + address + ") - Connected: " + connected + " - Available: " + available; 
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Device) {
			return address.equals(((Device) o).getAddress()); 
		}
		return false;
	}
	
	public static boolean contain(ArrayList<Device> devices, String address) {
		for (Device d : devices) {
			if (d.getAddress().equals(address)) {
				return true;
			}
		}
		return false;
	}
	
	public static Device getFromAddress(ArrayList<Device> devices, String address) {
		for (Device d : devices) {
			if (d.getAddress().equals(address)) {
				return d;
			}
		}
		return null;
	}
}
