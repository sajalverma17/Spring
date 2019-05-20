package com.rarecase.spring;

import com.rarecase.model.Song;
import com.rarecase.presenter.contracts.ISongListPresenter;
import java.util.List;
import java.util.concurrent.Callable;

public interface ISongListView{

    void showProgressBar();

    void hideProgressBar();

    void showSnackbar(String msg);

    void showSnackbarWithAction(String msg, String actionText, Callable action);

    void showErrorPopulatingSongs(String errorText);

    void hideErrorPopulatingSongs();

    void setSongList(List<Song> songList);

    ISongListPresenter getPresenter();

    void requestStoragePermission();
}
