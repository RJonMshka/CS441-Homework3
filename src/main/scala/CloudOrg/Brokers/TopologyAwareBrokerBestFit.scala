package CloudOrg.Brokers

import CloudOrg.Datacenters.{HybridNetworkDatacenter, RingNetworkDatacenter, StarNetworkDatacenter, TreeNetworkDatacenter}
import org.cloudbus.cloudsim.brokers.{DatacenterBrokerBestFit, DatacenterBrokerHeuristic}
import org.cloudbus.cloudsim.cloudlets.Cloudlet
import org.cloudbus.cloudsim.cloudlets.network.NetworkCloudlet
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.network.NetworkDatacenter
import org.cloudbus.cloudsim.vms.Vm

import java.util.Comparator
import scala.jdk.CollectionConverters.*

class TopologyAwareBrokerBestFit(simulation: CloudSim) extends DatacenterBrokerBestFit(simulation):


  private def findSuitableVmForCloudlet(cloudlet: Cloudlet, datacenterType: Class[? <: NetworkDatacenter]): Vm =
    val vm = super.defaultVmMapper(cloudlet)
    vm.getHost.getDatacenter.getClass match
      case datacenterType => vm
      case _ => {
        findSuitableVmForCloudlet(cloudlet, datacenterType)
      }


  override def defaultVmMapper(cloudlet: Cloudlet): Vm =
    cloudlet match
      case c: NetworkCloudlet => {
        // map reduce tasks
        if (c.getTasks.size == 2 || c.getTasks.size == 4) then
          findSuitableVmForCloudlet(c, classOf[TreeNetworkDatacenter])
        else if (c.getTasks.size == 3 || c.getTasks.size == 5) then
          // three tier app tasks
          findSuitableVmForCloudlet(c, classOf[RingNetworkDatacenter])
        else
          findSuitableVmForCloudlet(c, classOf[HybridNetworkDatacenter])
      }
      case _ => super.defaultVmMapper(cloudlet)
