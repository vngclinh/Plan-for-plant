package com.example.planforplant.DTO;

public class WeatherResponse {

    public String lat;
    public String lon;
    public Current current;

    public static class Current {
        public double temperature;
        public int icon_num;      // numeric code
        public String icon;       // string name (optional)
        public String summary;
    }
}
