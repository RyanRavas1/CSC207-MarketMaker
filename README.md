# MarketMaker

MarketMaker is a desktop paper-trading app for practising short-term stock trading on US
markets. You get a fake account with real starting cash, real market prices, and no way to
lose actual money. It is built in Java with a Swing interface.

The idea is simple. Trading with real money is an expensive way to learn what a stop-loss
does. MarketMaker gives you the same decisions without the consequences: search for a
stock, watch its price move, place an order, and see what it did to your balance.

## Contents

- [What you can do with it](#what-you-can-do-with-it)
- [Running it](#running-it)
  - [Requirements](#requirements)
  - [About API keys](#about-api-keys)
  - [Where your data goes](#where-your-data-goes)
  - [If something goes wrong](#if-something-goes-wrong)
- [Using it](#using-it)
- [How the code is organised](#how-the-code-is-organised)
- [Development](#development)
- [Contributing](#contributing)
- [Feedback and bug reports](#feedback-and-bug-reports)
- [Accessibility](#accessibility)
- [License](#license)
- [The team](#the-team)

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

### Requirements

| You need | Version | Where to get it | What for |
|---|---|---|---|
| Java (JDK) | 17 or newer | [adoptium.net](https://adoptium.net) | Running and building |
| Maven | 3.8 or newer | [maven.apache.org](https://maven.apache.org/download.cgi) | Building only |

Check both before you start:

```bash
java -version && mvn -version
```

MarketMaker runs on Windows, macOS and Linux. There is no platform-specific code, and
nothing needs to be installed beyond a JDK.

Everything else the app depends on is fetched by Maven and bundled into the jar, so there
is nothing else to install by hand:

| Library | Version | What for |
|---|---|---|
| [org.json](https://github.com/stleary/JSON-java) | 20240303 | Parsing API responses and the saved account file |
| [JUnit Jupiter](https://junit.org/junit5/) | 5.10.2 | Tests only |

Build the app:

```bash
mvn clean package
```

Run it:

```bash
java -jar target/marketmaker.jar
```

That jar carries its own dependencies, so once it is built you only need a JDK to run it.
Maven is a build-time requirement, not a runtime one.

On Windows, run the same two commands from PowerShell or Command Prompt.

### About API keys

**The app runs without any keys.** If you have not set any up, it starts on a built-in
simulated feed that walks prices around realistically, and the chart stays empty and says
so. We did that on purpose. US markets are closed most of the time we work on this, and an
app that only runs during market hours is not much use.

For real prices, copy the example config and fill in your own keys:

```bash
cp src/.env.example .env
```

| Key | What it powers | Where to get one |
|---|---|---|
| `FINNHUB_API_KEY` | Live quotes for the watchlist and market orders | [finnhub.io](https://finnhub.io) |
| `ALPHAVANTAGE_API_KEY` | Price history for the chart | [alphavantage.co](https://www.alphavantage.co) |

Both have free tiers. The app reads real environment variables first and falls back to the
`.env` file. `.env` is gitignored, so keys never end up in the repository.

The indicator in the top bar tells you which feed you are actually on, so you always know
whether the numbers are real.

### Where your data goes

Everything is stored locally in a `data/` folder next to wherever you run the app:

- `data/demo.json` holds your account: cash, holdings, orders, and trades
- `data/candles/` caches downloaded price history so re-opening a chart is instant

Delete `data/` to start over with a fresh account.

### If something goes wrong

| What you see | What it means | What to do |
|---|---|---|
| `UnsupportedClassVersionError` on startup | You built with Java 17 but are running an older JRE | Check `java -version`; install a JDK 17+ from [adoptium.net](https://adoptium.net) |
| `mvn: command not found` | Maven is not installed or not on your PATH | Install it from [maven.apache.org](https://maven.apache.org/download.cgi), then reopen your terminal |
| The top bar says the feed is simulated | No `FINNHUB_API_KEY` was found | Expected without a key. See [About API keys](#about-api-keys) |
| The chart is empty and says so | No `ALPHAVANTAGE_API_KEY` was found | Expected without a key. Everything else still works |
| The chart fails after working earlier | Alpha Vantage's free tier caps daily requests | Wait for the cap to reset. Cached charts in `data/candles/` still open |
| Prices stop updating | Finnhub returned a rate-limit response | Wait a minute. The app retries on the next poll |
| The account looks wrong or will not load | `data/demo.json` is corrupt | Delete `data/` and restart for a fresh account |

## Using it

Once the app is running:

1. **Add a stock.** Type a ticker (`AAPL`, `MSFT`) into the watchlist box and press Add,
   or Alt+A. Its price starts updating on its own.
2. **Select it.** Click the row. The chart draws its recent history, and the order ticket
   fills in the symbol.
3. **Place an order.** Choose Buy or Sell, choose Market, Limit or Stop, enter a quantity,
   and press Place Order, or Alt+P. A limit or stop order also needs its trigger price.
4. **Watch what happened.** A market order fills immediately and shows up in positions and
   in the order history. A limit or stop order sits in the pending list until the price
   reaches your number.
5. **Cancel if you change your mind.** Select a pending order and press Cancel. Filled
   orders cannot be cancelled.

The whole interface can be driven from the keyboard. The mnemonics are Alt+B (Buy), Alt+S
(Sell), Alt+R (Refresh), Alt+F (Profile), Alt+P (Place Order), Alt+A (Add to watchlist)
and Alt+M (Remove from watchlist).

## How the code is organised

The project follows Clean Architecture. Each layer only knows about the layer inside it,
so the UI can change without the trading logic noticing.

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

Two things are worth pointing out.

**The price feed is an interface with two implementations.** One talks to Finnhub, the
other walks prices around locally. `Main` picks whichever one it can build. Nothing above
that layer knows or cares which is running, and that is how the app keeps working when the
market is closed.

**Views never talk to data.** A button calls a controller, which calls an interactor,
which does the work and hands a result to a presenter, which updates a view model, which
the view is listening to. It is more indirection than a small app strictly needs, but it
lets us test the interactors without a screen.

## Development

Run the tests:

```bash
mvn test
```

That also produces a coverage report at `target/site/jacoco/index.html`. Interactors sit
at 97.6% line coverage and the codebase at 76.4%. `docs/testing.md` breaks that down by
package and says what is left untested and why.

Style is checked automatically on every build. The rules live in `config/checkstyle.xml`
and follow the conventions written down in `docs/CODE_STYLE.md`. A violation fails the
build, so it gets caught before review instead of after:

```bash
mvn checkstyle:check
```

The architecture diagrams in `docs/diagrams/` show the layer structure and the package
dependency graph. `docs/CODE_STYLE.md` has the conventions, and `ai.txt` records where we
used AI tools on this project.

## Contributing

Contributions are welcome.

1. Fork the repository on GitHub, or if you are on the team, branch directly from `main`.
2. Create a branch named for what it does, such as `fix-order-validation`.
3. Make your change. Add or update tests for anything in `entities` or `use_case`.
4. Run `mvn test` and `mvn checkstyle:check`. Both must pass. Checkstyle failures fail the
   build, so this is the same gate CI applies.
5. Open a pull request against `main` describing what changed and why.

What makes a pull request easy to accept:

- One concern per request. A formatting sweep mixed into a bug fix is hard to review.
- Commit messages in the form `type(scope): what changed`, matching the existing history.
- New behaviour in `use_case` comes with a test. That layer is at 97.6% coverage and we
  would like to keep it there.
- No new dependencies without saying in the pull request why the standard library or an
  existing dependency will not do.

**Review protocol.** `main` is protected. Every change goes through a pull request and
needs an approving review from another team member before it can be merged. The reviewer
checks that the build passes, that the change respects the Dependency Rule (no inner layer
importing an outer one), and that anything user-facing is reflected in the docs. We merge
with a merge commit so the branch history stays readable.

## Feedback and bug reports

Feedback goes through [GitHub Issues on this
repository](https://github.com/RyanRavas1/CSC207-MarketMaker/issues). It is the only
channel we monitor, so please do not send it by email.

A useful report tells us:

- What you did, step by step, so we can reproduce it
- What you expected, and what happened instead
- Your OS and the output of `java -version`
- Whether the top bar said the feed was live or simulated
- The full error text if the app printed one

Feature requests are welcome under the same issue tracker. Say what you are trying to do,
not only what control you want added.

What to expect after you file: this is a course project built by five students, so we
cannot promise a response time. We read every issue. Reproducible bug reports get looked
at first, and we will say plainly if something is out of scope rather than leaving it
open indefinitely.

## Accessibility

`accessibility-report.md` covers this properly, including the parts we did not get right.
The short version: the interface can be driven from the keyboard, form fields are bound to
their labels for screen readers, tables have spoken names, and gains and losses are marked
with a sign as well as a colour. What is still missing is inheriting your system font size
and high-contrast theme, since the app forces its own look, and a non-colour marker on the
buy and sell columns.

## License

MIT. The full text is in [LICENSE](LICENSE).

You can use, copy, modify and distribute this code, including commercially, as long as the
copyright notice and the licence text travel with it. It comes with no warranty. The
bundled dependencies keep their own licences: org.json is under its own permissive licence
and JUnit is under the Eclipse Public License 2.0, and neither is redistributed in source
form here.

## The team

Ericsson, Ryan, Wayne, Alex, and Ben.
