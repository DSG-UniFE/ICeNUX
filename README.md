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


## Configuring ICeNUX



## Running ICeNUX
The current version of ICeNUX comes with a very simple chat application to demonstrate the functionalities of the middleware. A Jar library containing the ICeNUX middleware for import within other Java projects is not yet available. More details on how to write an application that relies on ICeNUX for communications will be given below.

At startuup, ICeNUX issues commands that require root privileges in order to set up an hoc network for sending and receiving of packets. Therefore, it is necessary to launch ICeNUX as root.

    sudo java -jar ICeNUX.jar

The commands that ICeNUX issues for the set up of the ad hoc network differ depending on the OS and the distribution used.

### Ubuntu Linux OS
When running on Ubuntu machines, the following shell commands are launched to set up the ad hoc network:

    stop network-manager
	ip link set <network_interface_name> down
	iwconfig <network_interface_name> mode ad-hoc channel <network_channel> essid <network_BSSID>
    ip addr add <IP_address>/<netmask> broadcast <broadcast_address> dev <network_interface_name>
    ip link set <network_interface_name> up

When the application exits, the following shell commands are launched to restore the previous network configuration:

    start network-manager

All commands are issued as root.


### Systemd Linux OS (Red Hat, Fedora, Debian, ...)
When running on systemd-enabled machines, the following shell commands are launched to set up the ad hoc network:

    systemctl disable NetworkManager.service
    systemctl stop NetworkManager.service
	ip link set <network_interface_name> down
	iwconfig <network_interface_name> mode ad-hoc channel <network_channel> essid <network_BSSID>
    ip addr add <IP_address>/<netmask> broadcast <broadcast_address> dev <network_interface_name>
    ip link set <network_interface_name> up

When the application exits, the following shell commands are launched to restore the previous network configuration:

    systemctl enable NetworkManager.service
    systemctl start NetworkManager.service

All commands are issued as root.


### Apple Mac OS X
When running on Mac OS X machines, ICeNUX runs an Applescript script to set up a new ad hoc network or join an existing one. The script takes control of the OS user interface for a few seconds, in order to  The procedure has been tested only on Mac OS X El Capitain v10.11.5.
The script is available at [resources/enableWiFiAdHoc](https://github.com/DSG-UniFE/ICeNUX/blob/master/resources/enableWiFiAdHoc).

After the script has run, ICeNUX launches the following commands to configure the network interface:

    ifconfig <network_interface_name> inet <IP_address>/<netmask> broadcast <broadcast_address>

When the application exits, the following shell commands are launched to restore the previous network configuration:

    ifconfig <network_interface_name> down
    ifconfig <network_interface_name> up

All commands are issued as root.

