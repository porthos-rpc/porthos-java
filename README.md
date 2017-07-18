# Porthos

A RPC over AMQP library for Java (1.8 or higher).

## Goal

Provide a language-agnostic RPC library to write distributed systems.

## Client

```java
try (Client c = new Client("amqp://guest:guest@broker:5672/myVHost", "SampleService")) {
	Response r = c.call('method').withJSON("[1,2,3]").sync();

	if (r.getStatusCode() == Status.OK) {
		System.out.Println(r.getContent());
	}
}
```

## Server

Not implemented yet.

## Contributing

Pull requests are very much welcomed. Make sure a test or example is included that covers your change.

Docker is being used for the local environment. To build/run/test your code you can bash into the server container:

```sh
$ docker-compose run lib ash
root@porthos:/usr/src/app# mvn test
```
