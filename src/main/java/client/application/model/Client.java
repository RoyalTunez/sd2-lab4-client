package client.application.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Client {
    private final String login;
    private double money = 0.0;
    private Map<String, Stocks> stocks = new HashMap<>();

    public Client(String login) {
        this.login = login;
    }

    public double getMoney() {
        return money;
    }

    public List<Stocks> getStocks() {
        return List.copyOf(stocks.values());
    }

    public void addMoney(double change) {
        money += change;
    }

    public void buyStocks(Stocks newStocks) {
        var stockCopy = stocks.putIfAbsent(newStocks.getName(), newStocks);

        if (stockCopy != null && money > newStocks.getTotalPrice()) {
            stockCopy.changeAmount(newStocks.getAmount());
            money -= newStocks.getTotalPrice();
        }
    }

    public boolean sellStocks(Stocks stocksForSelling) {
        var stockCopy = stocks.putIfAbsent(stocksForSelling.getName(), stocksForSelling);

        if (stockCopy != null && stockCopy.getAmount() >= stocksForSelling.getAmount()) {
            stockCopy.changeAmount(-stocksForSelling.getAmount());
            money += stocksForSelling.getTotalPrice();

            if (stockCopy.getAmount() == 0) {
                stocks.remove(stockCopy.getName());
            }
            return true;
        }

        return false;
    }


    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();

        s.append("<h3>Client: ").append(login).append("</h3>");
        s.append("money: ").append(money);
        s.append("<br>Companies stocks bought: ").append(stocks.size());
        double totalMoney = 0.0;
        for (var companyStocks: stocks.values()) {
            s.append("<br>&ensp;").append(companyStocks.getName()).append(" bought:").append(companyStocks.getAmount());
        }
        return s.toString();
    }
}
