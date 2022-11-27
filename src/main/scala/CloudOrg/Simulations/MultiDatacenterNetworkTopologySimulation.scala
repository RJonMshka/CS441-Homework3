package CloudOrg.Simulations

import CloudOrg.HelperUtils.{CreateLogger, ObtainConfigReference, utils}
import CloudOrg.Brokers.{TopologyAwareBrokerBestFit, TopologyAwareDatacenterBroker}
import CloudOrg.Datacenters.{BusNetworkDatacenter, HybridNetworkDatacenter, RingNetworkDatacenter, StarNetworkDatacenter, TreeNetworkDatacenter}
import CloudOrg.Applications.{MapReduceJob, ThreeTierApplication}
import org.cloudbus.cloudsim.allocationpolicies.{VmAllocationPolicyBestFit, VmAllocationPolicyRandom, VmAllocationPolicySimple}
import org.cloudbus.cloudsim.brokers.{DatacenterBroker, DatacenterBrokerSimple}
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.distributions.UniformDistr
import org.cloudbus.cloudsim.vms.{Vm, VmCost}
import org.cloudsimplus.autoscaling.HorizontalVmScalingSimple
import org.cloudsimplus.builders.tables.CloudletsTableBuilder
import org.cloudsimplus.heuristics.CloudletToVmMappingSimulatedAnnealing

import scala.util.Random
import scala.jdk.CollectionConverters.*

object MultiDatacenterNetworkTopologySimulation {
  val logger = CreateLogger(classOf[MultiDatacenterNetworkTopologySimulation.type])
  val config = ObtainConfigReference("cloudOrganizationSimulations").get
  val multiDCSimConfig = config.getConfig("cloudOrganizationSimulations.multiDatacenter")
  val treeNetworkConfig = config.getConfig("cloudOrganizationSimulations.treeTopology")
  val mapReduceConfig = config.getConfig("cloudOrganizationSimulations.mapReduce")
  val threeTierAppConfig = config.getConfig("cloudOrganizationSimulations.threeTier")

  // tree topology specific
  val tree_count = treeNetworkConfig.getInt("tree_count")

  // host variables
  val hosts_count = multiDCSimConfig.getInt("hosts_count")
  val host_mips = multiDCSimConfig.getInt("host_mips")
  val host_pe_count = multiDCSimConfig.getInt("host_pe_count")
  val host_ram = multiDCSimConfig.getInt("host_ram")
  val host_bw = multiDCSimConfig.getLong("host_bw")
  val host_storage = multiDCSimConfig.getLong("host_storage")
  // dc variable
  val datacenter_count = multiDCSimConfig.getInt("datacenter_count")
  // vm variables
  val vm_count = multiDCSimConfig.getInt("vm_count") * datacenter_count
  val vm_pe_count = multiDCSimConfig.getInt("vm_pe_count")
  val vm_ram = multiDCSimConfig.getInt("vm_ram")
  val vm_bw = multiDCSimConfig.getInt("vm_bw")
  val vm_size = multiDCSimConfig.getInt("vm_size")

  // simple tasks count
  val simple_cloudlet_count = multiDCSimConfig.getInt("simple_cloudlet_count")

  // three tier job
  val client_cloudlet_task_count = threeTierAppConfig.getInt("client_cloudlets")
  val server_cloudlet_task_count = threeTierAppConfig.getInt("server_cloudlets")
  val client_server_task_cloudlets_count = client_cloudlet_task_count + server_cloudlet_task_count
  val client_server_app_count = multiDCSimConfig.getInt("client_server_app_count")
  val client_server_cloudlet_count = client_server_task_cloudlets_count * client_server_app_count

  // map reduce job
  val mapper_cloudlet_count = mapReduceConfig.getInt("mapper_cloudlets")
  val reducer_cloudlet_count = mapReduceConfig.getInt("reducer_cloudlets")
  val map_reduce_app_cloudlet_count = mapper_cloudlet_count + reducer_cloudlet_count
  val map_reduce_app_count = multiDCSimConfig.getInt("map_reduce_app_count")
  val map_reduce_cloudlet_count = map_reduce_app_count * map_reduce_app_cloudlet_count

  val cloudlet_pe_count = multiDCSimConfig.getInt("cloudlet_pe_count")
  val cloudlet_length = multiDCSimConfig.getInt("cloudlet_length")

  // Utilization
  val cloudlet_cpu_utilization = multiDCSimConfig.getDouble("cloudlet_cpu_utilization")
  val cloudlet_ram_utilization = multiDCSimConfig.getDouble("cloudlet_ram_utilization")
  val cloudlet_bw_utilization = multiDCSimConfig.getDouble("cloudlet_bw_utilization")
  val cloudlet_initial_ram_utilization = multiDCSimConfig.getDouble("cloudlet_initial_ram_utilization")
  val cloudlet_max_ram_utilization = multiDCSimConfig.getDouble("cloudlet_max_ram_utilization")

