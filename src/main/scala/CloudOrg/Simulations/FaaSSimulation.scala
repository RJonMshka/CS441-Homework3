package CloudOrg.Simulations

import CloudOrg.Datacenters.CustomDatacenterService
import CloudOrg.HelperUtils.ObtainConfigReference
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.core.CloudSim

object FaaSSimulation {
  val config = ObtainConfigReference("cloudOrganizationSimulations").get
  val faasSimConfig = config.getConfig("cloudOrganizationSimulations.faasSim")

  val faasCloudlets = faasSimConfig.getInt("faasCloudlets")
  val faaSCloudletLength = faasSimConfig.getInt("faaSCloudletLength")
  val faasCloudletPe = faasSimConfig.getInt("faasCloudletPe")
  val faasCloudletSize = faasSimConfig.getInt("faasCloudletSize")
  val faasCloudletOutputSize = faasSimConfig.getInt("faasCloudletOutputSize")

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
}
