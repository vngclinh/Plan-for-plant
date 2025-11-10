package com.example.planforplant.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class HealthResponse {

    @SerializedName("result")
    private Result result;
    public Result getResult() { return result; }

    public static class Result {
        @SerializedName("is_plant")
        private IsPlant isPlant;

        @SerializedName("classification")
        private Classification classification;

        @SerializedName("disease")
        private Disease disease;

        public IsPlant getIsPlant() { return isPlant; }
        public Classification getClassification() { return classification; }
        public Disease getDisease() { return disease; }
    }

    public static class IsPlant {
        public boolean binary;
        public double probability;
    }

    public static class Classification {
        public List<Suggestion> suggestions;
    }

    public static class Disease {
        public List<Suggestion> suggestions;
    }

    public static class Suggestion {
        public String id;
        public String name;
        public double probability;
    }
}
