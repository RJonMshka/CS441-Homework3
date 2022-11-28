package CloudOrg.HelperUtils

import org.cloudbus.cloudsim.allocationpolicies.*
import org.cloudbus.cloudsim.brokers.{DatacenterBroker, DatacenterBrokerHeuristic, DatacenterBrokerSimple}
import org.cloudbus.cloudsim.cloudlets.network.{CloudletExecutionTask, CloudletReceiveTask, CloudletSendTask, NetworkCloudlet}
import org.cloudbus.cloudsim.cloudlets.{Cloudlet, CloudletSimple}
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.network.NetworkDatacenter
import org.cloudbus.cloudsim.datacenters.{Datacenter, DatacenterSimple}
import org.cloudbus.cloudsim.distributions.UniformDistr
import org.cloudbus.cloudsim.hosts.network.NetworkHost
import org.cloudbus.cloudsim.hosts.{Host, HostSimple}
import org.cloudbus.cloudsim.network.topologies.BriteNetworkTopology
import org.cloudbus.cloudsim.power.models.PowerModelHostSimple
import org.cloudbus.cloudsim.provisioners.ResourceProvisionerSimple
import org.cloudbus.cloudsim.resources.{Pe, PeSimple, Ram}
import org.cloudbus.cloudsim.schedulers.cloudlet.{CloudletScheduler, CloudletSchedulerCompletelyFair, CloudletSchedulerSpaceShared, CloudletSchedulerTimeShared}
import org.cloudbus.cloudsim.schedulers.vm.{VmScheduler, VmSchedulerSpaceShared, VmSchedulerTimeShared}
import org.cloudbus.cloudsim.utilizationmodels.{UtilizationModelDynamic, UtilizationModelFull}
import org.cloudbus.cloudsim.vms.network.NetworkVm
import org.cloudbus.cloudsim.vms.{Vm, VmCost, VmSimple}
import org.cloudsimplus.autoscaling.{HorizontalVmScalingSimple, VerticalVmScalingSimple}
import org.cloudsimplus.builders.tables.CloudletsTableBuilder
import org.cloudsimplus.heuristics.CloudletToVmMappingSimulatedAnnealing
import org.slf4j.Logger
import CloudOrg.Datacenters.{BusNetworkDatacenter, HybridNetworkDatacenter, RingNetworkDatacenter, StarNetworkDatacenter, TreeNetworkDatacenter}
import CloudOrg.Schedulers.Vm.{VmSchedulerPseudoRandom, VmSchedulerRandom}
import com.typesafe.config.Config

import java.util
import java.util.function.{Predicate, Supplier}
import scala.jdk.CollectionConverters.*

// This object represent utilities used by all simulations and classes in this project
object utils {

  val logger: Logger = CreateLogger(classOf[utils.type])
  val config: Config = ObtainConfigReference("cloudOrganizationSimulations").get
  val powerConfig: Config = config.getConfig("cloudOrganizationSimulations.power")
  val allocationConfig: Config = config.getConfig("cloudOrganizationSimulations.allocation")
  val vmSchedulerConfig: Config = config.getConfig("cloudOrganizationSimulations.vmscheduler")

  // Power model specific varaibles
  val STATIC_POWER: Int = powerConfig.getInt("static_power")
  val MAX_POWER: Int = powerConfig.getInt("max_power")

  val HOST_STARTUP_DELAY: Int = powerConfig.getInt("startup_delay")
  val HOST_SHUTDOWN_DELAY: Int = powerConfig.getInt("shutdown_delay")
  val HOST_STARTUP_POWER: Int = powerConfig.getInt("startup_power")
  val HOST_SHUTDOWN_POWER: Int = powerConfig.getInt("shutdown_power")

  // random see for allocation policy
  val randomAllocationPolicySeed: Int = allocationConfig.getInt("random_allocation_policy_seed")

  // pseudo random and random scheduling specific variables
  val vmChance: Double = vmSchedulerConfig.getDouble("vmChanceForPseudoRandom")
  val vmMigrationOverhead: Double = vmSchedulerConfig.getDouble("vmMigrationCpuOverhead")

  // type of network datacenters
  enum NetworkDatacenterType:
    case STAR, TREE, RING, BUS, HYBRID, NORMAL

  // This method creates a new network datacenter based on the arguments passed
  def createNwDatacenter(nwDatacenterType: NetworkDatacenterType, simulation: CloudSim, hostList: util.List[NetworkHost], vmAllocationPolicy: VmAllocationPolicy, treeSize: Int): NetworkDatacenter =
    nwDatacenterType match
      case NetworkDatacenterType.STAR => StarNetworkDatacenter(simulation, hostList, vmAllocationPolicy)
      case NetworkDatacenterType.RING => RingNetworkDatacenter(simulation, hostList, vmAllocationPolicy)
      case NetworkDatacenterType.BUS => BusNetworkDatacenter(simulation, hostList, vmAllocationPolicy)
      case NetworkDatacenterType.TREE => TreeNetworkDatacenter(simulation, hostList, vmAllocationPolicy, treeSize)
      case NetworkDatacenterType.HYBRID => HybridNetworkDatacenter(simulation, hostList, vmAllocationPolicy)
      case NetworkDatacenterType.NORMAL => NetworkDatacenter(simulation, hostList, vmAllocationPolicy)


