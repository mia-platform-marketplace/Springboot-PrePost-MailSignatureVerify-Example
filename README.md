# mia_template_service_name_placeholder



Welcome to Java Spring Boot example service for Mia-Platform!

## Summary

%CUSTOM_PLUGIN_SERVICE_DESCRIPTION%

This example exposes a Pre and Post Decorator that can be added to any Microservice on Mia-Platform.
## Local Development

This example applies a DSA signature to a message sent over a PreDecoratorRequest,
 and checks for its validity in the PostDecoratorRequest. If the signatures is not valid,
 it adds the field:
 ```json
{
  "authentic-email": "no"
}
```
to the response.

Moreover, if the *from* field is not set and the request headers do not contain a *userId*, the decorator chain will aborted.

More details about decorators can be found here [https://docs.mia-platform.eu/development_suite/api-console/api-design/decorators/]().

### Create the Email Service

From the Mia-Platform Marketplace on DevOps Console select this service and Create it. In this example give to the microservice the name **SendEmail** (you can use any name)

Once created go in *DevOps Console -> Design -> Microservices -> your just created microservice (SendEmail)* and add to it the following environment variables in the table *Environment variable configuration* (Key=Value)

```
SERVICE_NAME=api-gateway
SERVICE_PORT=8080
```

Press *Commit & Generate* to save the configuration.

### How to add a Post Hook:
The Post hook will be triggered after an API call. To create it go to *DevOps Console -> Design -> Pre/Post Microservices -> POST -> Add New*.

Use the following parameters:

- name: emailDecorator (but you can use whatever)
- protocol: http
- microservice: the microservice you have created at Step-1, **SendEmail**
- port: 80
- path: /send (is the path exposed by **SendEmail**)
- Require request body: true
- Require response body: true

> Note: *Require request body* and *Require response body* must be checked otherwise you obtain unexpected error `invalid protocol in post`.

### Deploy and Test

That's all! Now you can Commit and Deploy.

- Press *Commit & Generate* and save your configuration
- Go to *DevOps Console -> Deploy*, select your branch and environment e press deploy

In less that one minute all will be up and running!

To test it go to  *DevOps Console -> Documentation -> The Environment where you deployed the configuration. This will open the API Portal.

Your API calls to the Email Service will now be authenticated.

### Run locally

To run locally this example just run the

```bash
mvn spring-boot:run
```

To change server port

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8000
```

To launch tests locally

```bash
mvn test
```

To build it

```bash
mvn clean package
```

To force mvn package update

```bash
mvn clean install -U
```

### Routes

The following routes are exposed

- [http://localhost:3000/pre/signemail]() - applies signature
- [http://localhost:3000/post/checksignature]() - verifies the signature
- [http://localhost:3000/-/ready]() - the service is ready (used by k8s)
- [http://localhost:3000/-/healthz]() - the service is healthy (used by k8s)
- [http://localhost:3000/documentation/json]() - the Open API 3 specification
