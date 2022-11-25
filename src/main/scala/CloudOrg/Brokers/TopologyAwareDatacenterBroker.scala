package CloudOrg.Brokers

import CloudOrg.Datacenters.{BusNetworkDatacenter, HybridNetworkDatacenter, RingNetworkDatacenter, StarNetworkDatacenter, TreeNetworkDatacenter}
import org.cloudbus.cloudsim.brokers.DatacenterBrokerHeuristic
import org.cloudbus.cloudsim.cloudlets.Cloudlet
import org.cloudbus.cloudsim.cloudlets.network.NetworkCloudlet
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.network.NetworkDatacenter
import org.cloudbus.cloudsim.vms.Vm

class TopologyAwareDatacenterBroker(simulation: CloudSim) extends DatacenterBrokerHeuristic(simulation):

  private def findSuitableVmForCloudlet(cloudlet: Cloudlet, datacenterType: Class[? <: NetworkDatacenter]): Vm =
    val heuristic = this.getHeuristic()
    val vm = heuristic.getBestSolutionSoFar().getResult().getOrDefault(cloudlet, super.defaultVmMapper(cloudlet))
    println("issue is here")
    vm.getHost.getDatacenter.getClass match
      case datacenterType => vm
      case _ => {
        findSuitableVmForCloudlet(cloudlet, datacenterType)
      }


  override def defaultVmMapper(cloudlet: Cloudlet): Vm =
    println("issue is here too")
    cloudlet match
      case c: NetworkCloudlet => {
        if (c.getTasks.size == 2 || c.getTasks.size == 4) then
          findSuitableVmForCloudlet(c, classOf[TreeNetworkDatacenter])
        else if (c.getTasks.size == 3 || c.getTasks.size == 5) then
          findSuitableVmForCloudlet(c, classOf[StarNetworkDatacenter])
//        else if (c.getTasks.size == 1 || c.getTasks.size > 5) then
//          findSuitableVmForCloudlet(c, classOf[HybridNetworkDatacenter])
        else
          findSuitableVmForCloudlet(c, classOf[RingNetworkDatacenter])
      }
      case _ => super.defaultVmMapper(cloudlet)
