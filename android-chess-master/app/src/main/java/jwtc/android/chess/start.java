package jwtc.android.chess;

import jwtc.android.chess.puzzle.practice;
import jwtc.android.chess.puzzle.puzzle;
import jwtc.android.chess.tools.pgntool;
import jwtc.android.chess.ics.*;
import jwtc.chess.JNI;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.Object;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.support.v7.media.MediaRouter.RouteInfo;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import static jwtc.android.chess.CornerSelector.getCornerPosition;
import static jwtc.chess.JNI.temp;

public class start extends AppCompatActivity {

	//private ListView _lvStart;
	public static final String TAG = "start";

	private MediaRouter mMediaRouter;
	private MediaRouteSelector mMediaRouteSelector;
	private MediaRouter.Callback mMediaRouterCallback;
	private CastDevice mSelectedDevice;
	private GoogleApiClient mApiClient;
	private Cast.Listener mCastListener;
	private ConnectionCallbacks mConnectionCallbacks;
	private ConnectionFailedListener mConnectionFailedListener;
	private ChessChannel mChessChannel;
	private boolean mApplicationStarted;
	private boolean mWaitingForReconnect;
	private String mSessionId;
	protected Tracker _tracker;

	private ListView _list;

	private JNI _jni;
	private Timer _timer;
	private String _lastMessage;
	private static String _ssActivity = "";

	    /**
		 * Called when the activity is first created.
		 */
		@Override
		public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences getData = getSharedPreferences("ChessPlayer", Context.MODE_PRIVATE);
		String myLanguage  	= getData.getString("localelanguage", "");

		Locale current = getResources().getConfiguration().locale;
		String language = current.getLanguage();
		if(myLanguage.equals("")){    // localelanguage not used yet? then use device default locale
			myLanguage = language;
		}

		Locale locale = new Locale(myLanguage);    // myLanguage is current language
		Locale.setDefault(locale);
		Configuration config = new Configuration();
		config.locale = locale;
		getBaseContext().getResources().updateConfiguration(config,
				getBaseContext().getResources().getDisplayMetrics());

		setContentView(R.layout.start);

		if (getIntent().getBooleanExtra("RESTART", false)) {
			finish();
			Intent intent = new Intent(this, start.class);
			startActivity(intent);
		}

		_jni = new JNI();

		_lastMessage = "";

		_timer = new Timer(true);
		_timer.schedule(new TimerTask() {
			@Override
			public void run() {
				sendMessage(_jni.toFEN());
			}
		}, 1000, 500);

		String[] title = getResources().getStringArray(R.array.start_menu);

