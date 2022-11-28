package CloudOrg.Datacenters

import CloudOrg.Applications.{MapReduceJob, ThreeTierApplication}
import CloudOrg.HelperUtils.{CreateLogger, ObtainConfigReference, utils}
import CloudOrg.Datacenters.{HybridNetworkDatacenter, RingNetworkDatacenter, StarNetworkDatacenter, TreeNetworkDatacenter}
import com.typesafe.config.Config
import org.cloudbus.cloudsim.brokers.DatacenterBroker
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.hosts.Host
import org.cloudbus.cloudsim.vms.{Vm, VmCost}
import org.cloudbus.cloudsim.cloudlets.Cloudlet
import org.cloudbus.cloudsim.cloudlets.network.NetworkCloudlet
import org.cloudsimplus.builders.tables.CloudletsTableBuilder
import org.slf4j.Logger

import java.util
import scala.jdk.CollectionConverters.*

object CustomDatacenterService {

  val logger: Logger = CreateLogger(classOf[CustomDatacenterService.type])
  val config: Config = ObtainConfigReference("cloudOrganizationSimulations").get
  val threeTierAppConfig: Config = config.getConfig("cloudOrganizationSimulations.threeTier")
  val mapReduceConfig: Config = config.getConfig("cloudOrganizationSimulations.mapReduce")
  val treeNetworkConfig: Config = config.getConfig("cloudOrganizationSimulations.treeTopology")
  val saasconfig: Config = config.getConfig("cloudOrganizationSimulations.saas")
  val paasconfig: Config = config.getConfig("cloudOrganizationSimulations.paas")
  val iaasconfig: Config = config.getConfig("cloudOrganizationSimulations.iaas")
  val faasconfig: Config = config.getConfig("cloudOrganizationSimulations.faas")

  // App specific variables
  // map reduce total cloudlet for a single map reduce job
  val map_reduce_cloudlet_count: Int = mapReduceConfig.getInt("mapper_cloudlets") + mapReduceConfig.getInt("reducer_cloudlets")

  // three tier total cloudlet for a single instance of app
  val three_tier_cloudlet_count: Int = threeTierAppConfig.getInt("client_cloudlets") + threeTierAppConfig.getInt("server_cloudlets")

  enum Application:
    case MapReduce, ThreeTier, Simple

  // SAAS VARIABLES
  enum SaaSCloudletType:
    case Type1, Type2, Type3

  val saas_hosts_count: Int = saasconfig.getInt("saas_hosts_count")
  val saas_host_mips: Int = saasconfig.getInt("saas_host_mips")
  val saas_host_pe_count: Int = saasconfig.getInt("saas_host_pe_count")
  val saas_host_ram: Int = saasconfig.getInt("saas_host_ram")
  val saas_host_bw: Int = saasconfig.getInt("saas_host_bw")
  val saas_host_storage: Int = saasconfig.getInt("saas_host_storage")

  val saas_vm_count: Int = saasconfig.getInt("saas_vm_count")
  val saas_vm_pe_count: Int = saasconfig.getInt("saas_vm_pe_count")
  val saas_vm_ram: Int = saasconfig.getInt("saas_vm_ram")
  val saas_vm_bw: Int = saasconfig.getInt("saas_vm_bw")
  val saas_vm_size: Int = saasconfig.getInt("saas_vm_size")

  // Type 1 cloudlet attributes
  val saas_type1_cloudlet_pe_count: Int = saasconfig.getInt("saas_type1_cloudlet_pe_count")
  val saas_type1_cloudlet_length: Int = saasconfig.getInt("saas_type1_cloudlet_length")
  val saas_type1_cloudlet_file_size: Int = saasconfig.getInt("saas_type1_cloudlet_file_size")
  val saas_type1_cloudlet_output_size: Int = saasconfig.getInt("saas_type1_cloudlet_output_size")

