package org.micro.social.eurekasecurity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"org.micro.social"})
@EnableDiscoveryClient
public class EurekaSecurityApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaSecurityApplication.class, args);
    }

}
