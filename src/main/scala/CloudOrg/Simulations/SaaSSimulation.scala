package CloudOrg.Simulations

import CloudOrg.Datacenters.CustomDatacenterService
import CloudOrg.HelperUtils.ObtainConfigReference
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.core.CloudSim

object SaaSSimulation {
  val config = ObtainConfigReference("cloudOrganizationSimulations").get
  val saasSimConfig = config.getConfig("cloudOrganizationSimulations.saasSim")
  // simulating user only have control over number of cloudlets and typpe of cloudlet (representing SAAS service tasks)
  val numberOfCloudlets = saasSimConfig.getInt("numberOfCloudlets")
  // can be type 1, type 2 or type 3
  val taskToPerform = CustomDatacenterService.SaaSCloudletType.Type1

  def main(args: Array[String]): Unit = {
    val simulation = CloudSim()
    val broker = DatacenterBrokerSimple(simulation)

    CustomDatacenterService.requestSaaSSimulation(simulation, broker, numberOfCloudlets, taskToPerform)
  }

}
