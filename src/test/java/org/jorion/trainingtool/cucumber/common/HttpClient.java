package org.jorion.trainingtool.cucumber.common;

import org.jorion.trainingtool.dto.json.UserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

// @Component
// @Scope(SCOPE_CUCUMBER_GLUE)
public class HttpClient {

    private static final String SERVER_URL = "http://localhost";
    private static final String REST_ENDPOINT = "/trainingtool/REST/v1";

    private int port;
    private final RestTemplate restTemplate = new RestTemplate();

    private String endpoint() {
        return SERVER_URL + ":" + port + REST_ENDPOINT;
    }

    public UserDTO getUser(String username) {

        String url = endpoint() + "/users/" + username;
        System.out.println("URL: " + url);
        ResponseEntity<UserDTO> entity = restTemplate.getForEntity(url, UserDTO.class);
        System.out.println(entity.getStatusCode());
        return entity.getBody();
    }
}
