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

  private ScanAdapter mScanAdapter;

  private ScanTask mScanTask;

  /* Activity */

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.scan);

    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

    this.mScanTask = (ScanTask) this.getLastNonConfigurationInstance();

    if (this.mScanTask != null) {
      ArrayList<Switcheroo> results = savedInstanceState.getParcelableArrayList("results");
      this.mScanAdapter = new ScanAdapter(this, android.R.layout.simple_list_item_2, results);
    } else {
      this.mScanTask = new ScanTask(adapter);
      this.mScanTask.execute(null);
      this.mScanAdapter = new ScanAdapter(this, android.R.layout.simple_list_item_2);
    }

    this.mScanTask.setScanAdapter(this.mScanAdapter);

    this.mGridView = (GridView) this.findViewById(R.id.grid);
    this.mGridView.setAdapter(mScanAdapter);

    this.mGridView.setOnItemClickListener(this);
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    ArrayList<Switcheroo> results = this.mScanAdapter.getItems();
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
    this.mScanTask.setScanAdapter(null);
    return this.mScanTask;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    if (this.isFinishing()) {
      this.mScanTask.cancel(true);
    }
  }

  /* OnItemClickListener */

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    Intent intent = new Intent(ScanActivity.this, MainActivity.class);

    final Switcheroo switcheroo = (Switcheroo) parent.getAdapter().getItem(position);
    intent.putExtra(Switcheroo.EXTRA_SWITCHEROO, switcheroo);

    this.startActivity(intent);
  }

  /* */

  private static final class ScanAdapter extends ArrayAdapter<Switcheroo> {

    private final ArrayList<Switcheroo> mObjects;

    public ScanAdapter(Context context, int resource) {
      this(context, resource, new ArrayList<Switcheroo>());
    }

    public ScanAdapter(Context context, int resource, ArrayList<Switcheroo> objects) {
      super(context, resource, android.R.id.text1, objects);
      this.mObjects = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      View view = super.getView(position, convertView, parent);

      TextView text2 = (TextView) view.findViewById(android.R.id.text2);
      text2.setText(this.getItem(position).getAddress());

      return view;
    }

    public ArrayList<Switcheroo> getItems() {
      return this.mObjects;
    }

  }

  /* */

  private static final class ScanTask extends AsyncTask<Void, Switcheroo, Void> implements BluetoothAdapter.LeScanCallback {

    private final BluetoothAdapter mBluetoothAdapter;

    private ScanAdapter mScanAdapter;

    public ScanTask(BluetoothAdapter adapter) {
      this.mBluetoothAdapter = adapter;
    }

    public void setScanAdapter(ScanAdapter adapter) {
      this.mScanAdapter = adapter;
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
      this.publishProgress(new GattSwitcheroo(device));
    }

    @Override
    public void onProgressUpdate(Switcheroo... results) {
      if (!this.mScanAdapter.getItems().contains(results[0])) {
        this.mScanAdapter.add(results[0]);
      }
    }

  }

}
