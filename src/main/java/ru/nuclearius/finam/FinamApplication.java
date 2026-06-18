package ru.nuclearius.finam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode;
import org.springframework.scheduling.annotation.EnableScheduling;

import ru.nuclearius.finam.config.FinamProperties;

@SpringBootApplication
@EnableScheduling
@EnableCaching
@EnableConfigurationProperties(FinamProperties.class)
@EnableSpringDataWebSupport(pageSerializationMode = PageSerializationMode.VIA_DTO)
public class FinamApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinamApplication.class, args);
	}
}
