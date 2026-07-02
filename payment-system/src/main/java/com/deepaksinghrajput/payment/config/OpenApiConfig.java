package com.deepaksinghrajput.payment.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI paymentSystemOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Enterprise Payment System API")
                .version("1.0.0")
                .description("Payment engine, wallet, fraud detection and compliance API")
                .contact(new Contact().name("Deepak Singh Rajput")));
    }
}
