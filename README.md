# Centrifuge Warmup Engine

Centrifuge is a framework for scheduling and running startup and warmup tasks; mostly used for JVM warmup.  
It provides an interface for implementing warmup logic (e.g., calling an HTTP endpoint, or populating cache, or 
pre-compilation for generated code). Centrifuge is responsible for thread management and execution of  warmup code.  

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
     * is thrown, centrifuge yields, and then retries calling init, until maximum
     * number of failures allowed is reached.
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

Register warmers either programmatically like this:

```java
public static void main(final String[] args) throws Exception {

    // warmer config contains configurations used to execute a warmer
    final WarmerConfig warmerConfig = new WarmerConfig();
    warmerConfig.setWarmerClass(EchoWarmer.class);
    warmerConfig.setMaxIterations(10);
    warmerConfig.setMaxFailure(3);
    warmerConfig.setTimeoutMillis(1000);
    warmerConfig.setRequired(true);
    final Map<String, Object> params = new HashMap<>();
    params.put("text", "echo is a sample warmer...");
    warmerConfig.setParams(params);

    final CentrifugeConfig centrifugeConfig = new CentrifugeConfig();
    // add the warmer config from ^^ to centrifuge config
    centrifugeConfig.addWarmerConfig(warmerConfig);

    // get a new instance and start the engine
    final Centrifuge centrifuge = Centrifuge.newInstance(centrifugeConfig);
    centrifuge.start();

    // check whether all *required* warmers are stopped (either completed successfully or reached maximum failure)
    // note that *non-required* warmers may still be running and will continue to run
    while (!centrifuge.isWarm()) {
        // wait until is warm, then ping up to be put into rotation
    }
}
```

Or descriptively by adding configurations similar to what is shown below to register warmers:

```java
// configuration for warmup engine
centrifuge {

  warmers = [
    {
      // warmer class name (required)
      class = "com.salesforce.centrifuge.EchoWarmer"  // echo warmer is a simple example warmer
      
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

Then initialize, start, and check for status:

```java
public static void main(final String[] args) throws Exception {

    // config for engine is loaded from the path given
    final CentrifugeConfig centrifugeConfig = new CentrifugeConfig("centrifuge.conf");

    // create an instance and start
    final Centrifuge centrifuge = Centrifuge.newInstance(centrifugeConfig);
    centrifuge.start();
    
    // ...
    
    // check whether all *required* warmers have successfully completed
    // note that *non-required* warmers may still be running and will continue to run
    while (!centrifuge.isWarm()) {
        // wait until is warm, then ping up to be put into rotation
    }
}
```

#### HTTP Warmer

Include the following maven dependency to pull this warmer into your project:

```xml
<dependency>
    <groupId>com.salesforce.centrifuge</groupId>
    <artifactId>centrifuge-warmers</artifactId>
    <version>${centrifuge.version}</version>
</dependency>
```

The HTTP Warmer can be used as a very simple warmer to call HTTP endpoints in order to trigger code path exercised by the
resource implementing the endpoint.  For example, an application may provide a homepage URL that when called would do
basic initializations, connect to a database and cache, etc.  These code paths can be warmed (JITed) by simply using 
the HTTP warmer to hit the homepage endpoint for some relatively large number of iterations.

Here is an example of how to use the HTTP warmer:

```java
{
  class = "com.salesforce.centrifuge.warmers.HttpWarmer"
  max_iterations = 1000  // make maximum of 1000 http calls
  timeout_millis = 30000  // kill after 30 seconds
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
