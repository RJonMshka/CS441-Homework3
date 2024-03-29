package CloudOrg.Network

import CloudOrg.HelperUtils.ObtainConfigReference
import com.typesafe.config.Config
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.network.NetworkDatacenter
import org.cloudbus.cloudsim.hosts.network.NetworkHost
import org.cloudbus.cloudsim.network.switches.{AggregateSwitch, EdgeSwitch, RootSwitch}

import java.util
import scala.jdk.CollectionConverters.*

object Topologies {
  // config
  val config: Config = ObtainConfigReference("cloudOrganizationSimulations").get
  val networkConfig: Config = config.getConfig("cloudOrganizationSimulations.network")

  // switch specific variables
  private val edge_switch_bw_down = networkConfig.getDouble("edge_switch_bw_down")
  private val edge_switch_bw_up = networkConfig.getDouble("edge_switch_bw_up")
  private val edge_switch_switching_delay = networkConfig.getDouble("edge_switch_switching_delay")

  private val aggregate_switch_bw_down = networkConfig.getDouble("aggregate_switch_bw_down")
  private val aggregate_switch_bw_up = networkConfig.getDouble("aggregate_switch_bw_up")
  private val aggregate_switch_switching_delay = networkConfig.getDouble("aggregate_switch_switching_delay")

  private val root_switch_bw_down = networkConfig.getDouble("root_switch_bw_down")
  private val root_switch_bw_up = networkConfig.getDouble("root_switch_bw_up")
  private val root_switch_switching_delay = networkConfig.getDouble("root_switch_switching_delay")

  // This method is used to create edge switch
  private def setEdgeSwitch(simulation: CloudSim, datacenter: NetworkDatacenter): EdgeSwitch =
    val edgeSwitch = EdgeSwitch(simulation, datacenter)
    edgeSwitch.setDownlinkBandwidth(edge_switch_bw_down)
    edgeSwitch.setUplinkBandwidth(edge_switch_bw_up)
    edgeSwitch.setSwitchingDelay(edge_switch_switching_delay)
    datacenter.addSwitch(edgeSwitch)
    edgeSwitch

  // This method is used to create aggregate switch
  private def setAggregateSwitch(simulation: CloudSim, datacenter: NetworkDatacenter): AggregateSwitch =
    val aggregateSwitch = AggregateSwitch(simulation, datacenter)
    aggregateSwitch.setDownlinkBandwidth(aggregate_switch_bw_down)
    aggregateSwitch.setUplinkBandwidth(aggregate_switch_bw_up)
    aggregateSwitch.setSwitchingDelay(aggregate_switch_switching_delay)
    datacenter.addSwitch(aggregateSwitch)
    aggregateSwitch

  // This method is used to create root switch
  private def setRootSwitch(simulation: CloudSim, datacenter: NetworkDatacenter): RootSwitch =
    val rootSwitch = RootSwitch(simulation, datacenter)
    rootSwitch.setDownlinkBandwidth(root_switch_bw_down)
    rootSwitch.setUplinkBandwidth(root_switch_bw_up)
    rootSwitch.setSwitchingDelay(root_switch_switching_delay)
    datacenter.addSwitch(rootSwitch)
    rootSwitch

  // This method creates star topology for a datacenter
  def createStarNetworkTopologyInDatacenter(simulation: CloudSim, datacenter: NetworkDatacenter, hostList: util.List[? <: NetworkHost]): Unit =
    // creating one root switch and one aggregate switch
    val rootSwitch = setRootSwitch(simulation, datacenter)
    val aggregateSwitch = setAggregateSwitch(simulation, datacenter)

    aggregateSwitch.getUplinkSwitches.add(rootSwitch)
    rootSwitch.getDownlinkSwitches.add(aggregateSwitch)
    // only one edge switch
    val edgeSwitch = setEdgeSwitch(simulation, datacenter)
    edgeSwitch.getUplinkSwitches.add(aggregateSwitch)
    aggregateSwitch.getDownlinkSwitches.add(edgeSwitch)
    // set port as number of hosts for edge switch
    edgeSwitch.setPorts(hostList.size)
    // connect edge switch with every host
    hostList.asScala.foreach(host => {
      edgeSwitch.connectHost(host)
    })
    ()

