## Welcome to OZorkAuth

The purpose of this project is to learn a little about OAuth2 flows and have fun doing it.

Following the instructions below, you'll be able to play the classic text-based adventure,
[Zork I](https://en.wikipedia.org/wiki/Zork) by interacting with an API via OAuth2.

This is a Spring Boot app powered by the [Stormpath](http://stormpath.com) intgeration which provides out-of-the-box
authentication and authorization through its SaaS platform, including OAuth2.

*Full Disclosure*: I work for Stormpath.

A live hosted version of this project can be found [here](https://ozorkauth.herokuapp.com/v1/instructions).

Or, you can deploy this app to your own Heroku account by clicking the purple button:

[![Deploy](https://www.herokucdn.com/deploy/button.png)](https://heroku.com/deploy)


## A Word on OAuth2

The [OAuth2](https://tools.ietf.org/html/rfc6749) specification is very broad. This example focuses
on just two of the workflows: `grant_type=password` and `grant_type=refresh_token`

The `grant_type=password` flow is an [*authorization grant*](https://tools.ietf.org/html/rfc6749#section-1.3.3) used
to return an access token (and possibly a refresh token) when a valid username and password are presented. This
alleviates the need to pass username and password on every request. Once you have an access token, you can present
this token to access protected resources.

The `grant_type=refresh_token` flow is used to exchange a refresh token for a new access token. Typically, an access
token will be short lived and a refresh token will be longer lived. When the access token expires, the next request
attempt using the access token will result in Bad Request (400) response. As long as the refresh token has not expired,
it can be used to obtain a new access token. Once the refresh token is expired, the user must login once again and get
a new access token and refresh token pair.

## How to play Zork, OAuth Style

There are 4 steps to be able to play the game:

1. Register for an account (this has nothing to do with OAuth2)
2. Get an access token and a refresh token (this is the `grant_type=password` flow)
3. Use the access token to interact with the game
4. When the access token expires, use the refresh token to get a new one (this is the `grant_type=refresh_token`)

*Note*: All the examples below use [httpie](https://github.com/jkbrzt/httpie). On Mac, this can be installed with:

```
brew install httpie
```

### Register for an Account

The OZorkAuth API exposes an endpoint for registration: `/v1/r`. Behind the scenes, an account will be created using the
Stormpath API.

Here's an example of the registration command for the sample app running on Heroku:

```
http POST \
  https://ozorkauth.herokuapp.com/v1/r \
  givenName=Bob surName=Smith email=bob@smith.com password=Password 
```

*Note*: Of course the above data, including the password is a made-up example. You should create your account using your own real information, including a strong password.

You get a response that looks like this:

```
{
    "response": [
        "Thank you for registering!",
        "Bob Smith",
        "bob@smith.com"
    ],
    "status": "SUCCESS"
}
```

### Get an Access Aoken and a Refresh Token

The OZorkAuth API exposes an endpoint for authorization: `/v1/a`. Behind the scenes, Stormpath verifies that the
username and password are valid and, if so, returns an access token and a refresh token.

Here's an example of the authorization command for the sample app running on Heroku:

```
http -f POST \
  https://ozorkauth.herokuapp.com/v1/a \
  Origin:https://ozorkauth.herokuapp.com \
  grant_type=password username=bob@smith.com password=Password
```

*Note*: The `-f` (form submission) parameter to `httpie` above is critical as it ensures that the `Content-Type` header
is set to: `application/x-www-form-urlencoded`, a requirement of the `grant_type=password` flow per the
[specification](https://tools.ietf.org/html/rfc6749#section-4.3.2).

You get a response that looks like this:

```
{
    "access_token": "eyJraWQiOiJSOTJTQkhKQzFVNERBSU1HUTNNSE9HVk1YIiwiYWxnIjoiSFMyNTYifQ...",
    "expires_in": 3600,
    "refresh_token": "eyJraWQiOiJSOTJTQkhKQzFVNERBSU1HUTNNSE9HVk1YIiwiYWxnIjoiSFMyNTYifQ...",
    "token_type": "Bearer"
}
```

*Note*: The `access_token` and `refresh_token` values above are truncated for brevity.

Save the values of the `access_token` and `refresh_token` for use in the following steps.

### The Fun Stuff: Use the Access Token to Interact with the Game

The OZorkAuth API exposes an endpoint for sending commands to the game: `/v1/c`. Behind the scenes, this
endpoint is secured using the Stormpath Spring Security Spring Boot WebMVC integrations. If the request
is not properly authenticated, it will be rejected.

You use the access token in the `Authorization` header, which the Stormpath Spring Security integration
looks up to verify it as valid.

Here's an example of the game command for the sample app running on Heroku:

```
http POST \
  https://ozorkauth.herokuapp.com/v1/c \
  Authorization:"Bearer eyJraWQiOiJSOTJTQkhKQzFVNERBSU1HUTNNSE9HVk1YIiwiYWxnIjoiSFMyNTYifQ..."
```

Without any other parameters, the response will be the result of looking at your surroundings in the game:

```
{
    "gameInfo": [
        "ZORK I: The Great Underground Empire",
        "Copyright (c) 1981, 1982, 1983 Infocom, Inc. All rights reserved.",
        "ZORK is a registered trademark of Infocom, Inc.",
        "Revision 88 / Serial number 840726"
    ],
    "look": [
        "West of House",
        "You are standing in an open field west of a white house, with a boarded front door.",
        "There is a small mailbox here."
    ],
    "status": "SUCCESS"
}
```

To issue a command in the game, use the above and include the `request=` parameter:

```
http POST \
  https://ozorkauth.herokuapp.com/v1/c \
  Authorization:"Bearer eyJraWQiOiJSOTJTQkhKQzFVNERBSU1HUTNNSE9HVk1YIiwiYWxnIjoiSFMyNTYifQ..." \
  request="open mailbox"
```

The response will look like this:

```
{
    "gameInfo": [
        "ZORK I: The Great Underground Empire",
        "Copyright (c) 1981, 1982, 1983 Infocom, Inc. All rights reserved.",
        "ZORK is a registered trademark of Infocom, Inc.",
        "Revision 88 / Serial number 840726"
    ],
    "look": [
        "West of House",
        "You are standing in an open field west of a white house, with a boarded front door.",
        "There is a small mailbox here."
    ],
    "request": "open mailbox",
    "response": [
        "Opening the small mailbox reveals a leaflet."
    ],
    "status": "SUCCESS"
}
```

*Note*: On each successful request, the game state is automatically saved as [Custom Data](https://docs.stormpath.com/rest/product-guide/latest/accnt_mgmt.html#how-to-store-additional-user-information-as-custom-data) to the Stormpath Account the `access_token` is associated with.

You can keep executing commands in this way until the `access_token` expires.

### Use the Refresh Token to Get a New Access Token

Once again use the authorization endpoint: `/v1/a`

Only this time, we use the `grant_type=refresh_token` OAuth2 flow.

Here's an example of the authorization command for the sample app running on Heroku:

```
http -f POST \
  https://ozorkauth.herokuapp.com/v1/a \
  Origin:https://ozorkauth.herokuapp.com \
  grant_type=refresh_token refresh_token=eyJraWQiOiJSOTJTQkhKQzFVNERBSU1HUTNNSE9HVk1YIiwiYWxnIjoiSFMyNTYifQ...
```

You will get a similar response as before when using the `grant_type=password` flow:

```
{
    "access_token": "eyJraWQiOiJSOTJTQkhKQzFVNERBSU1HUTNNSE9HVk1YIiwiYWxnIjoiSFMyNTYifQ...",
    "expires_in": 3600,
    "refresh_token": "eyJraWQiOiJSOTJTQkhKQzFVNERBSU1HUTNNSE9HVk1YIiwiYWxnIjoiSFMyNTYifQ...",
    "token_type": "Bearer"
}
```

*Note*: The `refresh_token` returned in the response will be the same while the `access_token` will be new.

## Acknowledgements & More Info

The Zork I game play is accomplished using the [Zax](https://github.com/mattkimmel/zax) Java based [Z-Machine](https://en.wikipedia.org/wiki/Z-machine) by Matt Kimmel.

The only change to the original source I made was to add a monitor to make the controller thread wait until the Zax thread has finished writing the game save file.
This is needed as Zax is intended to be used synchronously and the HTTP based API is inherently asynchronous.

For more information on Stormpath and the supported languages and integrations, checkout out the [docs](https://docs.stormpath.com).