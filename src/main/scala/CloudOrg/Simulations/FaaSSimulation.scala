package CloudOrg.Simulations

import CloudOrg.Datacenters.CustomDatacenterService
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.core.CloudSim

object FaaSSimulation {

  val faasCloudlets = 30
  val faaSCloudletLength = 500
  val faasCloudletPe = 1
  val faasCloudletSize = 200
  val faasCloudletOutputSize = 200

  def faaSSimulation(): Unit =
    val simulation = CloudSim()
    val broker = DatacenterBrokerSimple(simulation)

    CustomDatacenterService.requestFaaSService(
      simulation,
      broker,
      faasCloudlets,
      faaSCloudletLength,
      faasCloudletPe,
      faasCloudletSize,
      faasCloudletOutputSize
    )

  def main(args: Array[String]): Unit = {
    faaSSimulation()
  }
}
