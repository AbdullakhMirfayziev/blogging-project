package uz.smartup.academy.bloggingplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BloggingplatformApplication {

	public static void main(String[] args) 	{
		SpringApplication.run(BloggingplatformApplication.class, args);
	}

}	