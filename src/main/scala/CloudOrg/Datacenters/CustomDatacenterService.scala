package CloudOrg.Datacenters

import CloudOrg.Applications.{MapReduceJob, ThreeTierApplication}
import CloudOrg.HelperUtils.{CreateLogger, utils}
import CloudOrg.Datacenters.{HybridNetworkDatacenter, RingNetworkDatacenter, StarNetworkDatacenter, TreeNetworkDatacenter}
import org.cloudbus.cloudsim.brokers.DatacenterBroker
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.hosts.Host
import org.cloudbus.cloudsim.vms.{Vm, VmCost}
import org.cloudbus.cloudsim.cloudlets.Cloudlet
import org.cloudbus.cloudsim.cloudlets.network.NetworkCloudlet
import org.cloudsimplus.builders.tables.CloudletsTableBuilder

import java.util
import scala.jdk.CollectionConverters.*

object CustomDatacenterService {

  val logger = CreateLogger(classOf[CustomDatacenterService.type])

  // App specific variables
  // map reduce total cloudlet for a single map reduce job
  val map_reduce_cloudlet_count = 4

  // three tier total cloudlet for a single instance of app
  val three_tier_cloudlet_count = 3

  enum Application:
    case MapReduce, ThreeTier, Simple

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

  // Type 1 cloudlet attributes
  val saas_type1_cloudlet_pe_count = 1
  val saas_type1_cloudlet_length = 1000
  val saas_type1_cloudlet_file_size = 200
  val saas_type1_cloudlet_output_size = 500

  // Type 2 cloudlet attributes
  val saas_type2_cloudlet_pe_count = 2
  val saas_type2_cloudlet_length = 500

  // Type 3 cloudlet attributes
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
  val saas_cost_per_sec = 0.001
  val saas_cost_per_mem = 0.01
  val saas_cost_per_storage = 0.0001
  val saas_cost_per_bw = 0.01

  // scaling
  // horizontal scaling
  val saas_cpu_overload_threshold = 0.8
  // vertical ram scaling
  val saas_ram_scaling_factor = 0.1
  val saas_ram_upper_utilization_threshold = 0.8
  val saas_ram_lower_utilization_threshold = 0.3

  // allocation policy
  val saasAllocationPolicyType = "SIMPLE"

  // vm scheduling
  val saasVmSchedulingType = "TIMESHARED"

  // cloudlet scheduling
  val saasCloudletSchedulingType = "TIMESHARED"


  // PAAS VARIABLES
  val paas_tree_size = 3

  val paas_hosts_count = 9
  val paas_host_mips = 1000
  val paas_host_pe_count = 4
  val paas_host_ram = 16_384
  val paas_host_bw = 1000
  val paas_host_storage = 1_000_000l

  val paas_vm_count = 18
  val paas_vm_pe_count = 2
  val paas_vm_ram = 4000
  val paas_vm_bw = 1
  val paas_vm_size = 20_000

  // Utilization
  val paas_cloudlet_cpu_utilization = 0.8
  val paas_cloudlet_ram_utilization = 0.5
  val paas_cloudlet_bw_utilization = 0.3
  val paas_cloudlet_initial_ram_utilization = 0.1
  val paas_cloudlet_max_ram_utilization = 0.8

  // cost
  val paas_cost_per_sec = 0.001
  val paas_cost_per_mem = 0.01
  val paas_cost_per_storage = 0.0001
  val paas_cost_per_bw = 0.01

  // scaling
  // horizontal scaling
  val paas_cpu_overload_threshold = 0.8
  // vertical ram scaling
  val paas_ram_scaling_factor = 0.1
  val paas_ram_upper_utilization_threshold = 0.8
  val paas_ram_lower_utilization_threshold = 0.3

  // allocation policy
  val paasAllocationPolicyType = "SIMPLE"

  // vm scheduling
  val paasVmSchedulingType = "TIMESHARED"

  // cloudlet scheduling
  val paasCloudletSchedulingType = "TIMESHARED"

  // IAAS Variables
  val iaas_hosts_count = 9
  val iaas_host_mips = 1000
  val iaas_host_pe_count = 4
  val iaas_host_ram = 16_384
  val iaas_host_bw = 1000
  val iaas_host_storage = 1_000_000l

  val iaas_vm_count = 18
  val iaas_vm_pe_count = 2
  val iaas_vm_ram = 4000
  val iaas_vm_bw = 1
  val iaas_vm_size = 20_000

