package client.application;

import client.application.model.Client;
import client.application.model.Stocks;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(properties = "server.port=8090")
@Testcontainers
class ApplicationTests {
	public static final String COMPANY_NAME = "sudo";
	public static final double STOCK_PRICE = 100.0;
	public static final String CLIENT_NAME_1 = "denis";
	public static final String CLIENT_NAME_2 = "mishka";
	public static final String CLIENT_NAME_3 = "kirill";

	public static final String CLIENT = "http://localhost:8090";
	public static final String SERVER = "http://localhost:8080";

	@Container
	private GenericContainer server = new FixedHostPortGenericContainer("springio/gs-spring-boot-docker")
			.withFixedExposedPort(8080, 8080)
			.withExposedPorts(8080);

	@Autowired
	private RestTemplateBuilder restTemplateBuilder;

	@BeforeEach
	void setUp() throws URISyntaxException, IOException, InterruptedException {
		HttpClient client = HttpClient.newHttpClient();
		var addCompanyRequest = HttpRequest.newBuilder()
				.uri(new URI(SERVER + "/stocks/register/" + COMPANY_NAME + "/100/" + String.valueOf(STOCK_PRICE)))
				.GET()
				.build();
		client.send(addCompanyRequest, HttpResponse.BodyHandlers.ofString());
	}

	@Test
	void stockBuyingTest() {
		int moneyToAdd = 5000;
		int stocksCountToBuy = 1;

		Client client = new Client(CLIENT_NAME_1);
		client.addMoney(moneyToAdd);

		RestTemplate restTemplate = restTemplateBuilder.build();
		restTemplate.getForEntity(CLIENT + "/client/register/" + CLIENT_NAME_1, String.class);
		restTemplate.getForEntity(CLIENT + "/client/add/" + CLIENT_NAME_1 + "/" + moneyToAdd, String.class);

		var clientInfo = restTemplate.getForEntity(CLIENT + "/client/info/" + CLIENT_NAME_1, String.class).getBody();

		Assertions.assertEquals(clientInfo, client.toString());

		restTemplate.getForEntity(CLIENT + "/client/buy/" + CLIENT_NAME_1 + "/" + COMPANY_NAME + "/" + stocksCountToBuy, String.class);

		client.buyStocks(new Stocks(COMPANY_NAME, stocksCountToBuy, 100));

		clientInfo = restTemplate.getForEntity(CLIENT + "/client/info/" + CLIENT_NAME_1, String.class).getBody();

		Assertions.assertEquals(clientInfo, client.toString());
	}

	@Test
	void moneyChangedTest() throws InterruptedException, URISyntaxException, IOException {
		double moneyToAdd = 300.0;
		int stocksCountToBuy = 2;
		long stockPriceIncrease = 100;

		HttpClient httpClient = HttpClient.newHttpClient();
		var incStocks = HttpRequest.newBuilder()
				.uri(new URI(SERVER + "/stocks/inc/" + COMPANY_NAME + "/" + String.valueOf(10000000000L)))
				.GET()
				.build();

		var decStocks = HttpRequest.newBuilder()
				.uri(new URI(SERVER + "/stocks/dec/" + COMPANY_NAME + "/" + String.valueOf(10000000000L)))
				.GET()
				.build();

		Client client = new Client(CLIENT_NAME_2);
		Stocks sudoStocks = new Stocks(COMPANY_NAME, stocksCountToBuy, STOCK_PRICE);

		RestTemplate restTemplate = restTemplateBuilder.build();

		restTemplate.getForEntity(CLIENT + "/client/register/" + CLIENT_NAME_2, String.class);

		var clientMoney = restTemplate.getForEntity(CLIENT + "/client/money/" + CLIENT_NAME_2, String.class).getBody();

		Assertions.assertEquals(0.0, Double.valueOf(clientMoney));

		client.addMoney(moneyToAdd);

		restTemplate.getForEntity(CLIENT + "/client/add/" + CLIENT_NAME_2 + "/" + Integer.valueOf((int) moneyToAdd).toString(), String.class);

		clientMoney = restTemplate.getForEntity(CLIENT + "/client/money/" + CLIENT_NAME_2, String.class).getBody();

		Assertions.assertEquals(moneyToAdd, Double.valueOf(clientMoney));

		restTemplate.getForEntity(CLIENT + "/client/buy/" + CLIENT_NAME_2 + "/" + COMPANY_NAME + "/" + stocksCountToBuy, String.class);

		clientMoney = restTemplate.getForEntity(CLIENT + "/client/money/" + CLIENT_NAME_2, String.class).getBody();

		var oldMoney = clientMoney;

		for (int i = 0; i < 10; i++) {
			httpClient.send(incStocks, HttpResponse.BodyHandlers.ofString());

			var newMoney = restTemplate.getForEntity(CLIENT + "/client/money/" + CLIENT_NAME_2, String.class).getBody();

			Assertions.assertTrue(Double.valueOf(oldMoney) < Double.valueOf(newMoney));

			oldMoney = newMoney;
		}

		for (int i = 0; i < 10; i++) {
			httpClient.send(decStocks, HttpResponse.BodyHandlers.ofString());

			var newMoney = restTemplate.getForEntity(CLIENT + "/client/money/" + CLIENT_NAME_2, String.class).getBody();

			Assertions.assertTrue(Double.valueOf(oldMoney) > Double.valueOf(newMoney));

			oldMoney = newMoney;
		}
	}

	@Test
	void notEnoughMoneyTest() {
		double moneyToAdd = 30.0;

		RestTemplate restTemplate = restTemplateBuilder.build();
		restTemplate.getForEntity(CLIENT + "/client/register/" + CLIENT_NAME_3, String.class);

		var respone = restTemplate.getForEntity(CLIENT + "/client/buy/" + CLIENT_NAME_3 + "/" + COMPANY_NAME + "/1", String.class).getBody();

		Assertions.assertEquals("No enough money! Try later!", respone);

		restTemplate.getForEntity(CLIENT + "/client/add/" + CLIENT_NAME_3 + "/" + Integer.valueOf((int) moneyToAdd).toString(), String.class);

		respone = restTemplate.getForEntity(CLIENT + "/client/buy/" + CLIENT_NAME_3 + "/" + COMPANY_NAME + "/1", String.class).getBody();

		Assertions.assertEquals("No enough money! Try later!", respone);

		restTemplate.getForEntity(CLIENT + "/client/add/" + CLIENT_NAME_3 + "/" + Integer.valueOf(10 * (int)STOCK_PRICE).toString(), String.class);

		respone = restTemplate.getForEntity(CLIENT + "/client/buy/" + CLIENT_NAME_3 + "/" + COMPANY_NAME + "/1", String.class).getBody();

		Assertions.assertEquals("Success", respone);

		restTemplate.getForEntity(SERVER + "/stocks/inc/" + COMPANY_NAME + "/" + Integer.valueOf(10 * (int)STOCK_PRICE), String.class);

		respone = restTemplate.getForEntity(CLIENT + "/client/buy/" + CLIENT_NAME_3 + "/" + COMPANY_NAME + "/1", String.class).getBody();

		Assertions.assertEquals("No enough money! Try later!", respone);
	}

}