  // inter datacenter and broker connection variables
  val inter_dc_connection_bw = multiDCSimConfig.getDouble("inter_dc_connection_bw")
  val inter_dc_connection_latency = multiDCSimConfig.getDouble("inter_dc_connection_latency")
  val dc_broker_connection_bw = multiDCSimConfig.getDouble("dc_broker_connection_bw")
  val dc_broker_connection_latency = multiDCSimConfig.getDouble("dc_broker_connection_latency")

  // cost
  val cost_per_sec = multiDCSimConfig.getDouble("cost_per_sec")
  val cost_per_mem = multiDCSimConfig.getDouble("cost_per_mem")
  val cost_per_storage = multiDCSimConfig.getDouble("cost_per_storage")
  val cost_per_bw = multiDCSimConfig.getDouble("cost_per_bw")

  // scaling
  // horizontal scaling
  val cpu_overload_threshold = multiDCSimConfig.getDouble("cpu_overload_threshold")
  // vertical ram scaling
  val ram_scaling_factor = multiDCSimConfig.getDouble("ram_scaling_factor")
  val ram_upper_utilization_threshold = multiDCSimConfig.getDouble("ram_upper_utilization_threshold")
  val ram_lower_utilization_threshold = multiDCSimConfig.getDouble("ram_lower_utilization_threshold")

  // datacenter allocation Policy
  val allocationPolicyType = multiDCSimConfig.getString("allocationPolicyType")
  // vm scheduling policy
  val vmSchedulerType = multiDCSimConfig.getString("vmSchedulerType")
  // cloudlet scheduling policy
  val cloudletSchedulerType = multiDCSimConfig.getString("cloudletSchedulerType")