  // This method is used to create broker
  def createBroker(simulation: CloudSim): DatacenterBroker =
    DatacenterBrokerSimple(simulation)


  // This method create PE (CPU cores) list with specific pe and mips (million instructions per sec) count
  def createPeList(pes: Int, mips: Long): util.List[Pe] =
    Range(0, pes).map(_ => PeSimple(mips)).toList.asJava

  /**
   * This method creates host list
   * @param hosts - no. of hosts
   * @param hostPes - host pes
   * @param hostMips - host mips (million instructions per sec)
   * @param hostRam - ram
   * @param hostBw - bandwidth for hosts
   * @param hostStorage - storage for host
   * @param vmScheduler - scheduler type for host - see types in config file
   * @return - Host list
   */
  def createHostList(hosts: Int, hostPes: Int, hostMips: Long, hostRam: Long, hostBw: Long, hostStorage: Long, vmScheduler: String): util.List[Host] =
    Range(0, hosts).map(i => {
      val host = createHost(hostPes, hostMips, hostRam, hostBw, hostStorage)
      setPowerModelForHost(host)
      host.setVmScheduler(getVmSchedulingPolicy(vmScheduler))
      host.enableUtilizationStats()
      host.setRamProvisioner(ResourceProvisionerSimple()).setBwProvisioner(ResourceProvisionerSimple())
      host.setId(i)
      host
    }).toList.asJava

  /**
   * This method creates host list of network hosts
   * @param hosts - no. of hosts
   * @param hostPes - host pes
   * @param hostMips - host mips (million instructions per sec)
   * @param hostRam - ram
   * @param hostBw - bandwidth for host
   * @param hostStorage - storage for host
   * @param vmScheduler - scheduler type for host - see types in config file
   * @return - NetworkHost list
   */
  def createNwHostList(hosts: Int, hostPes: Int, hostMips: Long, hostRam: Long, hostBw: Long, hostStorage: Long, vmScheduler: String): util.List[NetworkHost] =
    Range(0, hosts).map(i => {
      val host = createNwHost(hostPes, hostMips, hostRam, hostBw, hostStorage)
      setPowerModelForHost(host)
      host.setVmScheduler(getVmSchedulingPolicy(vmScheduler))
      host.enableUtilizationStats()
      host.setRamProvisioner(ResourceProvisionerSimple()).setBwProvisioner(ResourceProvisionerSimple())
      host.setId(i)
      host
    }).toList.asJava

  /**
   * This method create a single host
   * @param hostPes - host pes
   * @param hostMips - host mips (million instructions per sec)
   * @param hostRam - ram
   * @param hostBw - bandwidth for host
   * @param hostStorage - storage for host
   * @return Host
   */
  def createHost(hostPes: Int, hostMips: Long, hostRam: Long, hostBw: Long, hostStorage: Long): Host =
    val peList = createPeList(hostPes, hostMips)
    HostSimple(hostRam, hostBw, hostStorage, peList)

  /**
   * This method create a single network host
   * @param hostPes - host pes
   * @param hostMips - host mips (million instructions per sec)
   * @param hostRam - ram
   * @param hostBw - bandwidth for host
   * @param hostStorage - storage for host
   * @return NetworkHost
   */
  def createNwHost(hostPes: Int, hostMips: Long, hostRam: Long, hostBw: Long, hostStorage: Long): NetworkHost =
    val peList = createPeList(hostPes, hostMips)
    NetworkHost(hostRam, hostBw, hostStorage, peList)

  /**
   * This method create a Vm instance
   * @param mipsCapacity - VM mips (million instructions per sec)
   * @param peCount - vm pes
   * @return Vm
   */
  def createVm(mipsCapacity: Long, peCount: Int): Vm =
    VmSimple(mipsCapacity, peCount)

  /**
   * This method create a NetworkVm instance
   * @param mipsCapacity - VM mips (million instructions per sec)
   * @param peCount - vm pes
   * @return NetworkVm
   */
  def createNwVm(mipsCapacity: Long, peCount: Int): NetworkVm =
    NetworkVm(mipsCapacity, peCount)

  /**
   * This method create a Vm instance
   * @param mipsCapacity - VM mips (million instructions per sec)
   * @param peCount - vm pes
   * @param vmRam - vm ram
   * @param vmBw - vm bandwidth
   * @param vmSize - vm storage
   * @param cloudletScheduler - cloudlet scheduler type - see config file
   * @return Vm
   */
  def createVm(mipsCapacity: Long, peCount: Int, vmRam: Long, vmBw: Long, vmSize: Long, cloudletScheduler: String): Vm =
    val vm = VmSimple(mipsCapacity, peCount)
    setVmCharacteristics(vm, vmRam, vmBw, vmSize, cloudletScheduler)
    vm

