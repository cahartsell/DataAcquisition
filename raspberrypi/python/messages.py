import CircularBuffer

MAX_FRAME_SIZE          = 1024
DELIMITER_SIZE          = 4

# Responses
NULL_TERM               = 0x00000000
FILE_NAMES_HDR          = 0x00000001
DATA_FILE_NAME          = 0x00000002
LOG_FILE_NAME           = 0x00000003
PY_LOG_FILE_NAME        = 0x00000004
FILE_NAMES_FTR          = 0x00000005

# Requests
FILE_NAMES_REQ          = 0x00010001


# Scan circular byte buffer and find the first null terminator message delimiter
# Null term should be two 0 bytes together
def findNullTerm(circBuf):
    repeatedZeroCnt = 0

    for i in range(circBuf.getHead(), circBuf.getTail()):
        if circBuf.get(i) == 0x00:
            if repeatedZeroCnt >= (DELIMITER_SIZE - 1):
                # Found enough repeated zeros (NULL TERM). Return starting index.
                return i - (DELIMITER_SIZE - 1)
            else:
                repeatedZeroCnt += 1
        else:
            repeatedZeroCnt = 0

    return -1

# Read the message type header from a circular byte buffer
# Assumes that the next 4 bytes in the buffer are the type delimiter. Otherwise, type will be invalid.
# Type bytes should be in BIG_ENDIAN form
def readTypeHeader(circBuf):
    head = circBuf.getHead()
    tempBuf = [circBuf.get(head), circBuf.get(head + 1), circBuf.get(head + 2), circBuf.get(head + 3)]

    return int.from_bytes(tempBuf, byteorder='big')

# Read the data portion of message from a circular byte buffer
# Assumes that valid data starts at the beginning of the buffer
# If no NULL_TERM is present, returns null
def readMsgData(circBuf):
    # Find NULL_TERM
    termStartIdx = findNullTerm(circBuf)
    if termStartIdx < 0:
        return None

    # Read data portion of message
    tempBuf = circBuf.readToIdx(termStartIdx)
    return tempBuf

# Interpret an array of bytes as a UTF-8 encoded string
# Return empty string if any error is encountered
def interpretAsUTF8(buf):
    tempStr = buf.decode("utf-8")
    return tempStr

# Convenience function - read message type header and remove it from buffer
def popTypeHeader(circBuf):
    type = readTypeHeader(circBuf)
    circBuf.popSize(DELIMITER_SIZE)
    return type

# Convenience function - read message data and remove it from buffer (including NULL_TERM)
def popMsgData(circBuf):
    # Read data portion of message
    buf = readMsgData(circBuf)

    # Remove data and NULL_TERM from buffer
    if buf is not None:
        circBuf.removeFromStart(len(buf))
        circBuf.removeFromStart(DELIMITER_SIZE)

    return buf

# Convenience function - combines getting message data and decoding UTF-8
def popMsgDataAsUTF8(circBuf):
    dataStr = ""
    buf = popMsgData(circBuf)
    if buf is not None:
        dataStr = interpretAsUTF8(buf)

    return dataStr

# Check if buffer contains a valid message
def hasValidMessage(circBuf):
    if circBuf.usedSpace() < DELIMITER_SIZE:
        return False

    # Some messages are type header only
    # Other messages contain data and should end with a NULL_TERM
    # Consider both cases
    type = readTypeHeader(circBuf)

    # Type header only messages
    if type == FILE_NAMES_HDR or type == FILE_NAMES_FTR or type == FILE_NAMES_REQ:
        return True

    # Data messages
    else:
        nullTermIdx = findNullTerm(circBuf)
        if nullTermIdx < 0:
            return False
        else:
            return True
