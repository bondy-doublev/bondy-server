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

  @Value("${python.recommend.url}")
  private String recommendUrl; // http://127.0.0.1:8000/recommend

  private final RestTemplate restTemplate = new RestTemplate();

  public List<RecommendedPost> getRecommendedPosts(Long userId, int topN) {

    String url = recommendUrl + "?user_id=" + userId + "&top_n=" + topN;

    ResponseEntity<List<RecommendedPost>> response =
      restTemplate.exchange(
        url,
        HttpMethod.GET,
        null,
        new ParameterizedTypeReference<List<RecommendedPost>>() {
        }
      );

    return response.getBody();
  }
}
