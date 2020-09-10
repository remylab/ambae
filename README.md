# Start the app

mvn package -DskipTests  
sh start-dev.sh

For H2 browse : http://localhost:9000/h2  

JDBC URL = `jdbc:h2:mem:testdb` / user,pwd = `sa`

# Endpoints

## Create reservation
```
curl -X POST 'http://localhost:9000/reservations' \
--header 'Content-Type: application/json' \
--data-raw '{
    "email": "remy@mail.com",
    "firstName": "remy",
    "lastName": "douda",
    "startDate": "2020-09-22",
    "endDate": "2020-09-24"
}'
```

## Get reservation
```
curl http://localhost:9000/reservations/1
```

## Get availability
```
curl http://localhost:9000/availability
curl http://localhost:9000/availability/2020-09-10/2020-09-30
```

## Delete reservation
```
curl -X DELETE http://localhost:9000/reservations/1
```

## Update Reservation
```
curl -X PUT 'http://localhost:9000/reservations/1' \
--header 'Content-Type: application/json' \
--data-raw '{
    "email": "new-remy@mail.com",
    "firstName": "new-remy",
    "lastName": "new-douda",
    "startDate": "2020-09-12",
    "endDate": "2020-09-14"
}'
```
