package de.uvwxy.snappy;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class Snappy extends Activity {
	MyCamera myCamera;
	SurfaceView myCameraPreview;
	Button btnStart;
	Button btnStop;
	EditText etSeconds;
	Timer timer;
	TimerTask task;
	TextView tvInfo;
	CheckBox cbPreview;
	RadioGroup rdbgFocus;
	RadioButton rdbAuto;
	RadioButton rdbInfinity;
	RadioButton rdbMacro;
	Focus focus;

	boolean app_was_run = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		myCameraPreview = (SurfaceView) findViewById(R.id.svMyCameraPreview);
		btnStart = (Button) findViewById(R.id.btnStart);
		btnStop = (Button) findViewById(R.id.btnStop);
		btnStop.setEnabled(false);
		etSeconds = (EditText) findViewById(R.id.etSeconds);
		tvInfo = (TextView) findViewById(R.id.tvInfo);
		cbPreview = (CheckBox) findViewById(R.id.cbPreview);
		rdbgFocus = (RadioGroup) findViewById(R.id.rdbgFocus);
		rdbAuto = (RadioButton) findViewById(R.id.rdbAuto);
		rdbInfinity = (RadioButton) findViewById(R.id.rdbInfinity);
		rdbMacro = (RadioButton) findViewById(R.id.rdbMacro);

		myCamera = new MyCamera("snappy", myCameraPreview.getHolder(), tvInfo, cbPreview);
		

		OnClickListener onClick = new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (arg0.equals(btnStart)) {
					// Check focus mode
					if (rdbAuto.isChecked()) {
						myCamera.setFocus(Focus.AUTO);
					} else if (rdbInfinity.isChecked()) {
						myCamera.setFocus(Focus.INFINITY);
					} else if (rdbMacro.isChecked()) {
						myCamera.setFocus(Focus.MACRO);
					} else {
						longToast("Please select Focus mode!");
						return;			// dont go any further on no selection!
					}
					// Disable/Enable interface
					btnStop.setEnabled(true);
					btnStart.setEnabled(false);
					etSeconds.setEnabled(false);

					tvInfo.setText("Timer: started");
					app_was_run = true;
					// always do preview at start
					try {
						myCamera.open();

					} catch (IOException e1) {
						Log.i("SNAPPY", "Error opening: " + e1);
					}

					// Start Timer
					timer = new Timer("Snapper", false);
					task = new TimerTask() {

						@Override
						public void run() {
							Log.i("SNAPPY", "Timer RUN!");
							if (cbPreview.isChecked()) {
								// if camera is not open: open it
								if (!myCamera.isOpen) {
									try {
										myCamera.close();
										myCamera.open();
									} catch (IOException e) {
										Log.i("SNAPPY", "Error: " + e.getMessage());
									}
								}
								// else just snap picture
								myCamera.snap();
								// preview is called when image is ready from
								// camera
							} else {
								// no preview, just open and snap
								try {
									myCamera.close();
									myCamera.open();
									myCamera.snap();
								} catch (IOException e) {
									Log.i("SNAPPY", "Error: " + e.getMessage());
								}
							}
						}
					};
					timer.schedule(task, Long.parseLong(etSeconds.getText().toString()) * 1000,
							Long.parseLong(etSeconds.getText().toString()) * 1000);
				} else if (arg0.equals(btnStop)) {
					// Disable/Enable interface
					btnStop.setEnabled(false);
					btnStart.setEnabled(true);
					etSeconds.setEnabled(true);
					tvInfo.setText("Timer: stopped");

					// Kill Timer
					timer.cancel();
					timer.purge();
					myCamera.close();
				}
			}
		};

		btnStart.setOnClickListener(onClick);
		btnStop.setOnClickListener(onClick);
	}

	public void onPause() {
		super.onPause();
		if (app_was_run)
			myCamera.close();
	}

	public void onDestroy() {
		super.onDestroy();
		if (app_was_run)
			myCamera.close();
	}
//	private void shortToast(String s) {
//		Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
//	}

	private void longToast(String s) {
		Toast.makeText(this, s, Toast.LENGTH_LONG).show();
	}
}