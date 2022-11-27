package CloudOrg.Simulations.TreeTopologySimulations

import CloudOrg.HelperUtils.{CreateLogger, utils}
import CloudOrg.Datacenters.TreeNetworkDatacenter
import CloudOrg.Simulations.CommonTopologySimpleCloudletSimulation
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple
import org.cloudbus.cloudsim.brokers.DatacenterBrokerHeuristic
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudsimplus.builders.tables.CloudletsTableBuilder

import scala.jdk.CollectionConverters.*

object TreeTopologySimpleCloudletSimulation {
  val logger = CreateLogger(classOf[TreeTopologySimpleCloudletSimulation.type])

  def startSimulation(): Unit =
    logger.info("Starting simulation for Simple cloudlets on Tree Network Datacenter")
    CommonTopologySimpleCloudletSimulation.startSimulation(utils.NetworkDatacenterType.TREE)
}
