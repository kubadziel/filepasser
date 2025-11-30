package uploader.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KeycloakUserService {

    @Value("${keycloak.validation.enabled:true}")
    private boolean validationEnabled;

    @Value("${keycloak.validation.token-url:}")
    private String tokenUrl;

    @Value("${keycloak.validation.users-url:}")
    private String usersUrl;

    @Value("${keycloak.validation.client-id:admin-cli}")
    private String clientId;

    @Value("${keycloak.validation.username:}")
    private String adminUsername;

    @Value("${keycloak.validation.password:}")
    private String adminPassword;

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean contractExists(String contractId) {
        if (!validationEnabled) {
            return true;
        }
        try {
            String token = fetchAdminToken();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            String url = usersUrl + "?q=contractId:" + contractId + "&exact=true&max=1";
            List<?> result = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, List.class).getBody();
            return result != null && !result.isEmpty();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to verify contract ID with Keycloak", ex);
        }
    }

    private String fetchAdminToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.put("grant_type", Collections.singletonList("password"));
        body.put("client_id", Collections.singletonList(clientId));
        body.put("username", Collections.singletonList(adminUsername));
        body.put("password", Collections.singletonList(adminPassword));
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        Map<?, ?> response = restTemplate.postForEntity(tokenUrl, request, Map.class).getBody();
        if (response == null || response.get("access_token") == null) {
            throw new IllegalStateException("Unable to obtain admin token from Keycloak");
        }
        return response.get("access_token").toString();
    }
}
