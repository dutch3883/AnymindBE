### Getting Start to be BTC Billionaire

1. start docker compose to initiate db-application using `docker-compose up` at project root 
    > this command will take sometime if you don't have mssql docker image
2. once db has start please wait until db service finish setting up and no more logs appear on the console
3. run scripts `./scripts/initialize_db.sh` at project root in another terminal to initialize db data.
4. run this command to start the application `sbt api run`
5. run this command to call service and add btc record
    ```
    curl --location --request POST 'http://localhost:8080/api/save-record' \
    --header 'Content-Type: application/json' \
    --data-raw '{
        "datetime": "2021-12-05T14:48:01+01:00",
        "amount": 20
    }'
    ```
6. run this command to query history of application (adjust datetime as you like)
    ```
    curl --location --request POST 'http://localhost:8080/api/history' \
    --header 'Content-Type: application/json' \
    --data-raw '{
        "startDatetime": "2020-10-05T10:48:01+00:00",
        "endDatetime": "2020-10-05T18:48:02+00:00"
    }'
    ```
   
---

#### Improvement Plan
1. Optimize DB Connection Pool
2. Optimize Thread management for I/O bound task and CPU bound task
3. Add index for datetime key 
4. Create buffer to make cache update operation async or make it an Actor

   