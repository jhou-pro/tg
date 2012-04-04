package ua.com.fielden.platform.domaintree.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.domaintree.ILocatorManager.Phase.EDITING_PHASE;
import static ua.com.fielden.platform.domaintree.ILocatorManager.Phase.FREEZED_EDITING_PHASE;
import static ua.com.fielden.platform.domaintree.ILocatorManager.Phase.USAGE_PHASE;
import static ua.com.fielden.platform.domaintree.ILocatorManager.Type.GLOBAL;
import static ua.com.fielden.platform.domaintree.ILocatorManager.Type.LOCAL;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTree.key;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.domaintree.ILocatorManager.Phase;
import ua.com.fielden.platform.domaintree.ILocatorManager.Type;
import ua.com.fielden.platform.domaintree.testing.EntityWithStringKeyType;
import ua.com.fielden.platform.domaintree.testing.MasterEntity;
import ua.com.fielden.platform.utils.Pair;

/**
 * This test case ensures correct persistence and retrieval of entities with properties of type byte[].
 *
 * @author TG Team
 *
 */
public class LocatorManagerTest extends GlobalDomainTreeRepresentationTest {
    private final String property = "entityProp.simpleEntityProp";
    private final Class<?> root = MasterEntity.class, propertyType = EntityWithStringKeyType.class;
    private final LocatorManager lm = createLocatorManager();

    private List<Pair<Class<?>, String>> list() {
	return Collections.emptyList();
    }

    private List<Pair<Class<?>, String>> list(final Class<?> root, final String property) {
	return Arrays.asList(key(root, property));
    }

    private Pair<Phase, Type> state(final Phase phase, final Type type) {
	return new Pair<Phase, Type>(phase, type);
    }

    private LocatorManager createLocatorManager() {
	final GlobalDomainTreeManager mgr = createManagerForNonBaseUser();
	final LocatorManager lm = new LocatorManager(mgr.getSerialiser(), new HashSet<Class<?>>() {{ add(root); }});
	GlobalDomainTreeManager.initLocatorManagerCrossReferences(lm, mgr.getGlobalRepresentation());
	return lm;
    }

    private void assert_synchronised_with_GlobalRepresentation() {
	// check if GLOBAL locator is synchronised with Global Representation type-related locator
	assertFalse("Incorrect state.", lm.getLocatorManager(root, property) == lm.getGlobalRepresentation().getLocatorManagerByDefault(propertyType));
	assertTrue("Incorrect state.", lm.getLocatorManager(root, property).equals(lm.getGlobalRepresentation().getLocatorManagerByDefault(propertyType)));
    }

    private void assert_NOT_synchronised_with_GlobalRepresentation() {
	// check if GLOBAL locator is synchronised with Global Representation type-related locator
	assertFalse("Incorrect state.", lm.getLocatorManager(root, property) == lm.getGlobalRepresentation().getLocatorManagerByDefault(propertyType));
	assertFalse("Incorrect state.", lm.getLocatorManager(root, property).equals(lm.getGlobalRepresentation().getLocatorManagerByDefault(propertyType)));
    }

    ////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// STATE 0 AT USAGE PHASE /////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////// History Beginning ////////////////////////////////
    private LocatorManager move0() {
	return lm;
    }

    private void state0() {
	assertNull("Incorrect state.", lm.getLocatorManager(root, property));
	assertEquals("Incorrect state.", state(USAGE_PHASE, GLOBAL), lm.phaseAndTypeOfLocatorManager(root, property));
	assertFalse("Incorrect state.", lm.isChangedLocatorManager(root, property));
	assertEquals("Incorrect state.", list(), lm.locatorKeys());
    }

    @Test
    public void test_that_at_beginning_of_history_there_are_USAGE_PHASE_with_empty_locator() {
	move0();
	state0();
    }

