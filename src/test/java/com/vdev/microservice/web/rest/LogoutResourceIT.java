package com.vdev.microservice.web.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.vdev.microservice.JHipsterRegistryApp;
import com.vdev.microservice.config.TestSecurityConfiguration;
import com.vdev.microservice.utils.OAuth2TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import com.vdev.microservice.security.AuthoritiesConstants;

/**
 * Integration tests for the {@link LogoutResource} REST controller.
 */
@AutoConfigureMockMvc
@SpringBootTest(classes = { JHipsterRegistryApp.class, TestSecurityConfiguration.class })
@ActiveProfiles(profiles = { "native", "oauth2" })
class LogoutResourceIT {

    @Autowired
    private ClientRegistrationRepository registrations;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    @Autowired
    private ClientRegistration clientRegistration;

    private MockMvc restLogoutMockMvc;

    private Map<String, Object> claims;

    @BeforeEach
    public void before() throws Exception {
        claims = new HashMap<>();
        claims.put("groups", Collections.singletonList(AuthoritiesConstants.USER));
        claims.put("sub", 123);

        SecurityContextHolder
            .getContext()
            .setAuthentication(OAuth2TestUtil.registerAuthenticationToken(authorizedClientService, clientRegistration, OAuth2TestUtil.authenticationToken(claims)));
        SecurityContextHolderAwareRequestFilter authInjector = new SecurityContextHolderAwareRequestFilter();
        authInjector.afterPropertiesSet();

        this.restLogoutMockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
    }

    @Test
    void getLogoutInformation() throws Exception {
        final String ORIGIN_URL = "http://localhost:8761";
        String logoutUrl =
            this.registrations.findByRegistrationId("oidc")
                .getProviderDetails()
                .getConfigurationMetadata()
                .get("end_session_endpoint")
                .toString();
        logoutUrl = logoutUrl + "?id_token_hint=" + OAuth2TestUtil.ID_TOKEN + "&post_logout_redirect_uri=" + ORIGIN_URL;
        restLogoutMockMvc
            .perform(post("/api/logout").header(HttpHeaders.ORIGIN, ORIGIN_URL))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.logoutUrl").value(logoutUrl));
    }
}
