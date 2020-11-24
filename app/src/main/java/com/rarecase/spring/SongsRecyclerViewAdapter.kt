package com.rarecase.spring

import android.content.Context
import android.graphics.Bitmap
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.rarecase.model.PidType
import com.rarecase.model.Song
import com.rarecase.presenter.contracts.ISongItemPresenter
import com.rarecase.presenter.presenters.SongItemPresenter

class SongsRecyclerViewAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    //Initialized in OnBindViewHolder
    private lateinit var songItemPresenter: ISongItemPresenter

    //Initialized in OnCreateViewHolder
    private lateinit var song_list: List<Song>
    private lateinit var _listType : PidType

    var recyclerViewContext: Context? = null              //The context of the Recycler view. Home / Download Activity

    constructor(song_list: List<Song>) : this() {
        this.song_list = song_list
    }

    constructor(song_list: List<Song>, listType : PidType) : this(song_list){
        _listType = listType
    }

    override fun getItemCount(): Int {
        return song_list.size
    }

    class myViewHolder(itemView: View, private var pidType: PidType) : RecyclerView.ViewHolder(itemView), ISongItemView {

        var id: String? = ""       //Use getSongItemId() to cache images with Pids as keys to cache map
        var album: String? = ""    //Use getSongItemAlbum() to cache images with album names as keys.

        //Using this will save memory and network request, as only one bitmap
        //will be downloaded, cached and displayed for all songs belonging to that album.
        var songTitle: TextView = itemView.findViewById(R.id.tvTitle) as TextView
        var songArtists: TextView = itemView.findViewById(R.id.tvArtists) as TextView
        var imgAlbumArt: ImageView = itemView.findViewById(R.id.albumArt) as ImageView
        var springAction: ImageButton = itemView.findViewById(R.id.btnSpringAction) as ImageButton

        override fun getSongItemId(): String? {
            return id
        }

        override fun getSongItemAlbum(): String? {
            return album
        }

        override fun setAlbumArt(albumArt: Bitmap?) {
            imgAlbumArt.setImageBitmap(albumArt)
        }

        override fun getViewPidType() : PidType{
            return pidType
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerViewContext = recyclerView?.context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layout: View = LayoutInflater.from(parent.context).inflate(R.layout.song, parent, false)

        //Initialize ViewHolder with PidType to distinguish in ItemPresenter
        val holder = myViewHolder(layout,_listType)

        if (_listType == PidType.Offline) {
            holder.springAction.setImageResource(R.drawable.locked)
        }
        return holder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val songItem: Song = song_list[position]
        val title = songItem.song

        val artists: String?
        if (songItem.featured_artists != "") {
            artists = songItem.primary_artists + " ft. " + songItem.featured_artists
        } else {
            artists = songItem.primary_artists
        }
        (holder as myViewHolder).songTitle.text = title
        holder.id = songItem.id
        holder.album = songItem.album
        holder.songArtists.text = artists
        holder.imgAlbumArt.setImageResource(R.drawable.defaultart)
        /*
        One itemPresenter created for each ViewHolder for async population of album art on ViewHolder
        The reference instance remains the same here since we only need it to access the static
        object of _itemRepository(for getting the imgCache) shared by all SongItemPresenters
         */
        songItemPresenter = SongItemPresenter(recyclerViewContext, holder)
        songItemPresenter.downloadAlbumArt(songItem)
        //songItem.albumArt = (holder.imgAlbumArt.drawable as BitmapDrawable).bitmap
        holder.springAction.setOnClickListener({ songItemPresenter.performSpringAction(songItem) })
    }
}







