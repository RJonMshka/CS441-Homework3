package CloudOrg.Datacenters

import CloudOrg.utils

import java.util
import org.cloudbus.cloudsim.core.{CloudSim, Simulation}
import org.cloudbus.cloudsim.datacenters.network.NetworkDatacenter
import org.cloudbus.cloudsim.hosts.network.NetworkHost
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicy

class StarNetworkDatacenter(simulation: Simulation, hostList: util.List[? <: NetworkHost], vmAllocationPolicy: VmAllocationPolicy) extends NetworkDatacenter(simulation, hostList, vmAllocationPolicy):
  utils.createStarNetworkTopologyInDatacenter(simulation.asInstanceOf[CloudSim], this, hostList)

