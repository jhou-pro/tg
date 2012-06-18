package ua.com.fielden.platform.entity.query;

import java.util.Collections;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Test;

import ua.com.fielden.platform.dao.PropertyColumn;
import ua.com.fielden.platform.dao.PropertyMetadata;
import ua.com.fielden.platform.dao.PropertyMetadata.PropertyCategory;
import ua.com.fielden.platform.entity.query.generation.BaseEntQueryTCase;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DomainMetadataPPIsTest extends BaseEntQueryTCase {
    @Test
    public void test1() {
	final SortedSet<PropertyMetadata> expected = new TreeSet<PropertyMetadata>();
	expected.add(ppi("id", LONG, false, hibType("long"), "_ID", PropertyCategory.ID));
	expected.add(ppi("version", LONG, false, hibType("long"), "_VERSION", PropertyCategory.VERSION));
	expected.add(ppi("key", STRING, false, hibType("string"), "KEY_", PropertyCategory.PRIMITIVE_KEY));
	expected.add(ppi("desc", STRING, true, hibType("string"), "DESC_", PropertyCategory.PROP));
	expected.add(ppi("make", MAKE, false, hibType("long"), "MAKE_", PropertyCategory.ENTITY));

	final SortedSet<PropertyMetadata> actual = new TreeSet<PropertyMetadata>();
	actual.addAll(DOMAIN_METADATA_ANALYSER.getPropertyMetadatasForEntity(MODEL));
	assertEquals("Incorrect result type", expected, actual);
    }

    @Test
    public void test2() {
	final SortedSet<PropertyMetadata> expected = new TreeSet<PropertyMetadata>();
	expected.add(ppi("id", LONG, false, hibType("long"), "_ID", PropertyCategory.ID));
	expected.add(ppi("version", LONG, false, hibType("long"), "_VERSION", PropertyCategory.VERSION));
	expected.add(ppi("key", STRING, false, hibType("string"), "KEY_", PropertyCategory.PRIMITIVE_KEY));
	expected.add(ppi("desc", STRING, true, hibType("string"), "DESC_", PropertyCategory.PROP));
	expected.add(ppi("model", MODEL, false, hibType("long"), "MODEL_", PropertyCategory.ENTITY));
	expected.add(ppi("price.amount", BIG_DECIMAL, true, hibType("big_decimal"), "PRICE_", PropertyCategory.COMPONENT_DETAILS));
	expected.add(ppi("purchasePrice.amount", BIG_DECIMAL, true, hibType("big_decimal"), "PURCHASEPRICE_", PropertyCategory.COMPONENT_DETAILS));
	expected.add(ppi("fuelUsages", FUEL_USAGE, false, null, Collections.<PropertyColumn> emptyList(), PropertyCategory.COLLECTIONAL));

	final SortedSet<PropertyMetadata> actual = new TreeSet<PropertyMetadata>();
	actual.addAll(DOMAIN_METADATA_ANALYSER.getPropertyMetadatasForEntity(VEHICLE));
	assertTrue(actual.containsAll(expected));
    }

    @Test
    public void test5() {
	final SortedSet<PropertyMetadata> expected = new TreeSet<PropertyMetadata>();
	expected.add(ppi("id", LONG, false, hibType("long"), "_ID", PropertyCategory.ID));
	expected.add(ppi("version", LONG, false, hibType("long"), "_VERSION", PropertyCategory.VERSION));
	//expected.add(ppi("key", STRING, false, hibType("string"), "KEY_", PropertyPersistenceType.PRIMITIVE_KEY));
	//expected.add(ppi("desc", STRING, false, hibType("string"), "DESC_", PropertyPersistenceType.PROP));
	expected.add(ppi("vehicle", VEHICLE, false, hibType("long"), "VEHICLE_", PropertyCategory.ENTITY_MEMBER_OF_COMPOSITE_KEY));
	expected.add(ppi("date", DATE, false, DOMAIN_METADATA_ANALYSER.getDomainMetadata().getHibTypesDefaults().get(Date.class), "DATE_", PropertyCategory.PRIMITIVE_MEMBER_OF_COMPOSITE_KEY));

	final SortedSet<PropertyMetadata> actual = new TreeSet<PropertyMetadata>();
	actual.addAll(DOMAIN_METADATA_ANALYSER.getPropertyMetadatasForEntity(FUEL_USAGE));

	assertTrue(actual.containsAll(expected));
    }


    @Test
    public void test4() {
	final SortedSet<PropertyMetadata> expected = new TreeSet<PropertyMetadata>();
	expected.add(ppi("id", LONG, false, hibType("long"), "_ID", PropertyCategory.ID));
	expected.add(ppi("version", LONG, false, hibType("long"), "_VERSION", PropertyCategory.VERSION));
	expected.add(ppi("key", STRING, false, hibType("string"), "USER_NAME", PropertyCategory.PRIMITIVE_KEY));
	expected.add(ppi("roles", UserAndRoleAssociation.class, false, null, Collections.<PropertyColumn> emptyList(), PropertyCategory.COLLECTIONAL));

	final SortedSet<PropertyMetadata> actual = new TreeSet<PropertyMetadata>();
	actual.addAll(DOMAIN_METADATA_ANALYSER.getPropertyMetadatasForEntity(User.class));

	assertTrue(actual.containsAll(expected));
    }

    @Test
    public void test3() {
	assertTrue(DOMAIN_METADATA_ANALYSER.isNullable(VEHICLE, "station"));
	assertTrue(DOMAIN_METADATA_ANALYSER.isNullable(VEHICLE, "station.key"));

	assertFalse(DOMAIN_METADATA_ANALYSER.isNullable(VEHICLE, "model"));
	assertFalse(DOMAIN_METADATA_ANALYSER.isNullable(VEHICLE, "model.id"));
	assertFalse(DOMAIN_METADATA_ANALYSER.isNullable(VEHICLE, "model.key"));
	assertTrue(DOMAIN_METADATA_ANALYSER.isNullable(VEHICLE, "model.desc"));
	assertFalse(DOMAIN_METADATA_ANALYSER.isNullable(VEHICLE, "model.make"));
	assertFalse(DOMAIN_METADATA_ANALYSER.isNullable(VEHICLE, "model.make.id"));
	assertFalse(DOMAIN_METADATA_ANALYSER.isNullable(VEHICLE, "model.make.key"));
	assertTrue(DOMAIN_METADATA_ANALYSER.isNullable(VEHICLE, "model.make.desc"));
	assertTrue(DOMAIN_METADATA_ANALYSER.isNullable(VEHICLE, "price.amount"));
	try {
	    assertTrue(DOMAIN_METADATA_ANALYSER.isNullable(VEHICLE, "price.currency"));
	    fail("Should have failed!");
	} catch (final Exception e) {
	}
    }
}