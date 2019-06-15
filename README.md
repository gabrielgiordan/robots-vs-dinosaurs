# Welcome to Robots vs Dinosaurs!

Clojure/ClojureScript applications to support simulations on an army of remote-controlled robots that fight dinosaurs!

## Contents
- [Overview](#overview)
	- [Architecture](#architecture)
		- [Ports](#ports)
		- [Adapters](#adapters)
		- [Controllers](#controllers)
		- [Logic](#logic)
- [Development](#development)
- [Deploy](#deploy)
- [Curl](#curl)


## Overview <a name="overview"></a>

The **Robots vs Dinosaurs** project is separated into two apps: the server-side app in Clojure and the extra client-side app in ClojureScript.

### Architecture <a name="architecture"></a>

The **API** is written in Clojure based on the **Alistair Cockburn's Hexagonal Architecture** with the **Stuart Sierra's Component Model** for managing the life-cycle of components which have run-time state, that can be seen as the dependency injection for immutable data structures.

#### Ports <a name="ports"></a>
Each **port** has a corresponding **Component** on the **System Map**:

- **Storage**
	- Which is a protocol to access a single **atom** to share and persist data (also considered to use `refs` to make use of the `STM`, e.g. for a transaction which generates the `ID` and saves an entity)
	
- **Routes**
	- To get the service routes mapping.

- **Service**
	-  With **Pedestal** as the service provider.

- **Router** 
	- With **Reitit** as the alternative routing engine, which is faster than the Pedestal's default.

	- **Interceptors** 
		- Custom redirect for trailing slashes `/`.
		- Clojure's **spec alpha** request/response coercion.
		- **Swagger** support, in `dev` mode.
		- Error logging using **Logback** with custom response.
	
- **Server**
	- With **Jetty** as the default http server which listens to `PORT` environment variable (useful for Heroku) or `8080` as the default port binding.

#### Adapters <a name="adapters"></a>
Used to wire the external spec to the internal spec.

#### Controllers <a name="controllers"></a>
The bridge between **ports**, **adapters** and **logic** layers.

#### Logic <a name="logic"></a>
The logic is the largest part of the application and is made of the below business entities with Clojure's **Spec** to domain modelling coercion: 

- **Simulation**
	- Has `id` `title` `scoreboard` and `board`.

- **Scoreboard**
	- Has `total`
	
- **Board**
	- Has `size` and `units`.

- **Unit**
	
	- **Robot**
		- Has `id` `point` `direction`.
				
	- **Dinosaur**
		- Has `id` `point`.
	
- **Size**
	- Has `width` and `height`.
	
- **Point**
	- Has `x` and `y`

- **Direction**
	- Has `orientation` and `point`

> How to deal with state is a good topic of thinking, the decision to use Records instead of maps is because of the performance of it (as read in **Joy of Clojure**), but other methods could also be used, e.g. the ones in this repository: [Managing State in Clojure](https://github.com/oubiwann/maintaining-state-in-clojure).

### Development <a name="development"></a>
The development workflow is based on the **Stuart Sierra's** [Clojure Reloaded Workflow](http://thinkrelevance.com/blog/2013/06/04/clojure-workflow-reloaded) to build the system while simultaneously interacting with it using **REPL**.

#### REPL
Start **REPL** with the `dev` profile:
`lein repl +dev`

Reload the **System**:
`user=> (user/reset)`

#### Aliases
Run with `dev` profile:
`lein run-dev` 

### Deploy <a name="deploy"></a>

#### Uberjar

Compile the `uberjar`:
`lein uberjar`

Run with optional `port`:
`java -Dport=4000 -jar target\uberjar\robots-vs-dinosaurs-standalone.jar`

#### Docker

To build and run the **Docker** container with the **Dockerfile** with the **uberjar** execute:
`docker build -t <image_tag> . && docker run --env PORT=4000 --name robots-vs-dinosaurs <image_tag> `

#### Heroku

The project has a `Procfile` file for Heroku deployment which can be executed with **Heroku Git** or **Heroku GitHub** integration which support automatic deployment on a specific branch.

### Curl e.g. <a name="curl"></a>

#### Simulations

`curl -X GET localhost:4000/api/simulations`

`curl -X GET localhost:4000/api/simulations/16`

```
curl -X POST --header 'Content-Type: application/json' -d 
'{ \  
	"title": "Aerodynamic Chinchilla", \ 
	"size": { \ 
		"width": 15, \ 
		"height": 15 \ 
   } \
 }' localhost:4000/api/simulations
 ```
 
`curl -X DELETE localhost:4000/api/simulations/16`

`curl -X GET localhost:4000/api/simulations/16/as-game`

#### Robots

`curl -X GET localhost:4000/api/simulations/16/robots`
```
curl -X POST --header 'Content-Type: application/json' -d 
'{ \ 
   "point": { \ 
     "x": 5, \ 
     "y": 5 \ 
   }, \ 
   "orientation": "right" \ 
 }' localhost:4000/api/simulations/16/robots
```
`curl -X GET localhost:4000/api/simulations/16/robots/17`
`curl -X GET localhost:4000/api/simulations/16/robots/17/turn-left`
`curl -X GET localhost:4000/api/simulations/16/robots/17/turn-right`
`curl -X GET localhost:4000/api/simulations/16/robots/17/move-forward`
`curl -X GET localhost:4000/api/simulations/16/robots/17/move-backward`
`curl -X GET localhost:4000/api/simulations/16/robots/17/attack`

#### Dinosaurs
`curl -X GET localhost:4000/api/simulations/16/dinosaurs`
```
curl -X POST --header 'Content-Type: application/json' -d 
'{ \ 
   "point": { \ 
     "x": 4, \ 
     "y": 4 \ 
   } \ 
 }' localhost:4000/api/simulations/16/dinosaurs
```
`curl -X GET localhost:4000/api/simulations/16/dinosaurs/20`

## License

Copyright © 2019

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
