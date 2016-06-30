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
Configuration is a necessary step in order to give ICeNUX the necessary information concerning the set up of the ad hoc network. All configurable parameters are available in the settings.cfg file inside the resources directory.
This guide only describes the parameters that need to be configured to start using ICeNUX; for more information on the other options, the reader can refer to the comments in the [resources/settings.cfg](https://github.com/DSG-UniFE/ICeNUX/blob/master/resources/settings.cfg) file.

All parameters are in the form `options = value`, with two spaces (' ') surrounding the '=' sign.

  * **NetworkInterface**, the name of the wireless network interface card (NIC) that will be used to send and receive broadcast packets;
  * **ESSID**, the SSID of the ad hoc network that ICeNUX will create or join;
  * **NetworkChannel**, the radio channel on which the NIC will transmit and listen for packets;
  * **HostIP**, the IPv4 address that will be assigned to the NIC (e.g., 192.168.1.1);
  * **NetworkMask**, the IPv4 subnet mask expressed in its short form (e.g., 24 for 255.255.255.0);
  * **BroadcastAddress**, the IPv4 broadcast address that ICeNUX will use to broadcast packets (e.g., for a /16 subnetwork, 192.168.255.255);
  * **ReceivePort**, the UDP port number on which ICeNUX will listen for packets.



## Running ICeNUX
Once configuration is done, ICeNUX is ready to run. The current version comes with a very simple chat application to demonstrate the functionalities of the middleware.
A Jar library containing the ICeNUX middleware for import within other Java projects is not yet available. More details on how to write an application that relies on ICeNUX for communications will be given below.

At startup, ICeNUX issues commands that require root privileges in order to set up an hoc network for sending and receiving of packets. Therefore, it is necessary to launch ICeNUX as root.

    sudo java -jar ICeNUX.jar

Note that, if, for any reasons, ICeNUX fails to set up the ad hoc network and exits with an error, it is possible for the user to manually configure the wireless NIC and create an ad hoc network that complies with the options specifed in the settings.cfg file. After this is done, ICeNUX should detect that the ad hoc network has already been created and configured and start working normally.

The commands that ICeNUX issues for the set up of the ad hoc network differ depending on the OS and the distribution used. The list of commands executed by ICeNUX follows.


#### Ubuntu Linux OS
When running on Ubuntu machines, the following shell commands are launched to set up the ad hoc network:

    stop network-manager
	ip link set <network_interface_name> down
	iwconfig <network_interface_name> mode ad-hoc channel <network_channel> essid <network_ESSID>
    ip addr add <IP_address>/<netmask> broadcast <broadcast_address> dev <network_interface_name>
    ip link set <network_interface_name> up

ICeNUX retrieves the parameters within '<' and '>' from the settings.cfg file.
When the application exits, the following shell commands are launched to restore the previous network configuration:

    start network-manager

All commands are issued as root.


#### Systemd Linux OS (Red Hat, Fedora, Debian, ...)
When running on systemd-enabled machines, the following shell commands are launched to set up the ad hoc network:

    systemctl disable NetworkManager.service
    systemctl stop NetworkManager.service
	ip link set <network_interface_name> down
	iwconfig <network_interface_name> mode ad-hoc channel <network_channel> essid <network_ESSID>
    ip addr add <IP_address>/<netmask> broadcast <broadcast_address> dev <network_interface_name>
    ip link set <network_interface_name> up

ICeNUX retrieves the parameters within '<' and '>' from the settings.cfg file.
When the application exits, the following shell commands are launched to restore the previous network configuration:

    systemctl enable NetworkManager.service
    systemctl start NetworkManager.service

All commands are issued as root.


#### Apple Mac OS X
When running on Mac OS X machines, ICeNUX runs an Applescript script to set up a new ad hoc network or join an existing one. The script takes control of the OS user interface for a few seconds, in order to  The procedure has been tested only on Mac OS X El Capitain v10.11.5.
The script is available at [resources/enableWiFiAdHoc](https://github.com/DSG-UniFE/ICeNUX/blob/master/resources/enableWiFiAdHoc).

After the script has run, ICeNUX launches the following commands to configure the network interface:

    ifconfig <network_interface_name> inet <IP_address>/<netmask> broadcast <broadcast_address>

ICeNUX retrieves the parameters within '<' and '>' from the settings.cfg file.
When the application exits, the following shell commands are launched to restore the previous network configuration:

    ifconfig <network_interface_name> down
    ifconfig <network_interface_name> up

All commands are issued as root.



## Writing your own application that uses ICeNUX

