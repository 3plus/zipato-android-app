#Browser Manager

The browser manager is one of the most important screen in our mobile system because it is the screen which will interact the most with the user and properly understand it is critical in order to fully understand how our mobile system work. 

First of all the browser manager is divided in 2 mains views (portrait) and 3 on tablet (landscape mode) . 
The first view on the left is where all user types are display! shorted in alphabetically order and categorize by uiType. 

You are probably wondering what is type? Right we introduced types in api v2, a type is just like a kind of entity(can be a device,an endpoint, a cluster  or an attribute for example) with certain sets of configurations and fields where some can be set by the user like the "Show as Device" (set to true if the user want to see a particular type in the browser manager for example), icon (the user can choose which icon to display and color instead of the default one), uiType( what kind of entity it is? is it a Light, a sensor or camera? etc) and some that cannot be modify by the user like templateID (used to specify and differentiate certain entity from others) . Every type  fetch from the servers should contains a name , link, uuid and uitype by default at least and  all its attributes if exist and if itself is not an attribute. 

Json example of a type fetched from the types API:
 
``` JSON
{
    "entityType": "ENDPOINT",
    "link": "http://staging.zipato.com/zipato-web/v2/endpoints/d69dc105-536d-4361-96b8-1d6701d44988",
    "name": "Zipato AirCondition IR",
    "room": 15432,
    "uuid": "d69dc105-536d-4361-96b8-1d6701d44988",
    "templateId": "",
    "endpointType": "meter",
    "attributes": [
        {
            "link": "http://staging.zipato.com/zipato-web/v2/attributes/27d673bb-26cc-4174-8b1d-1d92b27cd7bb",
            "name": "SETPOINT_COOLING",
            "attributeId": 169,
            "attributeName": "value2",
            "room": 15432,
            "uuid": "27d673bb-26cc-4174-8b1d-1d92b27cd7bb",
            "master": false
        },
        {
            "link": "http://staging.zipato.com/zipato-web/v2/attributes/7bc2340e-f1bd-495a-af31-40e26545f023",
            "name": "SETPOINT_DRY_AIR",
            "attributeId": 170,
            "attributeName": "value3",
            "room": 15432,
            "uuid": "7bc2340e-f1bd-495a-af31-40e26545f023",
            "master": false
        },
        {
            "link": "http://staging.zipato.com/zipato-web/v2/attributes/c107951a-f662-4f7b-a7b3-4a5aec2efcf3",
            "name": "SETPOINT_HEATING",
            "attributeId": 168,
            "attributeName": "value1",
            "room": 15432,
            "uuid": "c107951a-f662-4f7b-a7b3-4a5aec2efcf3",
            "master": false
        },
        {
            "link": "http://staging.zipato.com/zipato-web/v2/attributes/ef682f0c-e382-4429-998b-6311f24ce1e4",
            "name": "TEMPERATURE",
            "attributeId": 95,
            "attributeName": "value1",
            "room": 15432,
            "uuid": "ef682f0c-e382-4429-998b-6311f24ce1e4",
            "master": true
        },
        {
            "link": "http://staging.zipato.com/zipato-web/v2/attributes/83c7e7cc-0815-4584-b056-93e54bbe2902",
            "name": "THERMOSTAT_FAN_MODE",
            "attributeId": 193,
            "attributeName": "mode",
            "room": 15432,
            "uuid": "83c7e7cc-0815-4584-b056-93e54bbe2902",
            "master": false
        },
        {
            "link": "http://staging.zipato.com/zipato-web/v2/attributes/208dcce8-225b-4dab-bf15-3a6c020b1829",
            "name": "THERMOSTAT_MODE",
            "attributeId": 192,
            "attributeName": "mode",
            "room": 15432,
            "uuid": "208dcce8-225b-4dab-bf15-3a6c020b1829",
            "master": false
        },
        {
            "link": "http://staging.zipato.com/zipato-web/v2/attributes/72c1671f-5fab-4741-99c4-f1b73059556c",
            "name": "value",
            "attributeId": 185,
            "attributeName": "value",
            "room": 15432,
            "uuid": "72c1671f-5fab-4741-99c4-f1b73059556c",
            "master": false
        }
    ],
    "show": true,
    "uiType": {
        "link": "http://staging.zipato.com/zipato-web/v2/types/system/Generic+meter",
        "name": "Generic meter",
        "endpointType": "meter",
        "relativeUrl": "/meter.png"
    }
} 
```
In this example our type above is  an Endpoint as entity but of type meter (see uiType) and it will be categorize as meter on the left view with any other meter but the name shown to the user will be "Generic meter" , and if we look a little bit up with can see that this entity(Endpoint) has a name  of "Zipato AirCondition IR" , belong to the room with id = 15432, show is set to true(so it will appear on the browser manager) and has 7 attributes.

To get the full list of type use the webservice https://my.zipato.com/zipato-web/v2/types/search/all 
 
<img src="https://github.com/3plus/zipato-android-v2/blob/237bc95a7a5117b48f54507b3783a5c24d681702/media/figure_1.png" width="450">

 **Figure 1**
 
Figure 1 is and example on how a list of type is categorize and shown to the user. 

The right view is a kind of main container for the browser manager it will not just display a list of types in each category when user click on the left view but also display the appropriate controller when user select one type of the list.  

What is a controller? a controller is a custom made view made in order to make the interaction with user and a device/entity easy, pleasant and simple.Example a On/Off switch controller will display on screen a simple button where the user can  just press to turn on or turn off an actuator. 

Now that we know the importance of this 2 views let talk about how we handle the data in browser manager. 
You need to create a repository that will store the full types and the full trees (network, device, endpoint, cluster, attribute) and map each of entry with their respective UUID . The trees is useful when you need to retrieve let say the device in which a type belong to (with Entity Type endpoint for example) and vis versa. 

Use the network API : https://my.zipato.com/zipato-web/v2/networks/trees  to retrieve the full network trees 

This data should be store locally so that the app will only load them once and refresh the data only when there is a change on the server side or when requested by the server. 

On figure 1 i bet that you already noticed the value below each items and wondering what is it about, this value is the master value of attributes of each type (as i already mention a type can be either a device , endpoint,cluster,attribute) .An attribute in a type is master when its field master is set to true (can be configure by the user), in some case all attributes can have master attribute set to true, in that case the first one will be display on that container. 
