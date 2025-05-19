package ru.practicum.explorewithme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import ru.practicum.explorewithme.config.ClientConfig;

@SpringBootApplication(scanBasePackages = {"ru.practicum.explorewithme"})
@Import(ClientConfig.class)
public class ExploreWithMeServer {
    public static void main(String[] args) {
        SpringApplication.run(ExploreWithMeServer.class, args);
    }
}