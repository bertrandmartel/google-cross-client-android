# Google Cross Client - Android 

The Android client part that authentify with remote server compliant with <a href="https://developers.google.com/identity/protocols/CrossClientAuth">google standards</a>

To be used with this Node JS server : https://github.com/akinaru/google-cross-client-node

## Features

* google local & remote signin
* google local & remote signout

## Setup

* generate `google-services.json` file from <a href="https://developers.google.com/identity/sign-in/android/start-integrating#prerequisites">here</a>
* make sure you set valid certificate sha1 you can take debug keystore sha1 for development (password : android) :

```
keytool -exportcert -list -v \
-alias androiddebugkey -keystore ~/.android/debug.keystore
```

* copy `google-services.json` to `app` directory
* create file `app/src/main/assets/config.properties` and append the following configuration, replace parameters with your own configuration :

```
server_hostname=192.168.1.131
server_port=4747
server_protocol=https
server_client_id=12345678910200-sdfkdflb133434ezd.apps.googleusercontent.com
```

* build the project

## License

The MIT License (MIT) Copyright (c) 2016 Bertrand Martel