		_list = (ListView)findViewById(R.id.ListStart);
		_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				_ssActivity = parent.getItemAtPosition(position).toString();
				try {
					Intent i = new Intent();
					Log.i("start", _ssActivity);
					if (_ssActivity.equals(getString(R.string.start_play))) {
						i.setClass(start.this, main.class);
						i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
						startActivity(i);
					} /*else if (_ssActivity.equals(getString(R.string.start_practice))) {
						i.setClass(start.this, practice.class);
						i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
						startActivity(i);
					} else if (_ssActivity.equals(getString(R.string.start_puzzles))) {
						i.setClass(start.this, puzzle.class);
						i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
						startActivity(i);
					} */else if (_ssActivity.equals(getString(R.string.start_about))) {
						i.setClass(start.this, HtmlActivity.class);
						i.putExtra(HtmlActivity.HELP_MODE, "about");
						startActivity(i);
					}/* else if (_ssActivity.equals(getString(R.string.start_ics))) {
						i.setClass(start.this, ICSClient.class);
						startActivity(i);
					} else if (_ssActivity.equals(getString(R.string.start_pgn))) {
						i.setClass(start.this, pgntool.class);
						i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
						startActivity(i);
					}*/ /*else if (_ssActivity.equals(getString(R.string.start_globalpreferences))) {
						i.setClass(start.this, ChessPreferences.class);
						startActivityForResult(i, 0);
					} else if (_ssActivity.equals(getString(R.string.menu_help))) {
						i.setClass(start.this, HtmlActivity.class);
						i.putExtra(HtmlActivity.HELP_MODE, "help");
						startActivity(i);
					} */else if (_ssActivity.equals(getString(R.string.start_camera))){
						dispatchTakePictureIntent();
					} else if (_ssActivity.equals(getString(R.string.textbox))){
						i.setClass(start.this, textbox.class);
						startActivityForResult(i,TEXTBOX);
					}

				} catch (Exception ex) {
					Toast t = Toast.makeText(start.this, R.string.toast_could_not_start_activity, Toast.LENGTH_LONG);
					t.setGravity(Gravity.BOTTOM, 0, 0);
					t.show();
				}
			}
		});


		MyApplication application = (MyApplication) getApplication();
		_tracker = application .getDefaultTracker();

			// Configure Cast device discovery
		mMediaRouter = MediaRouter.getInstance(getApplicationContext());
		mMediaRouteSelector = new MediaRouteSelector.Builder()
				.addControlCategory(CastMediaControlIntent.categoryForCast("05EB93C6")).build();
		mMediaRouterCallback = new MyMediaRouterCallback();


	}
	Uri UriForPhoto;

	private static String mCurrentPhotoPath;

	public static String getmCurrentPhotoPath(){
		return mCurrentPhotoPath;
	}

	public void setmCurrentPhotoPath(String PhotoPath){
		this.mCurrentPhotoPath = PhotoPath;
	}

	private File createImageFile() throws IOException {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = "JPEG_" + timeStamp;
		File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
		//File storageDir = Environment.getExternalStorageDirectory();
		File image = File.createTempFile(imageFileName, ".jpg", storageDir);
		setmCurrentPhotoPath(image.getAbsolutePath());
		return image;
	}
	/*private void galleryAddPic() {
		Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		File f = new File(mCurrentPhotoPath);
		Uri contentUri = Uri.fromFile(f);
		mediaScanIntent.setData(contentUri);
		this.sendBroadcast(mediaScanIntent);
	}*/
	static final int REQUEST_TAKE_PHOTO = 1;
	static final int TEXTBOX = 3;
	private void dispatchTakePictureIntent(){
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
			File photoFile = null;
			try{
				photoFile = createImageFile();
			} catch (IOException ex){
			}
			if(photoFile != null){
				UriForPhoto = FileProvider.getUriForFile(this,"jwtc.android.chess.photoProvider",photoFile);
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, UriForPhoto);
				//fileObserver test = new fileObserver(mCurrentPhotoPath);
				//test.startWatching();
				startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
			}
		}
	}

	static final int REQUEST_CORNER_SELECTOR = 2;
	public void startCornerSelectorActivity() {
		final Context context3 = this;
		Intent intent = new Intent(context3, CornerSelector.class);
		startActivityForResult(intent, REQUEST_CORNER_SELECTOR);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("ActivityResult","result " + requestCode + " " + resultCode);
		if(requestCode == REQUEST_TAKE_PHOTO) {
			if (resultCode == RESULT_OK) {
				//galleryAddPic();
				// The user picked a contact.
				// The Intent's data Uri identifies which contact was selected.

				// Do something with the contact here (bigger example below)
				//startCornerSelectorActivity();
			}
		} else if(requestCode == 3) {
			Log.d("String_array","starting");
			temp();
		} else if(requestCode == 2) {
			if(resultCode == RESULT_CANCELED || resultCode == RESULT_OK || resultCode == 0) {
				//start another activity
				Log.d("result", "here");
				Log.d("Path Name", mCurrentPhotoPath);
				String[] array = mCurrentPhotoPath.split("/");
				boolean past = true;
				for (int i = 1; i < array.length - 1; i++) {
					Pattern p = Pattern.compile("[A-Za-z0-9.]+");
					Matcher m = p.matcher(array[i]);
					if (!m.matches()) {
						past = false;
					}
				}
				Pattern p = Pattern.compile("[A-Za-z0-9._\\-]+");
				Matcher m = p.matcher(array[array.length-1]);
				if (!m.matches()) {
					past = false;
				}
				if(past == true) {
					String filePath = "";
					for(int i = 0; i < array.length-1; i++){
						filePath += array[i] + "/";
					}
					Log.d("File Path", "" + filePath);
					Log.d("File Name", "" + array[array.length-1]);
					int[][] temp = getCornerPosition();
					for (int i = 0; i < 4; i++) {
						for (int j = 0; j < 2; j++) {
							Log.d("CornerPosition", "" + temp[i][j]);
						}
					}
					//board.init(filePath, array[array.length-1], "bottom", getCornerPosition());
					String path = getFilesDir().getAbsolutePath();
					Log.d("pathwieofjaweof",path);
					try {
						Runtime rt = Runtime.getRuntime();
						String[] commands = {"/qpython3", path +"/test.py"};
						Process proc = rt.exec(commands);

						BufferedReader stdInput = new BufferedReader(new
								InputStreamReader(proc.getInputStream()));

						BufferedReader stdError = new BufferedReader(new
								InputStreamReader(proc.getErrorStream()));

// read the output from the command
						Log.d("stdout","Here is the standard output of the command:");
						String s = null;
						while ((s = stdInput.readLine()) != null) {
							Log.d("stdout",s);
						}

// read any errors from the attempted command
						Log.d("stderr","Here is the standard error of the command (if any):");
						while ((s = stdError.readLine()) != null) {
							Log.d("stderr",s);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					//error
					Log.d("matcher", "error");
				}
			}
		} else {
			if (resultCode == 1) {
				Log.i(TAG, "finish and restart");
				Intent intent = new Intent(this, start.class);
				//intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra("RESTART", true);
				startActivity(intent);
			}
		}
	}

	@Override
	protected void onPostResume() {
		super.onPostResume();

		SharedPreferences getData = getSharedPreferences("ChessPlayer", Context.MODE_PRIVATE);
		if (getData.getBoolean("RESTART", false)) {
			finish();
			Intent intent = new Intent(this, start.class);
			//intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

			SharedPreferences.Editor editor = getData.edit();
			editor.putBoolean("RESTART", false);
			editor.apply();

			startActivity(intent);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		// Start media router discovery
		mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,
				MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
	}

	@Override
	protected void onStop() {
		// End media router discovery
		mMediaRouter.removeCallback(mMediaRouterCallback);
		super.onStop();
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		teardown(true);
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.start_topmenu, menu);
		MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
		MediaRouteActionProvider mediaRouteActionProvider
				= (MediaRouteActionProvider) MenuItemCompat
				.getActionProvider(mediaRouteMenuItem);
		// Set the MediaRouteActionProvider selector for device discovery.
		mediaRouteActionProvider.setRouteSelector(mMediaRouteSelector);
		return true;
	}

	/**
	 * Send a text message to the receiver
	 */
	private void sendMessage(final String message) {
		if (mApiClient != null && mChessChannel != null && message != null) {
			try {
				if(!_lastMessage.equals(message)) {
					//Log.i(TAG, "Try to send " + message);
					Cast.CastApi.sendMessage(mApiClient,
							mChessChannel.getNamespace(), message).setResultCallback(
							new ResultCallback<Status>() {
								@Override
								public void onResult(Status result) {
									if (result.isSuccess()) {
										_lastMessage = message;
									} else {
										Log.e(TAG, "Sending message failed");
									}
								}
							});
				}
			} catch (Exception e) {
				Log.e(TAG, "Exception while sending message", e);
			}
		}
	}

	/**
	 * Callback for MediaRouter events
	 */
	private class MyMediaRouterCallback extends MediaRouter.Callback {

		@Override
		public void onRouteSelected(MediaRouter router, RouteInfo info) {
			Log.d(TAG, "onRouteSelected");
			// Handle the user route selection.
			mSelectedDevice = CastDevice.getFromBundle(info.getExtras());

			launchReceiver();
		}

		@Override
		public void onRouteUnselected(MediaRouter router, RouteInfo info) {
			Log.d(TAG, "onRouteUnselected: info=" + info);
			teardown(false);
			mSelectedDevice = null;
		}
	}

	/**
	 * Google Play services callbacks
	 */
	private class ConnectionCallbacks implements
			GoogleApiClient.ConnectionCallbacks {

		@Override
		public void onConnected(Bundle connectionHint) {
			Log.d(TAG, "onConnected");

			if (mApiClient == null) {
				// We got disconnected while this runnable was pending
				// execution.
				return;
			}

			try {
				if (mWaitingForReconnect) {
					mWaitingForReconnect = false;

					// Check if the receiver app is still running
					if ((connectionHint != null)
							&& connectionHint.getBoolean(Cast.EXTRA_APP_NO_LONGER_RUNNING)) {
						Log.d(TAG, "App  is no longer running");
						teardown(true);
					} else {
						// Re-create the custom message channel
						try {
							Cast.CastApi.setMessageReceivedCallbacks(
									mApiClient,
									mChessChannel.getNamespace(),
									mChessChannel);
						} catch (IOException e) {
							Log.e(TAG, "Exception while creating channel", e);
						}
					}
				} else {
					// Launch the receiver app
					Cast.CastApi.launchApplication(mApiClient, "05EB93C6", false)
							.setResultCallback(
									new ResultCallback<Cast.ApplicationConnectionResult>() {
										@Override
										public void onResult(
												Cast.ApplicationConnectionResult result) {
											Status status = result.getStatus();
											Log.d(TAG,
													"ApplicationConnectionResultCallback.onResult:"
															+ status.getStatusCode());
											if (status.isSuccess()) {
												ApplicationMetadata applicationMetadata = result
														.getApplicationMetadata();
												mSessionId = result.getSessionId();
												String applicationStatus = result
														.getApplicationStatus();
												boolean wasLaunched = result.getWasLaunched();
												Log.d(TAG, "application name: "
														+ applicationMetadata.getName()
														+ ", status: " + applicationStatus
														+ ", sessionId: " + mSessionId
														+ ", wasLaunched: " + wasLaunched);
												mApplicationStarted = true;

												_tracker.send(new HitBuilders.EventBuilder()
														.setCategory("Cast")
														.setAction("started")
														.build());
												// Create the custom message
												// channel
												mChessChannel = new ChessChannel();
												try {
													Cast.CastApi.setMessageReceivedCallbacks(
															mApiClient,
															mChessChannel.getNamespace(),
															mChessChannel);
												} catch (IOException e) {
													Log.e(TAG, "Exception while creating channel",
															e);
												}

											} else {
												Log.e(TAG, "application could not launch");
												teardown(true);
											}
										}
									});
				}
			} catch (Exception e) {
				Log.e(TAG, "Failed to launch application", e);
			}
		}

		@Override
		public void onConnectionSuspended(int cause) {
			Log.d(TAG, "onConnectionSuspended");
			mWaitingForReconnect = true;
		}
	}
	/**
	 * Google Play services callbacks
	 */
	private class ConnectionFailedListener implements
			GoogleApiClient.OnConnectionFailedListener {

		@Override
		public void onConnectionFailed(ConnectionResult result) {
			Log.e(TAG, "onConnectionFailed ");

			teardown(false);
		}
	}

	/**
	 * Start the receiver app
	 */
	private void launchReceiver() {
		try {
			mCastListener = new Cast.Listener() {

				@Override
				public void onApplicationDisconnected(int errorCode) {
					Log.d(TAG, "application has stopped");
					teardown(true);
				}

			};
			// Connect to Google Play services
			mConnectionCallbacks = new ConnectionCallbacks();
			mConnectionFailedListener = new ConnectionFailedListener();
			Cast.CastOptions.Builder apiOptionsBuilder = Cast.CastOptions
					.builder(mSelectedDevice, mCastListener);
			mApiClient = new GoogleApiClient.Builder(this)
					.addApi(Cast.API, apiOptionsBuilder.build())
					.addConnectionCallbacks(mConnectionCallbacks)
					.addOnConnectionFailedListener(mConnectionFailedListener)
					.build();

			mApiClient.connect();
		} catch (Exception e) {
			Log.e(TAG, "Failed launchReceiver", e);
		}
	}

	/**
	 * Custom message channel
	 */
	class ChessChannel implements Cast.MessageReceivedCallback {

		/**
		 * @return custom namespace
		 */
		public String getNamespace() {
			return "urn:x-cast:nl.jwtc.chess.channel";
		}

		/*
         * Receive message from the receiver app
         */
		@Override
		public void onMessageReceived(CastDevice castDevice, String namespace,
									  String message) {
			//Log.d(TAG, "onMessageReceived: " + message);
		}

	}

	/**
	 * Tear down the connection to the receiver
	 */
	private void teardown(boolean selectDefaultRoute) {
		Log.d(TAG, "teardown");
		if (mApiClient != null) {
			if (mApplicationStarted) {
				if (mApiClient.isConnected() || mApiClient.isConnecting()) {
					try {
						Cast.CastApi.stopApplication(mApiClient, mSessionId);
						if (mChessChannel != null) {
							Cast.CastApi.removeMessageReceivedCallbacks(
									mApiClient,
									mChessChannel.getNamespace());
							mChessChannel = null;
						}
					} catch (IOException e) {
						Log.e(TAG, "Exception while removing channel", e);
					}
					mApiClient.disconnect();
				}
				mApplicationStarted = false;
			}
			mApiClient = null;
		}
		if (selectDefaultRoute) {
			mMediaRouter.selectRoute(mMediaRouter.getDefaultRoute());
		}
		mSelectedDevice = null;
		mWaitingForReconnect = false;
		mSessionId = null;
	}

	public static String get_ssActivity(){
		return _ssActivity;
	}

}
