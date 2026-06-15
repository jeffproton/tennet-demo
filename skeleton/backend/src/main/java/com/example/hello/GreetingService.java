package com.example.hello;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class GreetingService {

    private final GreetingRepository repository;

    @Value("${greeting.name:World}")
    private String greetingName;

    public GreetingService(GreetingRepository repository) {
        this.repository = repository;
    }

    /** Seed the DB with the configured name on first startup. */
    @EventListener(ApplicationReadyEvent.class)
    public void seed() {
        if (repository.count() == 0) {
            repository.save(new Greeting(greetingName));
        }
    }

    /** Read from Redis cache; fall back to Postgres on miss. */
    @Cacheable(value = "greeting", key = "'current'")
    public String getMessage() {
        return repository.findFirstByOrderByIdAsc()
                .map(g -> "Hello " + g.getName())
                .orElse("Hello World");
    }
}
