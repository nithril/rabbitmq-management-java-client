package org.nigajuan.rabbit.management.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.nigajuan.rabbit.management.client.AuthenticationRequestInterceptor;
import org.nigajuan.rabbit.management.client.RabbitManagementApi;
import org.nigajuan.rabbit.management.client.domain.binding.exchange.ExchangeBind;
import org.nigajuan.rabbit.management.client.domain.binding.queue.QueueBind;
import org.nigajuan.rabbit.management.client.domain.exchange.Exchange;
import org.nigajuan.rabbit.management.client.domain.permission.Permission;
import org.nigajuan.rabbit.management.client.domain.queue.Arguments;
import org.nigajuan.rabbit.management.client.domain.queue.Queue;
import org.nigajuan.rabbit.management.client.domain.user.User;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.converter.JacksonConverter;

import static org.testng.Assert.*;

/**
 * Created by nlabrot on 21/11/14.
 */
public class TestClient {

    public static final String VIRTUAL_HOST = "testapivhost";
    public static final String USERNAME = "testapiuser";
    public static final String PASSWORD = "testapiuser";

    public static final String ADMIN_USERNAME = "user";
    public static final String ADMIN_PASSWORD = "password";

    public static final String ENDPOINT = "http://localhost:15672/";



    private RabbitManagementApi apiService;
    private AuthenticationRequestInterceptor requestInterceptor;

    @BeforeClass
    public void init() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        requestInterceptor = new AuthenticationRequestInterceptor(ADMIN_USERNAME, ADMIN_PASSWORD);

        apiService = new RestAdapter.Builder()
                .setRequestInterceptor(requestInterceptor)
                .setEndpoint(ENDPOINT)
                .setConverter(new JacksonConverter(objectMapper))
                .build()
                .create(RabbitManagementApi.class);
    }

    @BeforeMethod
    public void deleteVirtualHost() {
        try {
            apiService.deleteVirtualHost(VIRTUAL_HOST);
        } catch (RetrofitError e) {
            assertEquals(e.getResponse().getStatus() , 404);
        }
    }

    @Test
    public void testVirtualHost() {
        apiService.createVirtualHost(VIRTUAL_HOST);
        assertTrue(apiService.listVirtualHost().stream().filter(v -> VIRTUAL_HOST.equals(v.getName())).findFirst().isPresent());

        apiService.deleteVirtualHost(VIRTUAL_HOST);
        assertFalse(apiService.listVirtualHost().stream().filter(v -> VIRTUAL_HOST.equals(v.getName())).findFirst().isPresent());
    }


    @Test
    public void testScenario() {

        //Create an admin user
        apiService.createUser(USERNAME, new User().withPassword(PASSWORD).withTags("administrator"));
        assertTrue(apiService.listUsers().stream().filter(u -> USERNAME.equals(u.getName())).findFirst().isPresent());

        //Change credential to this new user
        requestInterceptor.setCredentials(USERNAME , PASSWORD);

        //Create a virtual host
        apiService.createVirtualHost(VIRTUAL_HOST);

        try {
            apiService.listQueues(VIRTUAL_HOST);
            throw new RuntimeException("listQueues must not succeed");
        } catch (RetrofitError e) {
            Assert.assertEquals(e.getResponse().getStatus(), 401);
        }

        //Give permission
        apiService.createPermission(VIRTUAL_HOST, USERNAME, new Permission().withConfigure(".*").withRead(".*").withWrite(".*"));

        //Must succeed
        apiService.listQueues(VIRTUAL_HOST);

        //Create a queue
        apiService.createQueue(VIRTUAL_HOST, "q1", new Queue()
                .withDurable(true)
                .withArguments(new Arguments()
                        .withXMaxLength(200l)));

        assertTrue(apiService.listQueues().stream().filter(q -> "q1".equals(q.getName())).findFirst().isPresent());
        assertTrue(apiService.listQueues(VIRTUAL_HOST).stream().filter(q -> "q1".equals(q.getName())).findFirst().isPresent());


        //Create two exchange
        apiService.createExchange(VIRTUAL_HOST, "e1", new Exchange().withType("topic"));
        assertTrue(apiService.listExchanges().stream().filter(e -> "e1".equals(e.getName())).findFirst().isPresent());
        assertTrue(apiService.listExchanges(VIRTUAL_HOST).stream().filter(e -> "e1".equals(e.getName())).findFirst().isPresent());

        apiService.createExchange(VIRTUAL_HOST, "e2", new Exchange().withType("topic"));
        assertTrue(apiService.listExchanges().stream().filter(e -> "e2".equals(e.getName())).findFirst().isPresent());
        assertTrue(apiService.listExchanges(VIRTUAL_HOST).stream().filter(e -> "e2".equals(e.getName())).findFirst().isPresent());


        //Bind exchange -> queue
        apiService.bindExchangeToQueue(VIRTUAL_HOST, "e1", "q1", new QueueBind().withRoutingKey("fff"));
        assertTrue(apiService.listBindings(VIRTUAL_HOST).stream().filter(b ->
                "e1".equals(b.getSource()) && "q1".equals(b.getDestination())).findFirst().isPresent());


        //Bind exchange -> exchange
        apiService.bindExchangeToExchange(VIRTUAL_HOST, "e1", "e2", new ExchangeBind());
        assertTrue(apiService.listBindings(VIRTUAL_HOST).stream().filter(b ->
                "e1".equals(b.getSource()) && "e2".equals(b.getDestination())).findFirst().isPresent());


        //Delete queue
        assertEquals(apiService.listQueues(VIRTUAL_HOST).size(), 1);
        apiService.deleteQueue(VIRTUAL_HOST, "q1");
        assertEquals(apiService.listQueues(VIRTUAL_HOST).size(), 0);


        //Delete exchange
        assertTrue(apiService.listExchanges(VIRTUAL_HOST).stream().filter(e -> "e1".equals(e.getName())).findFirst().isPresent());
        apiService.deleteExchange(VIRTUAL_HOST, "e1");
        assertFalse(apiService.listExchanges(VIRTUAL_HOST).stream().filter(e -> "e1".equals(e.getName())).findFirst().isPresent());

        //Delete user
        requestInterceptor.setCredentials(ADMIN_USERNAME, ADMIN_PASSWORD);
        apiService.deleteUser(USERNAME);
        assertFalse(apiService.listUsers().stream().filter(u -> USERNAME.equals(u.getName())).findFirst().isPresent());

        //Delete vhost
        apiService.deleteVirtualHost(VIRTUAL_HOST);
    }


    @Test
    public void testFactory(){
        RabbitManagementApi.newInstance(ENDPOINT , ADMIN_USERNAME , ADMIN_PASSWORD).listUsers();
    }
}
