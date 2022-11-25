package CloudOrg.Applications

import java.util
import CloudOrg.utils
import org.cloudbus.cloudsim.cloudlets.network.NetworkCloudlet

import scala.collection.*
import scala.jdk.CollectionConverters.*

object BroadcastMessageJob {

  val sendMemory = 256
  val receiveMemory = 256
  val packetsToSend = 100
  val packetBytes = 1000

  def createBroadcastMessageTasks(sourceCloudlet: NetworkCloudlet, destCloudletList: util.List[NetworkCloudlet]): Unit =
    destCloudletList.asScala.foreach(cl => {
      utils.createSendTaskForNwCloudlet(sourceCloudlet, cl, sendMemory, packetsToSend, packetBytes)
      utils.createReceiveTaskForNwCloudlet(cl, sourceCloudlet, receiveMemory, packetsToSend)
    })
}
