package CloudOrg.Simulations

import CloudOrg.HelperUtils.CreateLogger
import org.cloudbus.cloudsim.brokers.DatacenterBroker
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.hosts.HostSimple
import CloudOrg.utils
import org.cloudsimplus.builders.tables.CloudletsTableBuilder

object BasicSimulation {
  
  val logger = CreateLogger(classOf[BasicSimulation.type])
  val hosts_count = 4
  val host_mips = 1000
  val host_pe_count = 8
  val host_ram = 16_384
  val host_bw = 10_000l
  val host_storage = 1_000_000l

  val vm_count = 8
  val vm_pe_count = 2

  val cloudlet_count = 10
  val cloudlet_pe_count = 2
  val cloudlet_length = 10000
  val cloudlet_file_size = 200
  val cloudlet_output_size = 500

  def main(args: Array[String]): Unit = {
    val simulation:CloudSim = CloudSim()
    val broker: DatacenterBroker = utils.createBroker(simulation)
    val hostList = utils.createHostList(hosts_count, host_pe_count, host_mips, host_ram, host_bw, host_storage, utils.SchedulerType.TIMESHARED)
    utils.createDataCenter(simulation, hostList)
    val vmList = utils.createVmList(vm_count, host_mips, vm_pe_count)
    val cloudletList = utils.createCloudletList(cloudlet_count, cloudlet_length, cloudlet_pe_count)

    broker.submitVmList(vmList)
    broker.submitCloudletList(cloudletList)

    simulation.start

    val finishedCloudlets = broker.getCloudletFinishedList
    CloudletsTableBuilder(finishedCloudlets).build
  }

}
