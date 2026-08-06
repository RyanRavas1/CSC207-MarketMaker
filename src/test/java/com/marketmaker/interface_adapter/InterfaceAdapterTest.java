package com.marketmaker.interface_adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.marketmaker.data_access.AccountDAO;
import com.marketmaker.data_access.InMemoryAccountDAO;
import com.marketmaker.entities.Account;
import com.marketmaker.entities.Position;
import com.marketmaker.entities.Quote;
import com.marketmaker.interface_adapter.user_profile.ViewProfileController;
import com.marketmaker.interface_adapter.user_profile.ViewProfilePresenter;
import com.marketmaker.interface_adapter.user_profile.ViewProfileViewModel;
import com.marketmaker.interface_adapter.watchlist.WatchlistController;
import com.marketmaker.interface_adapter.watchlist.WatchlistPresenter;
import com.marketmaker.interface_adapter.watchlist.WatchlistViewModel;
import com.marketmaker.price_feed.PriceFeed;
import com.marketmaker.use_case.user_profile.ViewProfileInteractor;
import com.marketmaker.use_case.watchlist.WatchlistInteractor;

/** Controller -> interactor -> presenter -> view model, with a fixed-price feed. */
class InterfaceAdapterTest {

    // Every ticker quotes at $200 so the expected strings are exact.
    private static final PriceFeed FIXED_FEED = ticker -> new Quote(ticker, 200.0, Instant.EPOCH);

    @Test
    void watchlistReachesViewModelFormatted() {
        WatchlistViewModel viewModel = new WatchlistViewModel();
        WatchlistController controller = new WatchlistController(
                new WatchlistInteractor(FIXED_FEED, new WatchlistPresenter(viewModel)));

        controller.refresh(List.of("AAPL"));

        assertEquals(1, viewModel.getState().getRows().size());
        assertEquals("AAPL", viewModel.getState().getRows().get(0)[0]);
        assertEquals("$200.00", viewModel.getState().getRows().get(0)[1]);
        assertTrue(viewModel.getState().getError().isEmpty());
    }

    @Test
    void emptyWatchlistShowsErrorAndKeepsRows() {
        WatchlistViewModel viewModel = new WatchlistViewModel();
        WatchlistController controller = new WatchlistController(
                new WatchlistInteractor(FIXED_FEED, new WatchlistPresenter(viewModel)));

        controller.refresh(List.of("AAPL"));
        controller.refresh(List.of());

        assertEquals("Watchlist is empty.", viewModel.getState().getError());
        assertEquals(1, viewModel.getState().getRows().size()); // last good rows survive
    }

    @Test
    void profileReachesViewModelFormatted() {
        AccountDAO accountDAO = new InMemoryAccountDAO();
        Account account = new Account("demo", 1000.0);
        account.addPosition(new Position("AAPL", 10, 150.0));
        accountDAO.save(account);

        ViewProfileViewModel viewModel = new ViewProfileViewModel();
        ViewProfileController controller = new ViewProfileController(
                new ViewProfileInteractor(accountDAO, FIXED_FEED, new ViewProfilePresenter(viewModel)));

        controller.view("demo");

        assertEquals("demo", viewModel.getState().getUserName());
        assertEquals("$1000.00", viewModel.getState().getCashBalance());
        assertEquals("$3000.00", viewModel.getState().getTotalEquity()); // 1000 cash + 10 * 200
        assertEquals("$500.00", viewModel.getState().getHoldings().get(0)[5]); // (200 - 150) * 10
    }

    @Test
    void missingAccountShowsError() {
        ViewProfileViewModel viewModel = new ViewProfileViewModel();
        ViewProfileController controller = new ViewProfileController(
                new ViewProfileInteractor(new InMemoryAccountDAO(), FIXED_FEED, new ViewProfilePresenter(viewModel)));

        controller.view("nobody");

        assertEquals("Account not found.", viewModel.getState().getError());
        assertTrue(viewModel.getState().getHoldings().isEmpty());
    }
}
