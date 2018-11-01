# Centrifuge Warmup Engine

Centrifuge is a framework for scheduling and running startup and warmup tasks.
It is focused mainly on accelerating JVM restarts, and provides an interface for 
implementing warmup tasks, such as:

- calling an HTTP endpoint
- populating caches 
- handling pre-compilation tasks for generated code

Centrifuge is responsible for executing these warmup tasks and managing threads.

### How to Use Centrifuge

First, include a Maven dependency for Centrifuge in your POM:

```xml
<dependency>
    <groupId>com.salesforce.centrifuge</groupId>
    <artifactId>centrifuge-core</artifactId>
    <version>${centrifuge.version}</version>
</dependency>
```

Now implement the `Warmer` interface for each of your warmup tasks.
Make sure your warmer class

- has an accessible default constructor
- does not swallow `InterruptedException`

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

You can register your warmers either programmatically, with code,
or descriptively, with a configuration file. To register a warmer programmatically,
write code like this:

```java
public static void main(final String[] args) throws Exception {

    // warmer config contains configurations used to execute a warmer
    final WarmerConfig warmerConfig = new WarmerConfig();
    warmerConfig.setWarmerClass(EchoWarmer.class);
    warmerConfig.setMaxIterations(10);
    warmerConfig.setMaxFailure(3);
    warmerConfig.setTimeoutMillis(1000);
    warmerConfig.setRequired(true);
    
    // parameters to be passed to warmer on initialization
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

Registering warmers descriptively within a configuration file lets you add and remove warmers without 
recompiling. To register a warmer descriptively, create a `centrifuge.conf` configuration file that looks
like this:

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

Then load the configuration file into Centrifuge like this:

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

We provide a simple HTTP warmer to call HTTP endpoints in order to trigger code path exercised by the
resource implementing the endpoint.

Include this Maven dependency to pull the HTTP warmer into your project:

```xml
<dependency>
    <groupId>com.salesforce.centrifuge</groupId>
    <artifactId>centrifuge-warmers</artifactId>
    <version>${centrifuge.version}</version>
</dependency>
```

Suppose an application provides a homepage URL that, when called, performs
basic initialization, connects to a database, populates caches, etc. The HTTP warming can warm these 
code paths simply by using the HTTP warmer to hit the homepage endpoint a sufficiently large number of times.
The following configuration file sets up the HTTP warmer to do this:

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

### Want to Contribute to Centrifuge?

Just clone the repository:
```bash
$ git clone https://github.com/salesforce/centrifuge.git
```

Then compile and test like this:
```bash
$ cd centrifuge/
$ ./bang.sh
```
