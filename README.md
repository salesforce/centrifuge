# Centrifuge Warmup Engine

Centrifuge is a framework for scheduling and running startup and warmup tasks; mostly used for JVM warmup.  
JVM does not perform ideally at startup until JIT performs compilation of frequently executed code paths.  
This has high performance impact on JVM restarts.  Centrifuge provides a generic interface for implementing 
various warmup logic (e.g., simply calling an HTTP endpoint, populating cache, pre-compilation for generated 
code, etc.), and thread management for executing the warmup code.  A warmer implementation is executed over and over
until either it times out or the maximum number of iterations asked by user is reached.

### How to use?

First include the maven dependency like this:

```xml
<dependency>
    <groupId>com.salesforce.centrifuge</groupId>
    <artifactId>centrifuge-core</artifactId>
    <version>${centrifuge.version}</version>
</dependency>
```

Implement the `Warmer` interface; make sure:

- Warmer has an accessible default constructor.
- Warmer does not catch `InterruptedException`.


```java
public interface Warmer {

    /**
     * This method is called at the beginning of execution; if an exception
     * is thrown, centrifuge pauses (thread sleeps) for a configurable
     * duration, and then retries calling init.
     *
     * @param params parameters passed from config file
     */
    void init(final Map<String, Object> params) throws Exception;

    /**
     * This method is called as often as the number of iterations (config);
     * or until the max failure (exception thrown) is reached (config);
     * or until the max timeout for this warmer is reached (config);
     * or until the max timeout for engine is reached (config).
     */
    void next() throws Exception;
}
```

Add configurations to register warmer (the `EchoWarmer` is a simple warmer example):

```java
// configuration for warmup engine
centrifuge {

  warmers = [
    {
      // warmer class name (required)
      class = "com.salesforce.centrifuge.EchoWarmer"
      
      // flag to tell centrifuge that this warmer has to finish executing before app is warm (optional, default = false)
      required = false
      
      // maximum number of iterations to call the next() method (optional, default = 1)
      max_iterations = 100
      
      // maximum cumulative execution time to run this warmer (optional, default = Long.MAX_VALUE)
      timeout_millis = 500
      
      // time to yield before calling next() and init() to prevent exhausting resources (optional, default = 100)
      yield_millis = 10
      
      // maximum number of failures allowed before stopping warmer (optional, default = 3)
      max_failure = 1
      
      // number of concurrent threads to run this warmer (optional, default = 1)
      concurrency = 3
      
      // custom parameters to be passed to warmers init() method as a map (optional, default = Collections.emptyMap())
      params = {
        text = "hello world!"
      }
    }
  ]
}
```

Initialize the Centrifuge engine:

```java
public static void main(final String[] args) throws Exception {

    // config for engine is loaded from the path given
    final CentrifugeConfig centrifugeConfig = new CentrifugeConfig("centrifuge.conf");

    // create an instance and start
    final Centrifuge centrifuge = Centrifuge.newInstance(centrifugeConfig);
    centrifuge.start();
}
```

### Warmers

A number of commonly used warmers are implemented in the `centrifuge-warmer` module;  Include the following maven 
dependency to pull them in to your project:

```xml
<dependency>
    <groupId>com.salesforce.centrifuge</groupId>
    <artifactId>centrifuge-warmers</artifactId>
    <version>${centrifuge.version}</version>
</dependency>
```

Below is the list of warmers available:

#### HTTP Warmer

The HTTP Warmer can be used as a generic warmer to call HTTP endpoints in order to trigger code path exercised by the
resource implementing the endpoint.  For example, an application may provide a homepage URL that when called would do
basic initializations, connect to a database and cache, etc.  These code paths may be warmed (JITed) by simply using 
the HTTP warmer to hit the homepage endpoint for a number of iterations.

Here is an example of how to use this warmer:

```java
{
  class = "com.salesforce.centrifuge.warmers.HttpWarmer"
  max_iterations = 1000
  timeout_millis = 5000
  params = {
    method = "post"
    urls = [ "http://localhost:8080/foo/bar", "http://localhost:8080/bar/baz" ]
    body = "{ 'foo': 'bar' }"
    headers = {
      Content-Type = "application/json"
    }
  }
}
```

### Development

Clone the repository:
```bash
$ git clone https://github.com/salesforce/centrifuge.git
```

Compile like this:
```bash
$ cd centrifuge/
$ ./bang.sh
```
