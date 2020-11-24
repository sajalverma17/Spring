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

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.rarecase.model.PidType;
import com.rarecase.model.Song;
import com.rarecase.presenter.contracts.ISongListPresenter;
import com.rarecase.presenter.presenters.SongListPresenter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class HomeActivity extends Fragment implements ISongListView {

    static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 100;

    private RecyclerView songsRecyclerView;
    private ISongListPresenter _songListPresenter;

    private ArrayList<Song> savedSongList = null;

    private View thisView = null;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        thisView = inflater.inflate(R.layout.activity_home, container, false);

        _songListPresenter = new SongListPresenter(getContext(),this);
        songsRecyclerView = (RecyclerView) thisView.findViewById(R.id.content_home).findViewById(R.id.songsRecyclerView);
        RecyclerView.LayoutManager layoutManager1 = new LinearLayoutManager(getContext());
        songsRecyclerView.setLayoutManager(layoutManager1);

        //Get saved objects from Bundle
        //Fill up savedSongList when Device orientation changes
        if(savedInstanceState != null) {
            savedSongList = savedInstanceState.getParcelableArrayList("savedSongList");
        }
        //If device orientation changed after Async API call completes,
        // savedInstanceState object will set a non-null value to savedSongList
        if(savedSongList != null){
            songsRecyclerView.setAdapter(new SongsRecyclerViewAdapter(savedSongList,PidType.Offline));
            //lastSharedSongsRecyclerView.setAdapter(new SongsRecyclerViewAdapter(savedSongList));
        }
        //savedSongList will be null If device orientation is changed before the Async API call is made, so make call again
        else {
            _songListPresenter.loadOfflineSongs();
        }

        return thisView;
    }

    @Override
    public ISongListPresenter getPresenter(){
        if(_songListPresenter != null)
            return  _songListPresenter;
        else
            return new SongListPresenter(getContext(),this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("savedSongList",savedSongList);
    }

    @Override
    public void showProgressBar(){
        View view = thisView.findViewById(R.id.content_home);
        ProgressBar p = (ProgressBar) view.findViewById(R.id.loadingRecyclerViewProgressBar);
        p.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgressBar(){
        View view = thisView.findViewById(R.id.content_home);
        ProgressBar p = (ProgressBar) view.findViewById(R.id.loadingRecyclerViewProgressBar);
        p.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showSnackbar(String msg) {
        //Show the Snack Bar on ViewPager of TabActivity instead of on content_home of this fragment
        Snackbar.make(getActivity().findViewById(R.id.tabViewPager),msg,Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showSnackbarWithAction(String msg , String actionText, final Callable action) {
        //Show the Snack Bar on ViewPager of TabActivity instead of on content_home of this fragment
        Snackbar snackbar = Snackbar.make(getActivity().findViewById(R.id.tabViewPager),msg,Snackbar.LENGTH_INDEFINITE);

        snackbar.setAction(actionText, new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    try {
                        action.call();
                    } catch (Exception e) {
                        Log.i("HomeActivity: ","Exception calling retry callable");
                    }

                }
        });

        snackbar.show();
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
        LinearLayout errorLayout = (LinearLayout) thisView.findViewById(R.id.errorLayout);
        errorLayout.setVisibility(View.GONE);
    }

    @Override
    public void setSongList(List<Song> songList){

        savedSongList = (ArrayList<Song>) songList;
        songsRecyclerView.setAdapter(new SongsRecyclerViewAdapter(songList,PidType.Offline));
        //lastSharedSongsRecyclerView.setAdapter((new SongsRecyclerViewAdapter(songList)));
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
                _songListPresenter.loadOfflineSongs();
            }else{
                Toast.makeText(getActivity(),this.getString(R.string.please_grant_storage_permission),Toast.LENGTH_LONG).show();
            }
        }
    }

}

