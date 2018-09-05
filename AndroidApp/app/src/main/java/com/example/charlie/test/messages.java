package com.example.charlie.test;

public class messages {
    public static final int MAX_FRAME_SIZE          = 1024;
    public static final int DELIMITER_SIZE          = 4;

    public static final int NULL_TERM               = 0x00000000;
    public static final int FILE_NAMES_HDR          = 0x00000001;
    public static final int DATA_FILE_NAME          = 0x00000002;
    public static final int LOG_FILE_NAME           = 0x00000003;
    public static final int PY_LOG_FILE_NAME        = 0x00000004;
    public static final int FILE_NAMES_FTR          = 0x00000005;
}
