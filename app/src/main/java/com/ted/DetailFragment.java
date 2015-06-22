package com.ted;


import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;


/**
 * A simple {@link Fragment} subclass.
 */
public class DetailFragment extends Fragment {


    public DetailFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        if (getArguments() != null) {

            String title = getArguments().getString("title");

            if (title != null && !title.isEmpty()) {
                TextView tv = (TextView)rootView.findViewById(R.id.title);
                tv.setText(title);
                tv.setVisibility(View.VISIBLE);
            }

            String videoUrl = getArguments().getString("url");

            VideoView videoView = (VideoView) rootView.findViewById(R.id.videoView);
            MediaController controller = new MediaController(getActivity());
            videoView.setVideoPath(videoUrl);
            videoView.setMediaController(controller);
            videoView.start();

        }

        return rootView;
    }


}
