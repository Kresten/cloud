SkyCave - Starting point for the SkyCave project
====

*Henrik Bærbak Christensen, Computer Science, Aarhus University, E2016*

Minor but important contributions by thesis student *Daniel Damgaard*,
graduated Summer 2016.

What is it?
-----------

SkyCave is the exam project in the Cloud Computing and Architecture
course at Computer Science, Aarhus University. It is loosely inspired
by the very first adventure games in computing, *Colossal Cave
Adventure*, by Will Crowther, 1972. However, game elements have
been removed and replaced by MMO (Massive Multiuser Online) features.

Please consult material on the course homepage for any further
information.

Requirements
------------

To execute and develop SkyCave, you need Java 8, Ant, and Ivy installed.

How do I get started?
---------------------

Execute 'ant' to get an overview of most important targets to execute
SkyCave. 

For running the daemon (SkyCave application server) and the cmd
(SkyCave client/user interface), review the 'ant daemon' and 'ant cmd'
targets. Typical usage is

Starting a SkyCave application server (the *daemon*) using its most
advanced configuration (=latest.cpf):

    ant daemon 

To start the daemon in a specific configuration as required by an
exercise, use a CPF named after the exercise, like e.g.:
  
    ant daemon -Dcpf=weather-client.cpf

To shut down the daemon again, do it the hard way: Hit Ctrl-c.

Starting a SkyCave client command-line (the *cmd*) configured to
talk to a daemon in a specific configuration, again, use 
-Dcpf=(exercise.cpf):

    ant cmd -Dcpf=weather-circuit-breaker.cpf

Again, the default is `latest.cpf` which should always reflect your
client that is for your most advanced staging environment.

How to I get started understanding the code?
--------------------------------------------

I advice starting by reviewing the testcases for the Player abstraction.
Here the IPC layer is 'short-circuited' and all network calls simulated
by simple in-JVM method calls.

First review the tests for the domain implementation,
test/cloud/cave/server/TestPlayerServant, which gives you an impression
of the server side implementation.

The distribution aspect (which is central to the course) uses the
Broker pattern (Find a chapter on Broker from the forth coming 2nd
edition of "Flexible, Reliable Software" on the course web pages), and
if you follow the call sequence from the tests in the testcases in
test/cloud/cave/client/TestPlayerProxy, you will trace through many of
the central aspects (marshalling and IPC).

Next, review the IPC implementations that uses Java sockets for
communication in src/cloud/cave/config/socket.

And - review the design slides from the course web pages.

Configuration
-------------

SkyCave is heavily reconfigurable to allow automated testing, as well
as supporting incremental development work and alternative
implementations of protocols, databases, service connectors, etc. The
configurability is controlled by reading Chained Property Files
(CPFs), like `socket.cpf` above, which define properties = (key,value)
pairs for all configuration options. All keys are prefixed by
SKYCAVE_.

Review the provided `local.cpf`, `socket.cpf`, and `lastest.cpf` for examples.

What to do next?
----------------

Solve the exercises posted on Blackboard using the techniques taught
to increase your exam score. Happy coding. - Henrik