  // Utilization
  val iaas_cloudlet_cpu_utilization = 0.8
  val iaas_cloudlet_ram_utilization = 0.5
  val iaas_cloudlet_bw_utilization = 0.3
  val iaas_cloudlet_initial_ram_utilization = 0.1
  val iaas_cloudlet_max_ram_utilization = 0.8

  // cost
  val iaas_cost_per_sec = 0.001
  val iaas_cost_per_mem = 0.01
  val iaas_cost_per_storage = 0.0001
  val iaas_cost_per_bw = 0.01

  // allocation policy
  val iaasAllocationPolicyType = "SIMPLE"

  // vm scheduling
  val iaasVmSchedulingType = "TIMESHARED"

  // cloudlet scheduling
  val iaasCloudletSchedulingType = "TIMESHARED"

  // FAAS Variables
  val faas_hosts_count = 9
  val faas_host_mips = 1000
  val faas_host_pe_count = 4
  val faas_host_ram = 8000
  val faas_host_bw = 1000
  val faas_host_storage = 100000

  val faas_vm_count = 36
  val faas_vm_pe_count = 1
  val faas_vm_ram = 512
  val faas_vm_bw = 1
  val faas_vm_size = 512

  val faas_max_cloudlet_length = 500
  val faas_max_cloudlet_pe = 1
  val faas_max_cloudlet_file_size = 200
  val faas_max_cloudlet_output_file_size = 200

  // Utilization
  val faas_cloudlet_cpu_utilization = 0.8
  val faas_cloudlet_ram_utilization = 0.5
  val faas_cloudlet_bw_utilization = 0.3
  val faas_cloudlet_initial_ram_utilization = 0.1
  val faas_cloudlet_max_ram_utilization = 0.8

  // scaling
  // horizontal scaling
  val faas_cpu_overload_threshold = 0.8
  // vertical ram scaling
  val faas_ram_scaling_factor = 0.1
  val faas_ram_upper_utilization_threshold = 0.8
  val faas_ram_lower_utilization_threshold = 0.3

  // cost
  val faas_cost_per_sec = 0.001
  val faas_cost_per_mem = 0.01
  val faas_cost_per_storage = 0.0001
  val faas_cost_per_bw = 0.01

  // allocation policy
  val faasAllocationPolicyType = "SIMPLE"

  // vm scheduling
  val faasVmSchedulingType = "TIMESHARED"

  // cloudlet scheduling
  val faasCloudletSchedulingType = "TIMESHARED"


  def requestSaaSSimulation(simulation: CloudSim, broker: DatacenterBroker, numberOfCloudlet: Int, cloudletType: SaaSCloudletType): Unit =
    val hostList = utils.createNwHostList(saas_hosts_count, saas_host_pe_count, saas_host_mips, saas_host_ram, saas_host_bw, saas_host_storage, utils.SchedulerType.TIMESHARED)

    val allocationPolicy = utils.getAllocationPolicy(saasAllocationPolicyType)
    // We will be using star network datacenter for SAAS Simulations
    val datacenter = StarNetworkDatacenter(simulation, hostList, allocationPolicy)

    utils.setDatacenterCost(datacenter, saas_cost_per_sec, saas_cost_per_mem, saas_cost_per_storage, saas_cost_per_bw)

    val vmList = utils.createNwVmList(saas_vm_count, saas_host_mips, saas_vm_pe_count, saas_vm_ram, saas_vm_bw, saas_vm_size, utils.SchedulerType.SPACESHARED)
    vmList.asScala.foreach(vm => {
      utils.createHorizontalVmScaling(vm, saas_host_mips, saas_vm_pe_count, saas_vm_ram, saas_vm_bw, saas_vm_size, utils.SchedulerType.SPACESHARED, saas_cpu_overload_threshold)
      utils.createVerticalRamScalingForVm(vm, saas_ram_scaling_factor, saas_ram_upper_utilization_threshold, saas_ram_lower_utilization_threshold)
    })

