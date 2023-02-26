package com.vdev.microservice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.vdev.microservice.config.AsyncSyncConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base composite annotation for integration tests.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(classes = { JHipsterRegistryApp.class, AsyncSyncConfiguration.class })
@ActiveProfiles(profiles = { "native" })
public @interface IntegrationTest {
}
