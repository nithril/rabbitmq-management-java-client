rabbitmq-management-java-client
===============================

A Java client for RabbitMQ Management API. 

The purpose of this project is more too share code (fork and adapt to your need) than a project with solid support. Anyway I will give a look at your issues.

Implemented endpoints are listed here [```RabbitManagementApi```](https://github.com/nithril/rabbitmq-management-java-client/blob/master/src/main/java/org/nigajuan/rabbit/management/client/RabbitManagementApi.java). Not all endpoints are implemented.

This client uses [jsonschema2pojo](https://github.com/joelittlejohn/jsonschema2pojo) 
to generate Java Bean from the Jsons outputed by the Management API. Http requests are handled by [the square retrofit project](https://github.com/square/retrofit)

```java
public interface RabbitManagementApi {

    //****************************************** Virtual Host ******************************************
    @GET("/api/vhosts")
    List<Vhost> listVirtualHost();

    @PUT("/api/vhosts/{vhost}")
    @Headers("Content-Type: application/json")
    Response createVirtualHost(@Path("vhost") String vhost);
    
    //[...]
}
``` 

The Http client is basic. Only Http with basic auth is implemented. The http client used by retrofit can be configured to support TLS.


How to use? 
-------------

```java
RabbitManagementApi api = RabbitManagementApi.newInstance("http://localhost:15672/" , "user" , "password");

List<User> user = api.listUsers();

api.createQueue("myvhost", "queue1", new Queue()
      .withDurable(true)
      .withArguments(new Arguments()
      .withXMaxLength(200l)));

``` 

Take a look at ```org.nigajuan.rabbit.management.client.TestClient```


