A separate service has to be implemented to collect all user attributes values and all device state data with their respectively UUID value to map them later with proper attribute (to retrieve a value of a attribute) or device (to retrieve a state of a device) where the changed are updated every x second (this should not be store locally as subject to change at any moment) 

API used for both attributes values and devices statuses are: 
https://my.zipato.com/zipato-web/v2/attributes/values to get all attribute values and https://my.zipato.com/zipato-web/v2/devices/statuses to get all device state. 

We pull data to the server at every x second in order to keep the app up to date and this process can consume a lot of Mobile data depending on the user set up (for example if the user have a lot of devices paired with his account). In order to make it more efficient we get and save the eTag from every call request ()then set it back to the new request header so that the server will only return us updates (only changes from last request) instead of pulling all the time every attributes values/devices statuses. In this way we managed to use the network more efficiently. 

> __Ps__: eTag is only going to work with attributes values (https://my.zipato.com/zipato-web/v2/attributes/values) API and     > devices statuses API (https://my.zipato.com/zipato-web/v2/devices/statuses)*. 
 
