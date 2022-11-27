package CloudOrg.Applications

import CloudOrg.HelperUtils.{ObtainConfigReference, utils}
import org.cloudbus.cloudsim.cloudlets.network.NetworkCloudlet

object MapReduceJob {

  val config = ObtainConfigReference("cloudOrganizationSimulations").get
  val mapReduceConfig = config.getConfig("cloudOrganizationSimulations.mapReduce")

  val mapTaskLength = mapReduceConfig.getInt("mapTaskLength")
  val mapTaskMemory = mapReduceConfig.getInt("mapTaskMemory")

  val sendTaskMemory = mapReduceConfig.getInt("sendTaskMemory")
  val receiveTaskMemory = mapReduceConfig.getInt("receiveTaskMemory")

  val packetsToSend = mapReduceConfig.getInt("packetsToSend")
  val packetBytes = mapReduceConfig.getInt("packetBytes")

  val reduceTaskLength = mapReduceConfig.getInt("reduceTaskLength")
  val reduceTaskMemory = mapReduceConfig.getInt("reduceTaskMemory")

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
