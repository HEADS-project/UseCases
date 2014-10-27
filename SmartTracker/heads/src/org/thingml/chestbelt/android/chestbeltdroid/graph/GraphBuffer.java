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
package org.thingml.chestbelt.android.chestbeltdroid.graph;


public class GraphBuffer {

	private int[] graphData;
	private int size;
	private int lastValue;
	private boolean empty = true;
	private int notValidNumber = Integer.MIN_VALUE;
	private int counter = 0;
	
	public GraphBuffer(){
		size = 100;
		graphData = new int[size];
		initializeArray(notValidNumber);
		lastValue = notValidNumber;
	}
	
	public GraphBuffer(int customSize) {
		size = customSize;
		graphData = new int[size];
		initializeArray(notValidNumber);
		lastValue = notValidNumber;
	}
	
	public GraphBuffer(int customSize, int inValidNumber) {
		size = customSize;
		graphData = new int[size];
		notValidNumber = inValidNumber;
		initializeArray(notValidNumber);
		lastValue = notValidNumber;
	}

	public int getLastValue() {
		return lastValue;
	}
	
	public boolean isEmpty() {
		return empty;
	}
	
	public int getInvalidNumber() {
		return notValidNumber;
	}
	
	public synchronized int[] getGraphData() {
		int[] result = graphData.clone();
		return result;
	}

	public synchronized boolean insertData(int data) {
		if (data == notValidNumber) {
			return false;
		}
		lastValue = data;
		empty = false;
		if (counter >= size) {
			for (int i = 1 ; i < graphData.length ; i++) {
				graphData[i-1] = graphData[i];
			}
			graphData[size-1] = data;
			counter++;
			return true;
		} else {
			for (int i = 0 ; i < graphData.length ; i++){
				if (graphData[i] == notValidNumber) {
					graphData[i] = data;
					counter ++;
					return true;
				}
			}
		}
		return false;
	}
	
	private void initializeArray(int inValidNumber) {
		for (int i = 0 ; i < graphData.length ; i++) {
			graphData[i] = inValidNumber;
		}
		empty = true;
	}
}
