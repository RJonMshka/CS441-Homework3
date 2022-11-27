package CloudOrg.Simulations.StarTopologySimulations

import CloudOrg.Brokers.TopologyAwareDatacenterBroker
import CloudOrg.Datacenters.StarNetworkDatacenter
import CloudOrg.Applications.ThreeTierApplication
import CloudOrg.HelperUtils.{CreateLogger, utils}
import CloudOrg.Simulations.CommonTopologyThreeTierSimulation
import org.cloudbus.cloudsim.allocationpolicies.{VmAllocationPolicyRandom, VmAllocationPolicySimple}
import org.cloudbus.cloudsim.brokers.{DatacenterBroker, DatacenterBrokerHeuristic, DatacenterBrokerSimple}
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.distributions.UniformDistr
import org.cloudbus.cloudsim.vms.{Vm, VmCost}
import org.cloudsimplus.autoscaling.HorizontalVmScalingSimple
import org.cloudsimplus.builders.tables.CloudletsTableBuilder
import org.cloudsimplus.heuristics.CloudletToVmMappingSimulatedAnnealing

import scala.util.Random
import scala.jdk.CollectionConverters.*

object StarTopologyThreeTierComplexAppSimulation {
  val logger = CreateLogger(classOf[StarTopologyThreeTierComplexAppSimulation.type])

  def main(args: Array[String]): Unit = {
    logger.info("Starting simulation for Three Tier App jobs on Star Network Datacenter")
    CommonTopologyThreeTierSimulation.startSimulation(utils.NetworkDatacenterType.STAR)
  }

}
