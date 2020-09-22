package com.kickstarter.viewmodels

import android.os.Bundle
import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.factories.*
import com.kickstarter.mock.services.MockApiClient
import com.kickstarter.mock.services.MockApolloClient
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.services.apiresponses.ShippingRulesEnvelope
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeFlowContext
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.data.ProjectData
import junit.framework.TestCase
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber

class BackingAddOnsFragmentViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: BackingAddOnsFragmentViewModel.ViewModel
    private val shippingSelectorIsGone = TestSubscriber.create<Boolean>()
    private val addOnsList = TestSubscriber.create<Triple<ProjectData, List<Reward>, ShippingRule>>()
    private val showPledgeFragment = TestSubscriber.create<Pair<PledgeData, PledgeReason>>()
    private val isEnabledButton = TestSubscriber.create<Boolean>()
    private val totalSelectedAddOns = TestSubscriber.create<Int>()
    private val isEmptyState = TestSubscriber.create<Boolean>()
    private val showErrorDialog = TestSubscriber.create<Boolean>()

    private fun setUpEnvironment(@NonNull environment: Environment) {
        this.vm = BackingAddOnsFragmentViewModel.ViewModel(environment)
        this.vm.outputs.addOnsList().subscribe(this.addOnsList)
        this.vm.outputs.shippingSelectorIsGone().subscribe(this.shippingSelectorIsGone)
        this.vm.outputs.showPledgeFragment().subscribe(this.showPledgeFragment)
        this.vm.outputs.isEnabledCTAButton().subscribe(this.isEnabledButton)
        this.vm.outputs.totalSelectedAddOns().subscribe(this.totalSelectedAddOns)
        this.vm.outputs.isEmptyState().subscribe(this.isEmptyState)
        this.vm.outputs.showErrorDialog().subscribe(this.showErrorDialog)
    }

    @Test
    fun emptyAddOnsListForReward() {
        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfig()
        currentConfig.config(config)

        setUpEnvironment(buildEnvironmentWith(emptyList(), ShippingRulesEnvelopeFactory.emptyShippingRules(), currentConfig))

        val rw = RewardFactory
                .rewardHasAddOns()
                .toBuilder()
                .shippingPreference(Reward.ShippingPreference.UNRESTRICTED.name)
                .shippingPreferenceType(Reward.ShippingPreference.UNRESTRICTED)
                .build()

        val project = ProjectFactory.project().toBuilder().rewards(listOf(rw)).build()
        val projectData = ProjectDataFactory.project(project, null, null)
        val pledgeReason = PledgeFlowContext.forPledgeReason(PledgeReason.PLEDGE)

        val bundle = Bundle()
        bundle.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, PledgeData.with(pledgeReason, projectData, rw))
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.PLEDGE)
        this.vm.arguments(bundle)

        this.addOnsList.assertValue(Triple(projectData, emptyList(), ShippingRuleFactory.emptyShippingRule()))
        this.isEmptyState.assertValue(true)

        this.lakeTest.assertValue("Add-Ons Page Viewed")
    }

    @Test
    fun addOnsForUnrestrictedSameShippingRules() {
        val shippingRule = ShippingRulesEnvelopeFactory.shippingRules()
        val addOn = RewardFactory.addOn().toBuilder()
                .shippingRules(shippingRule.shippingRules())
                .shippingPreferenceType(Reward.ShippingPreference.UNRESTRICTED) // - Reward from GraphQL use this field
                .build()
        val listAddons = listOf(addOn, addOn, addOn)

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfig()
        currentConfig.config(config)

        setUpEnvironment(buildEnvironmentWith(listAddons, shippingRule, currentConfig))

        val rw = RewardFactory.rewardHasAddOns().toBuilder()
                .shippingType(Reward.ShippingPreference.UNRESTRICTED.name.toLowerCase())
                .shippingRules(shippingRule.shippingRules())
                .shippingPreferenceType(Reward.ShippingPreference.UNRESTRICTED) // - Reward from GraphQL use this field
                .shippingPreference(Reward.ShippingPreference.UNRESTRICTED.name.toLowerCase()) // - Reward from V1 use this field
                .build()

        val project = ProjectFactory.project().toBuilder().rewards(listOf(rw)).build()
        val projectData = ProjectDataFactory.project(project, null, null)
        val pledgeReason = PledgeFlowContext.forPledgeReason(PledgeReason.PLEDGE)

        val bundle = Bundle()
        bundle.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, PledgeData.with(pledgeReason, projectData, rw))
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.PLEDGE)
        this.vm.arguments(bundle)
        this.addOnsList.assertValue(Triple(projectData,listAddons, shippingRule.shippingRules().first()))

        this.lakeTest.assertValue("Add-Ons Page Viewed")
    }

    @Test
    fun addOnsForRestrictedSameShippingRules() {
        val shippingRule = ShippingRulesEnvelopeFactory.shippingRules()
        val addOn = RewardFactory.addOn().toBuilder()
                .shippingRules(shippingRule.shippingRules())
                .shippingPreferenceType(Reward.ShippingPreference.UNRESTRICTED) // - Reward from GraphQL use this field
                .build()
        val listAddons = listOf(addOn, addOn, addOn)

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfig()
        currentConfig.config(config)

        setUpEnvironment(buildEnvironmentWith(listAddons, shippingRule, currentConfig))

        val rw = RewardFactory.rewardHasAddOns().toBuilder()
                .shippingType(Reward.ShippingPreference.RESTRICTED.name.toLowerCase())
                .shippingRules(shippingRule.shippingRules())
                .shippingPreferenceType(Reward.ShippingPreference.RESTRICTED) // - Reward from GraphQL use this field
                .shippingPreference(Reward.ShippingPreference.RESTRICTED.name.toLowerCase()) // - Reward from V1 use this field
                .build()

        val project = ProjectFactory.project().toBuilder().rewards(listOf(rw)).build()
        val projectData = ProjectDataFactory.project(project, null, null)
        val pledgeReason = PledgeFlowContext.forPledgeReason(PledgeReason.PLEDGE)

        val bundle = Bundle()
        bundle.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, PledgeData.with(pledgeReason, projectData, rw))
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.PLEDGE)
        this.vm.arguments(bundle)
        this.addOnsList.assertValue(Triple(projectData,listAddons, shippingRule.shippingRules().first()))

        this.lakeTest.assertValue("Add-Ons Page Viewed")
    }

    @Test
    fun addOnsForRestricted_whenNoMatchingShippingRulesInReward() {
        val shippingRuleAddOn = ShippingRuleFactory.germanyShippingRule()
        val shippingRuleRw = ShippingRuleFactory.usShippingRule()
        val addOn = RewardFactory.addOn().toBuilder()
                .shippingRules(listOf(shippingRuleAddOn, shippingRuleAddOn, shippingRuleAddOn))
                .shippingPreferenceType(Reward.ShippingPreference.UNRESTRICTED) // - Reward from GraphQL use this field
                .shippingPreference(Reward.ShippingPreference.UNRESTRICTED.name.toLowerCase())
                .shippingType(Reward.SHIPPING_TYPE_ANYWHERE)
                .build()
        val listAddons = listOf(addOn, addOn, addOn)

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfig()
        currentConfig.config(config)

        setUpEnvironment(buildEnvironmentWith(listAddons, ShippingRulesEnvelope.builder().shippingRules(listOf(shippingRuleRw)).build(), currentConfig))

        val rw = RewardFactory.rewardHasAddOns().toBuilder()
                .shippingRules(listOf(shippingRuleRw))
                .shippingPreferenceType(Reward.ShippingPreference.RESTRICTED) // - Reward from GraphQL use this field
                .shippingPreference(Reward.ShippingPreference.RESTRICTED.name.toLowerCase()) // - Reward from V1 check this field
                .shippingType(Reward.SHIPPING_TYPE_MULTIPLE_LOCATIONS) // - Reward from V1 to check is digital use this field
                .build()

        val project = ProjectFactory.project().toBuilder().rewards(listOf(rw)).build()
        val projectData = ProjectDataFactory.project(project, null, null)
        val pledgeReason = PledgeFlowContext.forPledgeReason(PledgeReason.PLEDGE)

        val bundle = Bundle()
        bundle.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, PledgeData.with(pledgeReason, projectData, rw))
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.PLEDGE)
        this.vm.arguments(bundle)

        this.addOnsList.assertValue(Triple(projectData, emptyList(), shippingRuleRw))

        this.lakeTest.assertValue("Add-Ons Page Viewed")
    }

    @Test
    fun addOnsForRestrictedOneMatchingShippingRules() {
        val shippingRuleAddOn = ShippingRuleFactory.germanyShippingRule()
        val shippingRuleRw = ShippingRuleFactory.usShippingRule()
        val addOn = RewardFactory.addOn().toBuilder()
                .shippingRules(listOf(shippingRuleAddOn, shippingRuleRw))
                .shippingPreferenceType(Reward.ShippingPreference.UNRESTRICTED) // - Reward from GraphQL use this field
                .build()
        val listAddons = listOf(addOn, addOn, addOn)

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfig()
        currentConfig.config(config)

        setUpEnvironment(buildEnvironmentWith(listAddons, ShippingRulesEnvelope.builder().shippingRules(listOf(shippingRuleRw)).build(), currentConfig))

        val rw = RewardFactory.rewardHasAddOns().toBuilder()
                .shippingType(Reward.ShippingPreference.RESTRICTED.name.toLowerCase())
                .shippingRules(listOf(shippingRuleRw))
                .shippingPreferenceType(Reward.ShippingPreference.RESTRICTED) // - Reward from GraphQL use this field
                .shippingPreference(Reward.ShippingPreference.RESTRICTED.name.toLowerCase()) // - Reward from V1 use this field
                .build()

        val project = ProjectFactory.project().toBuilder().rewards(listOf(rw)).build()
        val projectData = ProjectDataFactory.project(project, null, null)
        val pledgeReason = PledgeFlowContext.forPledgeReason(PledgeReason.PLEDGE)

        val bundle = Bundle()
        bundle.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, PledgeData.with(pledgeReason, projectData, rw))
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.PLEDGE)
        this.vm.arguments(bundle)

        this.addOnsList.assertValue(Triple(projectData, listAddons, shippingRuleRw))

        this.lakeTest.assertValue("Add-Ons Page Viewed")
    }

    @Test
    fun addOnsForRestrictedFilterOutNoMatching() {
        val shippingRuleAddOn = ShippingRuleFactory.germanyShippingRule()
        val shippingRuleRw = ShippingRuleFactory.usShippingRule()
        val addOn = RewardFactory.addOn().toBuilder()
                .shippingRules(listOf(shippingRuleRw))
                .shippingPreferenceType(Reward.ShippingPreference.UNRESTRICTED) // - Reward from GraphQL use this field
                .build()
        val addOn2 = RewardFactory.rewardHasAddOns().toBuilder()
                .id(11)
                .shippingRules(listOf(shippingRuleAddOn))
                .shippingPreferenceType(Reward.ShippingPreference.UNRESTRICTED) // - Reward from GraphQL use this field
                .build()
        val listAddons = listOf(addOn, addOn2, addOn, addOn)

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfig()
        currentConfig.config(config)

        setUpEnvironment(buildEnvironmentWith(listAddons, ShippingRulesEnvelope.builder().shippingRules(listOf(shippingRuleRw)).build(), currentConfig))

        val rw = RewardFactory.rewardHasAddOns().toBuilder()
                .shippingType(Reward.ShippingPreference.RESTRICTED.name.toLowerCase())
                .shippingRules(listOf(shippingRuleRw))
                .shippingPreferenceType(Reward.ShippingPreference.RESTRICTED) // - Reward from GraphQL use this field
                .shippingPreference(Reward.ShippingPreference.RESTRICTED.name.toLowerCase()) // - Reward from V1 use this field
                .build()

        val project = ProjectFactory.project().toBuilder().rewards(listOf(rw)).build()
        val projectData = ProjectDataFactory.project(project, null, null)
        val pledgeReason = PledgeFlowContext.forPledgeReason(PledgeReason.PLEDGE)

        val bundle = Bundle()
        bundle.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, PledgeData.with(pledgeReason, projectData, rw))
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.PLEDGE)
        this.vm.arguments(bundle)

        this.vm.outputs.addOnsList().subscribe {
            TestCase.assertEquals(it.second.size, 1)
            val filteredAddOn = it.second.first()
            TestCase.assertEquals(filteredAddOn, addOn2)
        }

        this.lakeTest.assertValue("Add-Ons Page Viewed")
    }

    @Test
    fun addOnsForRestricted_NoDigitalAddOn_ChangeSelectedShippingRule() {
        val shippingRuleRw = ShippingRuleFactory.usShippingRule()
        val addOn = RewardFactory.addOn().toBuilder()
                .shippingRules(listOf(shippingRuleRw))
                .shippingPreferenceType(Reward.ShippingPreference.RESTRICTED) // - Reward from GraphQL use this field
                .shippingType(Reward.SHIPPING_TYPE_SINGLE_LOCATION)
                .build()
        val listAddons = listOf(addOn, addOn, addOn)

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfig()
        currentConfig.config(config)

        setUpEnvironment(buildEnvironmentWith(listAddons, ShippingRulesEnvelope.builder().shippingRules(listOf(shippingRuleRw)).build(), currentConfig))

        val rw = RewardFactory.rewardHasAddOns().toBuilder()
                .shippingType(Reward.ShippingPreference.RESTRICTED.name.toLowerCase())
                .shippingRules(listOf(shippingRuleRw))
                .shippingPreferenceType(Reward.ShippingPreference.RESTRICTED) // - Reward from GraphQL use this field
                .shippingPreference(Reward.ShippingPreference.RESTRICTED.name.toLowerCase()) // - Reward from V1 use this field
                .build()

        val project = ProjectFactory.project().toBuilder().rewards(listOf(rw)).build()
        val projectData = ProjectDataFactory.project(project, null, null)
        val pledgeReason = PledgeFlowContext.forPledgeReason(PledgeReason.PLEDGE)

        val bundle = Bundle()
        bundle.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, PledgeData.with(pledgeReason, projectData, rw))
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.PLEDGE)
        this.vm.arguments(bundle)

        this.addOnsList.assertValue(Triple(projectData, listAddons, shippingRuleRw))

        val shippingRuleAddOn = ShippingRuleFactory.germanyShippingRule()
        this.vm.inputs.shippingRuleSelected(shippingRuleAddOn)

        this.addOnsList.assertValues(Triple(projectData, listAddons, shippingRuleRw), Triple(projectData, emptyList(), shippingRuleAddOn))

        this.lakeTest.assertValue("Add-Ons Page Viewed")
    }

    @Test
    fun testDigitalAddOns_whenDigitalReward() {

        // DIGITAL AddOns
        val addOn = RewardFactory.addOn().toBuilder()
                .shippingPreferenceType(Reward.ShippingPreference.NONE) // - Reward from GraphQL use this field
                .shippingType(Reward.SHIPPING_TYPE_NO_SHIPPING) // - // - Reward from V1 use this field
                .build()
        val listAddons = listOf(addOn, addOn, addOn)

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfig()
        currentConfig.config(config)

        setUpEnvironment(buildEnvironmentWith(listAddons, ShippingRulesEnvelopeFactory.emptyShippingRules(), currentConfig))

        // - Digital Reward
        val rw = RewardFactory.rewardHasAddOns().toBuilder()
                .shippingType(Reward.ShippingPreference.NOSHIPPING.name.toLowerCase())
                .shippingPreferenceType(Reward.ShippingPreference.NONE) // - Reward from GraphQL use this field
                .shippingType(Reward.SHIPPING_TYPE_NO_SHIPPING) // - Reward from V1 use this field
                .build()

        val project = ProjectFactory.project().toBuilder().rewards(listOf(rw)).build()
        val projectData = ProjectDataFactory.project(project, null, null)
        val pledgeReason = PledgeFlowContext.forPledgeReason(PledgeReason.PLEDGE)

        val bundle = Bundle()
        bundle.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, PledgeData.with(pledgeReason, projectData, rw))
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.PLEDGE)
        this.vm.arguments(bundle)

        this.addOnsList.assertValue(Triple(projectData, listAddons, ShippingRuleFactory.emptyShippingRule()))

        this.lakeTest.assertValue("Add-Ons Page Viewed")
    }

    @Test
    fun testShippingSelectorGone_WhenNoAddOns_Shippable() {
        val shippingRuleRw = ShippingRuleFactory.usShippingRule()
        val addOn = RewardFactory.addOn().toBuilder()
                .shippingType(Reward.SHIPPING_TYPE_NO_SHIPPING)
                .shippingPreferenceType(Reward.ShippingPreference.NOSHIPPING) // - Reward from GraphQL use this field
                .build()
        val listAddons = listOf(addOn, addOn, addOn)

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfig()
        currentConfig.config(config)

        setUpEnvironment(buildEnvironmentWith(listAddons, ShippingRulesEnvelope.builder().shippingRules(listOf(shippingRuleRw)).build(), currentConfig))

        val rw = RewardFactory.rewardHasAddOns().toBuilder()
                .shippingType(Reward.SHIPPING_TYPE_NO_SHIPPING)
                .shippingRules(listOf(shippingRuleRw))
                .shippingPreferenceType(Reward.ShippingPreference.NONE) // - Reward from GraphQL use this field
                .shippingPreference(Reward.ShippingPreference.NOSHIPPING.name.toLowerCase()) // - Reward from V1 use this field
                .build()

        val project = ProjectFactory.project().toBuilder().rewards(listOf(rw)).build()
        val projectData = ProjectDataFactory.project(project, null, null)
        val pledgeReason = PledgeFlowContext.forPledgeReason(PledgeReason.PLEDGE)

        val bundle = Bundle()
        bundle.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, PledgeData.with(pledgeReason, projectData, rw))
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.PLEDGE)
        this.vm.arguments(bundle)

        this.shippingSelectorIsGone.assertValues(true)

        this.lakeTest.assertValue("Add-Ons Page Viewed")
    }


    @Test
    fun testShippingSelectorGoneWhenBaseRewardIsNotShippable() {
        val shippingRuleRw = ShippingRuleFactory.usShippingRule()
        val addOn = RewardFactory.addOn().toBuilder()
                .build()
        val listAddons = listOf(addOn, addOn, addOn)

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfig()
        currentConfig.config(config)

        setUpEnvironment(buildEnvironmentWith(listAddons, ShippingRulesEnvelope.builder().shippingRules(listOf(shippingRuleRw)).build(), currentConfig))

        val rw = RewardFactory.rewardHasAddOns().toBuilder()
                .shippingType(Reward.SHIPPING_TYPE_NO_SHIPPING)
                .shippingRules(listOf(shippingRuleRw))
                .shippingPreferenceType(Reward.ShippingPreference.NONE) // - Reward from GraphQL use this field
                .shippingPreference(Reward.ShippingPreference.NOSHIPPING.name.toLowerCase()) // - Reward from V1 use this field
                .build()

        val project = ProjectFactory.project().toBuilder().rewards(listOf(rw)).build()
        val projectData = ProjectDataFactory.project(project, null, null)
        val pledgeReason = PledgeFlowContext.forPledgeReason(PledgeReason.PLEDGE)

        val bundle = Bundle()
        bundle.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, PledgeData.with(pledgeReason, projectData, rw))
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.PLEDGE)
        this.vm.arguments(bundle)

        this.shippingSelectorIsGone.assertValues(true)

        this.lakeTest.assertValue("Add-Ons Page Viewed")
    }

    @Test
    fun continueButtonPressedNoAddOnsSelected() {
        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfig()
        currentConfig.config(config)

        setUpEnvironment(buildEnvironmentWith(emptyList(), ShippingRulesEnvelopeFactory.emptyShippingRules(), currentConfig))

        val rw = RewardFactory
                .rewardHasAddOns()
                .toBuilder()
                .shippingPreference(Reward.ShippingPreference.UNRESTRICTED.name)
                .shippingPreferenceType(Reward.ShippingPreference.UNRESTRICTED)
                .build()

        val project = ProjectFactory.project().toBuilder().rewards(listOf(rw)).build()
        val projectData = ProjectDataFactory.project(project, null, null)
        val pledgeReason = PledgeFlowContext.forPledgeReason(PledgeReason.PLEDGE)
        val pledgeData = PledgeData.with(pledgeReason, projectData, rw)

        val bundle = Bundle()
        bundle.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, pledgeData)
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.PLEDGE)
        this.vm.arguments(bundle)

        val quantityPerId = Pair(0, rw.id())
        this.vm.inputs.quantityPerId(quantityPerId)
        this.vm.inputs.continueButtonPressed()

        this.addOnsList.assertValue(Triple(projectData, emptyList(), ShippingRuleFactory.emptyShippingRule()))
        this.vm.outputs.showPledgeFragment()
                .subscribe {
                    TestCase.assertEquals(it.first, pledgeData)
                    TestCase.assertEquals(it.second, pledgeReason)
                }

        this.lakeTest.assertValues("Add-Ons Page Viewed", "Add-Ons Continue Button Clicked")
    }

    @Test
    fun continueButtonPressedAddOnsFewAddOnsSelected() {
        val shippingRule = ShippingRulesEnvelopeFactory.shippingRules()
        val addOn = RewardFactory.addOn().toBuilder()
                .shippingRules(shippingRule.shippingRules())
                .shippingPreferenceType(Reward.ShippingPreference.UNRESTRICTED) // - Reward from GraphQL use this field
                .build()
        val addOn2 = addOn.toBuilder().id(8).build()
        val addOn3 = addOn.toBuilder().id(99).build()
        val listAddons = listOf(addOn, addOn2, addOn3)

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfig()
        currentConfig.config(config)

        setUpEnvironment(buildEnvironmentWith(listAddons, shippingRule, currentConfig))

        val rw = RewardFactory.rewardHasAddOns().toBuilder()
                .shippingType(Reward.ShippingPreference.UNRESTRICTED.name.toLowerCase())
                .shippingRules(shippingRule.shippingRules())
                .shippingPreferenceType(Reward.ShippingPreference.UNRESTRICTED) // - Reward from GraphQL use this field
                .shippingPreference(Reward.ShippingPreference.UNRESTRICTED.name.toLowerCase()) // - Reward from V1 use this field
                .build()

        val project = ProjectFactory.project().toBuilder().rewards(listOf(rw)).build()
        val projectData = ProjectDataFactory.project(project, null, null)
        val pledgeReason = PledgeFlowContext.forPledgeReason(PledgeReason.PLEDGE)
        val pledgeData = PledgeData.with(pledgeReason, projectData, rw)

        val bundle = Bundle()
        bundle.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, PledgeData.with(pledgeReason, projectData, rw))
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.PLEDGE)
        this.vm.arguments(bundle)
        this.addOnsList.assertValue(Triple(projectData,listAddons, shippingRule.shippingRules().first()))

        val quantityPerIdAddOn1 = Pair(7, addOn.id())
        val quantityPerIdAddOn2 = Pair(2, addOn2.id())
        val quantityPerIdAddOn3 = Pair(5, addOn3.id())

        this.vm.inputs.quantityPerId(quantityPerIdAddOn1)
        this.vm.inputs.quantityPerId(quantityPerIdAddOn2)
        this.vm.inputs.quantityPerId(quantityPerIdAddOn3)
        this.vm.inputs.continueButtonPressed()

        // - Comparison purposes the quantity of the add-ons has been updated in the previous vm.input.quantityPerId
        val listAddons1 = listOf(addOn.toBuilder().quantity(7).build(), addOn2, addOn3)
        val listAddons2 = listOf(addOn.toBuilder().quantity(7).build(), addOn2.toBuilder().quantity(2).build(), addOn3)
        val listAddons3 = listOf(addOn.toBuilder().quantity(7).build(),
                addOn2.toBuilder().quantity(2).build(),
                addOn3.toBuilder().quantity(5).build())

        this.addOnsList.assertValues(
                Triple(projectData,listAddons, shippingRule.shippingRules().first()),
                Triple(projectData,listAddons1, shippingRule.shippingRules().first()),
                Triple(projectData,listAddons2, shippingRule.shippingRules().first()),
                Triple(projectData,listAddons3, shippingRule.shippingRules().first()))

        this.isEnabledButton.assertNoValues()
        this.vm.outputs.showPledgeFragment()
                .subscribe {
                    TestCase.assertEquals(it.first, pledgeData)
                    TestCase.assertEquals(it.second, pledgeReason)

                    val selectedAddOnsList = pledgeData.addOns()
                    TestCase.assertEquals(selectedAddOnsList, 3)

                    TestCase.assertEquals(selectedAddOnsList?.first()?.id(), addOn.id())
                    TestCase.assertEquals(selectedAddOnsList?.first()?.quantity(), 7)

                    TestCase.assertEquals(selectedAddOnsList?.last()?.id(), 99)
                    TestCase.assertEquals(selectedAddOnsList?.last()?.quantity(), 5)
                }

        this.lakeTest.assertValues("Add-Ons Page Viewed", "Add-Ons Continue Button Clicked")
    }

    @Test
    fun givenBackedAddOns_whenUpdatingRewardReason_DisabledButton() {
        val shippingRule = ShippingRulesEnvelopeFactory.shippingRules()
        val addOn = RewardFactory.addOn().toBuilder()
                .shippingRules(shippingRule.shippingRules())
                .shippingPreferenceType(Reward.ShippingPreference.UNRESTRICTED) // - Reward from GraphQL use this field
                .build()
        val addOn2 = addOn.toBuilder().id(8).build()
        val addOn3 = addOn.toBuilder().id(99).build()
        val listAddons = listOf(addOn, addOn2, addOn3)
        val listAddonsBacked = listOf(addOn2.toBuilder().quantity(2).build(), addOn3.toBuilder().quantity(1).build())
        val combinedList = listOf(addOn, addOn2.toBuilder().quantity(2).build(), addOn3.toBuilder().quantity(1).build())

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfig()
        currentConfig.config(config)

        setUpEnvironment(buildEnvironmentWith(listAddons, shippingRule, currentConfig))

        val rw = RewardFactory.rewardHasAddOns().toBuilder()
                .shippingType(Reward.ShippingPreference.UNRESTRICTED.name.toLowerCase())
                .shippingRules(shippingRule.shippingRules())
                .shippingPreferenceType(Reward.ShippingPreference.UNRESTRICTED) // - Reward from GraphQL use this field
                .shippingPreference(Reward.ShippingPreference.UNRESTRICTED.name.toLowerCase()) // - Reward from V1 use this field
                .build()
        val project = ProjectFactory.project()

        // -Build the backing with location and list of AddOns
        val backing = BackingFactory.backing(project, UserFactory.user(), rw)
                .toBuilder()
                .locationId(ShippingRuleFactory.usShippingRule().location().id())
                .location(ShippingRuleFactory.usShippingRule().location())
                .addOns(listAddonsBacked)
                .build()
        val backedProject = project.toBuilder()
                .rewards(listOf(rw))
                .backing(backing)
                .build()

        val projectData = ProjectDataFactory.project(backedProject, null, null)
        val pledgeReason = PledgeFlowContext.forPledgeReason(PledgeReason.UPDATE_REWARD)

        val bundle = Bundle()
        bundle.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, PledgeData.with(pledgeReason, projectData, rw))
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.UPDATE_REWARD)

        this.vm.arguments(bundle)

        this.isEnabledButton.assertValue(false)
        this.addOnsList.assertValue(Triple(projectData,combinedList, shippingRule.shippingRules().first()))

        this.lakeTest.assertValue("Add-Ons Page Viewed")
    }

    @Test
    fun givenBackedAddOns_whenUpdatingRewardReasonIncreaseQuantity_EnabledButtonAndResultList() {
        val shippingRule = ShippingRulesEnvelopeFactory.shippingRules()

        val addOn = RewardFactory.addOn().toBuilder()
                .shippingRules(shippingRule.shippingRules())
                .shippingPreferenceType(Reward.ShippingPreference.UNRESTRICTED) // - Reward from GraphQL use this field
                .build()
        val addOn2 = addOn.toBuilder().id(8).build()
        val addOn3 = addOn.toBuilder().id(99).build()

        val listAddons = listOf(addOn, addOn2, addOn3)
        val listAddonsBacked = listOf(addOn2.toBuilder().quantity(2).build(), addOn3.toBuilder().quantity(1).build())
        val combinedList = listOf(addOn, addOn2.toBuilder().quantity(2).build(), addOn3.toBuilder().quantity(1).build())

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfig()
        currentConfig.config(config)

        setUpEnvironment(buildEnvironmentWith(listAddons, shippingRule, currentConfig))

        val rw = RewardFactory.rewardHasAddOns().toBuilder()
                .shippingType(Reward.ShippingPreference.UNRESTRICTED.name.toLowerCase())
                .shippingRules(shippingRule.shippingRules())
                .shippingPreferenceType(Reward.ShippingPreference.UNRESTRICTED) // - Reward from GraphQL use this field
                .shippingPreference(Reward.ShippingPreference.UNRESTRICTED.name.toLowerCase()) // - Reward from V1 use this field.
                .shippingType(Reward.SHIPPING_TYPE_ANYWHERE) // - Reward from V1 use this field to check if is Digital
                .build()
        val project = ProjectFactory.project()

        // -Build the backing with location and list of AddOns
        val backing = BackingFactory.backing(project, UserFactory.user(), rw)
                .toBuilder()
                .locationId(ShippingRuleFactory.usShippingRule().location().id())
                .location(ShippingRuleFactory.usShippingRule().location())
                .addOns(listAddonsBacked)
                .build()
        val backedProject = project.toBuilder()
                .rewards(listOf(rw))
                .backing(backing)
                .build()

        val projectData = ProjectDataFactory.project(backedProject, null, null)
        val pledgeReason = PledgeFlowContext.forPledgeReason(PledgeReason.UPDATE_REWARD)

        val bundle = Bundle()
        bundle.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, PledgeData.with(pledgeReason, projectData, rw))
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.UPDATE_REWARD)

        this.vm.arguments(bundle)

        val updateList = listOf(addOn, addOn2.toBuilder().quantity(2).build(), addOn3.toBuilder().quantity(7).build())
        this.vm.inputs.quantityPerId(Pair(7, addOn3.id()))
        this.vm.inputs.continueButtonPressed()

        this.isEnabledButton.assertValues(false, true)
        this.addOnsList.assertValues(
                Triple(projectData,combinedList, shippingRule.shippingRules().first()),
                Triple(projectData,updateList, shippingRule.shippingRules().first())
        )

        // - Always 0 first time, them summatory of all addOns quantity every time the list gets updated
        this.totalSelectedAddOns.assertValues(0, 9, 9)
        this.vm.outputs.showPledgeFragment()
                .subscribe {
                    TestCase.assertEquals(it.first.addOns(), updateList)
                }

        this.lakeTest.assertValues("Add-Ons Page Viewed", "Add-Ons Continue Button Clicked")
    }

    @Test
    fun emptyState_whenNoAddOnsForShippingRule_shouldShowEmptyViewState() {
        val shippingRuleAddOn = ShippingRuleFactory.germanyShippingRule()
        val shippingRuleRw = ShippingRuleFactory.usShippingRule()
        val addOn = RewardFactory.addOn().toBuilder()
                .shippingRules(listOf(shippingRuleAddOn, shippingRuleAddOn, shippingRuleAddOn))
                .shippingPreferenceType(Reward.ShippingPreference.RESTRICTED)
                .shippingType(Reward.SHIPPING_TYPE_SINGLE_LOCATION)
                .build()
        val listAddons = listOf(addOn, addOn, addOn)

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfig()
        currentConfig.config(config)

        setUpEnvironment(buildEnvironmentWith(listAddons, ShippingRulesEnvelope.builder().shippingRules(listOf(shippingRuleRw)).build(), currentConfig))

        val rw = RewardFactory.rewardHasAddOns().toBuilder()
                .shippingType(Reward.ShippingPreference.RESTRICTED.name.toLowerCase())
                .shippingRules(listOf(shippingRuleRw))
                .shippingPreferenceType(Reward.ShippingPreference.RESTRICTED)
                .shippingPreference(Reward.ShippingPreference.RESTRICTED.name.toLowerCase())
                .build()

        val project = ProjectFactory.project().toBuilder().rewards(listOf(rw)).build()
        val projectData = ProjectDataFactory.project(project, null, null)
        val pledgeReason = PledgeFlowContext.forPledgeReason(PledgeReason.PLEDGE)

        val bundle = Bundle()
        bundle.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, PledgeData.with(pledgeReason, projectData, rw))
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.PLEDGE)
        this.vm.arguments(bundle)

        this.addOnsList.assertValue(Triple(projectData, emptyList(), shippingRuleRw))
        this.isEmptyState.assertValue(true)

        this.lakeTest.assertValue("Add-Ons Page Viewed")
    }

    @Test
    fun emptyState_whenMatchingShippingRule_ShouldNotShowEmptyState() {
        val shippingRuleAddOn = ShippingRuleFactory.usShippingRule()
        val shippingRuleRw = ShippingRuleFactory.usShippingRule()
        val addOn = RewardFactory.addOn().toBuilder()
                .shippingRules(listOf(shippingRuleAddOn, shippingRuleAddOn, shippingRuleAddOn))
                .shippingPreferenceType(Reward.ShippingPreference.RESTRICTED)
                .build()
        val listAddons = listOf(addOn, addOn, addOn)

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfig()
        currentConfig.config(config)

        setUpEnvironment(buildEnvironmentWith(listAddons, ShippingRulesEnvelope.builder().shippingRules(listOf(shippingRuleRw)).build(), currentConfig))

        val rw = RewardFactory.rewardHasAddOns().toBuilder()
                .shippingType(Reward.ShippingPreference.RESTRICTED.name.toLowerCase())
                .shippingRules(listOf(shippingRuleRw))
                .shippingPreferenceType(Reward.ShippingPreference.RESTRICTED)
                .shippingPreference(Reward.ShippingPreference.RESTRICTED.name.toLowerCase())
                .build()

        val project = ProjectFactory.project().toBuilder().rewards(listOf(rw)).build()
        val projectData = ProjectDataFactory.project(project, null, null)
        val pledgeReason = PledgeFlowContext.forPledgeReason(PledgeReason.PLEDGE)

        val bundle = Bundle()
        bundle.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, PledgeData.with(pledgeReason, projectData, rw))
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.PLEDGE)
        this.vm.arguments(bundle)

        this.addOnsList.assertValue(Triple(projectData, listAddons, shippingRuleRw))
        this.isEmptyState.assertValue(false)

        this.lakeTest.assertValue("Add-Ons Page Viewed")
    }

    @Test
    fun errorState_whenErrorReturned_shouldShowErrorAlertDialog() {
        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfig()
        currentConfig.config(config)

        setUpEnvironment(buildEnvironmentWithError(currentConfig))

        val rw = RewardFactory.rewardHasAddOns().toBuilder()
                .build()

        val project = ProjectFactory.project().toBuilder().rewards(listOf(rw)).build()
        val projectData = ProjectDataFactory.project(project, null, null)
        val pledgeReason = PledgeFlowContext.forPledgeReason(PledgeReason.PLEDGE)

        val bundle = Bundle()
        bundle.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, PledgeData.with(pledgeReason, projectData, rw))
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.PLEDGE)
        this.vm.arguments(bundle)

        // Two values -> two failed network calls
        this.showErrorDialog.assertValues(true, true)
    }

    fun addOnsList_whenUnavailable_FilteredOut() {
        val shippingRule = ShippingRulesEnvelopeFactory.shippingRules()
        val addOn = RewardFactory.addOn().toBuilder()
                .shippingRules(shippingRule.shippingRules())
                .shippingPreferenceType(Reward.ShippingPreference.UNRESTRICTED) // - Reward from GraphQL use this field
                .build()

        val addOns2 = addOn.toBuilder().isAvailable(false).build()
        val listAddons = listOf(addOn, addOns2, addOn)

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfig()
        currentConfig.config(config)

        setUpEnvironment(buildEnvironmentWith(listAddons, shippingRule, currentConfig))

        val rw = RewardFactory.rewardHasAddOns().toBuilder()
                .shippingType(Reward.ShippingPreference.UNRESTRICTED.name.toLowerCase())
                .shippingRules(shippingRule.shippingRules())
                .shippingPreferenceType(Reward.ShippingPreference.UNRESTRICTED) // - Reward from GraphQL use this field
                .shippingPreference(Reward.ShippingPreference.UNRESTRICTED.name.toLowerCase()) // - Reward from V1 use this field
                .build()

        val project = ProjectFactory.project().toBuilder().rewards(listOf(rw)).build()
        val projectData = ProjectDataFactory.project(project, null, null)
        val pledgeReason = PledgeFlowContext.forPledgeReason(PledgeReason.PLEDGE)

        val bundle = Bundle()
        bundle.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, PledgeData.with(pledgeReason, projectData, rw))
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.PLEDGE)
        this.vm.arguments(bundle)

        val filteredList = listOf(addOn, addOn)
        this.addOnsList.assertValue(Triple(projectData,filteredList, shippingRule.shippingRules().first()))
    }

    private fun buildEnvironmentWithError(currentConfig: MockCurrentConfig): Environment {

        return environment()
                .toBuilder()
                .apolloClient(object : MockApolloClient() {
                    override fun getProjectAddOns(slug: String): Observable<List<Reward>> {
                        return Observable.error(ApiExceptionFactory.badRequestException())
                    }
                })
                .apiClient(object : MockApiClient() {
                    override fun fetchShippingRules(project: Project, reward: Reward): Observable<ShippingRulesEnvelope> {
                        return Observable.error(ApiExceptionFactory.badRequestException())
                    }
                })
                .currentConfig(currentConfig)
                .build()
    }

    private fun buildEnvironmentWith(addOns: List<Reward>, shippingRule: ShippingRulesEnvelope, currentConfig: MockCurrentConfig): Environment {
        return environment()
                .toBuilder()
                .apolloClient(object : MockApolloClient() {
                    override fun getProjectAddOns(slug: String): Observable<List<Reward>> {
                        return Observable.just(addOns)
                    }
                })
                .apiClient(object : MockApiClient() {
                    override fun fetchShippingRules(project: Project, reward: Reward): Observable<ShippingRulesEnvelope> {
                        return Observable.just(shippingRule)
                    }
                })
                .currentConfig(currentConfig)
                .build()
    }

    private fun buildEnvironmentWith(addOns: List<Reward>, currentConfig: MockCurrentConfig): Environment {

        return environment()
                .toBuilder()
                .apolloClient(object : MockApolloClient() {
                    override fun getProjectAddOns(slug: String): Observable<List<Reward>> {
                        return Observable.just(addOns)
                    }
                })
                .currentConfig(currentConfig)
                .build()
    }
}