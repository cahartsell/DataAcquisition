import CircularBuffer

MAX_FRAME_SIZE          = 1024
DELIMITER_SIZE          = 4
NETWORK_BYTE_ORDER      = 'big'

# Responses
NULL_TERM               = 0x00000000
FILE_NAMES_HDR          = 0x00000001
DATA_FILE_NAME          = 0x00000002
LOG_FILE_NAME           = 0x00000003
PY_LOG_FILE_NAME        = 0x00000004
FILE_NAMES_FTR          = 0x00000005
FILE_CONTENT_HDR        = 0x00000006
FILE_CONTENT_FTR        = 0x00000007
FILE_CONTENT_CHUNK      = 0x00000008
FILE_NOT_FOUND          = 0x00000009

# Requests
FILE_NAMES_REQ          = 0x00010001
FILE_CONTENT_REQ        = 0x00010002

################# Byte versions of message delimiters #######################
# Responses
NULL_TERM_B               = (0x00000000).to_bytes(DELIMITER_SIZE, byteorder=NETWORK_BYTE_ORDER)
FILE_NAMES_HDR_B          = (0x00000001).to_bytes(DELIMITER_SIZE, byteorder=NETWORK_BYTE_ORDER)
DATA_FILE_NAME_B          = (0x00000002).to_bytes(DELIMITER_SIZE, byteorder=NETWORK_BYTE_ORDER)
LOG_FILE_NAME_B           = (0x00000003).to_bytes(DELIMITER_SIZE, byteorder=NETWORK_BYTE_ORDER)
PY_LOG_FILE_NAME_B        = (0x00000004).to_bytes(DELIMITER_SIZE, byteorder=NETWORK_BYTE_ORDER)
FILE_NAMES_FTR_B          = (0x00000005).to_bytes(DELIMITER_SIZE, byteorder=NETWORK_BYTE_ORDER)
FILE_CONTENT_HDR_B        = (0x00000006).to_bytes(DELIMITER_SIZE, byteorder=NETWORK_BYTE_ORDER)
FILE_CONTENT_FTR_B        = (0x00000007).to_bytes(DELIMITER_SIZE, byteorder=NETWORK_BYTE_ORDER)
FILE_CONTENT_CHUNK_B      = (0x00000008).to_bytes(DELIMITER_SIZE, byteorder=NETWORK_BYTE_ORDER)
FILE_NOT_FOUND_B          = (0x00000009).to_bytes(DELIMITER_SIZE, byteorder=NETWORK_BYTE_ORDER)

# Requests
FILE_NAMES_REQ_B          = (0x00010001).to_bytes(DELIMITER_SIZE, byteorder=NETWORK_BYTE_ORDER)
FILE_CONTENT_REQ_B        = (0x00010002).to_bytes(DELIMITER_SIZE, byteorder=NETWORK_BYTE_ORDER)


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
    # Find NULL_TERM
    termStartIdx = findNullTerm(circBuf)
    if termStartIdx < 0:
        return None

    # Pop data portion of message and NULL_TERM
    tempBuf = circBuf.popToIdx(termStartIdx)
    circBuf.popSize(DELIMITER_SIZE)
    return tempBuf

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
