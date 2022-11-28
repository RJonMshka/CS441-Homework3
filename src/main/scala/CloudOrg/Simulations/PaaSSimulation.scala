package CloudOrg.Simulations

import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.core.CloudSim
import CloudOrg.Datacenters.CustomDatacenterService
import CloudOrg.HelperUtils.ObtainConfigReference

object PaaSSimulation {
  val config = ObtainConfigReference("cloudOrganizationSimulations").get
  val paasSimConfig = config.getConfig("cloudOrganizationSimulations.paasSim")

  // Map Reduce PaaS inputs
  val mapReduceJobs = paasSimConfig.getInt("mapReduceJobs")
  val jobLength = paasSimConfig.getInt("jobLength")
  val mapReduceJobPeConsumption = paasSimConfig.getInt("mapReduceJobPeConsumption")

  // Three Tier PaaS inputs
  val threeTierAppInstances = paasSimConfig.getInt("threeTierAppInstances")
  val executionLength = paasSimConfig.getInt("executionLength")
  val threeTierAppPeConsumption = paasSimConfig.getInt("threeTierAppPeConsumption")


  /**
   * PAAS Cloud Customer simulation for MapReduce Platform
   */
  def mapReducePaaSSimulation(): Unit =
    val simulation = CloudSim()
    val broker = DatacenterBrokerSimple(simulation)
    CustomDatacenterService.requestMapReducePaaSSimulation(simulation, broker, mapReduceJobs, jobLength, mapReduceJobPeConsumption)

  /**
   * PAAS Cloud Customer simulation for Three Tier App Platform
   */
  def threeTierPaaSSimulation(): Unit =
    val simulation = CloudSim()
    val broker = DatacenterBrokerSimple(simulation)
    CustomDatacenterService.requestThreeTierPaaSService(simulation, broker, threeTierAppInstances, executionLength, threeTierAppPeConsumption)

}
