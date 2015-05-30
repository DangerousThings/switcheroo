package com.switcherooboard.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import java.util.Arrays;
import java.util.ArrayList;

public final class BondsActivity extends Activity implements OnItemClickListener {

  private static final String TAG = "BondsActivity";

  private GridView mGridView;

  private BondsAdapter mBondsAdapter;

  private ReadBondsTask mReadBondsTask;

  /* Activity */

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.bonds);

    ISwitcheroo switcheroo = this.getIntent().getParcelableExtra(ScanActivity.EXTRA_SWITCHEROO);

    this.mReadBondsTask = (ReadBondsTask) this.getLastNonConfigurationInstance();

    if (this.mReadBondsTask != null) {
      ArrayList<String> bonds = savedInstanceState.getStringArrayList("bonds");
      this.mBondsAdapter = new BondsAdapter(this, R.layout.bond_tile, bonds);
    } else {
      this.mReadBondsTask = new ReadBondsTask(switcheroo);
      this.mReadBondsTask.execute(null);
      this.mBondsAdapter = new BondsAdapter(this, R.layout.bond_tile);
    }

    this.mReadBondsTask.setBondsAdapter(this.mBondsAdapter);

    this.mGridView = (GridView) this.findViewById(R.id.grid);
    this.mGridView.setAdapter(mBondsAdapter);
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    ArrayList<String> bonds = this.mBondsAdapter.getItems(); 
    outState.putStringArrayList("bonds", bonds);
  }

  @Override
  public void onPause() {
    super.onPause();

    if (this.isFinishing()) {
      this.mReadBondsTask.cancel(true);
    }
  }

  @Override
  public Object onRetainNonConfigurationInstance() {
    this.mReadBondsTask.setBondsAdapter(null);
    return this.mReadBondsTask;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    if (this.isFinishing()) {
      this.mReadBondsTask.cancel(true);
    }
  }

  /* OnItemClickListener */

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
  }

  /* */

  private static final class BondsAdapter extends ArrayAdapter<String> {

    private final ArrayList<String> mObjects;

    public BondsAdapter(Context context, int resource) {
      this(context, resource, new ArrayList<String>());
    }

    public BondsAdapter(Context context, int resource, ArrayList<String> objects) {
      super(context, resource, 0, objects);
      this.mObjects = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      return super.getView(position, convertView, parent);
    }

    public ArrayList<String> getItems() {
      return this.mObjects;
    }

  }

  /* */

  private static final class Bond {

    public final Integer slot;

    public final String name;

    public Bond(Integer slot, String name) {
      this.slot = slot;
      this.name = name;
    }

  }

  private static final class ReadBondsTask extends AsyncTask<Void, Bond, Void> {

    private final ISwitcheroo mSwitcheroo;

    private BondsAdapter mBondsAdapter;

    public ReadBondsTask(ISwitcheroo switcheroo) {
      this.mSwitcheroo = switcheroo;
    }

    public void setBondsAdapter(BondsAdapter adapter) {
      this.mBondsAdapter = adapter;
    }

    public Void doInBackground(Void... params) {
      for (int i = 0; i < 10; i++) {
        Bond value = new Bond(0, "admin"); // TODO
        this.publishProgress(value);
      }

      return null;
    }

    public void onProgressUpdate(Bond... bonds) {
      for (Bond bond : bonds) {
        this.mBondsAdapter.insert(bond.name, bond.slot);
      }
    }

  }

}
