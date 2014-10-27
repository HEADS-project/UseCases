package no.tellu.tracker.control;

import no.tellu.android.app.ModuleController;
import no.tellu.tracker.Track;
import no.tellu.tracker.TrackerControl;
import no.tellu.tracker.heads.R;

import org.thingml.chestbelt.android.chestbeltdroid.communication.ChestBeltServiceConnection;
import org.thingml.chestbelt.android.chestbeltdroid.graph.GraphBaseView;
import org.thingml.chestbelt.android.chestbeltdroid.graph.GraphBaseView.GraphListenner;
import org.thingml.chestbelt.android.chestbeltdroid.graph.GraphDetailsView;
import org.thingml.chestbelt.android.chestbeltdroid.graph.GraphWrapper;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Main view of the application, with tracker control and
 * status.
 * 
 * @author Lars Thomas Boye, Tellu AS
 */
public class HeadsControlView extends FullControlView implements OnCheckedChangeListener, OnItemSelectedListener, GraphListenner {
	CheckBox toggleSwitch;
	boolean switchListenerOn = true;
	private String deviceName;
	
	private HeadsControl hc;
	private TextView tvSensorName;
	private TextView tvSensorValue;
	private ImageView ivSensorLed;
	private ImageView ivSensorIcon;
	private Spinner spinnerSensorName;
	private GraphDetailsView graph;
	private ChestBeltServiceConnection chestBeltConnection;
	private String curGraph = "Heart Rate";
	
	public HeadsControlView(ModuleController mc, String id) {
		super(mc, id);
		this.hc = (HeadsControl)mc;
	}
	
	@Override
	public void onShown(View viewObj, String paramString) {
		toggleSwitch = (CheckBox)findViewById(R.id.trswitch);
		toggleSwitch.setOnCheckedChangeListener(this);
		ivSensorLed = (ImageView)findViewById(R.id.heads_status_led);
		ivSensorLed.setImageResource(R.drawable.led_green);
		tvSensorName = (TextView) findViewById(R.id.heads_status_text);
		String status = getApp().getString(R.string.heads_status);
		tvSensorName.setText(status + "  ("+hc.getDeviceName()+")");
		
		spinnerSensorName = (Spinner)findViewById(R.id.spinner_graphrow_name);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(spinnerSensorName.getContext(), android.R.layout.simple_spinner_item, new String[]{"Heart Rate", "ECG", "Activity", "Posture", "Temperature", "Battery"});
		
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerSensorName.setAdapter(adapter);
		spinnerSensorName.setOnItemSelectedListener(this);
		
		tvSensorName = (TextView) findViewById(R.id.tv_sensor_name);
		ivSensorIcon = (ImageView) findViewById(R.id.iv_sensor_icon);
		tvSensorValue = (TextView) findViewById(R.id.tv_sensor_value);
		tvSensorName.setText("Heart rate");
		ivSensorIcon.setImageResource(R.drawable.ic_heartrate);
		graph =  (GraphDetailsView) findViewById(R.id.gv_sensor_graph);
		
	}
	

	@Override
	public void onHidden() {
		toggleSwitch = null;
	}

	@Override
	public void onTrackerState(int newState, Track track) {
		switchListenerOn = false;
		toggleSwitch.setChecked(newState > TrackerControl.TRACKING_TRACK);
		switchListenerOn = true;
	}
	
	

