# P2P-Sim
Cyclic P2P network simulator

Starting the simulator:
cd to src directory
compile with "javac *.java"
execute a setup script in the format of setup.sh and setup2.sh with "./setup.sh"
These scripts will open each peer in a new xterm window, where they can be controlled individually

Available commands:
quit - this will soft kill the peer, informing its neighbours and updating quickly
request XXXX - this sends a request for the file XXXX.pdf to be retrieved by the peer (a copy will be generated in /src when successfully transmitted)

Other controls:
Hard killing a peer (ctrl + C) will require a little while for the change to be noticed by other peers, allow the change to propogate before continuing. (limitation of the simulation)




Important Notes:
When requesting a file, the file must exist and be located in the src folder.
The file must follow the naming convention "XXXX.pdf" where the Xs represent a 4-digit zero-padded interger.

There must be a least 4 peers for it to continue working properly, since a peer must know 2 sucessors and 1 predecessor. Any less and behaviour will be erratic (not a use case required by the specs)
The setup script must correctly identify each peer and its neighbours, and correctness is assumed when executing.
