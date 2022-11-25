package CloudOrg.Applications

import CloudOrg.utils
import org.cloudbus.cloudsim.cloudlets.network.NetworkCloudlet

object ThreeTierApplication {

  val firstTaskExecLength = 500
  val firstTaskExecMemoryConsumption = 512
  val firstTaskSendMemoryConsumption = 256
  val firstTaskReceiveMemoryConsumption = 256
  val firstTaskPacketsToSend = 50
  val firstTaskPacketBytes = 1000

  val secondTaskExecLength = 2000
  val secondTaskExecMemoryConsumption = 512
  val secondTaskSendToFirstMemoryConsumption = 256
  val secondTaskReceiveFromFirstMemoryConsumption = 256
  val secondTaskSendToThirdMemoryConsumption = 512
  val secondTaskReceiveFromThirdMemoryConsumption = 512
  val secondTaskPacketsToSendToFirst = 10
  val secondTaskPacketBytesSendToFirst = 1000
  val secondTaskPacketsToSendToThird = 100
  val secondTaskPacketBytesSendToThird = 1000

  val thirdTaskExecLength = 500
  val thirdTaskExecMemoryConsumption = 512
  val thirdTaskSendMemoryConsumption = 256
  val thirdTaskReceiveMemoryConsumption = 256
  val thirdTaskPacketsToSend = 50
  val thirdTaskPacketBytes = 1000

  def createAppWorkFlow(firstCloudlet: NetworkCloudlet, secondCloudlet: NetworkCloudlet, thirdCloudlet: NetworkCloudlet): Unit =
    // First cloudlet tasks
    utils.createExecTaskForNwCloudlet(firstCloudlet, firstTaskExecLength, firstTaskExecMemoryConsumption)
    utils.createSendTaskForNwCloudlet(firstCloudlet, secondCloudlet, firstTaskSendMemoryConsumption, firstTaskPacketsToSend, firstTaskPacketBytes)
    utils.createReceiveTaskForNwCloudlet(firstCloudlet, secondCloudlet, firstTaskReceiveMemoryConsumption, secondTaskPacketsToSendToFirst)

    // second cloudlet tasks
    utils.createExecTaskForNwCloudlet(secondCloudlet, secondTaskExecLength, secondTaskExecMemoryConsumption)
    utils.createReceiveTaskForNwCloudlet(secondCloudlet, firstCloudlet, secondTaskReceiveFromFirstMemoryConsumption, firstTaskPacketsToSend)
    utils.createSendTaskForNwCloudlet(secondCloudlet, thirdCloudlet, secondTaskSendToThirdMemoryConsumption, secondTaskPacketsToSendToThird, secondTaskPacketBytesSendToThird)
    utils.createReceiveTaskForNwCloudlet(secondCloudlet, thirdCloudlet, secondTaskReceiveFromThirdMemoryConsumption, thirdTaskPacketsToSend)
    utils.createSendTaskForNwCloudlet(secondCloudlet, firstCloudlet, secondTaskSendToFirstMemoryConsumption, secondTaskPacketsToSendToFirst, secondTaskPacketBytesSendToFirst)

    // third cloudlet task
    utils.createReceiveTaskForNwCloudlet(thirdCloudlet, secondCloudlet, thirdTaskReceiveMemoryConsumption, secondTaskPacketsToSendToThird)
    utils.createExecTaskForNwCloudlet(thirdCloudlet, thirdTaskExecLength, thirdTaskExecMemoryConsumption)
    utils.createSendTaskForNwCloudlet(thirdCloudlet, secondCloudlet, thirdTaskSendMemoryConsumption, thirdTaskPacketsToSend, thirdTaskPacketBytes)
}
