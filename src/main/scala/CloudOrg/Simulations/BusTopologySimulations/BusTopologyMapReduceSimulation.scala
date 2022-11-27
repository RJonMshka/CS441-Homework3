package CloudOrg.Simulations.BusTopologySimulations

import CloudOrg.Applications.MapReduceJob
import CloudOrg.HelperUtils.{CreateLogger, utils}
import CloudOrg.Datacenters.BusNetworkDatacenter
import CloudOrg.Simulations.CommonTopologyMapReduceSimulation
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple
import org.cloudbus.cloudsim.brokers.{DatacenterBrokerHeuristic, DatacenterBrokerSimple}
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudsimplus.builders.tables.CloudletsTableBuilder

import scala.util.Random
import scala.jdk.CollectionConverters.*

object BusTopologyMapReduceSimulation {
  val logger = CreateLogger(classOf[BusTopologyMapReduceSimulation.type])

  def startSimulation(): Unit =
    logger.info("Starting simulation for Map Reduce Job on Bus Network Datacenter")
    CommonTopologyMapReduceSimulation.startSimulation(utils.NetworkDatacenterType.BUS)
}
