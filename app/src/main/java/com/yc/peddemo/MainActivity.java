package com.yc.peddemo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tbruyelle.rxpermissions.RxPermissions;
import com.yc.peddemo.customview.CustomPasswordDialog;
import com.yc.peddemo.customview.CustomProgressDialog;
import com.yc.pedometer.info.BPVOneDayInfo;
import com.yc.pedometer.info.RateOneDayInfo;
import com.yc.pedometer.info.SevenDayWeatherInfo;
import com.yc.pedometer.info.SkipDayInfo;
import com.yc.pedometer.info.SleepTimeInfo;
import com.yc.pedometer.info.SportsModesInfo;
import com.yc.pedometer.info.StepOneDayAllInfo;
import com.yc.pedometer.info.SwimDayInfo;
import com.yc.pedometer.listener.RateCalibrationListener;
import com.yc.pedometer.listener.TurnWristCalibrationListener;
import com.yc.pedometer.sdk.BLEServiceOperate;
import com.yc.pedometer.sdk.BloodPressureChangeListener;
import com.yc.pedometer.sdk.BluetoothLeService;
import com.yc.pedometer.sdk.DataProcessing;
import com.yc.pedometer.sdk.ICallback;
import com.yc.pedometer.sdk.ICallbackStatus;
import com.yc.pedometer.sdk.OnServerCallbackListener;
import com.yc.pedometer.sdk.RateChangeListener;
import com.yc.pedometer.sdk.ServiceStatusCallback;
import com.yc.pedometer.sdk.SleepChangeListener;
import com.yc.pedometer.sdk.StepChangeListener;
import com.yc.pedometer.sdk.UTESQLOperate;
import com.yc.pedometer.sdk.WriteCommandToBLE;
import com.yc.pedometer.update.Updates;
import com.yc.pedometer.utils.BandLanguageUtil;
import com.yc.pedometer.utils.CalendarUtils;
import com.yc.pedometer.utils.GBUtils;
import com.yc.pedometer.utils.GetFunctionList;
import com.yc.pedometer.utils.GlobalVariable;
import com.yc.pedometer.utils.LogcatHelper;
import com.yc.pedometer.utils.MultipleSportsModesUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import no.nordicsemi.android.dfu.DfuServiceInitiator;
import rx.functions.Action1;

public class MainActivity extends Activity implements OnClickListener,
		ICallback, ServiceStatusCallback, OnServerCallbackListener,RateCalibrationListener,TurnWristCalibrationListener {
	private TextView connect_status, rssi_tv, tv_steps, tv_distance,
			tv_calorie, tv_sleep, tv_deep, tv_light, tv_awake, show_result,
			tv_rate, tv_lowest_rate, tv_verage_rate, tv_highest_rate;
	private EditText et_height, et_weight, et_sedentary_period;
	private Button btn_confirm, btn_sync_step, btn_sync_sleep, update_ble,
			read_ble_version, read_ble_battery, set_ble_time,
			bt_sedentary_open, bt_sedentary_close, btn_sync_rate,
			btn_rate_start, btn_rate_stop, unit, push_message_content
			,open_camera,close_camera,sync_skip;
	
	private Button today_sports_time,seven_days_sports_time,universal_interface,settings_bracelet_interface_set,query_bracelet_model,query_customer_ID;
	private Button query_currently_sport,set_currently_sport,sync_currently_sport,query_data;
	private DataProcessing mDataProcessing;
	private CustomProgressDialog mProgressDialog;
	private UTESQLOperate mySQLOperate;
	// private PedometerUtils mPedometerUtils;
	private WriteCommandToBLE mWriteCommand;
	private Context mContext;
	private SharedPreferences sp;
	private Editor editor;

	private final int UPDATE_STEP_UI_MSG = 0;
	private final int UPDATE_SLEEP_UI_MSG = 1;
	private final int DISCONNECT_MSG = 18;
	private final int CONNECTED_MSG = 19;
	private final int UPDATA_REAL_RATE_MSG = 20;
	private final int RATE_SYNC_FINISH_MSG = 21;
	private final int OPEN_CHANNEL_OK_MSG = 22;
	private final int CLOSE_CHANNEL_OK_MSG = 23;
	private final int TEST_CHANNEL_OK_MSG = 24;
	private final int OFFLINE_SWIM_SYNC_OK_MSG = 25;
	private final int UPDATA_REAL_BLOOD_PRESSURE_MSG = 29;
	private final int OFFLINE_BLOOD_PRESSURE_SYNC_OK_MSG = 30;
	private final int SERVER_CALL_BACK_OK_MSG = 31;
	private final int OFFLINE_SKIP_SYNC_OK_MSG = 32;
	private final int test_mag1 = 35;
	private final int test_mag2 = 36;
	private final int OFFLINE_STEP_SYNC_OK_MSG = 37;
	private final int UPDATE_SPORTS_TIME_DETAILS_MSG = 38;
	
	private final int UNIVERSAL_INTERFACE_SDK_TO_BLE_SUCCESS_MSG = 39;//sdk发送数据到ble完成，并且校验成功，返回状态
	private final int UNIVERSAL_INTERFACE_SDK_TO_BLE_FAIL_MSG = 40;   //sdk发送数据到ble完成，但是校验失败，返回状态
	private final int UNIVERSAL_INTERFACE_BLE_TO_SDK_SUCCESS_MSG = 41;//ble发送数据到sdk完成，并且校验成功，返回数据
	private final int UNIVERSAL_INTERFACE_BLE_TO_SDK_FAIL_MSG = 42;   //ble发送数据到sdk完成，但是校验失败，返回状态
	
	private final int RATE_OF_24_HOUR_SYNC_FINISH_MSG = 43;
	 
	
	private final long TIME_OUT_SERVER = 10000;
	private final long TIME_OUT = 120000;
	private boolean isUpdateSuccess = false;
	private int mSteps = 0;
	private float mDistance = 0f;
	private float mCalories = 0,mRunCalories=0,mWalkCalories=0;
	private int mRunSteps, mRunDurationTime,mWalkSteps,mWalkDurationTime;
	private float mRunDistance,  mWalkDistance;
	private boolean isFirstOpenAPK = false;
	private int currentDay = 1;
	private int lastDay = 0;
	private String currentDayString = "20101202";
	private String lastDayString = "20101201";
	private static final int NEW_DAY_MSG = 3;
	protected static final String TAG = "MainActivity1";
	private Updates mUpdates;
	private BLEServiceOperate mBLEServiceOperate;
	private BluetoothLeService mBluetoothLeService;
	// caicai add for sdk
	public static final String EXTRAS_DEVICE_NAME = "device_name";
	public static final String EXTRAS_DEVICE_ADDRESS = "device_address";
	private final int CONNECTED = 1;
	private final int CONNECTING = 2;
	private final int DISCONNECTED = 3;
	private int CURRENT_STATUS = DISCONNECTED;

	private String mDeviceName;
	private String mDeviceAddress;

	private int tempRate = 70;
	private int tempStatus;
	private long mExitTime = 0;

	private Button test_channel;
	private StringBuilder resultBuilder = new StringBuilder();

	private TextView swim_time, swim_stroke_count, swim_calorie,
			tv_low_pressure, tv_high_pressure,skip_time, skip_count, skip_calorie;
	private Button btn_sync_swim, btn_sync_pressure, btn_start_pressure,
			btn_stop_pressure,rate_calibration,turn_wrist_calibration,set_band_language;

	private int high_pressure, low_pressure;
	private int tempBloodPressureStatus;
	private Button ibeacon_command;
	private Spinner setOrReadSpinner, ibeaconStatusSpinner;
	private List<String> ibeaconStatusSpinnerList = new ArrayList<String>();
	private List<String> SetOrReadSpinnerList = new ArrayList<String>();
	private ArrayAdapter<String> aibeaconStatusAdapter;
	private ArrayAdapter<String> setOrReadAdapter;
	private int ibeaconStatus = GlobalVariable.IBEACON_TYPE_UUID;
	private int ibeaconSetOrRead = GlobalVariable.IBEACON_SET;
	private int leftRightHand = GlobalVariable.LEFT_HAND_WEAR;
	private int dialType = GlobalVariable.SHOW_HORIZONTAL_SCREEN;

	public static final String CONNECTED_DEVICE_CHANNEL = "connected_device_channel";
	public static final String FILE_SAVED_CHANNEL = "file_saved_channel";
	public static final String PROXIMITY_WARNINGS_CHANNEL = "proximity_warnings_channel";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContext = getApplicationContext();
		sp = mContext.getSharedPreferences(GlobalVariable.SettingSP, 0);
		editor = sp.edit();
		mySQLOperate = UTESQLOperate.getInstance(mContext);// 2.2.1版本修改
		mBLEServiceOperate = BLEServiceOperate.getInstance(mContext);
		Log.d(TAG, "setServiceStatusCallback前 mBLEServiceOperate =" + mBLEServiceOperate);
		mBLEServiceOperate.setServiceStatusCallback(this);
		Log.d(TAG, "setServiceStatusCallback后 mBLEServiceOperate =" + mBLEServiceOperate);
		// 如果没在搜索界面提前实例BLEServiceOperate的话，下面这4行需要放到OnServiceStatuslt
		mBluetoothLeService = mBLEServiceOperate.getBleService();
		if (mBluetoothLeService != null) {
			mBluetoothLeService.setICallback(this);
			
			mBluetoothLeService.setRateCalibrationListener(this);//设置心率校准监听
			mBluetoothLeService.setTurnWristCalibrationListener(this);//设置翻腕校准监听
			
		}
		 
		mRegisterReceiver();
		mfindViewById();
		mWriteCommand = WriteCommandToBLE.getInstance(mContext);
		mUpdates = Updates.getInstance(mContext);
		mUpdates.setHandler(mHandler);// 获取升级操作信息
		mUpdates.registerBroadcastReceiver();
		mUpdates.setOnServerCallbackListener(this);
		Log.d(TAG, "MainActivity_onCreate   mUpdates  ="
				+ mUpdates);
		Intent intent = getIntent();
		mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
		mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

		mBLEServiceOperate.connect(mDeviceAddress);

		CURRENT_STATUS = CONNECTING;
		upDateTodaySwimData();
		upDateTodaySkipData();


		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//NRF升级用到
			DfuServiceInitiator.createDfuNotificationChannel(this);

			final NotificationChannel channel = new NotificationChannel(CONNECTED_DEVICE_CHANNEL, getString(R.string.channel_connected_devices_title), NotificationManager.IMPORTANCE_LOW);
			channel.setDescription(getString(R.string.channel_connected_devices_description));
			channel.setShowBadge(false);
			channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

			final NotificationChannel fileChannel = new NotificationChannel(FILE_SAVED_CHANNEL, getString(R.string.channel_files_title), NotificationManager.IMPORTANCE_LOW);
			fileChannel.setDescription(getString(R.string.channel_files_description));
			fileChannel.setShowBadge(false);
			fileChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

			final NotificationChannel proximityChannel = new NotificationChannel(PROXIMITY_WARNINGS_CHANNEL, getString(R.string.channel_proximity_warnings_title), NotificationManager.IMPORTANCE_LOW);
			proximityChannel.setDescription(getString(R.string.channel_proximity_warnings_description));
			proximityChannel.setShowBadge(false);
			proximityChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

			final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.createNotificationChannel(channel);
			notificationManager.createNotificationChannel(fileChannel);
			notificationManager.createNotificationChannel(proximityChannel);
		}

	}

	private void mRegisterReceiver() {
		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(GlobalVariable.READ_BATTERY_ACTION);
		mFilter.addAction(GlobalVariable.READ_BLE_VERSION_ACTION);
		registerReceiver(mReceiver, mFilter);
	}

	private void mfindViewById() {
		et_height = (EditText) findViewById(R.id.et_height);
		et_weight = (EditText) findViewById(R.id.et_weight);
		et_sedentary_period = (EditText) findViewById(R.id.et_sedentary_period);
		connect_status = (TextView) findViewById(R.id.connect_status);
		rssi_tv = (TextView) findViewById(R.id.rssi_tv);
		tv_steps = (TextView) findViewById(R.id.tv_steps);
		tv_distance = (TextView) findViewById(R.id.tv_distance);
		tv_calorie = (TextView) findViewById(R.id.tv_calorie);
		tv_sleep = (TextView) findViewById(R.id.tv_sleep);
		tv_deep = (TextView) findViewById(R.id.tv_deep);
		tv_light = (TextView) findViewById(R.id.tv_light);
		tv_awake = (TextView) findViewById(R.id.tv_awake);
		tv_rate = (TextView) findViewById(R.id.tv_rate);
		tv_lowest_rate = (TextView) findViewById(R.id.tv_lowest_rate);
		tv_verage_rate = (TextView) findViewById(R.id.tv_verage_rate);
		tv_highest_rate = (TextView) findViewById(R.id.tv_highest_rate);
		show_result = (TextView) findViewById(R.id.show_result);
		btn_confirm = (Button) findViewById(R.id.btn_confirm);
		bt_sedentary_open = (Button) findViewById(R.id.bt_sedentary_open);
		bt_sedentary_close = (Button) findViewById(R.id.bt_sedentary_close);
		btn_sync_step = (Button) findViewById(R.id.btn_sync_step);
		btn_sync_sleep = (Button) findViewById(R.id.btn_sync_sleep);
		btn_sync_rate = (Button) findViewById(R.id.btn_sync_rate);
		btn_rate_start = (Button) findViewById(R.id.btn_rate_start);
		btn_rate_stop = (Button) findViewById(R.id.btn_rate_stop);
		btn_confirm.setOnClickListener(this);
		bt_sedentary_open.setOnClickListener(this);
		bt_sedentary_close.setOnClickListener(this);
		btn_sync_step.setOnClickListener(this);
		btn_sync_sleep.setOnClickListener(this);
		btn_sync_rate.setOnClickListener(this);
		btn_rate_start.setOnClickListener(this);
		btn_rate_stop.setOnClickListener(this);
		read_ble_version = (Button) findViewById(R.id.read_ble_version);
		read_ble_version.setOnClickListener(this);
		read_ble_battery = (Button) findViewById(R.id.read_ble_battery);
		read_ble_battery.setOnClickListener(this);
		set_ble_time = (Button) findViewById(R.id.set_ble_time);
		set_ble_time.setOnClickListener(this);
		update_ble = (Button) findViewById(R.id.update_ble);
		update_ble.setOnClickListener(this);
		et_height.setText(sp.getString(GlobalVariable.PERSONAGE_HEIGHT, "175"));
		et_weight.setText(sp.getString(GlobalVariable.PERSONAGE_WEIGHT, "60"));

		mDataProcessing = DataProcessing.getInstance(mContext);
		mDataProcessing.setOnStepChangeListener(mOnStepChangeListener);
		mDataProcessing.setOnSleepChangeListener(mOnSleepChangeListener);
		mDataProcessing.setOnRateListener(mOnRateListener);
		mDataProcessing.setOnBloodPressureListener(mOnBloodPressureListener);

		Button open_alarm = (Button) findViewById(R.id.open_alarm);
		open_alarm.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mWriteCommand.sendToSetAlarmCommand(1, GlobalVariable.EVERYDAY,
						16, 25, true, 5);// 新增最后一个参数，振动次数//2.2.1版本修改
			}
		});
		Button close_alarm = (Button) findViewById(R.id.close_alarm);
		close_alarm.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 2.2.1版本修改
				mWriteCommand.sendToSetAlarmCommand(1, GlobalVariable.EVERYDAY,
						16, 23, false, 5);// 新增最后一个参数，振动次数
			}
		});

		Log.d(TAG, "main_mDataProcessing =" + mDataProcessing);

		unit = (Button) findViewById(R.id.unit);
		unit.setOnClickListener(this);
		test_channel = (Button) findViewById(R.id.test_channel);
		test_channel.setOnClickListener(this);
		push_message_content = (Button) findViewById(R.id.push_message_content);
		push_message_content.setOnClickListener(this);

		btn_sync_swim = (Button) findViewById(R.id.btn_sync_swim);
		btn_sync_swim.setOnClickListener(this);
		swim_time = (TextView) findViewById(R.id.swim_time);
		swim_stroke_count = (TextView) findViewById(R.id.swim_stroke_count);
		swim_calorie = (TextView) findViewById(R.id.swim_calorie);

		tv_low_pressure = (TextView) findViewById(R.id.tv_low_pressure);
		tv_high_pressure = (TextView) findViewById(R.id.tv_high_pressure);
		btn_sync_pressure = (Button) findViewById(R.id.btn_sync_pressure);
		btn_start_pressure = (Button) findViewById(R.id.btn_start_pressure);
		btn_stop_pressure = (Button) findViewById(R.id.btn_stop_pressure);

		btn_sync_pressure.setOnClickListener(this);
		btn_start_pressure.setOnClickListener(this);
		btn_stop_pressure.setOnClickListener(this);
		initIbeacon();
		open_camera=(Button) findViewById(R.id.open_camera);
		close_camera=(Button) findViewById(R.id.close_camera);
		open_camera.setOnClickListener(this);
		close_camera.setOnClickListener(this);
		
		skip_time = (TextView) findViewById(R.id.skip_time);
		skip_count = (TextView) findViewById(R.id.skip_count);
		skip_calorie = (TextView) findViewById(R.id.skip_calorie);
		sync_skip=(Button) findViewById(R.id.sync_skip);
		sync_skip.setOnClickListener(this);
		
		
		today_sports_time=(Button) findViewById(R.id.today_sports_time);
		today_sports_time.setOnClickListener(this);
		seven_days_sports_time=(Button) findViewById(R.id.seven_days_sports_time);
		seven_days_sports_time.setOnClickListener(this);
		
		universal_interface=(Button) findViewById(R.id.universal_interface);
		universal_interface.setOnClickListener(this);
		
		rate_calibration=(Button) findViewById(R.id.rate_calibration);
		rate_calibration.setOnClickListener(this);
		turn_wrist_calibration=(Button) findViewById(R.id.turn_wrist_calibration);
		turn_wrist_calibration.setOnClickListener(this);
		set_band_language=(Button) findViewById(R.id.set_band_language);
		set_band_language.setOnClickListener(this);
        settings_bracelet_interface_set=(Button) findViewById(R.id.settings_bracelet_interface_set);
        settings_bracelet_interface_set.setOnClickListener(this);
        query_bracelet_model=(Button) findViewById(R.id.query_bracelet_model);
        query_bracelet_model.setOnClickListener(this);
        query_customer_ID=(Button) findViewById(R.id.query_customer_ID);
        query_customer_ID.setOnClickListener(this);
        query_currently_sport=(Button) findViewById(R.id.query_currently_sport);
        query_currently_sport.setOnClickListener(this);
        set_currently_sport=(Button) findViewById(R.id.set_currently_sport);
        set_currently_sport.setOnClickListener(this);
        sync_currently_sport=(Button) findViewById(R.id.sync_currently_sport);
        sync_currently_sport.setOnClickListener(this);
        query_data=(Button) findViewById(R.id.query_data);
        query_data.setOnClickListener(this);

	}

	/**
	 * 计步监听 在这里更新UI
	 */
	private StepChangeListener mOnStepChangeListener = new StepChangeListener() {
		@Override
		public void onStepChange(StepOneDayAllInfo info) {
			if (info!=null) {
				mSteps = info.getStep();
				mDistance = info.getDistance();
				mCalories = info.getCalories();
				
				mRunSteps	= info.getRunSteps();
				mRunCalories= info.getRunCalories();
				mRunDistance= info.getRunDistance();
				mRunDurationTime= info.getRunDurationTime();
				
				mWalkSteps= info.getWalkSteps();
				mWalkCalories= info.getWalkCalories();
				mWalkDistance= info.getWalkDistance();
				mWalkDurationTime= info.getWalkDurationTime();
				 
			}
			Log.d(TAG, "mSteps =" + mSteps + ",mDistance ="
					+ mDistance + ",mCalories =" + mCalories + ",mRunSteps ="
					+ mRunSteps + ",mRunCalories =" + mRunCalories
					+ ",mRunDistance =" + mRunDistance + ",mRunDurationTime ="
					+ mRunDurationTime + ",mWalkSteps =" + mWalkSteps
					+ ",mWalkCalories =" + mWalkCalories + ",mWalkDistance ="
					+ mWalkDistance + ",mWalkDurationTime ="
					+ mWalkDurationTime);
			
			mHandler.sendEmptyMessage(UPDATE_STEP_UI_MSG);
	
		}
	};
	/**
	 * 睡眠监听 在这里更新UI
	 */
	private SleepChangeListener mOnSleepChangeListener = new SleepChangeListener() {

		@Override
		public void onSleepChange() {
			mHandler.sendEmptyMessage(UPDATE_SLEEP_UI_MSG);
		}

	};

	private RateChangeListener mOnRateListener = new RateChangeListener() {

		@Override
		public void onRateChange(int rate, int status) {
			tempRate = rate;
			tempStatus = status;
			Log.i(TAG, "Rate_tempRate =" + tempRate);
			mHandler.sendEmptyMessage(UPDATA_REAL_RATE_MSG);
		}
	};
	private BloodPressureChangeListener mOnBloodPressureListener = new BloodPressureChangeListener() {

		@Override
		public void onBloodPressureChange(int hightPressure, int lowPressure,
				int status) {
			tempBloodPressureStatus = status;
			high_pressure = hightPressure;
			low_pressure = lowPressure;
			mHandler.sendEmptyMessage(UPDATA_REAL_BLOOD_PRESSURE_MSG);
		}
	};
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case RATE_SYNC_FINISH_MSG:
				UpdateUpdataRateMainUI(CalendarUtils.getCalendar(0));
				Toast.makeText(mContext, "Rate sync finish", Toast.LENGTH_SHORT).show();
				break;
			case RATE_OF_24_HOUR_SYNC_FINISH_MSG:
				Toast.makeText(mContext, "24 Hour Rate sync finish", Toast.LENGTH_SHORT).show();
				break;
			case UPDATA_REAL_RATE_MSG:
				tv_rate.setText(tempRate + "");// 实时跳变
				if (tempStatus == GlobalVariable.RATE_TEST_FINISH) {
					UpdateUpdataRateMainUI(CalendarUtils.getCalendar(0));
					Toast.makeText(mContext, "Rate test finish", Toast.LENGTH_SHORT).show();
				}
				break;
			case GlobalVariable.GET_RSSI_MSG:
				Bundle bundle = msg.getData();
				rssi_tv.setText(bundle.getInt(GlobalVariable.EXTRA_RSSI) + "");
				break;
			case UPDATE_STEP_UI_MSG:
				updateSteps(mSteps);
				updateCalories(mCalories);
				updateDistance(mDistance);

				Log.d(TAG, "mSteps =" + mSteps + ",mDistance ="
						+ mDistance + ",mCalories =" + mCalories);
				break;
			case UPDATE_SLEEP_UI_MSG:
				querySleepInfo();
				Log.d(TAG, "UPDATE_SLEEP_UI_MSG");
				break;
			case NEW_DAY_MSG:
