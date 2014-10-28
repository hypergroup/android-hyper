package io.hypergroup.hyperexample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import java.net.URL;
import java.util.List;

import bolts.Continuation;
import bolts.Task;
import io.hypergroup.hyper.Hyper;
import io.hypergroup.hyper.json.HyperJson;


public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            testFetchDeepHyper();
            testFetchDeepString();
            testParallelFetches();
        } catch (Exception ex) {
            Log.e(TAG, "Test failed", ex);
        }

    }

    private void testFetchDeepHyper() throws Exception {
        final Hyper root = HyperJson.createRoot(new URL("http://private-4bbf0-hyperexperimental.apiary-mock.com/api"));
        Task<Hyper> task = root.get("current_user");
        task.waitForCompletion();
        Hyper result = task.getResult();
        Exception error = task.getError();
        Log.d(TAG, "current_user test, got: " + result + " w/ error: " + error);
    }

    private void testFetchDeepString() throws Exception {
        final Hyper root = HyperJson.createRoot(new URL("http://private-4bbf0-hyperexperimental.apiary-mock.com/api"));
        Task<String> task = root.get("current_user.first_name");
        task.waitForCompletion();
        String result = task.getResult();
        Exception error = task.getError();
        Log.d(TAG, "current_user.first_name test, got: " + result + " w/ error: " + error);
    }


    private void testParallelFetches() throws Exception {


        final Hyper collectionRootA = HyperJson.createRoot(new URL("http://private-4bbf0-hyperexperimental.apiary-mock.com/api/users"));
        final Hyper collectionRootB = HyperJson.createRoot(new URL("http://private-4bbf0-hyperexperimental.apiary-mock.com/api/users"));


        collectionRootA.<Hyper>getAsync("0").continueWith(new Continuation<Hyper, Void>() {
            @Override
            public Void then(final Task<Hyper> a) throws Exception {
                collectionRootA.<Hyper>getAsync("0").continueWith(new Continuation<Hyper, Void>() {
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

        collectionRootA.<Hyper>getAsync("0").continueWith(new Continuation<Hyper, Void>() {
            @Override
            public Void then(final Task<Hyper> a) throws Exception {
                collectionRootA.<Hyper>getAsync("0").continueWith(new Continuation<Hyper, Void>() {
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

        collectionRootA.<Hyper>getAsync("0").continueWith(new Continuation<Hyper, Void>() {
            @Override
            public Void then(final Task<Hyper> a) throws Exception {
                collectionRootA.<Hyper>getAsync("0").continueWith(new Continuation<Hyper, Void>() {
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

        collectionRootA.<Hyper>getAsync("0").continueWith(new Continuation<Hyper, Void>() {
            @Override
            public Void then(final Task<Hyper> a) throws Exception {
                collectionRootA.<Hyper>getAsync("0").continueWith(new Continuation<Hyper, Void>() {
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


        collectionRootB.eachAsync(true).continueWith(new Continuation<List<Object>, Void>() {
            @Override
            public Void then(final Task<List<Object>> a) throws Exception {
                collectionRootB.eachAsync(true).continueWith(new Continuation<List<Object>, Void>() {
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


//    private void traverseHyper(URL root) {
//        traverseHyper(HyperJson.createRoot(root));
//    }
//
//    private void traverseHyper(Hyper node) {
//        node.fetch().continueWith(new Continuation<Hyper, Void>() {
//            @Override
//            public Void then(Task<Hyper> task) throws Exception {
//                if (task.isFaulted()) {
//                    Log.e(TAG, "Error iterating", task.getError());
//                } else {
//                    final Hyper node = task.getResult();
//                    String[] keys = node.getAvailableKeys();
//                    for (final String key : keys) {
//                        node.get(key).continueWith(new Continuation<Object, Void>() {
//                            @Override
//                            public Void then(Task<Object> task) throws Exception {
//                                if (task.isFaulted()) {
//                                    Log.e(TAG, "Error retrieving " + key, task.getError());
//                                } else {
//                                    Object result = task.getResult();
//                                    Log.d(TAG, "Got: " + key + " :: " + result);
//                                    if (result instanceof Hyper) {
//                                        traverseHyper((Hyper) result);
//                                    }
//                                }
//                                return null;
//                            }
//                        });
//                    }
//                }
//                return null;
//            }
//        });
//    }

}
