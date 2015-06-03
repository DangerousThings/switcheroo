package com.switcherooboard.android;

import android.os.Parcelable;

abstract class Switcheroo implements Parcelable {
  public static final String EXTRA_SWITCHEROO = "com.switcherooboard.android.extra.SWITCHEROO";

  abstract String getName();
  abstract String getAddress();
  abstract void connect(final ISwitcherooCallback callback);
  abstract boolean flipRelay(final int index, final boolean state, Integer duration);
  abstract void disconnect();

  @Override
  public final String toString() {
    return this.getName();
  }

  @Override
  public final boolean equals(Object object) {
    String address = Switcheroo.class.cast(object).getAddress();
    return this.getAddress().equals(address);
  }

}
