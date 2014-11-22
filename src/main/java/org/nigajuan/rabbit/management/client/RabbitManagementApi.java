package org.nigajuan.rabbit.management.client;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.nigajuan.rabbit.management.client.domain.aliveness.Status;
import org.nigajuan.rabbit.management.client.domain.binding.Binding;
import org.nigajuan.rabbit.management.client.domain.binding.exchange.ExchangeBind;
import org.nigajuan.rabbit.management.client.domain.binding.queue.QueueBind;
import org.nigajuan.rabbit.management.client.domain.exchange.Exchange;
import org.nigajuan.rabbit.management.client.domain.permission.Permission;
import org.nigajuan.rabbit.management.client.domain.policy.Policy;
import org.nigajuan.rabbit.management.client.domain.queue.Queue;
import org.nigajuan.rabbit.management.client.domain.user.User;
import org.nigajuan.rabbit.management.client.domain.vhost.Vhost;
import retrofit.RestAdapter;
import retrofit.client.Response;
import retrofit.converter.JacksonConverter;
import retrofit.http.*;

import java.util.List;

/**
 * Created by nlabrot on 21/11/14.
 */
public interface RabbitManagementApi {

    //****************************************** Virtual Host ******************************************
    @GET("/api/vhosts")
    List<Vhost> listVirtualHost();

    @PUT("/api/vhosts/{vhost}")
    @Headers("Content-Type: application/json")
    Response createVirtualHost(@Path("vhost") String vhost);

    @DELETE("/api/vhosts/{vhost}")
    Response deleteVirtualHost(@Path("vhost") String vhost);


    //****************************************** Queue ******************************************
    @GET("/api/queues")
    List<Queue> listQueues();

    @GET("/api/queues/{vhost}")
    List<Queue> listQueues(@Path("vhost") String vhost);

    @PUT("/api/queues/{vhost}/{name}")
    Response createQueue(@Path("vhost") String vhost, @Path("name") String name, @Body Queue exchange);

    @DELETE("/api/queues/{vhost}/{name}")
    Response deleteQueue(@Path("vhost") String vhost, @Path("name") String name);

    @DELETE("/api/queues/{vhost}/{name}/contents")
    Response purgeQueue(@Path("vhost") String vhost, @Path("name") String name);


    //****************************************** Exchange ******************************************
    @GET("/api/exchanges")
    List<Exchange> listExchanges();

    @GET("/api/exchanges/{vhost}")
    List<Exchange> listExchanges(@Path("vhost") String vhost);

    @PUT("/api/exchanges/{vhost}/{name}")
    Response createExchange(@Path("vhost") String vhost, @Path("name") String name, @Body Exchange exchange);

    @DELETE("/api/exchanges/{vhost}/{name}")
    Response deleteExchange(@Path("vhost") String vhost, @Path("name") String name);


    //****************************************** Binding ******************************************
    @GET("/api/bindings")
    List<Binding> listBindings();

    @GET("/api/bindings/{vhost}")
    List<Binding> listBindings(@Path("vhost") String vhost);

    @POST("/api/bindings/{vhost}/e/{exchange}/q/{queue}")
    Response bindExchangeToQueue(@Path("vhost") String vhost, @Path("exchange") String exchange, @Path("queue") String queue, @Body QueueBind bind);

    @POST("/api/bindings/{vhost}/e/{source}/e/{destination}")
    Response bindExchangeToExchange(@Path("vhost") String vhost, @Path("source") String source, @Path("destination") String destination, @Body ExchangeBind bind);


    //****************************************** User ******************************************
    @GET("/api/users")
    List<User> listUsers();

    @PUT("/api/users/{user}")
    Response createUser(@Path("user") String name, @Body User user);

    @DELETE("/api/users/{user}")
    Response deleteUser(@Path("user") String name);


    //****************************************** Permission ******************************************
    @PUT("/api/permissions/{vhost}/{user}")
    Response createPermission(@Path("vhost") String vhost, @Path("user") String user, @Body Permission permission);

    @DELETE("/api/permissions/{vhost}/{user}")
    Response deletePermission(@Path("vhost") String vhost, @Path("user") String user);


    //****************************************** Policy ******************************************
    @GET("/api/policies/{vhost}")
    List<Policy> listPolicies(@Path("vhost") String vhost);

    @PUT("/api/policies/{vhost}/{name}")
    Response createPolicy(@Path("vhost") String vhost, @Path("name") String name, @Body Policy policy);

    @DELETE("/api/policies/{vhost}/{name}")
    Response deletePolicy(@Path("vhost") String vhost, @Path("name") String name);


    //****************************************** Aliveness Test ******************************************
    @GET("/api/aliveness-test/{vhost}")
    Status alivenessTest(@Path("vhost") String vhost);



    static RabbitManagementApi newInstance(String endpoint, String username, String password) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        RestAdapter.Builder builder = new RestAdapter.Builder()
                .setEndpoint(endpoint)
                .setConverter(new JacksonConverter(objectMapper));

        if (username != null && password != null) {
            builder.setRequestInterceptor(new AuthenticationRequestInterceptor(username, password));
        }

        return builder.build().create(RabbitManagementApi.class);
    }

}
