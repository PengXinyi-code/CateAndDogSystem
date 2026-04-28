package com.fast.succour.service.impl;

import com.fast.succour.service.PythonService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import org.springframework.http.HttpHeaders;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

@Service
public class PythonServiceImp implements PythonService {

//    public float[] extractFeature(MultipartFile file) throws Exception {
//        String url = "http://localhost:8000/extract";
//
//        RestTemplate restTemplate = new RestTemplate();
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
//
//        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
//        body.add("file", new MultipartInputStreamFileResource(
//                file.getInputStream(), file.getOriginalFilename()));
//
//        HttpEntity<MultiValueMap<String, Object>> requestEntity =
//                new HttpEntity<>(body, headers);
//
//        ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);
//
//        List<Double> featureList = (List<Double>) response.getBody().get("feature");
//
//        float[] feature = new float[featureList.size()];
//        for (int i = 0; i < featureList.size(); i++) {
//            feature[i] = featureList.get(i).floatValue();
//        }
//
//        return feature;
//    }
    @Override
    public float[] extractFeatureByPath(String path) throws Exception {

        String url = "http://localhost:8000/extract_by_path?path=" +
                URLEncoder.encode(path, "UTF-8");

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream())
        );

        String json = reader.readLine();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(json);

        JsonNode arr = node.get("feature");

        float[] result = new float[arr.size()];
        for (int i = 0; i < arr.size(); i++) {
            result[i] = (float) arr.get(i).asDouble();
        }

        return result;
    }
}