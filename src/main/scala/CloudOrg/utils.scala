package CloudOrg

import CloudOrg.HelperUtils.CreateLogger
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicy
import org.cloudbus.cloudsim.brokers.{DatacenterBroker, DatacenterBrokerHeuristic, DatacenterBrokerSimple}
import org.cloudbus.cloudsim.cloudlets.network.{CloudletExecutionTask, CloudletReceiveTask, CloudletSendTask, NetworkCloudlet}
import org.cloudbus.cloudsim.cloudlets.{Cloudlet, CloudletSimple}
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.network.NetworkDatacenter
import org.cloudbus.cloudsim.datacenters.{Datacenter, DatacenterSimple}
import org.cloudbus.cloudsim.distributions.UniformDistr
import org.cloudbus.cloudsim.hosts.network.NetworkHost
import org.cloudbus.cloudsim.hosts.{Host, HostSimple}
import org.cloudbus.cloudsim.network.switches.{AggregateSwitch, EdgeSwitch, RootSwitch}
import org.cloudbus.cloudsim.network.topologies.{BriteNetworkTopology, NetworkTopology}
import org.cloudbus.cloudsim.power.models.PowerModelHostSimple
import org.cloudbus.cloudsim.resources.{Pe, PeSimple, Ram}
import org.cloudbus.cloudsim.schedulers.cloudlet.{CloudletSchedulerSpaceShared, CloudletSchedulerTimeShared}
import org.cloudbus.cloudsim.utilizationmodels.{UtilizationModel, UtilizationModelDynamic, UtilizationModelFull}
import org.cloudbus.cloudsim.vms.network.NetworkVm
import org.cloudbus.cloudsim.vms.{Vm, VmCost, VmSimple}
import org.cloudbus.cloudsim.schedulers.vm.{VmScheduler, VmSchedulerSpaceShared, VmSchedulerTimeShared}
import org.cloudsimplus.autoscaling.{HorizontalVmScalingSimple, VerticalVmScalingSimple}
import org.cloudsimplus.heuristics.CloudletToVmMappingSimulatedAnnealing
import org.slf4j.Logger

import java.util
import java.util.*
import java.util.function.{Predicate, Supplier}
import scala.collection.*
import scala.jdk.CollectionConverters.*

object utils {

  val logger: Logger = CreateLogger(classOf[utils.type])

  val hybrid_toplogies_count = 3

  val STATIC_POWER = 50
  val MAX_POWER = 200

  val HOST_STARTUP_DELAY = 0
  val HOST_SHUTDOWN_DELAY = 0
  val HOST_STARTUP_POWER = 10
  val HOST_SHUTDOWN_POWER = 5

  enum SchedulerType:
    case TIMESHARED, SPACESHARED

  class RandomIntGenerator(minValue: Int, maxValue: Int, seed: Int):
    val random = UniformDistr(minValue, maxValue, seed)

    def getNextRandomValue(): Double =
      random.sample()

  def createDataCenter(simlulation: CloudSim, hostList: util.List[Host], vmAllocationPolicy: VmAllocationPolicy, schedulingInterval: Double): Datacenter =
    val datacenter = DatacenterSimple(simlulation, hostList, vmAllocationPolicy)
    datacenter.setSchedulingInterval(schedulingInterval)
    datacenter

  def createDataCenter(simlulation: CloudSim, hostList: util.ArrayList[Host], schedulingInterval: Double): Datacenter =
    val datacenter = DatacenterSimple(simlulation, hostList)
    datacenter.setSchedulingInterval(schedulingInterval)
    datacenter

  def createDataCenter(simlulation: CloudSim, hostList: util.List[Host], vmAllocationPolicy: VmAllocationPolicy): Datacenter = DatacenterSimple(simlulation, hostList, vmAllocationPolicy)

  def createDataCenter(simlulation: CloudSim, hostList: util.List[Host]): Datacenter = DatacenterSimple(simlulation, hostList)

  def createNwDataCenter(simlulation: CloudSim, hostList: util.List[NetworkHost]): NetworkDatacenter = NetworkDatacenter(simlulation, hostList)

  def createNwDataCenter(simlulation: CloudSim, hostList: util.List[NetworkHost], schedulingInterval: Double): NetworkDatacenter =
    val dc = NetworkDatacenter(simlulation, hostList)
    dc.setSchedulingInterval(schedulingInterval)
    dc

