package CloudOrg.Simulations.StarTopologySimulations

import CloudOrg.Brokers.TopologyAwareDatacenterBroker
import CloudOrg.Datacenters.StarNetworkDatacenter
import CloudOrg.HelperUtils.{CreateLogger, utils}
import CloudOrg.Applications.MapReduceJob
import CloudOrg.Simulations.CommonTopologyMapReduceSimulation
import org.cloudbus.cloudsim.allocationpolicies.{VmAllocationPolicyBestFit, VmAllocationPolicyRandom, VmAllocationPolicyRoundRobin, VmAllocationPolicySimple}
import org.cloudbus.cloudsim.brokers.{DatacenterBrokerHeuristic, DatacenterBrokerSimple}
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.distributions.{ContinuousDistribution, UniformDistr}
import org.cloudsimplus.builders.tables.CloudletsTableBuilder

import scala.util.Random
import scala.jdk.CollectionConverters.*

object StarTopologyMapReduceAppSimulation {
  val logger = CreateLogger(classOf[StarTopologyMapReduceAppSimulation.type])

  def main(args: Array[String]): Unit = {
    logger.info("Starting simulation for Map Reduce Job on Star Network Datacenter")
    CommonTopologyMapReduceSimulation.startSimulation(utils.NetworkDatacenterType.STAR)
  }
}
