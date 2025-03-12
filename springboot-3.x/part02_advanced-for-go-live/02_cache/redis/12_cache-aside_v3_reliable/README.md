

1. Region: CRUD
2. Create:
    - insert to db
    - update to cache
    - retry
3. Retrieve
    - get from cache
    - if cache empty, get from db, update cache (Cache Aside) with retry
4. Flush Cache
