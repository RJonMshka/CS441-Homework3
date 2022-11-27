package CloudOrg.Applications

import CloudOrg.HelperUtils.{ObtainConfigReference, utils}
import org.cloudbus.cloudsim.cloudlets.network.NetworkCloudlet

object ThreeTierApplication {
  val config = ObtainConfigReference("cloudOrganizationSimulations").get
  val threeTierAppConfig = config.getConfig("cloudOrganizationSimulations.threeTier")

  val firstTaskExecLength = threeTierAppConfig.getInt("firstTaskExecLength")
  val firstTaskExecMemoryConsumption = threeTierAppConfig.getInt("firstTaskExecMemoryConsumption")
  val firstTaskSendMemoryConsumption = threeTierAppConfig.getInt("firstTaskSendMemoryConsumption")
  val firstTaskReceiveMemoryConsumption = threeTierAppConfig.getInt("firstTaskReceiveMemoryConsumption")
  val firstTaskPacketsToSend = threeTierAppConfig.getInt("firstTaskPacketsToSend")
  val firstTaskPacketBytes = threeTierAppConfig.getInt("firstTaskPacketBytes")

  val secondTaskExecLength = threeTierAppConfig.getInt("secondTaskExecLength")
  val secondTaskExecMemoryConsumption = threeTierAppConfig.getInt("secondTaskExecMemoryConsumption")
  val secondTaskSendToFirstMemoryConsumption = threeTierAppConfig.getInt("secondTaskSendToFirstMemoryConsumption")
  val secondTaskReceiveFromFirstMemoryConsumption = threeTierAppConfig.getInt("secondTaskReceiveFromFirstMemoryConsumption")
  val secondTaskSendToThirdMemoryConsumption = threeTierAppConfig.getInt("secondTaskSendToThirdMemoryConsumption")
  val secondTaskReceiveFromThirdMemoryConsumption = threeTierAppConfig.getInt("secondTaskReceiveFromThirdMemoryConsumption")
  val secondTaskPacketsToSendToFirst = threeTierAppConfig.getInt("secondTaskPacketsToSendToFirst")
  val secondTaskPacketBytesSendToFirst = threeTierAppConfig.getInt("secondTaskPacketBytesSendToFirst")
  val secondTaskPacketsToSendToThird = threeTierAppConfig.getInt("secondTaskPacketsToSendToThird")
  val secondTaskPacketBytesSendToThird = threeTierAppConfig.getInt("secondTaskPacketBytesSendToThird")

  val thirdTaskExecLength = threeTierAppConfig.getInt("thirdTaskExecLength")
  val thirdTaskExecMemoryConsumption = threeTierAppConfig.getInt("thirdTaskExecMemoryConsumption")
  val thirdTaskSendMemoryConsumption = threeTierAppConfig.getInt("thirdTaskSendMemoryConsumption")
  val thirdTaskReceiveMemoryConsumption = threeTierAppConfig.getInt("thirdTaskReceiveMemoryConsumption")
  val thirdTaskPacketsToSend = threeTierAppConfig.getInt("thirdTaskPacketsToSend")
  val thirdTaskPacketBytes = threeTierAppConfig.getInt("thirdTaskPacketBytes")

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
