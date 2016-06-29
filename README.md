# ICeNUX
ICeNUX is an Information-centric Networking communications middleware for ad-hoc and opportunistic networks implemented in Java.
ICeNUX supports Unix-based operating systems and implements the techniques conceived within
[ICeDiM](http://endif.unife.it/en/research/research-1/information-technology/computer-science/distributed-systems-group/research-projects/iceone), a research project conducted by the University of Ferrara, Italy.


## Features
ICeNUX makes use of broadcast communications to optimize the spectre efficiency of the wireless communication medium.
All nodes that run ICeNUX need to join the same ad hoc network on top of which communications will take place. 
Communications between ICeNUX nodes are based on the Information-centric Networking (ICN) paradigm, which provides in-network caching of Information Objects (IOs) and a publish-subscribe communication scheme.

The current version of ICeNUX relies on the Spray and Wait [1] routing protocol to provide routing capability to nodes in the ad hoc network. ICeNUX aims at achieving the good results of flooding-based routing algorithms in terms of message delivery ratio and latency while keeping resource consumption under control.
To this purpose, ICeDIM introduces the concept of Application-level Dissemination Channels (ADCs), which limit the consumption of bandwidth, memory, and computational resources typical of looding-based routing algorithms while maintaing high levels of delivery ratio and low latency.


## Structure and Architecture

The figure below shows the architecture of ICeNUX:

![Architecture of the ICeNUX middleware](https://github.com/DSG-UniFE/ICeNUX/blob/master/doc/ICeNUX%20Architecture.png)




## References
[1] T. Spyropoulos, K. Psounis, C. S. Raghavendra, “Spray and wait: an efficient routing scheme for intermittently connected mobile networks” Proceedings of the 2005 ACM SIGCOMM workshop on Delay-tolerant networking (WDTN '05), pp. 252-259, 2005.
