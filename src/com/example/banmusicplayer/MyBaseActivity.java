package com.example.banmusicplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;

/**
 * 基础Activity
 * 
 * @author Ban
 * 
 */
public abstract class MyBaseActivity extends FragmentActivity {
	protected Activity activity = this;
	private Builder alertDialog;
	private ProgressDialog progressDialog;

	private OnClickListener positiveButtonListener = null;
	private OnClickListener negativeButtonListener = null;
	private MyRun myRun;
	
	private Handler handler = new Handler() {
		@SuppressWarnings("deprecation")
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				if (progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				break;
			case 1:
				progressDialog = new ProgressDialog(activity);
				progressDialog.setMessage(msg.obj.toString());
				if (msg.arg1 == 1) {
					progressDialog.setButton(
							getResources().getString(R.string.Sys_Cancel),
							new OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Message.obtain(handler, 0).sendToTarget();
								}
							});
				}
				progressDialog.setCancelable(false);
				progressDialog.setIndeterminate(false);
				progressDialog.show();
				break;
			case 2:
				OnClickListener[] a = (OnClickListener[]) msg.obj;
				if (a != null && a.length == 1) {
					alertDialog.setTitle(R.string.Sys_Warning)
							.setMessage(msg.arg1)
							.setPositiveButton(R.string.Sys_Yes, a[0]).show();
				} else {
					alertDialog.setTitle(R.string.Sys_Warning)
							.setMessage(msg.arg1)
							.setPositiveButton(R.string.Sys_Yes, null).show();
				}

				break;
			case 3:
				alertDialog.setTitle(R.string.Sys_Warning)
						.setMessage(msg.obj.toString())
						.setPositiveButton(R.string.Sys_Yes, null).show();

				break;
			case 4:
				OnClickListener[] b = (OnClickListener[]) msg.obj;

				alertDialog.setTitle(R.string.Sys_Warning).setMessage(msg.arg1)
						.setPositiveButton(R.string.Sys_Yes, b[0])
						.setNegativeButton(R.string.Sys_Cancel, null).show();

				break;
			case 5:

				alertDialog
						.setTitle(R.string.Sys_Warning)
						.setMessage(msg.obj.toString())
						.setPositiveButton(R.string.Sys_Yes,
								positiveButtonListener)
						.setNegativeButton(R.string.Sys_Cancel,
								negativeButtonListener).show();

				break;
			case 6:
				OnClickListener[] c = (OnClickListener[]) msg.obj;

				if (c != null && c.length == 1) {
					alertDialog.setTitle(msg.arg2).setMessage(msg.arg1)
							.setPositiveButton(R.string.Sys_Yes, c[0]).show();
				} else {
					alertDialog.setTitle(msg.arg2).setMessage(msg.arg1)
							.setPositiveButton(R.string.Sys_Yes, null).show();
				}

				break;
			case 7:
				if(myRun!=null){
					myRun.run();
				}
				
				break;
			default:
				break;
			}
		}
	};

	protected void onCreate(Bundle savedInstanceState, int layoutId) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		setContentView(layoutId);
		alertDialog = new AlertDialog.Builder(this);
		inti();
	}

	public void dismissProgressDialog() {
		Message.obtain(handler, 0).sendToTarget();
	}

	public void showProgressDialog(String message, boolean isShowCannelButton) {
		int showCannelButton = 0;
		if (isShowCannelButton) {
			showCannelButton = 1;
		}
		Message.obtain(handler, 1, showCannelButton, 0, message).sendToTarget();
	}

	public void showWarningMessage(int messageResoure,
			OnClickListener... listeners) {
		Message.obtain(handler, 2, messageResoure, 0, listeners).sendToTarget();
	}

	public void showWarningMessage(String messageResoure) {
		Message.obtain(handler, 3, messageResoure).sendToTarget();
	}

	public void showConfirmMessage(int messageResoure,
			OnClickListener... listeners) {
		Message.obtain(handler, 4, messageResoure, 0, listeners).sendToTarget();
	}

	public void showConfirmMessage(String messageResoure,
			OnClickListener... listeners) {
		positiveButtonListener = null;
		negativeButtonListener = null;

		if (listeners.length != 0) {
			positiveButtonListener = listeners[0];
			if (listeners.length > 1) {
				negativeButtonListener = listeners[1];
			}
		}
		Message.obtain(handler, 5, messageResoure).sendToTarget();
	}

	public void showTitleMessage(int messageResoure, int title,
			OnClickListener... listeners) {
		Message.obtain(handler, 6, messageResoure, title, listeners)
				.sendToTarget();
	}

	public void run(MyRun myRun) {
		this.myRun=myRun;
		Message.obtain(handler, 7).sendToTarget();
	}

	// public void runInBackgroundAndProgressDialog(){
	//
	// }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (progressDialog != null) {
			progressDialog.cancel();
		}
	}

	public void inti() {
		if (findViewById(R.id.back) != null) {
			android.view.View.OnClickListener listener = new android.view.View.OnClickListener() {
				@Override
				public void onClick(View v) {
					switch (v.getId()) {
					case R.id.back:
						finish();
						break;

					default:
						break;
					}
				}
			};
			findViewById(R.id.back).setOnClickListener(listener);
		}

		initView();
		initButton();
		initListView();
	}

	public abstract void initView();

	public abstract void initButton();

	public abstract void initListView();
	
	public interface MyRun{
		void run();
	}
}
