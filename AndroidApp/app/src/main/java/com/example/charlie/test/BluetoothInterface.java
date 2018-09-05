package com.example.charlie.test;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.CircularArray;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.List;

public class BluetoothInterface {
    private static final String TAG = "BluetoothInterface";
    public static final String ACTION_FILENAMES_UPDATED = "bluetooth_interface.action.FILENAMES_UPDATED";
    private SingletonBluetoothData sBTData;
    private List<String> dataFileNames, logFileNames, pyLogFileNames;

    BluetoothInterface() {
        sBTData = SingletonBluetoothData.getInstance();
        dataFileNames = new ArrayList<String>();
        logFileNames = new ArrayList<String>();
        pyLogFileNames = new ArrayList<String>();
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

    int listen() {
        if (sBTData.inStream == null) {
            return -1;
        }

        // Max size of Bluetooth packet should be <1024. Add one byte for null term
        // May need to store up to 2 * MAX_FRAME_SIZE in case message delimiters are cutoff by BT packet size
        byte[] buf = new byte[1025];
        CircularArray<Byte> circBuf = new CircularArray<Byte>(messages.MAX_FRAME_SIZE * 2);
        int size, msgType;
        boolean receivingNames = false;
        String filename;

        // Thread loop
        while(true) {
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

            // If a valid message is not available, wait for more data
            if (!hasValidMessage(circBuf)) {
                continue;
            }

            // Do something based on received message type
            msgType = popTypeHeader(circBuf);
            Log.d(TAG, "MSG_TYPE: " + Integer.toString(msgType));
            switch (msgType) {
                case messages.FILE_NAMES_HDR:
                    Log.i(TAG, "Receiving file names...");
                    dataFileNames.clear();
                    logFileNames.clear();
                    pyLogFileNames.clear();
                    receivingNames = true;
                    break;

                case messages.DATA_FILE_NAME:
                    // Only accept this message if we are currently receiving filenames
                    if (!receivingNames) {
                        Log.w(TAG, "Received unexpected DATA_FILE_NAME message type.");
                        break;
                    }

                    filename = popMsgDataAsUTF8(circBuf);
                    dataFileNames.add(filename);
                    Log.d(TAG, "Received DATA filename " + filename);
                    break;

                case messages.LOG_FILE_NAME:
                    // Only accept this message if we are currently receiving filenames
                    if (!receivingNames) {
                        Log.w(TAG, "Received unexpected LOG_FILE_NAME message type.");
                        break;
                    }

                    filename = popMsgDataAsUTF8(circBuf);
                    logFileNames.add(filename);
                    Log.d(TAG, "Received LOG filename " + filename);
                    break;

                case messages.PY_LOG_FILE_NAME:
                    // Only accept this message if we are currently receiving filenames
                    if (!receivingNames) {
                        Log.w(TAG, "Received unexpected PY_LOG_FILE_NAME message type.");
                        break;
                    }

                    filename = popMsgDataAsUTF8(circBuf);
                    pyLogFileNames.add(filename);
                    Log.d(TAG, "Received PY_LOG filename " + filename);
                    break;

                case messages.FILE_NAMES_FTR:
                    // Done receiving
                    Log.i(TAG, "Finished receiving filenames.");
                    receivingNames = false;

                    // Send local broadcast
                    Intent intent = new Intent(BluetoothInterface.ACTION_FILENAMES_UPDATED);
                    LocalBroadcastManager.getInstance(MyApplication.get_instance()).sendBroadcast(intent);

                default:
                    Log.w(TAG, "Received message with unrecognized type");
                    break;
            }
        }
        Log.i(TAG, "Exiting listener function.");
        return 0;
    }

    // Scan circular byte buffer and find the first null terminator message delimiter
    // Null term should be two 0 bytes together
    private static int findNullTerm(CircularArray<Byte> circBuf) {
        int i, repeatedZeroCnt = 0;

        for (i = 0; i < circBuf.size(); i++) {
            if (circBuf.get(i) == 0x00){
                if (repeatedZeroCnt >= (messages.DELIMITER_SIZE - 1)) {
                    // Found enough repeated zeros (NULL TERM). Return starting index.
                    return (i - (messages.DELIMITER_SIZE - 1));
                } else {
                    repeatedZeroCnt++;
                }
            } else {
                repeatedZeroCnt = 0;
            }
        }

        return -1;
    }

    // Read the message type header from a circular byte buffer
    // Assumes that the next 4 bytes in the buffer are the type delimiter. Otherwise, type will be invalid.
    // Type bytes should be in BIG_ENDIAN form
    private static int readTypeHeader(CircularArray<Byte> circBuf) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4);
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        byteBuffer.put(circBuf.get(0));
        byteBuffer.put(circBuf.get(1));
        byteBuffer.put(circBuf.get(2));
        byteBuffer.put(circBuf.get(3));
        byteBuffer.flip();
        return byteBuffer.getInt();
    }

    // Read the data portion of message from a circular byte buffer
    // Assumes that valid data starts at the beginning of the buffer
    // If no NULL_TERM is present, returns null
    private static byte[] readMsgData(CircularArray<Byte> circBuf) {
        // Find NULL_TERM
        int termStartIdx = findNullTerm(circBuf);
        if (termStartIdx < 0) {
            return null;
        }

        // Read data portion of message
        byte[] buf = new byte[termStartIdx];
        for (int i = 0; i < termStartIdx; i++){
            buf[i] = circBuf.get(i);
        }

        return buf;
    }

    // Interpret an array of bytes as a UTF-8 encoded string
    // Return empty string if any error is encountered
    private static String interpretAsUTF8(byte[] buf) {
        String data;
        try {
            data = new String(buf, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Error interpreting String as UTF-8");
            Log.e(TAG, e.getMessage());
            data = "";
        }
        return data;
    }

    // Convenience function - read message type header and remove it from buffer
    private static int popTypeHeader(CircularArray<Byte> circBuf) {
        int type = readTypeHeader(circBuf);
        circBuf.removeFromStart(messages.DELIMITER_SIZE);
        return type;
    }

    // Convenience function - read message data and remove it from buffer (including NULL_TERM)
    private static byte[] popMsgData(CircularArray<Byte> circBuf) {
        // Read data portion of message
        byte[] buf = readMsgData(circBuf);

        // Remove data and NULL_TERM from buffer
        if (buf != null) {
            circBuf.removeFromStart(buf.length);
            circBuf.removeFromStart(messages.DELIMITER_SIZE);
        }

        return buf;
    }

    // Convenience function - combines getting message data and decoding UTF-8
    private static String popMsgDataAsUTF8(CircularArray<Byte> circBuf) {
        byte[] buf;
        String dataStr = "";

        buf = popMsgData(circBuf);
        if (buf != null) {
            dataStr = interpretAsUTF8(buf);
        }

        return dataStr;
    }

    private static boolean hasValidMessage(CircularArray<Byte> circBuf) {
        if (circBuf.size() < messages.DELIMITER_SIZE) {
            return false;
        }

        // Some messages are type header only
        // Other messages contain data and should end with a NULL_TERM
        // Consider both cases
        int type = readTypeHeader(circBuf), nullTermIdx;
        switch (type) {
            // Type header only messages
            case messages.FILE_NAMES_HDR:
            case messages.FILE_NAMES_FTR:
                    return true;

            // Data messages
            default:
                nullTermIdx = findNullTerm(circBuf);
                if (nullTermIdx < 0) {
                    return false;
                } else {
                    return true;
                }
        }
    }

    void getFilenames() {

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
