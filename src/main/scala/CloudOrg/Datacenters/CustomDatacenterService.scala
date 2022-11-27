package CloudOrg.Datacenters

import CloudOrg.Applications.{MapReduceJob, ThreeTierApplication}
import CloudOrg.HelperUtils.{CreateLogger, ObtainConfigReference, utils}
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
  val config = ObtainConfigReference("cloudOrganizationSimulations").get
  val threeTierAppConfig = config.getConfig("cloudOrganizationSimulations.threeTier")
  val mapReduceConfig = config.getConfig("cloudOrganizationSimulations.mapReduce")
  val treeNetworkConfig = config.getConfig("cloudOrganizationSimulations.treeTopology")
  val saasconfig = config.getConfig("cloudOrganizationSimulations.saas")
  val paasconfig = config.getConfig("cloudOrganizationSimulations.paas")
  val iaasconfig = config.getConfig("cloudOrganizationSimulations.iaas")
  val faasconfig = config.getConfig("cloudOrganizationSimulations.faas")

  // App specific variables
  // map reduce total cloudlet for a single map reduce job
  val map_reduce_cloudlet_count = mapReduceConfig.getInt("mapper_cloudlets") + mapReduceConfig.getInt("reducer_cloudlets")

  // three tier total cloudlet for a single instance of app
  val three_tier_cloudlet_count = threeTierAppConfig.getInt("client_cloudlets") + threeTierAppConfig.getInt("server_cloudlets")

  enum Application:
    case MapReduce, ThreeTier, Simple

  // SAAS VARIABLES
  enum SaaSCloudletType:
    case Type1, Type2, Type3

  val saas_hosts_count = saasconfig.getInt("saas_hosts_count")
  val saas_host_mips = saasconfig.getInt("saas_host_mips")
  val saas_host_pe_count = saasconfig.getInt("saas_host_pe_count")
  val saas_host_ram = saasconfig.getInt("saas_host_ram")
  val saas_host_bw = saasconfig.getInt("saas_host_bw")
  val saas_host_storage = saasconfig.getInt("saas_host_storage")

  val saas_vm_count = saasconfig.getInt("saas_vm_count")
  val saas_vm_pe_count = saasconfig.getInt("saas_vm_pe_count")
  val saas_vm_ram = saasconfig.getInt("saas_vm_ram")
  val saas_vm_bw = saasconfig.getInt("saas_vm_bw")
  val saas_vm_size = saasconfig.getInt("saas_vm_size")

  // Type 1 cloudlet attributes
  val saas_type1_cloudlet_pe_count = saasconfig.getInt("saas_type1_cloudlet_pe_count")
  val saas_type1_cloudlet_length = saasconfig.getInt("saas_type1_cloudlet_length")
  val saas_type1_cloudlet_file_size = saasconfig.getInt("saas_type1_cloudlet_file_size")
  val saas_type1_cloudlet_output_size = saasconfig.getInt("saas_type1_cloudlet_output_size")

  // Type 2 cloudlet attributes
  val saas_type2_cloudlet_pe_count = saasconfig.getInt("saas_type2_cloudlet_pe_count")
  val saas_type2_cloudlet_length = saasconfig.getInt("saas_type2_cloudlet_length")

  // Type 3 cloudlet attributes
  val saas_type3_cloudlet_pe_count = saasconfig.getInt("saas_type3_cloudlet_pe_count")
  val saas_type3_cloudlet_length = saasconfig.getInt("saas_type3_cloudlet_length")
  val saas_type3_cloudlet_file_size = saasconfig.getInt("saas_type3_cloudlet_file_size")
  val saas_type3_cloudlet_output_size = saasconfig.getInt("saas_type3_cloudlet_output_size")

  // Utilization
  val saas_cloudlet_cpu_utilization = saasconfig.getDouble("saas_cloudlet_cpu_utilization")
  val saas_cloudlet_ram_utilization = saasconfig.getDouble("saas_cloudlet_ram_utilization")
  val saas_cloudlet_bw_utilization = saasconfig.getDouble("saas_cloudlet_bw_utilization")
  val saas_cloudlet_initial_ram_utilization = saasconfig.getDouble("saas_cloudlet_initial_ram_utilization")
  val saas_cloudlet_max_ram_utilization = saasconfig.getDouble("saas_cloudlet_max_ram_utilization")

  // cost - common
  val saas_cost_per_sec = saasconfig.getDouble("saas_cost_per_sec")
  val saas_cost_per_mem = saasconfig.getDouble("saas_cost_per_mem")
  val saas_cost_per_storage = saasconfig.getDouble("saas_cost_per_storage")
  val saas_cost_per_bw = saasconfig.getDouble("saas_cost_per_bw")

  // scaling
  // horizontal scaling
  val saas_cpu_overload_threshold = saasconfig.getDouble("saas_cpu_overload_threshold")
  // vertical ram scaling
  val saas_ram_scaling_factor = saasconfig.getDouble("saas_ram_scaling_factor")
  val saas_ram_upper_utilization_threshold = saasconfig.getDouble("saas_ram_upper_utilization_threshold")
  val saas_ram_lower_utilization_threshold = saasconfig.getDouble("saas_ram_lower_utilization_threshold")

  // allocation policy
  val saasAllocationPolicyType = saasconfig.getString("saasAllocationPolicyType")

  // vm scheduling
  val saasVmSchedulingType = saasconfig.getString("saasVmSchedulingType")

  // cloudlet scheduling
  val saasCloudletSchedulingType = saasconfig.getString("saasCloudletSchedulingType")


  // PAAS VARIABLES
  val paas_tree_size = treeNetworkConfig.getInt("tree_count")

  val paas_hosts_count = paasconfig.getInt("paas_hosts_count")
  val paas_host_mips = paasconfig.getInt("paas_host_mips")
  val paas_host_pe_count = paasconfig.getInt("paas_host_pe_count")
  val paas_host_ram = paasconfig.getInt("paas_host_ram")
  val paas_host_bw = paasconfig.getInt("paas_host_bw")
  val paas_host_storage = paasconfig.getInt("paas_host_storage")

  val paas_vm_count = paasconfig.getInt("paas_vm_count")
  val paas_vm_pe_count = paasconfig.getInt("paas_vm_pe_count")
  val paas_vm_ram = paasconfig.getInt("paas_vm_ram")
  val paas_vm_bw = paasconfig.getInt("paas_vm_bw")
  val paas_vm_size = paasconfig.getInt("paas_vm_size")

  // Utilization
  val paas_cloudlet_cpu_utilization = paasconfig.getDouble("paas_cloudlet_cpu_utilization")
  val paas_cloudlet_ram_utilization = paasconfig.getDouble("paas_cloudlet_ram_utilization")
  val paas_cloudlet_bw_utilization = paasconfig.getDouble("paas_cloudlet_bw_utilization")
  val paas_cloudlet_initial_ram_utilization = paasconfig.getDouble("paas_cloudlet_initial_ram_utilization")
  val paas_cloudlet_max_ram_utilization = paasconfig.getDouble("paas_cloudlet_max_ram_utilization")

  // cost
  val paas_cost_per_sec = paasconfig.getDouble("paas_cost_per_sec")
  val paas_cost_per_mem = paasconfig.getDouble("paas_cost_per_mem")
  val paas_cost_per_storage = paasconfig.getDouble("paas_cost_per_storage")
  val paas_cost_per_bw = paasconfig.getDouble("paas_cost_per_bw")

  // scaling
  // horizontal scaling
  val paas_cpu_overload_threshold = paasconfig.getDouble("paas_cpu_overload_threshold")
  // vertical ram scaling
  val paas_ram_scaling_factor = paasconfig.getDouble("paas_ram_scaling_factor")
  val paas_ram_upper_utilization_threshold = paasconfig.getDouble("paas_ram_upper_utilization_threshold")
  val paas_ram_lower_utilization_threshold = paasconfig.getDouble("paas_ram_lower_utilization_threshold")

  // allocation policy
  val paasAllocationPolicyType = paasconfig.getString("paasAllocationPolicyType")

  // vm scheduling
  val paasVmSchedulingType = paasconfig.getString("paasVmSchedulingType")

  // cloudlet scheduling
  val paasCloudletSchedulingType = paasconfig.getString("paasCloudletSchedulingType")

  // IAAS Variables
  val iaas_hosts_count = iaasconfig.getInt("iaas_hosts_count")
  val iaas_host_mips = iaasconfig.getInt("iaas_host_mips")
  val iaas_host_pe_count = iaasconfig.getInt("iaas_host_pe_count")
  val iaas_host_ram = iaasconfig.getInt("iaas_host_ram")
  val iaas_host_bw = iaasconfig.getInt("iaas_host_bw")
  val iaas_host_storage = iaasconfig.getInt("iaas_host_storage")

  val iaas_vm_count = iaasconfig.getInt("iaas_vm_count")
  val iaas_vm_pe_count = iaasconfig.getInt("iaas_vm_pe_count")
  val iaas_vm_ram = iaasconfig.getInt("iaas_vm_ram")
  val iaas_vm_bw = iaasconfig.getInt("iaas_vm_bw")
  val iaas_vm_size = iaasconfig.getInt("iaas_vm_size")

  // Utilization
  val iaas_cloudlet_cpu_utilization = iaasconfig.getDouble("iaas_cloudlet_cpu_utilization")
  val iaas_cloudlet_ram_utilization = iaasconfig.getDouble("iaas_cloudlet_ram_utilization")
  val iaas_cloudlet_bw_utilization = iaasconfig.getDouble("iaas_cloudlet_bw_utilization")
  val iaas_cloudlet_initial_ram_utilization = iaasconfig.getDouble("iaas_cloudlet_initial_ram_utilization")
  val iaas_cloudlet_max_ram_utilization = iaasconfig.getDouble("iaas_cloudlet_max_ram_utilization")

  // cost
  val iaas_cost_per_sec = iaasconfig.getDouble("iaas_cost_per_sec")
  val iaas_cost_per_mem = iaasconfig.getDouble("iaas_cost_per_mem")
  val iaas_cost_per_storage = iaasconfig.getDouble("iaas_cost_per_storage")
  val iaas_cost_per_bw = iaasconfig.getDouble("iaas_cost_per_bw")

  // allocation policy
  val iaasAllocationPolicyType = iaasconfig.getString("iaasAllocationPolicyType")

  // vm scheduling
  val iaasVmSchedulingType = iaasconfig.getString("iaasVmSchedulingType")

  // cloudlet scheduling
  val iaasCloudletSchedulingType = iaasconfig.getString("iaasCloudletSchedulingType")

  // FAAS Variables
  val faas_hosts_count = faasconfig.getInt("faas_hosts_count")
  val faas_host_mips = faasconfig.getInt("faas_host_mips")
  val faas_host_pe_count = faasconfig.getInt("faas_host_pe_count")
  val faas_host_ram = faasconfig.getInt("faas_host_ram")
  val faas_host_bw = faasconfig.getInt("faas_host_bw")
  val faas_host_storage = faasconfig.getInt("faas_host_storage")

  val faas_vm_count = faasconfig.getInt("faas_vm_count")
  val faas_vm_pe_count = faasconfig.getInt("faas_vm_pe_count")
  val faas_vm_ram = faasconfig.getInt("faas_vm_ram")
  val faas_vm_bw = faasconfig.getInt("faas_vm_bw")
  val faas_vm_size = faasconfig.getInt("faas_vm_size")

  val faas_max_cloudlet_length = faasconfig.getInt("faas_max_cloudlet_length")
  val faas_max_cloudlet_pe = faasconfig.getInt("faas_max_cloudlet_pe")
  val faas_max_cloudlet_file_size = faasconfig.getInt("faas_max_cloudlet_file_size")
  val faas_max_cloudlet_output_file_size = faasconfig.getInt("faas_max_cloudlet_output_file_size")

  // Utilization
  val faas_cloudlet_cpu_utilization = faasconfig.getDouble("faas_cloudlet_cpu_utilization")
  val faas_cloudlet_ram_utilization = faasconfig.getDouble("faas_cloudlet_ram_utilization")
  val faas_cloudlet_bw_utilization = faasconfig.getDouble("faas_cloudlet_bw_utilization")
  val faas_cloudlet_initial_ram_utilization = faasconfig.getDouble("faas_cloudlet_initial_ram_utilization")
  val faas_cloudlet_max_ram_utilization = faasconfig.getDouble("faas_cloudlet_max_ram_utilization")

  // scaling
  // horizontal scaling
  val faas_cpu_overload_threshold = faasconfig.getDouble("faas_cpu_overload_threshold")
  // vertical ram scaling
  val faas_ram_scaling_factor = faasconfig.getDouble("faas_ram_scaling_factor")
  val faas_ram_upper_utilization_threshold = faasconfig.getDouble("faas_ram_upper_utilization_threshold")
  val faas_ram_lower_utilization_threshold = faasconfig.getDouble("faas_ram_lower_utilization_threshold")

  // cost
  val faas_cost_per_sec = faasconfig.getDouble("faas_cost_per_sec")
  val faas_cost_per_mem = faasconfig.getDouble("faas_cost_per_mem")
  val faas_cost_per_storage = faasconfig.getDouble("faas_cost_per_storage")
  val faas_cost_per_bw = faasconfig.getDouble("faas_cost_per_bw")

  // allocation policy
  val faasAllocationPolicyType = faasconfig.getString("faasAllocationPolicyType")

  // vm scheduling
  val faasVmSchedulingType = faasconfig.getString("faasVmSchedulingType")

  // cloudlet scheduling
  val faasCloudletSchedulingType = faasconfig.getString("faasCloudletSchedulingType")


  def requestSaaSSimulation(simulation: CloudSim, broker: DatacenterBroker, numberOfCloudlet: Int, cloudletType: SaaSCloudletType): Unit =
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
    val hostList = utils.createNwHostList(paas_hosts_count, paas_host_pe_count, paas_host_mips, paas_host_ram, paas_host_bw, paas_host_storage, paasVmSchedulingType)

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
    Range(0, numberOfAppCloudlets).map(i => {
      MapReduceJob.createMapReduceTasks(cloudletList.get(map_reduce_cloudlet_count * i), cloudletList.get((map_reduce_cloudlet_count * i) + 1), cloudletList.get((map_reduce_cloudlet_count * i) + 2), cloudletList.get((map_reduce_cloudlet_count * i) + 3))
    })
    // submit vms, cloudlets, performs simulation and prints the result
    broker.submitVmList(vmList)
    broker.submitCloudletList(cloudletList)
    simulation.start
    utils.buildTableAndPrintResults(broker, vmList, hostList)

  def requestThreeTierPaaSService(simulation: CloudSim, broker: DatacenterBroker, numberOfAppCloudlets: Int, cloudletLength: Int, cloudletPe: Int): Unit =
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
      val hostList = utils.createNwHostList(faas_hosts_count, faas_host_pe_count, faas_host_mips, faas_host_ram, faas_host_bw, faas_host_storage, faasVmSchedulingType)

      val allocationPolicy = utils.getAllocationPolicy(faasAllocationPolicyType)
      // We will be using hybrid network datacenter for faas Simulations
      val datacenter = utils.createNwDatacenter(utils.NetworkDatacenterType.HYBRID, simulation, hostList, allocationPolicy, paas_tree_size)

      utils.setDatacenterCost(datacenter, faas_cost_per_sec, faas_cost_per_mem, faas_cost_per_storage, faas_cost_per_bw)

      val vmList = utils.createNwVmList(faas_vm_count, faas_host_mips, faas_vm_pe_count, faas_vm_ram, faas_vm_bw, faas_vm_size, faasCloudletSchedulingType)
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
