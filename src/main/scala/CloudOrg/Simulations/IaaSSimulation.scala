package CloudOrg.Simulations

import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.core.CloudSim
import CloudOrg.Datacenters.CustomDatacenterService

object IaaSSimulation {

  // variables for map reduce app on an iaas
  val mapReduceAppsToRun = 300
  val mapReduceCloudletLength = 1000
  val mapReduceAppPe = 2
  val mapReduceCloudScalingEnabled = true
  val mapReduceRamScalingFactor = 0.1
  val mapReduceCpuOverloadThreshold = 0.8
  val mapReduceRamUpperThreshold = 0.8
  val mapReduceRamLowerThreshold = 0.2

  // variables for three tier app on an iaas
  val threeTierAppsToRun = 30
  val threeTierCloudletLength = 1000
  val threeTierAppPe = 1
  val threeTierCloudScalingEnabled = true
  val threeTierRamScalingFactor = 0.2
  val threeTierCpuOverloadThreshold = 0.8
  val threeTierRamUpperThreshold = 0.9
  val threeTierRamLowerThreshold = 0.1

  // variables for simple (single simple cloudlet) app on an iaas
  val simpleAppsToRun = 100
  val simpleCloudletLength = 10000
  val simpleAppPe = 1
  val simpleCloudScalingEnabled = false
  val simpleRamScalingFactor = 0.2
  val simpleCpuOverloadThreshold = 0.8
  val simpleRamUpperThreshold = 0.9
  val simpleRamLowerThreshold = 0.1

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

  def main(args: Array[String]): Unit = {
    iaaSSimpleAppSimulation()
  }


}
