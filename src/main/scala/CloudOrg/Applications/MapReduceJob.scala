package CloudOrg.Applications

import CloudOrg.utils
import org.cloudbus.cloudsim.cloudlets.network.NetworkCloudlet

object MapReduceJob {

  val mapTaskLength = 1000
  val mapTaskMemory = 1024

  val sendTaskMemory = 512
  val receiveTaskMemory = 512

  val packetsToSend = 200
  val packetBytes = 1000

  val reduceTaskLength = 500
  val reduceTaskMemory = 512

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
