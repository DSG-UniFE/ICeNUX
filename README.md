# ICeNUX
ICeNUX is an *Information-centric Networking* communications middleware for ad-hoc and opportunistic networks implemented in Java.
ICeNUX supports Unix-based operating systems and implements the techniques conceived within
[ICeDiM](http://endif.unife.it/en/research/research-1/information-technology/computer-science/distributed-systems-group/research-projects/iceone), a research project conducted by the University of Ferrara, Italy.


More information on the ICeNUX project are available [here](https://github.com/DSG-UniFE/ICeNUX/blob/master/doc/ICeNUX.md).


## Installation
In order to install and use ICeDiM, it is necessary to compile and configure the framework.
First, download a copy of the source project from https://github.com/DSG-UniFE/ICeNUX or clone it on your hard drive using git:

    git clone https://github.com/DSG-UniFE/ICeNUX

Then, cd into the root folder of the project (`ICeNUX/` if you used git or `ICeNUX-master/` if you downloaded the ZIP file from the GitHub website) and compile it using the gradle wrapper, provided with the source code:

    ./gradlew build

A jar file named ICeNUX.jar should now be in the root folder of the project.



## Running ICeNUX
The current version of ICeNUX comes with a very simple chat application to demonstrate the functionalities of the middleware