  def createNwDataCenter(simlulation: CloudSim, hostList: util.List[NetworkHost], vmAllocationPolicy: VmAllocationPolicy, schedulingInterval: Double): NetworkDatacenter =
    val dc = NetworkDatacenter(simlulation, hostList, vmAllocationPolicy)
    dc.setSchedulingInterval(schedulingInterval)
    dc

  def createBroker(simulation: CloudSim): DatacenterBroker =
    DatacenterBrokerSimple(simulation)

  def createBrokerHeuristic(simulation: CloudSim): DatacenterBroker =
    val broker = DatacenterBrokerHeuristic(simulation)
    val hr = CloudletToVmMappingSimulatedAnnealing(0.1, UniformDistr(0, 1))
    hr.setColdTemperature(0.0001)
    hr.setCoolingRate(0.003)
    hr.setSearchesByIteration(50)
    broker.setHeuristic(hr)
    broker

  def createPeList(pes: Int, mips: Long): util.List[Pe] =
    Range(0, pes).map(_ => PeSimple(mips)).toList.asJava


  def createHostList(hosts: Int, hostPes: Int, hostMips: Long, hostRam: Long, hostBw: Long, hostStorage: Long, vmScheduler: SchedulerType): util.List[Host] =
    Range(0, hosts).map(i => {
      val host = createHost(hostPes, hostMips, hostRam, hostBw, hostStorage)
      setPowerModelForHost(host)
      setVmSchedulerForHost(host, vmScheduler)
      host.enableUtilizationStats
      host.setId(i)
      host
    }).toList.asJava

  def createNwHostList(hosts: Int, hostPes: Int, hostMips: Long, hostRam: Long, hostBw: Long, hostStorage: Long, vmScheduler: SchedulerType): util.List[NetworkHost] =
    Range(0, hosts).map(i => {
      val host = createNwHost(hostPes, hostMips, hostRam, hostBw, hostStorage)
      setPowerModelForHost(host)
      setVmSchedulerForHost(host, vmScheduler)
      host.enableUtilizationStats
      host.setId(i)
      host
    }).toList.asJava

  def createHost(hostPes: Int, hostMips: Long, hostRam: Long, hostBw: Long, hostStorage: Long): Host =
    val peList = createPeList(hostPes, hostMips)
    HostSimple(hostRam, hostBw, hostStorage, peList)

  def createNwHost(hostPes: Int, hostMips: Long, hostRam: Long, hostBw: Long, hostStorage: Long): NetworkHost =
    val peList = createPeList(hostPes, hostMips)
    NetworkHost(hostRam, hostBw, hostStorage, peList)

  def createVm(mipsCapacity: Long, peCount: Int): Vm =
    VmSimple(mipsCapacity, peCount)

  def createNwVm(mipsCapacity: Long, peCount: Int): NetworkVm =
    NetworkVm(mipsCapacity, peCount)

  def createVm(mipsCapacity: Long, peCount: Int, vmRam: Long, vmBw: Long, vmSize: Long, cloudletScheduler: SchedulerType): Vm =
    val vm = VmSimple(mipsCapacity, peCount)
    setVmCharacteristics(vm, vmRam, vmBw, vmSize, cloudletScheduler)
    vm

  def createNwVm( mipsCapacity: Long, peCount: Int, vmRam: Long, vmBw: Long, vmSize: Long, cloudletScheduler: SchedulerType): NetworkVm =
    val vm = NetworkVm(mipsCapacity, peCount)
    setVmCharacteristics(vm, vmRam, vmBw, vmSize, cloudletScheduler)
    vm

  def createVmList(vmCount: Int, mipsCapacity: Long, peCount: Int): util.List[Vm] =
    Range(0, vmCount).map(_ => createVm(mipsCapacity, peCount)).toList.asJava

  def createNwVmList(vmCount: Int, mipsCapacity: Long, peCount: Int): util.List[NetworkVm] =
    Range(0, vmCount).map(_ => createNwVm(mipsCapacity, peCount)).toList.asJava

  def createNwVmList(vmCount: Int, mipsCapacity: Long, peCount: Int, vmRam: Long, vmBw: Long, vmSize: Long, cloudletScheduler: SchedulerType): util.List[NetworkVm] =
    Range(0, vmCount).map(_ => createNwVm(mipsCapacity, peCount, vmRam, vmBw, vmSize, cloudletScheduler)).toList.asJava