  // Type 2 cloudlet attributes
  val saas_type2_cloudlet_pe_count: Int = saasconfig.getInt("saas_type2_cloudlet_pe_count")
  val saas_type2_cloudlet_length: Int = saasconfig.getInt("saas_type2_cloudlet_length")

  // Type 3 cloudlet attributes
  val saas_type3_cloudlet_pe_count: Int = saasconfig.getInt("saas_type3_cloudlet_pe_count")
  val saas_type3_cloudlet_length: Int = saasconfig.getInt("saas_type3_cloudlet_length")
  val saas_type3_cloudlet_file_size: Int = saasconfig.getInt("saas_type3_cloudlet_file_size")
  val saas_type3_cloudlet_output_size: Int = saasconfig.getInt("saas_type3_cloudlet_output_size")

  // Utilization
  val saas_cloudlet_cpu_utilization: Double = saasconfig.getDouble("saas_cloudlet_cpu_utilization")
  val saas_cloudlet_ram_utilization: Double = saasconfig.getDouble("saas_cloudlet_ram_utilization")
  val saas_cloudlet_bw_utilization: Double = saasconfig.getDouble("saas_cloudlet_bw_utilization")
  val saas_cloudlet_initial_ram_utilization: Double = saasconfig.getDouble("saas_cloudlet_initial_ram_utilization")
  val saas_cloudlet_max_ram_utilization: Double = saasconfig.getDouble("saas_cloudlet_max_ram_utilization")

  // cost - common
  val saas_cost_per_sec: Double = saasconfig.getDouble("saas_cost_per_sec")
  val saas_cost_per_mem: Double = saasconfig.getDouble("saas_cost_per_mem")
  val saas_cost_per_storage: Double = saasconfig.getDouble("saas_cost_per_storage")
  val saas_cost_per_bw: Double = saasconfig.getDouble("saas_cost_per_bw")

  // scaling
  // horizontal scaling
  val saas_cpu_overload_threshold: Double = saasconfig.getDouble("saas_cpu_overload_threshold")
  // vertical ram scaling
  val saas_ram_scaling_factor: Double = saasconfig.getDouble("saas_ram_scaling_factor")
  val saas_ram_upper_utilization_threshold: Double = saasconfig.getDouble("saas_ram_upper_utilization_threshold")
  val saas_ram_lower_utilization_threshold: Double = saasconfig.getDouble("saas_ram_lower_utilization_threshold")

  // allocation policy
  val saasAllocationPolicyType: String = saasconfig.getString("saasAllocationPolicyType")

  // vm scheduling
  val saasVmSchedulingType: String = saasconfig.getString("saasVmSchedulingType")

  // cloudlet scheduling
  val saasCloudletSchedulingType: String = saasconfig.getString("saasCloudletSchedulingType")


  // PAAS VARIABLES
  val paas_tree_size: Int = treeNetworkConfig.getInt("tree_count")

  val paas_hosts_count: Int = paasconfig.getInt("paas_hosts_count")
  val paas_host_mips: Int = paasconfig.getInt("paas_host_mips")
  val paas_host_pe_count: Int = paasconfig.getInt("paas_host_pe_count")
  val paas_host_ram: Int = paasconfig.getInt("paas_host_ram")
  val paas_host_bw: Int = paasconfig.getInt("paas_host_bw")
  val paas_host_storage: Int = paasconfig.getInt("paas_host_storage")

  val paas_vm_count: Int = paasconfig.getInt("paas_vm_count")
  val paas_vm_pe_count: Int = paasconfig.getInt("paas_vm_pe_count")
  val paas_vm_ram: Int = paasconfig.getInt("paas_vm_ram")
  val paas_vm_bw: Int = paasconfig.getInt("paas_vm_bw")
  val paas_vm_size: Int = paasconfig.getInt("paas_vm_size")

