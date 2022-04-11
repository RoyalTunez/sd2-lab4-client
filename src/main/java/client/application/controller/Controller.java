package client.application.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@RestController
@RequestMapping("client")
public class Controller {
    public static final String SERVER = "http://localhost:8080";

    @RequestMapping("/register/{login}")
    public String clientRegister(@PathVariable String login) {
        return send("/client/register/" + login);
    }

    @RequestMapping("/add/{login}/{change}")
    public String clientAddMoney(@PathVariable String login, @PathVariable String change) {
        return send("/client/add/" + login + "/" + change);
    }

    @RequestMapping("/info/{login}")
    public String clientInfo(@PathVariable String login) {
        return send("/client/info/" + login);
    }

    @RequestMapping("/buy/{login}/{companyName}/{amount}")
    public String clientBuyStocks(@PathVariable String login, @PathVariable String companyName, @PathVariable String amount) {
        return send("/client/buy/" + login + "/" + companyName + "/" + amount);
    }

    @RequestMapping("/sell/{login}/{companyName}/{amount}")
    public String clientSellStocks(@PathVariable String login, @PathVariable String companyName, @PathVariable String amount) {
       return send("/client/sell/" + login + "/" + companyName + "/" + amount);
    }

    @RequestMapping("/money/{login}")
    public String clientSellStocks(@PathVariable String login) {
        return send("/client/money/" + login);
    }

    private String send(String uri) {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(new URI(SERVER + uri)).GET().build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            return response.body().toString();
        } catch (URISyntaxException uriException) {
            return "Wrong uri";
        } catch (Exception exception) {
            return "Something goes wrong. Try again!";
        }
    }
}
