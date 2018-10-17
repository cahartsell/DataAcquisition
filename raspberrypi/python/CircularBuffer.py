# Simple implementation of a circular byte buffer
class CircularBuffer:
    def __init__(self, size):
        self.size = size
        self.storage = bytearray(size)
        self.head = 0
        self.tail = 0

    def totalSize(self):
        return self.size

    def getHead(self):
        return self.head

    def getTail(self):
        return self.tail

    def usedSpace(self):
        used = self.tail - self.head
        # Check if buffer wraps around 0
        if used < 0:
            used = self.totalSize() + used
        return used

    def freeSpace(self):
        return self.size - self.usedSpace()

    def clear(self):
        self.head = 0
        self.tail = 0
        return 0

    def write(self, _bytes):
        if len(_bytes) > self.freeSpace():
            return -1

        newTail = self.tail + len(_bytes)
        if newTail >= self.totalSize():
            newTail = newTail % self.totalSize()
            wrapSize = newTail
            self.storage[self.tail:self.totalSize()] = _bytes[0:len(_bytes) - wrapSize]
            self.storage[0:newTail] = _bytes[len(_bytes) - wrapSize:len(_bytes)]
        else:
            self.storage[self.tail:newTail] = _bytes
        self.tail = newTail

        return len(_bytes)

    def get(self, idx):
        # Check for empty buffer
        if self.usedSpace() <= 0:
            return None

        # Check invalid index
        if idx >= self.size or idx < 0:
            raise IndexError

        # Check for wrap around 0
        retVal = None
        if self.head > self.tail:
            if (idx >= self.head) or (idx < self.tail):
                retVal = self.storage[idx]

        else:
            if (idx >= self.head) and (idx < self.tail):
                retVal = self.storage[idx]

        return retVal

    def readSize(self, size):
        if size > self.usedSpace():
            return None

        newHead = self.head + size

        # Check for wrap around 0
        if newHead >= self.totalSize():
            newHead = newHead % self.totalSize()
            retVal = self.storage[self.head:self.totalSize()]
            retVal.append(self.storage[0:newHead])
        else:
            retVal = self.storage[self.head:newHead]
        return retVal

    def popSize(self, size):
        if size > self.usedSpace():
            return None

        newHead = self.head + size

        # Check for wrap around 0
        if newHead >= self.totalSize():
            newHead = newHead % self.totalSize()
            retVal = self.storage[self.head:self.totalSize()]
            retVal.append(self.storage[0:newHead])
        else:
            retVal = self.storage[self.head:newHead]

        # Update head pointer
        self.head = newHead
        return retVal

    def readToIdx(self, idx):
        newHead = idx + 1

        # Check for wrap around 0
        if self.head > newHead:
            retVal = self.storage[self.head:self.totalSize()]
            retVal.extend(self.storage[0:newHead])
        else:
            retVal = self.storage[self.head:newHead]

        return retVal

    # Return data from current head to spcified index (non-inclusive)
    # Set head pointer to index
    def popToIdx(self, idx):
        newHead = idx

        # Check for wrap around 0
        if self.head > newHead:
            retVal = self.storage[self.head:self.totalSize()]
            retVal.extend(self.storage[0:newHead])
        else:
            retVal = self.storage[self.head:newHead]
        self.head = newHead % self.totalSize()
        return retVal

    # Find the first occurrence of _byte in buffer. Return index.
    def index(self, _byte):
        # Check for wrap around 0
        if self.head > self.tail:
            try:
                idx = self.storage.index(_byte, self.head, self.totalSize())
            except ValueError:
                idx = None
            # If not yet found, search from 0 to tail
            if idx is None:
                try:
                    idx = self.storage.index(_byte, 0, self.tail)
                except ValueError:
                    idx = None
        else:
            # Empty buffer case
            if self.head == self.tail:
                idx = None
            else:
                try:
                    idx = self.storage.index(_byte, self.head, self.tail)
                except ValueError:
                    idx = None
        return idx
