package com.rarecase.presenter.contracts;

public interface ISongListPresenter {
    /**
     * Called from UI by presenters. Implement fetching and setting songs in the View here
     */
    void loadOfflineSongs();
}
