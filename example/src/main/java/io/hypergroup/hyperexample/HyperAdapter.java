package io.hypergroup.hyperexample;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

import io.hypergroup.hyper.Hyper;
import io.hypergroup.hyper.exception.HyperException;

public class HyperAdapter extends ArrayAdapter<HyperView.Entry> {

    private static final String TAG = "HyperAdapter";

    static void addKeyEntries(Hyper node, List<HyperView.Entry> entries) {
        String[] keys = node.getAvailableKeys();
        for (String key : keys) {
            if (!"collection".equals(key) && !"href".equals(key)) {
                try {
                    entries.add(new HyperView.Entry(node, key, node.get(key)));
                } catch (Exception ex) {
                    Log.e(TAG, "Error getting attribute " + key, ex);
                }
            }
        }
    }

    static void addCollectionEntries(Hyper node, List<HyperView.Entry> entries) {
        List<Object> collection = null;
        try {
            collection = node.each();
        } catch (HyperException ex) {
            // snarf
        } catch (Exception ex) {
            Log.e(TAG, "Error getting collection", ex);
        }

        if (collection != null) {
            for (int index = 0; index < collection.size(); index++) {
                entries.add(new HyperView.Entry(node, String.valueOf(index), collection.get(index)));
            }
        }
    }

    static List<HyperView.Entry> makeEntries(Hyper node) {
        List<HyperView.Entry> entries = new ArrayList<HyperView.Entry>(32);
        addKeyEntries(node, entries);
        addCollectionEntries(node, entries);
        return entries;
    }

    public HyperAdapter(Context context, Hyper node) {
        super(context, 0, makeEntries(node));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HyperView view;
        if (convertView != null) {
            view = (HyperView) convertView;
        } else {
            view = new HyperView(getContext());
        }
        HyperView.Entry entry = getItem(position);
        view.setEntry(entry);
        return view;
    }
}
