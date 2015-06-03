package com.switcherooboard.android;

import android.app.Fragment;
import android.os.Bundle;

public class SwitcherooFragment extends Fragment implements ISwitcherooCallback {

    private Switcheroo mSwitcheroo;

    /* Fragment */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRetainInstance(true);

        this.mSwitcheroo = this.getActivity().getIntent().getParcelableExtra(Switcheroo.EXTRA_SWITCHEROO);
        this.mSwitcheroo.connect(this);
    }

    /* ISwitcherooCallback */

    @Override
    public void onSwitcherooConnected() {
        ((MainActivity) this.getActivity()).onSwitcherooConnected();
    }

    @Override
    public void onSwitcherooDisconnected() {
        ((MainActivity) this.getActivity()).onSwitcherooDisconnected();
    }

    /* */

    protected String getAddress() {
      return this.mSwitcheroo.getAddress();
    }

    protected boolean flipRelay(final int index, final boolean state, final Integer duration) {
        return this.mSwitcheroo.flipRelay(index, state, duration);
    }

    protected void disconnect() {
        this.mSwitcheroo.disconnect();
    }

}
