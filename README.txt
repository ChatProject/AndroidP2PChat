This is a peer to peer chat application.

It connects to a webserver to update information about the user and get information about friends.

The peers currently communicate by sending and receiving instances of The Data class using
ObjectInputStream and ObjectOutputStream classes.

The friends list is received from the webserver in the form on JSON.

There is currently no user authentication.

All users that wish to chat must sign in and wait for their peers to sign in before starting a chat
so the database will have up to date information about user ip addresses.
