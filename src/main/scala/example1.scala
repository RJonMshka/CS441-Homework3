import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.cloudlets.{Cloudlet, CloudletSimple}
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.{Datacenter, DatacenterSimple}
import org.cloudbus.cloudsim.hosts.{Host, HostSimple}
import org.cloudbus.cloudsim.resources.{Pe, PeSimple}
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic
import org.cloudbus.cloudsim.vms.{Vm, VmSimple}
import org.cloudsimplus.builders.tables.CloudletsTableBuilder
import scala.collection.*
import java.util
import scala.jdk.CollectionConverters.*
import java.util.*

object example1 {

  val hosts = 1
  val host_processing_elements = 8
  val host_mips = 1000
  val host_ram = 2048
  val host_bw = 10000l
  val host_storage = 1000000l

  val vms = 2
  val vm_pes = 4

  val cloudlets = 4
  val cloudlet_pes = 2
  val cloudlet_length = 10000

  def createList[T, X](size: Int, f: () => T): immutable.List[T] =
    val elm = f()
    size match
      case 0 => Nil
      case _ => elm::createList(size - 1, f)

  private def createDataCenter(simulation: CloudSim): Datacenter =
    val hostList: List[Host] = createList(hosts, createHost).asJava
    DatacenterSimple(simulation, hostList)

  private def createHost(): Host =
    val peList: List[Pe] = createList(host_processing_elements, () => ()).map(_ => PeSimple(host_mips)).asJava
    HostSimple(host_ram, host_bw, host_storage, peList)

  def createVm(): Vm =
    val vm = VmSimple(host_mips, vm_pes)
    vm.setRam(512).setBw(1000).setSize(10000)
    vm

  private def createVMs(): List[Vm] =
    val vmList: List[Vm] = createList(vms, createVm).asJava
    vmList

  def createCloudlet(): Cloudlet =
    val utilizationModel = UtilizationModelDynamic(0.5)
    val cloudlet = CloudletSimple(cloudlet_length, cloudlet_pes, utilizationModel)
    cloudlet.setSizes(1024)
    cloudlet

  private def createCloudlets(): List[Cloudlet] =
     val cloudletList: List[Cloudlet] = createList(cloudlets, createCloudlet).asJava
     cloudletList

  private def basicExample(): Unit =
    val simulation: CloudSim = CloudSim()
    val broker0 = DatacenterBrokerSimple(simulation)
    val dataCenter0 = createDataCenter(simulation)
    val vmList = createVMs()
    val cloudletList = createCloudlets()
    broker0.submitVmList(vmList)
    broker0.submitCloudletList(cloudletList)
    simulation.start()
    val finishedCloudlets = broker0.getCloudletFinishedList
    CloudletsTableBuilder(finishedCloudlets).build

  def main(args: Array[String]): Unit = {
    basicExample()
  }
}
