package org.example.interactionservice.client;

import org.example.interactionservice.entity.RecommendedPost;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class RecommendationClient {

  @Value("${app.python.recommend.url}")
  private String recommendUrl;

  private final RestTemplate restTemplate = new RestTemplate();

  // RecommendationClient.java
  public List<RecommendedPost> getRecommendedPosts(Long userId, int offset, int limit) {
    String url = recommendUrl + "?user_id=" + userId + "&offset=" + offset + "&limit=" + limit;
    ResponseEntity<List<RecommendedPost>> response =
      restTemplate.exchange(url, HttpMethod.GET, null,
        new ParameterizedTypeReference<List<RecommendedPost>>() {
        });
    return response.getBody();
  }
}
