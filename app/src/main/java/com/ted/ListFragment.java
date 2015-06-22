package com.ted;


import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ListFragment extends Fragment {

    private ListView mList;

    private boolean mFillTheList = true;

    private ImmageArrayAdapter mImmageAdapter;

    private int mSelectedPos;

    class TED {

        public String title;
        public String image_url;
        public String link;
        public String enclosure;
    }

    class ImmageArrayAdapter extends ArrayAdapter<TED> {

        public ImmageArrayAdapter(Context context, List<TED> values) {
            super(context, R.layout.image_list_item, values);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            Tag tag;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.image_list_item, parent, false);
                convertView.setTag(tag = new Tag(convertView));
            } else {
                tag = (Tag) convertView.getTag();
            }

            final TED ted = getItem(position);
            tag.img.setImageBitmap(null);
            tag.title.setText(ted.title);

            mSelectedPos = position;

            tag.img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    DetailFragment detail = new DetailFragment();

                    Bundle args = new Bundle();
                    args.putString("url", ted.enclosure);
                    args.putString("title", ted.title);

                    detail.setArguments(args);

                    //FragmentManager fragmentManager = getFragmentManager();
                    //fragmentManager.beginTransaction().replace(R.id.container, detail).commit();

                    getFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.container, detail).commit();

                }
            });

            // рисуем элемент списка в фоне
            new AsyncTask<Tag, Void, Bitmap>(){

                private Tag tag;

                @Override
                protected Bitmap doInBackground(Tag... tags) {

                    tag = tags[0];


                    if ( ted.image_url != null && !ted.image_url.isEmpty()) {
                        try {


                            return Utils.loadImage(ted.image_url);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    return null;
                }


                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    super.onPostExecute(bitmap);

                    if ( bitmap != null) {
                        tag.img.setImageBitmap(bitmap);
                    }
                }

            }.execute(tag);

            return  convertView;
        }

        final class Tag {
            final ImageView img;
            final ProgressBar progress;
            final TextView title;

            Tag(View view){

                img = (ImageView) view.findViewById(R.id.img);
                title = (TextView) view.findViewById(R.id.title);
                progress = (ProgressBar) view.findViewById(R.id.progress);
            }
        }

    }

    public ListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_list, container, false);
        mList = (ListView)rootView.findViewById(R.id.list);
        return rootView;

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {

        super.onStart();

        if (this.mFillTheList) {

            new AsyncTask<Void, Void, List<TED>>(){

                @Override
                protected List<TED> doInBackground(Void... params) {
                    List<TED> result = null;
                    try {
                        String feed = getAndroidPitRssFeed();
                        result = parse(feed);
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return result;
                }

                @Override
                protected void onPostExecute(List<TED> listTED) {
                    super.onPostExecute(listTED);

                    if (listTED != null) {
                        mImmageAdapter = new ImmageArrayAdapter(getActivity(), listTED);
                        mList.setAdapter(mImmageAdapter);

                    }

                }

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                }

            }.execute();

            mFillTheList = false;

        } else {
            mList.setAdapter(mImmageAdapter);
            mList.setSelection(mSelectedPos);

        }
    }

    public String getAndroidPitRssFeed(){

        InputStream in = null;
        try {

            //URL url = new URL("http://www.androidpit.com/feed/main.xml");
            URL url = new URL("http://www.ted.com/themes/rss/id/6");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            in = conn.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int count; (count = in.read(buffer)) != -1; ) {
                out.write(buffer, 0, count);
            }
            byte[] response = out.toByteArray();
            String rssFeed = new String(response, "UTF-8");

            return rssFeed;

            //mRssFeed.setText(rssFeed);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;

    }


    public List<TED> parse(String feed) throws XmlPullParserException, IOException {

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(new StringReader(feed));

        return readRss(xpp);
    }

    private List<TED> readRss(XmlPullParser parser) throws XmlPullParserException, IOException {

        List<TED> items = new ArrayList<>();

        TED ted = null;

        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {

            if(eventType == XmlPullParser.START_DOCUMENT) {

                Log.d("RSS", "Start document");

            } else if(eventType == XmlPullParser.START_TAG) {

                if (parser.getName().equalsIgnoreCase("item")) {

                    ted = new TED();

                } else if (parser.getName().equalsIgnoreCase("enclosure")) {

                    if (ted != null) {
                        String url = parser.getAttributeValue(0);
                        ted.enclosure = url;
                    }

                } else if (parser.getName().equalsIgnoreCase("title")) {

                    if (ted != null) {
                        parser.next();

                        eventType = parser.getEventType();

                        if(eventType == XmlPullParser.TEXT) {
                            ted.title = parser.getText();
                        }
                    }

                } else if (parser.getName().equalsIgnoreCase("itunes:image")) {
                    if (ted != null) {
                        String image_url = parser.getAttributeValue(0);
                        ted.image_url = image_url;
                    }

                }

                //Log.d("RSS", "Start tag " + parser.getName());

            } else if(eventType == XmlPullParser.END_TAG) {

                if (parser.getName().equalsIgnoreCase("item")) {
                    items.add(ted);
                }

                //Log.d("RSS", "End tag " + parser.getName());

            } else if(eventType == XmlPullParser.TEXT) {

                //Log.d("RSS", "Text " + parser.getText());
            }
            eventType = parser.next();
        }

        System.out.println("End document");



        return items;
    }

}