    ///////////////////////////////////// Accept Discard SaveGlobally Freeze /////////////////////////
    @Test
    public void test_that_Accept_Discard_SaveGlobally_and_Freeze_are_not_applicable_at_USAGE_PHASE_for_empty_locator() {
	move0();

	try {
	/* */lm.acceptLocatorManager(root, property);
	    fail("Should not be applicable.");
	} catch (final IllegalArgumentException e) {
	}
	state0();
	try {
	/* */lm.discardLocatorManager(root, property);
	    fail("Should not be applicable.");
	} catch (final IllegalArgumentException e) {
	}
	state0();
	try {
	/* */lm.saveLocatorManagerGlobally(root, property);
	    fail("Should not be applicable.");
	} catch (final IllegalArgumentException e) {
	}
	state0();
	try {
	/* */lm.freezeLocatorManager(root, property);
	    fail("Should not be applicable.");
	} catch (final IllegalArgumentException e) {
	}
	state0();
    }

    ///////////////////////////////////// Refresh //////////////////////////////////////////
    private LocatorManager move1() {
	move0();
	/* */lm.refreshLocatorManager(root, property);
	return lm;
    }

    private void state1() {
	assertNotNull("Incorrect state.", lm.getLocatorManager(root, property));
	assertEquals("Incorrect state.", state(EDITING_PHASE, GLOBAL), lm.phaseAndTypeOfLocatorManager(root, property));
	assertFalse("Incorrect state.", lm.isChangedLocatorManager(root, property));
	assertEquals("Incorrect state.", list(root, property), lm.locatorKeys());
    }

    @Test
    public void test_that_Refresh_loads_GLOBAL_locator_and_moves_it_from_USAGE_PHASE_to_EDITING_PHASE() {
	move1();
	state1();
	assert_synchronised_with_GlobalRepresentation();
    }

    ///////////////////////////////////// ResetToDefault //////////////////////////////////////////
    @Test
    public void test_that_ResetToDefault_does_nothing_for_GLOBAL_NULL_locator_and_remains_it_in_USAGE_PHASE() {
	move0();
	/* */lm.resetLocatorManagerToDefault(root, property);
	state0();
    }

    ////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// STATE 1 AT EDITING PHASE ///////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////// Refresh ResetToDefault /////////////////////////
    @Test
    public void test_that_Refresh_and_ResetToDefault_are_not_applicable_at_EDITING_PHASE_for_GLOBAL_locator() {
	move1();

	try {
	/* */lm.refreshLocatorManager(root, property);
	    fail("Should not be applicable.");
	} catch (final IllegalArgumentException e) {
	}
	state1();
	assert_synchronised_with_GlobalRepresentation();
	try {
	/* */lm.resetLocatorManagerToDefault(root, property);
	    fail("Should not be applicable.");
	} catch (final IllegalArgumentException e) {
	}
	state1();
	assert_synchronised_with_GlobalRepresentation();
    }

    ///////////////////////////////////// Change //////////////////////////////////////////
    private LocatorManager move1_and_change() {
	move1();
	lm.getLocatorManager(root, property).setRunAutomatically(true);
	return lm;
    }

    private void state1_and_changed() {
	assertNotNull("Incorrect state.", lm.getLocatorManager(root, property));
	assertEquals("Incorrect state.", state(EDITING_PHASE, GLOBAL), lm.phaseAndTypeOfLocatorManager(root, property));
	assertTrue("Incorrect state.", lm.isChangedLocatorManager(root, property));
	assertEquals("Incorrect state.", list(root, property), lm.locatorKeys());
    }

    @Test
    public void test_that_Change_only_mutates_GLOBAL_locator_and_remains_it_in_EDITING_PHASE() {
	move1_and_change();
	state1_and_changed();
	assert_NOT_synchronised_with_GlobalRepresentation();
    }

    ///////////////////////////////////// SaveGlobally //////////////////////////////////////////
    @Test
    public void test_that_SaveGlobally_does_nothing_for_GLOBAL_locator_and_remains_it_in_EDITING_PHASE() {
	move1();
	/* */lm.saveLocatorManagerGlobally(root, property);
	state1();
	assert_synchronised_with_GlobalRepresentation();

	lm.discardLocatorManager(root, property);
	move1_and_change();
	/* */lm.saveLocatorManagerGlobally(root, property);
	state1_and_changed();
	assert_synchronised_with_GlobalRepresentation();
    }

