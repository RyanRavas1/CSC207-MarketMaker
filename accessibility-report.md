# MarketMaker - Accessibility Report

This report answers the three questions set for the project accessibility report, using
the concepts from the two embedded ethics (E3I) modules: User Diversity, and Disability
and Accessible Software.

We checked every claim below against the source in this repository before writing it, and
each one names the class or method it comes from. Where we found a gap we say so plainly
instead of describing something we only intend to build.

## Concepts we are using

Following Wasserman et al., a disability is a physical or mental impairment associated
with a personal or social limitation on the activities a person can perform. The two
models differ over which of these *causes* the limitation and which is only a *background
condition*:

- **Medical model.** The impairment causes the limitation; the human world is the
  background condition.
- **Social model.** The human world causes the limitation; the impairment is the
  background condition.

Neither model fits every case. The distinction also applies to interventions. A
medical-model intervention sits closer to the impairment, is designed with a particular
impairment in mind, can work for one person without working for everyone, and must be
switched on (the prosthetic). A social-model intervention sits further from the
impairment, applies to everyone at once, and needs little or nothing to trigger it (the
elevator). The WHO estimates roughly 15% of users have disabilities, so this is not a
marginal audience.

## 1. Principles of Universal Design

### Principle 1, Equitable Use

MarketMaker ships one interface instead of a standard version and a reduced accessible
one. Every user gets the same watchlist, the same order types, the same positions view,
the same order history and the same price trend chart. That is closer to guideline 1a's
"identical whenever possible" than to a segregated fallback.

Where the program does well on 1b is in how it writes money. `Format.signedMoney` renders
a gain as `+342.18` and a loss as `-78.50`, and `Format.signedDollars` does the same with
the currency symbol. A character carries the sign, not just a colour, so the direction of
a number survives being read by someone who cannot distinguish our green from our red.

The program can also be driven without a mouse. `Place Buy Order` is Alt+P, the toolbar's
Buy, Sell, Refresh and Profile are Alt+B, Alt+S, Alt+R and Alt+F, and the watchlist's Add
and Remove are Alt+A and Alt+M. Standard Tab traversal reaches the rest, and the radio
groups respond to the arrow keys. Keyboard and pointer users take equally direct routes to
the same functions, so neither route is a fallback for the other.

That was not true until we audited the program for this report. `ViewComponents.button`
called `setFocusable(false)` on every button it produced, which removed all of them from
the Tab order, and the dashboard could not be operated from the keyboard at all. One line
caused it and one line fixed it. We would not have found it without going looking.

**Gap:** the mnemonics cover the primary actions, not everything. Switching panels and
sorting a table column still need the mouse.

### Principle 2, Flexibility in Use

Limit and stop orders let a user set a target and step away instead of deciding at the
moment the market moves. `MatchPendingOrdersInteractor` fills them when the price
condition is met. That is guideline 2d, adaptability to the user's pace. We designed it as
a trading feature, but it works for pace of use either way.

The chart offers a choice of span through `Resolution` (`ONE_WEEK`, `ONE_MONTH`), so a
user can pick the level of detail they can actually read instead of being given one fixed
view. `DashboardFrame` opens at 1440x940 and is freely resizable above its minimum, so the
window can be enlarged for a magnifier.

**Gap:** the panel arrangement is fixed, with no mirrored or single-column layout and no
way to hide panels that are not in use.

### Principle 3, Simple and Intuitive Use

Failed actions return plain language, not error codes. `PlaceOrderInteractor` produces
"Quantity must be positive.", "Account not found.", "Insufficient buying power." and "Not
enough shares to sell."; `CancelOrderInteractor` adds "Order not found." and "Only pending
orders can be cancelled." Each says what went wrong in words a non-specialist can act on
(guideline 3e).

Order side and order type are presented as labelled radio buttons in `OrderTicketPanel`
(Buy/Sell, Market/Limit/Stop) instead of a dropdown the user must open to discover their
options, so the full set of choices is visible without interaction (3c). The dashboard is
divided into titled, bordered regions arranged the way commercial trading platforms
arrange them, so prior expectations transfer (3b).

**Gap:** there is no glossary or first-run explanation. "Buying power", "unrealized P/L"
and "stop" are assumed vocabulary. The tooltips added during this audit explain what a
control does, not what the terminology means.

### Principle 4, Perceptible Information

Information is partly redundantly coded, which is the core of this principle. Order sides,
types and statuses are rendered as text instead of colour swatches, and the signed-number
formatting described under Principle 1 means gains and losses carry a `+` or `-` alongside
their colour.

The interface is built from standard Swing widgets (`JTable`, `JButton`, `JTextField`,
`JRadioButton`) instead of custom-painted controls, so it inherits Java's
`AccessibleContext` support automatically. That is a better starting point than a
hand-drawn UI, and it is why the fixes described below were cheap.

