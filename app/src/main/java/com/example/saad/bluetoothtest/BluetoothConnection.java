package com.example.saad.bluetoothtest;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.UUID;

public class BluetoothConnection {

    private final UUID SECUREUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private final String APPID = "BLUETOOTHTEST";

    private Context context;
    private ProgressDialog progressDialog;

    private ConnectThread connectThread;
    private AcceptThread acceptThread;
    private ConnectedThread connectedThread;

    //Used to create connections
    private BluetoothAdapter bluetoothAdapter;

    public BluetoothConnection(Context context) {
        this.context = context;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    /*
    * This thread is waiting for incoming connections. This represents the server-side of the application
    * */
    private class AcceptThread extends Thread {

        BluetoothServerSocket bluetoothServerSocket;
        BluetoothSocket bluetoothSocket;

        public AcceptThread(){
            try {
                bluetoothServerSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APPID, SECUREUUID);
            } catch (Exception e){ e.printStackTrace(); }
        }

        @Override
        public void run(){
            try{
                System.out.println("OPENING SERVER SIDE CONNECTION");
                bluetoothSocket = bluetoothServerSocket.accept();
                if(bluetoothSocket != null){
                    getConnectedData(bluetoothSocket);
                }
            } catch (Exception e) { e.printStackTrace(); }
        }

        public void killSearch(){
            try {
                bluetoothServerSocket.close();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket bluetoothSocket;
        private BluetoothDevice device;

        public ConnectThread(BluetoothDevice bDevice){
            device = bDevice;
            try {
                BluetoothDevice bonded = (BluetoothDevice) bluetoothAdapter.getBondedDevices().toArray()[0];
                BluetoothDevice actual = bluetoothAdapter.getRemoteDevice(bonded.getAddress());
                bluetoothSocket = actual.createRfcommSocketToServiceRecord(SECUREUUID);
                System.out.println("are you kidding: " + actual.getName());
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void run(){

            bluetoothAdapter.cancelDiscovery();

            try {
                System.out.println("OPENING CLIENT SIDE CONNECTION");
                System.out.println("ATTEMPTING TO CONNECT TO: " + bluetoothSocket.getRemoteDevice().getName());
                bluetoothSocket.connect();
            } catch (Exception e) {
                e.printStackTrace();
                /*
                try {
                    bluetoothSocket = (BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(device, 1);
                    bluetoothSocket.connect();
                } catch (Exception e2) {
                    e.printStackTrace();
                    e2.printStackTrace();
                }
                */
            }

            if(bluetoothSocket != null) {
                getConnectedData(bluetoothSocket);
            }
        }

        public void killSearch(){
            try{
                bluetoothSocket.close();
            } catch (Exception e){ e.printStackTrace(); }
        }
    }

    /*
    * This will start the thread that will accept connections
    * */
    public void startAccept(BluetoothDevice bluetoothDevice){

        if(connectThread != null) {
            connectThread.killSearch();
        }

        //Open accept thread
        if(acceptThread == null){
            acceptThread = new AcceptThread();
            acceptThread.start();
        }
    }

    public void startClient(BluetoothDevice device, UUID uuid){
        if(acceptThread != null) {
            acceptThread.killSearch();
        }
        connectThread = new ConnectThread(device);
        connectThread.start();
    }

    /*
     * Send and receive data through this thread. Maintain connection
     **/
    public class ConnectedThread extends Thread{
        private BluetoothSocket bluetoothSocket;
        private InputStream inStream;
        private OutputStream outStream;

        public ConnectedThread(BluetoothSocket bluetoothSocket){
            this.bluetoothSocket = bluetoothSocket;
            try {
                inStream = bluetoothSocket.getInputStream();
                outStream = bluetoothSocket.getOutputStream();
            } catch(Exception e){ e.printStackTrace(); }
        }

        public void run(){
            byte[] buffer = new byte[1024];

            int bytes;

            while (true) {
                try {
                    System.out.println("imhereboi");
                    bytes = inStream.read(buffer);
                    System.out.println("almostthere");
                    String incomingMessage = new String(buffer, 0, bytes);
                    System.out.println("CANT STOP NOW");
                    System.out.println("Incoming Message: " + incomingMessage);
                } catch (IOException e) {
                    break;
                }
            }

        }

        public void write(byte[] bytes){
            try {
                outStream.write(bytes);
            } catch(Exception e){
                e.printStackTrace();
            }
        }

        public void cancel(){
            try {
                bluetoothSocket.close();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void getConnectedData(BluetoothSocket socket){
        System.out.println("CONNECTED");
        connectedThread = new ConnectedThread(socket);
        connectedThread.start();
    }

    public void write(byte[] bytes){
        connectedThread.write(bytes);
    }

}
