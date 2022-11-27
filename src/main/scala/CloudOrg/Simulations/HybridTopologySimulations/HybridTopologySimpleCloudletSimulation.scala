package CloudOrg.Simulations.HybridTopologySimulations

import CloudOrg.HelperUtils.{CreateLogger, utils}
import CloudOrg.Datacenters.HybridNetworkDatacenter
import CloudOrg.Simulations.CommonTopologySimpleCloudletSimulation
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple
import org.cloudbus.cloudsim.brokers.DatacenterBrokerHeuristic
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudsimplus.builders.tables.CloudletsTableBuilder

import scala.jdk.CollectionConverters.*

object HybridTopologySimpleCloudletSimulation {
  val logger = CreateLogger(classOf[HybridTopologySimpleCloudletSimulation.type])

  def startSimulation(): Unit =
    logger.info("Starting simulation for Simple cloudlets on Hybrid Network Datacenter")
    CommonTopologySimpleCloudletSimulation.startSimulation(utils.NetworkDatacenterType.HYBRID)
}
