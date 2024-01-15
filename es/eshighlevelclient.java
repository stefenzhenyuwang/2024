// Create the low-level client
RestClient httpClient = RestClient.builder(
    new HttpHost("localhost", 9200)
).build();

// Create the HLRC
RestHighLevelClient hlrc = new RestHighLevelClientBuilder(httpClient)
    .setApiCompatibilityMode(true) 
    .build();

// Create the Java API Client with the same low level client
ElasticsearchTransport transport = new RestClientTransport(
    httpClient,
    new JacksonJsonpMapper()
);

ElasticsearchClient esClient = new ElasticsearchClient(transport);

// hlrc and esClient share the same httpClient