package com.smartcampus;

import javax.ws.rs.core.Application;
import javax.ws.rs.ApplicationPath;

@ApplicationPath("/api/v1")
public class SmartCampusApp extends Application {
    // path is defined in web.xml to avoid double registration conflict
}
