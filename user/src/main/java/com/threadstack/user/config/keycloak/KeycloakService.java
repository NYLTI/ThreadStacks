package com.threadstack.user.config.keycloak;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.representations.idm.ClientScopeRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import com.threadstack.user.model.FailedKeycloakEvent;
import com.threadstack.user.repository.FailedKeycloakEventRepository;
import com.threadstack.user.util.RetryUtility;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class KeycloakService {

    private final KeycloakProvider keycloakProvider;
    private final String realm;
    private final FailedKeycloakEventRepository failedKeycloakEventRepository;

    public KeycloakService(KeycloakProvider keycloakProvider,
	    FailedKeycloakEventRepository failedKeycloakEventRepository) {
	this.keycloakProvider = keycloakProvider;
	this.failedKeycloakEventRepository = failedKeycloakEventRepository;
	this.realm = "ThreadStacks";
    }

    public void createUser(String username, String password, String email) {
	try {
	    Keycloak keycloak = keycloakProvider.getKeycloakInstance();

	    // set properties
	    UserRepresentation user = new UserRepresentation();
	    user.setUsername(username);
	    user.setEmail(email);
	    user.setEnabled(true);
	    user.setEmailVerified(true);

	    // Set user credentials
	    CredentialRepresentation credential = new CredentialRepresentation();
	    credential.setType(CredentialRepresentation.PASSWORD);
	    credential.setValue(password);
	    credential.setTemporary(false);
	    user.setCredentials(Collections.singletonList(credential));

	    Response response = keycloak.realm(realm).users().create(user);

	    if (response.getStatus() >= 400) {
		throw new RuntimeException("Failed to create user in Keycloak. HTTP Status: " + response.getStatus());
	    }

	} catch (Exception e) {
	    queueFailedUserEvent(username, email, password);
	}
    }

    public void assignRoleToUser(String username, String roleName) {
	try {
	    Keycloak keycloak = keycloakProvider.getKeycloakInstance();
	    String userId = getUserIdByUsername(username);
	    if (userId == null)
		throw new RuntimeException("User not found: " + username);

	    createRoleIfNotExists(roleName);
	    RoleRepresentation role = getRole(roleName);
	    if (role == null)
		throw new RuntimeException("Role not found: " + roleName);

	    keycloak.realm("ThreadStacks").users().get(userId).roles().realmLevel()
		    .add(Collections.singletonList(role));
	} catch (Exception e) {
	    System.err.println(e.getMessage());
	    queueRoleAssignmentEvent(username, roleName);

	}
    }

    public UserRepresentation getUserByUsername(String username) {
	Keycloak keycloak = keycloakProvider.getKeycloakInstance();
	return keycloak.realm(realm).users().search(username).stream().findFirst().orElse(null);
    }

    private void createRoleIfNotExists(String roleName) {
	Keycloak keycloak = keycloakProvider.getKeycloakInstance();
	RolesResource rolesResource = keycloak.realm(realm).roles();

	try {
	    rolesResource.get(roleName).toRepresentation();
	} catch (NotFoundException e) {
	    RoleRepresentation newRole = new RoleRepresentation();
	    newRole.setName(roleName);
	    newRole.setDescription("Dynamically created role for moderation");
	    rolesResource.create(newRole);
	    RoleRepresentation createdRole = rolesResource.get(roleName).toRepresentation();
	    String clientScopeId = getRealmRolesClientScopeId();
	    if (clientScopeId != null) {
		addRoleToClientScope(createdRole, clientScopeId);
	    }
	} catch (Exception e) {
	    System.err.println(e.getMessage());
	    e.printStackTrace();
	}
    }

    public boolean isKeycloakAlive() {
	try {
	    keycloakProvider.getAccessToken();
	    return true;
	} catch (Exception e) {
	    return false;
	}
    }

    private String getUserIdByUsername(String username) {
	Keycloak keycloak = keycloakProvider.getKeycloakInstance();
	return keycloak.realm(realm).users().search(username).stream().findFirst().map(UserRepresentation::getId)
		.orElse(null);
    }

    private RoleRepresentation getRole(String roleName) {
	Keycloak keycloak = keycloakProvider.getKeycloakInstance();
	return keycloak.realm(realm).roles().get(roleName).toRepresentation();
    }

    private void queueFailedUserEvent(String username, String email, String password) {
	if (failedKeycloakEventRepository.findByUsernameAndEventType(username, "CREATE_USER").isPresent()) {
	    return;
	}
	FailedKeycloakEvent failedEvent = new FailedKeycloakEvent(null, username, null, password, "CREATE_USER", email,
		LocalDateTime.now());

	failedKeycloakEventRepository.save(failedEvent)
		.doOnSuccess(event -> RetryUtility.SHOULDRETRYKEYCLOAKUSERCREATION.set(true)).subscribe();
    }

    private void queueRoleAssignmentEvent(String username, String rolename) {
	if (failedKeycloakEventRepository.findByUsernameAndRoleName(username, rolename).isPresent()) {
	    return;
	}

	FailedKeycloakEvent failedEvent = new FailedKeycloakEvent(null, username, null, null, "ASSIGN_ROLE", null,
		LocalDateTime.now());

	failedKeycloakEventRepository.save(failedEvent)
		.doOnSuccess(event -> RetryUtility.SHOULDRETRYKEYCLOAKUSERCREATION.set(true)).subscribe();
    }

    private String getRealmRolesClientScopeId() {
	Keycloak keycloak = keycloakProvider.getKeycloakInstance();
	List<ClientScopeRepresentation> scopes = keycloak.realm(realm).clientScopes().findAll();
	return scopes.stream().filter(scope -> "realm-roles".equals(scope.getName()))
		.map(ClientScopeRepresentation::getId).findFirst().orElse(null);
    }

    private void addRoleToClientScope(RoleRepresentation roleRep, String clientScopeId) {
	Keycloak keycloak = keycloakProvider.getKeycloakInstance();
	keycloak.realm(realm)
	    .clientScopes()
	    .get(clientScopeId)
	    .getScopeMappings()
	    .realmLevel()
	    .add(Collections.singletonList(roleRep));

    }
}
