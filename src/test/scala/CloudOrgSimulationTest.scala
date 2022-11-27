import org.scalatest.*
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers.*
import com.typesafe.config.{Config, ConfigFactory}
import CloudOrg.HelperUtils.utils
import CloudOrg.Schedulers.Vm.{VmSchedulerPseudoRandom, VmSchedulerRandom}
import org.cloudbus.cloudsim.allocationpolicies.{VmAllocationPolicyBestFit, VmAllocationPolicyRandom, VmAllocationPolicyRoundRobin, VmAllocationPolicySimple}
import org.cloudbus.cloudsim.schedulers.cloudlet.{CloudletSchedulerCompletelyFair, CloudletSchedulerSpaceShared, CloudletSchedulerTimeShared}
import org.cloudbus.cloudsim.schedulers.vm.{VmSchedulerSpaceShared, VmSchedulerTimeShared}

class CloudOrgSimulationTest extends AnyFunSpec:

  describe("testing VM creation") {
    it("should create VM with specific parameters") {
      val mipsCapacity = 19
      val peCount = 10
      val vm = utils.createVm(mipsCapacity, peCount)
      vm.getMips shouldBe mipsCapacity.asInstanceOf[Double]
      vm.getNumberOfPes shouldBe peCount
    }

    it("should create VM with specific total mips") {
      val mipsCapacity = 19
      val peCount = 10
      val vm = utils.createVm(mipsCapacity, peCount)
      vm.getTotalMipsCapacity shouldBe (mipsCapacity * peCount).asInstanceOf[Double]
    }

    it("should create network VM with specific parameters") {
      val mipsCapacity = 19
      val peCount = 10
      val vm = utils.createNwVm(mipsCapacity, peCount)
      vm.getMips shouldBe mipsCapacity.asInstanceOf[Double]
      vm.getNumberOfPes shouldBe peCount
    }

    it("should create network VM with specific total mips") {
      val mipsCapacity = 19
      val peCount = 10
      val vm = utils.createNwVm(mipsCapacity, peCount)
      vm.getTotalMipsCapacity shouldBe (mipsCapacity * peCount).asInstanceOf[Double]
    }

    it("should set vm characteristics") {
      val mipsCapacity = 19
      val peCount = 10
      val vmRam = 512
      val vmSize = 50000
      val vmBw = 10
      val cloudletScheduler = "TIMESHARED"
      val vm = utils.createNwVm(mipsCapacity, peCount)
      utils.setVmCharacteristics(vm, vmRam, vmBw, vmSize, cloudletScheduler)

      vm.getRam.getCapacity shouldBe vmRam
      vm.getStorage.getCapacity shouldBe vmSize
      vm.getBw.getCapacity shouldBe vmBw
    }
  }

  describe("test cloudlet creation and characteristics") {
    it("should create a simple cloudlet with certain attributes") {
      val cloudletLength = 1000
      val cloudletPes = 2
      val cloudlet = utils.createCloudlet(cloudletLength, cloudletPes)
      cloudlet.getNumberOfPes shouldBe cloudletPes
      cloudlet.getLength shouldBe cloudletLength
    }

    it("should create a network cloudlet with certain attributes") {
      val cloudletLength = 1000
      val cloudletPes = 2
      val cloudlet = utils.createNwCloudlet(cloudletLength, cloudletPes)
      cloudlet.getNumberOfPes shouldBe cloudletPes
    }

    it("should set cloudlet size") {
      val cloudletLength = 1000
      val cloudletPes = 2
      val cloudletSize = 500
      val cloudletOutputSize = 500
      val cloudlet = utils.createNwCloudlet(cloudletLength, cloudletPes)
      utils.cloudletSetSize(cloudlet, cloudletSize, cloudletOutputSize)
      cloudlet.getFileSize shouldBe cloudletSize
      cloudlet.getOutputSize shouldBe cloudletOutputSize
    }

  }

  describe("test Host creation and its properties") {
    it("should create a host with certain attributes") {
      val host_mips = 1000
      val host_pe_count = 8
      val host_ram = 16384
      val host_bw = 1000
      val host_storage = 1000000
      val host = utils.createHost(host_pe_count, host_mips, host_ram, host_bw, host_storage)
      host.getBw.getCapacity shouldBe host_bw
      host.getRam.getCapacity shouldBe host_ram
      host.getMips shouldBe host_mips
      host.getStorage.getCapacity shouldBe host_storage
      host.getNumberOfPes shouldBe host_pe_count
    }

    it("should create a network host with certain attributes") {
      val host_mips = 1000
      val host_pe_count = 8
      val host_ram = 16384
      val host_bw = 1000
      val host_storage = 1000000
      val host = utils.createNwHost(host_pe_count, host_mips, host_ram, host_bw, host_storage)
      host.getBw.getCapacity shouldBe host_bw
      host.getRam.getCapacity shouldBe host_ram
      host.getMips shouldBe host_mips
      host.getStorage.getCapacity shouldBe host_storage
      host.getNumberOfPes shouldBe host_pe_count
    }
  }

  describe("test allocation and scheduling policy creations") {
    it("should test vm allocation policy util - simple") {
      val allocationPolicy = "SIMPLE"
      val policy = utils.getAllocationPolicy(allocationPolicy)
      policy.getClass shouldBe classOf[VmAllocationPolicySimple]
    }

    it("should test vm allocation policy util - random") {
      val allocationPolicy = "RANDOM"
      val policy = utils.getAllocationPolicy(allocationPolicy)
      policy.getClass shouldBe classOf[VmAllocationPolicyRandom]
    }

    it("should test vm allocation policy util - round robin") {
      val allocationPolicy = "ROUNDROBIN"
      val policy = utils.getAllocationPolicy(allocationPolicy)
      policy.getClass shouldBe classOf[VmAllocationPolicyRoundRobin]
    }

    it("should test vm allocation policy util - best fit") {
      val allocationPolicy = "BESTFIT"
      val policy = utils.getAllocationPolicy(allocationPolicy)
      policy.getClass shouldBe classOf[VmAllocationPolicyBestFit]
    }

    it("should test vm allocation policy util - other") {
      val allocationPolicy = "xyz"
      val policy = utils.getAllocationPolicy(allocationPolicy)
      policy.getClass shouldBe classOf[VmAllocationPolicySimple]
    }

    it("should test vm scheduler creation policy - timeshared") {
      val policy = "TIMESHARED"
      val vmScheduler = utils.getVmSchedulingPolicy(policy)
      vmScheduler.getClass shouldBe classOf[VmSchedulerTimeShared]
    }

    it("should test vm scheduler creation policy - spaceshared") {
      val policy = "SPACESHARED"
      val vmScheduler = utils.getVmSchedulingPolicy(policy)
      vmScheduler.getClass shouldBe classOf[VmSchedulerSpaceShared]
    }

    it("should test vm scheduler creation policy - random") {
      val policy = "RANDOM"
      val vmScheduler = utils.getVmSchedulingPolicy(policy)
      vmScheduler.getClass shouldBe classOf[VmSchedulerRandom]
    }

    it("should test vm scheduler creation policy - pseudo random") {
      val policy = "PSEUDORANDOM"
      val vmScheduler = utils.getVmSchedulingPolicy(policy)
      vmScheduler.getClass shouldBe classOf[VmSchedulerPseudoRandom]
    }

    it("should test vm scheduler creation policy - other") {
      val policy = "xyz"
      val vmScheduler = utils.getVmSchedulingPolicy(policy)
      vmScheduler.getClass shouldBe classOf[VmSchedulerTimeShared]
    }

    it("should test cloudlet scheduler creation policy - timeshared") {
      val policy = "TIMESHARED"
      val cloudletScheduler = utils.getCloudletSchedulingPolicy(policy)
      cloudletScheduler.getClass shouldBe classOf[CloudletSchedulerTimeShared]
    }

    it("should test cloudlet scheduler creation policy - spaceshared") {
      val policy = "SPACESHARED"
      val cloudletScheduler = utils.getCloudletSchedulingPolicy(policy)
      cloudletScheduler.getClass shouldBe classOf[CloudletSchedulerSpaceShared]
    }

    it("should test cloudlet scheduler creation policy - fair") {
      val policy = "FAIR"
      val cloudletScheduler = utils.getCloudletSchedulingPolicy(policy)
      cloudletScheduler.getClass shouldBe classOf[CloudletSchedulerCompletelyFair]
    }

    it("should test cloudlet scheduler creation policy - other") {
      val policy = "xyz"
      val cloudletScheduler = utils.getCloudletSchedulingPolicy(policy)
      cloudletScheduler.getClass shouldBe classOf[CloudletSchedulerSpaceShared]
    }
  }
