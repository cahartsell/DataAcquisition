from bluetooth import *

# Setup BT socket
server_sock = BluetoothSocket(RFCOMM)
server_sock.bind(("", PORT_ANY))
server_sock.listen(1)

# Advertise server
port = server_sock.getsockname()[1]
uuid = "94f39d29-7d6d-435d-973b-fba39e49d4ee"
advertise_service(server_sock, "DataAcqServer",
                  service_id=uuid,
                  service_classes=[uuid, SERIAL_PORT_CLASS],
                  profiles=[SERIAL_PORT_PROFILE])
                  #protocols=[OBEX_UUID])
print("Waiting for connection on RFCOMM channel %d" % port)

# Receive connection and spawn client_sock
client_sock, client_info = server_sock.accept()
print("Accepted connection from: ", client_info)

try:
    while True:
        data = client_sock.recv(1024)
        if len(data) == 0:
            break
        print("received [%s]" % data)
except IOError:
    pass

print("disconnected")

client_sock.close()
server_sock.close()

exit(0)
