package CloudOrg.Datacenters

import CloudOrg.HelperUtils.utils
import CloudOrg.Network.Topologies
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicy
import org.cloudbus.cloudsim.core.{CloudSim, Simulation}
import org.cloudbus.cloudsim.datacenters.network.NetworkDatacenter
import org.cloudbus.cloudsim.hosts.network.NetworkHost

import java.util

class RingNetworkDatacenter(simulation: Simulation, hostList: util.List[? <: NetworkHost], vmAllocationPolicy: VmAllocationPolicy) extends NetworkDatacenter(simulation, hostList, vmAllocationPolicy):
  Topologies.createRingNetworkTopologyInDatacenter(simulation.asInstanceOf[CloudSim], this, hostList)