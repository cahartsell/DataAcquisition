from bluetooth import *
import os
import errno
import time
import messages
from CircularBuffer import CircularBuffer

# Path for IPC communication.
# '@' symbol makes this an unnamed socket path (doesn't exist on disk)
# Don't have to worry about socket cleanup or already exists conflicts
DAS_IPC_ADDRESS = "@/var/lib/das/das_socket"
DAS_BASE_DIR = "/var/lib/das/"
DAS_DATA_DIR = DAS_BASE_DIR + "data/"
DAS_LOG_DIR = DAS_BASE_DIR + "log/"
DAS_DATA_DIR = "./"
DAS_LOG_DIR = "./"

#PYTHON_DIRECTORY = DAS_BASE_DIR + "python/"
PYTHON_DIRECTORY = "./"
MY_UUID = "94f39d29-7d6d-435d-973b-fba39e49d4ee"


def main():
    # Make sure python directory exists
    try:
        os.makedirs(PYTHON_DIRECTORY)
    except OSError as e:
        if e.errno != errno.EEXIST:
            raise

    # Create log file for this session
    now = time.strftime("%Y-%m-%d_%H-%M-%S", time.gmtime())
    logFileName = PYTHON_DIRECTORY + now + ".log"
    try:
        # Open log file with line buffering
        logFile = open(logFileName, "w", buffering=1)
    except Exception as e:
        print("Failed to create log file.")
        raise e
    logFile.write("Log file created.\n")

    # Setup IPC socket for communicating with DAS
    # dasSock = socket.socket(socket.AF_UNIX, socket.SOCK_STREAM)
    # try:
    #     dasSock.connect(DAS_IPC_ADDRESS)
    # except socket.error as e:
    #     logFile.write("Error creating IPC socket:\n" + str(e))
    #     sys.exit(-1)
    # logFile.write("Created IPC socket at address " + DAS_IPC_ADDRESS + '\n')

    # Setup Bluetooth socket
    serverSock = BluetoothSocket(RFCOMM)
    serverSock.bind(("", PORT_ANY))
    serverSock.listen(5)
    serverSockInfo = serverSock.getsockname()
    logFile.write("Bluetooth RFCOMM socket created and listening on address " + str(serverSockInfo[0]) + " port " + str(serverSockInfo[1]) + '\n')

    # Advertise server
    logFile.write("Advertising service as 'DataAcqServer' at UUID: " + str(MY_UUID) + '\n')
    channel = serverSock.getsockname()[1]
    advertise_service(serverSock, "DataAcqServer",
                      service_id=MY_UUID,
                      service_classes=[MY_UUID, SERIAL_PORT_CLASS],
                      profiles=[SERIAL_PORT_PROFILE])
                      #protocols=[OBEX_UUID])

    # Main program loop
    circBuf = CircularBuffer(messages.MAX_FRAME_SIZE * 2)
    while True:
        logFile.write("Waiting for connection on RFCOMM channel %d\n" % channel)

        # Receive connection and spawn client_sock
        (clientSock, clientInfo) = serverSock.accept()
        logFile.write("Accepted client connection from address " + str(clientInfo[0]) + " port: " + str(clientInfo[1]) + '\n')

        while True:
            # Max bluetooth message size should be 1024 or less
            # data = clientSock.recv(1024)
            # print(data)
            # if data == b'files':
            #     print("Sending file names...")
            #     sendFileNames(clientSock, fileNames)
            #     print("done")
            # if len(data) == 0:
            #     # Client disconnected
            #     logFile.write("Client at address " + str(clientInfo[0]) + " port " + str(clientInfo[1]) + ' has disconnected \n')
            #     clientSock.close()
            #     break

            # If a valid message is not available, wait for more data
            # Otherwise, process existing message before receiving more data
            if not (messages.hasValidMessage(circBuf)):
                # Read data from BT input stream
                try:
                    data = clientSock.recv(1024)
                    print(data)
                except IOError:
                    logFile.write("ERROR: IOError when reading from bluetooth socket")
                    continue

                if len(data) == 0:
                    # Client disconnected
                    logFile.write("Client at address " + str(clientInfo[0]) + " port " + str(clientInfo[1]) + ' has disconnected \n')
                    clientSock.close()
                    circBuf.clear()
                    break

                # Append read buffer to circular buffer
                print("Writing to buffer")
                circBuf.write(data)

            # It's possible we only received a partial message in last read.
            # FIXME: Possible to get stuck in a loop here if garbage gets into buffer
            if not(messages.hasValidMessage(circBuf)):
                continue

            # Do something based on received message type
            msgType = messages.popTypeHeader(circBuf)
            print("MSG_TYPE: ", str(msgType))
            if msgType == messages.FILE_NAMES_REQ:
                print("Sending file names...")
                logFile.write("INFO: Received file names request")
                fileNames = discoverFiles()
                sendFileNames(clientSock, fileNames)
            elif msgType == messages.FILE_CONTENT_REQ:
                print("Sending file data...")
                temp_filename = messages.popMsgDataAsUTF8(circBuf)
                logFile.write("INFO: Received file content request for file name: {}".format(temp_filename))
                sendFileContent(clientSock, temp_filename)

        # Placeholder. should be able to end loop somehow
        if False:
            break

    logFile.write("Program shutting down.")

    clientSock.close()
    serverSock.close()

    exit(0)


