package org.kendar.amqp;

import com.google.common.io.Files;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.qpid.server.SystemLauncher;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class FirstTest {
    static SystemLauncher broker = new SystemLauncher();
    private static File tmpFolder;
    private static int amqpPort;
    private static int httpPort;
    private static String qpidHomeDir;
    private static String configFileName;

    @BeforeAll
    static void before() throws Throwable {
        startQpidBroker();
        //createExchange();
    }

    @AfterAll
    static void after() {
        broker.shutdown();
    }

    static void startQpidBroker() throws Exception {
        tmpFolder = Files.createTempDir();


        amqpPort = 5672;
        httpPort = 8081;

        qpidHomeDir = "src/integTest/resources/";
        configFileName = "/test-config.json";
        File file = new File(qpidHomeDir);
        String homePath = file.getAbsolutePath();

        Map<String, Object> attributes = new HashMap<>();

        attributes.put("qpid.work_dir", tmpFolder.getAbsolutePath());

        attributes.put("qpid.amqp_port",amqpPort);
        attributes.put("qpid.http_port", httpPort);
        attributes.put("qpid.home_dir", homePath);
        
        attributes.put("type", "Memory");
        attributes.put("initialConfigurationLocation", findResourcePath("test-config.json"));
        broker.startup(attributes);
    }

    static String findResourcePath(final String fileName) {
        return FirstTest.class.getClassLoader().getResource(fileName).toExternalForm();
    }

    @Test
    void doTest() throws Exception {
        createConnection();
        System.out.println("TEST");
    }

    private void createConnection() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri("amqp://guest:password@localhost:"+amqpPort);


        var connection = factory.newConnection();
        //get a channel for sending the "kickoff" message
        var channel = connection.createChannel();

        channel.exchangeDeclare("EXCHANGE_NAME", "direct", true);
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, "EXCHANGE_NAME", "ROUTING_KEY");
    }
}
