# Per-use-case UML class diagrams

One diagram per presenter, for the individual presentation rubric ("a class diagram for
your full Use Case", 5 of 20 individual points). Piazza #375 confirmed this means a
**UML class diagram**, not the Clean Architecture layer diagram - that one lives in
`../ca-diagram.png` and covers the group element instead.

Save the IntelliJ diagram exports here under these exact names, so the deck and this
folder stay in step.

| File | Use case | Presenter |
|---|---|---|
| `01-ryan-place-limit-stop-order.png` | `place_limit_stop_order` | Ryan |
| `02-team-place-order.png` | `place_order` | Ryan (team story) |
| `03-ericsson-view-portfolio-summary.png` | `view_portfolio_summary` | Ericsson |
| `04-ericsson-view-positions.png` | `view_positions` | Ericsson |
| `05-wayne-watchlist.png` | `watchlist` | Wayne |
| `06-alex-view-order-history.png` | `view_order_history` | Alex |
| `07-ben-view-candlestick-chart.png` | `view_candlestick_chart` | Ben (the trend chart) |

All five presenters now have a diagram for their own use case. Ben's export is complete -
it shows `ViewCandlestickChartInteractor`, both boundaries, both models,
`HistoricalDataAccessInterface`, `Resolution` and `HistoricalDataUnavailableException`.

### Optional extra

An export of `search_ticker` was also produced. It is not anyone's headline use case -
it supports Wayne's watchlist story, which begins with a ticker search. Keep it as a
spare if Wayne wants to show where his flow starts; save it as
`08-search-ticker-optional.png` so it does not disturb the numbering above.

## Notes for the deck

- `02-team-place-order.png` is the **team** user story, not Ryan's own. If Ryan shows
  both, say which is which - the individual rubric grades his own use case.
- Ericsson has two of his three use cases here; `calculate_realized_pnl` is not exported.
  One diagram is enough for the rubric, but the realized-P/L interactor is the one his
  story is named after, so consider adding it.
- `cancel_order` and `match_pending_orders` are also part of Ryan's story and not
  exported. Optional - `place_limit_stop_order` already covers the requirement.
- Several exports are cropped (constructor signatures cut off at the right edge, `02`
  clips a class box, and `07` loses the left edge of `ViewCandlestickChartResponseModel`
  and the bottom of the interactor). Re-export at a wider canvas or zoom out first; a
  grader reading a truncated diagram is a needless loss on presentation polish.
- `07` carries a "Powered by yFiles" watermark in the corner, as IntelliJ exports do.
  Harmless - crop it out only if you are tidying the slide anyway.
