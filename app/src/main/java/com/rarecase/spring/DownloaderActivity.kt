package com.rarecase.spring

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.text.Layout
import android.view.View
import android.widget.*
import com.rarecase.model.PidType
import com.rarecase.model.Song
import com.rarecase.presenter.contracts.ISongListPresenter
import com.rarecase.presenter.presenters.SongListPresenter
import com.rarecase.spring.HomeActivity.WRITE_EXTERNAL_STORAGE_REQUEST_CODE
import java.util.*
import java.util.concurrent.Callable

class DownloaderActivity : AppCompatActivity(), ISongListView {

    lateinit var _presenter : SongListPresenter
    lateinit var songsRecyclerView : RecyclerView
    var stringExtra : String? = null
    var savedSongList : ArrayList<Song>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_downloader)

        val content = findViewById<RelativeLayout>(R.id.content_home)
        songsRecyclerView = content.findViewById(R.id.songsRecyclerView) as RecyclerView
        val layoutManager = LinearLayoutManager(this)
        songsRecyclerView.layoutManager = layoutManager

        _presenter = SongListPresenter(this,this)

        //Get saved objects from Bundle
        //Fill up savedSongList when Device orientation changes
        if(savedInstanceState != null) {
            savedSongList = savedInstanceState.getParcelableArrayList<Song>("savedSongList")
        }

        //If device orientation changed after Async API call completes,
        // savedInstanceState object will set a non-null value to savedSongList
        if(savedSongList != null){
                songsRecyclerView.adapter = SongsRecyclerViewAdapter(savedSongList as List<Song>,PidType.Shared)
        }

        //savedSongList will be null If device orientation is changed before the Async API call is made, so make call again
        else if (intent.action == Intent.ACTION_SEND) {
                stringExtra = intent.getStringExtra(Intent.EXTRA_TEXT)
                _presenter.processStringExtra(stringExtra)
        }
    }

    override fun getPresenter(): ISongListPresenter {
        return _presenter
    }

    override fun showErrorPopulatingSongs(errorText: String?) {
        val errorLayout = findViewById(R.id.errorLayout) as LinearLayout
        val textView = errorLayout.findViewById(R.id.errorTextView) as TextView
        textView.text = errorText
        errorLayout.visibility = View.VISIBLE
    }

    override fun hideErrorPopulatingSongs() {
        val errorLayout = findViewById(R.id.errorLayout) as LinearLayout
        errorLayout.visibility = View.GONE
    }


    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putParcelableArrayList("savedSongList",savedSongList)
    }

    override fun showProgressBar() {
        val view = findViewById<RelativeLayout>(R.id.content_home)
        val p = view.findViewById(R.id.loadingRecyclerViewProgressBar) as ProgressBar
        p.visibility = View.VISIBLE
    }

    override fun hideProgressBar() {
        val view = findViewById<RelativeLayout>(R.id.content_home)
        val p = view.findViewById(R.id.loadingRecyclerViewProgressBar) as ProgressBar
        p.visibility = View.INVISIBLE
    }

    override fun showSnackbar(msg: String) {
        Snackbar.make(findViewById(R.id.content_home),msg, Snackbar.LENGTH_LONG).show()
    }

    override fun showSnackbarWithAction(msg: String, actionText: String?, action: Callable<*>?) {
        val s = Snackbar.make(findViewById(R.id.content_home),msg, Snackbar.LENGTH_INDEFINITE)
        s.setAction(actionText, { action?.call() })
        s.show()
    }

    override fun setSongList(songList: MutableList<Song>?) {
        savedSongList = songList as ArrayList<Song>
        songsRecyclerView.adapter = SongsRecyclerViewAdapter(songList as List<Song>,PidType.Shared)
    }

    override fun requestStoragePermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), WRITE_EXTERNAL_STORAGE_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, this.getString(R.string.storage_permission_granted), Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(this, this.getString(R.string.please_grant_storage_permission), Toast.LENGTH_LONG).show()
            }
        }
    }
}