def discoverFiles():
    # Find all data files
    temp_files = os.listdir(DAS_DATA_DIR)
    data_files = []
    for file in temp_files:
        if file.endswith(".dat"):
            data_files.append(file)

    # Find all log files
    temp_files = os.listdir(DAS_LOG_DIR)
    log_files = []
    for file in temp_files:
        if file.endswith(".log"):
            log_files.append(file)

    # Find all python log files
    temp_files = os.listdir(PYTHON_DIRECTORY)
    py_log_files = []
    for file in temp_files:
        if file.endswith(".log"):
            py_log_files.append(file)

    # Construct and return dict of file names
    file_names = {'data': data_files, 'log': log_files, 'py_log': py_log_files}
    return file_names


def sendFileNames(sock, file_names):
    try:
        # Send start of file names delimiter
        sock.send(messages.FILE_NAMES_HDR.to_bytes(messages.DELIMITER_SIZE, byteorder='big'))

        # Send data files
        data_files = file_names.get('data')
        if data_files is not None:
            for file_name in data_files:
                # FIXME: Send's should be grouped into a single send
                sock.send(messages.DATA_FILE_NAME.to_bytes(messages.DELIMITER_SIZE, byteorder='big'))
                sock.send(file_name.encode("UTF-8"))
                sock.send(messages.NULL_TERM.to_bytes(messages.DELIMITER_SIZE, byteorder='big'))

        # Send log files
        log_files = file_names.get('log')
        if log_files is not None:
            for file_name in log_files:
                sock.send(messages.LOG_FILE_NAME.to_bytes(messages.DELIMITER_SIZE, byteorder='big'))
                sock.send(file_name.encode("UTF-8"))
                sock.send(messages.NULL_TERM.to_bytes(messages.DELIMITER_SIZE, byteorder='big'))

        # Send python log files
        py_log_files = file_names.get('py_log')
        if py_log_files is not None:
            for file_name in py_log_files:
                sock.send(messages.PY_LOG_FILE_NAME.to_bytes(messages.DELIMITER_SIZE, byteorder='big'))
                sock.send(file_name.encode("UTF-8"))
                sock.send(messages.NULL_TERM.to_bytes(messages.DELIMITER_SIZE, byteorder='big'))

        # Send end of file names delimiter
        sock.send(messages.FILE_NAMES_FTR.to_bytes(messages.DELIMITER_SIZE, byteorder='big'))

    except Exception as e:
        # FIXME: Don't want to crash if something goes wrong. Probably should do something better than this though
        print("WHOOPS")
        print(e)
        return -1

    return 0


def sendFileContent(sock, filename):
    # TODO: Write this
    # Make sure file exists
    full_filename = None
    filenames = discoverFiles()
    if filename in filenames['data']:
        full_filename = os.path.join(DAS_DATA_DIR, filename)
    elif filename in filenames['log']:
        full_filename = os.path.join(DAS_DATA_DIR, filename)
    elif filename in filenames['py_log']:
        full_filename = os.path.join(DAS_DATA_DIR, filename)

    # File not found response
    if full_filename is None:
        buf = bytearray()
        buf.extend(messages.FILE_NOT_FOUND_B)
        buf.extend(filename.encode("UTF-8"))
        buf.extend(messages.NULL_TERM_B)
        sock.send(bytes(buf))
        # TODO: Log error
        return -1

    # Send file in chunks
    # Header message echos back the requested filename
    buf = bytearray()
    buf.extend(messages.FILE_CONTENT_HDR_B)
    buf.extend(filename.encode("UTF-8"))
    buf.extend(messages.NULL_TERM_B)
    sock.send(bytes(buf))

    # TODO: Error handling
    # Init variables
    sequence_cnt = 0
    chunk_size = messages.MAX_FRAME_SIZE - (messages.DELIMITER_SIZE * 3)
    # Open file in binary reading mode
    with open(filename, 'rb') as fp:
        # Read until end of file (no more than chunk_size bytes in each read)
        chunk_data = fp.read(chunk_size)
        while chunk_data != b'':
            # Format and send data chunk message
            buf.clear()
            buf.extend(messages.FILE_CONTENT_CHUNK_B)
            buf.extend(sequence_cnt.to_bytes(messages.DELIMITER_SIZE, byteorder=messages.NETWORK_BYTE_ORDER))
            buf.extend(chunk_data)
            buf.extend(messages.NULL_TERM_B)
            sock.send(bytes(buf))

            # Update variables for next iteration
            sequence_cnt += 1
            chunk_data = fp.read(chunk_size)

    # End of file terminator message includes the total number of chunks sent and echos filename
    buf.clear()
    buf.extend(messages.FILE_CONTENT_FTR_B)
    buf.extend(sequence_cnt.to_bytes(messages.DELIMITER_SIZE, byteorder=messages.NETWORK_BYTE_ORDER))
    buf.extend(filename.encode("UTF-8"))
    buf.extend(messages.NULL_TERM_B)
    sock.send(bytes(buf))


# Run the program if this is the main file
if __name__ == "__main__":
    main()