  // This method creates ring topology for a datacenter
  def createRingNetworkTopologyInDatacenter(simulation: CloudSim, datacenter: NetworkDatacenter, hostList: util.List[? <: NetworkHost]): Unit =
    val rootSwitch = setRootSwitch(simulation, datacenter)
    val edgeSwitchList = hostList.asScala.map(host => {
      val edgeSwitch = setEdgeSwitch(simulation, datacenter)
      edgeSwitch.connectHost(host)
      edgeSwitch
    }).toList
    // connect in ring fashion
    if(edgeSwitchList.length > 1) {
      // connect all inline
      Range(0, edgeSwitchList.length - 1).foreach(i => {
        // connect each agg switch with root switch
        val aggregateSwitch = setAggregateSwitch(simulation, datacenter)
        aggregateSwitch.setPorts(2)
        rootSwitch.getDownlinkSwitches.add(aggregateSwitch)
        aggregateSwitch.getUplinkSwitches.add(rootSwitch)
        // connect two consecutive edge switches with an agg switch
        edgeSwitchList(i).getUplinkSwitches.add(aggregateSwitch)
        edgeSwitchList(i + 1).getUplinkSwitches.add(aggregateSwitch)
        aggregateSwitch.getDownlinkSwitches.add(edgeSwitchList(i))
        aggregateSwitch.getDownlinkSwitches.add(edgeSwitchList(i + 1))
      })
      // connect first and last
      val aggregateSwitch = setAggregateSwitch(simulation, datacenter)
      aggregateSwitch.setPorts(2)
      rootSwitch.getDownlinkSwitches.add(aggregateSwitch)
      aggregateSwitch.getUplinkSwitches.add(rootSwitch)
      edgeSwitchList.head.getUplinkSwitches.add(aggregateSwitch)
      edgeSwitchList.last.getUplinkSwitches.add(aggregateSwitch)
      aggregateSwitch.getDownlinkSwitches.add(edgeSwitchList.head)
      aggregateSwitch.getDownlinkSwitches.add(edgeSwitchList.last)
    } else if(edgeSwitchList.length == 1) {
      // edge case
      val aggregateSwitch = setAggregateSwitch(simulation, datacenter)
      rootSwitch.getDownlinkSwitches.add(aggregateSwitch)
      aggregateSwitch.getUplinkSwitches.add(rootSwitch)
      edgeSwitchList.head.getUplinkSwitches.add(aggregateSwitch)
      aggregateSwitch.getDownlinkSwitches.add(edgeSwitchList.head)
    }
    ()

  // This method creates bus topology for a datacenter
  def createBusNetworkTopologyInDatacenter(simulation: CloudSim, datacenter: NetworkDatacenter, hostList: util.List[? <: NetworkHost]): Unit =
    // similar to ring just that last and first are not connected
    val rootSwitch = setRootSwitch(simulation, datacenter)
    val edgeSwitchList = hostList.asScala.map(host => {
      val edgeSwitch = setEdgeSwitch(simulation, datacenter)
      edgeSwitch.connectHost(host)
      edgeSwitch
    }).toList
    // total aggregate switches will be one less than edge switches
    if(edgeSwitchList.length > 1) {
      Range(0, edgeSwitchList.length - 1).foreach(i => {
        // connect each agg switch with root switch
        val aggregateSwitch = setAggregateSwitch(simulation, datacenter)
        aggregateSwitch.setPorts(2)
        rootSwitch.getDownlinkSwitches.add(aggregateSwitch)
        aggregateSwitch.getUplinkSwitches.add(rootSwitch)
        // connect two consecutive edge switches with an agg switch
        edgeSwitchList(i).getUplinkSwitches.add(aggregateSwitch)
        edgeSwitchList(i + 1).getUplinkSwitches.add(aggregateSwitch)
        aggregateSwitch.getDownlinkSwitches.add(edgeSwitchList(i))
        aggregateSwitch.getDownlinkSwitches.add(edgeSwitchList(i + 1))
      })
    } else if(edgeSwitchList.length == 1) {
      val aggregateSwitch = setAggregateSwitch(simulation, datacenter)
      rootSwitch.getDownlinkSwitches.add(aggregateSwitch)
      aggregateSwitch.getUplinkSwitches.add(rootSwitch)
      edgeSwitchList.head.getUplinkSwitches.add(aggregateSwitch)
      aggregateSwitch.getDownlinkSwitches.add(edgeSwitchList.head)
    }
    ()