    val cloudletList = cloudletType match
      case SaaSCloudletType.Type1 => utils.createCloudletList(numberOfCloudlet, saas_type1_cloudlet_length, saas_type1_cloudlet_pe_count, saas_type1_cloudlet_file_size, saas_type1_cloudlet_output_size, saas_cloudlet_cpu_utilization, saas_cloudlet_initial_ram_utilization, saas_cloudlet_max_ram_utilization, saas_cloudlet_bw_utilization)
      case SaaSCloudletType.Type2 => utils.createCloudletList(numberOfCloudlet, saas_type2_cloudlet_length, saas_type2_cloudlet_pe_count , saas_cloudlet_cpu_utilization, saas_cloudlet_initial_ram_utilization, saas_cloudlet_max_ram_utilization, saas_cloudlet_bw_utilization)
      case SaaSCloudletType.Type3 => utils.createCloudletList(numberOfCloudlet, saas_type3_cloudlet_length, saas_type3_cloudlet_pe_count, saas_type3_cloudlet_file_size, saas_type3_cloudlet_output_size, saas_cloudlet_cpu_utilization, saas_cloudlet_initial_ram_utilization, saas_cloudlet_max_ram_utilization, saas_cloudlet_bw_utilization)

    // submit vms, cloudlets, performs simulation and prints the result
    broker.submitVmList(vmList)
    broker.submitCloudletList(cloudletList)
    simulation.start
    utils.buildTableAndPrintResults(broker, vmList, hostList)
  
  def requestMapReducePaaSSimulation(simulation: CloudSim, broker: DatacenterBroker, numberOfAppCloudlets: Int, cloudletLength: Int, cloudletPe: Int): Unit =
    val hostList = utils.createNwHostList(paas_hosts_count, paas_host_pe_count, paas_host_mips, paas_host_ram, paas_host_bw, paas_host_storage, utils.SchedulerType.TIMESHARED)

    val allocationPolicy = utils.getAllocationPolicy(paasAllocationPolicyType)
    // We will be using tree network datacenter for PAAS Simulations
    val datacenter = TreeNetworkDatacenter(simulation, hostList, allocationPolicy, paas_tree_size)

    utils.setDatacenterCost(datacenter, paas_cost_per_sec, paas_cost_per_mem, paas_cost_per_storage, paas_cost_per_bw)

    val vmList = utils.createNwVmList(paas_vm_count, paas_host_mips, paas_vm_pe_count, paas_vm_ram, paas_vm_bw, paas_vm_size, utils.SchedulerType.SPACESHARED)
    vmList.asScala.foreach(vm => {
      utils.createHorizontalVmScaling(vm, paas_host_mips, paas_vm_pe_count, paas_vm_ram, paas_vm_bw, paas_vm_size, utils.SchedulerType.SPACESHARED, paas_cpu_overload_threshold)
      utils.createVerticalRamScalingForVm(vm, paas_ram_scaling_factor, paas_ram_upper_utilization_threshold, paas_ram_lower_utilization_threshold)
    })

    val cloudletList = utils.createNwCloudletList(map_reduce_cloudlet_count * numberOfAppCloudlets, cloudletLength, cloudletPe, vmList, paas_cloudlet_cpu_utilization, paas_cloudlet_initial_ram_utilization, paas_cloudlet_max_ram_utilization, paas_cloudlet_bw_utilization)
    Range(0, numberOfAppCloudlets).map(i => {
      MapReduceJob.createMapReduceTasks(cloudletList.get(map_reduce_cloudlet_count * i), cloudletList.get((map_reduce_cloudlet_count * i) + 1), cloudletList.get((map_reduce_cloudlet_count * i) + 2), cloudletList.get((map_reduce_cloudlet_count * i) + 3))
    })
    // submit vms, cloudlets, performs simulation and prints the result
    broker.submitVmList(vmList)
    broker.submitCloudletList(cloudletList)
    simulation.start
    utils.buildTableAndPrintResults(broker, vmList, hostList)

  def requestThreeTierPaaSService(simulation: CloudSim, broker: DatacenterBroker, numberOfAppCloudlets: Int, cloudletLength: Int, cloudletPe: Int): Unit =
    val hostList = utils.createNwHostList(paas_hosts_count, paas_host_pe_count, paas_host_mips, paas_host_ram, paas_host_bw, paas_host_storage, utils.SchedulerType.TIMESHARED)

    val allocationPolicy = utils.getAllocationPolicy(paasAllocationPolicyType)
    // We will be using Ring network datacenter for PAAS Simulations
    val datacenter = RingNetworkDatacenter(simulation, hostList, allocationPolicy)

    utils.setDatacenterCost(datacenter, paas_cost_per_sec, paas_cost_per_mem, paas_cost_per_storage, paas_cost_per_bw)