  // Utilization
  val paas_cloudlet_cpu_utilization: Double = paasconfig.getDouble("paas_cloudlet_cpu_utilization")
  val paas_cloudlet_ram_utilization: Double = paasconfig.getDouble("paas_cloudlet_ram_utilization")
  val paas_cloudlet_bw_utilization: Double = paasconfig.getDouble("paas_cloudlet_bw_utilization")
  val paas_cloudlet_initial_ram_utilization: Double = paasconfig.getDouble("paas_cloudlet_initial_ram_utilization")
  val paas_cloudlet_max_ram_utilization: Double = paasconfig.getDouble("paas_cloudlet_max_ram_utilization")

  // cost
  val paas_cost_per_sec: Double = paasconfig.getDouble("paas_cost_per_sec")
  val paas_cost_per_mem: Double = paasconfig.getDouble("paas_cost_per_mem")
  val paas_cost_per_storage: Double = paasconfig.getDouble("paas_cost_per_storage")
  val paas_cost_per_bw: Double = paasconfig.getDouble("paas_cost_per_bw")

  // scaling
  // horizontal scaling
  val paas_cpu_overload_threshold: Double = paasconfig.getDouble("paas_cpu_overload_threshold")
  // vertical ram scaling
  val paas_ram_scaling_factor: Double = paasconfig.getDouble("paas_ram_scaling_factor")
  val paas_ram_upper_utilization_threshold: Double = paasconfig.getDouble("paas_ram_upper_utilization_threshold")
  val paas_ram_lower_utilization_threshold: Double = paasconfig.getDouble("paas_ram_lower_utilization_threshold")

  // allocation policy
  val paasAllocationPolicyType: String = paasconfig.getString("paasAllocationPolicyType")

  // vm scheduling
  val paasVmSchedulingType: String = paasconfig.getString("paasVmSchedulingType")

  // cloudlet scheduling
  val paasCloudletSchedulingType: String = paasconfig.getString("paasCloudletSchedulingType")

  // IAAS Variables
  val iaas_hosts_count: Int = iaasconfig.getInt("iaas_hosts_count")
  val iaas_host_mips: Int = iaasconfig.getInt("iaas_host_mips")
  val iaas_host_pe_count: Int = iaasconfig.getInt("iaas_host_pe_count")
  val iaas_host_ram: Int = iaasconfig.getInt("iaas_host_ram")
  val iaas_host_bw: Int = iaasconfig.getInt("iaas_host_bw")
  val iaas_host_storage: Int = iaasconfig.getInt("iaas_host_storage")

  val iaas_vm_count: Int = iaasconfig.getInt("iaas_vm_count")
  val iaas_vm_pe_count: Int = iaasconfig.getInt("iaas_vm_pe_count")
  val iaas_vm_ram: Int = iaasconfig.getInt("iaas_vm_ram")
  val iaas_vm_bw: Int = iaasconfig.getInt("iaas_vm_bw")
  val iaas_vm_size: Int = iaasconfig.getInt("iaas_vm_size")

  // Utilization
  val iaas_cloudlet_cpu_utilization: Double = iaasconfig.getDouble("iaas_cloudlet_cpu_utilization")
  val iaas_cloudlet_ram_utilization: Double = iaasconfig.getDouble("iaas_cloudlet_ram_utilization")
  val iaas_cloudlet_bw_utilization: Double = iaasconfig.getDouble("iaas_cloudlet_bw_utilization")
  val iaas_cloudlet_initial_ram_utilization: Double = iaasconfig.getDouble("iaas_cloudlet_initial_ram_utilization")
  val iaas_cloudlet_max_ram_utilization: Double = iaasconfig.getDouble("iaas_cloudlet_max_ram_utilization")

  // cost
  val iaas_cost_per_sec: Double = iaasconfig.getDouble("iaas_cost_per_sec")
  val iaas_cost_per_mem: Double = iaasconfig.getDouble("iaas_cost_per_mem")
  val iaas_cost_per_storage: Double = iaasconfig.getDouble("iaas_cost_per_storage")
  val iaas_cost_per_bw: Double = iaasconfig.getDouble("iaas_cost_per_bw")