  // This method creates tree topology for a datacenter
  def createTreeNetworkTopologyInDatacenter(simulation: CloudSim, datacenter: NetworkDatacenter, hostList: util.List[? <: NetworkHost], treeSize: Int): Unit =
  // fallback to star network
    if hostList.size % treeSize != 0 then
      createStarNetworkTopologyInDatacenter(simulation, datacenter, hostList)
    else
      val rootSwitch = setRootSwitch(simulation, datacenter)
      val treeLeafNodes = hostList.size / treeSize
      val hostNodesList = Range(0, treeLeafNodes).map(i => {
        hostList.subList(i * treeSize, (i + 1) * treeSize).asScala.toList
      }).toList
      val edgeSwitchList = hostNodesList.map(hList => {
        val edgeSwitch = setEdgeSwitch(simulation, datacenter)
        edgeSwitch.setPorts(treeSize)
        hList.foreach(host => {
          edgeSwitch.connectHost(host)
        })
        edgeSwitch
      })
      // total aggregate switches will be one less than edge switches
      if(edgeSwitchList.length > 1) {
        Range(0, edgeSwitchList.length - 1).foreach(i => {
          // connect each agg switch with root switch
          val aggregateSwitch = setAggregateSwitch(simulation, datacenter)
          aggregateSwitch.setPorts(2)
          rootSwitch.getDownlinkSwitches.add(aggregateSwitch)
          aggregateSwitch.getUplinkSwitches.add(rootSwitch)
          // connect two consecutive edge switches with an agg switch
          edgeSwitchList(i).getUplinkSwitches.add(aggregateSwitch)
          edgeSwitchList(i + 1).getUplinkSwitches.add(aggregateSwitch)
          aggregateSwitch.getDownlinkSwitches.add(edgeSwitchList(i))
          aggregateSwitch.getDownlinkSwitches.add(edgeSwitchList(i + 1))
        })
      } else if(edgeSwitchList.length == 1) {
        val aggregateSwitch = setAggregateSwitch(simulation, datacenter)
        rootSwitch.getDownlinkSwitches.add(aggregateSwitch)
        aggregateSwitch.getUplinkSwitches.add(rootSwitch)
        edgeSwitchList.head.getUplinkSwitches.add(aggregateSwitch)
        aggregateSwitch.getDownlinkSwitches.add(edgeSwitchList.head)
      }
      ()