  /**
   * This method create a NetworkVm instance
   * @param mipsCapacity - VM mips (million instructions per sec)
   * @param peCount - vm pes
   * @param vmRam - vm ram
   * @param vmBw - vm bandwidth
   * @param vmSize - vm storage
   * @param cloudletScheduler - cloudlet scheduler type - see config file
   * @return NetworkVm
   */
  def createNwVm(mipsCapacity: Long, peCount: Int, vmRam: Long, vmBw: Long, vmSize: Long, cloudletScheduler: String): NetworkVm =
    val vm = NetworkVm(mipsCapacity, peCount)
    setVmCharacteristics(vm, vmRam, vmBw, vmSize, cloudletScheduler)
    vm

  /**
   * This method create a VM list
   * @param vmCount - number of Vms
   * @param mipsCapacity - mips capacity of each VM
   * @param peCount - pe count for each VM
   * @return List[Vm]
   */
  def createVmList(vmCount: Int, mipsCapacity: Long, peCount: Int): util.List[Vm] =
    Range(0, vmCount).map(_ => createVm(mipsCapacity, peCount)).toList.asJava

  /**
   * This method create a NetworkVm list
   * @param vmCount - number of Vms
   * @param mipsCapacity - mips capacity of each VM
   * @param peCount - pe count for each VM
   * @return List[NetworkVm]
   */
  def createNwVmList(vmCount: Int, mipsCapacity: Long, peCount: Int): util.List[NetworkVm] =
    Range(0, vmCount).map(_ => createNwVm(mipsCapacity, peCount)).toList.asJava

  /**
   * This method create a NetworkVm list
   * @param vmCount - number of Vms
   * @param mipsCapacity - mips capacity of each VM
   * @param peCount - pe count for each VM
   * @param vmRam - ram for each vm
   * @param vmBw - bandwidth for each vm
   * @param vmSize -  storage for each vm
   * @param cloudletScheduler - cloudlet scheduler type for VM
   * @return List[NetworkVm]
   */
  def createNwVmList(vmCount: Int, mipsCapacity: Long, peCount: Int, vmRam: Long, vmBw: Long, vmSize: Long, cloudletScheduler: String): util.List[NetworkVm] =
    Range(0, vmCount).map(_ => createNwVm(mipsCapacity, peCount, vmRam, vmBw, vmSize, cloudletScheduler)).toList.asJava

  /**
   * This method create a Vm list
   * @param vmCount - number of Vms
   * @param mipsCapacity - mips capacity of each VM
   * @param peCount - pe count for each VM
   * @param vmRam - ram for each vm
   * @param vmBw - bandwidth for each vm
   * @param vmSize -  storage for each vm
   * @param cloudletScheduler - cloudlet scheduler type for VM
   * @return List[Vm]
   */
  def createVmList(vmCount: Int, mipsCapacity: Long, peCount: Int, vmRam: Long, vmBw: Long, vmSize: Long, cloudletScheduler: String): util.List[Vm] =
    Range(0, vmCount).map(_ => createVm(mipsCapacity, peCount, vmRam, vmBw, vmSize, cloudletScheduler)).toList.asJava

  /**
   * This method sets the characteristics of a particular VM
   * @param vm - vm whose characteristics need to be set
   * @param vmRam - ram to set
   * @param vmBw - bandwidth to set
   * @param vmSize - storage to set
   * @param cloudletScheduler - scheduler to set
   * @return Vm
   */
  def setVmCharacteristics(vm: Vm, vmRam: Long, vmBw: Long, vmSize: Long, cloudletScheduler: String): Vm =
    vm.setCloudletScheduler(getCloudletSchedulingPolicy(cloudletScheduler))
    vm.enableUtilizationStats()
    vm.setRam(vmRam).setBw(vmBw).setSize(vmSize)

  /**
   * This method creates a cloudlet
   * @param cloudletLength - million instructions for cloudlet to execute
   * @param cloudletPes - PEs that cloudlet demands
   * @return Cloudlet
   */
  def createCloudlet(cloudletLength: Long, cloudletPes: Int): Cloudlet = CloudletSimple(cloudletLength, cloudletPes)

  /**
   * This method creates a network cloudlet
   * @param cloudletLength - million instructions for cloudlet to execute
   * @param cloudletPes - PEs that cloudlet demands
   * @return NetworkCloudlet
   */
  def createNwCloudlet(cloudletLength: Long, cloudletPes: Int): NetworkCloudlet = NetworkCloudlet(cloudletLength, cloudletPes)

  /**
   * This method create a list of cloudlets
   * @param cloudletCount - no. of cloudlets to create
   * @param cloudletLength - million instructions for cloudlet to execute
   * @param cloudletPes - PEs that cloudlet demands
   * @return List[Cloudlet]
   */
  def createCloudletList(cloudletCount: Int, cloudletLength: Long, cloudletPes: Int): util.List[Cloudlet] =
    Range(0, cloudletCount).map(_ => createCloudlet(cloudletLength, cloudletPes)).toList.asJava

