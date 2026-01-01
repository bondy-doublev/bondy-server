package org.example.interactionservice.client;

import org.example.interactionservice.entity.RecommendedPost;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class RecommendationClient {

  @Value("${app.python.recommend.url}")
  private String recommendUrl;

  private final RestTemplate restTemplate = new RestTemplate();

  // GET recommend theo offset/limit
  public List<RecommendedPost> getRecommendedPosts(Long userId, int offset, int limit) {
    String url = recommendUrl + "?user_id=" + userId + "&offset=" + offset + "&limit=" + limit;
    ResponseEntity<List<RecommendedPost>> response =
      restTemplate.exchange(url, HttpMethod.GET, null,
        new ParameterizedTypeReference<List<RecommendedPost>>() {
        });
    return response.getBody();
  }

  // POST /react để cập nhật profile bên Recommendation Server
  public void pushUserReact(Long userId, Long postId) {
    String url = recommendUrl.replace("/recommend", "/react"); // chuyển sang endpoint /react

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    Map<String, Object> body = Map.of(
      "user_id", userId,
      "post_id", postId
    );

    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

    try {
      restTemplate.exchange(url, HttpMethod.POST, entity, new ParameterizedTypeReference<Map<String, Object>>() {
      });
    } catch (Exception ex) {
      // Không làm fail luồng chính nếu recommendation server lỗi
      // Có thể log WARN/DEBUG tùy nhu cầu
    }
  }

  // Tùy chọn: nếu muốn có nút refit thủ công sau các batch
  public void triggerRefit() {
    String url = recommendUrl.replace("/recommend", "/refit");
    try {
      restTemplate.exchange(url, HttpMethod.POST, null, new ParameterizedTypeReference<Map<String, Object>>() {
      });
    } catch (Exception ex) {
      // swallow lỗi, chỉ là best-effort
    }
  }
}