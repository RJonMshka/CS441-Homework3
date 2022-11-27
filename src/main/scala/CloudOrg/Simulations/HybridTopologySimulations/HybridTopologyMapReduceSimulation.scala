package CloudOrg.Simulations.HybridTopologySimulations

import CloudOrg.Applications.MapReduceJob
import CloudOrg.HelperUtils.{CreateLogger, utils}
import CloudOrg.Datacenters.HybridNetworkDatacenter
import CloudOrg.Simulations.CommonTopologyMapReduceSimulation
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple
import org.cloudbus.cloudsim.brokers.DatacenterBrokerHeuristic
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudsimplus.builders.tables.CloudletsTableBuilder

import scala.util.Random
import scala.jdk.CollectionConverters.*

object HybridTopologyMapReduceSimulation {
  val logger = CreateLogger(classOf[HybridTopologyMapReduceSimulation.type])

  def startSimulation(): Unit = {
    logger.info("Starting simulation for Map Reduce Job on Hybrid Network Datacenter")
    CommonTopologyMapReduceSimulation.startSimulation(utils.NetworkDatacenterType.HYBRID)
  }
}
