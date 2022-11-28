package CloudOrg.Simulations

import CloudOrg.HelperUtils.{CreateLogger, ObtainConfigReference, utils}
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple
import org.cloudbus.cloudsim.brokers.{DatacenterBrokerHeuristic, DatacenterBrokerSimple}
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudsimplus.listeners.EventListener

import scala.util.Random
import scala.jdk.CollectionConverters.*

object CommonTopologySimpleCloudletSimulation {
  val logger = CreateLogger(classOf[CommonTopologySimpleCloudletSimulation.type])
  val config = ObtainConfigReference("cloudOrganizationSimulations").get
  val commonConfig = config.getConfig("cloudOrganizationSimulations.commonSimulation")
  val simpleConfig = config.getConfig("cloudOrganizationSimulations.simpleCloudlet")
  val treeNetworkConfig = config.getConfig("cloudOrganizationSimulations.treeTopology")

  val tree_count = treeNetworkConfig.getInt("tree_count")
  val hosts_count = commonConfig.getInt("hosts_count")
  val host_mips = commonConfig.getInt("host_mips")
  val host_pe_count = commonConfig.getInt("host_pe_count")
  val host_ram = commonConfig.getInt("host_ram")
  val host_bw = commonConfig.getInt("host_bw")
  val host_storage = commonConfig.getInt("host_storage")

  val vm_count = commonConfig.getInt("vm_count")
  val vm_pe_count = commonConfig.getInt("vm_pe_count")
  val vm_ram = commonConfig.getInt("vm_ram")
  val vm_bw = commonConfig.getInt("vm_bw")
  val vm_size = commonConfig.getInt("vm_size")

  val cloudlet_count = simpleConfig.getInt("app_count")
  val cloudlet_pe_count = commonConfig.getInt("cloudlet_pe_count")
  val cloudlet_length = commonConfig.getInt("cloudlet_length")
  val cloudlet_file_size = commonConfig.getInt("cloudlet_file_size")
  val cloudlet_output_size = commonConfig.getInt("cloudlet_output_size")


  // Utilization
  val cloudlet_cpu_utilization = commonConfig.getDouble("cloudlet_cpu_utilization")
  val cloudlet_ram_utilization = commonConfig.getDouble("cloudlet_ram_utilization")
  val cloudlet_bw_utilization = commonConfig.getDouble("cloudlet_bw_utilization")
  val cloudlet_initial_ram_utilization = commonConfig.getDouble("cloudlet_initial_ram_utilization")
  val cloudlet_max_ram_utilization = commonConfig.getDouble("cloudlet_max_ram_utilization")

  // cost
  val cost_per_sec = commonConfig.getDouble("cost_per_sec")
  val cost_per_mem = commonConfig.getDouble("cost_per_mem")
  val cost_per_storage = commonConfig.getDouble("cost_per_storage")
  val cost_per_bw = commonConfig.getDouble("cost_per_bw")

  // scaling
  // horizontal scaling
  val cpu_overload_threshold = commonConfig.getDouble("cpu_overload_threshold")
  // vertical ram scaling
  val ram_scaling_factor = commonConfig.getDouble("ram_scaling_factor")
  val ram_upper_utilization_threshold = commonConfig.getDouble("ram_upper_utilization_threshold")
  val ram_lower_utilization_threshold = commonConfig.getDouble("ram_lower_utilization_threshold")

  // datacenter allocation Policy
  val allocationPolicyType = simpleConfig.getString("allocationPolicyType")
  // vm scheduling policy
  val vmSchedulerType = simpleConfig.getString("vmSchedulerType")
  // cloudlet scheduling policy
  val cloudletSchedulerType = simpleConfig.getString("cloudletSchedulerType")

  /**
   * This method starts the simulation
   * @param datacenterType - type of datacenter to be created for simulation
   */
  def startSimulation(datacenterType: utils.NetworkDatacenterType): Unit =
    logger.info("SIMPLE CLOUDLET SIMULATIONS")
    val simulation: CloudSim = CloudSim()
    val broker = DatacenterBrokerSimple(simulation)
    val hostList = utils.createNwHostList(hosts_count, host_pe_count, host_mips, host_ram, host_bw, host_storage, vmSchedulerType)

    // allocation policy
    val allocationPolicy = utils.getAllocationPolicy(allocationPolicyType)

    val datacenter = utils.createNwDatacenter(datacenterType, simulation, hostList, allocationPolicy, tree_count)
    utils.setDatacenterCost(datacenter, cost_per_sec, cost_per_mem, cost_per_storage, cost_per_bw)

    val vmList = utils.createNwVmList(vm_count, host_mips, vm_pe_count, vm_ram, vm_bw, vm_size, cloudletSchedulerType)

    // Set Auto scaling
    vmList.asScala.foreach(vm => {
      utils.createHorizontalVmScaling(vm, host_mips, vm_pe_count, vm_ram, vm_bw, vm_size, cloudletSchedulerType, cpu_overload_threshold)
      utils.createVerticalRamScalingForVm(vm, ram_scaling_factor, ram_upper_utilization_threshold, ram_lower_utilization_threshold)
    })
    val cloudletList = utils.createCloudletList(cloudlet_count, cloudlet_length, cloudlet_pe_count, cloudlet_cpu_utilization, cloudlet_initial_ram_utilization, cloudlet_max_ram_utilization, cloudlet_bw_utilization)

    // submit vms, cloudlets, performs simulation and prints the result
    broker.submitVmList(vmList)
    broker.submitCloudletList(cloudletList)
    simulation.start
    utils.buildTableAndPrintResults(broker, vmList, hostList)

}