//				mySQLOperate.updateStepSQL();//2.5.2版本删除
				// mySQLOperate.updateSleepSQL();//2.2.1版本删除
				mySQLOperate.updateRateSQL();
//				mySQLOperate.isDeleteRefreshTable();//2.5.2版本删除
				resetValues();
				break;
			case GlobalVariable.START_PROGRESS_MSG:
				Log.i(TAG, "(Boolean) msg.obj=" + (Boolean) msg.obj);
				isUpdateSuccess = (Boolean) msg.obj;
				Log.i(TAG, "BisUpdateSuccess=" + isUpdateSuccess);
				startProgressDialog();
				mHandler.postDelayed(mDialogRunnable, TIME_OUT);
				break;
			case GlobalVariable.DOWNLOAD_IMG_FAIL_MSG:
				Toast.makeText(MainActivity.this, R.string.download_fail, Toast.LENGTH_LONG)
						.show();
				if (mProgressDialog != null) {
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}
				if (mDialogRunnable != null)
					mHandler.removeCallbacks(mDialogRunnable);
				break;
			case GlobalVariable.DISMISS_UPDATE_BLE_DIALOG_MSG:
				Log.i(TAG, "(Boolean) msg.obj=" + (Boolean) msg.obj);
				isUpdateSuccess = (Boolean) msg.obj;
				Log.i(TAG, "BisUpdateSuccess=" + isUpdateSuccess);
				if (mProgressDialog != null) {
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}
				if (mDialogRunnable != null) {
					mHandler.removeCallbacks(mDialogRunnable);
				}

				if (isUpdateSuccess) {
					Toast.makeText(
							mContext,
							getResources().getString(
									R.string.ble_update_successful), Toast.LENGTH_SHORT).show();
				}
				break;
			case GlobalVariable.SERVER_IS_BUSY_MSG:
				Toast.makeText(mContext,
						getResources().getString(R.string.server_is_busy), Toast.LENGTH_SHORT)
						.show();
				break;
			case DISCONNECT_MSG:
				connect_status.setText(getString(R.string.disconnect));
				CURRENT_STATUS = DISCONNECTED;
				Toast.makeText(mContext, "disconnect or connect falie", Toast.LENGTH_SHORT)
						.show();

				String lastConnectAddr0 = sp.getString(
						GlobalVariable.LAST_CONNECT_DEVICE_ADDRESS_SP,
						"00:00:00:00:00:00");
				boolean connectResute0 = mBLEServiceOperate
						.connect(lastConnectAddr0);
				Log.i(TAG, "connectResute0=" + connectResute0);

				break;
			case CONNECTED_MSG:
				connect_status.setText(getString(R.string.connected));
				mBluetoothLeService.setRssiHandler(mHandler);
				new Thread(new Runnable() {
					@Override
					public void run() {
						while (!Thread.interrupted()) {
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							if (mBluetoothLeService != null) {
								mBluetoothLeService.readRssi();
							}
						}
					}
				}).start();
				CURRENT_STATUS = CONNECTED;
				Toast.makeText(mContext, "connected", Toast.LENGTH_SHORT).show();
				break;

			case GlobalVariable.UPDATE_BLE_PROGRESS_MSG: // (新) 增加固件升级进度
				int schedule = msg.arg1;
				Log.i("zznkey", "schedule =" + schedule);
				if (mProgressDialog == null) {
					startProgressDialog();
				}
				mProgressDialog.setSchedule(schedule);
				break;
			case OPEN_CHANNEL_OK_MSG:// 打开通道OK
				test_channel.setText(getResources().getString(
						R.string.open_channel_ok));
				resultBuilder.append(getResources().getString(
						R.string.open_channel_ok)
						+ ",");
				show_result.setText(resultBuilder.toString());

				mWriteCommand.sendAPDUToBLE(WriteCommandToBLE
						.hexString2Bytes(testKey1));
				break;
			case CLOSE_CHANNEL_OK_MSG:// 关闭通道OK
				test_channel.setText(getResources().getString(
						R.string.close_channel_ok));
				resultBuilder.append(getResources().getString(
						R.string.close_channel_ok)
						+ ",");
				show_result.setText(resultBuilder.toString());
				break;
			case TEST_CHANNEL_OK_MSG:// 通道测试OK
				test_channel.setText(getResources().getString(
						R.string.test_channel_ok));
				resultBuilder.append(getResources().getString(
						R.string.test_channel_ok)
						+ ",");
				show_result.setText(resultBuilder.toString());
				mWriteCommand.closeBLEchannel();
				break;

			case SHOW_SET_PASSWORD_MSG:
				showPasswordDialog(GlobalVariable.PASSWORD_TYPE_SET);
				break;
			case SHOW_INPUT_PASSWORD_MSG:
				showPasswordDialog(GlobalVariable.PASSWORD_TYPE_INPUT);
				break;
			case SHOW_INPUT_PASSWORD_AGAIN_MSG:
				showPasswordDialog(GlobalVariable.PASSWORD_TYPE_INPUT_AGAIN);
				break;
			case OFFLINE_SWIM_SYNC_OK_MSG:
				upDateTodaySwimData();
				show_result.setText(mContext.getResources().getString(
						R.string.sync_swim_finish));
				Toast.makeText(MainActivity.this,
						getResources().getString(R.string.sync_swim_finish), Toast.LENGTH_SHORT)
						.show();
				break;

			case UPDATA_REAL_BLOOD_PRESSURE_MSG:
				tv_low_pressure.setText(low_pressure + "");// 实时跳变
				tv_high_pressure.setText(high_pressure + "");// 实时跳变
				if (tempBloodPressureStatus == GlobalVariable.BLOOD_PRESSURE_TEST_FINISH) {
					UpdateBloodPressureMainUI(CalendarUtils.getCalendar(0));
					Toast.makeText(
							mContext,
							getResources().getString(R.string.test_pressure_ok),
							Toast.LENGTH_SHORT).show();
				}
				break;
			case OFFLINE_BLOOD_PRESSURE_SYNC_OK_MSG:
				UpdateBloodPressureMainUI(CalendarUtils.getCalendar(0));
				Toast.makeText(MainActivity.this,
						getResources().getString(R.string.sync_pressure_ok), Toast.LENGTH_SHORT)
						.show();
				break;
			case SERVER_CALL_BACK_OK_MSG:
				if (mProgressDialog != null) {
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}
				if (mDialogServerRunnable != null) {
					mHandler.removeCallbacks(mDialogServerRunnable);
				}
				String localVersion = sp.getString(
						GlobalVariable.IMG_LOCAL_VERSION_NAME_SP, "0");
				int status = mUpdates.getBLEVersionStatus(localVersion);
				Log.i(TAG, "固件升级 VersionStatus =" + status);
				if (status == GlobalVariable.OLD_VERSION_STATUS) {
					updateBleDialog();// update remind
				} else if (status == GlobalVariable.NEWEST_VERSION_STATUS) {
					Toast.makeText(mContext,
							getResources().getString(R.string.ble_is_newest), Toast.LENGTH_SHORT)
							.show();
				}/*
				 * else if (status == GlobalVariable.FREQUENT_ACCESS_STATUS) {
				 * Toast.makeText( mContext, getResources().getString(
				 * R.string.frequent_access_server), 0) .show(); }
				 */
				break;
			case OFFLINE_SKIP_SYNC_OK_MSG:
				upDateTodaySkipData();
				Toast.makeText(MainActivity.this,
						getResources().getString(R.string.sync_skip_finish), Toast.LENGTH_SHORT)
						.show();
				show_result.setText(mContext.getResources().getString(
						R.string.sync_skip_finish));
				break;
			case test_mag1:
				Toast.makeText(MainActivity.this,"表示按键1短按下，用来做切换屏,表示切换了手环屏幕", Toast.LENGTH_SHORT)//
						.show();
				show_result.setText("表示按键1短按下，用来做切换屏,表示切换了手环屏幕");
				break;
			case test_mag2:
				Toast.makeText(MainActivity.this,"表示按键3短按下，用来做一键SOS", Toast.LENGTH_SHORT)
				.show();
				show_result.setText("表示按键3短按下，用来做一键SOS");
				break;
			case OFFLINE_STEP_SYNC_OK_MSG:
				Toast.makeText(MainActivity.this,"计步数据同步成功", Toast.LENGTH_SHORT)
				.show();
				show_result.setText("计步数据同步成功");
				break;
			case UPDATE_SPORTS_TIME_DETAILS_MSG:
				show_result.setText(resultBuilder.toString());
				break;
			case UNIVERSAL_INTERFACE_SDK_TO_BLE_SUCCESS_MSG:
				show_result.setText("sdk发送数据到ble完成，并且校验成功，返回状态");
				break;
			case UNIVERSAL_INTERFACE_SDK_TO_BLE_FAIL_MSG:
				show_result.setText("sdk发送数据到ble完成，但是校验失败，返回状态");
				break;
			case UNIVERSAL_INTERFACE_BLE_TO_SDK_SUCCESS_MSG:
				show_result.setText("ble发送数据到sdk完成，并且校验成功，返回数据");
				break;
			case UNIVERSAL_INTERFACE_BLE_TO_SDK_FAIL_MSG:
				show_result.setText("ble发送数据到sdk完成，但是校验失败，返回状态");
				break;
 
			default:
				break;
			}
		}
	};

	/*
	 * 获取一天最新心率值、最高、最低、平均心率值
	 */
	private void UpdateUpdataRateMainUI(String calendar) {
		// UTESQLOperate mySQLOperate = UTESQLOperate.getInstance(mContext);
		RateOneDayInfo mRateOneDayInfo = mySQLOperate
				.queryRateOneDayMainInfo(calendar);
		if (mRateOneDayInfo != null) {
			int currentRate = mRateOneDayInfo.getCurrentRate();
			int lowestValue = mRateOneDayInfo.getLowestRate();
			int averageValue = mRateOneDayInfo.getVerageRate();
			int highestValue = mRateOneDayInfo.getHighestRate();
			// current_rate.setText(currentRate + "");
			if (currentRate == 0) {
				tv_rate.setText("--");
			} else {
				tv_rate.setText(currentRate + "");
			}
			if (lowestValue == 0) {
				tv_lowest_rate.setText("--");
			} else {
				tv_lowest_rate.setText(lowestValue + "");
			}
			if (averageValue == 0) {
				tv_verage_rate.setText("--");
			} else {
				tv_verage_rate.setText(averageValue + "");
			}
			if (highestValue == 0) {
				tv_highest_rate.setText("--");
			} else {
				tv_highest_rate.setText(highestValue + "");
			}
		} else {
			tv_rate.setText("--");
		}
	}

	/*
	 * 获取一天各测试时间点和心率值
	 */
	private void getOneDayRateinfo(String calendar) {
		// UTESQLOperate mySQLOperate = UTESQLOperate.getInstance(mContext);
		List<RateOneDayInfo> mRateOneDayInfoList = mySQLOperate
				.queryRateOneDayDetailInfo(calendar);
		if (mRateOneDayInfoList != null && mRateOneDayInfoList.size() > 0) {
			int size = mRateOneDayInfoList.size();
			int[] rateValue = new int[size];
			int[] timeArray = new int[size];
			for (int i = 0; i < size; i++) {
				rateValue[i] = mRateOneDayInfoList.get(i).getRate();
				timeArray[i] = mRateOneDayInfoList.get(i).getTime();
				Log.d(TAG, "rateValue[" + i + "]=" + rateValue[i]
						+ "timeArray[" + i + "]=" + timeArray[i]);
			}
		} else {

		}
	}

	private void startProgressDialog() {
		if (mProgressDialog == null) {
			mProgressDialog = CustomProgressDialog
					.createDialog(MainActivity.this);
			mProgressDialog.setMessage(getResources().getString(
					R.string.ble_updating));
			mProgressDialog.setCancelable(false);
			mProgressDialog.setCanceledOnTouchOutside(false);
		}
		mProgressDialog.show();
	}

	private Runnable mDialogRunnable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			// mDownloadButton.setText(R.string.suota_update_succeed);
			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
				mProgressDialog = null;
			}
			mHandler.removeCallbacks(mDialogRunnable);
			if (!isUpdateSuccess) {
				Toast.makeText(MainActivity.this,
						getResources().getString(R.string.ble_fail_update), Toast.LENGTH_SHORT)
						.show();
				mUpdates.clearUpdateSetting();
			} else {
				isUpdateSuccess = false;
				Toast.makeText(
						MainActivity.this,
						getResources()
								.getString(R.string.ble_update_successful), Toast.LENGTH_SHORT)
						.show();
			}

		}
	};
	private Runnable mDialogServerRunnable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			// mDownloadButton.setText(R.string.suota_update_succeed);
			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
				mProgressDialog = null;
			}
			mHandler.removeCallbacks(mDialogServerRunnable);
			Toast.makeText(MainActivity.this,
					getResources().getString(R.string.server_is_busy), Toast.LENGTH_SHORT)
					.show();
		}
	};
	private void updateSteps(int steps) {
		Log.d(TAG,"steps =" + steps);
		String stepString = "0";
		if (steps <= 0) {
			} else {
				stepString = "" + steps;
			}
		
		tv_steps.setText(stepString);

	}
 

	private void updateCalories(float mCalories) {
		if (mCalories <= 0) {
			tv_calorie.setText(mContext.getResources().getString(
					R.string.zero_kilocalorie));
		} else {
			tv_calorie.setText("" +  mCalories + " "
					+ mContext.getResources().getString(R.string.kilocalorie));
		}

	}

	private void updateDistance(float mDistance) {
		if (mDistance < 0.01) {
			tv_distance.setText(mContext.getResources().getString(
					R.string.zero_kilometers));

		} else{
			tv_distance.setText(mDistance+ " "
					+ mContext.getResources().getString(R.string.kilometers));
		}
//		else if (mDistance >= 100) {
//			tv_distance.setText(("" + mDistance).substring(0, 3) + " "
//					+ mContext.getResources().getString(R.string.kilometers));
//		} else {
//			tv_distance.setText(("" + (mDistance + 0.000001f)).substring(0, 4)
//					+ " "
//					+ mContext.getResources().getString(R.string.kilometers));
//		}
		
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		boolean ble_connecte = sp.getBoolean(GlobalVariable.BLE_CONNECTED_SP,
				false);
		if (ble_connecte) {
			connect_status.setText(getString(R.string.connected));
		} else {
			connect_status.setText(getString(R.string.disconnect));
		}
		JudgeNewDayWhenResume();

	}

	private void JudgeNewDayWhenResume() {
		isFirstOpenAPK = sp.getBoolean(GlobalVariable.FIRST_OPEN_APK, true);
		editor.putBoolean(GlobalVariable.FIRST_OPEN_APK, false);
		editor.commit();
		lastDay = sp.getInt(GlobalVariable.LAST_DAY_NUMBER_SP, 0);
		lastDayString = sp.getString(GlobalVariable.LAST_DAY_CALLENDAR_SP,
				"20101201");
		Calendar c = Calendar.getInstance();
		currentDay = c.get(Calendar.DAY_OF_YEAR);
		currentDayString = CalendarUtils.getCalendar(0);

		if (isFirstOpenAPK) {
			lastDay = currentDay;
			lastDayString = currentDayString;
			editor = sp.edit();
			editor.putInt(GlobalVariable.LAST_DAY_NUMBER_SP, lastDay);
			editor.putString(GlobalVariable.LAST_DAY_CALLENDAR_SP,
					lastDayString);
			editor.commit();
		} else {

			if (currentDay != lastDay) {
				if ((lastDay + 1) == currentDay || currentDay == 1) { // 连续的日期
					mHandler.sendEmptyMessage(NEW_DAY_MSG);
				} else {
//					mySQLOperate.insertLastDayStepSQL(lastDayString);//2.5.2版本删除
					// mySQLOperate.updateSleepSQL();//2.2.1版本删除
					resetValues();
				}
				lastDay = currentDay;
				lastDayString = currentDayString;
				editor.putInt(GlobalVariable.LAST_DAY_NUMBER_SP, lastDay);
				editor.putString(GlobalVariable.LAST_DAY_CALLENDAR_SP,
						lastDayString);
				editor.commit();
			} else {
				Log.d(TAG, "currentDay == lastDay");
			}
		}

	}

	private void resetValues() {
		editor.putInt(GlobalVariable.YC_PED_UNFINISH_HOUR_STEP_SP, 0);
		editor.putInt(GlobalVariable.YC_PED_UNFINISH_HOUR_VALUE_SP, 0);
		editor.putInt(GlobalVariable.YC_PED_LAST_HOUR_STEP_SP, 0);
		editor.commit();
		tv_steps.setText("0");
		tv_calorie.setText(mContext.getResources().getString(
				R.string.zero_kilocalorie));
		tv_distance.setText(mContext.getResources().getString(
				R.string.zero_kilometers));
		tv_sleep.setText("0");
		tv_deep.setText(mContext.getResources().getString(
				R.string.zero_hour_zero_minute));
		tv_light.setText(mContext.getResources().getString(
				R.string.zero_hour_zero_minute));
		tv_awake.setText(mContext.getResources().getString(R.string.zero_count));

		tv_rate.setText("--");
		tv_lowest_rate.setText("--");
		tv_verage_rate.setText("--");
		tv_highest_rate.setText("--");
	}

	@Override
	public void onClick(View v) {
		boolean ble_connecte = sp.getBoolean(GlobalVariable.BLE_CONNECTED_SP,
				false);
		switch (v.getId()) {
		case R.id.btn_confirm:
			
			if (ble_connecte) {
				String height = et_height.getText().toString();
				String weight = et_weight.getText().toString();
				if (height.equals("") || weight.equals("")) {
					Toast.makeText(mContext, "身高或体重不能为空", Toast.LENGTH_SHORT).show();
				} else {

					int Height = Integer.valueOf(height);
					int Weight = Integer.valueOf(weight);
					mWriteCommand.sendStepLenAndWeightToBLE(Height, Weight, 5,
							10000, true, true, 150,true,20,false,true,50,GlobalVariable.TMP_UNIT_CELSIUS,true);
//                    int height, int weight,int offScreenTime,int stepTask,
//                    boolean isRraisHandbrightScreenSwitchOpen,boolean isHighestRateOpen,
//                    int highestRate,boolean isMale,int age,boolean bandLostOpen
//                    ,boolean isLowestRateOpen,int lowestRate,int celsiusFahrenheitValue,boolean isChinese
					// 设置步长，体重，灭屏时间5s,目标步数10000，抬手亮屏开关true为开，false为关；最高心率提醒，true为开，false为关；
					//设置最高心率提醒的值；性别true为男，false为女；20为年龄（范围0~255）；手环防丢功能，true为开启，false为关闭;
//                    最低心率提醒  true 打开，false 为关闭；最低心率设置范围40-100，默认50
//                  int celsiusFahrenheitValue可设置为摄氏度GlobalVariable.TMP_UNIT_CELSIUS或华氏度GlobalVariable.TMP_UNIT_FAHRENHEIT
//                   boolean isChinese true 中文，false 英文
				}
			} else {
				Toast.makeText(mContext, getString(R.string.disconnect),
						Toast.LENGTH_SHORT).show();
			}
//			List<RateOneDayInfo> mRateOneDayInfoList = new ArrayList<RateOneDayInfo>();
//			mRateOneDayInfoList =mySQLOperate.queryRateOneDayDetailInfo(CalendarUtils.getCalendar(0));
//			Log.d(TAG, "mRateOneDayInfoList ="+mRateOneDayInfoList);
//			if (mRateOneDayInfoList!=null) {
//				for (int i = 0; i < mRateOneDayInfoList.size(); i++) {
//					int time = mRateOneDayInfoList.get(i).getTime();
//					int rate = mRateOneDayInfoList.get(i).getRate();
//					Log.d(TAG, "mRateOneDayInfoList time ="+time+",rate ="+rate);
//				}
//			}else {
//				
//			}
//			RateOneDayInfo mRateOneDayInfo = null;
//			mRateOneDayInfo =mySQLOperate.queryRateOneDayMainInfo(CalendarUtils.getCalendar(0));
//			if (mRateOneDayInfo!=null) {
//				 int lowestRate;
//				 int verageRate;
//				 int highestRate;
//				 int currentRate;
//			}
//			List<StepOneDayAllInfo> list = mySQLOperate.queryRunWalkAllDay();
//			if (list != null) {
//				for (int i = 0; i < list.size(); i++) {
//					String calendar = list.get(i).getCalendar();
//					int step = list.get(i).getStep();
//					int runSteps = list.get(i).getRunSteps();
//					int walkSteps = list.get(i).getWalkSteps();
//					Log.d(TAG, "queryRunWalkAllDay calendar =" + calendar
//							+ ",step =" + step + ",runSteps =" + runSteps
//							+ ",walkSteps =" + walkSteps);
//				}
//			}
			break;
		case R.id.bt_sedentary_open:
			String period = et_sedentary_period.getText().toString();
			if (period.equals("")) {
				Toast.makeText(mContext, "Please input remind peroid", Toast.LENGTH_SHORT)
						.show();
			} else {
				int period_time = Integer.valueOf(period);
				if (period_time < 30) {
					Toast.makeText(
							mContext,
							"Please make sure period_time more than 30 minutes",
							Toast.LENGTH_SHORT).show();
				} else {
					if (ble_connecte) {
//						mWriteCommand.sendSedentaryRemindCommand(
//								GlobalVariable.OPEN_SEDENTARY_REMIND,
//								period_time);
                        int fromTimeHour = 10;//开始时段的小时
                        int fromTimeMinute = 59;//开始时段的分钟
                        int toTimeHour = 16;//结束时段的小时
                        int toTimeMinute = 50;//结束时段的分钟
                        boolean lunchBreak = true;//午休免打扰 true为12:00-14:00 久坐提醒功能不提醒,false 为12:00-14:00 久坐提醒功能依然提醒
                        mWriteCommand.sendSedentaryRemindCommand(
                                GlobalVariable.OPEN_SEDENTARY_REMIND,
                                period_time, fromTimeHour, fromTimeMinute, toTimeHour, toTimeMinute, lunchBreak);
					} else {
						Toast.makeText(mContext,
								getString(R.string.disconnect),
								Toast.LENGTH_SHORT).show();
					}
				}
			}
			
//			StepOneDayAllInfo mStepInfo =mySQLOperate.queryRunWalkInfo("20171106");
//			if (mStepInfo != null) {
//				String calendar ="";
//				 int step = mStepInfo.getStep();
//				 int mCaloriesValue = mStepInfo.getCalories();
//				 float distance = mStepInfo.getDistance();
//				// 跑步
//				int runSteps = mStepInfo.getRunSteps();
//				int runCalories = mStepInfo.getRunCalories();
//				float runDistance = mStepInfo.getRunDistance();
//				int runDurationTime = mStepInfo.getRunDurationTime();
//				String runHourDetails = mStepInfo.getRunHourDetails();
//				// 走路
//				 int walkSteps = mStepInfo.getWalkSteps();
//				 int walkCalories = mStepInfo.getWalkCalories();
//				 float walkDistance = mStepInfo.getWalkDistance();
//				 int walkDurationTime = mStepInfo.getWalkDurationTime();
//				 String walkHourDetails = mStepInfo.getWalkHourDetails();
//				int totalSteps = runSteps + walkSteps;
//				
//				
//				Log.d(TAG, "queryRunWalkInfo calendar ="+calendar+",step ="+step+",mCaloriesValue ="+mCaloriesValue+",distance ="+distance);
//				Log.d(TAG, "queryRunWalkInfo runSteps ="+runSteps+",runCalories ="+runCalories+",runDistance ="+runDistance+",runDurationTime ="+runDurationTime);
//				Log.d(TAG, "queryRunWalkInfo walkSteps ="+walkSteps+",walkCalories ="+walkCalories+",walkDistance ="+walkDistance+",walkDurationTime ="+walkDurationTime);
//			}
			break;
		case R.id.bt_sedentary_close:
			if (ble_connecte) {
//				mWriteCommand.sendSedentaryRemindCommand(
//						GlobalVariable.CLOSE_SEDENTARY_REMIND, 0);
                int fromTimeHour = 10;//开始时段的小时
                int fromTimeMinute = 59;//开始时段的分钟
                int toTimeHour = 16;//结束时段的小时
                int toTimeMinute = 50;//结束时段的分钟
                boolean lunchBreak = true;//午休免打扰 true为12:00-14:00 久坐提醒功能不提醒,false 为12:00-14:00 久坐提醒功能依然提醒
                mWriteCommand.sendSedentaryRemindCommand(
                        GlobalVariable.CLOSE_SEDENTARY_REMIND,
                        0, fromTimeHour, fromTimeMinute, toTimeHour, toTimeMinute, lunchBreak);
			} else {
				Toast.makeText(mContext, getString(R.string.disconnect),
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.btn_sync_step:
			if (ble_connecte) {
				mWriteCommand.syncAllStepData();
				
//				mWriteCommand.syncAllSwimData();
//				mWriteCommand.syncAllSkipData();
//				mySQLOperate.querySkipDayInfo("20170629");
//				mySQLOperate.querySwimDayInfo("20170629");
//				mySQLOperate.queryRunWalkInfo("20170629");
			} else {
				Toast.makeText(mContext, getString(R.string.disconnect),
						Toast.LENGTH_SHORT).show();
			}

			break;
		case R.id.btn_sync_sleep:
			if (ble_connecte) {
				mWriteCommand.syncAllSleepData();
//				mWriteCommand.syncAllSportsModeData();
				// mWriteCommand.syncWeatherToBLE(mContext, "桂林市"); //测试天气接口
				// mWriteCommand.syncWeatherToBLE(mContext, "深圳市");
//				mWriteCommand.queryDialMode();//测试查询表盘切换方式
			} else {
				Toast.makeText(mContext, getString(R.string.disconnect),
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.btn_sync_rate:
//			mySQLOperate.queryBallSports(GlobalVariable.BALL_TYPE_TABLETENNIS);
			if (ble_connecte) {
				mWriteCommand.syncAllRateData();
				
			} else {
				Toast.makeText(mContext, getString(R.string.disconnect),
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.btn_rate_start:
//			 List<BallSportsInfo> info =mySQLOperate.queryBallSportsDayInfo(GlobalVariable.BALL_TYPE_TABLETENNIS,"20180625");
//			 if (info!=null) {
//				 for (int i = 0; i < info.size(); i++) {
//						String calendar =info.get(i).getCalendar();
//						int ca =info.get(i).getCalories();
//						Log.d(TAG, "查询出来的 calendar ="+calendar+",ca ="+ca);
//					}
//			}
			
			if (ble_connecte) {
				mWriteCommand
						.sendRateTestCommand(GlobalVariable.RATE_TEST_START);
			} else {
				Toast.makeText(mContext, getString(R.string.disconnect),
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.btn_rate_stop:
			if (ble_connecte) {
				mWriteCommand
						.sendRateTestCommand(GlobalVariable.RATE_TEST_STOP);
			} else {
				Toast.makeText(mContext, getString(R.string.disconnect),
						Toast.LENGTH_SHORT).show();
			}
			break;

		case R.id.read_ble_version:
			if (ble_connecte) {
				mWriteCommand.sendToReadBLEVersion(); // 发送请求BLE版本号
			} else {
				Toast.makeText(mContext, getString(R.string.disconnect),
						Toast.LENGTH_SHORT).show();
			}
			
		
			break;
		case R.id.read_ble_battery:
//			StepOneDayAllInfo allInfo = mySQLOperate
//					.queryRunWalkInfo("20170724");
//			Log.d(TAG, "allInfo =" + allInfo);
//			if (allInfo != null) {
//				// 走路跑步不区分
//				int steps = allInfo.getStep();
//				int calories = allInfo.getCalories();
//				float distance = allInfo.getDistance();
//				// 跑步
//				int runSteps = allInfo.getRunSteps();
//				int runCalories = allInfo.getRunCalories();
//				float runDistance = allInfo.getRunDistance();
//				int runDurationTime = allInfo.getRunDurationTime();
//				// 走路
//				int walkSteps = allInfo.getWalkSteps();
//				int walkCalories = allInfo.getWalkCalories();
//				float walkDistance = allInfo.getWalkDistance();
//				int walkDurationTime = allInfo.getWalkDurationTime();
//				Log.d(TAG, " steps =" + steps + ",calories =" + calories
//						+ ",distance" + distance);
//				Log.d(TAG, " runSteps =" + runSteps + ",runCalories ="
//						+ runCalories + ",runDistance" + runDistance
//						+ ",runDurationTime=" + runDurationTime);
//				Log.d(TAG, " walkSteps =" + walkSteps + ",walkCalories ="
//						+ walkCalories + ",walkDistance" + walkDistance
//						+ ",walkDurationTime=" + walkDurationTime);
//				int hourStep = 0;
//				int time = 0;
//				int startTime =0;
//				int endTime =0;
//				int useTime =0;
//				ArrayList<StepOneHourInfo> hourInfos = allInfo
//						.getStepOneHourArrayInfo();
//				for (int i = 0; i < hourInfos.size(); i++) {
//					time = hourInfos.get(i).getTime();
//					hourStep = hourInfos.get(i).getStep();
//					Log.d(TAG, "走路跑步不区分 time =" + time + ",hourStep ="
//							+ hourStep);
//				}
//				ArrayList<StepRunHourInfo> hourRunInfos = allInfo
//						.getStepRunHourArrayInfo();
//				for (int i = 0; i < hourRunInfos.size(); i++) {
//					time = hourRunInfos.get(i).getTime();
//					hourStep = hourRunInfos.get(i).getRunSteps();
//					startTime =hourRunInfos.get(i).getStartRunTime();
//					endTime =hourRunInfos.get(i).getEndRunTime();
//					useTime =hourRunInfos.get(i).getRunDurationTime();
//					Log.d(TAG, " 跑步 time =" + time + ",hourStep =" + hourStep+ ",startTime =" + startTime+ ",endTime =" + endTime+ ",useTime =" + useTime);
//
//				}
//				ArrayList<StepWalkHourInfo> hourWalkInfos = allInfo
//						.getStepWalkHourArrayInfo();
//				for (int i = 0; i < hourWalkInfos.size(); i++) {
//					time = hourWalkInfos.get(i).getTime();
//					hourStep = hourWalkInfos.get(i).getWalkSteps();
//					startTime =hourWalkInfos.get(i).getStartWalkTime();
//					endTime =hourWalkInfos.get(i).getEndWalkTime();
//					useTime =hourWalkInfos.get(i).getWalkDurationTime();
//					Log.d(TAG, " 走路 time =" + time + ",hourStep =" + hourStep+ ",startTime =" + startTime+ ",endTime =" + endTime+ ",useTime =" + useTime);
//
//				}
//			} else {
//
//			}
			
			if (ble_connecte) {
				mWriteCommand.sendToReadBLEBattery();// 请求获取电量指令
			} else {
				Toast.makeText(mContext, getString(R.string.disconnect),
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.set_ble_time:
			if (ble_connecte) {
//				 mWriteCommand.sendDisturbToBle(false, false, true, 20, 12,
//				 8, 20);
				mWriteCommand.syncBLETime();
			} else {
				Toast.makeText(mContext, getString(R.string.disconnect),
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.update_ble:

            new RxPermissions(MainActivity.this).request(Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(new Action1<Boolean>() {
                @Override
                public void call(Boolean granted) {
                    if (granted) {
                        if (sp.getBoolean(GlobalVariable.BLE_CONNECTED_SP, false)) {
                            mWriteCommand.queryDeviceFearture();
                            if (isNetworkAvailable(mContext)) {
                                String localVersion = sp.getString(
                                        GlobalVariable.IMG_LOCAL_VERSION_NAME_SP, "0");
                                if (!localVersion.equals("0")) {
                                    int status = mUpdates
                                            .accessServerersionStatus(localVersion);
                                    if (status == GlobalVariable.FREQUENT_ACCESS_STATUS) {
                                        Toast.makeText(
                                                mContext,
                                                getResources().getString(
                                                        R.string.frequent_access_server), Toast.LENGTH_SHORT)
                                                .show();
                                    } else {
                                        startProgressDialog();
                                        mHandler.postDelayed(mDialogServerRunnable,
                                                TIME_OUT_SERVER);
                                    }

                                    // int status = mUpdates
                                    // .accessServerersionStatus(localVersion);
                                    // Log.d(TAG, "固件升级 VersionStatus =" + status);
                                    // if (status == GlobalVariable.OLD_VERSION_STATUS) {
                                    // updateBleDialog();// update remind
                                    // } else if (status ==
                                    // GlobalVariable.NEWEST_VERSION_STATUS) {
                                    // Toast.makeText(
                                    // mContext,
                                    // getResources().getString(
                                    // R.string.ble_is_newest), 0).show();
                                    // } else if (status ==
                                    // GlobalVariable.FREQUENT_ACCESS_STATUS) {
                                    // Toast.makeText(
                                    // mContext,
                                    // getResources().getString(
                                    // R.string.frequent_access_server), 0)
                                    // .show();
                                    // }
                                } else {
                                    Toast.makeText(
                                            mContext,
                                            getResources().getString(
                                                    R.string.read_ble_version_first), Toast.LENGTH_SHORT)
                                            .show();
                                }
                            } else {
                                Toast.makeText(
                                        mContext,
                                        getResources().getString(
                                                R.string.confire_is_network_available), Toast.LENGTH_SHORT)
                                        .show();

                            }
                        } else {
                            Toast.makeText(
                                    mContext,
                                    getResources().getString(
                                            R.string.please_connect_bracelet), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
			break;
		// case 11:
		// mWriteCommand.sendToSetAlarmCommand(1, (byte) 33, 12, 22, true);
		// break;

		case R.id.unit:
			boolean ble_connected3 = sp.getBoolean(
					GlobalVariable.BLE_CONNECTED_SP, false);
			if (ble_connected3) {
				if (unit.getText()
						.toString()
						.equals(getResources()
								.getString(R.string.metric_system))) {
					editor.putBoolean(GlobalVariable.IS_METRIC_UNIT_SP, true);
					editor.commit();
					mWriteCommand.sendUnitAndHourFormatToBLE();
					unit.setText(getResources().getString(R.string.inch_system));
				} else {
					editor.putBoolean(GlobalVariable.IS_METRIC_UNIT_SP, false);
					editor.commit();
					mWriteCommand.sendUnitAndHourFormatToBLE();//
					// mWriteCommand.sendUnitAndHourFormatToBLE(unitType,
					// hourFormat);//也可以传如参数设置
					// unitType == GlobalVariable.UNIT_TYPE_METRICE 公制单位
					// unitType == GlobalVariable.UNIT_TYPE_IMPERIAL 英制单位
					// hourFormat == GlobalVariable.HOUR_FORMAT_24 24小时制
					// hourFormat == GlobalVariable.HOUR_FORMAT_12 12小时制
					unit.setText(getResources().getString(
							R.string.metric_system));
				}
			} else {
				Toast.makeText(
						mContext,
						getResources().getString(
								R.string.please_connect_bracelet), Toast.LENGTH_SHORT).show();
			}

			break;
		case R.id.test_channel:
			boolean ble_connected4 = sp.getBoolean(
					GlobalVariable.BLE_CONNECTED_SP, false);
			if (ble_connected4) {
				if (test_channel
						.getText()
						.toString()
						.equals(getResources().getString(R.string.test_channel))
						|| test_channel
								.getText()
								.toString()
								.equals(getResources().getString(
										R.string.test_channel_ok))
						|| test_channel
								.getText()
								.toString()
								.equals(getResources().getString(
										R.string.close_channel_ok))) {
					resultBuilder = new StringBuilder();
					mWriteCommand.openBLEchannel();
				} else {
					Toast.makeText(
							mContext,
							getResources().getString(R.string.channel_testting),
							Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(
						mContext,
						getResources().getString(
								R.string.please_connect_bracelet), Toast.LENGTH_SHORT).show();
			}

			break;
		case R.id.push_message_content:
			if (ble_connecte) {
				String pushContent = getResources().getString(
						R.string.push_message_content);// 推送的内容
//                pushContent ="Schauen Sie sich bitte die Studierenden an, die sich online für Wissenschaft interessieren.";
//                pushContent ="온라인으로 과학에 관심이있는 학생들을 살펴보십시오.";
//                Random random =new Random();//测试
//                int type =random.nextInt(24);
//                Log.d(TAG,"推送类型 type ="+type+",moreForeign ="+moreForeign);
//                mWriteCommand.sendTextToBle(pushContent, type);
                boolean moreForeign = GetFunctionList.isSupportFunction_Third(getApplicationContext(),
                        GlobalVariable.IS_SUPPORT_MORE_FOREIGN_APP);
                if (moreForeign){//需求判断，成立则支持以下注释的24种消息类型的推送
                    mWriteCommand.sendTextToBle(pushContent, GlobalVariable.TYPE_QQ);
                    // mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_WECHAT);
                    // editor.putString(GlobalVariable.SMS_RECEIVED_NUMBER,
                    // "18045811234");//保存推送短信的号码,短信推送时，必须
                    // editor.commit();
                    // mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_SMS);
                    // mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_PHONE);
//                     mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_OTHERS);//不区分类别
//                     mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_FACEBOOK);
//                     mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_TWITTER);
//                     mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_WHATSAPP);
//                     mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_SKYPE);
//                     mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_FACEBOOK_MESSENGER);
//                     mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_HANGOUTS);
//                     mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_LINE);
//                     mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_LINKEDIN);
//                     mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_INSTAGRAM);
//                     mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_VIBER);
//                     mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_KAKAO_TALK);
//                     mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_VKONTAKTE);
//                     mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_SNAPCHAT);
//                     mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_GOOGLE_PLUS);
//                     mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_GMAIL);
//                     mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_FLICKR);
//                     mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_TUMBLR);
//                     mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_PINTEREST);
//                     mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_YOUTUBE);

                }else {//不成立，则支持以下注释的5种消息类型的推送
                    mWriteCommand.sendTextToBle(pushContent, GlobalVariable.TYPE_QQ);
                    // mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_WECHAT);
                    // editor.putString(GlobalVariable.SMS_RECEIVED_NUMBER,
                    // "18045811234");//保存推送短信的号码,短信推送时，必须
                    // editor.commit();
                    // mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_SMS);
                    // mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_PHONE);
//                    mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_OTHERS);//不区分类别
                }


				show_result.setText(pushContent);
			} else {
				Toast.makeText(mContext, getString(R.string.disconnect),
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.btn_sync_swim:
			if (ble_connecte) {
				if (GetFunctionList.isSupportFunction(mContext, GlobalVariable.IS_SUPPORT_SWIMMING)) {
					mWriteCommand.syncAllSwimData();
					show_result.setText(mContext.getResources().getString(
							R.string.sync_swim));
				}else {
					Toast.makeText(mContext, getString(R.string.not_support_swim),
							Toast.LENGTH_SHORT).show();
				}
				
			} else {
				Toast.makeText(mContext, getString(R.string.disconnect),
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.btn_sync_pressure:
			if (ble_connecte) {
				mWriteCommand.syncAllBloodPressureData();
				show_result.setText(mContext.getResources().getString(
						R.string.sync_pressure));
			} else {
				Toast.makeText(mContext, getString(R.string.disconnect),
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.btn_start_pressure:
			if (ble_connecte) {
				mWriteCommand
						.sendBloodPressureTestCommand(GlobalVariable.BLOOD_PRESSURE_TEST_START);
				show_result.setText(mContext.getResources().getString(
						R.string.start_pressure));
			} else {
				Toast.makeText(mContext, getString(R.string.disconnect),
						Toast.LENGTH_SHORT).show();
			}
		 

			break;
		case R.id.btn_stop_pressure:
			if (ble_connecte) {
				mWriteCommand
						.sendBloodPressureTestCommand(GlobalVariable.BLOOD_PRESSURE_TEST_STOP);
				show_result.setText(mContext.getResources().getString(
						R.string.stop_pressure));
			} else {
				Toast.makeText(mContext, getString(R.string.disconnect),
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.ibeacon_command:
			if (ble_connecte) {
				if (GetFunctionList.isSupportFunction(mContext,
						GlobalVariable.IS_SUPPORT_IBEACON)) {// 先判断是否支持ibeacon功能
					switch (ibeaconSetOrRead) {
					case GlobalVariable.IBEACON_SET:// 设置
						switch (ibeaconStatus) {
						case GlobalVariable.IBEACON_TYPE_UUID:
							// 注意：在ibeacon
							// 中，UUID的数据长度固定为16byte的ASIIC,，如30313233343536373031323334353637
							mWriteCommand.sendIbeaconSetCommand(
									"30313233343536373031323334353637",
									GlobalVariable.IBEACON_TYPE_UUID);// 设置UUID
							break;
						case GlobalVariable.IBEACON_TYPE_MAJOR:
							// //major和minor固定长度为2byte的数字，如0224
							mWriteCommand.sendIbeaconSetCommand("0224",
									GlobalVariable.IBEACON_TYPE_MAJOR);// 设置major
							break;
						case GlobalVariable.IBEACON_TYPE_MINOR:
							// //major和minor固定长度为2byte的数字，如0424
							mWriteCommand.sendIbeaconSetCommand("3424",
									GlobalVariable.IBEACON_TYPE_MINOR);// 设置minor
							break;
						case GlobalVariable.IBEACON_TYPE_DEVICE_NAME:
							// //Device
							// name的长度范必须大于0小于14byte的ASIIC，如3031323334353637303132333435
							mWriteCommand.sendIbeaconSetCommand(
									"3031323334353637303132333435",
									GlobalVariable.IBEACON_TYPE_DEVICE_NAME);// 设置蓝牙device
																				// name
							break;
						case GlobalVariable.IBEACON_TYPE_TX_POWER:
							// TX_POWER（数据范围 1~0xfe，由客户设置)；
							mWriteCommand.sendIbeaconSetCommand("78",GlobalVariable.IBEACON_TYPE_TX_POWER);// 设置TX_POWER
							break;
						case GlobalVariable.IBEACON_TYPE_ADVERTISING_INTERVAL:
							// advertising interval（数据范围1~20，单位为100ms，默认800ms每次）
							mWriteCommand.sendIbeaconSetCommand("14",GlobalVariable.IBEACON_TYPE_ADVERTISING_INTERVAL);// 设置advertising interval
							break;
						default:
							break;
						}
						break;
					case GlobalVariable.IBEACON_GET:// 获取
						switch (ibeaconStatus) {
						case GlobalVariable.IBEACON_TYPE_UUID:
							// //获取UUID
							mWriteCommand
									.sendIbeaconGetCommand(GlobalVariable.IBEACON_TYPE_UUID);
							break;
						case GlobalVariable.IBEACON_TYPE_MAJOR:
							// //获取major
							mWriteCommand
									.sendIbeaconGetCommand(GlobalVariable.IBEACON_TYPE_MAJOR);
							break;
						case GlobalVariable.IBEACON_TYPE_MINOR:
							// 获取minor
							mWriteCommand
									.sendIbeaconGetCommand(GlobalVariable.IBEACON_TYPE_MINOR);
							break;
						case GlobalVariable.IBEACON_TYPE_DEVICE_NAME:
							// //获取device name
							mWriteCommand
									.sendIbeaconGetCommand(GlobalVariable.IBEACON_TYPE_DEVICE_NAME);
							break;
						case GlobalVariable.IBEACON_TYPE_TX_POWER:
							// //获取TX_POWER
							mWriteCommand
							.sendIbeaconGetCommand(GlobalVariable.IBEACON_TYPE_TX_POWER);
							break;
						case GlobalVariable.IBEACON_TYPE_ADVERTISING_INTERVAL:
							// //获取advertising interval
							mWriteCommand
							.sendIbeaconGetCommand(GlobalVariable.IBEACON_TYPE_ADVERTISING_INTERVAL);
							break;
						default:
							break;
						}
						break;

					default:
						break;
					}
				} else {
					Toast.makeText(mContext, "不支持ibeacon功能", Toast.LENGTH_SHORT)
							.show();
				}

			} else {
				Toast.makeText(mContext, getString(R.string.disconnect),
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.open_camera:
			if (ble_connecte) {
				mWriteCommand.NotifyBLECameraOpenOrNot(true);
			}else {
				Toast.makeText(mContext, getString(R.string.disconnect),
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.close_camera:
			if (ble_connecte) {
				mWriteCommand.NotifyBLECameraOpenOrNot(false);
			}else {
				Toast.makeText(mContext, getString(R.string.disconnect),
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.sync_skip:
			if (ble_connecte) {
				if (GetFunctionList.isSupportFunction(mContext, GlobalVariable.IS_SUPPORT_SKIP)) {
					mWriteCommand.syncAllSkipData();
					show_result.setText(mContext.getResources().getString(
							R.string.sync_skip));
				}else {
					Toast.makeText(mContext, getString(R.string.not_support_skip),
							Toast.LENGTH_SHORT).show();
				}
				
			}else {
				Toast.makeText(mContext, getString(R.string.disconnect),
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.today_sports_time:
			if (ble_connecte) {
				resultBuilder = new StringBuilder();
				resultBuilder.append(getString(R.string.today_sports_time)+":");
				mWriteCommand.sendKeyToGetSportsTime(GlobalVariable.SPORTS_TIME_TODAY);
			} else {
				Toast.makeText(mContext, getString(R.string.disconnect),
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.seven_days_sports_time:
			if (ble_connecte) {
				resultBuilder = new StringBuilder();
				resultBuilder.append(getString(R.string.seven_days_sports_time)+":");
				mWriteCommand.sendKeyToGetSportsTime(GlobalVariable.SPORTS_TIME_HISTORY_DAY);
			} else {
				Toast.makeText(mContext, getString(R.string.disconnect),
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.universal_interface:
			if (ble_connecte) {
				mWriteCommand.universalInterface(WriteCommandToBLE
						.hexString2Bytes(universalKey));
			} else {
				Toast.makeText(mContext, getString(R.string.disconnect),
						Toast.LENGTH_SHORT).show();
			}
			break;	
		case R.id.rate_calibration:
			if (ble_connecte) {
				if (GetFunctionList.isSupportFunction_Second(mContext,GlobalVariable.IS_SUPPORT_HEART_RATE_DETECTION_CALIBRATION)) {
					mWriteCommand.startRateCalibration();
				}else {
					Toast.makeText(mContext, "不支持心率校准功能",
							Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(mContext, getString(R.string.disconnect),
						Toast.LENGTH_SHORT).show();
			}
			break;	
		case R.id.turn_wrist_calibration:
			if (ble_connecte) {
				if (GetFunctionList.isSupportFunction_Second(mContext,GlobalVariable.IS_SUPPORT_TURN_WRIST_CALIBRATION)) {
					mWriteCommand.startTurnWristCalibration();
				}else {
					Toast.makeText(mContext, "不支持翻腕校准功能",
							Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(mContext, getString(R.string.disconnect),
						Toast.LENGTH_SHORT).show();
			}
			break;	
		case R.id.set_band_language:
			if (ble_connecte) {
				if (GetFunctionList.isSupportFunction_Second(mContext,GlobalVariable.IS_SUPPORT_BAND_LANGUAGE_FUNCTION)) {
//                    BandLanguageUtil.BAND_LANGUAGE_SYSTEM //跟随手机系统语言
//                    BandLanguageUtil.BAND_LANGUAGE_CN//中文简体
//                    BandLanguageUtil.BAND_LANGUAGE_EN//英语
//                    BandLanguageUtil.BAND_LANGUAGE_KO//韩语
//                    BandLanguageUtil.BAND_LANGUAGE_JA//日语
//                    BandLanguageUtil.BAND_LANGUAGE_DE//德语
//                    BandLanguageUtil.BAND_LANGUAGE_ES//西班牙语
//                    BandLanguageUtil.BAND_LANGUAGE_FR//法语
//                    BandLanguageUtil.BAND_LANGUAGE_IT//意大利语
//                    BandLanguageUtil.BAND_LANGUAGE_PT//葡萄牙语
//                    BandLanguageUtil.BAND_LANGUAGE_AR//阿拉伯语
//                    BandLanguageUtil.BAND_LANGUAGE_RU//俄语
//                    BandLanguageUtil.BAND_LANGUAGE_NL//印地语
					mWriteCommand.syncBandLanguage(BandLanguageUtil.BAND_LANGUAGE_SYSTEM);
				}else {
					Toast.makeText(mContext, "不支持手环语言设置功能",
							Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(mContext, getString(R.string.disconnect),
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.settings_bracelet_interface_set:
            if (ble_connecte) {
                boolean bandInterface = GetFunctionList.isSupportFunction_Second(mContext, GlobalVariable.IS_SUPPORT_CUSTOMIZED_BRACELET_INTERFACE);
                Log.i(TAG, "手环界面客制 bandInterface = " + bandInterface);
                if (bandInterface) {
                    startActivity(new Intent(mContext, BandInterfaceSetActivity.class));
                } else {
                    Toast.makeText(mContext, "不支持手环界面设置功能",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(mContext, getString(R.string.disconnect),
                        Toast.LENGTH_SHORT).show();
            }
			break;
		case R.id.query_bracelet_model:
            String BTName = mWriteCommand.getBTName();
            if (BTName != null) {
                show_result.setText(BTName);
            } else {
                Toast.makeText(mContext, getString(R.string.get_version_first),
                        Toast.LENGTH_SHORT).show();
            }
			break;
		case R.id.query_customer_ID:
            if (ble_connecte) {
                boolean isSupportCustomerID = GetFunctionList.isSupportFunction_Third(mContext, GlobalVariable.IS_SUPPORT_QUERY_CUSTOMER_ID);
                Log.i(TAG, "获取客户ID isSupportCustomerID = " + isSupportCustomerID);
                if (isSupportCustomerID) {//在onResult回调
                    mWriteCommand.queryCustomerID();
                } else {
                    Toast.makeText(mContext, "不支持获取客户ID功能",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(mContext, getString(R.string.disconnect),
                        Toast.LENGTH_SHORT).show();
            }
			break;
		case R.id.query_currently_sport:
            if (ble_connecte) {
                boolean isSupport = GetFunctionList.isSupportFunction_Third(mContext, GlobalVariable.IS_SUPPORT_MULTIPLE_SPORTS_MODES_HEART_RATE);
                Log.i(TAG, "多运动心率 isSupport = " + isSupport);
                if (isSupport) {//在onResult回调
                    mWriteCommand.queryCurrentlySportOpened();
                } else {
                    Toast.makeText(mContext, "手环不支持多运动心率功能",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(mContext, getString(R.string.disconnect),
                        Toast.LENGTH_SHORT).show();
            }
			break;
		case R.id.set_currently_sport:
            if (ble_connecte) {
                boolean isSupport = GetFunctionList.isSupportFunction_Third(mContext, GlobalVariable.IS_SUPPORT_MULTIPLE_SPORTS_MODES_HEART_RATE);
                Log.i(TAG, "多运动心率 isSupport = " + isSupport);
                if (isSupport) {//在onResult回调
                    Random random =new Random();
                    boolean isOpen = random.nextBoolean();
                    int sportType = random.nextInt(0X18);

                    int N =1;//每隔N个10秒（1byte，范围1~255，默认N=1即10s保存一次数据，用户可以设置选项10s，20s，30s、1分钟、2分钟、3分钟，4分钟、5分钟）。
                    Log.i(TAG, "多运动心率 isOpen = " + isOpen+",sportType ="+sportType+",N ="+N);
                    mWriteCommand.setMultipleSportsModes(isOpen,sportType,N);
                } else {
                    Toast.makeText(mContext, "手环不支持多运动心率功能",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(mContext, getString(R.string.disconnect),
                        Toast.LENGTH_SHORT).show();
            }
			break;
		case R.id.sync_currently_sport:
            if (ble_connecte) {
                boolean isSupport = GetFunctionList.isSupportFunction_Third(mContext, GlobalVariable.IS_SUPPORT_MULTIPLE_SPORTS_MODES_HEART_RATE);
                Log.i(TAG, "多运动心率 isSupport = " + isSupport);
                if (isSupport) {//在onResult回调
                    String calendar ="201903090819";//calendar 如201903090819 表示同步2019年03月09日08点19分之后的数据，字符串长度必须为12
                    mWriteCommand.syncMultipleSportsModes(calendar);
                } else {
                    Toast.makeText(mContext, "手环不支持多运动心率功能",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(mContext, getString(R.string.disconnect),
                        Toast.LENGTH_SHORT).show();
            }
			break;
		case R.id.query_data:
            int sportsModes = new Random().nextInt(30);
            String calendar = CalendarUtils.getCalendar(-new Random().nextInt(5));
            List<SportsModesInfo> list = UTESQLOperate.getInstance(mContext).querySportsModes(null);
            MultipleSportsModesUtils.LLogI("saveSportsModesData 查询 list.size() =" + list.size());

            for (int i = 0; i < list.size(); i++) {
                SportsModesInfo info = list.get(i);
                MultipleSportsModesUtils.LLogI("saveSportsModesData 查询 SportsModes =" + info.getCurrentSportsModes() + ",StartDateTime =" + info.getStartDateTime() + ",BleTimeInterval = " + info.getBleTimeInterval());
                byte[] a = GBUtils.getInstance(mContext).hexStringToBytes(info.getBleAllRate());
//                for (int j = 0; j < a.length; j++) {
//                    MultipleSportsModesUtils.LLogI("心率值 =" + (a[j] & 0xFF));
//                }
            }
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (CURRENT_STATUS == CONNECTING) {
			AlertDialog.Builder builder = new Builder(this);
			builder.setMessage("设备连接中，强制退出将关闭蓝牙，确认吗？");
			builder.setTitle(mContext.getResources().getString(R.string.tip));
			builder.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
									.getDefaultAdapter();
							if (mBluetoothAdapter == null) {
								finish();
							}
							if (mBluetoothAdapter.isEnabled()) {
								mBluetoothAdapter.disable();// 关闭蓝牙
							}
							finish();
						}
					});
			builder.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			builder.create().show();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private boolean updateBleDialog() {

		final AlertDialog alert = new AlertDialog.Builder(this).setCancelable(
				false).create();
		alert.show();
		window = alert.getWindow();
		window.setContentView(R.layout.update_dialog_layout);
		Button btn_yes = (Button) window.findViewById(R.id.btn_yes);
		Button btn_no = (Button) window.findViewById(R.id.btn_no);
		TextView update_warn_tv = (TextView) window
				.findViewById(R.id.update_warn_tv);
		update_warn_tv.setText(getResources().getString(
				R.string.find_new_version_ble));

		btn_yes.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isNetworkAvailable(mContext)) {
					mUpdates.startUpdateBLE();
				} else {
					Toast.makeText(
							mContext,
							getResources().getString(
									R.string.confire_is_network_available), Toast.LENGTH_SHORT)
							.show();
				}

				alert.dismiss();
			}
		});
		btn_no.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mUpdates.clearUpdateSetting();
				alert.dismiss();
			}

		});
		return false;

	}

	/**
	 * 获取某一天睡眠详细，并更新睡眠UI CalendarUtils.getCalendar(0)代表今天，也可写成"20141101"
	 * CalendarUtils.getCalendar(-1)代表昨天，也可写成"20141031"
	 * CalendarUtils.getCalendar(-2)代表前天，也可写成"20141030" 以此类推
	 */
	private void querySleepInfo() {
		SleepTimeInfo sleepTimeInfo = mySQLOperate.querySleepInfo(CalendarUtils
				.getCalendar(0));
		int deepTime, lightTime, awakeCount, sleepTotalTime;
		if (sleepTimeInfo != null) {
			deepTime = sleepTimeInfo.getDeepTime();
			lightTime = sleepTimeInfo.getLightTime();
			awakeCount = sleepTimeInfo.getAwakeCount();
			sleepTotalTime = sleepTimeInfo.getSleepTotalTime();

			int[] colorArray = sleepTimeInfo.getSleepStatueArray();// 绘图中不同睡眠状态可用不同颜色表示，颜色自定义
			int[] timeArray = sleepTimeInfo.getDurationTimeArray();
			int[] timePointArray = sleepTimeInfo.getTimePointArray();

			Log.d(TAG, "Calendar=" + CalendarUtils.getCalendar(0)
					+ ",timeArray =" + timeArray + ",timeArray.length ="
					+ timeArray.length + ",colorArray =" + colorArray
					+ ",colorArray.length =" + colorArray.length
					+ ",timePointArray =" + timePointArray
					+ ",timePointArray.length =" + timePointArray.length);

			double total_hour = ((float) sleepTotalTime / 60f);
			DecimalFormat df1 = new DecimalFormat("0.0"); // 保留1位小数，带前导零
			int deep_hour = deepTime / 60;
			int deep_minute = (deepTime - deep_hour * 60);
			int light_hour = lightTime / 60;
			int light_minute = (lightTime - light_hour * 60);
			int active_count = awakeCount;
			String total_hour_str = df1.format(total_hour);

			if (total_hour_str.equals("0.0")) {
				total_hour_str = "0";
			}
			tv_sleep.setText(total_hour_str);
			tv_deep.setText(deep_hour + " "
					+ mContext.getResources().getString(R.string.hour) + " "
					+ deep_minute + " "
					+ mContext.getResources().getString(R.string.minute));
			tv_light.setText(light_hour + " "
					+ mContext.getResources().getString(R.string.hour) + " "
					+ light_minute + " "
					+ mContext.getResources().getString(R.string.minute));
			tv_awake.setText(active_count + " "
					+ mContext.getResources().getString(R.string.count));
		} else {
			Log.d(TAG, "sleepTimeInfo =" + sleepTimeInfo);
			tv_sleep.setText("0");
			tv_deep.setText(mContext.getResources().getString(
					R.string.zero_hour_zero_minute));
			tv_light.setText(mContext.getResources().getString(
					R.string.zero_hour_zero_minute));
			tv_awake.setText(mContext.getResources().getString(
					R.string.zero_count));
		}
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(GlobalVariable.READ_BLE_VERSION_ACTION)) {
				String version = intent
						.getStringExtra(GlobalVariable.INTENT_BLE_VERSION_EXTRA);
				if (sp.getBoolean(BluetoothLeService.IS_RK_PLATFORM_SP, false)) {
					show_result.setText("version="
							+ version
							+ ","
							+ sp.getString(
									GlobalVariable.PATH_LOCAL_VERSION_NAME_SP,
									""));
				} else {
					show_result.setText("version=" + version);
				}

			} else if (action.equals(GlobalVariable.READ_BATTERY_ACTION)) {
				int battery = intent.getIntExtra(
						GlobalVariable.INTENT_BLE_BATTERY_EXTRA, -1);
				show_result.setText("battery=" + battery);

			}
		}
	};
	private Window window;

	private boolean isNetworkAvailable(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm == null) {
		} else {
			NetworkInfo[] info = cm.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "MainActivity_onDestroy");
		GlobalVariable.BLE_UPDATE = false;
		mUpdates.unRegisterBroadcastReceiver();
		try {
			unregisterReceiver(mReceiver);
		} catch (Exception e) {
			// TODO: handle exception
		}

		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
		if (mDialogRunnable != null)
			mHandler.removeCallbacks(mDialogRunnable);

		mBLEServiceOperate.disConnect();
	}

	@Override
	public void OnResult(boolean result, int status) {
		// TODO Auto-generated method stub
		Log.i(TAG, "result=" + result + ",status=" + status);
		switch (status) {
		case ICallbackStatus.OFFLINE_STEP_SYNC_OK:
			mHandler.sendEmptyMessage(OFFLINE_STEP_SYNC_OK_MSG);
			break;
		case ICallbackStatus.OFFLINE_SLEEP_SYNC_OK:
			break;
		case ICallbackStatus.SYNC_TIME_OK:// (时间在同步在SDK内部已经帮忙同步，你不需要同步时间了，sdk内部同步时间完成会自动回调到这里)
			                               //同步时间成功后，会回调到这里，延迟20毫秒，获取固件版本
			// delay 20ms  send	
			// to read
			// localBleVersion
			// mWriteCommand.sendToReadBLEVersion();
			break;
		case ICallbackStatus.GET_BLE_VERSION_OK:// 获取固件版本成功后会回调到这里，延迟20毫秒，设置身高体重到手环
			// localBleVersion
			// finish,
			// then sync
			// step
			// mWriteCommand.syncAllStepData();
			break;
		case ICallbackStatus.DISCONNECT_STATUS: 
			mHandler.sendEmptyMessage(DISCONNECT_MSG);
			break;
		case ICallbackStatus.CONNECTED_STATUS: 
			mHandler.sendEmptyMessage(CONNECTED_MSG);
			mHandler.postDelayed(new Runnable() {

				@Override
				public void run() {
					mWriteCommand.sendToQueryPasswardStatus();
				}
			}, 600);// 2.2.1版本修改
		
			break;

		case ICallbackStatus.DISCOVERY_DEVICE_SHAKE: 
			Log.d(TAG, "摇一摇拍照");
			// Discovery device Shake
			break;
		case ICallbackStatus.OFFLINE_RATE_SYNC_OK: 
			mHandler.sendEmptyMessage(RATE_SYNC_FINISH_MSG);
			break;
		case ICallbackStatus.OFFLINE_24_HOUR_RATE_SYNC_OK: 
			mHandler.sendEmptyMessage(RATE_OF_24_HOUR_SYNC_FINISH_MSG);
			break;
		case ICallbackStatus.SET_METRICE_OK: // 设置公制单位成功
			break;
		case ICallbackStatus.SET_INCH_OK: //// 设置英制单位成功
			break;
		case ICallbackStatus.SET_FIRST_ALARM_CLOCK_OK: // 设置第1个闹钟OK
			break;
		case ICallbackStatus.SET_SECOND_ALARM_CLOCK_OK: //设置第2个闹钟OK
			break;
		case ICallbackStatus.SET_THIRD_ALARM_CLOCK_OK: // 设置第3个闹钟OK
			break;
		case ICallbackStatus.SEND_PHONE_NAME_NUMBER_OK:
			mWriteCommand.sendQQWeChatVibrationCommand(5);
			break;
		case ICallbackStatus.SEND_QQ_WHAT_SMS_CONTENT_OK:
			mWriteCommand.sendQQWeChatVibrationCommand(1);
			break;
		case ICallbackStatus.PASSWORD_SET:
			Log.d(TAG, "没设置过密码，请设置4位数字密码");
			mHandler.sendEmptyMessage(SHOW_SET_PASSWORD_MSG);
			break;
		case ICallbackStatus.PASSWORD_INPUT:
			Log.d(TAG, "已设置过密码，请输入已设置的4位数字密码");
			mHandler.sendEmptyMessage(SHOW_INPUT_PASSWORD_MSG);
			break;
		case ICallbackStatus.PASSWORD_AUTHENTICATION_OK:
			Log.d(TAG, "验证成功或者设置密码成功");
			break;
		case ICallbackStatus.PASSWORD_INPUT_AGAIN:
			Log.d(TAG, "验证失败或者设置密码失败，请重新输入4位数字密码，如果已设置过密码，请输入已设置的密码");
			mHandler.sendEmptyMessage(SHOW_INPUT_PASSWORD_AGAIN_MSG);
			break;
		case ICallbackStatus.OFFLINE_SWIM_SYNCING:
			Log.d(TAG, "游泳数据同步中");
			break;
		case ICallbackStatus.OFFLINE_SWIM_SYNC_OK:
			Log.d(TAG, "游泳数据同步完成");
			mHandler.sendEmptyMessage(OFFLINE_SWIM_SYNC_OK_MSG);
			break;
		case ICallbackStatus.OFFLINE_BLOOD_PRESSURE_SYNCING:
			Log.d(TAG, "血压数据同步中");
			break;
		case ICallbackStatus.OFFLINE_BLOOD_PRESSURE_SYNC_OK:
			Log.d(TAG, "血压数据同步完成");
			mHandler.sendEmptyMessage(OFFLINE_BLOOD_PRESSURE_SYNC_OK_MSG);
			break;
		case ICallbackStatus.OFFLINE_SKIP_SYNCING:
			Log.d(TAG, "跳绳数据同步中");
			break;
		case ICallbackStatus.OFFLINE_SKIP_SYNC_OK:
			Log.d(TAG, "跳绳数据同步完成");
			mHandler.sendEmptyMessage(OFFLINE_SKIP_SYNC_OK_MSG);
			break;
		case ICallbackStatus.MUSIC_PLAYER_START_OR_STOP:
			Log.d(TAG, "音乐播放/暂停");
			break;
		case ICallbackStatus.MUSIC_PLAYER_NEXT_SONG:
			Log.d(TAG, "音乐下一首");
			break;
		case ICallbackStatus.MUSIC_PLAYER_LAST_SONG:
			Log.d(TAG, "音乐上一首");
			break;
		case ICallbackStatus.OPEN_CAMERA_OK:
			Log.d(TAG, "打开相机ok");
			break;
		case ICallbackStatus.CLOSE_CAMERA_OK:
			Log.d(TAG, "关闭相机ok");
			break;
		case ICallbackStatus.PRESS_SWITCH_SCREEN_BUTTON:
			Log.d(TAG, "表示按键1短按下，用来做切换屏,表示切换了手环屏幕");
			mHandler.sendEmptyMessage(test_mag1);
			break;
		case ICallbackStatus.PRESS_END_CALL_BUTTON:
			Log.d(TAG, "表示按键1长按下，一键拒接来电");
			break;
		case ICallbackStatus.PRESS_TAKE_PICTURE_BUTTON:
			Log.d(TAG, "表示按键2短按下，用来做一键拍照");
			break;
		case ICallbackStatus.PRESS_SOS_BUTTON:
			Log.d(TAG, "表示按键3短按下，用来做一键SOS");
			mHandler.sendEmptyMessage(test_mag2);
			break;
		case ICallbackStatus.PRESS_FIND_PHONE_BUTTON:
			Log.d(TAG, "表示按键按下，手环查找手机的功能。");
			
			break;
		case ICallbackStatus.READ_ONCE_AIR_PRESSURE_TEMPERATURE_SUCCESS:
			Log.d(TAG, "读取当前气压传感器气压值和温度值成功，数据已保存到数据库，查询请调用查询数据库接口，返回的数据中，最新的一条为本次读取的数据");
			break;
		case ICallbackStatus.SYNC_HISORY_AIR_PRESSURE_TEMPERATURE_SUCCESS:
			Log.d(TAG, "同步当天历史数据成功，包括气压传感器气压值和温度值，数据已保存到数据库，查询请调用查询数据库接口");
			break;
		case ICallbackStatus.SYNC_HISORY_AIR_PRESSURE_TEMPERATURE_FAIL:
			Log.d(TAG, "同步当天历史数据失败，数据不保存");
			break;
		default:
			break;
		}
	}

	private final String testKey1 = "00a4040008A000000333010101000003330101010000333010101000033301010100003330101010000033301010100333010101000033301010100003330101010000333010101000033301010100003330101010000333010101000033301010100003330101010000333010101000033301010100a4040008A0000003330101010000033301010100003330101010000333010101000033301010100000333010101003330101010000333010101000033301010100003330101010000333010101000033301010100003330101010000333010101000033301010100003330101010000333010101";
	private final String universalKey = "040008A00000033301010100000333010101000033301010100003330101010000333010101000003330100333010101000033301010100003330101010000333010101000033301010100003330101010000333010101000033301010100003330101010000333010";

	@Override
	public void OnDataResult(boolean result, int status, byte[] data) {
        StringBuilder stringBuilder = null;
        if (data != null && data.length > 0) {
            stringBuilder = new StringBuilder(data.length);
            for (byte byteChar : data) {
                stringBuilder.append(String.format("%02X", byteChar));
            }
            Log.i(TAG, "BLE---->APK data =" + stringBuilder.toString());
        }
//		if (status == ICallbackStatus.OPEN_CHANNEL_OK) {// 打开通道OK
//			mHandler.sendEmptyMessage(OPEN_CHANNEL_OK_MSG);
//		} else if (status == ICallbackStatus.CLOSE_CHANNEL_OK) {// 关闭通道OK
//			mHandler.sendEmptyMessage(CLOSE_CHANNEL_OK_MSG);
//		} else if (status == ICallbackStatus.BLE_DATA_BACK_OK) {// 测试通道OK，通道正常
//			mHandler.sendEmptyMessage(TEST_CHANNEL_OK_MSG);
//		}
        switch (status) {
            case ICallbackStatus.OPEN_CHANNEL_OK:// 打开通道OK
                mHandler.sendEmptyMessage(OPEN_CHANNEL_OK_MSG);
                break;
            case ICallbackStatus.CLOSE_CHANNEL_OK:// 关闭通道OK
                mHandler.sendEmptyMessage(CLOSE_CHANNEL_OK_MSG);
                break;
            case ICallbackStatus.BLE_DATA_BACK_OK:// 测试通道OK，通道正常
                mHandler.sendEmptyMessage(TEST_CHANNEL_OK_MSG);
                break;
            //========通用接口回调 Universal Interface   start====================
            case ICallbackStatus.UNIVERSAL_INTERFACE_SDK_TO_BLE_SUCCESS://sdk发送数据到ble完成，并且校验成功，返回状态
                mHandler.sendEmptyMessage(UNIVERSAL_INTERFACE_SDK_TO_BLE_SUCCESS_MSG);
                break;
            case ICallbackStatus.UNIVERSAL_INTERFACE_SDK_TO_BLE_FAIL://sdk发送数据到ble完成，但是校验失败，返回状态
                mHandler.sendEmptyMessage(UNIVERSAL_INTERFACE_SDK_TO_BLE_FAIL_MSG);
                break;
            case ICallbackStatus.UNIVERSAL_INTERFACE_BLE_TO_SDK_SUCCESS://ble发送数据到sdk完成，并且校验成功，返回数据
                mHandler.sendEmptyMessage(UNIVERSAL_INTERFACE_BLE_TO_SDK_SUCCESS_MSG);
                break;
            case ICallbackStatus.UNIVERSAL_INTERFACE_BLE_TO_SDK_FAIL://ble发送数据到sdk完成，但是校验失败，返回状态
                mHandler.sendEmptyMessage(UNIVERSAL_INTERFACE_BLE_TO_SDK_SUCCESS_MSG);
                break;
            //========通用接口回调 Universal Interface   end====================
            case ICallbackStatus.CUSTOMER_ID_OK://回调 客户id
                if (result) {
                    Log.d(TAG, "客户ID = " + GBUtils.getInstance(mContext).customerIDAsciiByteToString(data));
                }

                break;
            case ICallbackStatus.DO_NOT_DISTURB_CLOSE://回调 勿扰模式关闭
                if (data != null && data.length >= 2) {
                    switch (data[1]) {
                        case 0:
                            Log.d(TAG, "勿扰模式已关闭。勿扰时间段之外的时间，关闭手环振动和关闭信息提醒的开关处于关闭状态，即有振动和信息提醒");
                            break;
                        case 2:
                            Log.d(TAG, "勿扰模式已关闭。勿扰时间段之外的时间，关闭手环振动的开关处于打开状态，关闭信息提醒的开关处于关闭状态，即无振动但有信息提醒");
                            break;
                        case 4:
                            Log.d(TAG, "勿扰模式已关闭。勿扰时间段之外的时间，关闭手环振动的开关处于关闭状态，关闭信息提醒的开关处于打开状态，即有振动但无信息提醒");
                            break;
                        case 6:
                            Log.d(TAG, "勿扰模式已关闭。勿扰时间段之外的时间，关闭手环振动和关闭信息提醒的开关处于打开状态，即都没有振动和信息提醒");
                            break;
                    }
                }
                break;
            case ICallbackStatus.DO_NOT_DISTURB_OPEN://回调 勿扰模式打开
                if (data != null && data.length >= 2) {
                    switch (data[1]) {
                        case 0:
                            Log.d(TAG, "勿扰模式已打开。勿扰时间段之外的时间，关闭手环振动和关闭信息提醒的开关处于关闭状态，即有振动和信息提醒");
                            break;
                        case 2:
                            Log.d(TAG, "勿扰模式已打开。勿扰时间段之外的时间，关闭手环振动的开关处于打开状态，关闭信息提醒的开关处于关闭状态，即无振动但有信息提醒");
                            break;
                        case 4:
                            Log.d(TAG, "勿扰模式已打开。勿扰时间段之外的时间，关闭手环振动的开关处于关闭状态，关闭信息提醒的开关处于打开状态，即有振动但无信息提醒");
                            break;
                        case 6:
                            Log.d(TAG, "勿扰模式已打开。勿扰时间段之外的时间，关闭手环振动和关闭信息提醒的开关处于打开状态，即都没有振动和信息提醒");
                            break;
                    }
                }
                break;
            default:
                break;
        }

    }

	@Override
	public void onCharacteristicWriteCallback(int status) {// add 20170221
		// 写入操作的系统回调，status = 0为写入成功，其他或无回调表示失败
		Log.d(TAG, "Write System callback status = " + status);
	}

	@Override
	public void OnServerCallback(int status) {
		Log.i(TAG, "服务器回调 OnServerCallback status =" + status);

		mHandler.sendEmptyMessage(SERVER_CALL_BACK_OK_MSG);

	}

	@Override
	public void OnServiceStatuslt(int status) {
		if (status == ICallbackStatus.BLE_SERVICE_START_OK) {
			Log.d(TAG, "OnServiceStatuslt mBluetoothLeService11 ="+mBluetoothLeService);
			if (mBluetoothLeService == null) {
				mBluetoothLeService = mBLEServiceOperate.getBleService();
				mBluetoothLeService.setICallback(this);
				Log.d(TAG, "OnServiceStatuslt mBluetoothLeService22 ="+mBluetoothLeService);
			}
		}
	}

	private static final int SHOW_SET_PASSWORD_MSG = 26;
	private static final int SHOW_INPUT_PASSWORD_MSG = 27;
	private static final int SHOW_INPUT_PASSWORD_AGAIN_MSG = 28;

	private boolean isPasswordDialogShowing = false;
	private String password = "";

	private void showPasswordDialog(final int type) {
		Log.d(TAG, "showPasswordDialog");
		if (isPasswordDialogShowing) {
			Log.d(TAG, "已有对话框弹出");
			return;
		}
		CustomPasswordDialog.Builder builder = new CustomPasswordDialog.Builder(
				MainActivity.this, mTextWatcher);
		builder.setPositiveButton(getResources().getString(R.string.confirm),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						if (password.length() == 4) {
							Log.d("CustomPasswordDialog", "密码是4位  password =" + password);
							dialog.dismiss();
							isPasswordDialogShowing = false;

							mWriteCommand.sendToSetOrInputPassward(password,
									type);
						}
					}
				});
		builder.setNegativeButton(getResources().getString(R.string.cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						isPasswordDialogShowing = false;
					}
				});
		builder.create().show();

		if (type == GlobalVariable.PASSWORD_TYPE_SET) {
			builder.setTittle(mContext.getResources().getString(
					R.string.set_password_for_band));
		} else if (type == GlobalVariable.PASSWORD_TYPE_INPUT_AGAIN) {
			builder.setTittle(mContext.getResources().getString(
					R.string.input_password_for_band_again));
		} else {
			builder.setTittle(mContext.getResources().getString(
					R.string.input_password_for_band));
		}
		isPasswordDialogShowing = true;
	}

	private TextWatcher mTextWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			password = s.toString();
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub
		}

		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub
		}
	};

	private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";

	/**
	 * 激活设备管理权限
	 * 
	 * @return
	 */
	private boolean isEnabled() {
		String pkgName = getPackageName();
		Log.w(TAG, "---->pkgName = " + pkgName);
		final String flat = Settings.Secure.getString(getContentResolver(),
				ENABLED_NOTIFICATION_LISTENERS);
		if (!TextUtils.isEmpty(flat)) {
			final String[] names = flat.split(":");
			for (int i = 0; i < names.length; i++) {
				final ComponentName cn = ComponentName
						.unflattenFromString(names[i]);
				if (cn != null) {
					if (TextUtils.equals(pkgName, cn.getPackageName())) {
						return true;
					}
				}
			}
		}
		return false;
	}

    private void upDateTodaySwimData() {
        // TODO Auto-generated method stub
//		SwimInfo mSwimInfo = mySQLOperate.querySwimData(CalendarUtils
//				.getCalendar(0));// 传入日期，0为今天，-1为昨天，-2为前天。。。。
        List<SwimDayInfo> list = mySQLOperate.querySwimDayInfo(CalendarUtils
                .getCalendar(0));// 传入日期，0为今天，-1为昨天，-2为前天。。。。
        if (list != null) {
            SwimDayInfo mSwimInfo = null;
            for (int i = 0; i < list.size(); i++) {
                mSwimInfo = list.get(i);
                if (mSwimInfo!=null){
                    swim_time.setText(mSwimInfo.getUseTime() + "");
                    swim_stroke_count.setText(mSwimInfo.getCount() + "");
                    swim_calorie.setText(mSwimInfo.getCalories() + "");
                }
            }

        }
    }

    ;

	/*
	 * 获取一天最新心率值、最高、最低、平均心率值
	 */
	private void UpdateBloodPressureMainUI(String calendar) {
		// UTESQLOperate mySQLOperate = new UTESQLOperate(mContext);
		List<BPVOneDayInfo> mBPVOneDayListInfo = mySQLOperate
				.queryBloodPressureOneDayInfo(calendar);
		if (mBPVOneDayListInfo != null) {
			int highPressure = 0;
			int lowPressure = 0;
			int time = 0;
			for (int i = 0; i < mBPVOneDayListInfo.size(); i++) {
				highPressure = mBPVOneDayListInfo.get(i)
						.getHightBloodPressure();
				lowPressure = mBPVOneDayListInfo.get(i).getLowBloodPressure();
				time = mBPVOneDayListInfo.get(i).getBloodPressureTime();
			}
			Log.d(TAG, "highPressure =" + highPressure
					+ ",lowPressure =" + lowPressure);
			// current_rate.setText(currentRate + "");
			if (highPressure == 0) {
				tv_high_pressure.setText("--");

			} else {
				tv_high_pressure.setText(highPressure + "");

			}
			if (lowPressure == 0) {
				tv_low_pressure.setText("--");
			} else {
				tv_low_pressure.setText(lowPressure + "");
			}

		} else {
			tv_high_pressure.setText("--");
			tv_low_pressure.setText("--");

		}
	}

	private void initIbeacon() {
		// TODO Auto-generated method stub
		ibeacon_command = (Button) findViewById(R.id.ibeacon_command);
		ibeacon_command.setOnClickListener(this);
		ibeaconStatusSpinner = (Spinner) findViewById(R.id.ibeacon_status);
		setOrReadSpinner = (Spinner) findViewById(R.id.SetOrReadSpinner);
		ibeaconStatusSpinnerList.add("UUID");
		ibeaconStatusSpinnerList.add("major");
		ibeaconStatusSpinnerList.add("minor");
		ibeaconStatusSpinnerList.add("device name");
		ibeaconStatusSpinnerList.add("TX power");
		ibeaconStatusSpinnerList.add("advertising interval");
//		ibeaconStatusSpinnerList.add("横屏");
//		ibeaconStatusSpinnerList.add("竖屏英文");
//		ibeaconStatusSpinnerList.add("竖屏中文");
//		ibeaconStatusSpinnerList.add("不设置");
		aibeaconStatusAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, ibeaconStatusSpinnerList);
		aibeaconStatusAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		ibeaconStatusSpinner.setAdapter(aibeaconStatusAdapter);
		ibeaconStatusSpinner
				.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						// TODO Auto-generated method stub
						Log.d(TAG,
								"选择了 "
										+ aibeaconStatusAdapter
												.getItem(position));
					 
						if (position==0) {
							ibeaconStatus =GlobalVariable.IBEACON_TYPE_UUID;
//							if (GetFunctionList.isSupportFunction(mContext,
//									GlobalVariable.IS_SUPPORT_DIAL_SWITCH)) {
//								dialType = GlobalVariable.SHOW_HORIZONTAL_SCREEN;
//								mWriteCommand
//										.controlDialSwitchAandLeftRightHand(
//												leftRightHand, dialType);
//							}
						}else if (position==1) {
							ibeaconStatus =GlobalVariable.IBEACON_TYPE_MAJOR;
//							if (GetFunctionList.isSupportFunction(mContext,
//									GlobalVariable.IS_SUPPORT_DIAL_SWITCH)) {
//							dialType =GlobalVariable.SHOW_VERTICAL_ENGLISH_SCREEN;
//							mWriteCommand.controlDialSwitchAandLeftRightHand(leftRightHand, dialType);
//							}
						}else if (position==2) {
							ibeaconStatus =GlobalVariable.IBEACON_TYPE_MINOR;
//							if (GetFunctionList.isSupportFunction(mContext,
//									GlobalVariable.IS_SUPPORT_DIAL_SWITCH)) {
//							dialType =GlobalVariable.SHOW_VERTICAL_CHINESE_SCREEN;
//							mWriteCommand.controlDialSwitchAandLeftRightHand(leftRightHand, dialType);
//							}
						}else if (position==3) {
							ibeaconStatus =GlobalVariable.IBEACON_TYPE_DEVICE_NAME;
//							if (GetFunctionList.isSupportFunction(mContext,
//									GlobalVariable.IS_SUPPORT_DIAL_SWITCH)) {
//							dialType =GlobalVariable.NOT_SET_UP;
//							mWriteCommand.controlDialSwitchAandLeftRightHand(leftRightHand, dialType);
//							}
						}else if (position==4) {
							ibeaconStatus =GlobalVariable.IBEACON_TYPE_TX_POWER;
						}else if (position==5) {
							ibeaconStatus =GlobalVariable.IBEACON_TYPE_ADVERTISING_INTERVAL;
						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
						// TODO Auto-generated method stub

					}
				});
		
		SetOrReadSpinnerList.add("设置");
		SetOrReadSpinnerList.add("获取");
//		SetOrReadSpinnerList.add("左手");
//		SetOrReadSpinnerList.add("右手");
//		SetOrReadSpinnerList.add("不设置");

		setOrReadAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, SetOrReadSpinnerList);
		setOrReadAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		setOrReadSpinner.setAdapter(setOrReadAdapter);
		setOrReadSpinner
				.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						// TODO Auto-generated method stub
						Log.d(TAG, "选择了 " + setOrReadAdapter.getItem(position)/*+",支持表盘 ="+GetFunctionList.isSupportFunction(mContext,
								GlobalVariable.IS_SUPPORT_DIAL_SWITCH)*/);
						if (position==0) {
							ibeaconSetOrRead =GlobalVariable.IBEACON_SET;
//							if (GetFunctionList.isSupportFunction(mContext,
//									GlobalVariable.IS_SUPPORT_DIAL_SWITCH)) {
//							leftRightHand =GlobalVariable.LEFT_HAND_WEAR;
//							mWriteCommand.controlDialSwitchAandLeftRightHand(leftRightHand, dialType);
//							}
						}else if (position==1) {
							ibeaconSetOrRead =GlobalVariable.IBEACON_GET;
//							if (GetFunctionList.isSupportFunction(mContext,
//									GlobalVariable.IS_SUPPORT_DIAL_SWITCH)) {
//							leftRightHand =GlobalVariable.RIGHT_HAND_WEAR;
//							mWriteCommand.controlDialSwitchAandLeftRightHand(leftRightHand, dialType);
//							}
						}
//						else if (position==2) {
//							if (GetFunctionList.isSupportFunction(mContext,
//									GlobalVariable.IS_SUPPORT_DIAL_SWITCH)) {
//							leftRightHand =GlobalVariable.NOT_SET_UP;
//							mWriteCommand.controlDialSwitchAandLeftRightHand(leftRightHand, dialType);
//							}
//						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
						// TODO Auto-generated method stub

					}
				});
	}

	@Override
	public void onIbeaconWriteCallback(boolean result, int ibeaconSetOrGet,
			int ibeaconType, String data) {
		// public static final int IBEACON_TYPE_UUID = 0;// Ibeacon
		// 指令类型,设置UUID/获取UUID
		// public static final int IBEACON_TYPE_MAJOR = 1;// Ibeacon
		// 指令类型,设置major/获取major
		// public static final int IBEACON_TYPE_MINOR = 2;// Ibeacon
		// 指令类型,设置minor/获取minor
		// public static final int IBEACON_TYPE_DEVICE_NAME = 3;// Ibeacon
		// 指令类型,设置蓝牙device name/获取蓝牙device name
		// public static final int IBEACON_SET = 0;// Ibeacon
		// 设置(设置UUID/设置major,设置minor,设置蓝牙device name)
		// public static final int IBEACON_GET = 1;// Ibeacon
		// 获取(设置UUID/设置major,设置minor,设置蓝牙device name)
		Log.d(TAG, "onIbeaconWriteCallback 设置或获取结果result =" + result
				+ ",ibeaconSetOrGet =" + ibeaconSetOrGet + ",ibeaconType ="
				+ ibeaconType + ",数据data =" + data);
		if (result) {// success
			switch (ibeaconSetOrGet) {
			case GlobalVariable.IBEACON_SET:
				switch (ibeaconType) {
				case GlobalVariable.IBEACON_TYPE_UUID:
					Log.d(TAG, "设置UUID成功,data =" + data);
					break;
				case GlobalVariable.IBEACON_TYPE_MAJOR:
					Log.d(TAG, "设置major成功,data =" + data);
					break;
				case GlobalVariable.IBEACON_TYPE_MINOR:
					Log.d(TAG, "设置minor成功,data =" + data);
					break;
				case GlobalVariable.IBEACON_TYPE_DEVICE_NAME:
					Log.d(TAG, "设置device name成功,data =" + data);
					break;
				case GlobalVariable.IBEACON_TYPE_TX_POWER:
					Log.d(TAG, "设置TX power成功,data =" + data);
					break;
				case GlobalVariable.IBEACON_TYPE_ADVERTISING_INTERVAL:
					Log.d(TAG, "设置advertising interval成功,data =" + data);
					break;

				default:
					break;
				}
				break;
			case GlobalVariable.IBEACON_GET:
				switch (ibeaconType) {
				case GlobalVariable.IBEACON_TYPE_UUID:
					Log.d(TAG, "获取UUID成功,data =" + data);
					break;
				case GlobalVariable.IBEACON_TYPE_MAJOR:
					Log.d(TAG, "获取major成功,data =" + data);
					break;
				case GlobalVariable.IBEACON_TYPE_MINOR:
					Log.d(TAG, "获取minor成功,data =" + data);
					break;
				case GlobalVariable.IBEACON_TYPE_DEVICE_NAME:
					Log.d(TAG, "获取device name成功,data =" + data);
					break;
				case GlobalVariable.IBEACON_TYPE_TX_POWER:
					Log.d(TAG, "获取TX power成功,data =" + data);
					break;
				case GlobalVariable.IBEACON_TYPE_ADVERTISING_INTERVAL:
					Log.d(TAG, "获取advertising interval,data =" + data);
					break;

				default:
					break;
				}
				break;

			default:
				break;
			}

		} else {// fail
			switch (ibeaconSetOrGet) {
			case GlobalVariable.IBEACON_SET:
				switch (ibeaconType) {
				case GlobalVariable.IBEACON_TYPE_UUID:
					Log.d(TAG, "设置UUID失败");
					break;
				case GlobalVariable.IBEACON_TYPE_MAJOR:
					Log.d(TAG, "设置major失败");
					break;
				case GlobalVariable.IBEACON_TYPE_MINOR:
					Log.d(TAG, "设置minor失败");
					break;
				case GlobalVariable.IBEACON_TYPE_DEVICE_NAME:
					Log.d(TAG, "设置device name失败");
					break;

				default:
					break;
				}
				break;
			case GlobalVariable.IBEACON_GET:
				switch (ibeaconType) {
				case GlobalVariable.IBEACON_TYPE_UUID:
					Log.d(TAG, "获取UUID失败");
					break;
				case GlobalVariable.IBEACON_TYPE_MAJOR:
					Log.d(TAG, "获取major失败");
					break;
				case GlobalVariable.IBEACON_TYPE_MINOR:
					Log.d(TAG, "获取minor失败");
					break;
				case GlobalVariable.IBEACON_TYPE_DEVICE_NAME:
					Log.d(TAG, "获取device name失败");
					break;

				default:
					break;
				}
				break;

			default:
				break;
			}
		}

	}
	
	@Override
	public void onQueryDialModeCallback(boolean result, int screenWith,
			int screenHeight, int screenCount) {// 查询表盘方式回调
		Log.d(TAG, "result =" + result + ",screenWith =" + screenWith
				+ ",screenHeight =" + screenHeight + ",screenCount ="
				+ screenCount);
	}

	@Override
	public void onControlDialCallback(boolean result, int leftRightHand,
			int dialType) {// 控制表盘切换和左右手切换回调
		switch (leftRightHand) {
		case GlobalVariable.LEFT_HAND_WEAR:
			Log.d(TAG, "设置左手佩戴成功");
			break;
		case GlobalVariable.RIGHT_HAND_WEAR:
			Log.d(TAG, "设置右手佩戴成功");
			break;
		case GlobalVariable.NOT_SET_UP:
			Log.d(TAG, "不设置，保持上次佩戴方式成功");
			break;

		default:
			break;
		}
		switch (dialType) {
		case GlobalVariable.SHOW_VERTICAL_ENGLISH_SCREEN:
			Log.d(TAG, "设置显示竖屏英文界面成功");
			break;
		case GlobalVariable.SHOW_VERTICAL_CHINESE_SCREEN:
			Log.d(TAG, "设置显示竖屏中文界面成功");
			break;
		case GlobalVariable.SHOW_HORIZONTAL_SCREEN:
			Log.d(TAG, "设置显示横屏成功");
			break;
		case GlobalVariable.NOT_SET_UP:
			Log.d(TAG, "不设置，默认上次显示的屏幕成功");
			break;

		default:
			break;
		}
	}

 
	/**
	 * 发送七天天气接口
	 */
	private void testSendSevenDayWeather() {
		// TODO Auto-generated method stub
		// SevenDayWeatherInfo info =new SevenDayWeatherInfo(cityName,
		// todayWeatherCode, todayTmpCurrent, todayTmpMax, todayTmpMin,
		// todayPm25, todayAqi,
		// secondDayWeatherCode, secondDayTmpMax, secondDayTmpMin,
		// thirdDayWeatherCode, thirdDayTmpMax, thirdDayTmpMin,
		// fourthDayWeatherCode, fourthDayTmpMax, fourthDayTmpMin,
		// fifthDayWeatherCode, fifthDayTmpMax, fifthDayTmpMin,
		// sixthDayWeatherCode, sixthDayTmpMax, sixthDayTmpMin,
		// seventhDayWeatherCode, seventhDayTmpMax, seventhDayTmpMin);

		if (GetFunctionList.isSupportFunction(mContext,
				GlobalVariable.IS_SUPPORT_SEVEN_DAYS_WEATHER)) {
			SevenDayWeatherInfo info = new SevenDayWeatherInfo("深圳市", "308",
					25, 30, 20, 155, 50, "311", 32, 12, "312", 33, 13, "313",
					34, 14, "314", 35, 15, "315", 36, 16, "316", 37, 17);

			mWriteCommand.syncWeatherToBLEForXiaoYang(info);
		} else {
			Toast.makeText(mContext, "不支持七天天气", Toast.LENGTH_SHORT).show();
		}
	}

    private void upDateTodaySkipData() {
        // TODO Auto-generated method stub
        List<SkipDayInfo> list = mySQLOperate.querySkipDayInfo(CalendarUtils
                .getCalendar(0));// 传入日期，0为今天，-1为昨天，-2为前天。。。。

        if (list != null) {
            SkipDayInfo mSkipInfo = null;
            for (int i = 0; i < list.size(); i++) {
                mSkipInfo = list.get(i);
                if (mSkipInfo != null) {
                    skip_time.setText(mSkipInfo.getUseTime() + "");
                    skip_count.setText(mSkipInfo.getCount() + "");
                    skip_calorie.setText(mSkipInfo.getCalories() + "");
                }
            }
        }
    }

	@Override
	public void onSportsTimeCallback(boolean result, String calendar,int sportsTime,
			int timeType) {

		if (timeType == GlobalVariable.SPORTS_TIME_TODAY) {
			 
			Log.d(TAG, "今天的运动时间  calendar =" + calendar + ",sportsTime ="
					+ sportsTime);
			resultBuilder.append("\n" + calendar + "," + sportsTime
					+ getResources().getString(R.string.fminute));
			mHandler.sendEmptyMessage(UPDATE_SPORTS_TIME_DETAILS_MSG);

		} else if (timeType == GlobalVariable.SPORTS_TIME_HISTORY_DAY) {// 7天的运动时间
				Log.d(TAG, "7天的运动时间  calendar =" + calendar
						+ ",sportsTime =" + sportsTime);
				resultBuilder.append("\n" + calendar + "," + sportsTime
						+ getResources().getString(R.string.fminute));
			mHandler.sendEmptyMessage(UPDATE_SPORTS_TIME_DETAILS_MSG);
		}
	}

    @Override
    public void OnResultSportsModes(boolean result, int status, boolean switchStatus, int sportsModes, SportsModesInfo info) {
        MultipleSportsModesUtils.LLogI("OnResultSportsModes  result =" + result + ",status =" + status + ",switchStatus =" + switchStatus
                + ",sportsModes =" + sportsModes + ",info =" + info);
        switch (status) {
            case ICallbackStatus.CONTROL_MULTIPLE_SPORTS_MODES://多运动模式及运动心率 控制开关，模式
                break;
            case ICallbackStatus.INQUIRE_MULTIPLE_SPORTS_MODES://多运动模式及运动心率 查询当前模式和开关
                break;
            case ICallbackStatus.SYNC_MULTIPLE_SPORTS_MODES_START://多运动模式及运动心率 开始同步，返回此次同步有多少次运动
                //注意：sportsModes 在这个case有点特殊，sportsModes为返回此次同步有多少次运动
                break;
            case ICallbackStatus.SYNC_MULTIPLE_SPORTS_MODES://多运动模式及运动心率 某一种运动模式同步完成
                break;
            case ICallbackStatus.MULTIPLE_SPORTS_MODES_REAL://多运动模式及运动心率 实时数据，只有实时数据时，SportsModesInfo才不为空，其他的都为空
                break;
        }
    }

    @Override
	public void onRateCalibrationStatus(int status) {
		// TODO Auto-generated method stub
/*		status: 0----校准完成
		        1----校准开始
		        253---清除校准参数完成
		        校准开始后，应用端自己做超时，10秒钟没收到校准完成0，则需主动调用停止校准stopRateCalibration()*/

		Log.d(TAG, "心率校准 status:"+status);
		
	}
	
	@Override
	public void onTurnWristCalibrationStatus(int status) {
		// TODO Auto-generated method stub
		Log.d(TAG, "翻腕校准 status:"+status);
		/*status: 0----校准完成
                  1----校准开始
                  255----校准失败
                  253---清除校准参数完成*/
	}
	
	
	
//	private void queryAirPressureTemperature(int type) {
//		List<AirPressureTemperatureDayInfo>  list;
//		if (type==1) {//查询一天的气压数据，传入查询的日期
//			list =mySQLOperate.queryAirPressureTemperatureOneDayInfo("20171113");
//		}else {//查询所有的气压数据
//			list =mySQLOperate.queryAirPressureTemperatureAllDayInfo();
//		}
//		AirPressureTemperatureDayInfo info =null;
//		for (int i = 0; i < list.size(); i++) {
//			info =list.get(i);
//			String calendar = info.getCalendar();
//			int time = info.getTime();
//			int airPressure = info.getAirPressure();
//			int temperature = info.getTemperature();
//			Log.d(TAG,"查询气压温度  calendar ="+calendar+",time ="+time+",airPressure ="+airPressure+",temperature ="+temperature);
//		}
//	}

}
