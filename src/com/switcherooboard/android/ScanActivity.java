package com.switcherooboard.android;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import java.util.ArrayList;

public class ScanActivity extends Activity implements OnItemClickListener {

  private static final String TAG = "ScanActivity";

  private BluetoothAdapter mBluetoothAdapter;

  private GridView mGridView;

  private SwitcherooScan mSwitcherooScan;

  private ArrayAdapter mArrayAdapter;

  /* Activity */

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.scan);

    this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    this.mSwitcherooScan = (SwitcherooScan) this.getLastNonConfigurationInstance();

    if (this.mSwitcherooScan == null) {
      this.mSwitcherooScan = new SwitcherooScan();
      this.mBluetoothAdapter.startLeScan(this.mSwitcherooScan);
    }

    this.mSwitcherooScan.setDataSetObserver(new DataSetObserver() {
      @Override
      public void onChanged() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
          public void run() {
            ScanActivity.this.mArrayAdapter.notifyDataSetChanged();
          }
        });
      }

      @Override
      public void onInvalidated() {
      }
    });

    this.mArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, this.mSwitcherooScan.getResultsList());

    this.mGridView = (GridView) this.findViewById(R.id.gridview);

    this.mGridView.setAdapter(this.mArrayAdapter);

    this.mGridView.setOnItemClickListener(this);
  }

  @Override
  public Object onRetainNonConfigurationInstance() {
    this.mSwitcherooScan.setDataSetObserver(null);
    return this.mSwitcherooScan;
  }

  @Override
  public void onPause() {
    super.onPause();

    if (this.isFinishing()) {
      this.mBluetoothAdapter.stopLeScan(this.mSwitcherooScan);
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    if (this.isFinishing()) {
      this.mBluetoothAdapter.stopLeScan(this.mSwitcherooScan);
    }
  }

  /* */

  public static final String EXTRA_SWITCHEROO = "com.switcherooboard.android.extra.SWITCHEROO";

  /* OnItemClickListener */

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    Intent intent = new Intent(ScanActivity.this, MainActivity.class);

    final BluetoothDevice device = (BluetoothDevice) parent.getAdapter().getItem(position);
    intent.putExtra(ScanActivity.EXTRA_SWITCHEROO, new GattSwitcheroo(device.getAddress()));

    this.startActivity(intent);
  }

  /* */

  private static class SwitcherooScan implements BluetoothAdapter.LeScanCallback {

    private final ArrayList<BluetoothDevice> mResultsList = new ArrayList<BluetoothDevice>();

    private DataSetObserver mDataSetObserver;

    private Handler mHandler;

    public SwitcherooScan() {
      new Thread(new Runnable() {
        public void run() {
          Looper.prepare();
          SwitcherooScan.this.mHandler = new Handler();
          Looper.loop();
        }
      }).start();
    }

    public ArrayList<BluetoothDevice> getResultsList() {
      return mResultsList;
    }

    public void setDataSetObserver(DataSetObserver observer) {
      this.mDataSetObserver = observer;
    }

    @Override
    public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
      android.util.Log.d(TAG, "onLeScan" + " -> " + "\"" + device.getName() + "\"");

      if (device.getName().equals("Switcheroo")) {
        this.mHandler.post(new Runnable() {
          @Override
          public void run() {
            if (!SwitcherooScan.this.mResultsList.contains(device)) {
              SwitcherooScan.this.mResultsList.add(device);

              if (SwitcherooScan.this.mDataSetObserver != null) {
                SwitcherooScan.this.mDataSetObserver.onChanged();
              }
            }
          }
        });
      }
    }
  }

}