Controls are now named programmatically instead of by visual proximity. A `JLabel` sitting
beside a text field is only visually associated with it, so a screen reader announces the
field as an unnamed text box. `OrderTicketPanel.labelledRow` now calls `setLabelFor`,
which binds all six captions on the ticket (Symbol, Side, Order Type, Quantity, Limit
Price and Stop Price) in one place. `Tables.create` takes a name and applies it to the
`AccessibleContext`, so the five tables announce themselves as "Watchlist quotes", "Open
positions", "Order history", "Trade history" and "Account holdings" instead of as five
identical unnamed grids. The chart's span buttons carry accessible names and tooltips.

Before this pass the entire program contained two accessibility API calls, both tooltips.
That it was so cheap to fix is a point in the architecture's favour, not ours. See
section 4.

**Gap:** the summary bar's figures and the status dot are still unnamed, and the tab
control in the history panel does not describe what each tab holds.

**Gap, system settings.** `Main` calls `UIManager.setLookAndFeel(new MetalLookAndFeel())`
so the dashboard renders identically on every machine. That consistency comes at a direct
cost: the program does not inherit a user's system high-contrast theme or enlarged system
font. `UiTheme` then fixes body text at 11pt (`Font.SANS_SERIF, Font.PLAIN, 11`) with no
in-app zoom, so a user who has enlarged text system-wide sees no change here.

**Gap, colour as the only cue, which is also a legal one.** `StyledCellRenderer` colours
BUY green and SELL red, and tints rows with `GREEN_TINT` and `RED_TINT`. For the signed
numbers the sign reinforces the colour; for the side column and the row tints nothing
does. WCAG 2.0 success criterion 1.4.1, "Use of Color" (Level A), requires that colour
never be the sole visual means of conveying information, and under the AODA most Ontario
organizations are expected to meet WCAG 2.0 to the AA level. We have also not checked
`UiTheme.GREEN` against `UiTheme.RED` for contrast ratio.

### Principle 5, Tolerance for Error

Orders are validated before anything is written. In `PlaceOrderInteractor` a non-positive
quantity, an unknown account, a price-feed failure, insufficient cash and insufficient
shares are each rejected before any balance or position changes, so a rejected order
leaves the account exactly as it was. That is guideline 5a: the design isolates the hazard
instead of warning about it.

A pending limit or stop order can be cancelled before it executes, which is the nearest
thing the program has to an undo (5b), and `CancelOrderInteractor` refuses anything else
with "Only pending orders can be cancelled.", so cancelling can never disturb a filled
position. Most fundamentally, the balances are fictional. The worst error a real brokerage
must defend against, losing real money by mistake, cannot happen here.

**Gap:** a market order executes immediately with no confirmation step, so a mistyped
quantity is recoverable only by placing an opposing trade.

### Principle 6, Low Physical Effort

Quotes refresh on their own. `PolledQuoteSubscription` re-quotes subscribed tickers on an
interval, so watching the market requires no repeated clicking. The repetitive action is
removed, not merely accelerated (6d). Candle data is cached by `CandleFileCache`, so
re-opening a chart does not mean re-fetching and re-waiting.

The seven mnemonics described under Principle 1 mean the frequent actions no longer
require accurate pointer travel across a 1440x940 dashboard. Placing an order, adding a
symbol and refreshing are each one key combination.

**Gap:** quantities and prices are still typed into plain text fields with no stepper
buttons or scroll-to-adjust, so entering a value requires accurate typing.

### Principle 7, Size and Space for Approach and Use

This principle is written around physical reach: approach space, line of sight, room for
an assistive device. It does not transfer cleanly to a desktop application that occupies
no physical space of its own. In the literal sense the guideline describes, it does not
apply to MarketMaker.

Read as on-screen target size, we have not addressed it deliberately. Buttons use Swing's
default sizing and table rows a fixed height. More importantly, `DashboardFrame` sets
`setMinimumSize(new Dimension(1100, 700))`, a hard floor that cannot be resized away. A
user on a small display, or one running a screen magnifier that effectively shrinks the
usable area, cannot make the window fit. The concrete features here would be a
large-target display mode raising button padding and row height together, and lowering or
removing that minimum with a layout that reflows.

## 2. Target Market

We would market MarketMaker to people who want to practise active, short-term stock
trading without risking real money. Most directly that means university students in
finance, economics or computer science building intuition for order types, risk and
portfolio management before, or instead of, opening a real brokerage account. It also
suits retail investors curious about day trading but unwilling to start with real capital,
and self-directed learners preparing for interviews in trading-adjacent roles. Orders are
priced against live market data when an API key is configured and against a simulated feed
when one is not, so it works as a low-stakes sandbox either way. We would position it away
from complete beginners: the interface assumes the user already knows roughly what a limit
order is and wants somewhere safe to practise placing one.

## 3. Demographics and Likelihood of Use

