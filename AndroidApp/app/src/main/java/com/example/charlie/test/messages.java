package com.example.charlie.test;

import android.support.v4.util.CircularArray;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Messages {
    // Various size attributes
    public static final int MAX_FRAME_SIZE          = 1024;
    public static final int DELIMITER_SIZE          = 4;

    // Responses
    public static final int NULL_TERM               = 0x00000000;
    public static final int FILE_NAMES_HDR          = 0x00000001;
    public static final int DATA_FILE_NAME          = 0x00000002;
    public static final int LOG_FILE_NAME           = 0x00000003;
    public static final int PY_LOG_FILE_NAME        = 0x00000004;
    public static final int FILE_NAMES_FTR          = 0x00000005;

    // Requests
    public static final int FILE_NAMES_REQ          = 0x00010001;

    // Scan circular byte buffer and find the first null terminator message delimiter
    // Null term should be two 0 bytes together
    public static int findNullTerm(CircularArray<Byte> circBuf) {
        int i, repeatedZeroCnt = 0;

        for (i = 0; i < circBuf.size(); i++) {
            if (circBuf.get(i) == 0x00){
                if (repeatedZeroCnt >= (Messages.DELIMITER_SIZE - 1)) {
                    // Found enough repeated zeros (NULL TERM). Return starting index.
                    return (i - (Messages.DELIMITER_SIZE - 1));
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
    public static int readTypeHeader(CircularArray<Byte> circBuf) {
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
    public static byte[] readMsgData(CircularArray<Byte> circBuf) {
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
    public static String interpretAsUTF8(byte[] buf) {
        String data;
        try {
            data = new String(buf, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            data = "";
        }
        return data;
    }

    // Convenience function - read message type header and remove it from buffer
    public static int popTypeHeader(CircularArray<Byte> circBuf) {
        int type = readTypeHeader(circBuf);
        circBuf.removeFromStart(Messages.DELIMITER_SIZE);
        return type;
    }

    // Convenience function - read message data and remove it from buffer (including NULL_TERM)
    public static byte[] popMsgData(CircularArray<Byte> circBuf) {
        // Read data portion of message
        byte[] buf = readMsgData(circBuf);

        // Remove data and NULL_TERM from buffer
        if (buf != null) {
            circBuf.removeFromStart(buf.length);
            circBuf.removeFromStart(Messages.DELIMITER_SIZE);
        }

        return buf;
    }

    // Convenience function - combines getting message data and decoding UTF-8
    public static String popMsgDataAsUTF8(CircularArray<Byte> circBuf) {
        byte[] buf;
        String dataStr = "";

        buf = popMsgData(circBuf);
        if (buf != null) {
            dataStr = interpretAsUTF8(buf);
        }

        return dataStr;
    }

    // Check if buffer contains a valid message
    public static boolean hasValidMessage(CircularArray<Byte> circBuf) {
        if (circBuf.size() < Messages.DELIMITER_SIZE) {
            return false;
        }

        // Some Messages are type header only
        // Other Messages contain data and should end with a NULL_TERM
        // Consider both cases
        int type = readTypeHeader(circBuf);
        int nullTermIdx;
        switch (type) {
            // Type header only Messages
            case Messages.FILE_NAMES_HDR:
            case Messages.FILE_NAMES_FTR:
                return true;

            // Data Messages
            default:
                nullTermIdx = findNullTerm(circBuf);
                if (nullTermIdx < 0) {
                    return false;
                } else {
                    return true;
                }
        }
    }
}
