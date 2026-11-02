# HTTP Parser Error-Handling Policy

## 1. Protocol Support
- Support HTTP/1.1 request syntax.
- Reject unsupported HTTP versions.
- Do not interpret HTTP/2 traffic as HTTP/1.1.
- Recognize valid method tokens and distinguish unsupported methods from malformed syntax.

## 2. Request-Line Validation
- Require a method, request target, and HTTP version.
- Require exactly one space between the three request-line components.
- Reject empty components, invalid method tokens, malformed targets, and invalid version syntax.
- Enforce a configurable maximum request-line length.
- Do not silently repair malformed input.

## 3. Header Validation
- Require the header terminator CRLF CRLF.
- Reject malformed header lines, invalid header names, and prohibited control characters.
- Compare header names case-insensitively.
- Require exactly one valid Host field for HTTP/1.1 requests.
- Reject duplicate Host fields.
- Reject repeated Content-Length fields in the initial implementation.
- Reject invalid, negative, overflowing, or excessively large Content-Length values.
- Reject requests containing both Transfer-Encoding and Content-Length.
- Accept valid unknown headers and ignore them when not needed.
- Enforce configurable maximum header size and header count.

## 4. Body Validation
- Read exactly Content-Length bytes when Content-Length framing is used.
- Decode chunked bodies only when their framing is valid.
- Reject malformed chunk sizes, missing chunk delimiters, invalid trailers, and incomplete chunked bodies.
- Enforce a configurable maximum body size.
- Never dispatch a request until its body is complete.
- Treat a request with neither Content-Length nor Transfer-Encoding as having an empty body.

## 5. Disconnects and Timeouts
- Discard incomplete requests when EOF occurs before their framing is complete.
- Close the connection and release resources after socket I/O failures.
- Apply read timeouts to prevent clients from holding connections open indefinitely.
- Do not treat a timeout or premature EOF as successful request completion.
- Do not process incomplete buffered data as a separate valid request.

## 6. Error Responses
- Use 400 Bad Request for malformed request syntax or invalid framing.
- Use 408 Request Timeout when the server times out waiting for a request.
- Use 413 Content Too Large when configured request-size limits are exceeded.
- Use 414 URI Too Long when the request target exceeds its limit.
- Use 431 Request Header Fields Too Large when header limits are exceeded.
- Use 505 HTTP Version Not Supported for unsupported HTTP versions.
- Use 501 Not Implemented for recognized methods the server does not implement.
- Use 405 Method Not Allowed when a method is not allowed for a particular resource.
- Close the connection after malformed framing or ambiguous request boundaries.
- Keep error responses valid HTTP/1.1 responses with appropriate Content-Length headers.

## 7. Security and Resource Limits
- Reject ambiguous request framing rather than guessing.
- Prevent integer overflow when parsing lengths and chunk sizes.
- Enforce request-line, header, body, and timeout limits.
- Avoid logging sensitive request bodies or credentials.
- Ensure malformed requests cannot crash the server or corrupt parser state.

## 8. Minimum Test Cases
- Missing method, target, or HTTP version.
- Invalid and unsupported HTTP versions.
- Missing, malformed, and duplicate Host headers.
- Duplicate or invalid Content-Length.
- Both Transfer-Encoding and Content-Length present.
- Header terminator split across reads.
- Incomplete headers and premature body EOF.
- Invalid chunk sizes and missing chunk delimiters.
- Oversized request lines, headers, and bodies.
- Client disconnects and read timeouts.
- Valid unknown headers and unsupported methods.
- Two requests on one connection, including malformed first requests.