  def createVmList(vmCount: Int, mipsCapacity: Long, peCount: Int, vmRam: Long, vmBw: Long, vmSize: Long, cloudletScheduler: SchedulerType): util.List[Vm] =
    Range(0, vmCount).map(_ => createVm(mipsCapacity, peCount, vmRam, vmBw, vmSize, cloudletScheduler)).toList.asJava

  def setVmCharacteristics(vm: Vm, vmRam: Long, vmBw: Long, vmSize: Long, cloudletScheduler: SchedulerType): Vm =
    cloudletScheduler match
      case SchedulerType.TIMESHARED => vm.setCloudletScheduler(CloudletSchedulerTimeShared())
      case SchedulerType.SPACESHARED => vm.setCloudletScheduler(CloudletSchedulerSpaceShared())
    vm.enableUtilizationStats
    vm.setRam(vmRam).setBw(vmBw).setSize(vmSize)


  def createCloudlet(cloudletLength: Long, cloudletPes: Int): Cloudlet = CloudletSimple(cloudletLength, cloudletPes)

  def createNwCloudlet(cloudletLength: Long, cloudletPes: Int): NetworkCloudlet = NetworkCloudlet(cloudletLength, cloudletPes)

  def createCloudletList(cloudletCount: Int, cloudletLength: Long, cloudletPes: Int): util.List[Cloudlet] =
    Range(0, cloudletCount).map(_ => createCloudlet(cloudletLength, cloudletPes)).toList.asJava

  def createCloudletList(cloudletCount: Int, cloudletLength: Long, cloudletPes: Int, cpuUtilization: Double, ramUtilization: Double, bwUtilization: Double): util.List[Cloudlet] =
    Range(0, cloudletCount).map(_ => {
      val cloudlet = createCloudlet(cloudletLength, cloudletPes)
      cloudletUtilizationDynamic(cloudlet, cpuUtilization, ramUtilization, bwUtilization)
    }).toList.asJava

  def createCloudletList(cloudletCount: Int, cloudletLength: Long, cloudletPes: Int, cpuUtilization: Double, initialRamUtilization: Double, maxRamUtilization: Double, bwUtilization: Double): util.List[Cloudlet] =
    Range(0, cloudletCount).map(_ => {
      val cloudlet = createCloudlet(cloudletLength, cloudletPes)
      cloudletUtilizationDynamic(cloudlet, cpuUtilization, initialRamUtilization, maxRamUtilization, bwUtilization)
    }).toList.asJava

  def createNwCloudletList(cloudletCount: Int, cloudletLength: Long, cloudletPes: Int, vmList: util.List[NetworkVm]): util.List[NetworkCloudlet] =
    Range(0, cloudletCount).map(i => {
      val cloudlet = createNwCloudlet(cloudletLength, cloudletPes)
      cloudlet.setVm(vmList.get(i % vmList.size)).setId(i)
      cloudlet
    }).toList.asJava

  def createNwCloudletList(cloudletCount: Int, cloudletLength: Long, cloudletPes: Int, vmList: util.List[NetworkVm], cpuUtilization: Double, ramUtilization: Double, bwUtilization: Double): util.List[NetworkCloudlet] =
    Range(0, cloudletCount).map(i => {
      val cloudlet = createNwCloudlet(cloudletLength, cloudletPes)
      cloudlet.setVm(vmList.get(i % vmList.size)).setId(i)
      cloudletUtilizationDynamic(cloudlet, cpuUtilization, ramUtilization, bwUtilization)
      cloudlet
    }).toList.asJava

  def createNwCloudletList(cloudletCount: Int, cloudletLength: Long, cloudletPes: Int, vmList: util.List[NetworkVm], cpuUtilization: Double, initialRamUtilization: Double, maxRamUtilization: Double, bwUtilization: Double): util.List[NetworkCloudlet] =
    Range(0, cloudletCount).map(i => {
      val cloudlet = createNwCloudlet(cloudletLength, cloudletPes)
      cloudlet.setVm(vmList.get(i % vmList.size)).setId(i)
      cloudletUtilizationDynamic(cloudlet, cpuUtilization, initialRamUtilization, maxRamUtilization, bwUtilization)
      cloudlet
    }).toList.asJava


