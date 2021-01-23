package com.rarecase.spring;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rarecase.model.PidType;
import com.rarecase.model.Song;
import com.rarecase.presenter.contracts.ISongListPresenter;
import com.rarecase.presenter.presenters.SharedSongListPresenter;
import com.rarecase.presenter.presenters.SongListPresenter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class SharedSongListActivity extends Fragment implements ISongListView {

    SharedSongListPresenter _presenter;
    View thisView = null;
    ArrayList<Song> savedSongList;
    RecyclerView songsRecyclerView;

    static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 100;

    @Override
    public View onCreateView(LayoutInflater inflater,
                         @Nullable ViewGroup container,
                         @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        thisView = inflater.inflate(R.layout.activity_shared_songs, container, false);

        _presenter = new SharedSongListPresenter(getContext(),this);

        //Init recycler view
        songsRecyclerView = (RecyclerView) thisView.findViewById(R.id.content_home).findViewById(R.id.songsRecyclerView);
        RecyclerView.LayoutManager layoutManager1 = new LinearLayoutManager(getContext());
        songsRecyclerView.setLayoutManager(layoutManager1);
        if(savedInstanceState != null) {
            savedSongList = savedInstanceState.getParcelableArrayList("savedSongList");
        }
        //If device orientation changed after Async API call completes,
        // savedInstanceState object will set a non-null value to savedSongList
        if(savedSongList != null){
            songsRecyclerView.setAdapter(new SongsRecyclerViewAdapter(savedSongList,PidType.Shared));
            //lastSharedSongsRecyclerView.setAdapter(new SongsRecyclerViewAdapter(savedSongList));
        }
        //savedSongList will be null If device orientation is changed before the Async API call is made, so make call again
        else {
            _presenter.loadOfflineSongs();
        }
        return  thisView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("savedSongList",savedSongList);
    }

    @Override
    public ISongListPresenter getPresenter(){
        if(_presenter != null)
            return _presenter;
        else
            return new SongListPresenter(getContext(),this);
    }

    @Override
    public void requestStoragePermission(){
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults){
        if(requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                _presenter.loadOfflineSongs();
            }else{
                Toast.makeText(getActivity(),this.getString(R.string.please_grant_storage_permission),Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void showProgressBar() {

    }

    @Override
    public void hideProgressBar() {

    }

    @Override
    public void showSnackbar(String msg) {
            Snackbar.make(getActivity().findViewById(R.id.tabViewPager),msg,Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showSnackbarWithAction(String msg, String actionText, Callable action) {

    }

    @Override
    public void showErrorPopulatingSongs(String errorText) {
        LinearLayout errorLayout = (LinearLayout) thisView.findViewById(R.id.errorLayout);
        TextView textView = (TextView) errorLayout.findViewById(R.id.errorTextView);
        textView.setText(errorText);
        errorLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideErrorPopulatingSongs() {

    }

    @Override
    public void setSongList(List<Song> songList) {
        savedSongList = new ArrayList<Song>(songList);
        songsRecyclerView.setAdapter(new SongsRecyclerViewAdapter(songList,PidType.Shared));
    }
}
