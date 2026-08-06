package com.marketmaker.data_access;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import com.marketmaker.entities.Account;
import com.marketmaker.entities.Order;
import com.marketmaker.entities.Position;
import com.marketmaker.entities.Trade;

/**
 * Accounts stored as JSON in a local file, so cash, holdings, orders and the trade log are
 * all still there the next time the application opens.
 *
 * <p>The whole file is read once at startup and rewritten on every save. That is fine for a
 * single-user simulator whose file holds one account and a few hundred orders.
 * ponytail: rewrite-whole-file, no journal — move to an append-only log or a real database if
 * the trade log ever grows past what is comfortable to serialise on each fill.
 */
public class FileAccountDataAccessObject implements AccountDAO {
    private static final Logger LOGGER = Logger.getLogger(FileAccountDataAccessObject.class.getName());

    private final Path file;
    private final Map<String, Account> accounts = new HashMap<>();

    public FileAccountDataAccessObject(Path file) {
        this.file = file;
        load();
    }

    @Override
    public Account get(String accountId) {
        return accounts.get(accountId);
    }

    @Override
    public void save(Account account) {
        accounts.put(account.getUserName(), account);
        write();
    }

    // Reading

    private void load() {
        if (!Files.exists(file)) {
            return;
        }
        try {
            String body = Files.readString(file, StandardCharsets.UTF_8);
            JSONArray saved = new JSONObject(body).getJSONArray("accounts");
            for (int index = 0; index < saved.length(); index++) {
                Account account = readAccount(saved.getJSONObject(index));
                accounts.put(account.getUserName(), account);
            }
        } catch (IOException | RuntimeException exception) {
            // A corrupt or half-written file shouldn't stop the app from opening. Say so and
            // start empty — the next save rewrites it.
            LOGGER.warning(() -> "Could not read " + file + ", starting with no saved accounts: "
                    + exception.getMessage());
            accounts.clear();
        }
    }

    private Account readAccount(JSONObject saved) {
        Account account = new Account(saved.getString("userName"), saved.getDouble("userBalance"));

        JSONArray holdings = saved.optJSONArray("holdings");
        if (holdings != null) {
            for (int index = 0; index < holdings.length(); index++) {
                JSONObject holding = holdings.getJSONObject(index);
                account.addPosition(new Position(holding.getString("ticker"),
                        holding.getInt("shares"), holding.getDouble("averagePrice")));
            }
        }

        JSONArray orders = saved.optJSONArray("placedOrders");
        if (orders != null) {
            for (int index = 0; index < orders.length(); index++) {
                account.addOrder(readOrder(orders.getJSONObject(index)));
            }
        }

        JSONArray trades = saved.optJSONArray("tradeLog");
        if (trades != null) {
            for (int index = 0; index < trades.length(); index++) {
                account.addTrade(readTrade(trades.getJSONObject(index)));
            }
        }

        if (saved.has("dayStartDate")) {
            account.setDayStartDate(LocalDate.parse(saved.getString("dayStartDate")));
            account.setDayStartEquity(saved.optDouble("dayStartEquity", 0.0));
        }
        return account;
    }

    private Order readOrder(JSONObject saved) {
        Order order = new Order(saved.getString("id"), saved.getString("ticker"),
                Order.Side.valueOf(saved.getString("side")), Order.Type.valueOf(saved.getString("type")),
                saved.getInt("quantity"), optDouble(saved, "limitOrStopPrice"),
                Instant.parse(saved.getString("createdAt")));

        // Status isn't settable directly: replay the transition that produced it, which is
        // also the only way to get the fill price and time back onto the order.
        Order.Status status = Order.Status.valueOf(saved.getString("status"));
        if (status == Order.Status.FILLED) {
            order.fill(saved.getDouble("fillPrice"), Instant.parse(saved.getString("filledAt")));
        } else if (status == Order.Status.CANCELED) {
            order.cancel();
        }
        return order;
    }

    private Trade readTrade(JSONObject saved) {
        return new Trade(saved.getString("id"), saved.optString("orderId", ""),
                saved.getString("ticker"), Order.Side.valueOf(saved.getString("side")),
                saved.getInt("quantity"), saved.getDouble("price"),
                Instant.parse(saved.getString("timestamp")), optDouble(saved, "realizedPnL"));
    }

    // Writing

    private void write() {
        JSONArray saved = new JSONArray();
        for (Account account : accounts.values()) {
            saved.put(writeAccount(account));
        }

        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            // Write beside the target and swap it in, so a crash mid-write can't leave a
            // half-serialised trade log where the real one used to be.
            Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
            Files.writeString(temporary, new JSONObject().put("accounts", saved).toString(2),
                    StandardCharsets.UTF_8);
            Files.move(temporary, file, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            // Losing the save is bad, but taking the window down mid-trade is worse: the
            // in-memory account is still correct and the next save will try again.
            LOGGER.warning(() -> "Could not save accounts to " + file + ": " + exception.getMessage());
        }
    }

    private JSONObject writeAccount(Account account) {
        JSONArray holdings = new JSONArray();
        for (Position position : account.getHoldings()) {
            holdings.put(new JSONObject()
                    .put("ticker", position.getTicker())
                    .put("shares", position.getShares())
                    .put("averagePrice", position.getAveragePrice()));
        }

        JSONArray orders = new JSONArray();
        for (Order order : account.getPlacedOrders()) {
            orders.put(new JSONObject()
                    .put("id", order.getId())
                    .put("ticker", order.getTicker())
                    .put("side", order.getSide().toString())
                    .put("type", order.getType().toString())
                    .put("quantity", order.getQuantity())
                    .put("limitOrStopPrice", order.getLimitOrStopPrice())
                    .put("status", order.getStatus().toString())
                    .put("createdAt", order.getCreatedAt().toString())
                    .put("filledAt", order.getFilledAt() == null ? null : order.getFilledAt().toString())
                    .put("fillPrice", order.getFillPrice()));
        }

        JSONArray trades = new JSONArray();
        for (Trade trade : account.getTradeLog()) {
            trades.put(new JSONObject()
                    .put("id", trade.getId())
                    .put("orderId", trade.getOrderId())
                    .put("ticker", trade.getTicker())
                    .put("side", trade.getSide().toString())
                    .put("quantity", trade.getQuantity())
                    .put("price", trade.getPrice())
                    .put("timestamp", trade.getTimestamp().toString())
                    .put("realizedPnL", trade.getRealizedPnL()));
        }

        JSONObject saved = new JSONObject()
                .put("userName", account.getUserName())
                .put("userBalance", account.getUserBalance())
                .put("holdings", holdings)
                .put("placedOrders", orders)
                .put("tradeLog", trades)
                .put("dayStartEquity", account.getDayStartEquity());
        if (account.getDayStartDate() != null) {
            saved.put("dayStartDate", account.getDayStartDate().toString());
        }
        return saved;
    }

    // JSONObject.put drops a null, so an absent key and a null value both mean "no value".
    private Double optDouble(JSONObject saved, String key) {
        return saved.has(key) && !saved.isNull(key) ? saved.getDouble(key) : null;
    }
}
