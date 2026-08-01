package com.marketmaker.use_case.view_positions;

import java.util.List;

public class ViewPositionsResponseModel {
    private final List<PositionView> positions;

    public ViewPositionsResponseModel(List<PositionView> positions) {
        this.positions = positions;
    }

    public List<PositionView> getPositions() { return positions; }
}
