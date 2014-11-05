package io.hypergroup.hyperexample;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;

import bolts.Continuation;
import bolts.Task;
import io.hypergroup.hyper.Hyper;

public class HyperView extends RelativeLayout {

    private static final String TAG = HyperView.class.getSimpleName();

    public static class Entry {
        Hyper mParent;
        String mKey;
        Object mValue;
        String mContentType;

        public Entry(Hyper parent, String key, Object value) {
            mParent = parent;
            mKey = key;
            mValue = value;
        }

        public Hyper getParent() {
            return mParent;
        }

        public String getKey() {
            return mKey;
        }

        public Object getValue() {
            return mValue;
        }
    }

    Entry mEntry;

    public HyperView(Context context) {
        super(context);
        init(context);
    }

    private ImageView mImageView;
    private TextView mLabelTextView;
    private TextView mValueTextView;

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_hyper, this);
        setMinimumHeight(getResources().getDimensionPixelSize(R.dimen.hyper_item_height));
        mLabelTextView = (TextView) findViewById(R.id.text_label);
        mValueTextView = (TextView) findViewById(R.id.text_value);
        mImageView = (ImageView) findViewById(R.id.image);
    }

    public void setEntry(Entry entry) {
        mEntry = entry;
        mLabelTextView.setText(entry.getKey());
        Object value = entry.getValue();
        if (value instanceof Hyper) {
            loadHyper((Hyper) value);
        } else {
            String key = entry.getKey();
            if ("url".equals(key)) {
                showText("(loading)");
                loadUrl(entry);
            } else {
                showText(value.toString());
            }
        }
    }

    private void loadHyper(Hyper node) {
        if (node.getHref() != null) {
            showText("(loading)");
            node.fetchAsync().continueWith(new Continuation<Hyper, Object>() {
                @Override
                public Void then(final Task<Hyper> task) throws Exception {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, Thread.currentThread() + " loadHyper.then.run");
                            if (task.isFaulted()) {
                                Log.d(TAG, Thread.currentThread() + " Error fetching node", task.getError());
                                showText("xxx");
                            } else {
                                if (mEntry != null && mEntry.getValue() == task.getResult()) {
                                    Log.d(TAG, Thread.currentThread() + " loadHyper.then.run");
                                    showText("-->");
                                }
                            }
                        }
                    });
                    return null;
                }
            });
        } else {
            showText("-->");
        }
    }

    private void loadUrl(final Entry entry) {
        if (entry.mContentType == null) {
            (new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        OkHttpClient client = entry.getParent().getContext().getHttpClient();
                        Request request = new Request.Builder().head().url(entry.getValue().toString()).build();
                        Response response = client.newCall(request).execute();
                        final String contentType = response.header("content-type");
                        post(new Runnable() {
                            @Override
                            public void run() {
                                entry.mContentType = contentType;
                                onContentType(contentType, entry);
                            }
                        });
                    } catch (Exception ex) {
                        Log.e(TAG, Thread.currentThread() + " Error fetching url: " + entry.getValue(), ex);
                    }


                }
            })).start();
        } else {
            onContentType(entry.mContentType, entry);
        }
    }

    private void showText(String text) {
        mImageView.setVisibility(View.GONE);
        mValueTextView.setVisibility(View.VISIBLE);
        mValueTextView.setText(text);
    }

    private void showImage(String url) {
        mImageView.setVisibility(View.VISIBLE);
        mValueTextView.setVisibility(View.GONE);
        Picasso.with(getContext()).load(url).into(mImageView);
    }

    private void onContentType(String contentType, Entry entry) {
        if (contentType != null) {
            if (contentType.startsWith("image/")) {
                showImage(entry.getValue().toString());
            } else {
                showText(contentType);
            }
        } else {
            showText(entry.getValue().toString());
        }
    }


}
