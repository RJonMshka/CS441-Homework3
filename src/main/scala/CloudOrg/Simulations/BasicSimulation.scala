package CloudOrg.Simulations

import CloudOrg.HelperUtils.{CreateLogger, ObtainConfigReference, utils}
import org.cloudbus.cloudsim.brokers.DatacenterBroker
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.DatacenterSimple
import org.cloudbus.cloudsim.hosts.HostSimple
import org.cloudsimplus.builders.tables.CloudletsTableBuilder

object BasicSimulation {
  
  val logger = CreateLogger(classOf[BasicSimulation.type])
  val config = ObtainConfigReference("cloudOrganizationSimulations").get
  val basicSimConfig = config.getConfig("cloudOrganizationSimulations.basic")

  val hosts_count = basicSimConfig.getInt("hosts_count")
  val host_mips = basicSimConfig.getInt("host_mips")
  val host_pe_count = basicSimConfig.getInt("host_pe_count")
  val host_ram = basicSimConfig.getInt("host_ram")
  val host_bw = basicSimConfig.getInt("host_bw")
  val host_storage = basicSimConfig.getInt("host_storage")

  val vm_count = basicSimConfig.getInt("vm_count")
  val vm_pe_count = basicSimConfig.getInt("vm_pe_count")

  val cloudlet_count = basicSimConfig.getInt("cloudlet_count")
  val cloudlet_pe_count = basicSimConfig.getInt("cloudlet_pe_count")
  val cloudlet_length = basicSimConfig.getInt("cloudlet_length")
  val cloudlet_file_size = basicSimConfig.getInt("cloudlet_file_size")
  val cloudlet_output_size = basicSimConfig.getInt("cloudlet_output_size")

  // datacenter allocation Policy
  val allocationPolicyType = basicSimConfig.getString("allocationPolicyType")
  // vm scheduling policy
  val vmSchedulerType = basicSimConfig.getString("vmSchedulerType")

  def startSimulation(): Unit = {
    logger.info("BASIC EXAMPLE SIMULATION")
    val simulation:CloudSim = CloudSim()
    val broker: DatacenterBroker = utils.createBroker(simulation)
    val hostList = utils.createHostList(hosts_count, host_pe_count, host_mips, host_ram, host_bw, host_storage, vmSchedulerType)
    DatacenterSimple(simulation, hostList, utils.getAllocationPolicy(allocationPolicyType))
    val vmList = utils.createVmList(vm_count, host_mips, vm_pe_count)
    val cloudletList = utils.createCloudletList(cloudlet_count, cloudlet_length, cloudlet_pe_count)

    broker.submitVmList(vmList)
    broker.submitCloudletList(cloudletList)

    simulation.start

    val finishedCloudlets = broker.getCloudletFinishedList
    CloudletsTableBuilder(finishedCloudlets).build
  }

}
