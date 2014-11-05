package io.hypergroup.hyperexample;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import bolts.Continuation;
import bolts.Task;
import io.hypergroup.hyper.Hyper;

public class HyperFragment extends Fragment implements AdapterView.OnItemClickListener {

    private static final String TAG = HyperFragment.class.getSimpleName();

    protected static final String ARG_KEYPATH = "ARG_KEYPATH";

    private TextView mLoadingView;
    private ListView mListView;
    private HyperAdapter mAdapter;
    private String mKeyPath;

    public void setArguments(String keyPath) {
        Bundle args = new Bundle();
        args.putString(ARG_KEYPATH, keyPath);
        setArguments(args);
    }

    protected void loadArguments() {
        Bundle args = getArguments();
        loadKeyPath(args.getString(ARG_KEYPATH));
    }

    private void loadKeyPath(final String keyPath) {
        mKeyPath = keyPath;
        ((HyperActivity) getActivity()).getNode(keyPath).continueWith(new Continuation<Hyper, Object>() {
            @Override
            public Void then(final Task<Hyper> task) throws Exception {
                Log.d(TAG, Thread.currentThread() + " loadKeyPath.then");
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, Thread.currentThread() + " loadKeyPath.then.run");
                        if (task.isFaulted()) {
                            Log.e(TAG, Thread.currentThread() + " Failure", task.getError());
                            onFetchError(task.getError());
                        } else {
                            onFetchSuccess(task.getResult());
                        }
                        onFetchDone();
                    }
                });
                return null;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hyper, container, false);
        mLoadingView = (TextView) view.findViewById(R.id.text_loading);
        mListView = (ListView) view.findViewById(R.id.list);
        mListView.setOnItemClickListener(this);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadArguments();
    }

    private void onFetchError(Exception ex) {
        Toast.makeText(getActivity(), "Error loading node at " + mKeyPath + ": " + ex.getMessage(), Toast.LENGTH_SHORT).show();
    }

    private void onFetchSuccess(Hyper node) {
        Log.d(TAG, Thread.currentThread() + " onFetchSuccess");
        mAdapter = new HyperAdapter(getActivity(), node);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    private void onFetchDone() {
        mListView.setVisibility(View.VISIBLE);
        mLoadingView.setVisibility(View.GONE);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        HyperView.Entry entry = mAdapter.getItem(position);
        Object value = entry.getValue();
        if (value instanceof Hyper) {
            Hyper node = (Hyper) value;
            if (node.isFetched() || node.getHref() == null) {
                ((HyperActivity) getActivity()).openNode((Hyper) value);
            }
        }
    }
}
