package CloudOrg.Simulations.TreeTopologySimulations

import CloudOrg.Applications.MapReduceJob
import CloudOrg.HelperUtils.{CreateLogger, utils}
import CloudOrg.Datacenters.TreeNetworkDatacenter
import CloudOrg.Simulations.CommonTopologyMapReduceSimulation
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple
import org.cloudbus.cloudsim.brokers.DatacenterBrokerHeuristic
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudsimplus.builders.tables.CloudletsTableBuilder

import scala.util.Random
import scala.jdk.CollectionConverters.*

object TreeTopologyMapReduceSimulation {
  val logger = CreateLogger(classOf[TreeTopologyMapReduceSimulation.type])

  def startSimulation(): Unit = {
    logger.info("Starting simulation for Map Reduce Job on Tree Network Datacenter")
    CommonTopologyMapReduceSimulation.startSimulation(utils.NetworkDatacenterType.TREE)
  }
}
