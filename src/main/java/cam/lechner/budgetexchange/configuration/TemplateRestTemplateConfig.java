package cam.lechner.budgetexchange.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
//@ConditionalOnMissingBean(name = "RestTemplate")
public class TemplateRestTemplateConfig {
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        // Do any additional configuration here
        return builder.setConnectTimeout(Duration.ofMinutes(5)).
                build();
    }
}
