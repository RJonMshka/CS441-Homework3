package CloudOrg.Datacenters

import CloudOrg.HelperUtils.utils
import CloudOrg.Network.Topologies
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicy
import org.cloudbus.cloudsim.core.{CloudSim, Simulation}
import org.cloudbus.cloudsim.datacenters.network.NetworkDatacenter
import org.cloudbus.cloudsim.hosts.network.NetworkHost

import java.util

class TreeNetworkDatacenter(simulation: Simulation, hostList: util.List[? <: NetworkHost], vmAllocationPolicy: VmAllocationPolicy, treeSize: Int) extends NetworkDatacenter(simulation, hostList, vmAllocationPolicy):
  Topologies.createTreeNetworkTopologyInDatacenter(simulation.asInstanceOf[CloudSim], this, hostList, treeSize)
