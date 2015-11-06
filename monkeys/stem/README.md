The Stem monkey
===============

This Clojure project provides the base implementation for all the monkeys used
in the Micromonkeys presentation. Its task is to define the following concepts
that a monkey deals with:

- Discover Consul and read its own configuration
- Receive the clock ticks
- House a parasites colony
- Ask to be groomed
- Receive a request to be groomed
- Die (-> fail) after the parasites infestation has gone too far

Micromonkeys use Consul to register themselves, discover other micromonkeys (e.g. to ask to
get groomed) and to fetch their configuration.

Clock ticks
===========

Time in the world of the Micromonkeys is discrete. Nothing happens until the clock
fires the next tick. Micromonkeys receive the clock tick from an external time source,
the Simulator app, via an UDP multicast.

Every micromonkey keeps the current clock tick number, and includes it in each message sent
to the outside world, in order to maintain a partial ordering on events.

Parasites
=========

Micromonkeys can be infested by a very nasty breed of ticks. These parasites carry a
deadly disease that will kick the micromonkey out of this world if its not groomed
soon enough.

The amount of clock ticks before a parasites infestation causes a micromonkey to die is fetched
from Consul.

Grooming
========

When a micromonkey gets the parasites, it will need another micromonkey to groom them
away. This is where specific micromonkeys behavior come into play!

Grooming works as follows:

- the infested micromonkey asks Consul for the other micromonkeys IP addresses
- the infested micromonkey selects randomly a micromonkey from the available ones
- the infested micromonkey sends a REST request to be groomed, including its callback endpoint
- this second micromonkey will reply as it sees fit