Using the two models, the social model describes nearly all the friction we found. For a
colourblind user the impairment is a background condition; what causes the limitation, not
being able to tell a buy row from a sell row at a glance, is our decision in
`StyledCellRenderer` to encode that in green against red. For a low-vision user the
limitation comes from our choice to force the Metal look-and-feel and fix type at 11pt,
not from the impairment. For a keyboard-only user it comes from our never having added a
mnemonic. In each case a differently built version of MarketMaker would remove the
limitation while changing nothing about the person, which is the social model's argument.

Concretely, the groups least likely to use MarketMaker comfortably today are:

- **Blind and low-vision users.** Before this audit the entire program held two tooltips
  and no accessible names. The tables and the order ticket are named now, but there is
  still no inheritance of system fonts or high-contrast themes, and no zoom.
- **Colourblind users.** Red-green colour blindness affects roughly one in twelve men, and
  our tables lean on exactly that pair for the side column and row tints.
- **Users who cannot use a mouse**, until this audit. Every button sat outside the Tab
  order, which was disqualifying rather than merely inconvenient. That one is now largely
  addressed, and it is the clearest example in the project of a limitation that lived
  entirely in our code.

Both kinds of harm from the modules are in play. The tangible harm is direct: someone who
cannot operate the order ticket cannot practise trading, and so loses the opportunity the
program exists to provide. The relational harm is subtler and, we think, the more
important one. Module 1 draws the line well. Building software that works for Apple but
not Android users usually does not communicate that Android users are less than equal,
whereas building software that works for young but not elderly users usually does.
Shipping a trading simulator that could not be driven without a mouse, and whose buy and
sell rows are separated by colour, falls on the second side of that line. It communicates
whose participation was assumed. That is a failure of relational equality in Anderson's
sense, a matter of standing rather than of goods.

That the exclusion was unintended does not undo it. It is exactly the "designers assume
the user is like themselves" failure the module describes, and it is worth saying that
none of us needed a screen reader, a colourblind-safe palette or a keyboard-only path to
build this. That is why `setFocusable(false)` sat in a shared helper for weeks without
anyone noticing.

Beyond disability, the program assumes financial literacy. "Stop", "buying power" and
"unrealized P/L" appear with no explanation, so people without prior exposure to trading
are less likely to use it comfortably regardless of ability. That is a
background-knowledge barrier rather than a disability-related one, but it compounds the
others for anyone facing both.

## 4. Accessibility and our architecture

Module 2 notes that Clean Architecture makes accessibility features easier to change,
because they belong in the outer layer. That holds here and is checkable. Every gap
identified above lives in `com.marketmaker.view`: `StyledCellRenderer`'s colours,
`UiTheme`'s fonts, `DashboardFrame`'s minimum size and missing labels, `OrderTicketPanel`'s
missing mnemonics. The one exception is the `setLookAndFeel` call in `Main`, our
composition root.

We can put a number on that claim. Every accessibility change described in this report,
`setLabelFor` on the ticket, accessible names on five tables, seven mnemonics and the
`setFocusable` fix, touched nine files, all of them in `com.marketmaker.view`. We
modified no entity, interactor, presenter or data access class, and the whole test suite
passed unchanged afterwards. Naming the positions table never went near the code that
computes profit and loss, because the Dependency Rule already keeps that logic ignorant of
Swing.
The architecture we chose for other reasons is what made this cheap.

It is also worth separating the two kinds of intervention in our own feature list. The
signed-number formatting and the plain-language error messages are social-model
interventions: they apply to every user, need nothing switched on, and were not built for
a named impairment. A future high-contrast theme or large-target mode would sit nearer the
medical-model end, aimed at a particular impairment and requiring the user to enable it,
much like Night Mode, the module's own case study.

Our remaining steps, in the order we would take them: request the system look-and-feel
instead of forcing Metal, so the program inherits high-contrast themes and enlarged fonts;
add a non-colour marker to the side column; check `UiTheme.GREEN` against `UiTheme.RED` for
contrast ratio; lower the 1100x700 minimum with a layout that reflows; and, as the module
recommends, ask someone who actually uses a screen reader to try it, since none of us has
that experience.

---

### References

- Anderson, Elizabeth. 1999. "What is the Point of Equality?" *Ethics* 109(2): 287-337.
- Wasserman, David, Adrienne Asch, Jeffrey Blustein, and Daniel Putnam. "Disability:
  Definitions, Models, Experience." *The Stanford Encyclopedia of Philosophy.*
- Web Content Accessibility Guidelines (WCAG) 2.0, success criterion 1.4.1 "Use of Color"
  (Level A); Accessibility for Ontarians with Disabilities Act (AODA).
- CSC207 E3I modules 1 (User Diversity) and 2 (Disability and Accessible Software).

*The accessibility work described under Principles 1, 4 and 6 can be confirmed with:*

```bash
grep -rn "setLabelFor\|setAccessibleName\|setToolTipText\|setMnemonic" src/main/java
```