	@Override
	public void onCheckedChanged(CompoundButton view, boolean isChecked) {
		if (switchListenerOn) {
			if (isChecked)
				trackerCtrl.startTracking(false);
			else
				trackerCtrl.stopTracking(false);
		}
	}

	
	public void onBindingReady(ChestBeltServiceConnection chestBeltConnection) {
		this.chestBeltConnection = chestBeltConnection;
		if(chestBeltConnection.isConnected()){
			if(curGraph.equals("Heart Rate")){
				tvSensorName.setText("Heart rate");
				ivSensorIcon.setImageResource(R.drawable.ic_heartrate);
				ivSensorIcon.setBackgroundColor(0);
				GraphWrapper wrapper = new GraphWrapper(chestBeltConnection.getBufferizer().getBufferHeartrate());
				wrapper.setGraphOptions(Color.RED, 1000, GraphBaseView.BARCHART, 30, 160, "Heart rate");
				wrapper.setPrinterParameters(false, false, true);
				graph.registerWrapper(wrapper);		
			} else if (curGraph.equals("ECG")) {
				tvSensorName.setText("ECG");
				ivSensorIcon.setImageResource(R.drawable.ic_heartrate);
				ivSensorIcon.setBackgroundColor(0);
				GraphWrapper wrapper = new GraphWrapper(chestBeltConnection.getBufferizer().getBufferECG());
				wrapper.setGraphOptions(Color.RED, 100, GraphBaseView.LINECHART, 0, 4096, "ECG");
				wrapper.setLineNumber(0);
				graph.registerWrapper(wrapper);
			} else if (curGraph.equals("Temperature")) {
				tvSensorName.setText("Temperature");
				ivSensorIcon.setImageResource(R.drawable.ic_thermometer);
				ivSensorIcon.setBackgroundColor(0);
				GraphWrapper wrapper = new GraphWrapper(chestBeltConnection.getBufferizer().getBufferTemperature());
				wrapper.setGraphOptions(Color.BLUE, 1000, GraphBaseView.BARCHART, 20, 45, "Temperature");
				wrapper.setPrinterParameters(false, false, true);
				graph.registerWrapper(wrapper);
			} else if (curGraph.equals("Activity")) {
				tvSensorName.setText("Activity");
				ivSensorIcon.setImageResource(R.drawable.activity0);
				ivSensorIcon.setBackgroundColor(0xFF737373);
				GraphWrapper wrapper = new GraphWrapper(chestBeltConnection.getBufferizer().getBufferActivityLevel());
				wrapper.setGraphOptions(Color.GRAY, 500, GraphBaseView.BARCHART, 0, 3, "Activity");
				wrapper.setPrinterParameters(false, false, true);
				wrapper.setLineNumber(3);
				graph.registerWrapper(wrapper);
			} else if (curGraph.equals("Posture")) {
				tvSensorName.setText("Posture");
				ivSensorIcon.setImageResource(R.drawable.pos_upright);
				ivSensorIcon.setBackgroundColor(0xFF737373);
				GraphWrapper wrapper = new GraphWrapper(chestBeltConnection.getBufferizer().getBufferPosition());
				wrapper.setGraphOptions(Color.GRAY, 500, GraphBaseView.BARCHART, 0, 6, "Posture");
				wrapper.setPrinterParameters(false, false, true);
				wrapper.setLineNumber(3);
				graph.registerWrapper(wrapper);
			}else if (curGraph.equals("Battery")) {
				tvSensorName.setText("Battery");
				ivSensorIcon.setImageResource(R.drawable.tracker_pow);
				ivSensorIcon.setBackgroundColor(0);
				GraphWrapper wrapper = new GraphWrapper(chestBeltConnection.getBufferizer().getBufferBattery());
				wrapper.setGraphOptions(Color.GREEN, 3000, GraphBaseView.BARCHART, 0, 100, "Battery");
				wrapper.setPrinterParameters(false, false, true);
				graph.registerWrapper(wrapper);
			}
			graph.registerListenner(this);
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		this.curGraph = (String)parent.getItemAtPosition(pos);
		tvSensorValue.setText("");
		onBindingReady(chestBeltConnection);
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void lastValueChanged(final int value) {
		
		if(value == Integer.MIN_VALUE) return;
		
		Runnable r = new Runnable() {
			@Override
			public void run() {
				if(curGraph.equals("Activity")){
					tvSensorValue.setText("");
					switch (value) {
					case 0: ivSensorIcon.setImageResource(R.drawable.activity0); break;
						case 1: ivSensorIcon.setImageResource(R.drawable.activity1); break;
						case 2: ivSensorIcon.setImageResource(R.drawable.activity2); break;
						case 3: ivSensorIcon.setImageResource(R.drawable.activity3); break;
					default: break;
					}
				} else if(curGraph.equals("Posture")) {
					tvSensorValue.setText("");
					switch (value) {
						case 1: ivSensorIcon.setImageResource(R.drawable.pos_upright); break;
						case 2: ivSensorIcon.setImageResource(R.drawable.pos_prone); break;
						case 3: ivSensorIcon.setImageResource(R.drawable.pos_supine); break;
						case 4: ivSensorIcon.setImageResource(R.drawable.pos_side);	break;
						case 5: ivSensorIcon.setImageResource(R.drawable.pos_inverted);	break;
						case 6: ivSensorIcon.setImageResource(android.R.id.empty);	break;
					default:break; 					
					}
				} else if(curGraph.equals("ECG")) {
					tvSensorValue.setText("");
				} else {
					tvSensorValue.setText(String.valueOf(value));
				}
			}
		};
		Handler h = new Handler(Looper.getMainLooper());
		h.post(r);
	}

	public void disconnectDevice() {
		ivSensorLed.setImageResource(R.drawable.led_off);
		
	}
}
