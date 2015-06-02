package com.switcherooboard.android;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;

public class ScanActivity extends Activity implements OnItemClickListener {

  private static final String TAG = "ScanActivity";

  private GridView mGridView;

  private ResultsAdapter mResultsAdapter;

  private ScanTask mScanTask;

  /* Activity */

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.scan);

    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

    this.mScanTask = (ScanTask) this.getLastNonConfigurationInstance();

    if (this.mScanTask != null) {
      ArrayList<Result> results = savedInstanceState.getParcelableArrayList("results");
      this.mResultsAdapter = new ResultsAdapter(this, android.R.layout.simple_list_item_2, results);
    } else {
      this.mScanTask = new ScanTask(adapter);
      this.mScanTask.execute(null);
      this.mResultsAdapter = new ResultsAdapter(this, android.R.layout.simple_list_item_2);
    }

    this.mScanTask.setResultsAdapter(this.mResultsAdapter);

    this.mGridView = (GridView) this.findViewById(R.id.grid);
    this.mGridView.setAdapter(mResultsAdapter);

    this.mGridView.setOnItemClickListener(this);
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    ArrayList<Result> results = this.mResultsAdapter.getItems(); 
    outState.putParcelableArrayList("results", results);
  }

  @Override
  public void onPause() {
    super.onPause();

    if (this.isFinishing()) {
      this.mScanTask.cancel(true);
    }
  }

  @Override
  public Object onRetainNonConfigurationInstance() {
    this.mScanTask.setResultsAdapter(null);
    return this.mScanTask;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    if (this.isFinishing()) {
      this.mScanTask.cancel(true);
    }
  }

  /* */

  public static final String EXTRA_SWITCHEROO = "com.switcherooboard.android.extra.SWITCHEROO";

  /* OnItemClickListener */

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    Intent intent = new Intent(ScanActivity.this, MainActivity.class);

    final Result result = (Result) parent.getAdapter().getItem(position);
    intent.putExtra(ScanActivity.EXTRA_SWITCHEROO, new GattSwitcheroo(result.device.getAddress()));

    this.startActivity(intent);
  }

  /* */

  private static final class ResultsAdapter extends ArrayAdapter<Result> {

    private final ArrayList<Result> mObjects;

    public ResultsAdapter(Context context, int resource) {
      this(context, resource, new ArrayList<Result>());
    }

    public ResultsAdapter(Context context, int resource, ArrayList<Result> objects) {
      super(context, resource, android.R.id.text1, objects);
      this.mObjects = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      View view = super.getView(position, convertView, parent);

      TextView text2 = (TextView) view.findViewById(android.R.id.text2);
      text2.setText(this.getItem(position).device.getAddress());

      return view;
    }

    public ArrayList<Result> getItems() {
      return this.mObjects;
    }

  }

  /* */

  private static final class Result implements Parcelable {

    public final BluetoothDevice device;

    public final int rssi;

    public final byte[] scanRecord;

    public Result(BluetoothDevice device, int rssi, byte[] scanRecord) {
      this.device = device;
      this.rssi = rssi;
      this.scanRecord = scanRecord;
    }

    @Override
    public String toString() {
      return this.device.getName();
    }

    @Override
    public boolean equals(Object object) {
      String address = Result.class.cast(object).device.getAddress();
      return this.device.getAddress().equals(address);
    }

    @Override
    public int describeContents() {
      return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
      dest.writeParcelable(this.device, flags);
      dest.writeInt(this.rssi);
      dest.writeInt(this.scanRecord.length);
      dest.writeByteArray(this.scanRecord);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Result> CREATOR = new Parcelable.Creator<Result>() {
      @Override
      public Result createFromParcel(Parcel in) {
        BluetoothDevice device = (BluetoothDevice) in.readParcelable(BluetoothDevice.class.getClassLoader());

        int rssi = in.readInt();

        byte[] scanRecord = new byte[in.readInt()];
        in.readByteArray(scanRecord);

        return new Result(device, rssi, scanRecord);
      }

      @Override
      public Result[] newArray(int size) {
        return new Result[size];
      }
    };

  }

  private static final class ScanTask extends AsyncTask<Void, Result, Void> implements BluetoothAdapter.LeScanCallback {

    private final BluetoothAdapter mBluetoothAdapter;

    private ResultsAdapter mResultsAdapter;

    public ScanTask(BluetoothAdapter adapter) {
      this.mBluetoothAdapter = adapter;
    }

    public void setResultsAdapter(ResultsAdapter adapter) {
      this.mResultsAdapter = adapter;
    }

    @Override
    public Void doInBackground(Void... params) {
      this.mBluetoothAdapter.startLeScan(this);

      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }

      this.mBluetoothAdapter.stopLeScan(this);

      return null;
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
      Result value = new Result(device, rssi, scanRecord);
      this.publishProgress(value);
    }

    @Override
    public void onProgressUpdate(Result... results) {
      if (!this.mResultsAdapter.getItems().contains(results[0])) {
        this.mResultsAdapter.add(results[0]);
      }
    }

  }

}