    ///////////////////////////////////// Discard //////////////////////////////////////////
    @Test
    public void test_that_Discard_removes_GLOBAL_locator_and_moves_it_from_EDITING_PHASE_to_USAGE_PHASE() {
	move1();
	/* */lm.discardLocatorManager(root, property);
	state0();

	move1_and_change();
	/* */lm.discardLocatorManager(root, property);
	state0();
    }

    ///////////////////////////////////// Accept ///////////////////////////////////////////
    private LocatorManager move2() {
	move1_and_change();
	/* */lm.acceptLocatorManager(root, property);
	return lm;
    }

    private void state2() {
	assertNotNull("Should be not null.", lm.getLocatorManager(root, property));
	assertEquals("Incorrect state.", state(USAGE_PHASE, LOCAL), lm.phaseAndTypeOfLocatorManager(root, property));
	assertFalse("Incorrect state.", lm.isChangedLocatorManager(root, property));
	assertEquals("Incorrect state.", list(root, property), lm.locatorKeys());
    }

    @Test
    public void test_that_Accept_accepts_GLOBAL_locator_locally_and_moves_it_from_EDITING_PHASE_to_USAGE_PHASE() {
	move2();
	state2();

	lm.resetLocatorManagerToDefault(root, property); // reset
	move1();
	/* */lm.acceptLocatorManager(root, property);
	state2();
    }

    ////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// STATE 2 AT USAGE PHASE /////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////// Accept Discard SaveGlobally Freeze /////////////////////////
    @Test
    public void test_that_Accept_Discard_SaveGlobally_and_Freeze_are_not_applicable_at_USAGE_PHASE_for_LOCAL_locator() {
	move2();

	try {
	    lm.acceptLocatorManager(root, property);
	    fail("Should not be applicable.");
	} catch (final IllegalArgumentException e) {
	}
	state2();
	try {
	/* */lm.discardLocatorManager(root, property);
	    fail("Should not be applicable.");
	} catch (final IllegalArgumentException e) {
	}
	state2();
	try {
	/* */lm.saveLocatorManagerGlobally(root, property);
	    fail("Should not be applicable.");
	} catch (final IllegalArgumentException e) {
	}
	state2();
	try {
	/* */lm.freezeLocatorManager(root, property);
	    fail("Should not be applicable.");
	} catch (final IllegalArgumentException e) {
	}
	state2();
    }

    ///////////////////////////////////// Refresh //////////////////////////////////////////
    private LocatorManager move3() {
	move2();
	/* */lm.refreshLocatorManager(root, property);
	return lm;
    }

    private void state3() {
	assertNotNull("Incorrect state.", lm.getLocatorManager(root, property));
	assertEquals("Incorrect state.", state(EDITING_PHASE, LOCAL), lm.phaseAndTypeOfLocatorManager(root, property));
	assertFalse("Incorrect state.", lm.isChangedLocatorManager(root, property));
	assertEquals("Incorrect state.", list(root, property), lm.locatorKeys());
    }

    @Test
    public void test_that_Refresh_do_nothing_with_LOCAL_locator_and_moves_it_from_USAGE_PHASE_to_EDITING_PHASE() {
	move3();
	state3();
    }

    ///////////////////////////////////// ResetToDefault //////////////////////////////////////////
    @Test
    public void test_that_ResetToDefault_resets_to_GLOBAL_NULL_locator_and_remains_it_in_USAGE_PHASE() {
	move2();
	/* */lm.resetLocatorManagerToDefault(root, property);
	state0();
    }

    ////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// STATE 3 AT EDITING PHASE ///////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////// Refresh ResetToDefault /////////////////////////
    @Test
    public void test_that_Refresh_and_ResetToDefault_are_not_applicable_at_EDITING_PHASE_for_LOCAL_locator() {
	move3();

	try {
	/* */lm.refreshLocatorManager(root, property);
	    fail("Should not be applicable.");
	} catch (final IllegalArgumentException e) {
	}
	state3();
	try {
	/* */lm.resetLocatorManagerToDefault(root, property);
	    fail("Should not be applicable.");
	} catch (final IllegalArgumentException e) {
	}
	state3();
    }

