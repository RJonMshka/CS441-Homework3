package CloudOrg.Simulations.BusTopologySimulations

import CloudOrg.Datacenters.BusNetworkDatacenter
import CloudOrg.Applications.ThreeTierApplication
import CloudOrg.HelperUtils.{CreateLogger, utils}
import CloudOrg.Simulations.CommonTopologyThreeTierSimulation
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple
import org.cloudbus.cloudsim.brokers.DatacenterBrokerHeuristic
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudsimplus.builders.tables.CloudletsTableBuilder

import scala.util.Random
import scala.jdk.CollectionConverters.*

object BusTopologyThreeTierAppSimulation {
  val logger = CreateLogger(classOf[BusTopologyThreeTierAppSimulation.type])

  def main(args: Array[String]): Unit =
    logger.info("Starting simulation for Three Tier App jobs on Bus Network Datacenter")
    CommonTopologyThreeTierSimulation.startSimulation(utils.NetworkDatacenterType.BUS)
}
