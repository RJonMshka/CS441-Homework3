package CloudOrg.Schedulers.Vm

import CloudOrg.HelperUtils.ObtainConfigReference
import org.cloudbus.cloudsim.distributions.UniformDistr
import org.cloudbus.cloudsim.resources.Pe
import org.cloudbus.cloudsim.schedulers.MipsShare
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerAbstract
import org.cloudbus.cloudsim.vms.{Vm, VmSimple}

import java.util

class VmSchedulerRandom(migrationOverhead: Double) extends VmSchedulerAbstract(migrationOverhead):
  private val config = ObtainConfigReference("cloudOrganizationSimulations").get
  private val vmSchedulerConfig = config.getConfig("cloudOrganizationSimulations.vmscheduler")
  private val randomSeed = vmSchedulerConfig.getInt("randomPolicySeed")
  private val random = UniformDistr(0, 1, randomSeed)

  override def isSuitableForVmInternal(vm: Vm, requestedMips: MipsShare): Boolean =
    if(random.sample() <= 0.5) then true else false

  override def allocatePesForVmInternal(vm: Vm, mipsShareRequested: MipsShare): Boolean =
    if(random.sample() <= 0.5) then true else false

  override def deallocatePesFromVmInternal(vm: Vm, pesToRemove: Int): Long =
    removePesFromVm(vm, vm.asInstanceOf[VmSimple].getAllocatedMips, pesToRemove)