  def createExecTaskForNwCloudlet(cloudlet: NetworkCloudlet, taskLength: Long, taskMemory: Long): Unit =
    val task = CloudletExecutionTask(cloudlet.getTasks.size, taskLength)
    task.setMemory(taskMemory)
    cloudlet.addTask(task)
    ()

  def createSendTaskForNwCloudlet(sourceCloudlet: NetworkCloudlet, destCloudlet: NetworkCloudlet, taskMemory: Long, packetsToSend: Int, packetBytes: Long): Unit =
    val task = CloudletSendTask(sourceCloudlet.getTasks.size)
    task.setMemory(taskMemory)
    sourceCloudlet.addTask(task)
    Range(0, packetsToSend).map(_ -> task.addPacket(destCloudlet, packetBytes))
    ()

  def createReceiveTaskForNwCloudlet(destCloudlet: NetworkCloudlet, sourceCloudlet: NetworkCloudlet, taskMemory: Long, packetsToReceive: Long): Unit =
    val task = CloudletReceiveTask(destCloudlet.getTasks.size, sourceCloudlet.getVm)
    task.setMemory(taskMemory)
    task.setExpectedPacketsToReceive(packetsToReceive)
    destCloudlet.addTask(task)
    ()

  def cloudletUtilizationFullCommon(cloudlet: Cloudlet): Cloudlet =
    cloudlet.setUtilizationModel(UtilizationModelFull())

  def cloudletUtilizationDynamicCommon(cloudlet: Cloudlet, commonUtilization: Double): Cloudlet =
    cloudlet.setUtilizationModel(UtilizationModelDynamic(commonUtilization))

  def cloudletUtilizationDynamic(cloudlet: Cloudlet, cpuUtilization: Double, ramUtilization: Double, btwUtilization: Double): Cloudlet =
    cloudlet.setUtilizationModelRam(UtilizationModelDynamic(ramUtilization))
      .setUtilizationModelBw(UtilizationModelDynamic(btwUtilization))
      .setUtilizationModelCpu(UtilizationModelDynamic(cpuUtilization))

  def cloudletUtilizationDynamic(cloudlet: Cloudlet, cpuUtilization: Double, initialRamUtilization: Double, maxRamUtilization: Double, btwUtilization: Double): Cloudlet =
    cloudlet.setUtilizationModelRam(UtilizationModelDynamic(initialRamUtilization, maxRamUtilization))
      .setUtilizationModelBw(UtilizationModelDynamic(btwUtilization))
      .setUtilizationModelCpu(UtilizationModelDynamic(cpuUtilization))

  /**
   *
   * @param cloudlet
   * @param fileSize   - bytes
   * @param outputSize - bytes
   */
  def cloudletSetSize(cloudlet: Cloudlet, fileSize: Long, outputSize: Long): Cloudlet =
    cloudlet.setFileSize(fileSize)
      .setOutputSize(outputSize)

  def cloudletSetCommonSize(cloudlet: Cloudlet, commonSize: Long): Cloudlet =
    cloudlet.setSizes(commonSize)

  def setPowerModelForHost(host: Host): Unit =
    val powerModel = PowerModelHostSimple(MAX_POWER, STATIC_POWER)
    powerModel.setStartupDelay(HOST_STARTUP_DELAY)
      .setShutDownDelay(HOST_SHUTDOWN_DELAY)
      .setStartupPower(HOST_STARTUP_POWER)
      .setShutDownPower(HOST_SHUTDOWN_POWER)

    host.setPowerModel(powerModel)
    logger.info(s"Power model set for host ${host.getId}")

  def printVmPowerConsumptionAndCpuUtilization(vm: Vm): Unit =
    val powerModel = vm.getHost.getPowerModel.asInstanceOf[PowerModelHostSimple]
    val hostStaticPower = powerModel.getStaticPower
    val vmStaticPower = hostStaticPower / vm.getHost.getVmCreatedList.size

    val vmUtilPrecentMean = vm.getCpuUtilizationStats.getMean
    val vmRelCpuUtilization = vmUtilPrecentMean / vm.getHost.getVmCreatedList.size
    val vmPowerCons = powerModel.getPower(vmRelCpuUtilization) - hostStaticPower + vmStaticPower
    logger.info(
      f"VM ${vm.getId}%2d Mean CPU Usage is: ${vmUtilPrecentMean * 100.0}%4.1f%% and Power Consumption mean is: ${vmPowerCons}%4.0f Watt"
    )

