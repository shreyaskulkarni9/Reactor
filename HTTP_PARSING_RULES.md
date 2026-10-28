# HTTP Request Parsing Rules

## 1. TCP Input Handling
- TCP is a continuous byte stream, not a message-based protocol.
- A single read may return part of a request, one complete request, or bytes belonging to multiple requests.
- Maintain a persistent input buffer and preserve unread bytes between requests.
- Never assume one `read()` call equals one complete request.

## 2. Header Parsing
- Accumulate incoming bytes until the header terminator `\r\n\r\n` is found.
- The terminator may be split across multiple reads.
- Parse the request line and headers only after the complete header section is available.
- Enforce a maximum header size and reject malformed or incomplete headers when appropriate.
- Treat header names case-insensitively.

## 3. Determining the Request Body
- If `Transfer-Encoding: chunked` is present, decode chunks according to HTTP/1.1 framing rules.
- Otherwise, if a valid `Content-Length` is present, read exactly that many body bytes.
- If neither is present, treat the request body as empty.
- Reject requests containing both `Transfer-Encoding` and `Content-Length` in this initial implementation.
- Reject invalid lengths, malformed chunk framing, and bodies exceeding configured limits.

## 4. Handling Incomplete Requests
- If the header or body is incomplete, read more bytes and continue parsing.
- Do not dispatch a request until its complete framing has been received.
- Handle EOF before completion as an incomplete request.
- Handle socket errors without treating partial data as a complete request.

## 5. Multiple Requests Per Connection
- After completing one request, retain any remaining buffered bytes.
- Parse the next request from those remaining bytes.
- Support persistent HTTP/1.1 connections when appropriate.
- Respect connection-closing semantics and close the socket when required.

## 6. Parsing Loop
1. Accumulate bytes until the headers are complete.
2. Parse and validate the request line and headers.
3. Determine the body framing method.
4. Accumulate or decode the complete body.
5. Dispatch the complete request.
6. Consume exactly the bytes belonging to that request.
7. Continue parsing remaining buffered bytes if the connection stays open.

## 7. Validation Tests
- Request headers split across multiple reads.
- Header terminator split across reads.
- Body split across multiple reads.
- Two requests received in one read.
- One request followed by a partial second request.
- `Content-Length: 0`.
- Chunked bodies with multiple chunks and a terminating zero-size chunk.
- Malformed headers, invalid lengths, premature EOF, and oversized requests.
