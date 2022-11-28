package CloudOrg.Applications

import CloudOrg.HelperUtils.{ObtainConfigReference, utils}
import com.typesafe.config.Config
import org.cloudbus.cloudsim.cloudlets.network.NetworkCloudlet

object MapReduceJob {

  val config: Config = ObtainConfigReference("cloudOrganizationSimulations").get
  val mapReduceConfig: Config = config.getConfig("cloudOrganizationSimulations.mapReduce")

  val mapTaskLength: Int = mapReduceConfig.getInt("mapTaskLength")
  val mapTaskMemory: Int = mapReduceConfig.getInt("mapTaskMemory")

  val sendTaskMemory: Int = mapReduceConfig.getInt("sendTaskMemory")
  val receiveTaskMemory: Int = mapReduceConfig.getInt("receiveTaskMemory")

  val packetsToSend: Int = mapReduceConfig.getInt("packetsToSend")
  val packetBytes: Int = mapReduceConfig.getInt("packetBytes")

  val reduceTaskLength: Int = mapReduceConfig.getInt("reduceTaskLength")
  val reduceTaskMemory: Int = mapReduceConfig.getInt("reduceTaskMemory")

  def createMapReduceTasks(mapperOneCloudlet: NetworkCloudlet, mapperTwoCloudlet: NetworkCloudlet, mapperThreeCloudlet: NetworkCloudlet, reducerCloudlet: NetworkCloudlet): Unit =
    // map task
    utils.createExecTaskForNwCloudlet(mapperOneCloudlet, mapTaskLength, mapTaskMemory)
    utils.createSendTaskForNwCloudlet(mapperOneCloudlet, reducerCloudlet, sendTaskMemory, packetsToSend, packetBytes)

    utils.createExecTaskForNwCloudlet(mapperTwoCloudlet, mapTaskLength, mapTaskMemory)
    utils.createSendTaskForNwCloudlet(mapperTwoCloudlet, reducerCloudlet, sendTaskMemory, packetsToSend, packetBytes)

    utils.createExecTaskForNwCloudlet(mapperThreeCloudlet, mapTaskLength, mapTaskMemory)
    utils.createSendTaskForNwCloudlet(mapperThreeCloudlet, reducerCloudlet, sendTaskMemory, packetsToSend, packetBytes)

    // reduce task
    utils.createReceiveTaskForNwCloudlet(reducerCloudlet, mapperOneCloudlet, receiveTaskMemory, packetsToSend)
    utils.createReceiveTaskForNwCloudlet(reducerCloudlet, mapperTwoCloudlet, receiveTaskMemory, packetsToSend)
    utils.createReceiveTaskForNwCloudlet(reducerCloudlet, mapperThreeCloudlet, receiveTaskMemory, packetsToSend)
    utils.createExecTaskForNwCloudlet(reducerCloudlet, reduceTaskLength, reduceTaskMemory)
}
