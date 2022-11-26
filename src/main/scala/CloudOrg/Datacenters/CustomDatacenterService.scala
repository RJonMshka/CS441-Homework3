package CloudOrg.Datacenters

import CloudOrg.HelperUtils.CreateLogger
import CloudOrg.Datacenters.StarNetworkDatacenter
import CloudOrg.utils
import org.cloudbus.cloudsim.brokers.DatacenterBroker
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.hosts.Host
import org.cloudbus.cloudsim.vms.{Vm, VmCost}
import org.cloudbus.cloudsim.cloudlets.Cloudlet
import org.cloudsimplus.builders.tables.CloudletsTableBuilder

import java.util
import scala.jdk.CollectionConverters.*

object CustomDatacenterService {

  val logger = CreateLogger(classOf[CustomDatacenterService.type])

  // SAAS VARIABLES
  enum SaaSCloudletType:
    case Type1, Type2, Type3

  val saas_hosts_count = 9
  val saas_host_mips = 1000
  val saas_host_pe_count = 4
  val saas_host_ram = 16_384
  val saas_host_bw = 1000
  val saas_host_storage = 1_000_000l

  val saas_vm_count = 18
  val saas_vm_pe_count = 2
  val saas_vm_ram = 4000
  val saas_vm_bw = 1
  val saas_vm_size = 20_000

  val saas_type1_cloudlet_pe_count = 1
  val saas_type1_cloudlet_length = 1000
  val saas_type1_cloudlet_file_size = 200
  val saas_type1_cloudlet_output_size = 500

  val saas_type2_cloudlet_pe_count = 2
  val saas_type2_cloudlet_length = 500

  val saas_type3_cloudlet_pe_count = 2
  val saas_type3_cloudlet_length = 1000
  val saas_type3_cloudlet_file_size = 200
  val saas_type3_cloudlet_output_size = 500

  // Utilization
  val saas_cloudlet_cpu_utilization = 0.8
  val saas_cloudlet_ram_utilization = 0.5
  val saas_cloudlet_bw_utilization = 0.3
  val saas_cloudlet_initial_ram_utilization = 0.1
  val saas_cloudlet_max_ram_utilization = 0.8

  // cost - common
  val cost_per_sec = 0.001
  val cost_per_mem = 0.01
  val cost_per_storage = 0.0001
  val cost_per_bw = 0.01

  // scaling
  // horizontal scaling
  val saas_cpu_overload_threshold = 0.8
  // vertical ram scaling
  val saas_ram_scaling_factor = 0.1
  val saas_ram_upper_utilization_threshold = 0.8
  val saas_ram_lower_utilization_threshold = 0.3

  // allocation policy
  val allocationPolicyType = "SIMPLE"

  // vm scheduling
  val vmSchedulingType = "TIMESHARED"

  // cloudlet scheduling
  val cloudletSchedulingType = "TIMESHARED"


  // PAAS VARIABLES

  def requestSaaSSimulation(simulation: CloudSim, broker: DatacenterBroker, numberOfCloudlet: Int, cloudletType: SaaSCloudletType): Unit =
    val hostList = utils.createNwHostList(saas_hosts_count, saas_host_pe_count, saas_host_mips, saas_host_ram, saas_host_bw, saas_host_storage, utils.SchedulerType.TIMESHARED)

    val allocationPolicy = utils.getAllocationPolicy(allocationPolicyType)
    // We will be using star network datacenter for SAAS Simulations
    val datacenter = StarNetworkDatacenter(simulation, hostList, allocationPolicy)

    utils.setDatacenterCost(datacenter, cost_per_sec, cost_per_mem, cost_per_storage, cost_per_bw)

    val vmList = utils.createNwVmList(saas_vm_count, saas_host_mips, saas_vm_pe_count, saas_vm_ram, saas_vm_bw, saas_vm_size, utils.SchedulerType.SPACESHARED)
    vmList.asScala.foreach(vm => {
      utils.createHorizontalVmScaling(vm, saas_host_mips, saas_vm_pe_count, saas_vm_ram, saas_vm_bw, saas_vm_size, utils.SchedulerType.SPACESHARED, saas_cpu_overload_threshold)
      utils.createVerticalRamScalingForVm(vm, saas_ram_scaling_factor, saas_ram_upper_utilization_threshold, saas_ram_lower_utilization_threshold)
    })

    val cloudletList = cloudletType match
      case SaaSCloudletType.Type1 => utils.createCloudletList(numberOfCloudlet, saas_type1_cloudlet_length, saas_type1_cloudlet_pe_count, saas_type1_cloudlet_file_size, saas_type1_cloudlet_output_size, saas_cloudlet_cpu_utilization, saas_cloudlet_initial_ram_utilization, saas_cloudlet_max_ram_utilization, saas_cloudlet_bw_utilization)
      case SaaSCloudletType.Type2 => utils.createCloudletList(numberOfCloudlet, saas_type2_cloudlet_length, saas_type2_cloudlet_pe_count , saas_cloudlet_cpu_utilization, saas_cloudlet_initial_ram_utilization, saas_cloudlet_max_ram_utilization, saas_cloudlet_bw_utilization)
      case SaaSCloudletType.Type3 => utils.createCloudletList(numberOfCloudlet, saas_type3_cloudlet_length, saas_type3_cloudlet_pe_count, saas_type3_cloudlet_file_size, saas_type3_cloudlet_output_size, saas_cloudlet_cpu_utilization, saas_cloudlet_initial_ram_utilization, saas_cloudlet_max_ram_utilization, saas_cloudlet_bw_utilization)

    broker.submitVmList(vmList)
    broker.submitCloudletList(cloudletList)

    simulation.start

    val finishedCloudlets = broker.getCloudletFinishedList
    CloudletsTableBuilder(finishedCloudlets).build

    logger.info("<---------- HOSTS POWER AND CPU CONSUMPTION ------->")
    hostList.asScala.foreach(utils.printHostPowerConsumptionAndCpuUtilization)
    logger.info("<---------- VMS POWER AND CPU CONSUMPTION ------->")
    vmList.asScala.foreach(utils.printVmPowerConsumptionAndCpuUtilization)
    logger.info("<-------- RESOURCE BILLING INFORMATION ------------------>")
    utils.printTotalCostForVms(broker)


}
