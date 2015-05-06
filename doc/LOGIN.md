
Before you start making any successful web service call, you need to be authenticated first with the server. We use the user authentication API to first get a nonce (user/init, nonce is need when generate encrypted password), 
then the username + token is sent to request an authentication with the server. 

Use our user authentication API for login.

Use this algorithm to calculate the token from nonce and password:

**token** = SHA1(**nonce** + SHA1(**password**))

where **SHA1** has hex encoded output like sha1sum command
