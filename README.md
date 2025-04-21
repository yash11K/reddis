# 🚀 Redis Clone in Java

A high-performance Redis clone built from scratch in Java, featuring:

- Non-blocking TCP server using Java NIO
- RESP (Redis Serialization Protocol) support
- RDB persistence
- Key expiry mechanism
- Support for various Redis data types
- *Master-replica replication with command propagation*
- Commands implemented as extensible classes

[//]: # (![Redis Clone Architecture]&#40;./assets/architecture-diagram.png&#41;)

---

## 🧠 Project Overview

This project aims to deeply understand Redis internals by re-creating its core features using Java NIO and low-level networking, focusing on event-driven architecture and efficient memory handling.

---

## 🔧 Features Implemented

### ✅ 1. Non-blocking TCP Server with Java NIO

Built using Java's low-level `Selector`, `ServerSocketChannel`, and `SocketChannel` to handle multiple clients concurrently — without blocking threads.

**Event Selector + Threaded Execution Flow:**

1. Create a Selector (acts as the event manager)
2. Register the server socket to listen for new client connections with `OP_ACCEPT`
3. Enter an event loop and wait for events
4. Accept new client connections and register them for `OP_READ`
5. Read client data, decode it using RESP, and parse into a Command object
6. Submit the command to a thread pool (`ExecutorService`) for processing
7. ExecutorService delegates command execution to a thread which interacts with the core `RDB-backed data store` , ensuring thread-safety using `synchronized` access

Respond to the client, without blocking the main selector thread

---

### 📡 2. RESP (Redis Serialization Protocol) Support

Implements RESP to support native Redis CLI and client communication.

**Supported Types:**

- ✅ Simple Strings (`+OK\r\n`)
- ✅ Errors (`-Error message\r\n`)
- ✅ Integers (`:1000\r\n`)
- ✅ Bulk Strings (`$6\r\nfoobar\r\n`)
- ✅ Arrays (`*2\r\n$3\r\nfoo\r\n$3\r\nbar\r\n`)

RESP is simple, fast, and human-readable 

---

### 💾 3. RDB Persistence

Our server supports RDB file format saving the entire dataset at once.

- Binary serialization of in-memory data
- Manual or periodic save to disk
- Supports RDB transfer to replicas during sync

Helps with crash recovery and replication.

---

### ⏰ 4. Key Expiry Mechanism

You can `SET <key> px <expire_in_seconds>` keys or manually expire them. Our server:

- Stores key expiry timestamps
- Cleans up expired keys on access (lazy deletion)
- Runs periodic cleanup in background

Use cases: session tokens, TTL caches, auto-expiring state.

---

### 🧩 5. Modular Commands (Extensible)

Every command (like `GET`, `SET`, `DEL`, etc.) is implemented as its own **Command** class following a common interface.

```java
public interface Command {
    void execute(DataStore store, ClientConnection client);
}
```