  // allocation policy
  val iaasAllocationPolicyType: String = iaasconfig.getString("iaasAllocationPolicyType")

  // vm scheduling
  val iaasVmSchedulingType: String = iaasconfig.getString("iaasVmSchedulingType")

  // cloudlet scheduling
  val iaasCloudletSchedulingType: String = iaasconfig.getString("iaasCloudletSchedulingType")

  // FAAS Variables
  val faas_hosts_count: Int = faasconfig.getInt("faas_hosts_count")
  val faas_host_mips: Int = faasconfig.getInt("faas_host_mips")
  val faas_host_pe_count: Int = faasconfig.getInt("faas_host_pe_count")
  val faas_host_ram: Int = faasconfig.getInt("faas_host_ram")
  val faas_host_bw: Int = faasconfig.getInt("faas_host_bw")
  val faas_host_storage: Int = faasconfig.getInt("faas_host_storage")

  val faas_vm_count: Int = faasconfig.getInt("faas_vm_count")
  val faas_vm_pe_count: Int = faasconfig.getInt("faas_vm_pe_count")
  val faas_vm_ram: Int = faasconfig.getInt("faas_vm_ram")
  val faas_vm_bw: Int = faasconfig.getInt("faas_vm_bw")
  val faas_vm_size: Int = faasconfig.getInt("faas_vm_size")

  val faas_max_cloudlet_length: Int = faasconfig.getInt("faas_max_cloudlet_length")
  val faas_max_cloudlet_pe: Int = faasconfig.getInt("faas_max_cloudlet_pe")
  val faas_max_cloudlet_file_size: Int = faasconfig.getInt("faas_max_cloudlet_file_size")
  val faas_max_cloudlet_output_file_size: Int = faasconfig.getInt("faas_max_cloudlet_output_file_size")

  // Utilization
  val faas_cloudlet_cpu_utilization: Double = faasconfig.getDouble("faas_cloudlet_cpu_utilization")
  val faas_cloudlet_ram_utilization: Double = faasconfig.getDouble("faas_cloudlet_ram_utilization")
  val faas_cloudlet_bw_utilization: Double = faasconfig.getDouble("faas_cloudlet_bw_utilization")
  val faas_cloudlet_initial_ram_utilization: Double = faasconfig.getDouble("faas_cloudlet_initial_ram_utilization")
  val faas_cloudlet_max_ram_utilization: Double = faasconfig.getDouble("faas_cloudlet_max_ram_utilization")

  // scaling
  // horizontal scaling
  val faas_cpu_overload_threshold: Double = faasconfig.getDouble("faas_cpu_overload_threshold")
  // vertical ram scaling
  val faas_ram_scaling_factor: Double = faasconfig.getDouble("faas_ram_scaling_factor")
  val faas_ram_upper_utilization_threshold: Double = faasconfig.getDouble("faas_ram_upper_utilization_threshold")
  val faas_ram_lower_utilization_threshold: Double = faasconfig.getDouble("faas_ram_lower_utilization_threshold")

  // cost
  val faas_cost_per_sec: Double = faasconfig.getDouble("faas_cost_per_sec")
  val faas_cost_per_mem: Double = faasconfig.getDouble("faas_cost_per_mem")
  val faas_cost_per_storage: Double = faasconfig.getDouble("faas_cost_per_storage")
  val faas_cost_per_bw: Double = faasconfig.getDouble("faas_cost_per_bw")

  // allocation policy
  val faasAllocationPolicyType: String = faasconfig.getString("faasAllocationPolicyType")

  // vm scheduling
  val faasVmSchedulingType: String = faasconfig.getString("faasVmSchedulingType")

  // cloudlet scheduling
  val faasCloudletSchedulingType: String = faasconfig.getString("faasCloudletSchedulingType")

