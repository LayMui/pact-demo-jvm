## Consumer Driven Contracts Using Pact Framework

#### Projects

<table>
 <tr>
 <th>Project</th><th> Description</th>
</tr>
<tr>
<td><b>pact-contract-provider</b></td>
<td>provider application</td>
</tr>
<tr>
<td><b>pact-contract-consumer</b></td>
<td>consumer application</td>
</tr>
	
</table>

#### Compatability Matrix

choose the branch based on below maintained versions.

<table>
 <tr>
    <th style="text-align:left">Branch/Version</th>
    <th style="text-align:left">Spring Boot</th>
    <th style="text-align:left">Pact Broker</th>
  </tr>
  <tr>
    <td>master</td>
    <td>2.2.6.RELEASE</td>
    <td>4.0.10</td>
  </tr>
    <tr>
    <td>v2.0</td>
    <td>2.1.5.RELEASE</td>
    <td>3.5.7</td>
  </tr>
  <tr>
    <td>v1.0</td>
    <td>1.5.7.RELEASE</td>
    <td>3.5.7</td>
  </tr>  
</table>

### Run Pact Broker 

- Start pact broker as docker containers

```
git clone https://github.com/pact-foundation/pact-broker-docker
$ cd pact-broker-docker
$ docker-compose up -d
```

- View Pact broker [url](http://localhost:9292)

![pack broker view](images/pact_broker_view.png)


### Guide for Pact Consumer

Start with consumer first, As it is consumer driven contract framework. 

- Add below dependencies in pom.xml

```xml
        	 <dependency>
		    <groupId>au.com.dius</groupId>
		    <artifactId>pact-jvm-consumer-junit_2.12</artifactId>
		    <version>3.6.7</version>
		    <scope>test</scope>
		</dependency>
```
- Add pact jvm provider maven plugin

```xml
	    <plugin>
                <groupId>au.com.dius</groupId>
                <artifactId>pact-jvm-provider-maven_2.12</artifactId>
                <version>3.6.7</version>
                <configuration>
                   <pactBrokerUrl>${pact.broker.url}</pactBrokerUrl>
		   <pactDirectory>target/pacts</pactDirectory>               
                </configuration>
            </plugin>

```

- Write the consumer contract test 

```java
public class AddUserConsumerTest{
	
    @Rule
    public PactProviderRule mockProvider = new PactProviderRule("user_provider","localhost", 8080, this);
    private RestTemplate restTemplate=new RestTemplate();


    @Pact(provider = "user_provider", consumer = "user_consumer")
    public RequestResponsePact createPact(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", MediaType.APPLICATION_JSON_VALUE);


        PactDslJsonBody bodyResponse = new PactDslJsonBody()
                .stringValue("username", "mike")
                 .stringType("firstName", "mike")               
                .integerType("lastName", "tan");

        return builder
        		.given("create user").uponReceiving("a request to add user")
                .path("/api/user")
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

        User user = new User(12345,"mike", "mike", "tan");
        HttpHeaders headers=new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> request=new HttpEntity<Object>(user, headers);
        System.out.println("MOCK provider URL"+mockProvider.getUrl());
        ResponseEntity<String> responseEntity=restTemplate.postForEntity(mockProvider.getUrl()+"/api/user", request, String.class);
        assertEquals("mike", JsonPath.read(responseEntity.getBody(),"$.username"));
        assertEquals("mike", JsonPath.read(responseEntity.getBody(),"$.firstName"));
        assertEquals("tan", JsonPath.read(responseEntity.getBody(),"$.lastName"));
    }
}
```

- Run maven build to publish the pacts to the pact broker

```sh
cd pact-contract-consumer
mvn clean install pact:publish -Dpact.broker.url=http://localhost:9292
```

- Verify the pact broker with the contracts


### Guide for Pact Provider 

Now move on to provider side

-  Add the below dependencies in pom.xml

```xml
     	<properties>		
		<pact.version>4.0.10</pact.version>
	</properties>
	<dependency>
	    <groupId>au.com.dius</groupId>
	    <artifactId>pact-jvm-provider-junit</artifactId>
	    <version>${pact.version}</version>
	    <scope>test</scope>
	</dependency>
	 <dependency>
	    <groupId>au.com.dius</groupId>
	    <artifactId>pact-jvm-provider-spring</artifactId>
	    <version>${pact.version}</version>
	    <scope>test</scope>
	</dependency>
```

- Add the pact provider test case

Test case to test against the pacts from the pact broker  to test against the pacts from the pact broker 

```java

@RunWith(SpringRestPactRunner.class)
@SpringBootTest(classes=Application.class,properties={"spring.profiles.active=test","spring.cloud.config.enabled=false"},webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@PactBroker(host="${pact.host}",port="${pact.port}")
@Provider("user_provider")
public class UserProviderTest {

    @MockBean
    private UserService userService;

    @TestTarget
    public final Target target = new HttpTarget(9050);

    @State(value="create user")
    public void createUserState() throws Exception{

        User user=new User(1234, "mike", "mike", "tan");
        when(userService.addUser(any(User.class))).thenReturn(user) ;
    }
}

```

- Run maven build at the provider side

```sh
cd pact-contract-provider
mvn clean install  -Dpact.verifier.publishResults=true
(or)
mvn test -Dpact.verifier.publishResults=true
```


#### Notes: 

1. Using @PactBroker(host="localhost",port="9292") to define the pact broker host and port.
2. Using ```SpringRestPactRunner``` to load the spring container using @SpringBootTest.
3. Starting the application server using the line ```new HttpTarget(9050)```.
4. Mentioning Pact broker url and Pact directory is key to generate pacts at the consumer side.