  /**
   * This method create a list of cloudlets
   * @param cloudletCount - no. of cloudlets to create
   * @param cloudletLength - million instructions for cloudlet to execute
   * @param cloudletPes - PEs that cloudlet demands
   * @param cpuUtilization - utilization for cpu between 0 and 1
   * @param ramUtilization - utilization for ram between 0 and 1
   * @param bwUtilization - utilization for bandwidth between 0 and 1
   * @return List[Cloudlet]
   */
  def createCloudletList(cloudletCount: Int, cloudletLength: Long, cloudletPes: Int, cpuUtilization: Double, ramUtilization: Double, bwUtilization: Double): util.List[Cloudlet] =
    Range(0, cloudletCount).map(_ => {
      val cloudlet = createCloudlet(cloudletLength, cloudletPes)
      cloudletUtilizationDynamic(cloudlet, cpuUtilization, ramUtilization, bwUtilization)
    }).toList.asJava

  /**
   * This method create a list of cloudlets
   * @param cloudletCount - no. of cloudlets to create
   * @param cloudletLength - million instructions for cloudlet to execute
   * @param cloudletPes - PEs that cloudlet demands
   * @param cloudletInputSize - input file size for cloudlet
   * @param cloudletOutputInputSize - output file size for cloudlet
   * @param cpuUtilization - utilization for cpu between 0 and 1
   * @param ramUtilization - utilization for ram between 0 and 1
   * @param bwUtilization - utilization for bandwidth between 0 and 1
   * @return List[Cloudlet]
   */
  def createCloudletList(cloudletCount: Int, cloudletLength: Long, cloudletPes: Int, cloudletInputSize: Int, cloudletOutputInputSize: Int, cpuUtilization: Double, ramUtilization: Double, bwUtilization: Double): util.List[Cloudlet] =
    Range(0, cloudletCount).map(_ => {
      val cloudlet = createCloudlet(cloudletLength, cloudletPes)
      cloudletSetSize(cloudlet, cloudletInputSize, cloudletOutputInputSize)
      cloudletUtilizationDynamic(cloudlet, cpuUtilization, ramUtilization, bwUtilization)
    }).toList.asJava

  /**
   * This method create a list of cloudlets
   * @param cloudletCount - no. of cloudlets to create
   * @param cloudletLength - million instructions for cloudlet to execute
   * @param cloudletPes - PEs that cloudlet demands
   * @param cpuUtilization - utilization for cpu between 0 and 1
   * @param initialRamUtilization - intial ram utilization b/w 0 and 1
   * @param maxRamUtilization - maximum ram utilization b/w 0 and 1
   * @param bwUtilization - utilization for bandwidth between 0 and 1
   * @return List[Cloudlet]
   */
  def createCloudletList(cloudletCount: Int, cloudletLength: Long, cloudletPes: Int, cpuUtilization: Double, initialRamUtilization: Double, maxRamUtilization: Double, bwUtilization: Double): util.List[Cloudlet] =
    Range(0, cloudletCount).map(_ => {
      val cloudlet = createCloudlet(cloudletLength, cloudletPes)
      cloudletUtilizationDynamic(cloudlet, cpuUtilization, initialRamUtilization, maxRamUtilization, bwUtilization)
    }).toList.asJava

  /**
   * This method create a list of cloudlets
   * @param cloudletCount - no. of cloudlets to create
   * @param cloudletLength - million instructions for cloudlet to execute
   * @param cloudletPes - PEs that cloudlet demands
   * @param cloudletInputSize - input file size for cloudlet
   * @param cloudletOutputInputSize - output file size for cloudlet
   * @param cpuUtilization - utilization for cpu between 0 and 1
   * @param initialRamUtilization - intial ram utilization b/w 0 and 1
   * @param maxRamUtilization - maximum ram utilization b/w 0 and 1
   * @param bwUtilization - utilization for bandwidth between 0 and 1
   * @return List[Cloudlet]
   */
  def createCloudletList(cloudletCount: Int, cloudletLength: Long, cloudletPes: Int, cloudletInputSize: Int, cloudletOutputInputSize: Int, cpuUtilization: Double, initialRamUtilization: Double, maxRamUtilization: Double, bwUtilization: Double): util.List[Cloudlet] =
    Range(0, cloudletCount).map(_ => {
      val cloudlet = createCloudlet(cloudletLength, cloudletPes)
      cloudletSetSize(cloudlet, cloudletInputSize, cloudletOutputInputSize)
      cloudletUtilizationDynamic(cloudlet, cpuUtilization, initialRamUtilization, maxRamUtilization, bwUtilization)
    }).toList.asJava