  /**
   * SAAS Service. SAAS can only pass number of cloudlets and type of cloudlet to execute
   * @param simulation - simulation object
   * @param broker - broker object
   * @param numberOfCloudlet - number of cloudlets
   * @param cloudletType - type of cloudlet to execute
   */
  def requestSaaSSimulation(simulation: CloudSim, broker: DatacenterBroker, numberOfCloudlet: Int, cloudletType: SaaSCloudletType): Unit =
    logger.info("SAAS Service Started")
    val hostList = utils.createNwHostList(saas_hosts_count, saas_host_pe_count, saas_host_mips, saas_host_ram, saas_host_bw, saas_host_storage, saasVmSchedulingType)

    val allocationPolicy = utils.getAllocationPolicy(saasAllocationPolicyType)
    // We will be using star network datacenter for SAAS Simulations
    val datacenter = utils.createNwDatacenter(utils.NetworkDatacenterType.STAR, simulation, hostList, allocationPolicy, paas_tree_size)

    utils.setDatacenterCost(datacenter, saas_cost_per_sec, saas_cost_per_mem, saas_cost_per_storage, saas_cost_per_bw)

    val vmList = utils.createNwVmList(saas_vm_count, saas_host_mips, saas_vm_pe_count, saas_vm_ram, saas_vm_bw, saas_vm_size, saasCloudletSchedulingType)
    vmList.asScala.foreach(vm => {
      utils.createHorizontalVmScaling(vm, saas_host_mips, saas_vm_pe_count, saas_vm_ram, saas_vm_bw, saas_vm_size, saasCloudletSchedulingType, saas_cpu_overload_threshold)
      utils.createVerticalRamScalingForVm(vm, saas_ram_scaling_factor, saas_ram_upper_utilization_threshold, saas_ram_lower_utilization_threshold)
    })

    // decide cloudlet type
    val cloudletList = cloudletType match
      case SaaSCloudletType.Type1 => utils.createCloudletList(numberOfCloudlet, saas_type1_cloudlet_length, saas_type1_cloudlet_pe_count, saas_type1_cloudlet_file_size, saas_type1_cloudlet_output_size, saas_cloudlet_cpu_utilization, saas_cloudlet_initial_ram_utilization, saas_cloudlet_max_ram_utilization, saas_cloudlet_bw_utilization)
      case SaaSCloudletType.Type2 => utils.createCloudletList(numberOfCloudlet, saas_type2_cloudlet_length, saas_type2_cloudlet_pe_count , saas_cloudlet_cpu_utilization, saas_cloudlet_initial_ram_utilization, saas_cloudlet_max_ram_utilization, saas_cloudlet_bw_utilization)
      case SaaSCloudletType.Type3 => utils.createCloudletList(numberOfCloudlet, saas_type3_cloudlet_length, saas_type3_cloudlet_pe_count, saas_type3_cloudlet_file_size, saas_type3_cloudlet_output_size, saas_cloudlet_cpu_utilization, saas_cloudlet_initial_ram_utilization, saas_cloudlet_max_ram_utilization, saas_cloudlet_bw_utilization)

    // submit vms, cloudlets, performs simulation and prints the result
    broker.submitVmList(vmList)
    broker.submitCloudletList(cloudletList)
    simulation.start
    utils.buildTableAndPrintResults(broker, vmList, hostList)

  /**
   * PAAS MapReduce Platform.
   * customers have control over number Of Cloudlets, cloudlet Length in MI and how manu cores it require
   * @param simulation - simulation object
   * @param broker - broker object
   * @param numberOfCloudlet - number of cloudlets
   * @param cloudletLength - cloudlet length
   * @param cloudletPe - cloudlet PEs
   */
  def requestMapReducePaaSSimulation(simulation: CloudSim, broker: DatacenterBroker, numberOfAppCloudlets: Int, cloudletLength: Int, cloudletPe: Int): Unit =
    logger.info("PAAS MapReduce Service Started")
    val hostList = utils.createNwHostList(paas_hosts_count, paas_host_pe_count, paas_host_mips, paas_host_ram, paas_host_bw, paas_host_storage, paasVmSchedulingType)

