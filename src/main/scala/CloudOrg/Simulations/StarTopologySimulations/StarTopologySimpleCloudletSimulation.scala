package CloudOrg.Simulations.StarTopologySimulations

import CloudOrg.Brokers.TopologyAwareDatacenterBroker
import CloudOrg.Datacenters.StarNetworkDatacenter
import CloudOrg.HelperUtils.{CreateLogger, utils}
import CloudOrg.Simulations.CommonTopologySimpleCloudletSimulation
import org.cloudbus.cloudsim.allocationpolicies.{VmAllocationPolicyRandom, VmAllocationPolicySimple}
import org.cloudbus.cloudsim.brokers.DatacenterBrokerHeuristic
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudsimplus.builders.tables.CloudletsTableBuilder

import scala.jdk.CollectionConverters.*

object StarTopologySimpleCloudletSimulation {
  val logger = CreateLogger(classOf[StarTopologySimpleCloudletSimulation.type ])

  def startSimulation(): Unit =
    logger.info("Starting simulation for Simple cloudlets on Star Network Datacenter")
    CommonTopologySimpleCloudletSimulation.startSimulation(utils.NetworkDatacenterType.STAR)
}
