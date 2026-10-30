## Seperation Of Concerns

- Each component should handel a different part of overall problem
- Example
1. server handles a network communication
2. parser handles http syntax
3. Router decides which handler should pass the request

## Encapsulation

- keep the internal implementation and data of a component hidden. expose only what other components need
- the components should access only using public functions

## Single Responsiblity Principle

it means a class should have one primary responsiblity

## Architecture

- Server -> Parser -> Request Model -> Router -> Response Model

1. Server
- manage connections and network communications
what it should do ?
- start server on specified port 
- listen for incomming request
- accept clients connection
- Read incomming bytes 
- pass recieved data to parser
- send response bytes to client 
- close connections and manage back to clients 

2. Parser
Validate HTTP Syntax
- parsing the request line
- parsing headers
- identifying the headers
- Extract the body
- Validating the HTTP syntax
- Detecting malformed request
- reports the parsing errors

3. Request Model
- store information extracted by parser
- HTTP methods
- HTTP version
- HTTP target path
- Headers
- Request Body
- Access methods for retriving the information

4. Router 
- Match the incomming request to appropriate handeler
- Route Regestration
- Matching HTTP Methods
- Matching Http request path
- Selecting appropriate handler
- Handeling unmatched routes

5. Response Model
- Represent the HTTP response that needs to sent to client 
- HTTP verion
- Status code
- Headers 
- Response Body

## complete Flow

Server
Accepts the client connection and reads incoming bytes.

Parser
Interprets the bytes, validates HTTP syntax, and extracts the request information.

Request Model
Stores method = GET, path = /users, version = HTTP/1.1, and headers.

Router
Matches GET + /users and selects GetUsersHandler.

Handler
Executes the application logic and produces an HTTP response.

Response Model
Stores status code, headers, and response body.

Server
Sends the serialized response to the client.

## Server Lifecycle

1. Startup - Initialize the Server
2. Accepting - Accept the connections
3. Processing - recieve request and send response
4. Shutdown - Stop accepting connections and close resources

## Resource OwnerShip

- it is something that your program must eventually release
In HTTP server
release means close() and shutdown()
- it means after accepting the client and completing request we have to close the allocated request use try block to AutoClose

