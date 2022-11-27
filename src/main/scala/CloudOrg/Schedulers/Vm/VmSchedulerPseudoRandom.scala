package CloudOrg.Schedulers.Vm

import org.cloudbus.cloudsim.distributions.UniformDistr
import org.cloudbus.cloudsim.schedulers.MipsShare
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerAbstract
import org.cloudbus.cloudsim.resources.Pe
import org.cloudbus.cloudsim.vms.{Vm, VmSimple}

import java.util
import scala.jdk.CollectionConverters.*

class VmSchedulerPseudoRandom(private val chance: Double, migrationOverhead: Double) extends VmSchedulerAbstract(migrationOverhead):
  private val randomSeed = 50
  private val random = UniformDistr(0, 1, randomSeed)

  override def isSuitableForVmInternal(vm: Vm, requestedMips: MipsShare): Boolean =
    if(random.sample() < chance) then
      getTotalCapacityToBeAllocatedToVm(requestedMips).size >= requestedMips.pes
    else if(random.sample() < 0.5) then true else false

  override def allocatePesForVmInternal(vm: Vm, mipsShareRequested: MipsShare): Boolean =
    if(random.sample() < chance) then
      val selectedPes = getTotalCapacityToBeAllocatedToVm(mipsShareRequested)
      if selectedPes.size < mipsShareRequested.pes then false
      vm.asInstanceOf[VmSimple].setAllocatedMips(mipsShareRequested)
      true
    else if(random.sample() < 0.5) then true else false

  override def deallocatePesFromVmInternal(vm: Vm, pesToRemove: Int): Long =
    removePesFromVm(vm, vm.asInstanceOf[VmSimple].getAllocatedMips, pesToRemove)

  private def getTotalCapacityToBeAllocatedToVm(requestedMips: MipsShare): util.List[Pe] =
    if getHost.getWorkingPesNumber < requestedMips.pes then getHost.getWorkingPesNumber
    if getHost.getFreePeList.isEmpty then util.ArrayList[Pe]()

    getHost.getFreePeList.asScala.filter(pe => requestedMips.mips <= pe.getCapacity).asJava

