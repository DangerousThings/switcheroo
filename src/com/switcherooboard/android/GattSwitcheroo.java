package com.switcherooboard.android;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.UUID;

class GattSwitcheroo extends Switcheroo {
    private static final String TAG = "GattSwitcheroo";

    private static final UUID SERVICE_UUID = UUID(15);

    private int mState;

    private final BluetoothDevice mBluetoohDevice;

    private BluetoothGatt mBluetoothGatt;

    public GattSwitcheroo(BluetoothDevice device) {
        this.mBluetoohDevice = device;
    }

    /* ISwitcheroo */

    @Override
    public String getName() {
      return this.mBluetoohDevice.getName();
    }

    @Override
    public String getAddress() {
      return this.mBluetoohDevice.getAddress();
    }

    @Override
    public synchronized void connect(final ISwitcherooCallback callback) {
        if (this.mState != BluetoothProfile.STATE_DISCONNECTED) {
            throw new IllegalStateException();
        }

        this.mState = BluetoothProfile.STATE_CONNECTING;

        this.mBluetoothGatt = this.mBluetoohDevice.connectGatt(null, false, new BluetoothGattCallback() {
            public void onConnectionStateChange(BluetoothGatt _, int status, int newState) {
                android.util.Log.d(TAG, "onConnectionStateChange");

                GattSwitcheroo.this.mState = newState;

                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    android.util.Log.d(TAG, "onConnectionStateChange -> STATE_CONNECTED");
                    GattSwitcheroo.this.mBluetoothGatt.discoverServices();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    callback.onSwitcherooDisconnected();
                }
            }

            public void onServicesDiscovered(BluetoothGatt _, int status) {
                android.util.Log.d(TAG, "onServicesDiscovered");

                if (status == BluetoothGatt.GATT_SUCCESS) {
                    android.util.Log.d(TAG, "onServicesDiscovered -> GATT_SUCCESS");
                    callback.onSwitcherooConnected();
                }
            }
        });
    }

    @Override
    public boolean flipRelay(final int index, final boolean state, Integer duration) {
        if (index < 0 || index > 3) throw new IllegalArgumentException();

        final UUID uuid = UUID( 21 + index );
        final BluetoothGattCharacteristic characteristic = this.mBluetoothGatt.getService(SERVICE_UUID).getCharacteristic(uuid);

        final byte[] data = new byte[2];
        data[0] = (state == false) ? (byte) 0x00 : (byte) 0x01;
        data[1] = (duration == null) ? (byte) 0x00 : (byte) duration.intValue();

        characteristic.setValue(data);

        return this.mBluetoothGatt.writeCharacteristic(characteristic);
    }

    @Override
    public synchronized void disconnect() {
        if (this.mState != BluetoothProfile.STATE_CONNECTED) {
            throw new IllegalStateException();
        }

        this.mState = BluetoothProfile.STATE_DISCONNECTING;

        this.mBluetoothGatt.disconnect();
    }

    /* Parcelable */

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        if (this.mState != BluetoothProfile.STATE_DISCONNECTED) {
            throw new IllegalStateException();
        }

        parcel.writeParcelable(this.mBluetoohDevice, flags);
    }

    public static final Parcelable.Creator<GattSwitcheroo> CREATOR = new Parcelable.Creator<GattSwitcheroo>() {
        public GattSwitcheroo createFromParcel(Parcel in) {
            BluetoothDevice device = (BluetoothDevice) in.readParcelable(BluetoothDevice.class.getClassLoader());
            return new GattSwitcheroo(device);
        }

        public GattSwitcheroo[] newArray(int size) {
            return new GattSwitcheroo[size];
        }
    };

    /* */

    private static UUID UUID(int i) {
        if ((i != 15) && (i < 21 || i > 30)) throw new IllegalArgumentException();
        return UUID.fromString(String.format("000000%02d-9d7a-4919-b570-3bb24a4bf68e", i));
    }

}
