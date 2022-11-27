package CloudOrg.Simulations

import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.core.CloudSim
import CloudOrg.Datacenters.CustomDatacenterService

object PaaSSimulation {

  // Map Reduce PaaS inputs
  val mapReduceJobs = 20
  val jobLength = 1000
  val mapReduceJobPeConsumption = 1

  // Three Tier PaaS inputs
  val threeTierAppInstances = 20
  val executionLength = 1000
  val threeTierAppPeConsumption = 1


  // Three Tier PaaS inputs

  def mapReducePaaSSimulation(): Unit =
    val simulation = CloudSim()
    val broker = DatacenterBrokerSimple(simulation)
    CustomDatacenterService.requestMapReducePaaSSimulation(simulation, broker, mapReduceJobs, jobLength, mapReduceJobPeConsumption)


  def threeTierPaaSSimulation(): Unit =
    val simulation = CloudSim()
    val broker = DatacenterBrokerSimple(simulation)
    CustomDatacenterService.requestThreeTierPaaSService(simulation, broker, threeTierAppInstances, executionLength, threeTierAppPeConsumption)

  def main(args: Array[String]): Unit = {
    threeTierPaaSSimulation()
  }

}
