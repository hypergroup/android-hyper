# Hyper for Android

The goal of this library is to provide a resource for consuming [hyper+json] but allow for extending the library to other formats such as YAML and XML.
Another primary goal is to provide an easy to use API.

The API should ultimately allow for:

 1. Quick consumption of [hyper+json] endpoints
 2. Two-way data-binding
 3. Clean and friendly syntax

## For Developers

#### Using [hyper+json], a cheatsheet

At this stage in the development, prime-directive 1 is built for [hyper+json]. The syntax for creating and consuming Hyper API is as follows:

```java
// creating a root node
Hyper root = HyperJsons.createRoot(new URL("http://example.com/api/root"));

// getting properties
String firstName = root.get("current_user.first_name");
String lastName = root.get("current_user.last_name");

// getting specific nodes
Hyper scoreboard = root.get("scoreboard");

// getting items from a collection
Hyper firstPlaceUser = scoreboard.get("users.0");
String firstPlaceName = firstPlaceUser.get("first_name");
Integer firstPlaceScore = firstPlaceUser.get("score");

// getting all items from a collection
List<Hyper> users = scoreboard.get("users").each();

```

#### Creating Root Nodes Explained

There are a few methods to help you create root nodes in the `HyperJsons` class. For more information, check out the javadoc associated with each method.

```java
// Create a root node using the default OkHttpClient with http-caching
static Hyper createRoot(Context context, URL url)

// Create a root node at the given url using a default client (no caching!)
static Hyper createRoot(URL url)

// Create a root node at the given url with a custom HyperContext.
// The HyperContext lets you specify a multitude of things
// such as the OkHttpClient used as well as the background thread executor pool
static Hyper createRoot(URL url, HyperContext hyperContext)
```

#### HyperContext explained

You can create you own `HyperContext` which gives you more control over the behavior of your hyper tree. Using the `Builder` pattern, you can create you own `HyperContext`.

The `HyperContext` is shared across all nodes that are created from the original node.

```java
new HyperContext.Builder()
    .setHttpClient(myOkHttpClient)
    .setAsyncExecutor(myExecutor)
	.build();
```

#### Hyper node methods explained

There are a few more things you can do with your `Hyper` nodes. Besides acquiring information using `keyPaths`, you can also fetch collections and invalidate an item's data.

All of the methods are well documented with javadoc, this is simply a brief summary of the methods available to you.

You can also perform the property retrieval functions on new threads, usings Promises created with Bolts for Android

 * `get` -> `getAsync`
 * `fetch` -> `fetchAsync`
 * `each` -> `eachAsync`

```java
// Retrieve a property or a new node from a given key path. The magic here lies in the structure of the key path.
// Using dot-notation, you can retrieve a deep node or property, or you can select individual items from a collection using a number instead of a name.
// examples:
//
// current_user, you should expect back a Hyper node
// current_user.first_name, you should expect back a String
// current_user.friends, you should expect back a Hyper node
// current_user.friends.0, you should expect back a Hyper node
// current_user.friends.0.first_name, you should expect back a String
<T> T get(String keyPath)

// Return each item in the node's collection. T specifies the type of objects you are expecting back.
// If it is a list of nodes, expect List<Hyper>, if its a collection of strings, expect List<String>, if it is mixed, expect List<Object>
<T> List<T> each()

// Fetch this node's underlying data if it hasn't already and return itself.
Hyper fetch()

// Perform a get on a different thread
<T> Task<T> getAsync(String keyPath)

// Perform a fetch on a different thread
Task<Hyper> fetchAsync()

// Perform a get/fetch combo on a different thread, async equivalent to: node.get(keyPath).fetch()
Task<Hyper> fetchAsync(String keyPath)

// Perform an `each` on a different thread. If prefetch is true, each Hyper node in the collection will get fetched.
<T> Task<List<T>> eachAsync(boolean prefetch)

// Return the HyperContext this node is using
HyperContext getContext()

// Retrieve this node's underlying data, useful mostly for testing purposes
Data getData()

// Get the key path of this node
String getKeyPath()

// Get a friendly version of this node's keyPath (starts with `root.` instead of nothing)
String getFriendlyKeyPath()

// Get the href of this node, or null if there is none
URL getHref()

// Return whether or not the underlying data has been fetched
boolean isFetched()

// Invalidate this node, set the underlying data to null and mark it as unfetched. Useful for refreshing data.
void invalidate()

// Get the root node from which this node was pulled
Hyper getRoot()

// Get a list of properties currently available for `get`
// This value can change after a fetch
String[] getAvailableKeys()

// Return a key path relative to this node from the one you give it
String getConcatenatedKeyPath(String keyPath)
```

## For Contributors

To those contributing or planning to work on this project, a brief description of the architecture may be helpful.

A Hyper node is a virtual node that wraps data. When accessing data that doesn't exist, it uses contextual variables from HyperContext to `fetch` that data.

The `get` method retrieves the next property in the `keyPath`. If there is more keyPath to `get` then the call is made recursively.

`HyperJson` exists as a JSON implementation of `Hyper`.  `HyperJson` uses the `JsonData` implementation to parse [hyper+json] responses and use it meaningfully.

At this time there are only two implemented keywords:

 1. `collection`
 2. `href`

When contributing, be sure to write your unit tests and documentation.


##### Future tasks

 1. Set property values on nodes
 2. Implement pattern for propogating change events
 3. Easy android binding implementation
 4. Forms implementation?
 5. Paging implementation?

## Also included

In this repository, there is an example application that exists as a hyper-tree browser. You can drill down through a hyper tree's `keyPaths` and examine its contents.

[hyper+json]:https://github.com/hypergroup/hyper-json