    ///////////////////////////////////// Change //////////////////////////////////////////
    private LocatorManager move3_and_change() {
	move3();
	lm.getLocatorManager(root, property).setRunAutomatically(false);
	return lm;
    }

    private void state3_and_changed() {
	assertNotNull("Incorrect state.", lm.getLocatorManager(root, property));
	assertEquals("Incorrect state.", state(EDITING_PHASE, LOCAL), lm.phaseAndTypeOfLocatorManager(root, property));
	assertTrue("Incorrect state.", lm.isChangedLocatorManager(root, property));
	assertEquals("Incorrect state.", list(root, property), lm.locatorKeys());
    }

    @Test
    public void test_that_Change_only_mutates_LOCAL_locator_and_remains_it_in_EDITING_PHASE() {
	move3_and_change();
	state3_and_changed();
    }

    ///////////////////////////////////// SaveGlobally //////////////////////////////////////////
    @Test
    public void test_that_SaveGlobally_does_nothing_for_LOCAL_locator_and_remains_it_in_EDITING_PHASE() {
	move3();
	/* */lm.saveLocatorManagerGlobally(root, property);
	state3();
	assert_synchronised_with_GlobalRepresentation();

	lm.discardLocatorManager(root, property);
	move3_and_change();
	/* */lm.saveLocatorManagerGlobally(root, property);
	state3_and_changed();
	assert_synchronised_with_GlobalRepresentation();
    }

    ///////////////////////////////////// Discard //////////////////////////////////////////
    @Test
    public void test_that_Discard_discards_LOCAL_locator_changes_and_moves_it_from_EDITING_PHASE_to_USAGE_PHASE() {
	move3();
	/* */lm.discardLocatorManager(root, property);
	state2();

	lm.resetLocatorManagerToDefault(root, property); // reset
	move3_and_change();
	/* */lm.discardLocatorManager(root, property);
	state2();
    }

    ///////////////////////////////////// Accept ///////////////////////////////////////////
    @Test
    public void test_that_Accept_accepts_LOCAL_locator_changes_and_moves_it_from_EDITING_PHASE_to_USAGE_PHASE() {
	move3();
	/* */lm.acceptLocatorManager(root, property);
	state2();

	lm.resetLocatorManagerToDefault(root, property); // reset
	move3_and_change();
	/* */lm.acceptLocatorManager(root, property);
	state2();
    }

    ////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// STATE 4 AT FREEZED EDITING PHASE ///////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////// Freeze ///////////////////////////////////////////
    private LocatorManager move4() {
	move3_and_change();
	/* */lm.freezeLocatorManager(root, property);
	return lm;
    }

    private void state4() {
	assertNotNull("Incorrect state.", lm.getLocatorManager(root, property));
	assertEquals("Incorrect state.", state(FREEZED_EDITING_PHASE, LOCAL), lm.phaseAndTypeOfLocatorManager(root, property));
	assertFalse("Incorrect state.", lm.isChangedLocatorManager(root, property));
	assertEquals("Incorrect state.", list(root, property), lm.locatorKeys());
    }

    private void state4_GLOBAL() {
	assertNotNull("Incorrect state.", lm.getLocatorManager(root, property));
	assertEquals("Incorrect state.", state(FREEZED_EDITING_PHASE, GLOBAL), lm.phaseAndTypeOfLocatorManager(root, property));
	assertFalse("Incorrect state.", lm.isChangedLocatorManager(root, property));
	assertEquals("Incorrect state.", list(root, property), lm.locatorKeys());
    }

    @Test
    public void test_that_Freeze_freezes_locator_changes_and_moves_it_from_EDITING_PHASE_to_FREEZED_EDITING_PHASE() {
	move4();
	state4();

	lm.discardLocatorManager(root, property); // move to EDITING
	lm.discardLocatorManager(root, property); // move to USAGE
	lm.resetLocatorManagerToDefault(root, property); // reset
	move3();
	/* */lm.freezeLocatorManager(root, property);
	state4();

	lm.discardLocatorManager(root, property); // move to EDITING
	lm.discardLocatorManager(root, property); // move to USAGE
	lm.resetLocatorManagerToDefault(root, property); // reset
	move1_and_change();
	/* */lm.freezeLocatorManager(root, property);
	state4_GLOBAL();

	lm.discardLocatorManager(root, property); // move to EDITING
	lm.discardLocatorManager(root, property); // reset
	move1();
	/* */lm.freezeLocatorManager(root, property);
	state4_GLOBAL();
    }

