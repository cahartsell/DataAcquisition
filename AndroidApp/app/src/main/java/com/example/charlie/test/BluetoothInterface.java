package com.example.charlie.test;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.CircularArray;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class BluetoothInterface {
    private SingletonBluetoothData sBTData;
    private ArrayList<String> dataFileNames, logFileNames, pyLogFileNames;
    private ReentrantLock fileNamesLock;
    private boolean listening;

    // Constants
    private static final String TAG = "BluetoothInterface";
    public static final String ACTION_FILENAMES_UPDATED = "bluetooth_interface.action.FILENAMES_UPDATED";
    public static final String ACTION_FILE_DOWNLOAD_COMPLETE = "bluetooth_interface.action.FILE_DOWNLOAD_COMPLETE";
    public static final String ACTION_LISTENER_STOPPED = "bluetooth_interface.action.LISTENER_STOPPED";

    private static volatile BluetoothInterface sBluetoothInterface = new BluetoothInterface();

    private BluetoothInterface() {
        sBTData = SingletonBluetoothData.getInstance();
        dataFileNames = new ArrayList<String>();
        logFileNames = new ArrayList<String>();
        pyLogFileNames = new ArrayList<String>();
        fileNamesLock = new ReentrantLock();
        listening = false;
    }

    public static BluetoothInterface getInstance() {
        return sBluetoothInterface;
    }

    int sendString(String msg) {
        return send(msg.getBytes());
    }

    boolean connected() {
        return sBTData.socketConnected();
    }

    int send(byte[] data) {
        if (sBTData.outStream != null) {
            try {
                sBTData.outStream.write(data);
            } catch (IOException e) {
                Log.w(TAG, "Failed to write message to bluetooth output stream.");
                return -1;
            }
            return 0;
        }
        return -1;
    }

    int sendInt(int data) {
        ByteBuffer tempBuf = ByteBuffer.allocate(4);
        tempBuf.order(ByteOrder.BIG_ENDIAN);
        tempBuf.putInt(data);
        return send(tempBuf.array());
    }

    boolean is_listening() {
        return this.listening;
    }

    int listen() {
        if (sBTData.inStream == null) {
            return -1;
        }

        // Set listening flag
        listening = true;
        Log.i(TAG, "listener function started.");

        // Max size of Bluetooth packet should be <1024. Add one byte for null term
        // May need to store up to 2 * MAX_FRAME_SIZE in case message delimiters are cutoff by BT packet size
        byte[] buf = new byte[1025];
        byte[] tempBuf;
        CircularArray<Byte> circBuf = new CircularArray<Byte>(Messages.MAX_FRAME_SIZE * 2);
        int size, msgType, tempInt;
        int sequenceNum = 0;
        boolean receivingNames = false;
        boolean receivingFileData = false;
        String filename = null, filenameInProgress = null, tempStr = null;
        File outputFile = null;
        FileOutputStream outputFileStream = null;
        Intent intent = null;

        // Thread loop
        while(true) {
            // If a valid message is not available,  wait for more data
            // Otherwise, process existing message before receiving more data
            if (!Messages.hasValidMessage(circBuf)) {
                // Read data from BT input stream
                Log.d(TAG, "Listening...");
                try {
                    size = sBTData.inStream.read(buf);
                } catch (ClosedChannelException e) {
                    // FIXME: ClosedChannelException is not the right exception type
                    Log.i(TAG, "Bluetooth connection closed.");
                    break;
                } catch (IOException e) {
                    Log.e(TAG, "Exception reading data from bluetooth socket:");
                    Log.e(TAG, e.getMessage());
                    break;
                }

                // Append read buffer to circular buffer
                // TODO: Can this be done more efficiently?
                for (int i = 0; i < size; i++) {
                    circBuf.addLast(buf[i]);
                }
            }

            // It's possible we only received a partial message in last read.
            if (!Messages.hasValidMessage(circBuf)) {
                continue;
            }

            // Do something based on received message type
            msgType = Messages.popTypeHeader(circBuf);
            Log.d(TAG, "MSG_TYPE: " + Integer.toString(msgType));
            switch (msgType) {
                case Messages.FILE_NAMES_HDR:
                    Log.i(TAG, "Receiving file names...");
                    fileNamesLock.lock();
                    dataFileNames.clear();
                    logFileNames.clear();
                    pyLogFileNames.clear();
                    fileNamesLock.unlock();
                    receivingNames = true;
                    break;

                case Messages.DATA_FILE_NAME:
                    // Only accept this message if we are currently receiving filenames
                    if (!receivingNames) {
                        Log.w(TAG, "Received unexpected DATA_FILE_NAME message type.");
                        break;
                    }

                    filename = Messages.popMsgDataAsUTF8(circBuf);
                    fileNamesLock.lock();
                    dataFileNames.add(filename);
                    fileNamesLock.unlock();
                    Log.d(TAG, "Received DATA filename " + filename);
                    break;

                case Messages.LOG_FILE_NAME:
                    // Only accept this message if we are currently receiving filenames
                    if (!receivingNames) {
                        Log.w(TAG, "Received unexpected LOG_FILE_NAME message type.");
                        break;
                    }

                    filename = Messages.popMsgDataAsUTF8(circBuf);
                    fileNamesLock.lock();
                    logFileNames.add(filename);
                    fileNamesLock.unlock();
                    Log.d(TAG, "Received LOG filename " + filename);
                    break;

                case Messages.PY_LOG_FILE_NAME:
                    // Only accept this message if we are currently receiving filenames
                    if (!receivingNames) {
                        Log.w(TAG, "Received unexpected PY_LOG_FILE_NAME message type.");
                        break;
                    }

                    filename = Messages.popMsgDataAsUTF8(circBuf);
                    fileNamesLock.lock();
                    pyLogFileNames.add(filename);
                    fileNamesLock.unlock();
                    Log.d(TAG, "Received PY_LOG filename " + filename);
                    break;

                case Messages.FILE_NAMES_FTR:
                    // Done receiving
                    Log.i(TAG, "Finished receiving filenames.");
                    receivingNames = false;

                    // Send local broadcast
                    intent = new Intent(BluetoothInterface.ACTION_FILENAMES_UPDATED);
                    LocalBroadcastManager.getInstance(MyApplication.get_instance()).sendBroadcast(intent);
                    break;

                case Messages.FILE_NOT_FOUND:
                    String tempFilename = Messages.popMsgDataAsUTF8(circBuf);
                    Log.w(TAG, "Bluetooth device returned file not found for filename: ".concat(tempFilename));
                    break;

                case Messages.FILE_CONTENT_HDR:
                    // Set variables for receiving a new file
                    filenameInProgress = Messages.popMsgDataAsUTF8(circBuf);
                    receivingFileData = true;
                    sequenceNum = 0;
                    // Open file for writing
                    try {
                        outputFile = new File(MyApplication.get_instance().getExternalFilesDir(null), filenameInProgress);
                        outputFileStream = new FileOutputStream(outputFile, false);
                    } catch (Exception e) {
                        receivingFileData = false;
                        Log.e(TAG, e.getMessage());
                    }
                    break;

                // TODO: Need timeout method for long running operations like receiving file data
                case Messages.FILE_CONTENT_CHUNK:
                    // If we didn't expect to receive file data, discard message contents
                    if (!receivingFileData) {
                        Log.w(TAG, "Received unexpected file data chunk.");
                        Messages.popMsgData(circBuf);
                        break;
                    }
                    // First bytes in message content should be the sequence number. Same size as message delimiter
                    tempInt = Messages.popTypeHeader(circBuf);
                    // Remainder of the message (until NULL_TERM) is data
                    tempBuf = Messages.popMsgData(circBuf);
                    // If sequence numbers don't match, don't write data
                    if (tempInt != sequenceNum) { break; }
                    // Write data to file
                    try {
                        Log.i(TAG, "Writing data chunk to file.");
                        outputFileStream.write(tempBuf);
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                    sequenceNum++;
                    break;

                case Messages.FILE_CONTENT_FTR:
                    // If we didn't expect to receive file data, discard message contents
                    if (!receivingFileData) {
                        Log.w(TAG, "Received unexpected file data chunk.");
                        Messages.popMsgData(circBuf);
                        break;
                    }

                    // Close file and verify contents
                    try {
                        outputFileStream.close();
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                    // TODO: Hash checking would be better solution here
                    // First bytes in message content should be the sequence number. Same size as message delimiter
                    tempInt = Messages.popTypeHeader(circBuf);
                    // Remainder of the message (until NULL_TERM) is filename String
                    tempStr = Messages.popMsgDataAsUTF8(circBuf);
                    // Verify filename and total chunk count match expected values
                    if ((filenameInProgress == null) || !(tempStr.contentEquals(filenameInProgress))){
                        // TODO: Handle error
                        Log.w(TAG, "Filename received after download did not match expected.");
                    }
                    if (tempInt != sequenceNum){
                        // TODO: Handle error
                        Log.w(TAG, "File data chunk count received after download did not match expected.");
                    }

                    // Send broadcast to rest of app
                    // TODO: Attach filename and result (OK/FAILED/CORRUPTED) to intent
                    intent = new Intent(BluetoothInterface.ACTION_FILENAMES_UPDATED);
                    LocalBroadcastManager.getInstance(MyApplication.get_instance()).sendBroadcast(intent);
                    break;

                default:
                    Log.w(TAG, "Received message with unrecognized type");
                    break;
            }
        }

        // Un-Set listening flag
        listening = false;

        // Send shutdown notification to rest of app
        intent = new Intent(BluetoothInterface.ACTION_LISTENER_STOPPED);
        LocalBroadcastManager.getInstance(MyApplication.get_instance()).sendBroadcast(intent);
        Log.i(TAG, "Exiting listener function.");
        return 0;
    }

    void requestFilenames() {
        Log.i(TAG, "Requesting file names");
        sendInt(Messages.FILE_NAMES_REQ);
    }

    void requestFileContent(String filename) {
        Log.i(TAG, "Requesting file: ".concat(filename));
        ByteBuffer msgBuf = ByteBuffer.allocate(Messages.DELIMITER_SIZE * 2 + filename.length());
        msgBuf.order(ByteOrder.BIG_ENDIAN);
        msgBuf.putInt(Messages.FILE_CONTENT_REQ);
        msgBuf.put(filename.getBytes());
        msgBuf.putInt(Messages.NULL_TERM);
        this.send(msgBuf.array());
    }

    void getDataFileNames(ArrayList<String> dest) {
        fileNamesLock.lock();
        for (int i =0; i < dataFileNames.size(); i++){
            dest.add(dataFileNames.get(i));
        }
        //ArrayList<String> names = new ArrayList<String>(dataFileNames);
        fileNamesLock.unlock();
        //return names;
    }

    void getLogFileNames(ArrayList<String> dest) {
        fileNamesLock.lock();
        for (int i =0; i < logFileNames.size(); i++){
            dest.add(logFileNames.get(i));
        }
        //ArrayList<String> names = new ArrayList<String>(logFileNames);
        fileNamesLock.unlock();
        //return names;
    }

    void getPyLogFileNames(ArrayList<String> dest) {
        fileNamesLock.lock();
        for (int i =0; i < logFileNames.size(); i++){
            dest.add(pyLogFileNames.get(i));
        }
        fileNamesLock.unlock();
    }
}

// AsyncTask<Params, Progress, Result>
//class btTask extends AsyncTask<BluetoothInterface, Void, Integer> {
//    @Override
//    protected Integer doInBackground(BluetoothInterface... params) {
//        return params[0].listen();
//    }
//
//    @Override
//    protected void onPostExecute(Integer result) {
//        super.onPostExecute(result);
//    }
//}
