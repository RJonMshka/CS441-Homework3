package CloudOrg.Simulations.BusTopologySimulations

import CloudOrg.HelperUtils.{CreateLogger, utils}
import CloudOrg.Datacenters.BusNetworkDatacenter
import CloudOrg.Simulations.CommonTopologySimpleCloudletSimulation
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple
import org.cloudbus.cloudsim.brokers.DatacenterBrokerHeuristic
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudsimplus.builders.tables.CloudletsTableBuilder

import scala.jdk.CollectionConverters.*

object BusTopologySimpleCloudletSimulation {
  val logger = CreateLogger(classOf[BusTopologySimpleCloudletSimulation.type])

  def startSimulation(): Unit =
    logger.info("Starting simulation for Simple cloudlets on Bus Network Datacenter")
    CommonTopologySimpleCloudletSimulation.startSimulation(utils.NetworkDatacenterType.BUS)
}
