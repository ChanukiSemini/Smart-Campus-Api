# Smart Campus Sensor & Room Management API

A RESTful API built with JAX-RS (Jersey 2.41) for managing campus rooms and IoT sensor infrastructure. Developed as part of the 5COSC022W Client-Server Architectures module at the University of Westminster.



## Overview

The Smart Campus API exposes three core resource types:

 **Rooms** — physical spaces on campus (e.g. lecture halls, labs)
 **Sensors** — IoT devices installed inside rooms (temperature, CO2, occupancy, etc.)
 **SensorReadings** — time-series measurement history for each sensor

All data is held in-memory using `ConcurrentHashMap` and `ArrayList` and  no database is used.



## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | JAX-RS via Jersey 2.41 |
| Server | Apache Tomcat 9.x |
| Build | Maven 3.x |
| JSON | Jackson (via jersey-media-json-jackson) |



## How to Build and Run

### Prerequisites
- Java 21
- Maven 3.6+
- Apache Tomcat 9.x

### Step 1 — Clone the repository
```bash
git clone https://github.com/YOUR_USERNAME/smart-campus-api.git
cd smart-campus-api
```

### Step 2 — Build the WAR file
```bash
mvn clean package
```

### Step 3 — Deploy to Tomcat
Copy the WAR file into your Tomcat webapps directory:
```bash
copy "target\smart-campus-api-1.0-SNAPSHOT.war" "C:\Program Files\Apache Software Foundation\Tomcat 9.0\webapps\"
```

### Step 4 — Start Tomcat
Start the Tomcat service from Windows Services or system tray.

### Step 5 — Test it
Open your browser and go to:
```
http://localhost:8080/smart-campus-api-1.0-SNAPSHOT/api/v1
```

---

## API Endpoints

| Method | Path | Description |
|---|---|---|
| GET | /api/v1 | Discovery / health check |
| GET | /api/v1/rooms | List all rooms |
| POST | /api/v1/rooms | Create a room |
| GET | /api/v1/rooms/{id} | Get a single room |
| DELETE | /api/v1/rooms/{id} | Delete a room (blocked if sensors present) |
| GET | /api/v1/sensors | List sensors (optional ?type= filter) |
| GET | /api/v1/sensors/{id} | Get a single sensor |
| POST | /api/v1/sensors | Register a sensor |
| GET | /api/v1/sensors/{id}/readings | Get reading history |
| POST | /api/v1/sensors/{id}/readings | Add a new reading |

---

## Sample Curl Commands

### 1. Discovery Endpoint
```bash
curl -X GET http://localhost:8080/smart-campus-api/api/v1
```

### 2. Get All Rooms
```bash
curl -X GET http://localhost:8080/smart-campus-api/api/v1/rooms
```

### 3. Create a New Room
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/rooms -H "Content-Type: application/json" -d "{\"id\":\"CS-101\",\"name\":\"Computer Science Lab\",\"capacity\":40}"
```

### 4. Get a Specific Room
```bash
curl -X GET http://localhost:8080/smart-campus-api/api/v1/rooms/LIB-301
```

### 5. Delete a Room With No Sensors - Success 200
```bash
curl -X DELETE http://localhost:8080/smart-campus-api/api/v1/rooms/CS-101
```

### 6. Delete a Room That Has Sensors - 409 Conflict
```bash
curl -X DELETE http://localhost:8080/smart-campus-api/api/v1/rooms/LIB-301
```

### 7. Get All Sensors
```bash
curl -X GET http://localhost:8080/smart-campus-api/api/v1/sensors
```

### 8. Get Sensors Filtered by Type
```bash
curl -X GET "http://localhost:8080/smart-campus-api/api/v1/sensors?type=Temperature"
```

### 9. Get a Specific Sensor
```bash
curl -X GET http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-001
```

### 10. Create a New Sensor
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors -H "Content-Type: application/json" -d "{\"id\":\"HUM-001\",\"type\":\"Humidity\",\"status\":\"ACTIVE\",\"currentValue\":55.0,\"roomId\":\"LIB-301\"}"
```

### 11. Create Sensor With Invalid Room - 422 Unprocessable Entity
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors -H "Content-Type: application/json" -d "{\"id\":\"TEMP-999\",\"type\":\"Temperature\",\"status\":\"ACTIVE\",\"currentValue\":0.0,\"roomId\":\"FAKE-ROOM\"}"
```

### 12. Get Sensor Reading History
```bash
curl -X GET http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-001/readings
```

### 13. Add a New Sensor Reading - Success 201
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-001/readings -H "Content-Type: application/json" -d "{\"value\":24.5}"
```

