package CloudOrg.Simulations

import CloudOrg.Datacenters.CustomDatacenterService
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.core.CloudSim

object SaaSSimulation {

  // simulating user only have control over number of cloudlets and typpe of cloudlet (representing SAAS service tasks)
  val numberOfCloudlets = 20
  val taskToPerform = CustomDatacenterService.SaaSCloudletType.Type1

  def main(args: Array[String]): Unit = {
    val simulation = CloudSim()
    val broker = DatacenterBrokerSimple(simulation)

    CustomDatacenterService.requestSaaSSimulation(simulation, broker, numberOfCloudlets, taskToPerform)
  }

}
