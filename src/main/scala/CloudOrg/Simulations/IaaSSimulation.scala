package CloudOrg.Simulations

import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.core.CloudSim
import CloudOrg.Datacenters.CustomDatacenterService
import CloudOrg.HelperUtils.ObtainConfigReference

object IaaSSimulation {
  val config = ObtainConfigReference("cloudOrganizationSimulations").get
  val iaasSimConfig = config.getConfig("cloudOrganizationSimulations.iaasSim")

  // variables for map reduce app on an iaas
  val mapReduceAppsToRun = iaasSimConfig.getInt("mapReduceAppsToRun")
  val mapReduceCloudletLength = iaasSimConfig.getInt("mapReduceCloudletLength")
  val mapReduceAppPe = iaasSimConfig.getInt("mapReduceAppPe")
  val mapReduceCloudScalingEnabled = iaasSimConfig.getBoolean("mapReduceCloudScalingEnabled")
  val mapReduceRamScalingFactor = iaasSimConfig.getDouble("mapReduceRamScalingFactor")
  val mapReduceCpuOverloadThreshold = iaasSimConfig.getDouble("mapReduceCpuOverloadThreshold")
  val mapReduceRamUpperThreshold = iaasSimConfig.getDouble("mapReduceRamUpperThreshold")
  val mapReduceRamLowerThreshold = iaasSimConfig.getDouble("mapReduceRamLowerThreshold")

  // variables for three tier app on an iaas
  val threeTierAppsToRun = iaasSimConfig.getInt("threeTierAppsToRun")
  val threeTierCloudletLength = iaasSimConfig.getInt("threeTierCloudletLength")
  val threeTierAppPe = iaasSimConfig.getInt("threeTierAppPe")
  val threeTierCloudScalingEnabled = iaasSimConfig.getBoolean("threeTierCloudScalingEnabled")
  val threeTierRamScalingFactor = iaasSimConfig.getDouble("threeTierRamScalingFactor")
  val threeTierCpuOverloadThreshold = iaasSimConfig.getDouble("threeTierCpuOverloadThreshold")
  val threeTierRamUpperThreshold = iaasSimConfig.getDouble("threeTierRamUpperThreshold")
  val threeTierRamLowerThreshold = iaasSimConfig.getDouble("threeTierRamLowerThreshold")

  // variables for simple (single simple cloudlet) app on an iaas
  val simpleAppsToRun = iaasSimConfig.getInt("simpleAppsToRun")
  val simpleCloudletLength = iaasSimConfig.getInt("simpleCloudletLength")
  val simpleAppPe = iaasSimConfig.getInt("simpleAppPe")
  val simpleCloudScalingEnabled = iaasSimConfig.getBoolean("simpleCloudScalingEnabled")
  val simpleRamScalingFactor = iaasSimConfig.getDouble("simpleRamScalingFactor")
  val simpleCpuOverloadThreshold = iaasSimConfig.getDouble("simpleCpuOverloadThreshold")
  val simpleRamUpperThreshold = iaasSimConfig.getDouble("simpleRamUpperThreshold")
  val simpleRamLowerThreshold = iaasSimConfig.getDouble("simpleRamLowerThreshold")

  def iaaSMapReduceAppSimulation(): Unit =
    val simulation = CloudSim()
    val broker = DatacenterBrokerSimple(simulation)
    CustomDatacenterService.requestIaaSService(
      simulation,
      broker,
      CustomDatacenterService.Application.MapReduce,
      mapReduceAppsToRun,
      mapReduceCloudletLength,
      mapReduceAppPe,
      mapReduceCloudScalingEnabled,
      mapReduceCpuOverloadThreshold,
      mapReduceRamScalingFactor,
      mapReduceRamUpperThreshold,
      mapReduceRamLowerThreshold
    )


  def iaaSThreeTierAppSimulation(): Unit =
    val simulation = CloudSim()
    val broker = DatacenterBrokerSimple(simulation)
    CustomDatacenterService.requestIaaSService(
      simulation,
      broker,
      CustomDatacenterService.Application.MapReduce,
      threeTierAppsToRun,
      threeTierCloudletLength,
      threeTierAppPe,
      threeTierCloudScalingEnabled,
      threeTierCpuOverloadThreshold,
      threeTierRamScalingFactor,
      threeTierRamUpperThreshold,
      threeTierRamLowerThreshold
    )

  def iaaSSimpleAppSimulation(): Unit =
    val simulation = CloudSim()
    val broker = DatacenterBrokerSimple(simulation)
    CustomDatacenterService.requestIaaSService(
      simulation,
      broker,
      CustomDatacenterService.Application.MapReduce,
      simpleAppsToRun,
      simpleCloudletLength,
      simpleAppPe,
      simpleCloudScalingEnabled,
      simpleCpuOverloadThreshold,
      simpleRamScalingFactor,
      simpleRamUpperThreshold,
      simpleRamLowerThreshold
    )


}
