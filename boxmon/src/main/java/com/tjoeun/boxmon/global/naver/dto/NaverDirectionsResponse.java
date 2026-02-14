package com.tjoeun.boxmon.global.naver.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class NaverDirectionsResponse {
    private String code;
    private String message;
    private CurrentDateTime currentDateTime;
    private Route route;

    @Getter
    @Setter
    public static class CurrentDateTime {
        private String value;
    }

    @Getter
    @Setter
    public static class Route {
        private List<TrafficGuide> trafast;
        private List<TrafficGuide> traoptimal;
        private List<TrafficGuide> tracomfort;
        // Other route types like "trafast", "tracomfort" can be added if needed
    }

    @Getter
    @Setter
    public static class TrafficGuide {
        private Summary summary;
        // private List<Guidance> guidance; // Not needed for now
    }

    @Getter
    @Setter
    public static class Summary {
        private Point start;
        private Point goal;
        private double distance; // 미터 단위
        private int duration;    // 밀리초 단위
        private String tollFare;
        private String taxiFare;
        private String fuelPrice;
    }

    @Getter
    @Setter
    public static class Point {
        private double[] location; // [longitude, latitude]
    }
}
