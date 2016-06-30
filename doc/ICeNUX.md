# Introduction
ICeNUX makes use of broadcast communications to optimize the spectre efficiency of the wireless communication medium.
All nodes that run ICeNUX need to join the same ad hoc network on top of which communications will take place. 
Communications between ICeNUX nodes are based on the Information-centric Networking (ICN) paradigm, which provides in-network caching of Information Objects (IOs) and a publish-subscribe communication scheme.

The current version of ICeNUX relies on the *Spray and Wait* [1] routing protocol to provide routing capability to nodes in the ad hoc network. ICeNUX aims at achieving the good results of flooding-based routing algorithms in terms of message delivery ratio and latency while keeping resource consumption under control.
To this purpose, ICeDIM introduces the concept of **Application-level Dissemination Channels** (ADCs), which limit the consumption of bandwidth, memory, and computational resources typical of looding-based routing algorithms while maintaing high levels of delivery ratio and low latency.



## Architecture
The figure below shows the architecture of ICeNUX:

![Architecture of the ICeNUX middleware](https://github.com/DSG-UniFE/ICeNUX/blob/master/doc/ICeNUX%20Architecture.png)


The **ICeNUX API** is the layer that allows applications to interface with the middleware.

The **ADC Manager** is the core of ICeNUX. It implemets the probabilistic logic that drives the information dissemination when Application-level Dissemination Channels are used and takes care of delivering IOs to the applications. The ADC Manager interfaces with the Subscription Manager, the Neighborhood Manager, and the Routing Algorithm to make decisions concerning message dissemination.

The **Subscription Manager** keeps track of all ADCs joined by the current node and which applications subscribed to which channel.

The **Routing Algorithm** exposes a common interface to which all implementations of a routing protocol need to adhere. The current version of ICeNUX only supports the Spray and Wait routing protocol. Following the rules of the routing protocol used, IOs are enqueued in a forwarding queue from which the **Message Sender** retrieves packet for broadcasting.

The **Neighborhood Manager** keeps track of all neighbors a node has encountered in the past and those that are currently within communication range. The contact history also includes the ADCs to which other nodes belong and the IOs they have in their cache.

The **Hello Message Handler** analyzes HELLO messages sent by nearby neighbors and updates the information stored in the Neighborhood Manager.

The **Message Receiver** continuously listens for new packets coming from the network interface and forwards them to the **Message Dispatcher**, which infers the type of packet received and dispatches it to the right component for handling. Message types include *HELLO messages* (dispatched to the Hello Message Handler) and *ICeNUX data messages* (dispatched to the ADC Manager).




## Project Structure




## References
[1] T. Spyropoulos, K. Psounis, C. S. Raghavendra, “Spray and wait: an efficient routing scheme for intermittently connected mobile networks” Proceedings of the 2005 ACM SIGCOMM workshop on Delay-tolerant networking (WDTN '05), pp. 252-259, 2005.