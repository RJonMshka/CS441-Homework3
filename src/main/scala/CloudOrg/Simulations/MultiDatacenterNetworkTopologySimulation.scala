package CloudOrg.Simulations

import CloudOrg.HelperUtils.CreateLogger
import CloudOrg.Brokers.{TopologyAwareBrokerBestFit, TopologyAwareDatacenterBroker}
import CloudOrg.Datacenters.{BusNetworkDatacenter, HybridNetworkDatacenter, RingNetworkDatacenter, StarNetworkDatacenter, TreeNetworkDatacenter}
import CloudOrg.Applications.{MapReduceJob, ThreeTierApplication}
import CloudOrg.utils
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

  // host variables
  val tree_count = 3
  val hosts_count = 9
  val host_mips = 1000
  val host_pe_count = 4
  val host_ram = 16_384
  val host_bw = 1_000_000l
  val host_storage = 1_000_000l
  
  val datacenter_count = 5

  // vm variables
  val vm_count = 18 * datacenter_count
  val vm_pe_count = 2
  val vm_ram = 2048
  val vm_bw = 10000
  val vm_size = 20_000

  // simple tasks count
  val simple_cloudlet_count = 250

  // three tier job
  val client_cloudlet_task_count = 1
  val server_cloudlet_task_count = 2
  val client_server_task_cloudlets_count = client_cloudlet_task_count + server_cloudlet_task_count
  val client_server_app_count = 100
  val client_server_cloudlet_count = client_server_task_cloudlets_count * client_server_app_count

  // map reduce job
  val mapper_cloudlet_count = 3
  val reducer_cloudlet_count = 1
  val map_reduce_app_cloudlet_count = mapper_cloudlet_count + reducer_cloudlet_count
  val map_reduce_app_count = 100
  val map_reduce_cloudlet_count = map_reduce_app_count * map_reduce_app_cloudlet_count

  val cloudlet_pe_count = 1
  val cloudlet_length = 500
  val cloudlet_file_size = 200
  val cloudlet_output_size = 500

  // inter datacenter connection variables
  val connection_bw = 250.0d
  val connection_latency = 1.0d

  // Utilization
  val cloudlet_cpu_utilization = 0.9
  val cloudlet_ram_utilization = 0.5
  val cloudlet_bw_utilization = 0.3
  val cloudlet_initial_ram_utilization = 0.1
  val cloudlet_max_ram_utilization = 0.9

  // cost
  val cost_per_sec = 0.001
  val cost_per_mem = 0.001
  val cost_per_storage = 0.0001
  val cost_per_bw = 0.01

  // Simulated annealing heuristic params
  val initial_temperature = 0.1
  val cold_temperature = 0.001
  val cooling_rate = 0.01
  val number_of_searches = 50

  // scaling
  // horizontal scaling
  val cpu_overload_threshold = 0.8
  // vertical ram scaling
  val ram_scaling_factor = 0.1
  val ram_upper_utilization_threshold = 0.8
  val ram_lower_utilization_threshold = 0.3

  def main(args: Array[String]): Unit = {

    // creating simulation object
    val simulation: CloudSim = CloudSim()
    // broker object representing cloud customer
    val broker = TopologyAwareBrokerBestFit(simulation)

    // creating host list for each datacenter
    val hostList1 = utils.createNwHostList(hosts_count, host_pe_count, host_mips, host_ram, host_bw, host_storage, utils.SchedulerType.TIMESHARED)
    val hostList2 = utils.createNwHostList(hosts_count, host_pe_count, host_mips, host_ram, host_bw, host_storage, utils.SchedulerType.TIMESHARED)
    val hostList3 = utils.createNwHostList(hosts_count, host_pe_count, host_mips, host_ram, host_bw, host_storage, utils.SchedulerType.TIMESHARED)
    val hostList4 = utils.createNwHostList(hosts_count, host_pe_count, host_mips, host_ram, host_bw, host_storage, utils.SchedulerType.TIMESHARED)
    val hostList5 = utils.createNwHostList(hosts_count, host_pe_count, host_mips, host_ram, host_bw, host_storage, utils.SchedulerType.TIMESHARED)

    // setting up datacenters
    val datacenter1 = StarNetworkDatacenter(simulation, hostList1, VmAllocationPolicyBestFit())
    utils.setDatacenterCost(datacenter1, cost_per_sec, cost_per_mem, cost_per_storage, cost_per_bw)

    val datacenter2 = RingNetworkDatacenter(simulation, hostList2, VmAllocationPolicyBestFit())
    utils.setDatacenterCost(datacenter2, cost_per_sec, cost_per_mem, cost_per_storage, cost_per_bw)

    val datacenter3 = BusNetworkDatacenter(simulation, hostList3, VmAllocationPolicyBestFit())
    utils.setDatacenterCost(datacenter3, cost_per_sec, cost_per_mem, cost_per_storage, cost_per_bw)

    val datacenter4 = TreeNetworkDatacenter(simulation, hostList4, VmAllocationPolicyBestFit(), tree_count)
    utils.setDatacenterCost(datacenter4, cost_per_sec, cost_per_mem, cost_per_storage, cost_per_bw)

    val datacenter5 = HybridNetworkDatacenter(simulation, hostList5, VmAllocationPolicyBestFit())
    utils.setDatacenterCost(datacenter5, cost_per_sec, cost_per_mem, cost_per_storage, cost_per_bw)

    // configure inter-datacenter network topology
    utils.configureNetwork(simulation, datacenter1, datacenter2, datacenter3, datacenter4, datacenter5, broker, connection_latency, connection_bw, connection_latency, connection_bw)

    // creating VMs and applying auto-scaling parameters are well
    val vmList = utils.createNwVmList(vm_count, host_mips, vm_pe_count, vm_ram, vm_bw, vm_size, utils.SchedulerType.SPACESHARED)
    vmList.asScala.foreach(vm => {
      utils.createHorizontalVmScaling(vm, host_mips, vm_pe_count, vm_ram, vm_bw, vm_size, utils.SchedulerType.SPACESHARED, cpu_overload_threshold)
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