  /**
   * This method create a list of cloudlets
   * @param cloudletCount - no. of cloudlets to create
   * @param cloudletLength - million instructions for cloudlet to execute
   * @param cloudletPes - PEs that cloudlet demands
   * @param vmList - vm list
   * @return List[NetworkCloudlet]
   */
  def createNwCloudletList(cloudletCount: Int, cloudletLength: Long, cloudletPes: Int, vmList: util.List[NetworkVm]): util.List[NetworkCloudlet] =
    Range(0, cloudletCount).map(i => {
      val cloudlet = createNwCloudlet(cloudletLength, cloudletPes)
      cloudlet.setVm(vmList.get(i % vmList.size)).setId(i)
      cloudlet
    }).toList.asJava

  /**
   * This method create a list of cloudlets
   * @param cloudletCount - no. of cloudlets to create
   * @param cloudletLength - million instructions for cloudlet to execute
   * @param cloudletPes - PEs that cloudlet demands
   * @param vmList - vm list
   * @param cpuUtilization - utilization for cpu between 0 and 1
   * @param ramUtilization - utilization for ram between 0 and 1
   * @param bwUtilization - utilization for bandwidth between 0 and 1
   * @return List[NetworkCloudlet]
   */
  def createNwCloudletList(cloudletCount: Int, cloudletLength: Long, cloudletPes: Int, vmList: util.List[NetworkVm], cpuUtilization: Double, ramUtilization: Double, bwUtilization: Double): util.List[NetworkCloudlet] =
    Range(0, cloudletCount).map(i => {
      val cloudlet = createNwCloudlet(cloudletLength, cloudletPes)
      cloudlet.setVm(vmList.get(i % vmList.size)).setId(i)
      cloudletUtilizationDynamic(cloudlet, cpuUtilization, ramUtilization, bwUtilization)
      cloudlet
    }).toList.asJava

  /**
   * This method create a list of cloudlets
   * @param cloudletCount - no. of cloudlets to create
   * @param cloudletLength - million instructions for cloudlet to execute
   * @param cloudletPes - PEs that cloudlet demands
   * @param vmList - vm list
   * @param cpuUtilization - utilization for cpu between 0 and 1
   * @param initialRamUtilization - intial ram utilization b/w 0 and 1
   * @param maxRamUtilization - maximum ram utilization b/w 0 and 1
   * @param bwUtilization - utilization for bandwidth between 0 and 1
   * @return List[NetworkCloudlet]
   */
  def createNwCloudletList(cloudletCount: Int, cloudletLength: Long, cloudletPes: Int, vmList: util.List[NetworkVm], cpuUtilization: Double, initialRamUtilization: Double, maxRamUtilization: Double, bwUtilization: Double): util.List[NetworkCloudlet] =
    Range(0, cloudletCount).map(i => {
      val cloudlet = createNwCloudlet(cloudletLength, cloudletPes)
      cloudlet.setVm(vmList.get(i % vmList.size)).setId(i)
      cloudletUtilizationDynamic(cloudlet, cpuUtilization, initialRamUtilization, maxRamUtilization, bwUtilization)
      cloudlet
    }).toList.asJava

  /**
   * Create execution task for cloudlet
   * @param cloudlet - cloudlet to add execution task
   * @param taskLength - length of task in MI
   * @param taskMemory - memory the task requires
   */
  def createExecTaskForNwCloudlet(cloudlet: NetworkCloudlet, taskLength: Long, taskMemory: Long): Unit =
    val task = CloudletExecutionTask(cloudlet.getTasks.size, taskLength)
    task.setMemory(taskMemory)
    cloudlet.addTask(task)
    ()

  /**
   * Create send task for cloudlet
   * @param sourceCloudlet - source cloudlet
   * @param destCloudlet - destination cloudlet
   * @param taskMemory - memory the task requires
   * @param packetsToSend - packets to send
   * @param packetBytes - bytes in one packet
   */
  def createSendTaskForNwCloudlet(sourceCloudlet: NetworkCloudlet, destCloudlet: NetworkCloudlet, taskMemory: Long, packetsToSend: Int, packetBytes: Long): Unit =
    val task = CloudletSendTask(sourceCloudlet.getTasks.size)
    task.setMemory(taskMemory)
    sourceCloudlet.addTask(task)
    Range(0, packetsToSend).map(_ -> task.addPacket(destCloudlet, packetBytes))
    ()

  /**
   *
   * @param destCloudlet - destination cloudlet
   * @param sourceCloudlet - source cloudlet
   * @param taskMemory - memory the task requires
   * @param packetsToReceive - packets to receive
   */
  def createReceiveTaskForNwCloudlet(destCloudlet: NetworkCloudlet, sourceCloudlet: NetworkCloudlet, taskMemory: Long, packetsToReceive: Long): Unit =
    val task = CloudletReceiveTask(destCloudlet.getTasks.size, sourceCloudlet.getVm)
    task.setMemory(taskMemory)
    task.setExpectedPacketsToReceive(packetsToReceive)
    destCloudlet.addTask(task)
    ()

