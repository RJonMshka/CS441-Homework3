package CloudOrg.Schedulers.Vm

import CloudOrg.HelperUtils.ObtainConfigReference
import org.cloudbus.cloudsim.distributions.UniformDistr
import org.cloudbus.cloudsim.resources.Pe
import org.cloudbus.cloudsim.schedulers.MipsShare
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerAbstract
import org.cloudbus.cloudsim.vms.{Vm, VmSimple}

import java.util

/**
 * Vm scheduler which is completely random
 * @param migrationOverhead - migration overhead b/w 0 and 1
 */
class VmSchedulerRandom(migrationOverhead: Double) extends VmSchedulerAbstract(migrationOverhead):
  private val config = ObtainConfigReference("cloudOrganizationSimulations").get
  private val vmSchedulerConfig = config.getConfig("cloudOrganizationSimulations.vmscheduler")
  private val randomSeed = vmSchedulerConfig.getInt("randomPolicySeed")
  private val random = UniformDistr(0, 1, randomSeed)

  /**
   * Checks if vm is suitable for requested execution time
   * @param vm - vm
   * @param requestedMips - requested million instructions per sec
   * @return true if vm is suitable for requested execution time
   */
  override def isSuitableForVmInternal(vm: Vm, requestedMips: MipsShare): Boolean =
    if(random.sample() <= 0.5) then true else false

  /**
   * Whether to allocate the requested MIPS to vm or not
   * @param vm - vm
   * @param mipsShareRequested - requested million instructions per sec
   * @return boolean - Whether to allocate the requested MIPS to vm or not
   */
  override def allocatePesForVmInternal(vm: Vm, mipsShareRequested: MipsShare): Boolean =
    if(random.sample() <= 0.5) then true else false

  /**
   * Deallocate MIPS from vm
   * @param vm - vm
   * @param pesToRemove - PES that needs to be deallocated
   * @return - how many PEs are deallocated
   */
  override def deallocatePesFromVmInternal(vm: Vm, pesToRemove: Int): Long =
    removePesFromVm(vm, vm.asInstanceOf[VmSimple].getAllocatedMips, pesToRemove)
