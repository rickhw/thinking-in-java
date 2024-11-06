import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.FilterRegistrationBean;

@Configuration
public class AppConfig {

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilter() {
        FilterRegistrationBean<RateLimitFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RateLimitFilter());
        registrationBean.addUrlPatterns("/api/*"); // 指定需要限制的 API 路徑
        registrationBean.setOrder(1); // 設定 Filter 優先順序
        return registrationBean;
    }
}
