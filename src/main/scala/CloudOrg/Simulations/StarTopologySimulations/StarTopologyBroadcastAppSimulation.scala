package CloudOrg.Simulations.StarTopologySimulations

import CloudOrg.utils
import CloudOrg.Brokers.TopologyAwareDatacenterBroker
import CloudOrg.Datacenters.StarNetworkDatacenter
import CloudOrg.HelperUtils.CreateLogger
import CloudOrg.Applications.BroadcastMessageJob
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple
import org.cloudbus.cloudsim.brokers.DatacenterBrokerHeuristic
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudsimplus.builders.tables.CloudletsTableBuilder

import scala.util.Random
import scala.jdk.CollectionConverters.*

object StarTopologyBroadcastAppSimulation {
  val logger = CreateLogger(classOf[StarTopologyBroadcastAppSimulation.type])
  val tree_count = 3
  val hosts_count = 9
  val host_mips = 1000
  val host_pe_count = 4
  val host_ram = 16_384
  val host_bw = 10000
  val host_storage = 1_000_000l

  val vm_count = 18
  val vm_pe_count = 2
  val vm_ram = 1024
  val vm_bw = 10
  val vm_size = 20_000

  val sender_cloudlet_count = 1
  val receiver_cloudlet_count = 10
  val app_cloudlet_count = sender_cloudlet_count + receiver_cloudlet_count

  val app_count = 7
  val cloudlet_count = app_count * app_cloudlet_count
  val cloudlet_pe_count = 1
  val cloudlet_length = 1000
  val cloudlet_file_size = 200
  val cloudlet_output_size = 500

  val connection_bw = 250.0d
  val connection_latency = 100.0d

  // Utilization
  val cloudlet_cpu_utilization = 0.8
  val cloudlet_ram_utilization = 0.5
  val cloudlet_bw_utilization = 0.3
  val cloudlet_initial_ram_utilization = 0.1
  val cloudlet_max_ram_utilization = 0.8

  // cost
  val cost_per_sec = 0.001
  val cost_per_mem = 0.01
  val cost_per_storage = 0.0001
  val cost_per_bw = 0.01

  // Simulated annealing heuristic params
  val initial_temperature = 0.1
  val cold_temperature = 0.0001
  val cooling_rate = 0.001
  val number_of_searches = 100

  // scaling
  // horizontal scaling
  val cpu_overload_threshold = 0.8
  // vertical ram scaling
  val ram_scaling_factor = 0.1
  val ram_upper_utilization_threshold = 0.8
  val ram_lower_utilization_threshold = 0.3


  def main(args: Array[String]): Unit = {

    val simulation: CloudSim = CloudSim()
    val broker = DatacenterBrokerHeuristic(simulation)
    utils.setSimulatedAnnealingHeuristicForBroker(broker, initial_temperature, cold_temperature, cooling_rate, number_of_searches)
    val hostList = utils.createNwHostList(hosts_count, host_pe_count, host_mips, host_ram, host_bw, host_storage, utils.SchedulerType.TIMESHARED)

    val datacenter = StarNetworkDatacenter(simulation, hostList, VmAllocationPolicySimple())
    utils.setDatacenterCost(datacenter, cost_per_sec, cost_per_mem, cost_per_storage, cost_per_bw)

    val vmList = utils.createNwVmList(vm_count, host_mips, vm_pe_count, vm_ram, vm_bw, vm_size, utils.SchedulerType.TIMESHARED)
    vmList.asScala.foreach(vm => {
      utils.createHorizontalVmScaling(vm, host_mips, vm_pe_count, vm_ram, vm_bw, vm_size, utils.SchedulerType.TIMESHARED, cpu_overload_threshold)
      utils.createVerticalRamScalingForVm(vm, ram_scaling_factor, ram_upper_utilization_threshold, ram_lower_utilization_threshold)
    })
    val cloudletList = utils.createNwCloudletList(cloudlet_count, cloudlet_length, cloudlet_pe_count, vmList, cloudlet_cpu_utilization, cloudlet_initial_ram_utilization, cloudlet_max_ram_utilization, cloudlet_bw_utilization)
    val randomCloudlets = Random.shuffle(cloudletList.asScala.toList).asJava

    Range(0, app_count).map(i => {
      BroadcastMessageJob.createBroadcastMessageTasks(randomCloudlets.get(app_cloudlet_count * i), randomCloudlets.subList((app_cloudlet_count * i)+ 1, ((app_cloudlet_count * i)+ 1) + receiver_cloudlet_count) )
    })
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
}