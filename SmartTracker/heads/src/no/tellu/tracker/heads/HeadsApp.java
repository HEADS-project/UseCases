package no.tellu.tracker.heads;

import java.util.List;

import android.content.pm.ActivityInfo;
import no.tellu.android.app.ModuleDef;
import no.tellu.android.app.conf.ConfigurationController;
import no.tellu.android.app.logconf.LogfileController;
import no.tellu.android.app.system.TelluApp;
import no.tellu.android.app.system.TelluService;
import no.tellu.android.app.view.LayoutDef;
import no.tellu.android.app.view.ViewManagerImpl;
import no.tellu.android.util.log.Alog;
import no.tellu.android.util.log.Logger;
import no.tellu.tracker.PosStringUtil;
import no.tellu.tracker.TrackerControl;
import no.tellu.tracker.TrackerLogger;
import no.tellu.tracker.com.ComController;
import no.tellu.tracker.control.HeadsControl;
import no.tellu.tracker.db.TrackerDatabase;
import no.tellu.tracker.heads.sensor.SensorComController;
import no.tellu.tracker.position.PositionManager;
import no.tellu.tracker.power.PowerObserver;

/**
 * The TelluApp class of the HEADS sensor application, representing
 * the application as a whole. This sub-class contains configuration
 * and initiation of the app, defining its modules, GUI container
 * configuration etc.
 * 
 * @author Lars Thomas Boye, Tellu AS
 */
public class HeadsApp extends TelluApp {
	public static final String LOG_SUBMIT_URL =
			"http://tellu.no/applog/postFile.php?app_id=heads";

	@Override
	protected Class<? extends TelluService> defineService() {
		return TelluService.class;
	}

	@Override
	protected void defineModules(List<ModuleDef> modules) {
		//Framework modules
		ModuleDef def = ConfigurationController.defineModule();
		def.setLoggerName(BASE_LOGGER_DEFAULT);
		modules.add(def);
		def = LogfileController.defineModule();
		def.setLoggerName(BASE_LOGGER_DEFAULT);
		modules.add(def);
		
		//Tracker modules
		def = HeadsControl.defineModule();
		def.setLoggerName(BASE_LOGGER_DEFAULT);
		modules.add(def);
		def = ComController.defineModule();
		def.setLoggerName(BASE_LOGGER_DEFAULT);
		modules.add(def);
		def = PositionManager.defineModule();
		def.setLoggerName(BASE_LOGGER_DEFAULT);
		modules.add(def);
		def = PowerObserver.defineModule();
		def.setLoggerName(BASE_LOGGER_DEFAULT);
		modules.add(def);
		def = SensorComController.defineModule();
		def.setLoggerName(BASE_LOGGER_DEFAULT);
		modules.add(def);
	}

	@Override
	protected ViewManagerImpl defineGui() {
		ViewManagerImpl vmi = new ViewManagerImpl(this, HeadsActivity.class);
		vmi.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		vmi.setInitialTheme(R.style.TelluTheme_Dark);
		
		LayoutDef full = new LayoutDef("fullscreen", R.layout.layout_fullscreen, R.id.mainframe);
		vmi.addLayout(full, false);
		
		return vmi;
	}

	@Override
	protected void onStart(AppVersionInfo appVersionInfo) {
		PosStringUtil.setContext(this);
		
		createModule(ConfigurationController.MODULE_ID);
		
		LogfileController lc =
				(LogfileController)createModule(LogfileController.MODULE_ID);
		lc.setSubmitUrl(LOG_SUBMIT_URL);
		lc.setGuiConfig(R.drawable.conf_loggers, null, R.drawable.conf_logfiles, null);
		lc.addLogger(Alog.getLogger(BASE_LOGGER_DEFAULT), Logger.INFO,
				"", R.string.trctr_config_syslog, new Integer[]{-1,1,3,4,6}, R.array.tlib_logger_levels);
		Logger tl = Alog.getLogger(TrackerLogger.class,
				TrackerControl.TRACKER_LOGGER);
		lc.addLogger(tl, Logger.INFO, TrackerControl.TRACKER_LOGGER,
				R.string.trctr_config_tlog, new Integer[]{-1,1,3,4}, R.array.tracker_config_tlog);
		
		TrackerDatabase tdb = new TrackerDatabase(this);
		addDataLayer(tdb);
		
		createModule(HeadsControl.MODULE_ID);
	}

}
