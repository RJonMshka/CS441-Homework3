package CloudOrg.Simulations.RingTopologySimulations

import CloudOrg.Applications.MapReduceJob
import CloudOrg.HelperUtils.{CreateLogger, utils}
import CloudOrg.Datacenters.RingNetworkDatacenter
import CloudOrg.Simulations.CommonTopologyMapReduceSimulation
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple
import org.cloudbus.cloudsim.brokers.DatacenterBrokerHeuristic
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudsimplus.builders.tables.CloudletsTableBuilder

import scala.util.Random
import scala.jdk.CollectionConverters.*

object RingTopologyMapReduceAppSimulation {
  val logger = CreateLogger(classOf[RingTopologyMapReduceAppSimulation.type])


  def main(args: Array[String]): Unit = {
    logger.info("Starting simulation for Map Reduce Job on Ring Network Datacenter")
    CommonTopologyMapReduceSimulation.startSimulation(utils.NetworkDatacenterType.RING)
  }
}
