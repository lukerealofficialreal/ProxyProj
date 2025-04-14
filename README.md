An implementation of the TFTP file transfer protocol with added support for TCP-style sliding windows. This implementation only supports sending 
and accepting read requests, not write requests.

Also included is both a client program and a proxy program. The client can make and send TFTP requests with a url to an image file (.png, .jpg, .jpeg). 
The proxy can accept these requests, download the requested image using HTTP, cache it for future requests, and send it to the client using TFTP.
The client can display all received images using JFrame.

Also included is a basic timer which will save the throughput in bits per second of each acknowledged data packet if enabled. 
