package com.fast.animal.service.impl;

import com.fast.animal.domain.CatDogAnalysisResult;
import com.fast.animal.service.PythonService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

@Service
public class PythonServiceImp implements PythonService {

    @Override
    public float[] extractFeatureByPath(String path) throws Exception {

        JsonNode node = getJson("http://localhost:8000/extract_by_path?path=" + encode(path));

        JsonNode arr = node.get("feature");

        float[] result = new float[arr.size()];
        for (int i = 0; i < arr.size(); i++) {
            result[i] = (float) arr.get(i).asDouble();
        }

        return result;
    }

    @Override
    public CatDogAnalysisResult analyzeCatDogByPath(String path) throws Exception {
        JsonNode node = getJson("http://localhost:8000/analyze_cat_dog_by_path?path=" + encode(path));

        CatDogAnalysisResult result = new CatDogAnalysisResult();
        result.setCatDog(node.path("isCatDog").asBoolean(false));
        result.setCategoryCode(node.path("categoryCode").asText(null));
        result.setCategoryName(node.path("categoryName").asText(null));
        result.setCategoryConfidence(node.hasNonNull("categoryConfidence") ? node.get("categoryConfidence").asDouble() : null);
        result.setBreedCode(node.path("breedCode").asText(null));
        result.setBreedName(node.path("breedName").asText(null));
        result.setBreedConfidence(node.hasNonNull("breedConfidence") ? node.get("breedConfidence").asDouble() : null);
        result.setRawDetectLabel(node.path("rawDetectLabel").asText(null));
        result.setRawBreedLabel(node.path("rawBreedLabel").asText(null));
        result.setCropPath(node.path("cropPath").asText(null));
        result.setMessage(node.path("message").asText(null));

        if ("cat".equals(result.getCategoryCode())) {
            result.setCategoryId("cat");
        } else if ("dog".equals(result.getCategoryCode())) {
            result.setCategoryId("dog");
        }

        return result;
    }

    private JsonNode getJson(String url) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream())
        );

        String json = reader.readLine();
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(json);
    }

    private String encode(String value) throws Exception {
        return URLEncoder.encode(value, "UTF-8");
    }
}
