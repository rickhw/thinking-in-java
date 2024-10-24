// package com.example.article;

// import com.example.article.entity.Article;
// import org.junit.jupiter.api.Test;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.web.client.RestTemplate;

// import java.time.LocalDateTime;
// import java.util.Arrays;
// import java.util.List;

// @SpringBootTest
// public class ArticleApplicationTests {

//     private static final String BASE_URL = "http://localhost:8080/article";

//     private final RestTemplate restTemplate = new RestTemplate();

//     @Test
//     public void testCreateArticle() {
//         Article article = new Article();
//         article.setSubject("Test Article");
//         article.setContent("This is a test article.");
//         article.setCreatedAt(LocalDateTime.now());

//         Article createdArticle = restTemplate.postForObject(BASE_URL, article, Article.class);
//         assert createdArticle != null;
//         System.out.println("Created Article: " + createdArticle.getId());
//     }

//     @Test
//     public void testGetArticles() {
//         int cursor = 0;
//         int pageSize = 10;
//         String url = BASE_URL + "?cursor=" + cursor + "&pageSize=" + pageSize;

//         Article[] articles = restTemplate.getForObject(url, Article[].class);
//         assert articles != null;
//         List<Article> articleList = Arrays.asList(articles);
//         System.out.println("Fetched Articles: " + articleList.size());
//     }
// }
