from bluetooth import *
import os
import errno
import time

# Path for IPC communication.
# '@' symbol makes this an unnamed socket path (doesn't exist on disk)
# Don't have to worry about socket cleanup or already exists conflicts
DAS_IPC_ADDRESS = "@/var/lib/das/das_socket"
DAS_BASE_DIR = "/var/lib/das/"
DAS_DATA_DIR = DAS_BASE_DIR + "data/"
DAS_LOG_DIR = DAS_BASE_DIR + "log/"

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
    dasSock = socket.socket(socket.AF_UNIX, socket.SOCK_STREAM)
    try:
        dasSock.connect(DAS_IPC_ADDRESS)
    except socket.error as e:
        logFile.write("Error creating IPC socket:\n" + str(e))
        sys.exit(-1)
    logFile.write("Created IPC socket at address " + DAS_IPC_ADDRESS + '\n')

    # Setup Bluetooth socket
    serverSock = BluetoothSocket(RFCOMM)
    serverSock.bind(("", PORT_ANY))
    serverSock.listen(5)
    serverSockInfo = serverSock.getsockname()
    logFile.write("Bluetooth RFCOMM socket created and listening on address " + str(serverSockInfo[0]) + " port " + str(serverSockInfo[1]) + '\n')

    # Advertise server
    logFile.write("Advertising service as 'DataAcqServer' at UUID: " + str(MY_UUID) + '\n')
    port = serverSock.getsockname()[1]
    advertise_service(serverSock, "DataAcqServer",
                      service_id=MY_UUID,
                      service_classes=[MY_UUID, SERIAL_PORT_CLASS],
                      profiles=[SERIAL_PORT_PROFILE])
                      #protocols=[OBEX_UUID])

    # Main program loop
    while True:
        logFile.write("Waiting for connection on RFCOMM channel %d\n" % port)

        # Receive connection and spawn client_sock
        (clientSock, clientInfo) = serverSock.accept()
        logFile.write("Accepted client connection from address " + str(clientInfo[0]) + " port: " + str(clientInfo[1]) + '\n')

        # TODO: Send client latest info
        (dataFiles, logFiles, pyLogFiles) = discoverFiles()
        clientSock.send(pyLogFiles)

        while True:
            try:
                data = clientSock.recv(1024)
                if len(data) == 0:
                    # Client disconnected
                    logFile.write("Client at address " + str(clientInfo[0]) + " port " + str(clientInfo[1]) + ' has disconnected \n')
                    clientSock.close()
                    break

                # TODO: Handle commands from client here
            except IOError:
                pass

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

    return (data_files, log_files, py_log_files)


# Run the program if this is the main file
if __name__ == "__main__":
    main()