  /**
   * Set cloudlet utilization models
   * @param cloudlet - cloudlet whose utilization model needs to set
   * @param cpuUtilization - utilization for cpu between 0 and 1
   * @param ramUtilization - utilization for ram between 0 and 1
   * @param bwUtilization - utilization for bandwidth between 0 and 1
   * @return Cloudlet
   */
  def cloudletUtilizationDynamic(cloudlet: Cloudlet, cpuUtilization: Double, ramUtilization: Double, btwUtilization: Double): Cloudlet =
    cloudlet.setUtilizationModelRam(UtilizationModelDynamic(ramUtilization))
      .setUtilizationModelBw(UtilizationModelDynamic(btwUtilization))
      .setUtilizationModelCpu(UtilizationModelDynamic(cpuUtilization))

  /**
   * Set cloudlet utilization models
   * @param cloudlet - cloudlet whose utilization model needs to set
   * @param cpuUtilization - utilization for cpu between 0 and 1
   * @param initialRamUtilization - inital utilization for ram between 0 and 1
   * @param maxRamUtilization - maximum utilization for ram between 0 and 1
   * @param bwUtilization - utilization for bandwidth between 0 and 1
   * @return Cloudlet
   */
  def cloudletUtilizationDynamic(cloudlet: Cloudlet, cpuUtilization: Double, initialRamUtilization: Double, maxRamUtilization: Double, btwUtilization: Double): Cloudlet =
    cloudlet.setUtilizationModelRam(UtilizationModelDynamic(initialRamUtilization, maxRamUtilization))
      .setUtilizationModelBw(UtilizationModelDynamic(btwUtilization))
      .setUtilizationModelCpu(UtilizationModelDynamic(cpuUtilization))

  /**
   * This method sets cloudlet sizes
   * @param cloudlet - cloudlet whose size needs to be set
   * @param fileSize - input file size
   * @param outputSize - output file size
   * @return Cloudlet
   */
  def cloudletSetSize(cloudlet: Cloudlet, fileSize: Long, outputSize: Long): Cloudlet =
    cloudlet.setFileSize(fileSize)
      .setOutputSize(outputSize)

  /**
   * Sets power model for host
   * @param host - host whose power model needs to be set
   */
  def setPowerModelForHost(host: Host): Unit =
    val powerModel = PowerModelHostSimple(MAX_POWER, STATIC_POWER)
    powerModel.setStartupDelay(HOST_STARTUP_DELAY)
      .setShutDownDelay(HOST_SHUTDOWN_DELAY)
      .setStartupPower(HOST_STARTUP_POWER)
      .setShutDownPower(HOST_SHUTDOWN_POWER)

    host.setPowerModel(powerModel)
    logger.info(s"Power model set for host ${host.getId}")


  /**
   * print cpu utilization and power consumption metrics for a VM
   * @param vm - VM
   */
  def printVmPowerConsumptionAndCpuUtilization(vm: Vm): Unit =
    val powerModel = vm.getHost.getPowerModel.asInstanceOf[PowerModelHostSimple]
    val hostStaticPower = powerModel.getStaticPower
    val vmStaticPower = hostStaticPower / vm.getHost.getVmCreatedList.size

    val vmUtilPrecentMean = vm.getCpuUtilizationStats.getMean
    val vmRelCpuUtilization = vmUtilPrecentMean / vm.getHost.getVmCreatedList.size
    val vmPowerCons = powerModel.getPower(vmRelCpuUtilization) - hostStaticPower + vmStaticPower
    logger.info(
      f"VM ${vm.getId}%2d Mean CPU Usage is: ${vmUtilPrecentMean * 100.0}%4.1f%% and Power Consumption mean is: $vmPowerCons%4.0f Watt"
    )

  /**
   * print cpu utilization and power consumption metrics for a Host
   * @param host - Host
   */
  def printHostPowerConsumptionAndCpuUtilization(host: Host): Unit =
    val hostCpuStats = host.getCpuUtilizationStats
    val utilPercentMean = hostCpuStats.getMean
    val powerCons = host.getPowerModel.getPower(utilPercentMean)

    // cpu utilization by mips
    val mipsByPe = host.getTotalMipsCapacity / host.getNumberOfPes
    logger.info(
      f"Host ${host.getId}%2d | PE Amount: ${host.getNumberOfPes}%2d | MIPS by PE: $mipsByPe%1.0f | Mean CPU Usage is: ${utilPercentMean * 100.0}%4.1f%% | Power Consumption mean is: $powerCons%4.0f Watt"
    )

  /**
   * Sets datacenter cost
   * @param datacenter - datacenter
   * @param costPerSec - per second cost
   * @param costPerMemAccess - per memory access cost
   * @param costPerStorage - per storage cost
   * @param costPerBw - per bandwidth access cost
   */
  def setDatacenterCost(datacenter: Datacenter, costPerSec: Double, costPerMemAccess: Double, costPerStorage: Double, costPerBw: Double): Unit =
    datacenter.getCharacteristics
      .setCostPerSecond(costPerSec)
      .setCostPerMem(costPerMemAccess)
      .setCostPerStorage(costPerStorage)
      .setCostPerBw(costPerBw)
    ()

