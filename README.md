# Robots vs Dinosaurs [![CircleCI](https://circleci.com/gh/gabrielgiordan/robots-vs-dinosaurs.svg?style=svg&circle-token=954c8aa4101100fc9fb48f3729736e23fcc8b2cd)](https://circleci.com/gh/gabrielgiordan/robots-vs-dinosaurs) [![codecov](https://codecov.io/gh/gabrielgiordan/robots-vs-dinosaurs/branch/master/graph/badge.svg?token=qRIPgTLw1S)](https://codecov.io/gh/gabrielgiordan/robots-vs-dinosaurs)

Clojure/ClojureScript apps to support simulations on an army of remote-controlled robots that fight dinosaurs!

[Click here to view live on Heroku](https://robots-vs-dinosaurs.herokuapp.com/api/simulations)

## Contents
- [Overview](#overview)
	- [Architecture](#architecture)
		- [Ports](#ports)
		- [Adapters](#adapters)
		- [Controllers](#controllers)
		- [Logic](#logic)
- [Development](#development)
- [Deployment](#deployment)
- [Examples](#examples)
	- [2xx](#examples-2xx)
	- [4xx](#examples-4xx)

<a name="overview"></a>
## Overview 

The **Robots vs Dinosaurs** project is separated into two apps: the **Clojure** server-side app and the extra **ClojureScript** client-side app.

---

<a name="architecture"></a>
### Architecture 

The **API** is written in Clojure based on the **Alistair Cockburn's Hexagonal Architecture** with the **Stuart Sierra's Component Model** for managing the life-cycle of components which have run-time state, also can be seen as the dependency injection for immutable data structures.

<img src="doc/hexagonal-architecture.png" width="600" />

<a name="ports"></a>
#### Ports 
Each **port** has a corresponding **Component** on the **System Map**:

- **Storage**
	- Which is a protocol to access a single **atom** to share and persist data (also considered to use `refs` to make use of the `STM`, e.g. for a transaction which generates the `ID` and saves an entity)

- **Service**
	-  With **Pedestal** as the service provider.
	- **Swagger** integration available in `dev` profile at `/docs` and Open API spec at `/open-api.json`.

- **Router** 
	- With **Reitit** as the alternative routing engine, which is faster than the Pedestal's default.

	- **Interceptors** 
		- Custom redirect for trailing slashes `/`.
		- Clojure's **spec alpha** request/response coercion.
		- Error logging using **Logback** with custom response.
	- **Supported Formats**
		- JSON
		- Transit (both JSON and MessagePack)
		- EDN (used in Clojure and Datomic)
		
- **Server**
	- With **Jetty** as the default http server which listens to `PORT` environment variable (useful for Heroku) or `4000` as the default port binding.

<a name="adapters"></a>
#### Adapters 
Used to wire the external spec to the internal spec.

<a name="controllers"></a>
#### Controllers 
The bridge between **ports**, **adapters** and **logic** layers.

<a name="logic"></a>
#### Logic 
The logic is the largest part of the application and is made of the below business entities with Clojure's **Spec** to domain modelling coercion: 

- **Simulation** has `id` `title` `scoreboard` and `board`.

- **Scoreboard** has `total`
	
- **Board** has `size` and `units`.

- **Unit**
	
	- **Robot** has `id` `point` and `direction`.
				
	- **Dinosaur** has `id` `subtype` and `point`.
	
- **Size** has `width` and `height`.
	
- **Point** has `x` and `y`

- **Direction** has `orientation` and `point`

**Notes:** How to deal with state is a good topic of discussion, the decision to use Records over maps is because of its performance (as read in **Joy of Clojure**), but other state maintaining methods could also be used, e.g. [Managing State in Clojure](https://github.com/oubiwann/maintaining-state-in-clojure).

---

<a name="development"></a>
### Development
The development workflow is based on the **Stuart Sierra's** [Clojure Reloaded Workflow](http://thinkrelevance.com/blog/2013/06/04/clojure-workflow-reloaded) to build the system while simultaneously interacting with it using **REPL**.

#### REPL
Start **REPL** with the `dev` profile:

```posh
lein repl +dev
```

Reload the **System**:

```clj
user=> (user/reset)
```

#### Aliases
Run with `dev` profile:

```clj
lein run-dev
``` 

---

<a name="deployment"></a>
### Deployment 

#### Uberjar

Compile the `uberjar`:

```posh
lein uberjar
```

Run with optional `port`:

```posh
java -Dport=4000 -jar target\uberjar\robots-vs-dinosaurs-standalone.jar
```

#### Docker

To build and run the **Docker** container with the **uberjar** execute:

```posh
docker build -t <image_tag> . && docker run --env PORT=4000 --name robots-vs-dinosaurs <image_tag>
```

#### Heroku

The project has a `Procfile` file for Heroku deployment which can be executed with **Heroku Git** or **Heroku GitHub** integration which supports automatic deployment on a branch of choice.

---

<a name="examples"></a>
### Examples

<a name="examples-2xx"></a>
#### 2xx

`curl -X GET localhost:4000/api/simulations`

`200 Content-Type: application/json`

<img src="doc/img/get-simulations-response.png?raw=true" width=380 />


`curl -X DELETE localhost:4000/api/simulations/16`

`200 Content-Type: application/json`

<img src="doc/img/delete-simulation-response.png?raw=true" width=180 />


`curl -X GET localhost:4000/api/simulations/16/as-game`

`200 Content-Type: text/plain`

<img src="doc/img/get-simulation-as-game-response.png?raw=true" width=120 />


`curl -X POST --header 'Content-Type: application/json' -d '{"point": {"x": 5, "y": 5}, "orientation": "right"}' localhost:4000/api/simulations/16/robots`

`201 Content-Type: application/json`

<img src="doc/img/new-robot-response.png?raw=true" width=250 />


`curl -X GET localhost:4000/api/simulations/16/robots/17/attack`

`200 Content-Type: application/json`

<img src="doc/img/robot-attack-response.png?raw=true" width=250 />


`curl -X GET --header 'Accept: application/transit+json' localhost:4000/api/simulations/0/robots/2/turn-right`

`200 Content-Type: application/transit+json`

<img src="doc/img/robot-attack-transit-json-response.png?raw=true" height=100 />


<a name="examples-4xx"></a>
#### 4xx

`curl -X GET http://localhost:4000/api/simulations/93`

`404 Content-Type: application/json`

<img src="doc/img/error-0-response.png?raw=true" height=200 />


`curl -X GET http://localhost:4000/api/simulations/string`

`400 Content-Type: application/json`

<img src="doc/img/error-1-response.png?raw=true" height=200 />


`curl -X GET --header http://localhost:4000/api/simulations/16/robots/53/move-forward`

`403 Content-Type: application/json`

<img src="doc/img/error-10-response.png?raw=true" height=200 />


`403 Content-Type: application/json`

`curl -X GET http://localhost:4000/api/simulations/16/robots/63/attack`

<img src="doc/img/error-11-response.png?raw=true" height=200 />


`403 Content-Type: application/edn`

`curl -X GET --header 'Accept: application/edn' http://localhost:4000/api/simulations/16/robots/63/attack`

<img src="doc/img/error-11-edn-response.png?raw=true" height=115 />


`403 Content-Type: application/json`

`curl -X GET http://localhost:4000/api/simulations/16/robots/53/move-backward`

<img src="doc/img/error-13-response.png?raw=true" height=200 />

---


## License

Copyright © 2019
