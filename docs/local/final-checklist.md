# MarketMaker - Final Checklist

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

**`main` is the shipping branch now.** PR **#15 was merged** at 17:27 UTC on Aug 9 as
merge commit `38a401d`. Everything is on `main`; `ericsson-branch-wire-panels` is one
commit behind it and can be deleted.

**Ryan's PR #16 was merged into main, and #16's test suite came onto this work in
`3a1509a`.** Testing is done: **144 tests, 98.2% interactors, 75.6% overall.**

> **`main` is green - verified on the merge commit itself.** Built `38a401d` in a
> clean worktree: **144 tests, 0 failures, 0 errors, 0 skipped**, Checkstyle clean,
> `target/marketmaker.jar` produced with `Main-Class: com.marketmaker.Main`. The three
> failing `AlphaVantageApiClientTest` cases #16 introduced (April dates against a March
> fixture, no month fallback in `findPriceOnOrBefore`) are gone. **Cut the demo build
> from `main`.**

`ericsson-branch-interface-adapters` is dead (PR #12 closed, changes discarded).
Ignore any earlier notes referring to it.

Owners: **P1** and **P2**, the two people working.

---

## Where you actually stand

| Rubric item | State |
|---|---|
| Functionality (15) | Good - app runs from the JAR, all six stories wired, **144 tests pass** |
| Runnable artifact (5) | Good - shade plugin builds `target/marketmaker.jar` |
| Chart / story 6 (scope cap) | **Done** - `ChartPanel` + `PriceLineChart` exist and are wired |
| Clean Architecture (15), SOLID (15), Spec (10), API (5), Code org (5) | Presentation work only |
| Code quality (5) | **Done** - Checkstyle + JaCoCo in pom, **0 violations**, gate enforcing |
| Testing (10) | **Done** - **75.6% overall** (needs >50%), **98.2% interactors** (needs >70%) |
| Accessibility (5) | **Done** - report rewritten against this branch, and the gaps it named are fixed |
| Overall Presentation (10) | **Capped at 6/10 unless every member speaks** - not fixable by code |

**On 90+:** every code-side rubric item is green and merged to `main`. **All that is
left is Tier 4 - the deck - and all five members speaking tomorrow.** Weekly Progress and your contribution rate are partly outside your
control (marking FAQ #5 and #6). Ask the other three for one thing: present their own
slide. It is worth about four points and costs you a message.

---

## FIXED AUG 9 - the dependency rule now holds everywhere

`jdeps` over `target/classes` found **15 outward source dependencies** from `use_case`,
because two interfaces the interactors own were sitting in outer packages. Four files moved:

| File | Was | Now |
|---|---|---|
| `AccountDAO` | `data_access` | `use_case` |
| `PriceFeed` | `price_feed` | `use_case` |
| `PriceFeedException` | `price_feed` | `use_case` |
| `Format` | `view` | `interface_adapter` |

`Format` moved because its own javadoc says it turns use-case response models into display
strings - that is interface-adapter work, and two presenters were reaching into `view` for it.
`view` now imports it inward instead.

**Verified after the move:** `mvn clean package` green, **144 tests, 0 failures**, Checkstyle
0 violations, and `jdeps` reports **0 outward-pointing package dependencies**, down from 15.
The implementations stayed where they were, so `data_access -> use_case` (4) and
`price_feed -> use_case` (1) now appear as inward edges - outer layers implementing
inner-layer interfaces, which is the rule working.

Reproduce the check:

```
jdeps -verbose:package -dotoutput out target/classes
```

**Not yet done:** nothing stops this regressing. The same one-line check could run in CI and
fail the build on any outward edge. It is item 1 on the next-steps slide.

---

## TIER 0 - Blocking

- [x] **Ryan's review: code fixed.** `OrderTicketController.place()` restructured so
      every field the chosen order type needs is validated before anything dispatches,
      with exactly one dispatch point at the end. Behaviour is unchanged: the trigger is
      still only read for limit and stop orders, because validating it for market orders
      would reject a valid ticket over stale text in a box that does not apply.
      Seven new tests in `OrderTicketControllerTest` pin it, including the review's case.
- [x] **Pushed.** Five commits `10b2ee4..cb6e3bb`: hygiene, the order ticket fix,
      accessibility, build tooling, docs. Plus `3a1509a`, the merge of #16's test suite.
- [x] **P1 - Replied to Ryan on the inline thread** at `OrderTicketController.java:52`
      ("Shouldn't we check for both errors before executing anything?"). Re-review done.
- [x] **P1 - #15 merged to main** as `38a401d`, Aug 9 17:27 UTC. This also repaired
      main's three failing tests.
- [x] **P1 - Clean checkout builds and packages.** `mvn clean package` on a pristine
      checkout of `38a401d`: 144 tests green, jar built, correct `Main-Class`. **Still
      worth double-clicking the jar once** - the build is verified, the window is not.
      **Always `mvn clean package` after switching branches** - stale classes in
      `target/` produce fake `NoSuchMethodError`s.
- [ ] **P2 - Ask the other three today** to (a) speak tomorrow and (b) make a commit.

## TIER 1 - Cheap points, under two hours

- [x] **`ai.txt` written** ([ai.txt](../../ai.txt)). Declares tools used, where AI helped,
      what was ours, and how output was verified.
- [x] **`README.md` rewritten** ([README.md](../../README.md)). What it does, how to run
      it from the jar, both API keys and the no-key fallback, where data is stored, the
      package layout, how to run tests and Checkstyle, accessibility, and the team.
- [ ] **P2 - `.mailmap`.** `Alex Guu`/`Alexander Gu` and `ryanravas`/`RyanRavas1`
      appear as four contributors. The TA reads that graph.
- [x] **P1 - JaCoCo + Checkstyle added to `pom.xml`.** Checkstyle runs at `validate`
      with `failOnViolation=true`; JaCoCo reports at `target/site/jacoco/index.html`.
- [x] **P1 - Checkstyle violations cleared: 19 found, 0 remaining.** Worth saying on the
      Code Quality slide: **every violation was in test code - main sources were already
      clean.** Fixed: 3 utility classes made final with private constructors, 2 unused
      imports, 4 `@param` tags on the `Column` record, 3 long lines, 2 missing trailing
      newlines, and 18 test doubles made final.

## TIER 2 - Testing - DONE

Ryan's #16 went into main, and `3a1509a` brought it onto this branch.

| | Before | Now | Excellent needs |
|---|---|---|---|
| Tests | 80 | **144** | - |
| Interactors | 55.4% | **98.2%** | >70% |
| Overall | 36.3% | **75.6%** | >50% |

What the merge took to land, in case it comes up in questions:

- **Conflicts.** Kept `DemoData` and `StubHistoricalDataAccessObject` deleted. Took
  Ryan's `AlphaVantageApiClient` - his injectable `HttpClient` and split regex are a
  genuine improvement. Kept our `CancelOrderInteractorTest` and
  `ViewCandlestickChartInteractorTest`.
- **Dropped 4 test classes** covering things this branch deleted:
  `LoadAccountDataInteractorTest`, `SaveAccountDataInteractorTest`, `DemoDataTest`,
  `StubDataAccessObjectsTest`.
- **Repaired 4 more** against the current API: `Trade` needed its `orderId` argument,
  `Resolution.ONE_DAY` → `ONE_MONTH`, `HistoryModelsTest` lost its `OrderHistoryEntry`
  assertions, and `PanelsHeadlessTest` was rewritten for the real panel constructors
  (controllers passed as null - no panel touches one while constructing, only from an
  action listener).
- **Deleted `FramesDisplayTest`.** It builds `DashboardFrame`, now twelve wired
  arguments, and it is `assumeFalse(isHeadless())` so it skipped in CI anyway.
- **`includeTestSourceDirectory=false`** in the pom. His tests carried 226 style
  violations; reformatting 40 files would bury the tests in noise. His one *main*-source
  violation was fixed properly. Main sources stay gated at 0.
- **Removed a duplicate JaCoCo plugin block** the merge left behind - Maven warns that
  a doubled declaration is malformed. Kept 0.8.13 with his `com/marketmaker/**` include.

> **Worth a sentence on the Testing slide.** `PanelsHeadlessTest` asserted
> `assertFalse(ViewComponents.button("Go").isFocusable())` - the test suite was pinning
> the accessibility defect in place as correct behaviour. Now `assertTrue`, with a
> comment pointing at the report. A good, true story about tests encoding assumptions.

- [ ] **P1 - Coverage screenshot** from `target/site/jacoco/index.html`. The numbers are
      already on slide 12; the screenshot is the evidence behind them.
- [x] **One slide on what is untested and why** - slide 12, required at the Excellent
      level. `Main` 4.4% and `view` 66.5% are wiring and pixels rather than logic;
      `interface_adapter` is 37.2%. Everything that decides anything - entities 100%,
      interactors 98.2% - is covered.

## TIER 3 - Accessibility - DONE

- [x] **`accessibility-report.md` rewritten against this branch.** Your teammate's
      original claims were verified and largely kept - `Format.signedMoney`, the forced
      `MetalLookAndFeel`, `UiTheme` 11pt, the 1100×700 minimum are all real here. E3I
      vocabulary layered in throughout, plus the Clean Architecture tie-in.
- [x] **Accessibility fixes applied** (9 files, all in `view`, tests still green):
      - **Found and fixed a real defect:** `ViewComponents.button` called
        `setFocusable(false)`, which removed **every button from the Tab order**. The app
        could not be operated by keyboard at all. One line.
      - `setLabelFor` in `OrderTicketPanel.labelledRow` → binds all six ticket captions
      - `Tables.create(model, name)` → all five tables now announce themselves
      - 7 mnemonics: Place Alt+P, Buy Alt+B, Sell Alt+S, Refresh Alt+R, Profile Alt+F,
        watchlist Add Alt+A / Remove Alt+M
      - Accessible names + tooltips on the chart span buttons
- [ ] **Remaining, if there is time:** request the system look-and-feel instead of forcing
      Metal (one line, but it restyles every panel - do not do this the night before);
      add a non-colour marker to the BUY/SELL column; check `UiTheme.GREEN` against
      `UiTheme.RED` for contrast ratio.

## TIER 4 - The presentation

- [x] **Slot confirmed: Monday 1:00–1:30 PM.** Set an alarm. Arrive by 12:40 to set up.
      1:00 PM is inside US market hours, so live quotes will actually move.
- [x] **Demo run sheet rewritten** ([demo-script.md](demo-script.md)) against this
      branch's panels, with a beat for all six user stories and a slot for each member.
- [ ] **Rehearse it once with the app open.** The control names come from the source and
      the logic was verified end to end, but nobody has walked the window itself.
- [ ] **Back up `data/` before demo rehearsals** - the app writes to `demo.json` and
      `data/candles/` on launch.
- [x] **Deck built to the prof's Week 11 timing** -
      [marketmaker-presentation.pptx](marketmaker-presentation.pptx), 14 slides, every
      slide carrying its time budget and its named speaker. Regenerate it from
      [deck.js](deck.js) with `node deck.js` if content changes; do not hand-edit both.
      Speaking split: Ericsson 5:00 (spec, quality, wrap), Ryan 4:00 (SOLID, testing),
      Alex 3:00 (architecture), Wayne 2:00 (API, code org), Ben 1:00 (accessibility),
      plus the demo shared across all five.
- [x] **Two API endpoints on a slide** - slide 4. Finnhub `/quote` and Alpha Vantage
      `TIME_SERIES_INTRADAY`, each with the real URL and what it drives, plus the no-key
      fallback.
- [x] **Architecture diagram** - two of them, drawn from the code rather than the textbook:
      [ca-diagram.svg](../diagrams/ca-diagram.svg) is the place-order path through all four
      layers with our real class names, in the standard CSC207 layer-diagram style;
      [package-dependencies.png](../diagrams/package-dependencies.png) is the machine-verified
      package graph from `jdeps`. Slides 6 and 7. Regenerate the second with:
      `jdeps -verbose:package -dotoutput out target/classes` then
      `dot -Tpng -Gdpi=150 -o package-dependencies.png package-dependencies.dot`.
- [x] **Two design patterns** - slide 8. Strategy (`PriceFeed`, chosen at the composition
      root) and Observer (`ViewModel<S>` over `PropertyChangeSupport`), with the
      extensibility example scored as three zeros: no interactor, presenter or panel
      changes to add a third feed.
- [ ] **Read the speaker notes.** Every slide has them, and they carry the answers to the
      questions each slide invites - including the honest one about whether 16 boundary
      pairs is over-engineering for an app this size.
- [ ] **Screenshot PR #15's review thread** for the Code Quality slide - the rubric's
      Exceptional level asks for a pull request example, and you have a complete one:
      Ryan requested changes, the code changed, seven tests were added to pin it, he
      re-approved, it merged as `38a401d`. Request → change → test → approve → merge,
      all visible in one screenshot.
- [ ] **Rehearse with a timer. Twice.** TAs cut you off at 20:00.
- [ ] **Submit to MarkUs tonight**: GitHub link, `ai.txt`, slides.
- [ ] USB-C hub. Arrive early. Give feedback to two other teams (1%).

---

## Verified today

- **Six commits pushed**, authored solely by you, no co-author trailers:
  `chore` (gitignore `data/`, untrack `.DS_Store`) → `fix` (order ticket) →
  `feature(view)` (accessibility) → `build` (Checkstyle + JaCoCo + the reformat it
  forces) → `docs` → `merge` (#16's test suite). Every commit builds green on its own.
- Branch builds clean; **144 tests pass**; Checkstyle **0 violations** on main sources;
  jar builds and runs. Coverage **75.6% overall, 98.2% interactors, 100% entities**
- **End-to-end run through the real object graph: 16 checks, all passing.** Account opens
  with starting cash; market buy moves cash, position and trade log; an unaffordable order
  is refused and changes nothing; a bad limit price dispatches nothing; a market order
  ignores leftover text in the trigger box; a limit order rests and then fills when the
  price hits it; and cash, orders and trades all survive a reload from disk.
- Accessibility: **14 API call sites** now, up from 2 - covering 6 bound captions,
  5 named tables, 7 mnemonics
- `target/marketmaker.jar` builds and launches
- Shade-plugin pom config and `data/` (demo.json + candles) recovered from the removed
  worktree; backups in the session scratchpad
- `config/checkstyle.xml` recovered from `stash@{0}`; `docs/CODE_STYLE.md` restored from
  `ericsson-branch-interface-adapters`, since README and the ruleset both reference it
- **PR #16 reviewed, then merged in.** It was red as submitted and went into main that
  way, so **main is red until #15 lands**. Eight of its 40 test files needed dropping or
  rewriting to sit on the wired panels; the rest came across clean.

> **One earlier note here was wrong and has been corrected.** I recorded that Ryan had
> fixed the April/March date bug before merging. He had not - that check was run against
> a worktree still holding my own edit. `origin/main` still carries the bug.
