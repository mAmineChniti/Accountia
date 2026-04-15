package com.accountia.auth.service;

import com.accountia.auth.dto.RegisterRequest;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class KeycloakService {

    private static final Logger logger = LoggerFactory.getLogger(KeycloakService.class);

    @Value("${keycloak.auth-server-url:http://keycloak:8080}")
    private String serverUrl;

    @Value("${keycloak.realm:accountia}")
    private String realm;

    @Value("${keycloak.admin.username:admin}")
    private String adminUsername;

    @Value("${keycloak.admin.password:admin}")
    private String adminPassword;

    public void createUser(RegisterRequest request) {
        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm("master")
                .clientId("admin-cli")
                .username(adminUsername)
                .password(adminPassword)
                .build();

        UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmailVerified(true);

        // Set password
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setTemporary(false);
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(request.getPassword());
        user.setCredentials(Collections.singletonList(credential));

        UsersResource usersResource = keycloak.realm(realm).users();
        
        try (Response response = usersResource.create(user)) {
            if (response.getStatus() == 201) {
                logger.info("User {} successfully created in Keycloak", request.getUsername());
            } else if (response.getStatus() == 409) {
                logger.warn("User {} already exists in Keycloak", request.getUsername());
            } else {
                String errorMsg = response.readEntity(String.class);
                logger.error("Failed to create user in Keycloak. Status: {}, Error: {}", response.getStatus(), errorMsg);
                throw new RuntimeException("Keycloak user creation failed: " + errorMsg);
            }
        } catch (Exception e) {
            logger.error("Error calling Keycloak API: {}", e.getMessage(), e);
            throw new RuntimeException("Keycloak connection error", e);
        } finally {
            keycloak.close();
        }
    }
}
