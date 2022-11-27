package CloudOrg.Simulations.RingTopologySimulations

import CloudOrg.HelperUtils.{CreateLogger, utils}
import CloudOrg.Datacenters.RingNetworkDatacenter
import CloudOrg.Simulations.CommonTopologySimpleCloudletSimulation
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple
import org.cloudbus.cloudsim.brokers.DatacenterBrokerHeuristic
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudsimplus.builders.tables.CloudletsTableBuilder

import scala.jdk.CollectionConverters.*

object RingTopologySimpleCloudletSimulation {
  val logger = CreateLogger(classOf[RingTopologySimpleCloudletSimulation.type])

  def startSimulation(): Unit =
    logger.info("Starting simulation for Simple cloudlets on Ring Network Datacenter")
    CommonTopologySimpleCloudletSimulation.startSimulation(utils.NetworkDatacenterType.RING)
}