    val vmList = utils.createNwVmList(paas_vm_count, paas_host_mips, paas_vm_pe_count, paas_vm_ram, paas_vm_bw, paas_vm_size, utils.SchedulerType.SPACESHARED)
    vmList.asScala.foreach(vm => {
      utils.createHorizontalVmScaling(vm, paas_host_mips, paas_vm_pe_count, paas_vm_ram, paas_vm_bw, paas_vm_size, utils.SchedulerType.SPACESHARED, paas_cpu_overload_threshold)
      utils.createVerticalRamScalingForVm(vm, paas_ram_scaling_factor, paas_ram_upper_utilization_threshold, paas_ram_lower_utilization_threshold)
    })

    val cloudletList = utils.createNwCloudletList(three_tier_cloudlet_count * numberOfAppCloudlets, cloudletLength, cloudletPe, vmList, paas_cloudlet_cpu_utilization, paas_cloudlet_initial_ram_utilization, paas_cloudlet_max_ram_utilization, paas_cloudlet_bw_utilization)
    Range(0, numberOfAppCloudlets).map(i => {
      ThreeTierApplication.createAppWorkFlow(cloudletList.get(three_tier_cloudlet_count * i), cloudletList.get((three_tier_cloudlet_count * i) + 1), cloudletList.get((three_tier_cloudlet_count * i) + 2))
    })
    // submit vms, cloudlets, performs simulation and prints the result
    broker.submitVmList(vmList)
    broker.submitCloudletList(cloudletList)
    simulation.start
    utils.buildTableAndPrintResults(broker, vmList, hostList)

  def requestIaaSService(simulation: CloudSim, broker: DatacenterBroker, application: Application, appCloudletCount: Int, cloudletLength: Int, cloudletPe: Int, needScaling: Boolean, cpuOverloadThreshold: Double, ramScalingFactor: Double, ramUpperUtilizationThreshold: Double, ramLowerUtilizationThreshold: Double): Unit =
    val hostList = utils.createNwHostList(
      iaas_hosts_count,
      iaas_host_pe_count,
      iaas_host_mips,
      iaas_host_ram,
      iaas_host_bw,
      iaas_host_storage,
      utils.SchedulerType.TIMESHARED
    )

    val allocationPolicy = utils.getAllocationPolicy(iaasAllocationPolicyType)
    // We will be using ring network datacenter for IAAS Simulations
    val datacenter = RingNetworkDatacenter(simulation, hostList, allocationPolicy)

    utils.setDatacenterCost(datacenter,
      iaas_cost_per_sec,
      iaas_cost_per_mem,
      iaas_cost_per_storage,
      iaas_cost_per_bw
    )

    val vmList = utils.createNwVmList(
      iaas_vm_count,
      iaas_host_mips,
      iaas_vm_pe_count,
      iaas_vm_ram,
      iaas_vm_bw,
      iaas_vm_size,
      utils.SchedulerType.SPACESHARED
    )

    // set scaling if user needs to
    if needScaling then
      vmList.asScala.foreach(vm => {
        utils.createHorizontalVmScaling(
          vm,
          iaas_host_mips,
          iaas_vm_pe_count,
          iaas_vm_ram,
          iaas_vm_bw,
          iaas_vm_size,
          utils.SchedulerType.SPACESHARED,
          cpuOverloadThreshold
        )
        utils.createVerticalRamScalingForVm(
          vm,
          ramScalingFactor,
          ramUpperUtilizationThreshold,
          ramLowerUtilizationThreshold
        )
      })

    val cloudletList = application match
      case Application.Simple => utils.createCloudletList(
        appCloudletCount,
        cloudletLength,
        cloudletPe,
        iaas_cloudlet_cpu_utilization,
        iaas_cloudlet_initial_ram_utilization,
        iaas_cloudlet_max_ram_utilization,
        iaas_cloudlet_bw_utilization
      )
      case Application.ThreeTier => utils.createNwCloudletList(
        three_tier_cloudlet_count * appCloudletCount,
        cloudletLength,
        cloudletPe,
        vmList,
        iaas_cloudlet_cpu_utilization,
        iaas_cloudlet_initial_ram_utilization,
        iaas_cloudlet_max_ram_utilization,
        iaas_cloudlet_bw_utilization
      )
      case Application.MapReduce => utils.createNwCloudletList(
        map_reduce_cloudlet_count * appCloudletCount,
        cloudletLength,
        cloudletPe,
        vmList,
        iaas_cloudlet_cpu_utilization,
        iaas_cloudlet_initial_ram_utilization,
        iaas_cloudlet_max_ram_utilization,
        iaas_cloudlet_bw_utilization
      )

