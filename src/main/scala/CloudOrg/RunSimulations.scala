package CloudOrg

import CloudOrg.HelperUtils.CreateLogger
import CloudOrg.Simulations.{BasicSimulation, FaaSSimulation, IaaSSimulation, MultiDatacenterNetworkTopologySimulation, PaaSSimulation, SaaSSimulation}
import CloudOrg.Simulations.HybridTopologySimulations.HybridTopologySimpleCloudletSimulation
import CloudOrg.Simulations.RingTopologySimulations.{RingTopologyMapReduceAppSimulation, RingTopologyThreeTierAppSimulation}
import CloudOrg.Simulations.TreeTopologySimulations.TreeTopologyMapReduceSimulation

object RunSimulations {
  val logger = CreateLogger(classOf[RunSimulations.type])

  def main(args: Array[String]): Unit = {
    logger.info("starting basic simulation")
    BasicSimulation.startSimulation()
    logger.info("basic simulation ended")

    logger.info("starting hybrid simple cloudlet simulation")
    HybridTopologySimpleCloudletSimulation.startSimulation()
    logger.info("hybrid simple cloudlet ended")

    logger.info("starting ring three tier simulation")
    RingTopologyThreeTierAppSimulation.startSimulation()
    logger.info("basic ring three tier ended")

    logger.info("starting multi-datacenter with multiple network topologies simulation")
    MultiDatacenterNetworkTopologySimulation.startSimulation()
    logger.info("multi-datacenter simulation ended")

    logger.info("starting saas simulation")
    SaaSSimulation.startSaaSSimulation()
    logger.info("saas simulation ended")

    logger.info("starting paas map reduce simulation")
    PaaSSimulation.mapReducePaaSSimulation()
    logger.info("paas map reduce simulation ended")

    logger.info("starting paas three tier simulation")
    PaaSSimulation.threeTierPaaSSimulation()
    logger.info("paas three tier simulation ended")

    logger.info("starting iaas simple app simulation")
    IaaSSimulation.iaaSSimpleAppSimulation()
    logger.info("iaas simple app simulation ended")

    logger.info("starting iaas map reduce app simulation")
    IaaSSimulation.iaaSMapReduceAppSimulation()
    logger.info("iaas map reduce app simulation ended")

    logger.info("starting iaas three tier app simulation")
    IaaSSimulation.iaaSThreeTierAppSimulation()
    logger.info("iaas three tier app simulation ended")

    logger.info("starting faas simulation")
    FaaSSimulation.faaSSimulation()
    logger.info("faas simulation ended")
  }

}
