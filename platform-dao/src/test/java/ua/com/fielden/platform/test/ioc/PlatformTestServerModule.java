package ua.com.fielden.platform.test.ioc;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import ua.com.fielden.platform.dao.EntityWithMoneyDao;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.ioc.BasicWebServerModule;
import ua.com.fielden.platform.migration.controller.IMigrationErrorDao;
import ua.com.fielden.platform.migration.controller.IMigrationHistoryDao;
import ua.com.fielden.platform.migration.controller.IMigrationRunDao;
import ua.com.fielden.platform.migration.dao.MigrationErrorDao;
import ua.com.fielden.platform.migration.dao.MigrationHistoryDao;
import ua.com.fielden.platform.migration.dao.MigrationRunDao;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;
import ua.com.fielden.platform.sample.domain.ITgAverageFuelUsage;
import ua.com.fielden.platform.sample.domain.ITgBogie;
import ua.com.fielden.platform.sample.domain.ITgBogieClass;
import ua.com.fielden.platform.sample.domain.ITgBogieLocation;
import ua.com.fielden.platform.sample.domain.ITgFuelType;
import ua.com.fielden.platform.sample.domain.ITgFuelUsage;
import ua.com.fielden.platform.sample.domain.ITgMakeCount;
import ua.com.fielden.platform.sample.domain.ITgMeterReading;
import ua.com.fielden.platform.sample.domain.ITgOrgUnit1;
import ua.com.fielden.platform.sample.domain.ITgOrgUnit2;
import ua.com.fielden.platform.sample.domain.ITgOrgUnit3;
import ua.com.fielden.platform.sample.domain.ITgOrgUnit4;
import ua.com.fielden.platform.sample.domain.ITgOrgUnit5;
import ua.com.fielden.platform.sample.domain.ITgTimesheet;
import ua.com.fielden.platform.sample.domain.ITgVehicle;
import ua.com.fielden.platform.sample.domain.ITgVehicleFinDetails;
import ua.com.fielden.platform.sample.domain.ITgVehicleMake;
import ua.com.fielden.platform.sample.domain.ITgVehicleModel;
import ua.com.fielden.platform.sample.domain.ITgWagon;
import ua.com.fielden.platform.sample.domain.ITgWagonClass;
import ua.com.fielden.platform.sample.domain.ITgWagonClassCompatibility;
import ua.com.fielden.platform.sample.domain.ITgWagonSlot;
import ua.com.fielden.platform.sample.domain.ITgWorkshop;
import ua.com.fielden.platform.sample.domain.TgAverageFuelUsageDao;
import ua.com.fielden.platform.sample.domain.TgBogieClassDao;
import ua.com.fielden.platform.sample.domain.TgBogieDao;
import ua.com.fielden.platform.sample.domain.TgBogieLocationDao;
import ua.com.fielden.platform.sample.domain.TgFuelTypeDao;
import ua.com.fielden.platform.sample.domain.TgFuelUsageDao;
import ua.com.fielden.platform.sample.domain.TgMakeCountDao;
import ua.com.fielden.platform.sample.domain.TgMeterReadingDao;
import ua.com.fielden.platform.sample.domain.TgOrgUnit1Dao;
import ua.com.fielden.platform.sample.domain.TgOrgUnit2Dao;
import ua.com.fielden.platform.sample.domain.TgOrgUnit3Dao;
import ua.com.fielden.platform.sample.domain.TgOrgUnit4Dao;
import ua.com.fielden.platform.sample.domain.TgOrgUnit5Dao;
import ua.com.fielden.platform.sample.domain.TgTimesheetDao;
import ua.com.fielden.platform.sample.domain.TgVehicleDao;
import ua.com.fielden.platform.sample.domain.TgVehicleFinDetailsDao;
import ua.com.fielden.platform.sample.domain.TgVehicleMakeDao;
import ua.com.fielden.platform.sample.domain.TgVehicleModelDao;
import ua.com.fielden.platform.sample.domain.TgWagonClassCompatibilityDao;
import ua.com.fielden.platform.sample.domain.TgWagonClassDao;
import ua.com.fielden.platform.sample.domain.TgWagonDao;
import ua.com.fielden.platform.sample.domain.TgWagonSlotDao;
import ua.com.fielden.platform.sample.domain.TgWorkshopDao;
import ua.com.fielden.platform.security.provider.SecurityTokenProvider;
import ua.com.fielden.platform.serialisation.impl.ISerialisationClassProvider;

