# MarketMaker — 5-Minute Demo Run Sheet

**Slot: Monday August 10, 1:00–1:30 PM.**

Written against `ericsson-branch-wire-panels`, the branch that ships. Every control named
below exists in the current code. Numbers are chosen so the outcome is deterministic on
the simulated feed.

> Verification note: the click path was derived from the source and confirmed by an
> end-to-end run through the real interactors (16 checks, all passing). The window itself
> was not driven interactively, so **rehearse this once with the app open** before Monday.

## Pre-flight

1. **Decide which feed you are on.** With no keys configured the app uses the simulated
   feed (AAPL 190, MSFT 410, NVDA 120) and the chart stays empty. With
   `ALPHAVANTAGE_API_KEY` set the chart draws real history; with `FINNHUB_API_KEY` set the
   quotes are real.
   **Recommended: keep `ALPHAVANTAGE_API_KEY` set so the chart works, and let the quote
   feed be whichever you have.** A visible empty chart for 20 minutes is the single worst
   thing on screen. Your slot is 1:00 PM, inside US market hours, so live quotes will move.
2. **Reset the account** for a clean start: `rm -rf data/` (keeps `data/candles` cache out
   of the way too, though re-downloading costs a moment). The app opens a fresh `demo`
   account with the default watchlist AAPL, MSFT, NVDA.
3. **Back up `data/` first if you want your rehearsal state.** The app writes on launch.
4. **Launch from the jar**, in front of the audience, from a terminal they can see:
   ```bash
   java -jar target/marketmaker.jar
   ```
   That is your Runnable Artifact evidence. Do not launch from IntelliJ.
5. Full-screen the window. It opens at 1440x940.

## The run sheet

### 0:00 – 0:30 · Opening (Team story)

**Driver:** whoever launches

- Run the jar from the terminal so the audience sees a build artifact starting.
- Point at the top bar: **Cash**, **Buying Power**, **Equity**, **Day P/L**.
- Point at the feed indicator and say which feed you are on. It reports the real one.
- Line: *"First launch opens a paper account with a starting balance, and it has already
  been written to a local JSON file."*

### 0:30 – 1:15 · Watchlist (Wayne, story 2)

**Panel:** Watchlist

- AAPL, MSFT and NVDA are already listed. Prices update on their own.
- Type a symbol into the watchlist field, click **Add** (or press **Alt+A**).
- Say the line about the typo guard: *"Adds are checked against the market first, so a
  symbol that does not exist is refused rather than sitting there with no price."*
- Select a row, click **Remove** (**Alt+M**).

### 1:15 – 2:00 · Market order and a rejection (Team story)

**Panel:** Order Ticket

- Symbol `AAPL`, Side **Buy**, Order Type **Market**, Quantity `10`. Leave Limit and Stop
  empty.
- Click **Place Buy Order** (**Alt+P**). Cash drops, the position appears, the top bar
  moves, and the order lands in the history panel.
- Now the validation beat: same ticket, Quantity `100000`, place it. It is refused with
  *"Insufficient buying power."* and nothing changes.
- Line: *"Every order is validated before anything is debited. A rejected order leaves the
  account exactly as it was."*

### 2:00 – 3:00 · Limit, stop and cancel (Ryan, story 3)

Three beats, in this order:

1. **An order that rests.** Symbol `MSFT`, Buy, **Limit**, Quantity `1`, Limit Price a
   long way *below* the current price. It stays pending.
2. **An order that fills itself.** Symbol `AAPL`, Buy, **Limit**, Quantity `2`, Limit Price
   comfortably *above* the current price. A buy limit triggers when the price is at or
   below the limit, so it fills on the next price tick, live on screen.
3. **Cancel.** Select the still-resting MSFT order and cancel it.

> Why those numbers: a buy limit above the market fills immediately; one far below rests.
> Do not pick a trigger near the current price, or it will fire mid-sentence, or not at all.

Optional if time allows: a **Stop** sell above the current price fires on the next tick too.

### 3:00 – 3:45 · Positions and P/L (Ericsson, story 4)

**Panels:** Positions, and the summary bar

- The positions table shows each holding with its share count, average cost and current
  value, recalculating as prices move.
- Sell part of a position with a market order. **Day P/L** in the top bar moves off zero
  and is coloured.
- Line: *"Unrealized P/L is a live valuation, realized P/L is booked at the fill. Both the
  bar and the table read one view model, so they cannot disagree."*

### 3:45 – 4:20 · History and persistence (Alex, story 5)

**Panel:** Order & Trade History, with its **Orders** and **Trades** tabs

- Every order from this demo is there: filled, cancelled, and the limit that filled itself.
- Switch to the **Trades** tab to show completed transactions separately from orders.
- **Close the app and relaunch it.** Everything returns: cash, positions, the full log.
- Line: *"Nothing is lost between sessions. The account, its holdings and the whole trade
  log are one local JSON file."*

### 4:20 – 5:00 · Chart (Ben, story 6)

**Panel:** Chart

- Type a symbol into the chart's symbol field and press **Enter**.
- Switch span with the **1W** and **1M** buttons.
- Line: *"Price history comes from a second API, Alpha Vantage, and is cached locally so
  reopening a chart does not re-fetch."*

### Close

- Hand to whoever starts the Clean Architecture section.

## If something goes wrong

- **Chart empty:** the Alpha Vantage key is missing or rate-limited. Say so plainly and
  move on. The free tier is limited per day, so do not burn requests rehearsing.
- **A price does not move:** the simulated feed only ticks tickers it knows (AAPL, MSFT,
  NVDA). Stick to those if you are offline.
- **An order does not fill:** check the side. A buy limit fills at or below the limit; a
  sell limit fills at or above it.
- **Keep the app running for Q&A.** The graders may ask you to demonstrate something
  specific after the 20 minutes.

## Worth saying out loud during the demo

- The feed indicator names the real source, so the numbers are never overstated.
- The app runs with no API keys at all, on a simulated feed. That was a deliberate design
  decision, not a fallback, and it is why this demo works whatever the market is doing.
