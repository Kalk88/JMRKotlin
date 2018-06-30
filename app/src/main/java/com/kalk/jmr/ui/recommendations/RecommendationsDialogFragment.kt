package com.kalk.jmr.ui.recommendations

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kalk.jmr.R
import com.kalk.jmr.SpotifyCommands
import com.kalk.jmr.getGenreRepository
import com.kalk.jmr.getPlaylistRepository
import com.kalk.jmr.ui.genres.GenresViewModel
import com.kalk.jmr.ui.genres.GenresViewModelFactory
import com.kalk.jmr.ui.settings.SettingsViewModel
import kotlinx.android.synthetic.main.recommendations_dialog_fragment.*
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.doAsync

class RecommendationsDialogFragment: DialogFragment() {
    private lateinit var playCommands: SpotifyCommands
    private lateinit var genreViewModel: GenresViewModel
    private lateinit var recommendationsViewModel: RecommendationsViewModel
    private lateinit var settings: SettingsViewModel
    private var tracks:ArrayList<String> = arrayListOf()

    companion object {
        val TAG = RecommendationsDialogFragment::class.java.simpleName
        fun newInstance() = RecommendationsDialogFragment()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        playCommands = context as SpotifyCommands
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.recommendations_dialog_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        genreViewModel = ViewModelProviders.of(activity!!,
                GenresViewModelFactory(
                        getGenreRepository(activity!!.applicationContext)))
                .get(GenresViewModel::class.java)

        recommendationsViewModel = ViewModelProviders.of(activity!!, RecommendationsViewModelFactory(
                getPlaylistRepository(activity!!.applicationContext)))
                .get(RecommendationsViewModel::class.java)

        /**
         * SETTINGS
         */
        settings = ViewModelProviders.of(activity!!).get(SettingsViewModel::class.java)

        button_recommend_from_history.isEnabled = false
        doAsync {
            //Settings values can never be null as the are always set when main activity is created.
            tracks = recommendationsViewModel.tracksConnectedToContext(
                    checkForActivity = settings.activity.value!!,
                    checkForLocation = settings.location.value!!,
                    checkForTime = settings.time.value!!)
            number_of_tracks.text = resources.getString(R.string.found_with_current_context, tracks.size)
            if (tracks.isNotEmpty())
                button_recommend_from_history.isEnabled = true
        }

        button_recommend_from_genre.setOnClickListener {
            if(genreViewModel.chosenGenre.value != null && genreViewModel.genreText.value != null ) {
                val id = genreViewModel.chosenGenre.value!!
                val text = genreViewModel.genreText.value!!


                doAsync {
                    val recommendation = recommendationsViewModel.makeRecommendationFromGenre(text)
                    if(recommendation.isNotEmpty()) {
                        playCommands.play(recommendation.map { it.uri })
                        recommendationsViewModel.storePlaylist(id, recommendation)
                    }
                    snackbar(parentFragment?.view!!, "Retrieving tracks recommendation with ${text}")
                    dismiss()
                }
            } else {
                snackbar(parentFragment?.view!!, "Please choose a genre")
                dismiss()
            }
        }

        if (tracks.isEmpty()) button_recommend_from_history.isEnabled = false
        button_recommend_from_history.setOnClickListener {
            doAsync {
                if(tracks.isNotEmpty()) {
                    val recommendation = recommendationsViewModel.makeRecommendationFromHistory(tracks)

                    if(recommendation.isNotEmpty()) {
                        playCommands.play(recommendation.map { it.uri })
                        recommendationsViewModel.storePlaylist(-1, recommendation)
                    }
                    snackbar(parentFragment?.view!!, "Retrieving tracks recommendation with history")
                    dismiss()
                }
            }
        }
    }
}