    application match
      case Application.ThreeTier => {
        Range(0, appCloudletCount).map(i => {
          ThreeTierApplication.createAppWorkFlow(
            cloudletList.asInstanceOf[util.List[NetworkCloudlet]].get(three_tier_cloudlet_count * i),
            cloudletList.asInstanceOf[util.List[NetworkCloudlet]].get((three_tier_cloudlet_count * i) + 1),
            cloudletList.asInstanceOf[util.List[NetworkCloudlet]].get((three_tier_cloudlet_count * i) + 2))
        })
      }
      case Application.MapReduce => {
        Range(0, appCloudletCount).map(i => {
          MapReduceJob.createMapReduceTasks(
            cloudletList.asInstanceOf[util.List[NetworkCloudlet]].get(map_reduce_cloudlet_count * i),
            cloudletList.asInstanceOf[util.List[NetworkCloudlet]].get((map_reduce_cloudlet_count * i) + 1),
            cloudletList.asInstanceOf[util.List[NetworkCloudlet]].get((map_reduce_cloudlet_count * i) + 2),
            cloudletList.asInstanceOf[util.List[NetworkCloudlet]].get((map_reduce_cloudlet_count * i) + 3))
        })
      }
      case _ => ()

    // submit vms, cloudlets, performs simulation and prints the result
    broker.submitVmList(vmList)
    broker.submitCloudletList(cloudletList)
    simulation.start
    utils.buildTableAndPrintResults(broker, vmList, hostList)

  def requestFaaSService(simulation: CloudSim, broker: DatacenterBroker, appCloudletCount: Int, cloudletLength: Int, cloudletPe: Int, cloudletSize: Int, cloudletOutputSize: Int) =

    if cloudletLength > faas_max_cloudlet_length then
      logger.error(s"Cloudlet length cannot be larger than $faas_max_cloudlet_length in FaaS model")
    else if cloudletPe > faas_max_cloudlet_pe then
      logger.error(s"Cloudlet PE cannot be larger than $faas_max_cloudlet_pe in FaaS model")
    else if cloudletSize > faas_max_cloudlet_file_size then
      logger.error(s"Cloudlet file size cannot be larger than $faas_max_cloudlet_file_size in FaaS model")
    else if cloudletOutputSize > faas_max_cloudlet_output_file_size then
      logger.error(s"Cloudlet output file size cannot be larger than $faas_max_cloudlet_output_file_size in FaaS model")
    else
      val hostList = utils.createNwHostList(faas_hosts_count, faas_host_pe_count, faas_host_mips, faas_host_ram, faas_host_bw, faas_host_storage, utils.SchedulerType.TIMESHARED)

      val allocationPolicy = utils.getAllocationPolicy(faasAllocationPolicyType)
      // We will be using hybrid network datacenter for faas Simulations
      val datacenter = HybridNetworkDatacenter(simulation, hostList, allocationPolicy)

      utils.setDatacenterCost(datacenter, faas_cost_per_sec, faas_cost_per_mem, faas_cost_per_storage, faas_cost_per_bw)

      val vmList = utils.createNwVmList(faas_vm_count, faas_host_mips, faas_vm_pe_count, faas_vm_ram, faas_vm_bw, faas_vm_size, utils.SchedulerType.SPACESHARED)
      vmList.asScala.foreach(vm => {
        utils.createHorizontalVmScaling(vm, faas_host_mips, faas_vm_pe_count, faas_vm_ram, faas_vm_bw, faas_vm_size, utils.SchedulerType.SPACESHARED, faas_cpu_overload_threshold)
        utils.createVerticalRamScalingForVm(vm, faas_ram_scaling_factor, faas_ram_upper_utilization_threshold, faas_ram_lower_utilization_threshold)
      })

      val cloudletList = utils.createCloudletList(appCloudletCount, cloudletLength, cloudletPe, cloudletSize, cloudletOutputSize, faas_cloudlet_cpu_utilization, faas_cloudlet_initial_ram_utilization, faas_cloudlet_max_ram_utilization, faas_cloudlet_bw_utilization)

      // submit vms, cloudlets, performs simulation and prints the result
      broker.submitVmList(vmList)
      broker.submitCloudletList(cloudletList)
      simulation.start
      utils.buildTableAndPrintResults(broker, vmList, hostList)

}
