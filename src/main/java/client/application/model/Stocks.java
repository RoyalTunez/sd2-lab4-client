package client.application.model;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

public class Stocks {
    final double CHANGE_LOWER_BOUND = 0.99;
    final double CHANGE_UPPER_BOUND = 1.01;
    final long CHANGE_FREQUENCY = 10;

    private long amount = 0;
    private double price = 0.0;
    private final String companyName;

    public long getAmount() {
        return amount;
    }

    public void changeAmount(long change) {
        if (amount + change >= 0) {
            amount += change;
        }
    }

    public void changePrice(double change) {
        if (price + change > -1e-9) {
            price += change;
        }
    }

    public double getPrice() {
        return price;
    }

    public String getName() {
        return companyName;
    }

    public Stocks(String companyName, long amount, double argPrice) {
        this.companyName = companyName;
        this.price = argPrice;
        this.amount = amount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(companyName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Stocks stocks = (Stocks) o;

        return companyName.equals(stocks.companyName);
    }

    @Override
    public String toString() {
        return "<h3>Company: " + companyName + "</h3>stocks: " + String.valueOf(amount) + "<br>price: " + String.valueOf(price) + "<br><h3>Total: "+ String.valueOf(getTotalPrice()) +"</h3>";
    }

    public double getTotalPrice() {
        return price * amount;
    }
}
