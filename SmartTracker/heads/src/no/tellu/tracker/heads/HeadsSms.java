package no.tellu.tracker.heads;

import no.tellu.android.app.system.SmsReceiver;

public class HeadsSms extends SmsReceiver {

	@Override
	protected String getCodeword() {
		return "STGPS#";
	}
}
