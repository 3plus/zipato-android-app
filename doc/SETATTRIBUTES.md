#Set attributes

When a user click on a list (in the right) the proper controller for that is display on the screen if exist of course or the list of its attributes + value is display as default. 

To load the controller for a type with first look at his template id, for example if templateid = "camera" we load the camera controller where the user will be able to stream a video on that particular camera. If template ID is missing we loop trough its attribute.definition.cluster to load the proper controller.  

__Example__ :

```JSON
{
    "entityType": "ENDPOINT",
    "link": "http://staging.zipato.com/zipato-web/v2/endpoints/71acb04d-ab8f-4e74-921a-3f5f13a7c08e",
    "name": "GEplus",
    "room": 14928,
    "uuid": "71acb04d-ab8f-4e74-921a-3f5f13a7c08e",
    "templateId": "",
    "endpointType": "actuator.onoff",
    "attributes": [
        {
            "link": "http://staging.zipato.com/zipato-web/v2/attributes/51545411-7dde-40b3-8669-a11705b4e451",
            "name": "STATE",
            "attributeId": 11,
            "attributeName": "state",
            "room": 14928,
            "uuid": "51545411-7dde-40b3-8669-a11705b4e451",
            "master": false
        }
    ],
    "show": true,
    "uiType": {
        "link": "http://staging.zipato.com/zipato-web/v2/types/system/OnOff+switch",
        "name": "OnOff switch",
        "endpointType": "actuator.onoff",
        "relativeUrl": "/actuator.onoff.png"
    }
} 
```
 
Above is a type (OnOff switch)  with attribute *uuid* 51545411-7dde-40b3-8669-a11705b4e451 and when retrieving that particular attribute on server we should have something like this in *json*: 

```JSON
{
    "uuid": "51545411-7dde-40b3-8669-a11705b4e451",
    "name": "STATE",
    "definition": {
        "id": 11,
        "attribute": "state",
        "attributeType": "BOOLEAN",
        "cluster": "com.zipato.cluster.OnOff",
        "readable": true,
        "reportable": true,
        "writable": true
    },
    "config": {
        "name": null,
        "master": false,
        "hidden": false,
        "reported": true,
        "expire": null,
        "compression": null,
        "unit": null,
        "enumValues": {
            "true": "On",
            "false": "Off"
        },
        "scale": 0,
        "precision": 0,
        "room": 14928
    },
    "attributeId": 11
}
``` 
In this particular attribute we can see  on its definition that the cluster is "com.zipato.cluster.onoff", this is how we know that we should load the OnOff controller so that the user can turn On or turn Off a light  for example. 

As mentioned earlier most of types should have their controllers so that the user can for example turn off/on a light. In order for that magic to happen you need to send a message to the server  saying "turn on light for device x",  well not exactly in this word but the idea is the same. 

First of all lets talk a little bit more about attribute. An attribute in a device is the last child of the tree and its the one which define which kind of device we are dealing in general base on its AtrributType filed found on attribute.definition.  
There are 5 types of  attributeType in our system: '**BOOLEAN**','**INTEGER**', '**DOUBLE**', '**STRING**' or '**NUMBER**' (in the example above we can see that we have boolean as attributeType). For example an attribute with attributeType = boolean, can store only two type of values, which are true or false, and that can be translated to either a sensor device (door sensor for example than can be either open or close) or an actuator (plug device for example that can turn  on or off ) or any type of device which require two state. 

So in order to let say switch an actuator device with attributeType boolean we need to send either true or false to server right? so the question is how do we do that. 

First we need to know what kind of attribute we are dealing with (most of the time we already know via either the templateID of the type or via the attribute>definition>cluster) for attributeTYpe then base on its UUID send a put request with the corresponding value in the body to  the server. 

Example to turn on the actuator above we use this web service 
`https://my.zipato.com:443/zipato-web/v2/attributes/51545411-7dde-40b3-8669-a11705b4e451/value 
 and in the body we put the specific json {"value" : "true"} and voila! 
 
> __PS__: please note that some attribute.definition has writable field set to false, which mean that the value of that   
> specific attribute cannot be altered
