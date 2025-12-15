package com.example.planforplant.DTO;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class HealthResponse {

    @SerializedName("result")
    private Result result;

    public Result getResult() { return result; }

    // ======== Result ========
    public static class Result {
        @SerializedName("is_healthy")
        private IsHealthy isHealthy;

        private Disease disease;

        public IsHealthy getIsHealthy() { return isHealthy; }
        public Disease getDisease() { return disease; }
    }

    // ======== is_healthy ========
    public static class IsHealthy {
        public boolean binary;
        public double probability;
    }

    // ======== disease ========
    public static class Disease {

        @SerializedName("suggestions")
        private List<Suggestion> suggestions;

        @SerializedName("question")
        private Question question;

        public List<Suggestion> getSuggestions() { return suggestions; }
        public Question getQuestion() { return question; }
    }

    // ======== suggestion ========
    public static class Suggestion {
        public String id;
        public String name;
        public double probability;
        public Boolean redundant;

        @SerializedName("local_name")
        public String localName;

        @SerializedName("description")
        public String description;

        @SerializedName("url")
        public String url;

        @SerializedName("cause")
        public String cause;

        @SerializedName("common_names")
        public List<String> commonNames;

        @SerializedName("classification")
        public Classification classification;

        @SerializedName("treatment")
        public Treatment treatment;
    }

    // ======== Classification ========
    public static class Classification {
        public String type;
        public String family;
        public String genus;
    }

    // ======== Treatment ========
    public static class Treatment {
        public String prevention;
        public String biological;
        public String chemical;
        public String description;
    }

    // ======== Question ========
    public static class Question {
        public JsonElement options;
    }

    public static class Option {
        @SerializedName("suggestion_index")
        public int suggestionIndex;

        @SerializedName("entity_id")
        public String entityId;

        public String name;
    }
}