  def printHostPowerConsumptionAndCpuUtilization(host: Host): Unit =
    val hostCpuStats = host.getCpuUtilizationStats
    val utilPercentMean = hostCpuStats.getMean
    val powerCons = host.getPowerModel.getPower(utilPercentMean)

    // cpu utilization by mips
    val mipsByPe = host.getTotalMipsCapacity / host.getNumberOfPes
    logger.info(
      f"Host ${host.getId}%2d | PE Amount: ${host.getNumberOfPes}%2d | MIPS by PE: ${mipsByPe}%1.0f | Mean CPU Usage is: ${utilPercentMean * 100.0}%4.1f%% | Power Consumption mean is: ${powerCons}%4.0f Watt"
    )

  def setVmSchedulerForHost(host: Host, schedulerType: SchedulerType): Host =
    schedulerType match
      case SchedulerType.TIMESHARED => host.setVmScheduler(VmSchedulerTimeShared())
      case SchedulerType.SPACESHARED => host.setVmScheduler(VmSchedulerSpaceShared())

  def setDatacenterCost(datacenter: Datacenter, costPerSec: Double, costPerMemAccess: Double, costPerStorage: Double, costPerBw: Double): Unit =
    datacenter.getCharacteristics
      .setCostPerSecond(costPerSec)
      .setCostPerMem(costPerMemAccess)
      .setCostPerStorage(costPerStorage)
      .setCostPerBw(costPerBw)
    ()

  def setSimulatedAnnealingHeuristicForBroker(broker: DatacenterBrokerHeuristic, initialTemperature: Double, coldTemperature: Double, coolingRate: Double, numberOfsearches: Int): Unit =
    val heuristic = CloudletToVmMappingSimulatedAnnealing(initialTemperature, UniformDistr(0, 1))
    heuristic.setColdTemperature(coldTemperature)
    heuristic.setCoolingRate(coolingRate)
    heuristic.setSearchesByIteration(numberOfsearches)
    broker.setHeuristic(heuristic)

  def horizontalVmScalingOutPredicate(vm: Vm, utilizationThreshold: Double): Boolean = vm.getCpuPercentUtilization > utilizationThreshold

  def createHorizontalVmScaling(vm: Vm, mipsCapacity: Long, peCount: Int, vmRam: Long, vmBw: Long, vmSize: Long, cloudletScheduler: SchedulerType, overloadingThreshold: Double): Unit =
    val horizontalVmScaling = HorizontalVmScalingSimple()
    val createVm = new Supplier[Vm]:
      override def get(): Vm = vm match
        case v: NetworkVm => utils.createNwVm(mipsCapacity, peCount, vmRam, vmBw, vmSize, cloudletScheduler)
        case _ => utils.createVm(mipsCapacity, peCount, vmRam, vmBw, vmSize, cloudletScheduler)
    val predicate = new Predicate[Vm]:
      override def test(t: Vm): Boolean = horizontalVmScalingOutPredicate(t, overloadingThreshold)
    horizontalVmScaling.setVmSupplier(createVm).setOverloadPredicate(predicate)
    vm.setHorizontalScaling(horizontalVmScaling)
    ()

  def createVerticalRamScalingForVm(vm: Vm, scalingFactor: Double, upperThreshold: Double, lowerThreshold: Double): Unit =
    val verticalRamScaling = VerticalVmScalingSimple(classOf[Ram], scalingFactor)
    verticalRamScaling.setLowerThresholdFunction((v: Vm) => lowerThreshold)
    verticalRamScaling.setUpperThresholdFunction((v: Vm) => upperThreshold)
    vm.setRamVerticalScaling(verticalRamScaling)
    ()

      
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
    logger.info(f"Non idle VM count is ${totalNonIdleVms}%2.0f out of ${broker.getVmsNumber}%2d created VMs")
    logger.info(f"Total processing cost is: ${processingCost}%2.2f units")
    logger.info(f"Total memory cost is: ${memCost}%2.2f units")
    logger.info(f"Total storage cost is: ${storageCost}%2.2f units")
    logger.info(f"Total bandwidth cost is: ${bwCost}%2.2f units")
    logger.info(f"Total cost is: ${totalCost}%2.2f units")

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
}