### 14. Add Reading to MAINTENANCE Sensor - 403 Forbidden
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-002/readings -H "Content-Type: application/json" -d "{\"value\":20.0}"
```

### 15. Trigger Global Error Handler - 500 Internal Server Error
```bash
curl -X GET http://localhost:8080/smart-campus-api/api/v1/test-error
```

---

## Conceptual Report

### Part 1.1 — JAX-RS Resource Class Lifecycle

By default, JAX-RS creates a brand new instance of each resource class for every incoming HTTP request. This is called per-request scope. The motivation is thread safety  if one instance handled many concurrent requests, developers would need to carefully synchronize every field access, which is error-prone.

The consequence for in-memory data storage is significant. If we stored rooms and sensors as instance fields on the resource class, they would be created fresh for every request and discarded immediately — every GET would return an empty list. To share state across requests, we use the Singleton pattern in DataStore. One DataStore instance is created when the JVM loads the class and persists for the entire server lifetime. Since it is shared across all resource instances and may be accessed concurrently, we use ConcurrentHashMap rather than HashMap. Regular HashMap is not thread-safe and can corrupt its internal structure when two threads write simultaneously.

### Part 1.2 — HATEOAS and Hypermedia

HATEOAS (Hypermedia As The Engine Of Application State) is the practice of including navigation links in API responses — similar to how web pages include hyperlinks. A discovery response tells a client that rooms live at /api/v1/rooms and sensors at /api/v1/sensors, rather than requiring the developer to read static documentation.

This benefits client developers in several ways. First, clients become more resilient to URL changes — if the server reorganises its paths, clients following links adapt automatically. Second, new developers can discover available resources programmatically. Third, it reduces coupling between client and server — the client only needs to know one entry-point URL and the API guides the rest.

### Part 2.1 — Returning IDs vs Full Objects

Returning only IDs in a list response forces every client to make one additional GET request per room to get useful data. For 100 rooms that is 100 extra round trips — terrible for performance. Returning full room objects in one call eliminates those extra requests entirely. For typical campus management dashboards the client almost always needs the name and capacity immediately, so returning full objects is the right trade-off.

### Part 2.2 — Is DELETE Idempotent?

In this implementation DELETE is not strictly idempotent in terms of HTTP response codes. The first call returns 200 OK. A second identical call returns 404 Not Found because the room no longer exists. However the effect is idempotent — after either call the room does not exist. A strictly idempotent version would return 200 or 204 every time regardless of whether the room existed.

### Part 3.1 — @Consumes and Content-Type Mismatch

The @Consumes(MediaType.APPLICATION_JSON) annotation tells Jersey that the POST endpoint expects a JSON request body. If a client sends Content-Type: text/plain or application/xml, Jersey intercepts the request before our method is even called and returns 415 Unsupported Media Type. This prevents Jackson from trying to deserialise an incompatible body and enforces the API contract at the framework level.

### Part 3.2 — Query Parameters vs Path Segments for Filtering

Putting the type in the path such as /api/v1/sensors/type/CO2 creates a problem — it looks like a new resource rather than a filtered view of an existing collection. REST convention is that path segments represent resources. CO2 is not a resource, it is a filter criterion. Query parameters are the idiomatic HTTP way to say give me the sensors collection but filtered. They also allow multiple filters to be combined naturally such as ?type=CO2&status=ACTIVE.

### Part 4.1 — Sub-Resource Locator Pattern Benefits

Without the sub-resource locator pattern every nested path would be defined in one class. SensorResource would contain methods for listing sensors, creating sensors, fetching sensors, listing readings, and creating readings — growing into a large unfocused class. The locator pattern delegates responsibility — SensorResource handles sensor-level concerns and returns a SensorReadingResource instance for the /readings sub-path. Each class has a single clear responsibility, making both smaller and easier to maintain.

### Part 5.2 — Why 422 is More Accurate Than 404

404 Not Found means the URL you requested does not exist on this server. But when a client POSTs a sensor with a non-existent roomId, the URL /api/v1/sensors absolutely exists. The problem is that the content of the payload references an entity that does not exist. 422 Unprocessable Entity was defined precisely for this scenario — the request is syntactically valid JSON and the URL is correct, but the semantic meaning of the body is broken. Returning 422 gives the client developer a much more accurate signal.

### Part 5.4 — Security Risks of Exposing Stack Traces

Returning raw Java stack traces to API consumers is a significant security vulnerability. An attacker can extract class and package names which reveals internal code structure, library versions which allows looking up known CVEs for those exact versions, file paths and line numbers which tells the attacker exactly where to look in the code, and technology fingerprinting which confirms which Java version and framework is in use. The global exception mapper eliminates all of this by logging the full stack trace privately server-side and returning a safe generic message to the client.

### Part 5.5 — Filters vs Manual Logging

Manually inserting Logger.info() at the start and end of every resource method is repetitive boilerplate that is easy to forget when adding new endpoints and inconsistent across developers. JAX-RS filters solve all of this. LoggingFilter registers once and Jersey calls it automatically for every request and response regardless of which resource method handles it. Adding a new endpoint gives you logging for free. The resource methods stay clean and focused on business logic only.