    // allocation policy
    val allocationPolicy = utils.getAllocationPolicy(paasAllocationPolicyType)
    // We will be using tree network datacenter for PAAS Simulations
    val datacenter = utils.createNwDatacenter(utils.NetworkDatacenterType.TREE, simulation, hostList, allocationPolicy, paas_tree_size)

    utils.setDatacenterCost(datacenter, paas_cost_per_sec, paas_cost_per_mem, paas_cost_per_storage, paas_cost_per_bw)

    val vmList = utils.createNwVmList(paas_vm_count, paas_host_mips, paas_vm_pe_count, paas_vm_ram, paas_vm_bw, paas_vm_size, paasCloudletSchedulingType)
    vmList.asScala.foreach(vm => {
      utils.createHorizontalVmScaling(vm, paas_host_mips, paas_vm_pe_count, paas_vm_ram, paas_vm_bw, paas_vm_size, paasCloudletSchedulingType, paas_cpu_overload_threshold)
      utils.createVerticalRamScalingForVm(vm, paas_ram_scaling_factor, paas_ram_upper_utilization_threshold, paas_ram_lower_utilization_threshold)
    })

    val cloudletList = utils.createNwCloudletList(map_reduce_cloudlet_count * numberOfAppCloudlets, cloudletLength, cloudletPe, vmList, paas_cloudlet_cpu_utilization, paas_cloudlet_initial_ram_utilization, paas_cloudlet_max_ram_utilization, paas_cloudlet_bw_utilization)

    // create app workflow
    Range(0, numberOfAppCloudlets).foreach(i => {
      MapReduceJob.createMapReduceTasks(cloudletList.get(map_reduce_cloudlet_count * i), cloudletList.get((map_reduce_cloudlet_count * i) + 1), cloudletList.get((map_reduce_cloudlet_count * i) + 2), cloudletList.get((map_reduce_cloudlet_count * i) + 3))
    })
    // submit vms, cloudlets, performs simulation and prints the result
    broker.submitVmList(vmList)
    broker.submitCloudletList(cloudletList)
    simulation.start
    utils.buildTableAndPrintResults(broker, vmList, hostList)

  /**
   * PAAS Three Tier Platform service
   * customers have control over number Of Cloudlets, cloudlet Length in MI and how manu cores it require
   * @param simulation - simulation object
   * @param broker - broker object
   * @param numberOfCloudlet - number of cloudlets
   * @param cloudletLength - cloudlet length
   * @param cloudletPe - cloudlet PEs
   */
  def requestThreeTierPaaSService(simulation: CloudSim, broker: DatacenterBroker, numberOfAppCloudlets: Int, cloudletLength: Int, cloudletPe: Int): Unit =
    logger.info("PAAS Three Tier Service Started")
    val hostList = utils.createNwHostList(paas_hosts_count, paas_host_pe_count, paas_host_mips, paas_host_ram, paas_host_bw, paas_host_storage, paasVmSchedulingType)

    val allocationPolicy = utils.getAllocationPolicy(paasAllocationPolicyType)
    // We will be using Ring network datacenter for PAAS Simulations
    val datacenter = utils.createNwDatacenter(utils.NetworkDatacenterType.RING, simulation, hostList, allocationPolicy, paas_tree_size)

    utils.setDatacenterCost(datacenter, paas_cost_per_sec, paas_cost_per_mem, paas_cost_per_storage, paas_cost_per_bw)

    val vmList = utils.createNwVmList(paas_vm_count, paas_host_mips, paas_vm_pe_count, paas_vm_ram, paas_vm_bw, paas_vm_size, paasCloudletSchedulingType)
    vmList.asScala.foreach(vm => {
      utils.createHorizontalVmScaling(vm, paas_host_mips, paas_vm_pe_count, paas_vm_ram, paas_vm_bw, paas_vm_size, paasCloudletSchedulingType, paas_cpu_overload_threshold)
      utils.createVerticalRamScalingForVm(vm, paas_ram_scaling_factor, paas_ram_upper_utilization_threshold, paas_ram_lower_utilization_threshold)
    })

