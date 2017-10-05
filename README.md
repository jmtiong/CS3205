# CS3205

[![Join the chat: https://gitter.im/CS3205/CS3205Private](https://badges.gitter.im/CS3205/CS3205Private.svg)](https://gitter.im/CS3205/CS3205Private?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

**This project is to develop a security-focused component subsystem of a prototype web/internet based software system for health care, responsible for maintaining, inputting and displaying timestamped health records of various types.**

## CS3205 SubSystem 3
* This subsystem is on involving data collection from sensors.
* A smartphone based interface will be used to access the database, which provides for authentic generation of data, associated with a particular patient.

* Workflow/API:
Note that a) all paths can be prefixed with `/api`, e.g. `/api/oauth/token` or `/api/session`;
1. Send a POST request to `/oauth/token` or `/oauth/token` with a non-empty X-NFC-Token and a body with the form
```json
{
    "grant_type": "password",
    "username": "anything",
    "passhash": "anything"
}
```
the server will respond with a json with `token_type = Bearer` and `access_token=access.token.here`. this token is valid for 15 minutes. later we will implement sliding-sessions token to renew it

2. Authenticate yourself when sending `GET`/`POST` request to `/upload` endpoint by  
a. including the header `Authorization: Bearer access.Token.Here`  
b. including the header `X-NFC-Token: anything`

Use either `https://cs3205-3.comp.nus.edu.sg/session` or `https://cs3205-3.comp.nus.edu.sg/upload` (deprecated)  
2.1. GET `https://cs3205-3.comp.nus.edu.sg/session`: -> links (not used)

2.2. GET `https://cs3205-3.comp.nus.edu.sg/session/{type}` with {type} as a string that begins with either `step`, `heart`,  `image` or `video` (i.e. `/steps` or `/heartrate` also works)
-> Retrieve session

2.3. POST `https://cs3205-3.comp.nus.edu.sg/session/{type}?timestamp={timestamp}` **or ** `https://cs3205-3.comp.nus.edu.sg/session/{type}/{timestamp}` (deprecated)

Body: the content of the files (steps,image,video) or the heartrate (heart)
-> Upload data


## About this repository
This repository is on the web server of subsystem 3.

#### Site Map
Google Docs for consolidating documents:
* [Link](https://drive.google.com/drive/folders/0BwBeSTZ7ylMeUHNheHJrUE9PejA)

Repository for mobile application:
* [Link](https://github.com/yeejfe/CS3205)
