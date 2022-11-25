package CloudOrg.Simulations.StarTopologySimulations

import CloudOrg.Brokers.TopologyAwareDatacenterBroker
import CloudOrg.Datacenters.StarNetworkDatacenter
import CloudOrg.Applications.ThreeTierApplication
import CloudOrg.HelperUtils.CreateLogger
import CloudOrg.utils
import org.cloudbus.cloudsim.allocationpolicies.{VmAllocationPolicyRandom, VmAllocationPolicySimple}
import org.cloudbus.cloudsim.brokers.{DatacenterBroker, DatacenterBrokerHeuristic}
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.distributions.UniformDistr
import org.cloudbus.cloudsim.vms.{Vm, VmCost}
import org.cloudsimplus.autoscaling.HorizontalVmScalingSimple
import org.cloudsimplus.builders.tables.CloudletsTableBuilder
import org.cloudsimplus.heuristics.CloudletToVmMappingSimulatedAnnealing

import scala.util.Random
import scala.jdk.CollectionConverters.*

object StarTopologyThreeTierComplexAppSimulation {
  val logger = CreateLogger(classOf[StarTopologyThreeTierComplexAppSimulation.type])

  val tree_count = 3
  val hosts_count = 9
  val host_mips = 1000
  val host_pe_count = 4
  val host_ram = 16_384
  val host_bw = 1000
  val host_storage = 1_000_000l

  val vm_count = 18
  val vm_pe_count = 2
  val vm_ram = 4000
  val vm_bw = 1
  val vm_size = 20_000

  val client_cloudlet_task_count = 1
  val server_cloudlet_task_count = 2
  val app_cloudlet_tasks_count = client_cloudlet_task_count + server_cloudlet_task_count

  val app_cloutlet_count = 50
  val cloudlet_count = app_cloudlet_tasks_count * app_cloutlet_count
  val cloudlet_pe_count = 1
  val cloudlet_length = 1000
  val cloudlet_file_size = 200
  val cloudlet_output_size = 500

  val connection_bw = 250.0d
  val connection_latency = 1.0d

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

    //val datacenter = utils.createNwDataCenter(simulation, hostList, 1.0)
    val datacenter = StarNetworkDatacenter(simulation, hostList, VmAllocationPolicySimple())
    utils.setDatacenterCost(datacenter, cost_per_sec, cost_per_mem, cost_per_storage, cost_per_bw)

    val vmList = utils.createNwVmList(vm_count, host_mips, vm_pe_count, vm_ram, vm_bw, vm_size, utils.SchedulerType.TIMESHARED)
    vmList.asScala.foreach(vm => {
      utils.createHorizontalVmScaling(vm, host_mips, vm_pe_count, vm_ram, vm_bw, vm_size, utils.SchedulerType.TIMESHARED, cpu_overload_threshold)
      utils.createVerticalRamScalingForVm(vm, ram_scaling_factor, ram_upper_utilization_threshold, ram_lower_utilization_threshold)
    })
    val cloudletList = utils.createNwCloudletList(cloudlet_count, cloudlet_length, cloudlet_pe_count, vmList, cloudlet_cpu_utilization, cloudlet_initial_ram_utilization, cloudlet_max_ram_utilization, cloudlet_bw_utilization)
    val clientCloudlets = cloudletList.subList(0, app_cloutlet_count).asScala.toList
    val otherCloudlets = Random.shuffle(cloudletList.subList(app_cloutlet_count, cloudletList.size).asScala.toList)

    Range(0, clientCloudlets.length).map(i => {
      ThreeTierApplication.createAppWorkFlow(clientCloudlets(i), otherCloudlets(server_cloudlet_task_count * i), otherCloudlets((server_cloudlet_task_count * i) + 1))
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
