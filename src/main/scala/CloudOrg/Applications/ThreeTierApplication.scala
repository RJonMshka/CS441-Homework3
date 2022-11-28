package CloudOrg.Applications

import CloudOrg.HelperUtils.{ObtainConfigReference, utils}
import com.typesafe.config.Config
import org.cloudbus.cloudsim.cloudlets.network.NetworkCloudlet

object ThreeTierApplication {
  val config: Config = ObtainConfigReference("cloudOrganizationSimulations").get
  val threeTierAppConfig: Config = config.getConfig("cloudOrganizationSimulations.threeTier")

  val firstTaskExecLength: Int = threeTierAppConfig.getInt("firstTaskExecLength")
  val firstTaskExecMemoryConsumption: Int = threeTierAppConfig.getInt("firstTaskExecMemoryConsumption")
  val firstTaskSendMemoryConsumption: Int = threeTierAppConfig.getInt("firstTaskSendMemoryConsumption")
  val firstTaskReceiveMemoryConsumption: Int = threeTierAppConfig.getInt("firstTaskReceiveMemoryConsumption")
  val firstTaskPacketsToSend: Int = threeTierAppConfig.getInt("firstTaskPacketsToSend")
  val firstTaskPacketBytes: Int = threeTierAppConfig.getInt("firstTaskPacketBytes")

  val secondTaskExecLength: Int = threeTierAppConfig.getInt("secondTaskExecLength")
  val secondTaskExecMemoryConsumption: Int = threeTierAppConfig.getInt("secondTaskExecMemoryConsumption")
  val secondTaskSendToFirstMemoryConsumption: Int = threeTierAppConfig.getInt("secondTaskSendToFirstMemoryConsumption")
  val secondTaskReceiveFromFirstMemoryConsumption: Int = threeTierAppConfig.getInt("secondTaskReceiveFromFirstMemoryConsumption")
  val secondTaskSendToThirdMemoryConsumption: Int = threeTierAppConfig.getInt("secondTaskSendToThirdMemoryConsumption")
  val secondTaskReceiveFromThirdMemoryConsumption: Int = threeTierAppConfig.getInt("secondTaskReceiveFromThirdMemoryConsumption")
  val secondTaskPacketsToSendToFirst: Int = threeTierAppConfig.getInt("secondTaskPacketsToSendToFirst")
  val secondTaskPacketBytesSendToFirst: Int = threeTierAppConfig.getInt("secondTaskPacketBytesSendToFirst")
  val secondTaskPacketsToSendToThird: Int = threeTierAppConfig.getInt("secondTaskPacketsToSendToThird")
  val secondTaskPacketBytesSendToThird: Int = threeTierAppConfig.getInt("secondTaskPacketBytesSendToThird")

  val thirdTaskExecLength: Int = threeTierAppConfig.getInt("thirdTaskExecLength")
  val thirdTaskExecMemoryConsumption: Int = threeTierAppConfig.getInt("thirdTaskExecMemoryConsumption")
  val thirdTaskSendMemoryConsumption: Int = threeTierAppConfig.getInt("thirdTaskSendMemoryConsumption")
  val thirdTaskReceiveMemoryConsumption: Int = threeTierAppConfig.getInt("thirdTaskReceiveMemoryConsumption")
  val thirdTaskPacketsToSend: Int = threeTierAppConfig.getInt("thirdTaskPacketsToSend")
  val thirdTaskPacketBytes: Int = threeTierAppConfig.getInt("thirdTaskPacketBytes")

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