  // This method creates hybrid topology for a datacenter
  def createHybridNetworkTopologyInDatacenter(simulation: CloudSim, datacenter: NetworkDatacenter,hostList: util.List[? <: NetworkHost]): Unit =
    // in hybrid we will divide all hosts in 3 set and try different network for them and connect them together by having all of their aggregate switches talking to root switch
    val rootSwitch = setRootSwitch(simulation, datacenter)
    val numberOfHostsInEachSet = hostList.size / 3
    // if very less number of hosts are there then fallback to star topology (there should be atleast 2 hosts in each set to produce a good hybrid network)
    if(numberOfHostsInEachSet < 2) {
      createStarNetworkTopologyInDatacenter(simulation, datacenter, hostList)
    } else {
      val firstNwHostList = hostList.subList(0, numberOfHostsInEachSet)
      val secondNwHostList = hostList.subList(numberOfHostsInEachSet, 2 * numberOfHostsInEachSet)
      val thirdNwHostList = hostList.subList(2 * numberOfHostsInEachSet, hostList.size)

      // first network will mimic star network
      val firstNwAggrSwitch = setAggregateSwitch(simulation, datacenter)
      firstNwAggrSwitch.getUplinkSwitches.add(rootSwitch)
      rootSwitch.getDownlinkSwitches.add(firstNwAggrSwitch)
      val firstNwEdgeSwitch = setEdgeSwitch(simulation, datacenter)
      firstNwEdgeSwitch.setPorts(firstNwHostList.size)
      firstNwEdgeSwitch.getUplinkSwitches.add(firstNwAggrSwitch)
      firstNwAggrSwitch.getDownlinkSwitches.add(firstNwEdgeSwitch)
      firstNwHostList.asScala.foreach(host => {
        firstNwEdgeSwitch.connectHost(host)
      })

      // second network will have a tree-bus mix like structure where each host will have a dedicated edge switch connected to it and all edge switches are connected to a single aggregate switch
      val secondAggrSwitch = setAggregateSwitch(simulation, datacenter)
      rootSwitch.getDownlinkSwitches.add(secondAggrSwitch)
      secondAggrSwitch.getUplinkSwitches.add(rootSwitch)

      secondNwHostList.asScala.foreach(host => {
        val edgeSwitch = setEdgeSwitch(simulation, datacenter)
        edgeSwitch.connectHost(host)
        edgeSwitch.getUplinkSwitches.add(secondAggrSwitch)
        secondAggrSwitch.getDownlinkSwitches.add(edgeSwitch)
        edgeSwitch
      })

      // third network will have hosts connected in ring, so there will be multiple aggregate switches
      val thirdNwEdgeSwitchList = thirdNwHostList.asScala.map(host => {
        val edgeSwitch = setEdgeSwitch(simulation, datacenter)
        edgeSwitch.connectHost(host)
        edgeSwitch
      }).toList
      if(thirdNwEdgeSwitchList.length > 1) {
        // connect all inline
        Range(0, thirdNwEdgeSwitchList.length - 1).foreach(i => {
          // connect each agg switch with root switch
          val aggregateSwitch = setAggregateSwitch(simulation, datacenter)
          aggregateSwitch.setPorts(2)
          rootSwitch.getDownlinkSwitches.add(aggregateSwitch)
          aggregateSwitch.getUplinkSwitches.add(rootSwitch)
          // connect two consecutive edge switches with an agg switch
          thirdNwEdgeSwitchList(i).getUplinkSwitches.add(aggregateSwitch)
          thirdNwEdgeSwitchList(i + 1).getUplinkSwitches.add(aggregateSwitch)
          aggregateSwitch.getDownlinkSwitches.add(thirdNwEdgeSwitchList(i))
          aggregateSwitch.getDownlinkSwitches.add(thirdNwEdgeSwitchList(i + 1))
        })
        // connect first and last
        val aggregateSwitch = setAggregateSwitch(simulation, datacenter)
        aggregateSwitch.setPorts(2)
        rootSwitch.getDownlinkSwitches.add(aggregateSwitch)
        aggregateSwitch.getUplinkSwitches.add(rootSwitch)
        thirdNwEdgeSwitchList.head.getUplinkSwitches.add(aggregateSwitch)
        thirdNwEdgeSwitchList.last.getUplinkSwitches.add(aggregateSwitch)
        aggregateSwitch.getDownlinkSwitches.add(thirdNwEdgeSwitchList.head)
        aggregateSwitch.getDownlinkSwitches.add(thirdNwEdgeSwitchList.last)
      } else if(thirdNwEdgeSwitchList.length == 1) {
        // edge case
        val aggregateSwitch = setAggregateSwitch(simulation, datacenter)
        rootSwitch.getDownlinkSwitches.add(aggregateSwitch)
        aggregateSwitch.getUplinkSwitches.add(rootSwitch)
        thirdNwEdgeSwitchList.head.getUplinkSwitches.add(aggregateSwitch)
        aggregateSwitch.getDownlinkSwitches.add(thirdNwEdgeSwitchList.head)
      }
    }
    ()

}