    ///////////////////////////////////// Refresh SaveGlobally Freeze ResetToDefault ///////////////////////////////////
    @Test
    public void test_that_Refresh_SaveGlobally_ResetToDefault_and_Freeze_are_not_applicable_at_FREEZED_EDITING_PHASE() {
	move4();

	try {
	/* */lm.refreshLocatorManager(root, property);
	    fail("Should not be applicable.");
	} catch (final IllegalArgumentException e) {
	}
	state4();
	try {
	/* */lm.saveLocatorManagerGlobally(root, property);
	    fail("Should not be applicable.");
	} catch (final IllegalArgumentException e) {
	}
	state4();
	try {
	/* */lm.freezeLocatorManager(root, property);
	    fail("Should not be applicable.");
	} catch (final IllegalArgumentException e) {
	}
	state4();
	try {
	/* */lm.resetLocatorManagerToDefault(root, property);
	    fail("Should not be applicable.");
	} catch (final IllegalArgumentException e) {
	}
	state4();
    }

    ///////////////////////////////////// Change //////////////////////////////////////////
    private LocatorManager move4_and_change() {
	move4();
	// lm.getLocatorManager(root, property).setRunAutomatically(false);
	lm.getLocatorManager(root, property).setUseForAutocompletion(true);
	return lm;
    }

    private void state4_and_changed() {
	assertNotNull("Incorrect state.", lm.getLocatorManager(root, property));
	assertEquals("Incorrect state.", state(FREEZED_EDITING_PHASE, LOCAL), lm.phaseAndTypeOfLocatorManager(root, property));
	assertTrue("Incorrect state.", lm.isChangedLocatorManager(root, property));
	assertEquals("Incorrect state.", list(root, property), lm.locatorKeys());
    }

    @Test
    public void test_that_Change_only_mutates_freezed_locator_and_remains_it_in_FREEZED_EDITING_PHASE() {
	move4_and_change();
	state4_and_changed();
    }

    ///////////////////////////////////// Discard //////////////////////////////////////////
    @Test
    public void test_that_Discard_discards_freezed_locator_changes_and_moves_it_from_FREEZED_EDITING_PHASE_to_EDITING_PHASE() {
	move4();
	/* */lm.discardLocatorManager(root, property);
	state3_and_changed();

	lm.discardLocatorManager(root, property); // move to USAGE
	lm.resetLocatorManagerToDefault(root, property); // reset
	move4_and_change();
	/* */lm.discardLocatorManager(root, property);
	state3_and_changed();
    }

    ///////////////////////////////////// Accept ///////////////////////////////////////////
    private void state3_and_changed_with_freezing_changes() {
	assertNotNull("Incorrect state.", lm.getLocatorManager(root, property));
	assertEquals("Incorrect state.", state(EDITING_PHASE, LOCAL), lm.phaseAndTypeOfLocatorManager(root, property));
	assertTrue("Incorrect state.", lm.isChangedLocatorManager(root, property));
	assertEquals("Incorrect state.", list(root, property), lm.locatorKeys());

	// what is the state?
	assertFalse("Incorrect state.", lm.getLocatorManager(root, property).isRunAutomatically());
	assertTrue("Incorrect state.", lm.getLocatorManager(root, property).isUseForAutocompletion());
    }

    @Test
    public void test_that_Accept_accepts_freezed_locator_changes_and_moves_it_from_FREEZED_EDITING_PHASE_to_EDITING_PHASE() {
	move4();
	/* */lm.acceptLocatorManager(root, property);
	state3_and_changed();

	lm.discardLocatorManager(root, property); // move to USAGE
	lm.resetLocatorManagerToDefault(root, property); // reset
	move4_and_change();
	/* */lm.acceptLocatorManager(root, property);
	state3_and_changed_with_freezing_changes();
    }
}