# Task
The attached image contains an application implementing the most straightforward TODO manager with CRUD operations. The image can be loaded via `docker load` and run using `docker run -p 8080:4242`.

The task includes two parts:

Firstly, it's required to write some tests for checking the functionality of the application. We don't provide strict specifications because the domain is simple enough. Therefore, it is necessary to come up with cases by yourself.

Secondly, it's necessary to check the performance of the `POST /todos` endpoint. It's not required to draw graphs. Measurements and some summary are enough.

Note that it can be useful to run the application with `VERBOSE=1` to see more logs (`docker run -e VERBOSE=1`).

## Endpoints
The only entity here is TODO  represented by a structure with the following three fields:
* `id` — an unsigned 64-bit identifier
* `text` - description of TODO
* `completed` - whether the todo is completed or not

### `GET /todos`

Get a JSON list of TODOs.

Available query parameters:
* `offset` — how many TODOs should be skipped
* `limit` - the maximum number of TODOs to be returned

### `POST /todos`

Create a new TODO. This endpoint expects the whole `TODO` structure as the request body.

### `PUT /todos/:id`

Update an existing TODO with the provided one.

### `DELETE /todos/:id`

Delete an existing TODO.

The endpoint requires the `Authorization` request header containing the `admin:admin` credentials in the `Basic` authorization schema.
