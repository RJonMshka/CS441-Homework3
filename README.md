# CS-441 Cloud Computing Objects
## Homework 2 Documentation
## By: Rajat Kumar (UIN: 653922910)

## Cloud Organization Simulations using CloudSimPlus and Scala

---
## Introduction
This homework involves demonstration of creating different cloud organizations using CloudSimPlus (a framework for simulation of cloud infrastructure and its properties with great accuracy).
The tasks for this homework includes creating different cloud architectures with changing properties and assigned workload to conclude which architecture is best for certain circumstances.
Also, SAAS(Software as a Service), PAAS(Platform as a service), IAAS (Infrastructure as a Service) and FAAS(Function as a service) models have also been simulated upto some extend with project specific constraints.
All of these tasks are described in the later sections of this documentation.
---


## How to run the application


---

## Experiments and its Attributes
The experiments performed with this project revolves around three types of applications which is described in later sections.

### Datacenter
Experimentation is done with six types of datacenters.
1. DatacenterSimple - This datacenter is used for basic simulation and performs good normal simple cloudlets.

2. StarNetworkDatacenter - Implemented star topology by connecting host in a certain star fashion using edge switches, aggregate switches. The way host are connected is shown the topology diagram.

![Star Topology](images/star_topology.png)

3. TreeNetworkDatacenter - Implemented tree topology by connecting host in a tree structure using edge switches, aggregate switches. Tree network is best suited for Map reduce jobs are per the experiments performed. The way host are connected is shown the topology diagram.

![Tree Topology](images/tree_topology.png)

4. BusNetorkDatacenter - Implemented tree topology by connecting host in a tree structure using edge switches, aggregate switches. The way host are connected is shown the topology diagram.

![Bus Topology](images/bus_topology.png)

5. RingNetworkDatacenter - Implemented ring topology by connecting host in a ring structure using edge switches, aggregate switches. The way host are connected is shown the topology diagram.

![Ring Topology](images/ring_topology.png)

6. HybridNetworkDatacenter - Implmented hybrid network which is mix of star, ring and tree-bus mix structure. As per the experiments, it is suited for simple cloudlet execution. The way host are connected is shown the topology diagram.

![Hybrid Topology](images/hybrid_topology.png)

### Broker
We experimented with three types of broker.
1. DatacenterBrokerSimple - This broker performs well in a single datacenter cloud organization.
2. TopologyAwareBrokerHeuristic - This is a custom implemented broker which takes into consideration various topologies and type of tasks/cloudlets passed into consideration and send particular type of cloudlets to particular type of datacenter. This broker also extends the functionality of DatacenterBrokerHeuristic and can be used with any type of heuristic.
3. TopologyAwareBrokerBestFit - This is also a custom implemented broker which takes into consideration various topologies and type of tasks/cloudlets passed into consideration and send particular type of cloudlets to particular type of datacenter. However, this extends best fit broker functionality so as per my experiments, it performed better than heuristic one and hence, decided to be used for multi-datacenter experiment.


### Hosts
Other experiments are done with hosts, networks hosts, their capacity, processing power, processing elements (cpu cores), ram, bandwidth, storage and how VMs are allocated to it through its datacenter.
Network hosts are also created to gain advantage of network topologies and various network topologies have been implemented.

### VMAllocationPolicy
Implemented Simple, Bestfit, Random and RoundRobin policies for vm allocation. Out of them BestFit and Simple were found to be most performant.

### VMs
Virtual Machines are created and submitted to broker for simulation. These will have different characteristics like
capacity, processing power, processing elements (cpu cores), ram, bandwidth, storage. Also, VMs are provided with particular VM scheduler which schedules the execution of VM on host on which they are created.

### VM Schedulers
Implemented Timeshared, Spaceshared, Random (custom created) and PseudoRandom (custom created - mix of random and spaceshared).
Both Timeshared and Spaceshared can be used to achieve great performance, however, they needs to be used in conjucture with certain type of cloudlet scheduler to achieve greater results.

### Cloudlets
Cloudlets are basically the task the cloud customer sends to the cloud provider to be executed on the resources provided by cloud organization.
This project uses two types of cloudlets - CloudletSimple and NetworkCloudlet. CloudletSimple can have one level of execution, however, network cloudlet have subtasks which can be used to further perform executions as well as create send and receive events for communicating with other cloudlets. 
In this project network cloudlet is used to create complex applications to test the efficiency of cloud organization and the underlying network topologies.

### Cloudlet Schedulers
Implemented Timeshared, Spaceshared, Fair schedulers for experimenting. This scheduler refers to scheduling execution of cloudlets on a particular VM and its functionality actually makes a decision on whether to execute in on the a particular VM or not (based on different factors).

As per my experiment, best results are achieved when either VM scheduler is timeshared or spaceshared and cloudlet scheduler is either timeshared and spaceshared and neither one of them is same (they are exclusive).

Diagram if possible

### Power Modeling
This project also provides how much of static and max power a host can use. Also implemented startup and shutdown delays and power consumption.

### Cost Modeling
This project also involves creating cost models where particular type of resource (computing, memory, bandwidth, storage) accesses will incur a particular cost the user.

### AutoScaling
In this project we have used Vertical scaling for CPUs(VMs) and horizontal scaling for memory by configuring certain threshold and predicates.

### Utilization Modeling
CPU, RAM and bandwidth utilization models are created with dynamic utilization of resources and how that would change resouces needed by cloudlets.

---

## Results of the experiment

### 1. Basic Example (Cloudsim introduction)
The first experiment is the basic one. In this there is only Datacentersimple with simple cloudlet and no custom scheduler.
Results are:
![basic exp](images/basic_simulation.png)
Also, there are no performance metrics captured for this one.


