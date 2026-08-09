package com.marketmaker.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

import com.marketmaker.interface_adapter.ViewModel;
import com.marketmaker.use_case.view_positions.PositionView;
import com.marketmaker.use_case.view_positions.ViewPositionsResponseModel;

/** Current holdings, priced at the latest quote. */
public class PositionsPanel extends TitledPanel {

    private static final List<Column<PositionView>> COLUMNS = List.of(
            Column.of("Sym", String.class, PositionView::getTicker),
            new Column<>("Qty", Integer.class, PositionView::getShares, CellStyle.NUMBER),
            new Column<>("Avg", Double.class, PositionView::getAverageCost, CellStyle.NUMBER),
            new Column<>("Last", Double.class, PositionView::getCurrentPrice, CellStyle.NUMBER),
            new Column<>("Unrl P/L", Double.class, PositionView::getUnrealizedPnL, CellStyle.SIGNED));

    private final ListTableModel<PositionView> model = new ListTableModel<>(COLUMNS);

    public PositionsPanel(ViewModel<ViewPositionsResponseModel> viewModel) {
        super("Positions");
        setPreferredSize(new Dimension(358, 138));

        getContent().add(Tables.scroll(Tables.create(model, "Open positions")), BorderLayout.CENTER);

        viewModel.onState(response -> model.setRows(response.getPositions()));
    }
}
