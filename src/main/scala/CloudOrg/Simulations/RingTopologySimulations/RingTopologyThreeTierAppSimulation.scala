package CloudOrg.Simulations.RingTopologySimulations

import CloudOrg.Applications.ThreeTierApplication
import CloudOrg.HelperUtils.{CreateLogger, utils}
import CloudOrg.Datacenters.RingNetworkDatacenter
import CloudOrg.Simulations.CommonTopologyThreeTierSimulation
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple
import org.cloudbus.cloudsim.brokers.DatacenterBrokerHeuristic
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudsimplus.builders.tables.CloudletsTableBuilder

import scala.util.Random
import scala.jdk.CollectionConverters.*

object RingTopologyThreeTierAppSimulation {
  val logger = CreateLogger(classOf[RingTopologyThreeTierAppSimulation.type])

  def startSimulation(): Unit =
    logger.info("Starting simulation for Three Tier App jobs on Ring Network Datacenter")
    CommonTopologyThreeTierSimulation.startSimulation(utils.NetworkDatacenterType.RING)
}
