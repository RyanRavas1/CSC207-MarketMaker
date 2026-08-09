# MarketMaker — Final Checklist

**Presentation: Monday August 10, 1:00–1:30 PM.** Re-baselined Aug 9 against the branch
that ships.

### Project files

| File | What it is |
|---|---|
| [accessibility-report.md](../../accessibility-report.md) | The graded accessibility deliverable |
| [README.md](../../README.md) | Project README, rewritten to the checklist |
| [ai.txt](../../ai.txt) | AI use declaration, required by MarkUs |
| [demo-script.md](demo-script.md) | The 5-minute demo run sheet |
| [config/checkstyle.xml](../../config/checkstyle.xml) | Style rules, enforced on every build |
| [pom.xml](../../pom.xml) | Build, with JaCoCo and Checkstyle wired in |
| [docs/CODE_STYLE.md](../CODE_STYLE.md) | The conventions Checkstyle encodes |

**Shipping branch: `ericsson-branch-wire-panels`.** It is **12 commits ahead of
`origin/main` and 0 behind**, so merging it is a fast-forward — no surgery, nothing
discarded, and it fixes main's compile errors on the way in. PR **#15** is already
open for exactly this merge.

`ericsson-branch-interface-adapters` is dead (PR #12 closed, changes discarded).
Ignore any earlier notes referring to it.

Owners: **P1** and **P2**, the two people working.

---

## Where you actually stand

| Rubric item | State |
|---|---|
| Functionality (15) | Good — app runs from the JAR, all six stories wired, **73 tests pass** |
| Runnable artifact (5) | Good — shade plugin builds `target/marketmaker.jar` |
| Chart / story 6 (scope cap) | **Done** — `ChartPanel` + `PriceLineChart` exist and are wired |
| Clean Architecture (15), SOLID (15), Spec (10), API (5), Code org (5) | Presentation work only |
| Code quality (5) | **Done** — Checkstyle + JaCoCo in pom, **0 violations**, gate enforcing |
| Testing (10) | **34.8% overall** (needs >50%), **55.4% interactors** (needs >70%) |
| Accessibility (5) | **Done** — report rewritten against this branch, and the gaps it named are fixed |
| Overall Presentation (10) | **Capped at 6/10 unless every member speaks** — not fixable by code |

**On 90+:** the group rubric is within reach if Tier 0–2 land *and all five members
speak tomorrow*. Weekly Progress and your contribution rate are partly outside your
control (marking FAQ #5 and #6). Ask the other three for one thing: present their own
slide. It is worth about four points and costs you a message.

---

## TIER 0 — Blocking

- [x] **Ryan's review: code fixed.** `OrderTicketController.place()` restructured so
      every field the chosen order type needs is validated before anything dispatches,
      with exactly one dispatch point at the end. Behaviour is unchanged: the trigger is
      still only read for limit and stop orders, because validating it for market orders
      would reject a valid ticket over stale text in a box that does not apply.
      Seven new tests in `OrderTicketControllerTest` pin it, including the review's case.
- [ ] **P1 — Push the fix and reply to Ryan, then ask for re-review.** Not done for you:
      pushing and commenting are yours to send. Suggested reply is in the session notes.
- [ ] **P1 — Merge #15 to main** once approved. Fast-forward, `MERGEABLE`.
- [ ] **P1 — Verify a fresh clone runs.** Clone to a new directory, `mvn package`,
      `java -jar target/marketmaker.jar`. **Always `mvn clean package` after switching
      branches** — stale classes in `target/` produce fake `NoSuchMethodError`s.
- [ ] **P2 — Ask the other three today** to (a) speak tomorrow and (b) make a commit.

## TIER 1 — Cheap points, under two hours

- [x] **`ai.txt` written** ([ai.txt](../../ai.txt)). Declares tools used, where AI helped,
      what was ours, and how output was verified.
- [x] **`README.md` rewritten** ([README.md](../../README.md)). What it does, how to run
      it from the jar, both API keys and the no-key fallback, where data is stored, the
      package layout, how to run tests and Checkstyle, accessibility, and the team.
- [ ] **P2 — `.mailmap`.** `Alex Guu`/`Alexander Gu` and `ryanravas`/`RyanRavas1`
      appear as four contributors. The TA reads that graph.
- [x] **P1 — JaCoCo + Checkstyle added to `pom.xml`.** Checkstyle runs at `validate`
      with `failOnViolation=true`; JaCoCo reports at `target/site/jacoco/index.html`.
- [x] **P1 — Checkstyle violations cleared: 19 found, 0 remaining.** Worth saying on the
      Code Quality slide: **every violation was in test code — main sources were already
      clean.** Fixed: 3 utility classes made final with private constructors, 2 unused
      imports, 4 `@param` tags on the `Column` record, 3 long lines, 2 missing trailing
      newlines, and 18 test doubles made final.

## TIER 2 — Testing. The one number that still needs real work.

Twelve of sixteen interactors are at 78–100%. **Three are at exactly zero**, and they
are the trading core:

- [ ] **P1 — `PlaceOrderInteractor`** (62 lines, 0%)
- [ ] **P1 — `MatchPendingOrdersInteractor`** (70 lines, 0%)
- [ ] **P1 — `PlaceLimitStopOrderInteractor`** (38 lines, 0%)

Cover the rejection paths and one happy path each: non-positive quantity, unknown
account, price-feed failure, insufficient cash, insufficient shares, stop-loss must be
a sell, and buy-vs-sell trigger comparison. At ~80% these three take interactors from
**55.4% to roughly 88%** — clearing Excellent.

- [ ] **P2 — `interface_adapter` package: 197 lines at 0%.** Presenters and
      controllers are easy to test and are the biggest cheap win on the *overall*
      number outside the views. Needed to approach 50%.
- [ ] **P1 — Coverage screenshot** from `target/site/jacoco/index.html`.
- [ ] **P1 — One slide on what is untested and why** (Swing views and `Main` are
      wiring, not logic). Required at the Excellent level.

## TIER 3 — Accessibility — DONE

- [x] **`accessibility-report.md` rewritten against this branch.** Your teammate's
      original claims were verified and largely kept — `Format.signedMoney`, the forced
      `MetalLookAndFeel`, `UiTheme` 11pt, the 1100×700 minimum are all real here. E3I
      vocabulary layered in throughout, plus the Clean Architecture tie-in.
- [x] **Accessibility fixes applied** (7 files, all in `view`, 73 tests still green):
      - **Found and fixed a real defect:** `ViewComponents.button` called
        `setFocusable(false)`, which removed **every button from the Tab order**. The app
        could not be operated by keyboard at all. One line.
      - `setLabelFor` in `OrderTicketPanel.labelledRow` → binds all six ticket captions
      - `Tables.create(model, name)` → all five tables now announce themselves
      - 7 mnemonics: Place Alt+P, Buy Alt+B, Sell Alt+S, Refresh Alt+R, Profile Alt+F,
        watchlist Add Alt+A / Remove Alt+M
      - Accessible names + tooltips on the chart span buttons
- [ ] **Remaining, if there is time:** request the system look-and-feel instead of forcing
      Metal (one line, but it restyles every panel — do not do this the night before);
      add a non-colour marker to the BUY/SELL column; check `UiTheme.GREEN` against
      `UiTheme.RED` for contrast ratio.

## TIER 4 — The presentation

- [x] **Slot confirmed: Monday 1:00–1:30 PM.** Set an alarm. Arrive by 12:40 to set up.
      1:00 PM is inside US market hours, so live quotes will actually move.
- [x] **Demo run sheet rewritten** ([demo-script.md](demo-script.md)) against this
      branch's panels, with a beat for all six user stories and a slot for each member.
- [ ] **Rehearse it once with the app open.** The control names come from the source and
      the logic was verified end to end, but nobody has walked the window itself.
- [ ] **Back up `data/` before demo rehearsals** — the app writes to `demo.json` and
      `data/candles/` on launch.
- [ ] Deck to the prof's Week 11 timing (spec 3:00, API 1:00, demo 5:00, CA 3:00,
      SOLID 3:00, code org 1:00, quality 1:00, testing 1:00, accessibility 1:00, wrap 1:00).
- [ ] **Two API endpoints on a slide** — Finnhub `/quote` and the AlphaVantage
      historical endpoint used by the chart. Worth 2 points, costs one line.
- [ ] **Architecture diagram** — required for Clean Architecture 3/5 and above.
- [ ] **Two design patterns** — Strategy (`PriceFeed`: live vs replay, chosen at the
      composition root) and Observer (view models → views). Plus one extensibility example.
- [ ] **Screenshot PR #15's review thread** for the Code Quality slide — the rubric's
      Exceptional level asks for a pull request example, and you have a real one.
- [ ] **Rehearse with a timer. Twice.** TAs cut you off at 20:00.
- [ ] **Submit to MarkUs tonight**: GitHub link, `ai.txt`, slides.
- [ ] USB-C hub. Arrive early. Give feedback to two other teams (1%).

---

## Verified today

- Branch builds clean; **80 tests pass**; Checkstyle **0 violations**; jar builds and runs
- **End-to-end run through the real object graph: 16 checks, all passing.** Account opens
  with starting cash; market buy moves cash, position and trade log; an unaffordable order
  is refused and changes nothing; a bad limit price dispatches nothing; a market order
  ignores leftover text in the trigger box; a limit order rests and then fills when the
  price hits it; and cash, orders and trades all survive a reload from disk.
- Accessibility: **14 API call sites** now, up from 2 — covering 6 bound captions,
  5 named tables, 7 mnemonics
- `target/marketmaker.jar` builds and launches
- Shade-plugin pom config and `data/` (demo.json + candles) recovered from the removed
  worktree; backups in the session scratchpad
- `config/checkstyle.xml` recovered from `stash@{0}`
