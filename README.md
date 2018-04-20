# How to use Ham DPC's

Password Cracker Server

## This includes the following dependnecy

* rabbitmq
* redis
* scalatra/sbt (other dependency in `build.sbt`)

## How to run server

1. `./build.sh` to build the image it basically compile a jar file and the dockerfile copy the jar
file into the container
2. `docker-compose up -d` to run the server

## How to run worker
1. `./build.sh` to build the image it basically compile a jar file and the dockerfile copy the jarfile into the container
2. `./run.sh <redis endpoint> <rabbit endpoint>`

## Explanation of How it run

* the scala webserver connect to rabbitmq and redis, it keep current offset and what password is currently cracking in redis where you can inspect the status
* rabbitmq keep a queue of job and job result
* when a job finish it publishes the result to the job queue
* the worker is subscribe to the global jobqueue
* the server subscribe to the result jobqueue
* when the server receive the result send add more job to the queue
* when a new task is receive seed the queue with 10 jobs
* you can send task or check task status through the server api (you can use the pwcracker-client.py for ease of use)

 