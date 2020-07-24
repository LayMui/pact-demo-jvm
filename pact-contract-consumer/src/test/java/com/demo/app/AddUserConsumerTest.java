package com.demo.app;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.PactProviderRule;
import au.com.dius.pact.consumer.junit.PactVerification;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.jayway.jsonpath.JsonPath;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class AddUserConsumerTest {
    @Rule
    public PactProviderRule mockProvider = new PactProviderRule("user_provider","localhost", 8080, this);
    private RestTemplate restTemplate=new RestTemplate();

    @Pact(provider = "user_provider", consumer = "user_consumer")
    public RequestResponsePact createPact(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", MediaType.APPLICATION_JSON_VALUE);


        PactDslJsonBody bodyResponse = new PactDslJsonBody()
                .integerType("uuid", 12345)
                .stringValue("username", "mike")
                .stringValue("firstName", "mike")
                .stringValue("lastName", "tan");

        return builder
                .given("create user").uponReceiving("a request to add user")
                //.path("/api/admin/users?username=mike&firstName=mike&lastName=tan")
                .path("/api/users")
                .body(bodyResponse)
                .headers(headers)
                .method(RequestMethod.POST.name())
                .willRespondWith()
                .headers(headers)
                .status(200).body(bodyResponse).toPact();
    }

    @Test
    @PactVerification
    public void testCreateUserConsumer() throws IOException {

        User user = new User(12345, "mike", "mike", "tan");
        HttpHeaders headers=new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> request=new HttpEntity<Object>(user, headers);
        System.out.println("MOCK provider URL"+mockProvider.getUrl());
        ResponseEntity<String> responseEntity=restTemplate.postForEntity(mockProvider.getUrl()+"/api/users", request, String.class);
        assertEquals("mike", JsonPath.read(responseEntity.getBody(),"$.username"));
        assertEquals("mike", JsonPath.read(responseEntity.getBody(),"$.firstName"));
        assertEquals("tan", JsonPath.read(responseEntity.getBody(),"$.lastName"));
    }
}
