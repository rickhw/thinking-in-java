
1. shutdown cache
    1. retry to get the value from cache
    2. if failed, read from db
    - fail
2. shutdown cache, then restart the cache again
    - expected: the operation is successful.
    - pass