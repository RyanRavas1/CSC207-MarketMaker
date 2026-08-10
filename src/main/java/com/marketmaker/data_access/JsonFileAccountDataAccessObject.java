package com.marketmaker.data_access;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDate;

import org.json.JSONArray;
import org.json.JSONObject;

import com.marketmaker.entities.Account;
import com.marketmaker.entities.Order;
import com.marketmaker.entities.Position;
import com.marketmaker.entities.Trade;
import com.marketmaker.use_case.AccountDAO;

/**
 * Persists a full account snapshot to a JSON file, one file per account id.
 *
 * <p>This is the {@link AccountDAO} every trading and reporting use case takes, so saving is
 * not a separate step anyone has to remember: an interactor that changes the account saves it
 * here, and the next launch reads it back.
 */
public class JsonFileAccountDataAccessObject implements AccountDAO {
    private final Path directory;

    public JsonFileAccountDataAccessObject(Path directory) {
        this.directory = directory;
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new AccountPersistenceException("Could not create data directory " + directory, e);
        }
    }

    @Override
    public void save(Account account) {
        try {
            // Written beside the target and moved into place: a crash mid-write would
            // otherwise leave a half-serialised trade log where the real one used to be.
            Path file = fileFor(account.getUserName());
            Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
            Files.writeString(temporary, toJson(account).toString(2));
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new AccountPersistenceException("Could not save account " + account.getUserName(), e);
        }
    }

    /** Reads the account back off disk. Kept public because it names what it does. */
    public Account load(String accountId) {
        Path file = fileFor(accountId);
        if (!Files.exists(file)) {
            return null;
        }
        try {
            return fromJson(new JSONObject(Files.readString(file)));
        } catch (IOException e) {
            throw new AccountPersistenceException("Could not load account " + accountId, e);
        }
    }

    // AccountDAO names the same lookup "get"; load() already answers null when there is no
    // file yet, which is the contract both interfaces expect.
    @Override
    public Account get(String accountId) {
        return load(accountId);
    }

    private Path fileFor(String accountId) {
        return directory.resolve(accountId + ".json");
    }

    private JSONObject toJson(Account account) {
        JSONObject json = new JSONObject();
        json.put("userName", account.getUserName());
        json.put("userBalance", account.getUserBalance());

        JSONArray holdings = new JSONArray();
        for (Position position : account.getHoldings()) {
            JSONObject positionJson = new JSONObject();
            positionJson.put("ticker", position.getTicker());
            positionJson.put("shares", position.getShares());
            positionJson.put("averagePrice", position.getAveragePrice());
            holdings.put(positionJson);
        }
        json.put("holdings", holdings);

        JSONArray orders = new JSONArray();
        for (Order order : account.getPlacedOrders()) {
            JSONObject orderJson = new JSONObject();
            orderJson.put("id", order.getId());
            orderJson.put("ticker", order.getTicker());
            orderJson.put("side", order.getSide().name());
            orderJson.put("type", order.getType().name());
            orderJson.put("quantity", order.getQuantity());
            orderJson.put("limitOrStopPrice", order.getLimitOrStopPrice());
            orderJson.put("status", order.getStatus().name());
            orderJson.put("createdAt", order.getCreatedAt().toString());
            orderJson.put("filledAt", order.getFilledAt() == null ? JSONObject.NULL : order.getFilledAt().toString());
            orderJson.put("fillPrice", order.getFillPrice());
            orders.put(orderJson);
        }
        json.put("placedOrders", orders);

        JSONArray trades = new JSONArray();
        for (Trade trade : account.getTradeLog()) {
            JSONObject tradeJson = new JSONObject();
            tradeJson.put("id", trade.getId());
            // Written only when known, so an older file without it stays readable.
            if (trade.getOrderId() != null) {
                tradeJson.put("orderId", trade.getOrderId());
            }
            tradeJson.put("ticker", trade.getTicker());
            tradeJson.put("side", trade.getSide().name());
            tradeJson.put("quantity", trade.getQuantity());
            tradeJson.put("price", trade.getPrice());
            tradeJson.put("timestamp", trade.getTimestamp().toString());
            tradeJson.put("realizedPnL", trade.getRealizedPnL());
            trades.put(tradeJson);
        }
        json.put("tradeLog", trades);

        json.put("watchlist", new JSONArray(account.getWatchlist().getTickers()));

        // Day P/L is measured against this mark, so it has to outlive the session.
        json.put("dayStartEquity", account.getDayStartEquity());
        if (account.getDayStartDate() != null) {
            json.put("dayStartDate", account.getDayStartDate().toString());
        }

        return json;
    }

    private Account fromJson(JSONObject json) {
        Account account = new Account(json.getString("userName"), json.getDouble("userBalance"));

        for (Object item : json.getJSONArray("holdings")) {
            JSONObject positionJson = (JSONObject) item;
            account.addPosition(new Position(positionJson.getString("ticker"), positionJson.getInt("shares"),
                    positionJson.getDouble("averagePrice")));
        }

        for (Object item : json.getJSONArray("placedOrders")) {
            JSONObject orderJson = (JSONObject) item;
            Double limitOrStopPrice = orderJson.isNull("limitOrStopPrice")
                    ? null : orderJson.getDouble("limitOrStopPrice");
            Order order = new Order(orderJson.getString("id"), orderJson.getString("ticker"),
                    Order.Side.valueOf(orderJson.getString("side")), Order.Type.valueOf(orderJson.getString("type")),
                    orderJson.getInt("quantity"), limitOrStopPrice, Instant.parse(orderJson.getString("createdAt")));
            if (orderJson.getString("status").equals(Order.Status.FILLED.name())) {
                order.fill(orderJson.getDouble("fillPrice"), Instant.parse(orderJson.getString("filledAt")));
            } else if (orderJson.getString("status").equals(Order.Status.CANCELED.name())) {
                order.cancel();
            }
            account.addOrder(order);
        }

        for (Object item : json.getJSONArray("tradeLog")) {
            JSONObject tradeJson = (JSONObject) item;
            Double realizedPnL = tradeJson.isNull("realizedPnL") ? null : tradeJson.getDouble("realizedPnL");
            account.addTrade(new Trade(tradeJson.getString("id"),
                    tradeJson.optString("orderId", null), tradeJson.getString("ticker"),
                    Order.Side.valueOf(tradeJson.getString("side")), tradeJson.getInt("quantity"),
                    tradeJson.getDouble("price"), Instant.parse(tradeJson.getString("timestamp")), realizedPnL));
        }

        for (Object ticker : json.getJSONArray("watchlist")) {
            account.getWatchlist().add((String) ticker);
        }

        if (json.has("dayStartDate")) {
            account.setDayStartDate(LocalDate.parse(json.getString("dayStartDate")));
            account.setDayStartEquity(json.optDouble("dayStartEquity", 0.0));
        }

        return account;
    }
}
