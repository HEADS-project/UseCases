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

import org.thingml.chestbelt.android.chestbeltdroid.graph.GraphBuffer;
import org.thingml.chestbelt.driver.ChestBeltListener;

public class ChestBeltGraphBufferizer implements ChestBeltListener {
	
	public interface ChestBeltCallback {
		public void connectionLost(String address);
	}

	private ChestBeltCallback listenner;
	private String address;
	
	private GraphBuffer bufferHeartrate = new GraphBuffer();
	
	private GraphBuffer bufferTemperature = new GraphBuffer();
	private GraphBuffer bufferBattery = new GraphBuffer();
	private GraphBuffer bufferActivityLevel = new GraphBuffer();
	
	private GraphBuffer bufferECG = new GraphBuffer(400);
	
	private GraphBuffer bufferGyroPitch = new GraphBuffer(250);
	private GraphBuffer bufferGyroRoll = new GraphBuffer(250);
	private GraphBuffer bufferGyroYaw = new GraphBuffer(250);
	
	private GraphBuffer bufferAccLateral = new GraphBuffer(250);
	private GraphBuffer bufferAccLongitudinal = new GraphBuffer(250);
	private GraphBuffer bufferAccVertical = new GraphBuffer(250);
	
	public ChestBeltGraphBufferizer(ChestBeltCallback listenner, String address) {
		this.listenner = listenner;
		this.address = address;
	}
	
	public GraphBuffer getBufferHeartrate() {
		return bufferHeartrate;
	}
	
	public GraphBuffer getBufferTemperature() {
		return bufferTemperature;
	}
	
	public GraphBuffer getBufferBattery() {
		return bufferBattery;
	}
	
	public GraphBuffer getBufferActivityLevel() {
		return bufferActivityLevel;
	}
	
	public GraphBuffer getBufferECG() {
		return bufferECG;
	}

	public GraphBuffer getBufferGyroPitch() {
		return bufferGyroPitch;
	}

	public GraphBuffer getBufferGyroRoll() {
		return bufferGyroRoll;
	}

	public GraphBuffer getBufferGyroYaw() {
		return bufferGyroYaw;
	}

	public GraphBuffer getBufferAccLateral() {
		return bufferAccLateral;
	}

	public GraphBuffer getBufferAccLongitudinal() {
		return bufferAccLongitudinal;
	}

	public GraphBuffer getBufferAccVertical() {
		return bufferAccVertical;
	}

	@Override
	public void cUSerialNumber(long value, int timestamp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void batteryStatus(final int value, int timestamp) {
		bufferBattery.insertData(value);
	}

	@Override
	public void indication(int value, int timestamp) {
		if (value >= 10 && value <= 13) {
			bufferActivityLevel.insertData(value - 10);
		}
	}

	@Override
	public void status(int value, int timestamp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void messageOverrun(int value, int timestamp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void heartRate(final int value, int timestamp) {
		bufferHeartrate.insertData(value/10);
	}

	@Override
	public void heartRateConfidence(int value, int timestamp) {
		//bufferHeartrateConfidence.insertData(value);
	}

	@Override
	public void eCGData(final int value) {
		bufferECG.insertData(value);
	}

	@Override
	public void eCGSignalQuality(int value, int timestamp) {
		//bufferECGSignalQuality.insertData(value);
	}

	@Override
	public void eCGRaw(int value, int timestamp) {
		bufferECG.insertData(value);	
	}

	@Override
	public void gyroPitch(int value, int timestamp) {
		bufferGyroPitch.insertData(value);
	}

	@Override
	public void gyroRoll(int value, int timestamp) {
		bufferGyroRoll.insertData(value);
	}

	@Override
	public void gyroYaw(int value, int timestamp) {
		bufferGyroYaw.insertData(value);
	}

	@Override
	public void accLateral(int value, int timestamp) {
		bufferAccLateral.insertData(value);
	}

	@Override
	public void accLongitudinal(int value, int timestamp) {
		bufferAccLongitudinal.insertData(value);
	}

	@Override
	public void accVertical(int value, int timestamp) {
		bufferAccVertical.insertData(value);
	}

	@Override
	public void rawActivityLevel(int value, int timestamp) {
		bufferActivityLevel.insertData(value - 10);
	}

	@Override
	public void combinedIMU(int ax, int ay, int az, int gx, int gy, int gz, int timestamp) {
		bufferAccLateral.insertData(ay);
		bufferAccLongitudinal.insertData(az);
		bufferAccVertical.insertData(ax);
		bufferGyroPitch.insertData(gy);
		bufferGyroRoll.insertData(gx);
		bufferGyroYaw.insertData(gz);
	}

	@Override
	public void skinTemperature(final int value, int timestamp) {
		bufferTemperature.insertData(value/10);	
	}

	@Override
	public void cUFWRevision(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fullClockTimeSync(long arg0, boolean arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void referenceClockTime(long arg0, boolean arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void connectionLost() {
		listenner.connectionLost(address);
	}

}
