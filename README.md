# ThreadStacks

# use this command to run the services
# it build and runs the microservice with 2 instances of each of the main service
# to make calls, use localhost:8080 then the respective endpoint
docker-compose up --scale user-service=2 --scale thread-service=2 --scale room-service=2 --build