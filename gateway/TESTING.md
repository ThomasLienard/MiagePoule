# Gateway Testing

This file contains example `curl` commands to test the gateway running on `localhost:8080` 

Prerequisites
- Gateway running: `http://localhost:8080` (see `gateway/src/main/resources/application.properties`).
- Upstream microservices should be running on the ports configured in `application.properties` (or the gateway will return upstream connection errors).

Placeholders
- Replace `<TOKEN>` with a valid JWT (if required by your upstream services).
- Replace JSON bodies and `{ids}` with concrete values for your data.

Quick curl examples

Signup (create account)
```
curl -v -X POST http://localhost:8080/signup \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"pass"}'
```

Login
```
curl -v -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"pass"}'
```

Logout
```
curl -v -X POST http://localhost:8080/logout
```

Get personal data (requires token)
```
curl -v -X GET http://localhost:8080/account/personal-data \
  -H "Authorization: Bearer <TOKEN>"
```

Public endpoints
```
curl -v -X GET http://localhost:8080/public/events
curl -v -X GET http://localhost:8080/public/map
```

Agenda
```
curl -v -X GET http://localhost:8080/agenda -H "Authorization: Bearer <TOKEN>"
curl -v -X POST http://localhost:8080/agenda/add \
  -H "Content-Type: application/json" \
  -d '{"eventId":123,"time":"2025-12-01T10:00:00"}'
```

Championship / events
```
curl -v -X POST http://localhost:8080/championship/create \
  -H "Content-Type: application/json" \
  -d '{"name":"Champ1"}' -H "Authorization: Bearer <TOKEN>"

curl -v -X POST http://localhost:8080/championship/1/events/create \
  -H "Content-Type: application/json" \
  -d '{"name":"100m"}' -H "Authorization: Bearer <TOKEN>"

curl -v -X POST http://localhost:8080/championship/1/events/10/results/update \
  -H "Content-Type: application/json" \
  -d '{"results":[{"athleteId":5,"time":"00:10:23"}]}' \
  -H "Authorization: Bearer <TOKEN>"
```

Notifications / Reports
```
curl -v -X POST http://localhost:8080/events/10/notifications/create \
  -H "Content-Type: application/json" \
  -d '{"message":"Event starting"}' -H "Authorization: Bearer <TOKEN>"

curl -v -X GET http://localhost:8080/report -H "Authorization: Bearer <TOKEN>"
```

Crypto
```
curl -v -X POST http://localhost:8080/crypto/encrypt \
  -H "Content-Type: application/json" -d '{"data":"sensitive"}'

curl -v -X POST http://localhost:8080/crypto/decrypt \
  -H "Content-Type: application/json" -d '{"data":"<encrypted>"}'
```

Run the script
- Run all example calls non-interactively (POSIX shell):
```
bash gateway/test-calls.sh
```

Or to only print the commands without executing them:
```
DRY_RUN=1 bash gateway/test-calls.sh
```

If you want, I can add more focused calls (health checks, status assertions) or produce a PowerShell-friendly script.
