# Quarkus Redis Client

Forked from sourcesense/quarkus-redis, it's now updated to work with Quarkus 1.9.0.Final.

## Usage

Not so fancy, yet.

We need to inject Jedis provider in class:

```java
    @Inject
    Instance<Jedis> provider;
```

Once injected you can do this into your method:

```java
        Jedis jedis = provider.get();
        // retrieve data from Redis
        String cacheResult = jedis.get("key");
        
        if (cacheResult == null) {

           // calculate result
           ...
        
           // insert data into Redis
           jedis.set(key, result);

           return result;
           
        } else {
            return cacheResult;
            
## Next steps
Create annotations to simplify usage
        
