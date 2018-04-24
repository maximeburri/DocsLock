# DocsLock
Android documents reader for courses or exams. PDFs can be synced by the administration application and devices can be locked and then unlocked. 
![Logo](docs/images/logo.png)

## Getting started
### Architecture
DocsLock is divised into three components : 
- Android application which can be installed on desired devices
- Client application to administrate the devices and sends the documents
- A server to make the communication between devices and application, serves client application and REST API 
![Architecture](docs/images/architecture.png)

### Prerequisites
- Android application are tested and works on android 7.0
- A local WiFi LAN needs to be configured and needs to contains server and devices (no other devices for security reasons, c.f. Limitations), can be disconnect from Internet

### Dependencies used
- Client : [Angular4](https://angular.io/), [Angular Material](https://material.angular.io/) and [socket.io](https://socket.io/)
- Server : [MongoDB](https://www.mongodb.com/), [Sails.js 0.12](https://sailsjs.com/) and [socket.io](https://socket.io/)
- Android : [retrofit2](http://square.github.io/retrofit/) and [barteksc/AndroidPdfViewer](https://github.com/barteksc/AndroidPdfViewer)

### Limitations
For reasons of time, there are multiple security issues. Communications are not in HTTPS, devices are not authenticated and client administration application are not secured by a password. For theses reasons you need to configure a private Wifi LAN only used for this application. If not, others computer could change devices states. Packets could be snooping and attacked by a MITM.

Other warning : don't give unlocked devices. They could go to the administration application and change unlock/lock the other devices. 

We want to fix this security issue in next release. The current release is think as a Proof of Concept.


## Installation
...
## Deployement
...
## Developpment
...


## Next steps
The project is in pause mode and just some bugs will be fixed. If the project will be restarted, the priority will be give to :
- Make communication over HTTPS (easy)
- Authenticates with OAuth devices (long)
- Add users managment for administration application (long) or protect by a single password (easy)

## Authors
This project is made for [HEPIA](http://hepia.hesge.ch/) initied by [Florent GLUCK](https://github.com/florentgluck) and developped by (c.f. [Contributors](https://github.com/maximeburri/DocsLock/graphs/contributors)): 
- [Maxime BURRI](https://github.com/maximeburri)
- [Salvatore CICCIU](https://github.com/sa)