    val cloudletList = utils.createNwCloudletList(three_tier_cloudlet_count * numberOfAppCloudlets, cloudletLength, cloudletPe, vmList, paas_cloudlet_cpu_utilization, paas_cloudlet_initial_ram_utilization, paas_cloudlet_max_ram_utilization, paas_cloudlet_bw_utilization)
    Range(0, numberOfAppCloudlets).foreach(i => {
      ThreeTierApplication.createAppWorkFlow(cloudletList.get(three_tier_cloudlet_count * i), cloudletList.get((three_tier_cloudlet_count * i) + 1), cloudletList.get((three_tier_cloudlet_count * i) + 2))
    })
    // submit vms, cloudlets, performs simulation and prints the result
    broker.submitVmList(vmList)
    broker.submitCloudletList(cloudletList)
    simulation.start
    utils.buildTableAndPrintResults(broker, vmList, hostList)

  /**
   * IAAS Service - Customers executing their own application
   * @param simulation - simulation object
   * @param broker - broker object
   * @param application - application type provided by customer - simple, mapreduce and threetier supported only
   * @param appCloudletCount - number of apps
   * @param cloudletLength - cloudlet length for apps
   * @param cloudletPe - cloudlet PE count
   * @param needScaling - if user wants to enable auto-scaling for IAAS
   * @param cpuOverloadThreshold - cpu overload threshold for horizontal scaling
   * @param ramScalingFactor - ram scaling factor (b/w 0 and 1) for vertical ram scaling
   * @param ramUpperUtilizationThreshold - ram upper threshold for vertical ram scaling
   * @param ramLowerUtilizationThreshold - ram lower threshold for vertical ram scaling
   */
  def requestIaaSService(simulation: CloudSim, broker: DatacenterBroker, application: Application, appCloudletCount: Int, cloudletLength: Int, cloudletPe: Int, needScaling: Boolean, cpuOverloadThreshold: Double, ramScalingFactor: Double, ramUpperUtilizationThreshold: Double, ramLowerUtilizationThreshold: Double): Unit =
    logger.info("IAAS Service Started")
    val hostList = utils.createNwHostList(
      iaas_hosts_count,
      iaas_host_pe_count,
      iaas_host_mips,
      iaas_host_ram,
      iaas_host_bw,
      iaas_host_storage,
      iaasVmSchedulingType
    )

    val allocationPolicy = utils.getAllocationPolicy(iaasAllocationPolicyType)
    // We will be using ring network datacenter for IAAS Simulations
    val datacenter = utils.createNwDatacenter(utils.NetworkDatacenterType.RING, simulation, hostList, allocationPolicy, paas_tree_size)

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
      iaasCloudletSchedulingType
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
          iaasCloudletSchedulingType,
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

    // decide which application user requested to run
    application match
      case Application.ThreeTier =>
        Range(0, appCloudletCount).foreach(i => {
          ThreeTierApplication.createAppWorkFlow(
            cloudletList.asInstanceOf[util.List[NetworkCloudlet]].get(three_tier_cloudlet_count * i),
            cloudletList.asInstanceOf[util.List[NetworkCloudlet]].get((three_tier_cloudlet_count * i) + 1),
            cloudletList.asInstanceOf[util.List[NetworkCloudlet]].get((three_tier_cloudlet_count * i) + 2))
        })
      case Application.MapReduce =>
        Range(0, appCloudletCount).foreach(i => {
          MapReduceJob.createMapReduceTasks(
            cloudletList.asInstanceOf[util.List[NetworkCloudlet]].get(map_reduce_cloudlet_count * i),
            cloudletList.asInstanceOf[util.List[NetworkCloudlet]].get((map_reduce_cloudlet_count * i) + 1),
            cloudletList.asInstanceOf[util.List[NetworkCloudlet]].get((map_reduce_cloudlet_count * i) + 2),
            cloudletList.asInstanceOf[util.List[NetworkCloudlet]].get((map_reduce_cloudlet_count * i) + 3))
        })
      case _ => ()