  /**
   * Set Simulated Annealing(SA) heuristics
   * @param broker - broker whose heuristic needs to be set
   * @param initialTemperature - initial temp for SA
   * @param coldTemperature - cold temp for SA
   * @param coolingRate - cooling rate for SA
   * @param numberOfsearches - number of searches in the search space
   */
  def setSimulatedAnnealingHeuristicForBroker(broker: DatacenterBrokerHeuristic, initialTemperature: Double, coldTemperature: Double, coolingRate: Double, numberOfsearches: Int): Unit =
    val heuristic = CloudletToVmMappingSimulatedAnnealing(initialTemperature, UniformDistr(0, 1))
    heuristic.setColdTemperature(coldTemperature)
    heuristic.setCoolingRate(coolingRate)
    heuristic.setSearchesByIteration(numberOfsearches)
    broker.setHeuristic(heuristic)

  /**
   * Create predicate/logic for Horizontal VM scaling
   * @param vm - VM to scale
   * @param utilizationThreshold - upper threshold for horizontal scaling
   * @return - a condition (Boolean) / predicate result
   */
  def horizontalVmScalingOutPredicate(vm: Vm, utilizationThreshold: Double): Boolean = vm.getCpuPercentUtilization > utilizationThreshold

  /**
   * This method sets horizontal scaling
   * @param vm - vm to attach scaling rule
   * @param mipsCapacity - vm mips capacity of new VM to be created
   * @param peCount - pe count of new VM to be created
   * @param vmRam - ram of new VM to be created
   * @param vmBw - bandwidth of new VM to be created
   * @param vmSize - storage of new VM to be created
   * @param cloudletScheduler - cloudlet scheduler type of new VM to be created
   * @param overloadingThreshold - threshold above which new VM to be created
   */
  def createHorizontalVmScaling(vm: Vm, mipsCapacity: Long, peCount: Int, vmRam: Long, vmBw: Long, vmSize: Long, cloudletScheduler: String, overloadingThreshold: Double): Unit =
    val horizontalVmScaling = HorizontalVmScalingSimple()
    val createVm = new Supplier[Vm]:
      override def get(): Vm = vm match
        case _: NetworkVm => utils.createNwVm(mipsCapacity, peCount, vmRam, vmBw, vmSize, cloudletScheduler)
        case _ => utils.createVm(mipsCapacity, peCount, vmRam, vmBw, vmSize, cloudletScheduler)
    val predicate = new Predicate[Vm] :
      override def test(t: Vm): Boolean = horizontalVmScalingOutPredicate(t, overloadingThreshold)
    horizontalVmScaling.setVmSupplier(createVm).setOverloadPredicate(predicate)
    vm.setHorizontalScaling(horizontalVmScaling)
    ()

  /**
   * Enables Vertical RAM scaling for particular VM
   * @param vm - vm which needs to be scaled
   * @param scalingFactor - scaling factor by which ram needs to be added or removed
   * @param upperThreshold - upper threshold for rule triggering
   * @param lowerThreshold - lower threshold for rule triggering
   */
  def createVerticalRamScalingForVm(vm: Vm, scalingFactor: Double, upperThreshold: Double, lowerThreshold: Double): Unit =
    val verticalRamScaling = VerticalVmScalingSimple(classOf[Ram], scalingFactor)
    verticalRamScaling.setLowerThresholdFunction((_: Vm) => lowerThreshold)
    verticalRamScaling.setUpperThresholdFunction((_: Vm) => upperThreshold)
    vm.setRamVerticalScaling(verticalRamScaling)
    ()

  /**
   * Prints total cost for broker
   * @param broker - broker
   */
  def printTotalCostForVms(broker: DatacenterBroker): Unit =
    val (processingCost, memCost, storageCost, bwCost, totalCost, totalNonIdleVms) = broker.getVmCreatedList.asScala.foldLeft((0.0, 0.0, 0.0, 0.0, 0.0, 0.0))((x, y) => {
      val vm = y.asInstanceOf[Vm]
      val cost = VmCost(vm)
      (
        x._1 + cost.getProcessingCost,
        x._2 + cost.getMemoryCost,
        x._3 + cost.getStorageCost,
        x._4 + cost.getBwCost,
        x._5 + cost.getTotalCost,
        x._6 + (if vm.getTotalExecutionTime > 0 then 1 else 0)
      )
    })
    logger.info(f"Non idle VM count is $totalNonIdleVms%2.0f out of ${broker.getVmsNumber}%2d created VMs")
    logger.info(f"Total processing cost is: $processingCost%2.2f units")
    logger.info(f"Total memory cost is: $memCost%2.2f units")
    logger.info(f"Total storage cost is: $storageCost%2.2f units")
    logger.info(f"Total bandwidth cost is: $bwCost%2.2f units")
    logger.info(f"Total cost is: $totalCost%2.2f units")

