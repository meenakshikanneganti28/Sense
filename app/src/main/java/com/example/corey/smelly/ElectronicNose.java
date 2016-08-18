package com.example.corey.smelly;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

// Class that connects to the sensor and collects the data
public class ElectronicNose {
    private static final String TAG = "Enose";
    private static final UUID ez = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard //SerialPortService ID

    private String address;
    private BluetoothDevice bd = null;
    private BluetoothSocket bs = null;

    private InputStream bIn;
    private OutputStream bOut;

    private ArrayList<String> data =  null;

    private byte[] buffer = null;

    public ElectronicNose(String addr) {

        this.address = new String(addr);

        buffer = new byte[4096];

    }

    private void sendCmd(String cmd) {
        if (cmd == null){
            return;
        }

        Log.d(TAG, String.format("S[%s]", cmd));
        try {
            byte[] ascii = cmd.getBytes("US-ASCII");
            bOut.write(ascii);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String getLine() throws IOException {
        int dex = 0;
        String s = null;

        while (true) {

            if(bIn.available() > 0){

                bIn.read(buffer, dex, 1);

                if(buffer[dex] == '\r'){
                    continue;
                }
                if(buffer[dex] == '\n'){
                    s = new String(buffer, 0, dex, "US-ASCII");
                    Log.d(TAG, "L[" + s + "]");
                    return s;
                }
                dex++;
            }

        } //end infinite loop
    }

    public void disconnect() {
        if (bs == null) {
            return;
        }
        if (bs.isConnected() == false) {
            return;
        }

        try {
            bs.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return;

    }

    public int connect() {

        if(bs != null && bs.isConnected()) {
            Log.d(TAG, "Tried to connect when already connected");
            return -1;
        }

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Log.d(TAG, "no bluetooth support!!!");
            return -1;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "Bluetooth not enabled!");
            return -1;
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                Log.d(TAG, "Paired with: " + device.getName() + "[" + device.getAddress() + "]");

            }
        }

        //Regardless of what was in the paired list
        //connect to our hardcoded address...
        bd = mBluetoothAdapter.getRemoteDevice(address); // use our hardcoded adapter (Adafruit EZ link)

        try {
            bs = bd.createInsecureRfcommSocketToServiceRecord(ez);
        } catch (IOException e) {
            Log.e(TAG, "create() failed", e);
            return -1;
        }

        Log.d(TAG, "Created a socket!");

        try {
            // This is a blocking call and will only return on a
            // successful connection or an exception
            bs.connect();
        } catch (IOException e) {
            // Close the socket
            try {
                bs.close();
            } catch (IOException e2) {
                Log.e(TAG, "unable to close() socket during connection failure", e2);
            }
            Log.d(TAG, "Failed to connect on socket");
            return -1;
        }


        Log.d(TAG, "connected!");

        try {
            bIn = bs.getInputStream();
            bOut = bs.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "IO sockets not created", e);
        }



        return 0;
    }

    public String getVersionString() {

        String response = null;
        try {
            bOut.write('v');

            response = getLine();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }

    public ArrayList<String> collectData()
    {
        ArrayList<String> data = new ArrayList<String>();

        String line = "";

        try {
            bOut.write('d');

            while (true) {

                line = getLine();

                if (line.startsWith("data=")) {
                    data.add(line);
                }
                else if (line.startsWith("cycle complete")){
                    break;
                }
                else {
                    Log.d(TAG, "Got unexpected data line:" + line);
                }

            } //end inf loop

            return data;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void setTimeParameter(String s) {
        sendCmd(s);

        try {
            String response = getLine();
            Log.d(TAG, String.format("Got param response [%s]", response));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setBaselineTime(int ms) {
        String cmd = String.format("q%d;", ms);
        setTimeParameter(cmd);
    }

    public void setSampleTime(int ms) {
        String cmd = String.format("w%d;", ms);
        setTimeParameter(cmd);
    }

    public void setPurgeTime(int ms) {
        String cmd = String.format("e%d;", ms);
        setTimeParameter(cmd);
    }

    public void setSettleTime(int ms) {
        String cmd = String.format("r%d;", ms);
        setTimeParameter(cmd);
    }

}
