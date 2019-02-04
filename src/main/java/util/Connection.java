package util;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import evaluation.Factor;
import evaluation.StrategicIndicator;

import java.io.IOException;

public class Connection {
    private static RestHighLevelClient client;
    private static RestClient lowLevelClient;
    private static final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();


    /**
     *  This method set up the connection to the elasticsearch RESTful services. In order to access to the elasticsearch
     *  RESTful services, we construct the connection string as: <ip>:<port>/<path>/
     *  To query the indexes, we added the prefix to the index name.
     *
     * @param ip IP where the elasticsearch is available [MANDATORY]
     * @param port PORT where the elasticsearch restful services are available [MANDATORY]
     * @param path path where the q-rapids indexes are located. If this path does not end with the character '/', it is added. [OPTIONAL]
     * @param prefix prefix used to group all the q-rapids indexes. If this preffix does not end with the character '.', it is added. [OPTIONAL]
     * @param username credentials when the elasticsearch requires them. [OPTIONAL]
     * @param password credentials when the elasticsearch requires them. [OPTIONAL]
     *
     * @return
     * @throws IOException
     */
    public static void initConnection(String ip, int port, String path, String prefix, String username, String password) {

        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        lowLevelClient = RestClient.builder(
                new HttpHost(ip, port))
                .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                    @Override
                    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                        httpClientBuilder.disableAuthCaching();
                        return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    }
                })
                .build();
        client = new RestHighLevelClient(lowLevelClient);

        // The prefix needs to end with the character '.'
        if (!prefix.isEmpty() && !prefix.endsWith("."))
            prefix=prefix.concat(".");
        Constants.INDEX_PREFIX = prefix;
        // The path needs to end with the character '/'
        if (!path.isEmpty() && !path.endsWith("/"))
            path=path.concat("/");
        Constants.PATH = path;
    }

    public static RestHighLevelClient getConnectionClient() {
        return client;
    }

    public static RestClient getLowLevelConnectionClient() {
        return lowLevelClient;
    }

    public static void closeConnection() throws IOException {
        lowLevelClient.close();
        Factor.resetFactorsIDNames();
        StrategicIndicator.resetFactorsIDNames();
    }

}
