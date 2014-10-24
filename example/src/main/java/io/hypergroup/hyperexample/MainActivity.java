package io.hypergroup.hyperexample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

import bolts.Continuation;
import bolts.Task;
import io.hypergroup.hyper.Hyper;
import io.hypergroup.hyper.HyperJson;


public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        test();

    }

    private void test() {

        final Hyper root = HyperJson.createRoot("http://private-4bbf0-hyperexperimental.apiary-mock.com/api");
        final Hyper collectionRootA = HyperJson.createRoot("http://private-4bbf0-hyperexperimental.apiary-mock.com/api/users");
        final Hyper collectionRootB = HyperJson.createRoot("http://private-4bbf0-hyperexperimental.apiary-mock.com/api/users");

        root.<Hyper>get("current_user").continueWith(new Continuation<Hyper, Void>() {
            @Override
            public Void then(final Task<Hyper> a) throws Exception {
                root.<Hyper>get("current_user").continueWith(new Continuation<Hyper, Void>() {
                    @Override
                    public Void then(final Task<Hyper> b) throws Exception {
                        if (a.isFaulted()) {
                            Log.d(TAG, "'A' failed", a.getError());
                        }
                        if (b.isFaulted()) {
                            Log.d(TAG, "'B' failed", b.getError());
                        }
                        Log.d(TAG, "Got hyper 1 and 2 from root: " + a.getResult() + " w/ " + a.getError() + " :: " + b.getResult() + " w/ " + b.getError());
                        return null;
                    }
                });
                return null;
            }
        });

        collectionRootA.<Hyper>get("0").continueWith(new Continuation<Hyper, Void>() {
            @Override
            public Void then(final Task<Hyper> a) throws Exception {
                collectionRootA.<Hyper>get("0").continueWith(new Continuation<Hyper, Void>() {
                    @Override
                    public Void then(final Task<Hyper> b) throws Exception {
                        if (a.isFaulted()) {
                            Log.d(TAG, "'A' failed", a.getError());
                        }
                        if (b.isFaulted()) {
                            Log.d(TAG, "'B' failed", b.getError());
                        }
                        Log.d(TAG, "Got hyper 1 and 2 from collection: " + a.getResult() + " w/ " + a.getError() + " :: " + b.getResult() + " w/ " + b.getError());
                        return null;
                    }
                });
                return null;
            }
        });

        collectionRootA.<Hyper>get("0").continueWith(new Continuation<Hyper, Void>() {
            @Override
            public Void then(final Task<Hyper> a) throws Exception {
                collectionRootA.<Hyper>get("0").continueWith(new Continuation<Hyper, Void>() {
                    @Override
                    public Void then(final Task<Hyper> b) throws Exception {
                        if (a.isFaulted()) {
                            Log.d(TAG, "'A' failed", a.getError());
                        }
                        if (b.isFaulted()) {
                            Log.d(TAG, "'B' failed", b.getError());
                        }
                        Log.d(TAG, "Got hyper 1 and 2 from collection: " + a.getResult() + " w/ " + a.getError() + " :: " + b.getResult() + " w/ " + b.getError());
                        return null;
                    }
                });
                return null;
            }
        });

        collectionRootA.<Hyper>get("0").continueWith(new Continuation<Hyper, Void>() {
            @Override
            public Void then(final Task<Hyper> a) throws Exception {
                collectionRootA.<Hyper>get("0").continueWith(new Continuation<Hyper, Void>() {
                    @Override
                    public Void then(final Task<Hyper> b) throws Exception {
                        if (a.isFaulted()) {
                            Log.d(TAG, "'A' failed", a.getError());
                        }
                        if (b.isFaulted()) {
                            Log.d(TAG, "'B' failed", b.getError());
                        }
                        Log.d(TAG, "Got hyper 1 and 2 from collection: " + a.getResult() + " w/ " + a.getError() + " :: " + b.getResult() + " w/ " + b.getError());
                        return null;
                    }
                });
                return null;
            }
        });

        collectionRootA.<Hyper>get("0").continueWith(new Continuation<Hyper, Void>() {
            @Override
            public Void then(final Task<Hyper> a) throws Exception {
                collectionRootA.<Hyper>get("0").continueWith(new Continuation<Hyper, Void>() {
                    @Override
                    public Void then(final Task<Hyper> b) throws Exception {
                        if (a.isFaulted()) {
                            Log.d(TAG, "'A' failed", a.getError());
                        }
                        if (b.isFaulted()) {
                            Log.d(TAG, "'B' failed", b.getError());
                        }
                        Log.d(TAG, "Got hyper 1 and 2 from collection: " + a.getResult() + " w/ " + a.getError() + " :: " + b.getResult() + " w/ " + b.getError());
                        return null;
                    }
                });
                return null;
            }
        });


        collectionRootB.each().continueWith(new Continuation<List<Object>, Void>() {
            @Override
            public Void then(final Task<List<Object>> a) throws Exception {
                collectionRootB.each().continueWith(new Continuation<List<Object>, Void>() {
                    @Override
                    public Void then(final Task<List<Object>> b) throws Exception {
                        if (a.isFaulted()) {
                            Log.d(TAG, "'A' failed", a.getError());
                        }
                        if (b.isFaulted()) {
                            Log.d(TAG, "'B' failed", b.getError());
                        }
                        Log.d(TAG, "Got list 1 and 2 from root: " + a.getResult() + " w/ " + a.getError() + " :: " + b.getResult() + " w/ " + b.getError());
                        return null;
                    }
                });
                return null;
            }
        });
    }

}