  /**
   * Configure 5 datacenters in a network - specifically in a ring fashion
   * Broker will be connect to all
   * @param simulation - simulation object
   * @param dc1 - datacenter 1
   * @param dc2 - datacenter 2
   * @param dc3 - datacenter 3
   * @param dc4 - datacenter 4
   * @param dc5 - datacenter 5
   * @param broker - broker
   * @param interDatacenterLatency - inter datacenter latency
   * @param interDatacenterBw - inter datacenter bandwidth
   * @param brokerDatacenterLatency - broker-datacenter latency
   * @param brokerDatacenterBw - broker-datacenter bandwidth
   */
  def configureNetwork(simulation: CloudSim, dc1: Datacenter, dc2: Datacenter, dc3: Datacenter, dc4: Datacenter, dc5: Datacenter, broker: DatacenterBroker, interDatacenterLatency: Double, interDatacenterBw: Double, brokerDatacenterLatency: Double, brokerDatacenterBw: Double): Unit =
    val nwTopology = BriteNetworkTopology()
    simulation.setNetworkTopology(nwTopology)
    nwTopology.addLink(broker, dc1, brokerDatacenterBw, brokerDatacenterLatency)
    nwTopology.addLink(broker, dc2, brokerDatacenterBw, brokerDatacenterLatency)
    nwTopology.addLink(broker, dc3, brokerDatacenterBw, brokerDatacenterLatency)
    nwTopology.addLink(broker, dc4, brokerDatacenterBw, brokerDatacenterLatency)
    nwTopology.addLink(broker, dc5, brokerDatacenterBw, brokerDatacenterLatency)

    nwTopology.addLink(dc1, dc2, interDatacenterBw, interDatacenterLatency)
    nwTopology.addLink(dc2, dc3, interDatacenterBw, interDatacenterLatency)
    nwTopology.addLink(dc3, dc4, interDatacenterBw, interDatacenterLatency)
    nwTopology.addLink(dc4, dc5, interDatacenterBw, interDatacenterLatency)
    nwTopology.addLink(dc5, dc1, interDatacenterBw, interDatacenterLatency)
    ()

  /**
   * Creates allocation policy of vm on host machines
   * @param allocationPolicy - policy to execute
   * @return - allocation policy
   */
  def getAllocationPolicy(allocationPolicy: String): VmAllocationPolicy =
    allocationPolicy match
      case "SIMPLE" => VmAllocationPolicySimple()
      case "RANDOM" =>
        val random = UniformDistr(0, 1, randomAllocationPolicySeed)
        VmAllocationPolicyRandom(random)
      case "BESTFIT" => VmAllocationPolicyBestFit()
      case "ROUNDROBIN" => VmAllocationPolicyRoundRobin()
      case _ => VmAllocationPolicySimple()

  /**
   * Create a scheduling policy for VM execution
   * @param schedulingPolicy - policy to be executed
   * @return scheduling policy
   */
  def getVmSchedulingPolicy(schedulingPolicy: String): VmScheduler =
    schedulingPolicy match
      case "TIMESHARED" => VmSchedulerTimeShared()
      case "SPACESHARED" => VmSchedulerSpaceShared()
      case "RANDOM" => VmSchedulerRandom(vmMigrationOverhead)
      case "PSEUDORANDOM" => VmSchedulerPseudoRandom(vmChance, vmMigrationOverhead)
      case _ => VmSchedulerTimeShared()

  /**
   * Create a scheduling policy for cloudlet execution
   * @param schedulingPolicy - policy to be executed
   * @return scheduling policy
   */
  def getCloudletSchedulingPolicy(schedulingPolicy: String): CloudletScheduler =
    schedulingPolicy match
      case "TIMESHARED" => CloudletSchedulerTimeShared()
      case "SPACESHARED" => CloudletSchedulerSpaceShared()
      case "FAIR" => CloudletSchedulerCompletelyFair()
      case _ => CloudletSchedulerSpaceShared()

  /**
   * Prints result
   * @param broker - datacenter broker
   * @param vmList - list of vms
   * @param hostList - list of hosts
   */
  def buildTableAndPrintResults(broker: DatacenterBroker, vmList: util.List[? <: Vm], hostList: util.List[? <: Host]): Unit =
    logger.info("<---------- CLOUDLET PERFORMANCE TABLE ------->")
    CloudletsTableBuilder(broker.getCloudletFinishedList).build()
    logger.info("<---------- HOSTS POWER AND CPU CONSUMPTION ------->")
    hostList.asScala.foreach(printHostPowerConsumptionAndCpuUtilization)
    logger.info("<---------- VMS POWER AND CPU CONSUMPTION ------->")
    vmList.asScala.foreach(printVmPowerConsumptionAndCpuUtilization)
    logger.info("<-------- RESOURCE BILLING INFORMATION ------------------>")
    printTotalCostForVms(broker)
}
