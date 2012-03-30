package ua.com.fielden.platform.test;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.domain.PlatformDomainTypes;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.persistence.composite.EntityWithDynamicCompositeKey;
import ua.com.fielden.platform.persistence.types.EntityWithExTaxAndTaxMoney;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;
import ua.com.fielden.platform.persistence.types.EntityWithSimpleMoney;
import ua.com.fielden.platform.persistence.types.EntityWithSimpleTaxMoney;
import ua.com.fielden.platform.persistence.types.EntityWithTaxMoney;
import ua.com.fielden.platform.sample.domain.TgBogieClass;
import ua.com.fielden.platform.sample.domain.TgFuelUsage;
import ua.com.fielden.platform.sample.domain.TgModelCount;
import ua.com.fielden.platform.sample.domain.TgModelYearCount;
import ua.com.fielden.platform.sample.domain.TgOrgUnit1;
import ua.com.fielden.platform.sample.domain.TgOrgUnit2;
import ua.com.fielden.platform.sample.domain.TgOrgUnit3;
import ua.com.fielden.platform.sample.domain.TgOrgUnit4;
import ua.com.fielden.platform.sample.domain.TgOrgUnit5;
import ua.com.fielden.platform.sample.domain.TgTimesheet;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.sample.domain.TgWagon;
import ua.com.fielden.platform.sample.domain.TgWagonClass;
import ua.com.fielden.platform.sample.domain.TgWagonClassCompatibility;
import ua.com.fielden.platform.sample.domain.TgWorkOrder;
import ua.com.fielden.platform.test.entities.ComplexKeyEntity;
import ua.com.fielden.platform.test.entities.CompositeEntity;
import ua.com.fielden.platform.test.entities.CompositeEntityKey;

/**
 * A class to enlist platform test domain entities. Should be replaced with runtime generation via reflection.
 *
 * @author TG Team
 *
 */
public class PlatformTestDomainTypes {
    public static final List<Class<? extends AbstractEntity<?>>> entityTypes = new ArrayList<Class<? extends AbstractEntity<?>>>();

    static void add(final Class<? extends AbstractEntity<?>> domainType) {
	entityTypes.add(domainType);
    }

    static {
	entityTypes.addAll(PlatformDomainTypes.types);
	add(TgBogieClass.class);
	add(TgWagon.class);
	add(TgWagonClass.class);
	add(TgWagonClassCompatibility.class);
	add(TgTimesheet.class);
	add(TgVehicle.class);
	add(TgVehicleModel.class);
	add(TgVehicleMake.class);
	add(TgOrgUnit1.class);
	add(TgOrgUnit2.class);
	add(TgOrgUnit3.class);
	add(TgOrgUnit4.class);
	add(TgOrgUnit5.class);
	add(TgWorkOrder.class);
	add(TgFuelUsage.class);
	add(TgModelCount.class);
	add(TgModelYearCount.class);
	add(CompositeEntity.class);
	add(CompositeEntityKey.class);
	add(ComplexKeyEntity.class);
	add(EntityWithMoney.class);
	add(EntityWithTaxMoney.class);
	add(EntityWithExTaxAndTaxMoney.class);
	add(EntityWithSimpleTaxMoney.class);
	add(EntityWithSimpleMoney.class);
	add(EntityWithDynamicCompositeKey.class);
    }
}