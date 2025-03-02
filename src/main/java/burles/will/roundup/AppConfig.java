package burles.will.roundup;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AppConfig {


	@Value("${starling.base.url}")
	private String starlingBaseUrl;
	@Value("${starling.auth.token}")
	private String starlingAuthToken;

	@Bean
	public WebClient getStarlingWebClient(WebClient.Builder builder) {
		return builder.baseUrl(starlingBaseUrl) // Base API URL
					.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
					.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer "+starlingAuthToken)
					.build();
	}
}
