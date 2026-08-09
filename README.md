# MarketMaker

MarketMaker is a desktop paper-trading app for practising short-term stock trading on US
markets. You get a fake account with real starting cash, real market prices, and no way to
lose actual money. It is built in Java with a Swing interface.

The idea is simple. Trading with real money is an expensive way to learn what a stop-loss
does. MarketMaker gives you the same decisions without the consequences: search for a
stock, watch its price move, place an order, and see what it did to your balance.

## What you can do with it

**Watch a list of stocks.** Add tickers to a watchlist and their prices keep updating on
their own while the app is open. Remove the ones you stop caring about.

**Place three kinds of order.**

| Order type | What it does |
|---|---|
| Market | Fills straight away at whatever the price is right now |
| Limit | Waits until the price reaches the number you set, then fills |
| Stop | Waits until the price falls to your trigger, then sells |

Limit and stop orders sit in a pending list and are checked against the price as it moves.
When one of them is triggered, the app fills it, updates your cash and holdings, and
records the trade. You can cancel any order that has not filled yet.

**Watch your position.** The positions panel shows every stock you hold, how many shares,
what you paid on average, and what the position is worth right now. The bar across the top
keeps a running total of your cash, your buying power, your total equity, and your profit
or loss for the day.

**Look back at what you did.** Every order is written to a timestamped log, whether it
filled, is still waiting, or was cancelled. The log survives closing the app.

**See the price, not just a number.** Select a stock and the chart draws its recent price
history, over the last week or the last month.

Nothing here touches real money or places real trades. It is a simulator.

## Running it

You need Java 17 or newer. Check with `java -version`.

Build the app:

```bash
mvn clean package
```

Run it:

```bash
java -jar target/marketmaker.jar
```

That jar carries its own dependencies, so it runs anywhere Java 17 is installed. You do
not need Maven to run it, only to build it.

### About API keys

**The app runs without any keys.** If you have not set any up, it starts on a built-in
simulated feed that walks prices around realistically, and the chart stays empty and says
so. That is deliberate: US markets are closed most of the time we work on this, and an app
that only runs during market hours is not much use.

For real prices, copy the example config and fill in your own keys:

```bash
cp src/.env.example .env
```

| Key | What it powers | Where to get one |
|---|---|---|
| `FINNHUB_API_KEY` | Live quotes for the watchlist and market orders | [finnhub.io](https://finnhub.io) |
| `ALPHAVANTAGE_API_KEY` | Price history for the chart | [alphavantage.co](https://www.alphavantage.co) |

Both have free tiers. The app reads real environment variables first and falls back to the
`.env` file, and `.env` is gitignored so keys never end up in the repository.

The indicator in the top bar tells you which feed you are actually on, so there is never
any doubt about whether the numbers are real.

### Where your data goes

Everything is stored locally in a `data/` folder next to wherever you run the app:

- `data/demo.json` holds your account: cash, holdings, orders, and trades
- `data/candles/` caches downloaded price history so re-opening a chart is instant

Delete `data/` to start over with a fresh account.

## How the code is organised

The project follows Clean Architecture. Each layer only knows about the layer inside it,
which is why the UI can change without the trading logic noticing.

```
com.marketmaker
├── entities        Account, Order, Position, Trade, Quote, Candle
├── use_case        One package per use case: interactor, boundaries, request/response models
├── interface_adapter   Controllers, presenters, and view models
├── data_access     JSON storage, the Finnhub and Alpha Vantage clients, caching
├── price_feed      The quote source behind an interface, live or simulated
├── view            Swing panels and the dashboard frame
└── Main            Wires everything together at startup
```

Two things are worth pointing at:

**The price feed is an interface with two implementations.** One talks to Finnhub, the
other walks prices around locally. `Main` picks whichever one it can build. Nothing above
that layer knows or cares which is running, which is how the app keeps working when the
market is closed.

**Views never talk to data.** A button calls a controller, which calls an interactor,
which does the work and hands a result to a presenter, which updates a view model, which
the view is listening to. It is more indirection than a small app strictly needs, but it
is what makes the interactors testable without a screen.

## Development

Run the tests:

```bash
mvn test
```

That also produces a coverage report at `target/site/jacoco/index.html`.

Style is checked automatically on every build. The rules live in `config/checkstyle.xml`
and follow the conventions written down in `docs/CODE_STYLE.md`. A violation fails the
build, so it gets caught before review rather than after:

```bash
mvn checkstyle:check
```

`main` is protected. Changes go through a pull request and need approval from another team
member before merging.

## Accessibility

`accessibility-report.md` covers this properly, including the parts we did not get right.
The short version: the interface can be driven from the keyboard, form fields are bound to
their labels for screen readers, tables have spoken names, and gains and losses are marked
with a sign and not only with colour. What is still missing is inheriting your system font
size and high-contrast theme, since the app forces its own look, and a non-colour marker
on the buy and sell columns.

## The team

Ericsson, Ryan, Wayne, Alex, and Ben. Built for CSC207 at the University of Toronto,
Summer 2026.