    // submit vms, cloudlets, performs simulation and prints the result
    broker.submitVmList(vmList)
    broker.submitCloudletList(cloudletList)
    simulation.start
    utils.buildTableAndPrintResults(broker, vmList, hostList)

  /**
   * FAAS Service - small cloudlet execution
   * Fast Execution, rules for rejecting user request for FAAS if large cloudlets are passed
   * @param simulation - simulation object
   * @param broker - broker object
   * @param appCloudletCount - number of apps
   * @param cloudletLength - cloudlet length for apps
   * @param cloudletPe - cloudlet PE count
   * @param cloudletSize - cloudlet file size
   * @param cloudletOutputSize - cloudlet output file size
   */
  def requestFaaSService(simulation: CloudSim, broker: DatacenterBroker, appCloudletCount: Int, cloudletLength: Int, cloudletPe: Int, cloudletSize: Int, cloudletOutputSize: Int): Unit =
    if cloudletLength > faas_max_cloudlet_length then
      logger.error(s"Cloudlet length cannot be larger than $faas_max_cloudlet_length in FaaS model")
    else if cloudletPe > faas_max_cloudlet_pe then
      logger.error(s"Cloudlet PE cannot be larger than $faas_max_cloudlet_pe in FaaS model")
    else if cloudletSize > faas_max_cloudlet_file_size then
      logger.error(s"Cloudlet file size cannot be larger than $faas_max_cloudlet_file_size in FaaS model")
    else if cloudletOutputSize > faas_max_cloudlet_output_file_size then
      logger.error(s"Cloudlet output file size cannot be larger than $faas_max_cloudlet_output_file_size in FaaS model")
    else

      logger.info("FAAS Service Started")
      val hostList = utils.createNwHostList(faas_hosts_count, faas_host_pe_count, faas_host_mips, faas_host_ram, faas_host_bw, faas_host_storage, faasVmSchedulingType)

      // allocation policy
      val allocationPolicy = utils.getAllocationPolicy(faasAllocationPolicyType)
      // We will be using hybrid network datacenter for faas Simulations
      val datacenter = utils.createNwDatacenter(utils.NetworkDatacenterType.HYBRID, simulation, hostList, allocationPolicy, paas_tree_size)

      utils.setDatacenterCost(datacenter, faas_cost_per_sec, faas_cost_per_mem, faas_cost_per_storage, faas_cost_per_bw)

      val vmList = utils.createNwVmList(faas_vm_count, faas_host_mips, faas_vm_pe_count, faas_vm_ram, faas_vm_bw, faas_vm_size, faasCloudletSchedulingType)

      // set auto-scaling
      vmList.asScala.foreach(vm => {
        utils.createHorizontalVmScaling(vm, faas_host_mips, faas_vm_pe_count, faas_vm_ram, faas_vm_bw, faas_vm_size, faasCloudletSchedulingType, faas_cpu_overload_threshold)
        utils.createVerticalRamScalingForVm(vm, faas_ram_scaling_factor, faas_ram_upper_utilization_threshold, faas_ram_lower_utilization_threshold)
      })

      val cloudletList = utils.createCloudletList(appCloudletCount, cloudletLength, cloudletPe, cloudletSize, cloudletOutputSize, faas_cloudlet_cpu_utilization, faas_cloudlet_initial_ram_utilization, faas_cloudlet_max_ram_utilization, faas_cloudlet_bw_utilization)

      // submit vms, cloudlets, performs simulation and prints the result
      broker.submitVmList(vmList)
      broker.submitCloudletList(cloudletList)
      simulation.start
      utils.buildTableAndPrintResults(broker, vmList, hostList)

}