import com.google.inject.TypeLiteral;


/**
 * Serve IoC module for platform related testing.
 *
 * @author TG Team
 *
 */
public class PlatformTestServerModule extends BasicWebServerModule {

    public PlatformTestServerModule(
	    final Map<Class, Class> defaultHibernateTypes, //
	    final List<Class<? extends AbstractEntity<?>>> applicationEntityTypes,//
	    final Class<? extends ISerialisationClassProvider> serialisationClassProviderType, //
	    final Class<? extends IFilter> automaticDataFilterType, //
	    final SecurityTokenProvider tokenProvider,//
	    final Properties props) throws Exception {
	super(defaultHibernateTypes, applicationEntityTypes, serialisationClassProviderType, automaticDataFilterType, tokenProvider, props);
    }

    public PlatformTestServerModule(
	    final Map<Class, Class> defaultHibernateTypes, //
	    final List<Class<? extends AbstractEntity<?>>> applicationEntityTypes,//
	    final Class<? extends ISerialisationClassProvider> serialisationClassProviderType, //
	    final Class<? extends IFilter> automaticDataFilterType, //
	    final Properties props) throws Exception {
	super(defaultHibernateTypes, applicationEntityTypes, serialisationClassProviderType, automaticDataFilterType, null, props);
    }

    @Override
    protected void configure() {
	super.configure();
	// bind DAO
//	bind(IWheelsetDao.class).to(WheelsetDao.class);
//	bind(IWorkshopDao2.class).to(WorkshopDao2.class);
//	bind(IWheelsetClassDao.class).to(WheelsetClassDao.class);
//	bind(IWorkorderDao.class).to(WorkorderDao.class);
//	bind(IWorkorderableDao.class).to(WorkorderableDao.class);
//	bind(IAdviceDao.class).to(AdviceDao.class);

	bind(ITgOrgUnit1.class).to(TgOrgUnit1Dao.class);
	bind(ITgOrgUnit2.class).to(TgOrgUnit2Dao.class);
	bind(ITgOrgUnit3.class).to(TgOrgUnit3Dao.class);
	bind(ITgOrgUnit4.class).to(TgOrgUnit4Dao.class);
	bind(ITgOrgUnit5.class).to(TgOrgUnit5Dao.class);

	bind(ITgBogieLocation.class).to(TgBogieLocationDao.class);
	bind(ITgBogie.class).to(TgBogieDao.class);
	bind(ITgBogieClass.class).to(TgBogieClassDao.class);
	bind(ITgWagon.class).to(TgWagonDao.class);
	bind(ITgWagonSlot.class).to(TgWagonSlotDao.class);
	bind(ITgWagonClass.class).to(TgWagonClassDao.class);
	bind(ITgWagonClassCompatibility.class).to(TgWagonClassCompatibilityDao.class);
	bind(ITgWorkshop.class).to(TgWorkshopDao.class);
	bind(ITgTimesheet.class).to(TgTimesheetDao.class);
	bind(ITgVehicle.class).to(TgVehicleDao.class);
	bind(ITgVehicleFinDetails.class).to(TgVehicleFinDetailsDao.class);
	bind(ITgFuelUsage.class).to(TgFuelUsageDao.class);
	bind(ITgFuelType.class).to(TgFuelTypeDao.class);
	bind(ITgVehicleModel.class).to(TgVehicleModelDao.class);
	bind(ITgVehicleMake.class).to(TgVehicleMakeDao.class);
	bind(ITgMeterReading.class).to(TgMeterReadingDao.class);
	bind(IMigrationErrorDao.class).to(MigrationErrorDao.class);
	bind(IMigrationRunDao.class).to(MigrationRunDao.class);
	bind(IMigrationHistoryDao.class).to(MigrationHistoryDao.class);

	bind(ITgMakeCount.class).to(TgMakeCountDao.class);
	bind(ITgAverageFuelUsage.class).to(TgAverageFuelUsageDao.class);

	bind(new TypeLiteral<IEntityDao<EntityWithMoney>>() {
	}).to(EntityWithMoneyDao.class);
    }
}
