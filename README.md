# Java TCP Signup System

**Individual coursework · Semester 6 · COMP2221 Networks**

Java TCP 报名系统：客户端命令、线程池服务端、容量限制和同步共享状态。

A socket-programming coursework project. A server owns multiple signup lists, each with a fixed capacity. Clients request list totals, inspect members, or join a list. The server uses a 20-worker thread pool and synchronized handlers to protect list operations and log requests.

## Build and run

Requires a JDK (verified with Java 17). From the repository root:

```bash
mkdir build
javac -encoding UTF-8 -d build src/Server.java src/Client.java
java -cp build Server 3 5
```

In a second terminal:

```bash
java -cp build Client totals
java -cp build Client join 1 "Demo Participant"
java -cp build Client list 1
```

The server listens on port `8888`; the client connects to `localhost`. Each connection carries one newline-terminated command. Requests are logged locally to `log.txt`. Stop the server with Ctrl+C.

## Protocol

| Command | Purpose |
| --- | --- |
| `totals` | Show list count, capacity and occupancy |
| `list <1-based index>` | Return members of one list |
| `join <1-based index> <name>` | Add a name when capacity allows |

This is an in-memory teaching service: lists are not persisted, there is no authentication/TLS, and duplicate names are allowed. It is intended for local protocol and concurrency demonstrations. [Publication details](PROVENANCE.md).