  def main(args: Array[String]): Unit = {

    // creating simulation object
    val simulation: CloudSim = CloudSim()
    // broker object representing cloud customer
    val broker = TopologyAwareBrokerBestFit(simulation)

    // creating host list for each datacenter
    val hostList1 = utils.createNwHostList(hosts_count, host_pe_count, host_mips, host_ram, host_bw, host_storage, vmSchedulerType)
    val hostList2 = utils.createNwHostList(hosts_count, host_pe_count, host_mips, host_ram, host_bw, host_storage, vmSchedulerType)
    val hostList3 = utils.createNwHostList(hosts_count, host_pe_count, host_mips, host_ram, host_bw, host_storage, vmSchedulerType)
    val hostList4 = utils.createNwHostList(hosts_count, host_pe_count, host_mips, host_ram, host_bw, host_storage, vmSchedulerType)
    val hostList5 = utils.createNwHostList(hosts_count, host_pe_count, host_mips, host_ram, host_bw, host_storage, vmSchedulerType)

    // setting up datacenters
    val datacenter1 = utils.createNwDatacenter(utils.NetworkDatacenterType.STAR, simulation, hostList1, utils.getAllocationPolicy(allocationPolicyType), tree_count)
    utils.setDatacenterCost(datacenter1, cost_per_sec, cost_per_mem, cost_per_storage, cost_per_bw)

    val datacenter2 = utils.createNwDatacenter(utils.NetworkDatacenterType.STAR, simulation, hostList2, utils.getAllocationPolicy(allocationPolicyType), tree_count)
    utils.setDatacenterCost(datacenter2, cost_per_sec, cost_per_mem, cost_per_storage, cost_per_bw)

    val datacenter3 = utils.createNwDatacenter(utils.NetworkDatacenterType.STAR, simulation, hostList3, utils.getAllocationPolicy(allocationPolicyType), tree_count)
    utils.setDatacenterCost(datacenter3, cost_per_sec, cost_per_mem, cost_per_storage, cost_per_bw)

    val datacenter4 = utils.createNwDatacenter(utils.NetworkDatacenterType.STAR, simulation, hostList4, utils.getAllocationPolicy(allocationPolicyType), tree_count)
    utils.setDatacenterCost(datacenter4, cost_per_sec, cost_per_mem, cost_per_storage, cost_per_bw)

    val datacenter5 = utils.createNwDatacenter(utils.NetworkDatacenterType.STAR, simulation, hostList5, utils.getAllocationPolicy(allocationPolicyType), tree_count)
    utils.setDatacenterCost(datacenter5, cost_per_sec, cost_per_mem, cost_per_storage, cost_per_bw)

    // configure inter-datacenter network topology
    utils.configureNetwork(simulation, datacenter1, datacenter2, datacenter3, datacenter4, datacenter5, broker, inter_dc_connection_latency, inter_dc_connection_bw, inter_dc_connection_latency, inter_dc_connection_bw)

    // creating VMs and applying auto-scaling parameters are well
    val vmList = utils.createNwVmList(vm_count, host_mips, vm_pe_count, vm_ram, vm_bw, vm_size, cloudletSchedulerType)
    vmList.asScala.foreach(vm => {
      utils.createHorizontalVmScaling(vm, host_mips, vm_pe_count, vm_ram, vm_bw, vm_size, cloudletSchedulerType, cpu_overload_threshold)
      utils.createVerticalRamScalingForVm(vm, ram_scaling_factor, ram_upper_utilization_threshold, ram_lower_utilization_threshold)
    })

    // creating different types of cloudlets
    val simpleCloudletList = utils.createCloudletList(simple_cloudlet_count, cloudlet_length, cloudlet_pe_count, cloudlet_cpu_utilization, cloudlet_initial_ram_utilization, cloudlet_max_ram_utilization, cloudlet_bw_utilization)
    val totalNwCloudlets = map_reduce_cloudlet_count + client_server_cloudlet_count
    val nwCloudletList = utils.createNwCloudletList(totalNwCloudlets, cloudlet_length, cloudlet_pe_count, vmList, cloudlet_cpu_utilization, cloudlet_initial_ram_utilization, cloudlet_max_ram_utilization, cloudlet_bw_utilization)
    val mapReduceCloudletList = nwCloudletList.subList(0, map_reduce_cloudlet_count).asScala.toList
    val clientServerCloudletList = nwCloudletList.subList(map_reduce_cloudlet_count, totalNwCloudlets).asScala.toList

    // setting up tasks for map reduce cloudlets
    Range(0, map_reduce_app_count).map(i => {
      MapReduceJob.createMapReduceTasks(mapReduceCloudletList(map_reduce_app_cloudlet_count * i), mapReduceCloudletList((map_reduce_app_cloudlet_count * i) + 1), mapReduceCloudletList((map_reduce_app_cloudlet_count * i) + 2), mapReduceCloudletList((map_reduce_app_cloudlet_count * i) + 3))
    })

    // setting up tasks for client server app cloudlets
    Range(0, client_server_app_count).map(i => {
      ThreeTierApplication.createAppWorkFlow(clientServerCloudletList(client_server_task_cloudlets_count * i), clientServerCloudletList((client_server_task_cloudlets_count * i) + 1), clientServerCloudletList((client_server_task_cloudlets_count * i) + 2))
    })

    // concat all cloudlet list
    val newCloudletList = simpleCloudletList.asScala.concat(mapReduceCloudletList).concat(clientServerCloudletList)

    // submitting VMs and cloudlets to broker
    broker.submitVmList(vmList)
    broker.submitCloudletList(newCloudletList.toList.asJava)

    // finally, simulation starts
    simulation.start

    val finishedCloudlets = broker.getCloudletFinishedList
    // printing table of cloudlet execution
    CloudletsTableBuilder(finishedCloudlets).build
    // printing other performance related parameters - cost, power consumption and utilization
    logger.info("<---------- HOSTS POWER AND CPU CONSUMPTION FOR STAR NETWORK DATACENTER ------->")
    hostList1.asScala.foreach(utils.printHostPowerConsumptionAndCpuUtilization)
    logger.info("<---------- HOSTS POWER AND CPU CONSUMPTION FOR RING NETWORK DATACENTER ------->")
    hostList2.asScala.foreach(utils.printHostPowerConsumptionAndCpuUtilization)
    logger.info("<---------- HOSTS POWER AND CPU CONSUMPTION FOR BUS NETWORK DATACENTER ------->")
    hostList3.asScala.foreach(utils.printHostPowerConsumptionAndCpuUtilization)
    logger.info("<---------- HOSTS POWER AND CPU CONSUMPTION FOR TREE NETWORK DATACENTER ------->")
    hostList4.asScala.foreach(utils.printHostPowerConsumptionAndCpuUtilization)
    logger.info("<---------- HOSTS POWER AND CPU CONSUMPTION FOR HYBRID NETWORK DATACENTER ------->")
    hostList5.asScala.foreach(utils.printHostPowerConsumptionAndCpuUtilization)
    logger.info("<---------- VMS POWER AND CPU CONSUMPTION ------->")
    vmList.asScala.foreach(utils.printVmPowerConsumptionAndCpuUtilization)
    logger.info("<-------- RESOURCE BILLING INFORMATION ------------------>")
    utils.printTotalCostForVms(broker)
  }
}
