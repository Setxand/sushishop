package config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties(prefix = "liqpay")
@Getter
@Setter
public class LiqpayConfigProps {

	private String publicKey;
	private String privateKey;
	private String serverUrl;

}


