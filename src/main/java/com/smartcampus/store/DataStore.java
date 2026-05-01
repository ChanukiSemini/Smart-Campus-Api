package com.smartcampus.store;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Singleton in-memory store shared across all requests.
// ConcurrentHashMap keeps things thread-safe when multiple requests hit at once.
public class DataStore {

    private static final DataStore INSTANCE = new DataStore();

    public static DataStore getInstance() {
        return INSTANCE;
    }

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    private final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    private DataStore() {
        // sample rooms loaded at startup so the API has data right away
        Room r1 = new Room("LIB-301", "Library Quiet Study", 50);
        Room r2 = new Room("LAB-101", "Computer Lab", 30);
        Room r3 = new Room("HALL-001", "Main Lecture Hall", 200);
        rooms.put(r1.getId(), r1);
        rooms.put(r2.getId(), r2);
        rooms.put(r3.getId(), r3);

        // sample sensors - TEMP-002 is in MAINTENANCE so we can demo the 403 error
        Sensor s1 = new Sensor("TEMP-001", "Temperature", "ACTIVE",   22.5, "LIB-301");
        Sensor s2 = new Sensor("CO2-001",  "CO2",         "ACTIVE",  412.0, "LAB-101");
        Sensor s3 = new Sensor("OCC-001",  "Occupancy",   "ACTIVE",   45.0, "HALL-001");
        Sensor s4 = new Sensor("TEMP-002", "Temperature", "MAINTENANCE", 0.0, "LAB-101");
        sensors.put(s1.getId(), s1);
        sensors.put(s2.getId(), s2);
        sensors.put(s3.getId(), s3);
        sensors.put(s4.getId(), s4);

        // link sensors to their rooms
        r1.getSensorIds().add(s1.getId());
        r2.getSensorIds().add(s2.getId());
        r2.getSensorIds().add(s4.getId());
        r3.getSensorIds().add(s3.getId());

        // empty reading history lists for each sensor
        sensors.keySet().forEach(id -> readings.put(id, new ArrayList<>()));

        // a couple of seed readings so the history endpoint isn't empty
        readings.get("TEMP-001").add(new SensorReading(21.8));
        readings.get("TEMP-001").add(new SensorReading(22.1));
        readings.get("CO2-001").add(new SensorReading(408.5));
    }

    public Map<String, Room> getRooms() { return rooms; }
    public Room getRoom(String id) { return rooms.get(id); }
    public void addRoom(Room room) { rooms.put(room.getId(), room); }
    public boolean deleteRoom(String id) { return rooms.remove(id) != null; }

    public Map<String, Sensor> getSensors() { return sensors; }
    public Sensor getSensor(String id) { return sensors.get(id); }
    public void addSensor(Sensor sensor) {
        sensors.put(sensor.getId(), sensor);
        readings.putIfAbsent(sensor.getId(), new ArrayList<>());
    }

    public List<SensorReading> getReadings(String sensorId) {
        return readings.getOrDefault(sensorId, new ArrayList<>());
    }
    public void addReading(String sensorId, SensorReading reading) {
        readings.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);
    }
}
