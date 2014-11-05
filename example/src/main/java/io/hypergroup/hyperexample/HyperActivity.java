package io.hypergroup.hyperexample;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Stack;

import bolts.Continuation;
import bolts.Task;
import io.hypergroup.hyper.Hyper;
import io.hypergroup.hyper.HyperContext;
import io.hypergroup.hyper.json.HyperJsons;


public class HyperActivity extends Activity {

    private static final String TAG = HyperActivity.class.getSimpleName();

    private Hyper mRoot;

    private Stack<String> mTitleStack = new Stack<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HyperContext context = new HyperContext.Builder()
            .setAsyncExecutor(Task.BACKGROUND_EXECUTOR)
            .setNetworkExecutor(Task.BACKGROUND_EXECUTOR)
            .setHttpClient(HyperJsons.Client.createClient(this, "cache" + ((int) (1000000000 * Math.random())), 1024 * 1024 * 100))
            .build();

        try {
            mRoot = HyperJsons.createRoot(new URL(Constants.HYPER_ROOT), context);
        } catch (MalformedURLException ex) {
            Toast.makeText(HyperActivity.this, "Failure creating root hyper node: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }

        mRoot.fetchAsync().continueWith(new Continuation<Hyper, Object>() {
            @Override
            public Object then(final Task<Hyper> task) throws Exception {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (task.isFaulted()) {
                            Toast.makeText(HyperActivity.this, "Failed to fetch root: " + task.getError().getMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            openNode(task.getResult());
                        }
                    }
                });
                return null;
            }
        });

    }

    public Task<Hyper> getNode(String keyPath) {
        if (TextUtils.isEmpty(keyPath)) {
            return mRoot.fetchAsync();
        } else {
            return mRoot.fetchAsync(keyPath);
        }
    }

    public void openNode(Hyper node) {
        startNewFragment(node.getKeyPath(), node.getFriendlyKeyPath());
    }

    private void startNewFragment(String keyPath, String friendlyPath) {
        HyperFragment fragment = new HyperFragment();
        fragment.setArguments(keyPath);

        FragmentTransaction txn = getFragmentManager().beginTransaction();
        txn.replace(R.id.container, fragment, keyPath);
        if (!mTitleStack.empty()) {
            txn.addToBackStack(keyPath);
        }
        txn.commit();

        setTitle(friendlyPath);
        mTitleStack.push(friendlyPath);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (!mTitleStack.empty()) {
            mTitleStack.pop();
            if (!mTitleStack.empty()) {
                setTitle(mTitleStack.peek());
                return;
            }
        }
        setTitle(mRoot.getFriendlyKeyPath());
    }
}
